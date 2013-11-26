package fr.logica.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import fr.logica.business.Context;
import fr.logica.business.Entity;

public class DbManagerUpdatable extends DbManager {

	public DbManagerUpdatable(DbConnection dbcnx, String sql) {
		super(dbcnx, sql, null, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public DbManagerUpdatable(DbConnection dbcnx, String sql, Object[] parms) {
		super(dbcnx, sql, parms, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public DbManagerUpdatable(Context ctx, DbQuery query) {
		super(ctx, query, query.getBindValues().toArray(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public void insertRow(Entity bean) throws SQLException {

		try {
			rs.moveToInsertRow();
		} catch (SQLException e) {
			throw new RuntimeException("Move to Insert row failed.", e);
		}
		putToResultSet(bean, rs, true);
		try {
			rs.insertRow();
		} catch (SQLException e) {
			throw e;
		}
	}

	public void updateRow(Entity bean) throws SQLException {

		putToResultSet(bean, rs, true);
		try {
			rs.updateRow();
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * Méthode conservée pour compatibilité avec les précédentes version.
	 * 
	 * @deprecated Préférer la méthode {@link #deleteRow(Entity)}.
	 */
	@Deprecated
	public void deleteRow() throws SQLException {
		rs.deleteRow();
	}

	public void deleteRow(Entity bean) throws SQLException {

		rs.deleteRow();
	}

	/**
	 * Gestion des id : si seulement un id est du type Compteur, alors set automatique avec la nextVal
	 * 
	 * @param entity
	 * @param dbConnection
	 * @return
	 */
	public static Entity fillAutoIncrement(Entity entity, DbConnection dbcnx) {
		for (String fieldName : entity.getModel().getFields()) {
			if (entity.getModel().isAutoIncrementField(fieldName) && entity.invokeGetter(fieldName) == null) {
				// Auto Increment field is not filled
				String selectNextValSql = "SELECT MAX(" + entity.getModel().getField(fieldName).getSqlName() + ") + 1 FROM "
						+ entity.getModel().$_getDbName();

				DbManager dbManager = new DbManager(dbcnx, selectNextValSql);
				Integer nextVal = 1;
				if (dbManager.next()) {
					nextVal = dbManager.getInt(1);
					dbManager.close();
				}
				entity.invokeSetter(fieldName, nextVal);
			}
		}
		return entity;
	}
	

}
