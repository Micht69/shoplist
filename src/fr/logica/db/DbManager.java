package fr.logica.db;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import fr.logica.application.AbstractApplicationLogic;
import fr.logica.application.ApplicationUtils;
import fr.logica.business.Constants;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.EntityField;
import fr.logica.business.EntityField.Memory;
import fr.logica.business.EntityField.SqlTypes;
import fr.logica.business.FileContainer;
import fr.logica.business.Key;
import fr.logica.business.TechnicalException;
import fr.logica.business.context.RequestContext;
import fr.logica.business.data.ListCategoryData;
import fr.logica.business.data.ListData;
import fr.logica.business.data.Row;
import fr.logica.reflect.DomainUtils;


/**
 * Utility class used to query database.
 * 
 * During construction of the object, the query is executed and the result set is initialized.
 *
 * This class is not thread-safe and therefore must not be shared between threads or put into static attributes.
 */
public class DbManager {

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(DbManager.class);

	protected PreparedStatement ps = null;
	protected ResultSet rs = null;
	protected DbQuery dbQuery = null;

	protected RequestContext ctx = null;

	/** Initialize a DbManager by executing the query and getting the result set from the database. */
	protected void init(RequestContext ctx, String sql, Object[] parms, int resultsetType, int resultsetConcurrency) {

		/* create a PreparedStatement from the SQL query */
		try {
			ps = ctx.getDbConnection().getCnx().prepareStatement(sql, resultsetType, resultsetConcurrency);
		} catch (SQLException e) {
			LOGGER.error("PrepareStatement failed.", e);
			throw new DbException("Init DbManager: PrepareStatement failed.", e);
		}

		/* apply query parameters */
		if (parms != null) {
			for (int i = 0; i < parms.length; i++) {
				try {
					if (parms[i] instanceof Timestamp) {
						ps.setTimestamp(i + 1, (Timestamp) parms[i]);

					} else if (parms[i] instanceof Time) {
						ps.setTime(i + 1, (Time) parms[i]);

					} else if (parms[i] instanceof Date) {
						ps.setDate(i + 1, new java.sql.Date(((Date) parms[i]).getTime()));

					} else {
						ps.setObject(i + 1, parms[i]);
					}
				} catch (SQLException e) {
					throw new DbException("Init DbManager: SetObject failed.", e);
				}
			}
		}

		/* Execution of the prepared statement and getting the result set */
		try {
			ps.execute();
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.error(sql);
			throw new DbException("Init DbManager: Execute failed for [" + sql + "]", e);
		}
		try {
			rs = ps.getResultSet();
		} catch (SQLException e) {
			throw new DbException("Init DbManager: Get ResultSet failed.", e);
		}
	}

	/** Internal constructor with every parameter */
	DbManager(RequestContext ctx, String sql, Object[] parms, int resultsetType, int resultsetConcurrency) {
		init(ctx, sql, parms, resultsetType, resultsetConcurrency);
	}

	/** Default : result set is forward only, no parameters */
	protected DbManager(RequestContext ctx, String sql) {
		this(ctx, sql, null, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/** Default : result set is forward only */
	protected DbManager(RequestContext ctx, String sql, Object[] parms) {
		this(ctx, sql, parms, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/** Full public constructor : initialize a DbManager with a query and a few parameters. Use it if you know what you are doing. */
	public DbManager(RequestContext ctx, DbQuery query, Object[] parms, int resultsetType, int resultsetConcurrency) {
		this.dbQuery = query;
		this.ctx = ctx;
		Object[] params = parms;
		if (query.getMainEntity() != null) {
			DomainLogic<? extends Entity> logic = DomainUtils.getLogic(query.getMainEntity());
			logic.internalDbSecure(query, ctx);
			params = query.getBindValues().toArray();
			init(ctx, query.toSelect(), params, resultsetType, resultsetConcurrency);
		}
	}

	/** Commodity public constructor, with default value for the result set parameters. */
	public DbManager(RequestContext ctx, DbQuery query) {
		this(ctx, query, query.getBindValues().toArray(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Indicates if the query still contains results.
	 * 
	 * @return {@code true} if the query still contains results, {@code false} otherwise.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public boolean next() throws DbException {
		try {
			boolean results = rs.next();
			if (!results) {
				close();
			}
			return results;
		} catch (SQLException e) {
			throw new DbException("Get ResultSet failed.", e);
		}
	}

	/**
	 * Gets a domain object from the current position of the result set.
	 * 
	 * @param tableAlias
	 *            Alias which represents the given entity in query executed by this dbManager.
	 * @param bean
	 *            Domain object to retrieve.
	 * @param ctx
	 *            Current RequestContext.
	 * @return An entity with properties filled by the result of the query executed by this dbManager..
	 */
	@SuppressWarnings("unchecked")
	public <E extends Entity> E getEntity(String tableAlias, Entity bean, RequestContext ctx) {
		E e = (E) getFromResultSet(bean, tableAlias);

		/* setting calculated values */
		DomainLogic<E> custom = (DomainLogic<E>) DomainUtils.getLogic(e.name());
		List<String> transientFields = e.getTransientFields();
		Map<String, Object> entityDump = e.dump();
		for (String transientFieldName : transientFields) {
			EntityField ef = e.getModel().getField(transientFieldName);
			if (ef.getMemory() != Memory.NEVER && !ef.isFromDatabase()) {
				Object customValue = custom.internalDoVarValue(e, transientFieldName, ctx);
				if (customValue == null) {
					customValue = custom.internalDoVarValue(entityDump, e.name(), transientFieldName, ctx);
				}
				if (customValue != null) {
					e.invokeSetter(transientFieldName, customValue);
				}
			}
		}

		return e;
	}

	/**
	 * Gets the column alias created by the query to identify in a unique way the selected value. This alias can be passed to the getIndex()
	 * method in order to get the resultSet index.
	 * 
	 * @param entityName
	 *            Entity (=table) name
	 * @param columnName
	 *            Variable (=column) name
	 * @return the alias used by the query to identify the column selected. The alias can be hashed if it's longer than 30 characters.
	 */
	public String getColumnAlias(String entityName, String columnName) {
		return dbQuery.getColumnAlias(entityName, columnName);
	}

	/**
	 * Returns the index of the variable name
	 * 
	 * @param entityName
	 *            Entity (=table) name
	 * @param columnName
	 *            Variable (=column) name
	 * @return The index of alias name in the query. This should be used to get data from result set instead of accessing RS via names.
	 */
	public int getColumnIndex(String entityName, String columnName) {
		return dbQuery.getIndex(dbQuery.getColumnAlias(entityName, columnName));
	}

	/**
	 * @param columnIndex
	 *            column to get the value from
	 * @return Boolean value of the column columnName
	 */
	public Boolean getBoolean(int columnIndex) {
		try {
			return rs.getBoolean(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use getBoolean(int columnIndex) instead. Get index with getColumnIndex method. */
	@Deprecated
	public Boolean getBoolean(String columnName) {
		try {
			return rs.getBoolean(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * @param columnIndex
	 *            column to get the value from
	 * @return String value of the column columnName
	 */
	public String getString(int columnIndex) {
		try {
			return rs.getString(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use getString(int columnIndex) instead. Get index with getColumnIndex method. */
	@Deprecated
	public String getString(String columnName) {
		try {
			return rs.getString(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public BigDecimal getBigDecimal(int columnIndex) {
		try {
			return rs.getBigDecimal(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use getBigDecimal(int columnIndex) instead. Get index with getColumnIndex method. */
	@Deprecated
	public BigDecimal getBigDecimal(String columnName) {
		try {
			return rs.getBigDecimal(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public String getStringEmpty(int columnIndex) {
		String ret = getString(columnIndex);
		return (ret == null) ? "" : ret;
	}

	/** @deprecated Use getStringEmpty(int columnIndex) instead. Get index with getColumnIndex method. */
	@Deprecated
	public String getStringEmpty(String columnName) {
		String ret = getString(columnName);
		return (ret == null) ? "" : ret;
	}

	public Date getDate(int columnIndex) {
		try {
			return rs.getDate(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use getDate(int columnIndex) instead. Get index with getColumnIndex method. */
	@Deprecated
	public Date getDate(String columnName) {
		try {
			return rs.getDate(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public Time getTime(int columnIndex) {
		try {
			return rs.getTime(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use getTime(int columnIndex) instead. Get index with getColumnIndex method */
	@Deprecated
	public Time getTime(String columnName) {
		try {
			return rs.getTime(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Integer value of the column. Unlike {@link #getInt(int)}, returns <code>null</code> if the column was <code>NULL</code> in the
	 * ResultSet.
	 */
	public Integer getInteger(int columnIndex) {
		try {
			int result = rs.getInt(columnIndex);
			if (rs.wasNull()) {
				return null;
			} else {
				return result;
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the int (primitve) value of the column. Unlike {@link #getInteger(int)}, returns <code>0</code> if the column was
	 * <code>NULL</code> in the ResultSet.
	 */
	public int getInt(int columnIndex) {
		try {
			return rs.getInt(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return 0;
		}
	}

	/** @deprecated Use getInt(int columnIndex) instead. Get index with getColumnIndex method. */
	@Deprecated
	public int getInt(String columnName) {
		try {
			return rs.getInt(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * Retrieve a byte array from the current resultSet.
	 * 
	 * @param columnIndex
	 *            Index of the column to retrieve.
	 * @return A byte array or {@code null}.
	 */
	public byte[] getBlob(int columnIndex) {
		try {
			Blob blob = rs.getBlob(columnIndex);
			return (null != blob) ? blob.getBytes(1, (int) blob.length()) : null;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use {@link #getBlob(int)} instead. Get index with {@link #getColumnIndex(String, String)} method. */
	@Deprecated
	public byte[] getBlob(String columnName) {
		try {
			Blob blob = rs.getBlob(columnName);
			return (null != blob) ? blob.getBytes(1, (int) blob.length()) : null;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Retrieve a character sequence from the current resultSet.
	 * 
	 * @param columnIndex
	 *            Index of the column to retrieve.
	 * @return A string or {@code null}.
	 */
	public String getClob(int columnIndex) {
		try {
			Clob clob = rs.getClob(columnIndex);
			return (null != clob) ? clob.getSubString(1, (int) clob.length()) : null;

		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/** @deprecated Use {@link #getClob(int)} instead. Get index with {@link #getColumnIndex(String, String)} method. */
	@Deprecated
	public String getClob(String columnName) {
		try {
			Clob clob = rs.getClob(columnName);
			return (null != clob) ? clob.getSubString(1, (int) clob.length()) : null;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Closes resources (result, query) used by this dbManager.
	 * 
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public void close() throws DbException {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			throw new DbException("Error closing resultSet", e);
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					throw new DbException("Error closing preparedStatement", e);
				}
			}
		}
	}

	public ListData getListData() throws DbException {
		ListData data = new ListData(dbQuery.getMainEntity().name());
		if (dbQuery.getCategoryBreak().size() > 0) {
			data = new ListCategoryData(dbQuery.getMainEntity().name());
			((ListCategoryData) data).setCategoryBreak(dbQuery.getCategoryBreak());
		}

		AbstractApplicationLogic appLogic = ApplicationUtils.getApplicationLogic();
		SimpleDateFormat defaultDateFormatter = new SimpleDateFormat(appLogic.getDateFormat());
		SimpleDateFormat defaultTimeFormatter = new SimpleDateFormat(appLogic.getTimeFormat());
		SimpleDateFormat defaultTimestampFormatter = new SimpleDateFormat(appLogic.getTimestampFormat());
		String queryName = dbQuery.getName();
		DomainLogic<? extends Entity> domainLogic = DomainUtils.getLogic(dbQuery.getMainEntity());

		int rownum = dbQuery.getMinRownum();
		try {
			while (next()) {
				Row r = getNextRow(domainLogic, queryName, defaultDateFormatter, defaultTimeFormatter, defaultTimestampFormatter);
				r.put(Constants.RESULT_ROWNUM, rownum++);
				data.add(r);
			}
		} catch (SQLException ex) {
			throw new DbException(ex.getMessage(), ex);
		} finally {
			close();
		}
		return data;
	}

	private <E extends Entity> Row getNextRow(DomainLogic<E> domainLogic, String queryName,
			SimpleDateFormat defaultDateFormatter, SimpleDateFormat defaultTimeFormatter, SimpleDateFormat defaultTimestampFormatter)
			throws SQLException {

		Row row = new Row();

		// Pour chaque ligne du result set, on construit la PK
		Key pk = dbQuery.getMainEntity().getPrimaryKey();
		pk.nullify();

		// Pour chaque variable attendue, on récupère le nom et le type SQL.
		for (DbQuery.Var var : dbQuery.getOutVars()) {
			if (!var.model.isFromDatabase()) {
				continue;
			}

			/* Get the rsResult (object in the resultset) and the rsResultDisplay (object converted to a displayable shape, most likely a String) */
			Object rsResult = null;
			Object rsResultDisplay = null;
			String sqlName = var.tableId + "_" + var.model.getSqlName();
			int index = dbQuery.getIndex(sqlName);
			try {
				if (var.model.getSqlType() == SqlTypes.INTEGER) {
					rsResult = rs.getInt(index);
					if (rs.wasNull()) {
						rsResult = null;
					}
					if (var.model.hasDefinedValues()) {
						rsResultDisplay = var.model.getDefinedLabel(rsResult, ctx.getSessionContext().getLocale());
					}
				} else if (var.model.getSqlType() == SqlTypes.BOOLEAN) {
					rsResult = rs.getBoolean(index);
					if (var.model.hasDefinedValues()) {
						rsResultDisplay = var.model.getDefinedLabel(rsResult, ctx.getSessionContext().getLocale());
					}
				} else if (var.model.getSqlType() == SqlTypes.VARCHAR2 || var.model.getSqlType() == SqlTypes.CHAR) {
					rsResult = rs.getString(index);
					if (var.model.hasDefinedValues()) {
						rsResultDisplay = var.model.getDefinedLabel(rsResult, ctx.getSessionContext().getLocale());
					}
				} else if (var.model.getSqlType() == SqlTypes.DATE) {
					rsResult = rs.getDate(index);
					if (rsResult != null) {
						rsResultDisplay = defaultDateFormatter.format(rsResult);
					}
				} else if (var.model.getSqlType() == SqlTypes.TIME) {
					rsResult = rs.getTime(index);
					if (rsResult != null) {
						rsResultDisplay = defaultTimeFormatter.format(rsResult);
					}
				} else if (var.model.getSqlType() == SqlTypes.TIMESTAMP) {
					try {
						rsResult = rs.getTimestamp(index);
					} catch (SQLException ex) {
						if (DbConnection.getDbType() == DbConnection.Type.MySQL && "S1009".equals(ex.getSQLState())) {
							// MySQL handles null timestamp in a very strange way.
							// This can be avoided by setting zeroDateTimeBehavior=convertToNull in jdbc connection string.
							// If the parameter is not set, it may break.
							rsResult = null;
						} else {
							throw ex;
						}
					}
					if (rsResult != null) {
						rsResultDisplay = defaultTimestampFormatter.format(rsResult);
					}
				} else if (var.model.getSqlType() == SqlTypes.DECIMAL) {
					rsResult = rs.getBigDecimal(index);
				} else {
					LOGGER.warn("Type non géré !!! : " + var.model.getSqlType());
				}

			} catch (SQLException ex) {
				throw new TechnicalException("Erreur à la récupération du champ " + sqlName, ex);
			}

			// Compute display value
			if (rsResultDisplay == null) {
				/* only case where rsResultDisplay is not a String */
				rsResultDisplay = (rsResult == null ? "" : rsResult);
			}

			if (dbQuery.getMainEntityAlias().equals(var.tableId)
					&& dbQuery.getMainEntity().getModel().getKeyModel().getFields().contains(var.name)) {
				pk.setValue(var.name, rsResult);
			}

			row.put(var.getColumnAlias(), rsResultDisplay);
		}

		/* manage calculated values */
		for (DbQuery.Var var : dbQuery.getOutVars()) {
			if (!var.model.isFromDatabase() && var.model.getMemory() != Memory.NEVER) {
				Object uiVarValue = domainLogic.internalUiListVarValue(row, queryName, dbQuery.getEntity(var.tableId), var.name, ctx);
				if (uiVarValue == null) {
					uiVarValue = domainLogic.internalDoVarValue(row, dbQuery.getEntity(var.tableId), var.name, ctx);
				}
				row.put(var.tableId + "_" + var.name, uiVarValue);
			}
		}
		row.put(Constants.RESULT_PK, pk);
		return row;
	}

	/** Returns the number of records in database corresponding to the query executed by this dbManager. */
	public int count() throws DbException {
		dbQuery.setCount(true);
		DbManager mgr = new DbManager(ctx, dbQuery);
		int count = 0;

		try {
			if (mgr.next()) {
				// La requête est en mode "comptage", il n'y a qu'une colonne qui
				// contient 1 seul entier, le nombre de résultats.
				count = mgr.getInt(1);
			}
		} finally {
			mgr.close();
		}

		dbQuery.setCount(false);
		return count;
	}

	/**
	 * Récupère le type de données des champs de la requête
	 * 
	 * @return Map <champ,typeSql>
	 */
	public Map<String, SqlTypes> getTypeDonnes(DbQuery dbquery) {
		Map<String, SqlTypes> map = new HashMap<String, SqlTypes>();
		// Pour chaque variable sélectionnée, on récupère le nom et le type SQL.
		for (DbQuery.Var var : dbQuery.getOutVars()) {
			String sqlName = var.model.getSqlName();
			map.put(sqlName, var.model.getSqlType());
		}
		return map;
	}

	protected Entity getFromResultSet(Entity entity, String tableAlias) {
		for (Field field : entity.getFields()) {
			try {
				String methodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
				Method method = entity.getClass().getDeclaredMethod(methodName, field.getType());
				String className = field.getType().getName();

				EntityField eField = entity.getModel().getField(field.getName());
				String dbName = tableAlias + "_" + eField.getSqlName();
				Integer index = (dbQuery == null ? null : dbQuery.getIndex(dbName));
				Object rsResult;

				if (byte[].class.getName().equals(className)) {
					rsResult = (index == null ? rs.getBlob(dbName) : rs.getBlob(index));
				} else if (eField.getSqlType() == SqlTypes.CLOB) {
					rsResult = (index == null ? rs.getClob(dbName) : rs.getClob(index));
				} else {
					rsResult = (index == null ? rs.getObject(dbName) : rs.getObject(index));
				}

				if (rsResult == null || rs.wasNull()) {
					// Null values are processed here
					method.invoke(entity, (Object) null);

					if (eField.getSqlType() == SqlTypes.BLOB || eField.getSqlType() == SqlTypes.CLOB) {
						method = entity.getClass().getDeclaredMethod(methodName + "Container", FileContainer.class);
						FileContainer container = new FileContainer();
						container.setNull(true);
						method.invoke(entity, container);
					}

				} else if (Date.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getDate(dbName) : rs.getDate(index)));

				} else if (Timestamp.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getTimestamp(dbName) : rs.getTimestamp(index)));

				} else if (Time.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getTime(dbName) : rs.getTime(index)));

				} else if (Integer.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getInt(dbName) : rs.getInt(index)));

				} else if (Long.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getBigDecimal(dbName) : rs.getBigDecimal(index)).longValue());

				} else if (Boolean.class.getName().equals(className)) {
					int bVal = (index == null ? rs.getInt(dbName) : rs.getInt(index));
					if (bVal == 1) {
						method.invoke(entity, Boolean.TRUE);
					} else {
						method.invoke(entity, Boolean.FALSE);
					}

				} else if (eField.getSqlType() == SqlTypes.BLOB || eField.getSqlType() == SqlTypes.CLOB) {
					method = entity.getClass().getDeclaredMethod(methodName + "Container", FileContainer.class);
					FileContainer container = new FileContainer();
					method.invoke(entity, container);

				} else if (Geometry.class.equals(field.getType())) {
					rsResult = (index == null ? rs.getString(dbName) : rs.getString(index));
					WKTReader reader = new WKTReader();
					Geometry geom = reader.read((String) rsResult);
					method.invoke(entity, geom);
				} else {
					method.invoke(entity, rsResult);
				}
			} catch (SQLException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable " + field.getName(), e);
			} catch (SecurityException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable " + field.getName(), e);
			} catch (NoSuchMethodException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable " + field.getName(), e);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable " + field.getName(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable " + field.getName(), e);
			} catch (InvocationTargetException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable " + field.getName(), e);
			} catch (com.vividsolutions.jts.io.ParseException e) {
				LOGGER.error("Erreur lors de la mise à jour de la variable geometry " + field.getName(), e);
			}
		}
		return entity;
	}

	protected void putToResultSet(Entity entity, ResultSet rs, boolean updateNullValues) {
		for (Field field : entity.getFields()) {
			try {
				Object result = getValue(entity, field);

				EntityField eField = entity.getModel().getField(field.getName());
				String sqlName = eField.getSqlName();
				if (eField.isTransient()) {
					// Skip Memory Vars from all types
					continue;
				} 
				if (eField.getSqlType() == SqlTypes.BOOLEAN) {
					Boolean bResult = null;
					if (result instanceof String) {
						// On regarde si le champ est un booleen
						bResult = Boolean.valueOf((String) result);
					} else if (result instanceof Boolean) {
						bResult = (Boolean) result;
					}
					if (bResult != null) {
						if (bResult.booleanValue()) {
							rs.updateInt(sqlName, 1);
						} else {
							rs.updateInt(sqlName, 0);
						}
						continue;
					}
					// FIXME : Time et Timestamp OK ?
				} else if (result instanceof java.util.Date && !(result instanceof Time) && eField.getSqlType() != SqlTypes.TIMESTAMP) {
					java.util.Date date = (java.util.Date) result;
					result = new java.sql.Date(date.getTime());
				} else if (result instanceof Geometry) {
					result = ((Geometry) result).toText();
				} else if (null == result && (eField.getSqlType() == SqlTypes.BLOB || eField.getSqlType() == SqlTypes.CLOB)) {
					FileContainer container = (FileContainer) entity.invokeGetter(field.getName() + "Container");
					if (null != container && !container.isNull()) {
						/* BLOB data exists but has not been loaded, so update is skipped. */
						continue;
					}
					// It seems updateBlob does not work with MySQL in an updatable resultset, see http://bugs.mysql.com/bug.php?id=53002,
					// Fortunately, updateObject is working (but it may be not efficient).
				}
				if (result == null) {
					if (updateNullValues) {
						rs.updateNull(sqlName);
					}
				} else {
					rs.updateObject(sqlName, result);
				}
			} catch (SQLException e) {
				String msg = "Update failed for variable " + field.getName() + " in entity " + entity.name();
				LOGGER.error(msg, e);
				throw new DbException(msg, e);
			} catch (IllegalArgumentException e) {
				String msg = "Update failed for variable " + field.getName() + " in entity " + entity.name();
				LOGGER.error(msg, e);
				throw new TechnicalException(msg, e);
			}
		}
	}

	protected Object getValue(Entity entity, Field field) {
		try {
			String methodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method method = entity.getClass().getMethod(methodName);
			Object result = method.invoke(entity);
			return result;
		} catch (IllegalArgumentException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (IllegalAccessException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (InvocationTargetException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (SecurityException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (NoSuchMethodException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		}
	}

}
