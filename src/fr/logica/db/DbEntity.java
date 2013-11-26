package fr.logica.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.Context;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.EntityField;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.Key;
import fr.logica.business.Link;
import fr.logica.business.LinkModel;
import fr.logica.business.Results;
import fr.logica.business.TechnicalException;
import fr.logica.db.DbFactory;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Join;
import fr.logica.reflect.DomainUtils;

/**
 * Utility class used to query database for a given domain object.
 */
public class DbEntity {

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param entity
	 *            Entity linked to the domain object to retrieve.
	 * @param linkModel
	 *            Model object representing the link between the given entity and the domain object to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */

	public Entity getRef(Entity e, LinkModel linkModel, Context ctx) throws DbException {
		Key foreignKey = e.getForeignKey(linkModel.getKeyName());
		if (!foreignKey.isFull()) {
			return null;
		}
		// FIXME we assume that target key is the primary key !
		Key primaryKey = new Key(linkModel.getRefEntityName());
		primaryKey.setValue(foreignKey);
		return get(linkModel.getRefEntityName(), primaryKey, ctx);
	}

	/**
	 * Retrieves a domain object by its primary key.
	 * 
	 * @param entityName
	 *            Name of the domain object to retrieve.
	 * @param primaryKey
	 *            Primary key of the domain object to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return The domain object identified by the primary key or {@code null} if the domain object is not found.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */

	public Entity get(String domainName, Key primaryKey, Context ctx) throws DbException {
		if (primaryKey == null || domainName == null) {
			throw new TechnicalException("Unable to find domain object of class " + domainName + " for primary key : " + primaryKey);
		}
		DbQuery dbQuery = DB.createQuery(ctx, domainName, "T1");
		dbQuery.addCondKey(primaryKey, "T1");
		DbManager mgr = null;
		Entity e = null;
		try {
			mgr = new DbManager(ctx, dbQuery);

			if (mgr.next()) {
				e = mgr.getEntity("T1", DomainUtils.newDomain(domainName), ctx);
			}
		} finally {
			if (null != mgr) {
				mgr.close();
			}
		}
		return e;
	}

	/**
	 * Insert a domain object into database. Will fail if entity already exists in database.
	 * 
	 * @param entity
	 *            Domain object to persist.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs or if entity already exists in database
	 */

	public void insert(Entity entity, Context ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			DbManagerUpdatable.fillAutoIncrement(entity, ctx.getConnection());

			DbQuery query = DB.createQuery(ctx, entity.$_getName(), "T01");
			query.addCondKey(entity.getPrimaryKey(), "T01");
			query.setForUpdate(true);

			dbMgr = new DbManagerUpdatable(ctx, query);
			dbMgr.insertRow(entity);

		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Update a domain object into database. Will fail if entity does not exists in database.
	 * 
	 * @param entity
	 *            Domain object to persist.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs or if entity does not exists in database
	 */

	public void update(Entity entity, Context ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			DbManagerUpdatable.fillAutoIncrement(entity, ctx.getConnection());

			DbQuery query = DB.createQuery(ctx, entity.$_getName(), "T01");
			query.addCondKey(entity.getPrimaryKey(), "T01");
			query.setForUpdate(true);

			dbMgr = new DbManagerUpdatable(ctx, query);
			if (dbMgr.rs.next()) {
				dbMgr.updateRow(entity);
			}

		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Persist a domain object into database
	 * 
	 * @param domain
	 *            The Domain object to persist
	 * @param ctx
	 *            Current context
	 * @return <code>true</code> if domain object has been inserted, <code>false</code> if it has been updated.
	 */

	public boolean persist(Entity domain, Context ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			DbManagerUpdatable.fillAutoIncrement(domain, ctx.getConnection());

			DbQuery query = DB.createQuery(ctx, domain.$_getName(), "T01");
			query.addCondKey(domain.getPrimaryKey(), "T01");
			query.setForUpdate(true);

			dbMgr = new DbManagerUpdatable(ctx, query);
			if (dbMgr.rs.next()) {
				dbMgr.updateRow(domain);
				return false;
			} else {
				dbMgr.insertRow(domain);
				return true;
			}

		} catch (SQLException sqlEx) {
			System.out.println(domain.$_getName());
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Persists entities' associations into database.
	 * 
	 * @param baseBean
	 *            Domain object linked to associations to persist.
	 * @param linkName
	 *            Name of the link between {@code baseBean} and associations to persist.
	 * @param selectedKeys
	 *            Keys of the associations to persist.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */

	public void persistAssociations(Entity baseBean, String linkName, List<Key> selectedKeys, Context ctx) throws DbException {
		Link link = baseBean.getLink(linkName);
		EntityModel entityModel = EntityManager.getEntityModel(link.getModel().getEntityName());
		String associatedLinkName = entityModel.getAssociatedLink(linkName);
		String associatedKeyName = entityModel.getLinkModel(associatedLinkName).getKeyName();

		for (Key selectedKey : selectedKeys) {
			// For each selected element we'll create an association.
			Entity association = DomainUtils.newDomain(link.getModel().getEntityName());
			association.setForeignKey(associatedKeyName, selectedKey);
			association.setForeignKey(link.getModel().getKeyName(), baseBean.getPrimaryKey());
			DB.persist(association, new Action(Constants.SELECT, Constants.SELECT), ctx);
		}
	}

	/**
	 * Deletes a domain object into database.
	 * 
	 * @param entity
	 *            Domain object to delete.
	 * @param ctx
	 *            Current context.
	 * @return {@code true} if the given entity has been deleted, {@code false} otherwise.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public boolean remove(Entity entity, Context ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			boolean removed = false;
			DbQuery query = DB.createQuery(ctx, entity.$_getName(), "T01");
			query.addCondKey(entity.getPrimaryKey(), "T01");
			query.setForUpdate(true);
			dbMgr = new DbManagerUpdatable(ctx, query);
			if (dbMgr.next()) {
				dbMgr.deleteRow(entity);
				removed = true;
			}
			return removed;
		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Removes entities' associations into database.
	 * 
	 * @param baseBean
	 *            Domain object linked to associations to remove.
	 * @param linkName
	 *            Name of the link between {@code baseBean} and associations to remove.
	 * @param selectedKeys
	 *            Keys of the associations to remove.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public void removeAssociations(Entity baseBean, String linkName, List<Key> selectedKeys, Context ctx) throws DbException {
		Link link = baseBean.getLink(linkName);
		EntityModel entityModel = EntityManager.getEntityModel(link.getModel().getEntityName());
		String associatedLinkName = entityModel.getAssociatedLink(linkName);
		String associatedKeyName = entityModel.getLinkModel(associatedLinkName).getKeyName();

		for (Key selectedKey : selectedKeys) {
			// For each selected element we'll create an association.
			Entity association = DomainUtils.newDomain(link.getModel().getEntityName());
			association.setForeignKey(associatedKeyName, selectedKey);
			association.setForeignKey(link.getModel().getKeyName(), baseBean.getPrimaryKey());
			DB.remove(association, new Action(Constants.DETACH, Constants.DETACH), ctx);
		}
	}

	/**
	 * Retrieves a list of domain objects linked to the given entity.
	 * 
	 * @param entity
	 *            Entity linked to the domain objects to retrieve.
	 * @param linkName
	 *            Name of the link between the given entity and the domain objects to retrieve.
	 * @param queryName
	 *            Name of the query to use.
	 * @param ctx
	 *            Current context.
	 * @return A list of domain objects linked to the given entity or an empty list.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */

	public Results getList(Entity entity, String linkName, String queryName, Context ctx) throws DbException {
		DbQuery dbQuery = getLinkQuery(entity, linkName, queryName, false, ctx);
		DbManager dbManager = null;

		try {
			dbManager = new DbManager(ctx, dbQuery);
			Results results = dbManager.toResults();
			results.setResultSetCount(dbManager.count());
			DomainLogic<? extends Entity> domainLogic = DomainUtils.getLogic(dbQuery.getMainEntity());
			EntityModel eModel = EntityManager.getEntityModel(entity.$_getName());
			LinkModel backRefModel = eModel.getBackRefModel(linkName);

			for (DbQuery.Var var : dbQuery.getOutVars()) {
				String columnKey = var.tableId + "_" + var.name;
				results.getTitles().put(columnKey, domainLogic.internalUiListColumnCaption(dbQuery, backRefModel, columnKey, ctx));
			}
			return results;

		} catch (Exception e) {
			throw new DbException(e.getMessage(), e);
		} finally {
			if (null != dbManager) {
				dbManager.close();
			}
		}
	}

	/**
	 * Retrieves a list of domain objects linked to the given entity.
	 * 
	 * @param entity
	 *            Entity linked to the domain objects to retrieve.
	 * @param linkName
	 *            Name of the link between the given entity and the domain objects to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return A list of domain objects linked to the given entity or an empty list.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */

	public List<Entity> getLinkedEntities(Entity entity, String linkName, Context ctx) throws DbException {
		DbQuery dbQuery = getLinkQuery(entity, linkName, null, true, ctx);
		// dbQuery.setForUpdate(true);
		List<Entity> list = new ArrayList<Entity>();
		DbManager dbManager = null;

		try {
			dbManager = new DbManager(ctx, dbQuery);
			while (dbManager.next()) {
				list.add(dbManager.getEntity(dbQuery.getMainEntityAlias(), DomainUtils.newDomain(dbQuery.getMainEntity().$_getName()), ctx));
			}
			return list;

		} catch (Exception e) {
			throw new DbException(e.getMessage(), e);
		} finally {
			if (null != dbManager) {
				dbManager.close();
			}
		}

	}

	public List<Entity> getLinkedEntities(Entity entity, String linkName, String queryName, Context ctx) throws DbException {
		DbQuery dbQuery = getLinkQuery(entity, linkName, queryName, queryName == null, ctx);
		List<Entity> list = new ArrayList<Entity>();
		DbManager dbManager = null;

		try {
			dbManager = new DbManager(ctx, dbQuery);
			while (dbManager.next()) {
				list.add(dbManager.getEntity(dbQuery.getMainEntityAlias(), DomainUtils.newDomain(dbQuery.getMainEntity().$_getName()), ctx));
			}
			return list;

		} catch (Exception e) {
			throw new DbException(e.getMessage(), e);
		} finally {
			if (null != dbManager) {
				dbManager.close();
			}
		}
	}

	/**
	 * @param e
	 * @param linkName
	 * @param queryName
	 * @param selectAll
	 * @param ctx
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DbQuery getLinkQuery(Entity e, String linkName, String queryName, boolean selectAll, Context ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.$_getName());
		if (!eModel.getBackRefNames().contains(linkName)) {
			throw new TechnicalException("Link " + linkName + " is not a backRef of " + e.$_getName());
		}
		LinkModel backRefModel = eModel.getBackRefModel(linkName);
		Key foreignKey = EntityManager.buildForeignKey(backRefModel.getEntityName(), e.getPrimaryKey(), linkName);
		boolean isAssociative = EntityManager.getEntityModel(backRefModel.getEntityName()).isAssociative();

		DbQuery dbQuery;
		if (isAssociative) {
			String associatedLinkName = EntityManager.getEntityModel(backRefModel.getEntityName()).getAssociatedLink(linkName);
			String associatedEntityName = EntityManager.getEntityModel(backRefModel.getEntityName()).getLinkModel(associatedLinkName)
					.getRefEntityName();
			if (selectAll) {
				dbQuery = DB.createQuery(ctx, associatedEntityName);
			} else {
				dbQuery = DB.getQuery(ctx, associatedEntityName, queryName);
			}
		} else {
			if (selectAll) {
				dbQuery = DB.createQuery(ctx, backRefModel.getEntityName());
			} else {
				dbQuery = DB.getQuery(ctx, backRefModel.getEntityName(), queryName);
			}
		}
		if (foreignKey != null && !foreignKey.isNull()) {
			if (isAssociative) {
				dbQuery.addEntity(backRefModel.getEntityName(), "ASSO_NN", Join.STRICT);
				dbQuery.addCondKey(foreignKey, "ASSO_NN");
			} else {
				dbQuery.addCondKey(foreignKey, dbQuery.getAlias(backRefModel.getEntityName()));
			}
		}
		// FIXME bazint layer-sql Appeler la custom-class de l'entité cible ?
		((DomainLogic<Entity>) DomainUtils.getLogic(backRefModel.getEntityName())).internalUiListPrepare(dbQuery,
				DomainUtils.newDomain(backRefModel.getEntityName()), null, ctx);
		return dbQuery;
	}

	public byte[] getLobContent(Context ctx, Entity entity, String propertyName) {
		byte[] content = null;
		String alias = "T01";
		DbQuery query = new DbFactory().createDbQuery(ctx, entity.$_getName(), alias);
		query.addColumn(propertyName, alias);
		query.addCondKey(entity.getPrimaryKey(), alias);
		DbManager manager = null;
		EntityField lobField = entity.getModel().getField(propertyName);

		try {
			manager = new DbManager(ctx, query);

			if (manager.next()) {

				if ("CLOB".equals(lobField.getSqlType())) {
					String clob = manager.getClob(manager.getColumnIndex(entity.$_getName(), propertyName));
					if (null != clob) {
						content = clob.getBytes();
					}

				} else {
					content = manager.getBlob(manager.getColumnIndex(entity.$_getName(), propertyName));
				}
			}

		} finally {
			if (manager != null)
				manager.close();
		}
		return content;
	}

}
