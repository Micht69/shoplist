package fr.logica.db;

import java.util.List;

import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.Context;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.LinkModel;
import fr.logica.business.Results;
import fr.logica.business.TechnicalException;
import fr.logica.reflect.DomainUtils;

public class DB {

	private final static DbFactory dbFactory;

	static {
		String className = Constants.DB_PACKAGE + "." + "DbFactory";

		try {
			dbFactory = (DbFactory) Class.forName(className).newInstance();

		} catch (InstantiationException e) {
			throw new RuntimeException("Impossible d'instancier la classe " + className);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Impossible d'instancier la classe " + className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Impossible d'instancier la classe " + className);
		}
	}

	public static DbQuery createQuery(Context ctx, String entityName) throws TechnicalException {
		return dbFactory.createDbQuery(ctx, entityName);
	}

	public static DbQuery createQuery(Context ctx, String entityName, String alias) throws TechnicalException {
		return dbFactory.createDbQuery(ctx, entityName, alias);
	}

	public static DbQuery getQuery(Context ctx, String entityName, String queryName) throws TechnicalException {
		return dbFactory.getDbQuery(ctx, entityName, queryName);
	}

	public static DbQuery getLinkQuery(Context ctx, Entity entity, String linkName, String queryName) throws TechnicalException {
		return dbFactory.createDbEntity().getLinkQuery(entity, linkName, queryName, (queryName == null), ctx);
	}

	public static DbManager createDbManager(Context ctx, DbQuery query) throws TechnicalException {
		return dbFactory.createDbManager(ctx, query);
	}

	public static DbEntity createDbEntity() throws TechnicalException {
		return dbFactory.createDbEntity();
	}

	public static DbConnection createDbConnection() throws TechnicalException {
		return dbFactory.createDbConnection();
	}

	public static <E extends Entity> Entity getRef(Entity e, LinkModel linkModel, Context ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(linkModel.getRefEntityName()));
		E ref = (E) dbFactory.createDbEntity().getRef(e, linkModel, ctx);
		if (ref != null) {
			logic.internalDbPostLoad(ref, new Action(Constants.DUMMY, Constants.DUMMY), ctx);
		}
		return ref;
	}

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param entity
	 *            Entity linked to the domain object to retrieve.
	 * @param linkName
	 *            Name of the link between the given entity and the domain object to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public static Entity getRef(Entity e, String linkName, Context ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.$_getName());
		if (!eModel.getLinkNames().contains(linkName)) {
			throw new DbException("Link " + linkName + " is not a link of " + e.$_getName());
		}
		return getRef(e, eModel.getLinkModel(linkName), ctx);
	}

	/** Retrieves one entity from the database, given its primary key. */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E get(String domainName, Key primaryKey, Context ctx) {
		Action action = new Action(Constants.DUMMY, Constants.DUMMY);
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domainName));
		E e = (E) dbFactory.createDbEntity().get(domainName, primaryKey, ctx);
		if (e != null) {
			logic.internalDbPostLoad(e, action, ctx);
		}
		return e;
	}

	/** Retrieves one entity from the database, given its primary key. */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E get(String domainName, Key primaryKey, Action action, Context ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domainName));
		E e = (E) dbFactory.createDbEntity().get(domainName, primaryKey, ctx);
		if (e != null) {
			logic.internalDbPostLoad(e, action, ctx);
		}
		return e;
	}

	public static <E extends Entity> boolean persist(E domain, Context ctx) {
		return persist(domain, new Action(Constants.ACTION_MODIFY, Constants.MODIFY), ctx);
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
	@SuppressWarnings("unchecked")
	public static <E extends Entity> boolean persist(E domain, Action action, Context ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(ctx.getMessages());
		}
		boolean insert = dbFactory.createDbEntity().persist(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
		return insert;
	}
	
	public static <E extends Entity> void insert(E domain, Context ctx) {
		insert(domain, new Action(Constants.ACTION_CREATE, Constants.CREATE), ctx);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> void insert(E domain, Action action, Context ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(ctx.getMessages());
		}
		dbFactory.createDbEntity().insert(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
	}
	
	public static <E extends Entity> void update(E domain, Context ctx) {
		update(domain, new Action(Constants.ACTION_MODIFY, Constants.MODIFY), ctx);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> void update(E domain, Action action, Context ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(ctx.getMessages());
		}
		dbFactory.createDbEntity().update(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
	}
	
	public static <E extends Entity> void remove(E domain, Context ctx) {
		remove(domain, new Action(Constants.ACTION_DELETE, Constants.DELETE), ctx);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> boolean remove(E domain, Action action, Context ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnDelete(domain, action, ctx);
		boolean delete = dbFactory.createDbEntity().remove(domain, ctx);
		logic.internalDbPostDelete(domain, action, ctx);
		return delete;
	}

	public static Results getList(Entity e, String linkName, String queryName, Context ctx) {
		return dbFactory.createDbEntity().getList(e, linkName, queryName, ctx);
	}

	public static List<Entity> getLinkedEntities(Entity e, String linkName, Context ctx) {
		return dbFactory.createDbEntity().getLinkedEntities(e, linkName, ctx);
	}

	public static List<Entity> getLinkedEntities(Entity e, String linkName, String queryName, Context ctx) {
		return dbFactory.createDbEntity().getLinkedEntities(e, linkName, ctx);
	}

	public static void persistAssociations(Entity baseBean, String linkName, List<Key> selectedKeys, Context ctx) throws DbException {
		dbFactory.createDbEntity().persistAssociations(baseBean, linkName, selectedKeys, ctx);
	}

	public static void removeAssociations(Entity baseBean, String linkName, List<Key> selectedKeys, Context ctx) throws DbException {
		dbFactory.createDbEntity().removeAssociations(baseBean, linkName, selectedKeys, ctx);
	}
	
	public static int count(DbQuery countQuery, Context ctx) {
		DbManager mgr = createDbManager(ctx, countQuery);
		try {
			return mgr.count();
		} finally {
			mgr.close();
		}
	}
}
