package fr.logica.db;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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

		rs.moveToInsertRow();
		putToResultSet(bean, rs, true);
		rs.insertRow();
	}

	public void updateRow(Entity bean) throws SQLException {

		putToResultSet(bean, rs, true);
		rs.updateRow();
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
	 * @param entity Current entity
	 * @param ctx Current request context
	 * @return the given entity with autoincrement fields updated
	 */
	public static Entity fillAutoIncrement(Entity entity, RequestContext ctx) {
		for (String fieldName : entity.getModel().getFields()) {
			if (entity.getModel().isAutoIncrementField(fieldName) && entity.invokeGetter(fieldName) == null) {
				// Auto Increment field is not filled
				// compute delta here to avoid context alive when getting count from db but dead when computing delta
				int delta = concurrentAccesses(entity.getModel().name(), ctx);
				String selectNextValSql = "SELECT MAX(" + entity.getModel().getField(fieldName).getSqlName() + ") + 1 FROM "
						+ entity.getModel().dbName();
				DbManager dbManager = new DbManager(ctx, selectNextValSql);
				Integer nextVal = 1;
				if (dbManager.next()) {
					nextVal = dbManager.getInt(1) + delta;
					dbManager.close();
				}
				entity.invokeSetter(fieldName, nextVal);
			}
		}
		return entity;
	}

	private static Map<String, List<WeakReference<RequestContext>>> activeContexts = new Hashtable<String, List<WeakReference<RequestContext>>>();

	/**
	 * get the number of concurrent access to a given entity
	 * 
	 * @param entityName
	 * @param ctx
	 * @return count of concurrent access
	 */
	private static synchronized int concurrentAccesses(String entityName, RequestContext ctx) {
		int delta = 0;
		List<WeakReference<RequestContext>> contexts;
		if (activeContexts.keySet().contains(entityName)) {
			contexts = activeContexts.get(entityName);
			// remove obsolete contexts
			Iterator<WeakReference<RequestContext>> it = contexts.iterator();
			while (it.hasNext()) {
				WeakReference<RequestContext> weakRequestContext = it.next();
				RequestContext rc = weakRequestContext.get();
				if (rc != null)
					try {
						Connection cxn = (rc.hasDbConnection() ? rc.getDbConnection().getCnx() : null);
						if (cxn == null || cxn.isClosed()) {
							// connection closed --> remove context
							weakRequestContext.clear();
							it.remove();
						}
					} catch (SQLException e) {
						// do nothing, context will be removed when garbaged
					}
			}
			// get delta from unique contexts
			if (!contexts.contains(ctx)) {
				delta = contexts.size();
				contexts.add(new WeakReference<RequestContext>(ctx));
			}
		} else {
			contexts = new ArrayList<WeakReference<RequestContext>>(2);
			contexts.add(new WeakReference<RequestContext>(ctx));
			activeContexts.put(entityName, contexts);
		}
		return delta;
	}



}
