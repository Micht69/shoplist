package fr.logica.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import fr.logica.business.Entity;
import fr.logica.business.context.RequestContext;

public class DbManagerUpdatable extends DbManager {

	public DbManagerUpdatable(RequestContext ctx, String sql) {
		super(ctx, sql, null, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public DbManagerUpdatable(RequestContext ctx, String sql, Object[] parms) {
		super(ctx, sql, parms, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public DbManagerUpdatable(RequestContext ctx, DbQuery query) {
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
	 * M�thode conserv�e pour compatibilit� avec les pr�c�dentes version.
	 * 
	 * @deprecated Pr�f�rer la m�thode {@link #deleteRow(Entity)}.
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
	 * @param RequestContext
	 * @return
	 */
	public static Entity fillAutoIncrement(Entity entity, RequestContext ctx) {
		for (String fieldName : entity.getModel().getFields()) {
			if (entity.getModel().isAutoIncrementField(fieldName) && entity.invokeGetter(fieldName) == null) {
				// Auto Increment field is not filled
				String selectNextValSql = "SELECT MAX(" + entity.getModel().getField(fieldName).getSqlName() + ") + 1 FROM "
						+ entity.getModel().dbName();

				DbManager dbManager = new DbManager(ctx, selectNextValSql);
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
