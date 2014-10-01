package fr.logica.db;

import java.util.ArrayList;
import java.util.List;

import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.LinkModel;
import fr.logica.business.TechnicalException;
import fr.logica.business.context.RequestContext;
import fr.logica.reflect.DomainUtils;
import fr.logica.ui.Message;

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

	public static DbQuery createQuery(RequestContext ctx, String entityName) throws TechnicalException {
		return dbFactory.createDbQuery(ctx, entityName);
	}

	public static DbQuery createQuery(RequestContext ctx, String entityName, String alias) throws TechnicalException {
		return dbFactory.createDbQuery(ctx, entityName, alias);
	}

	public static DbQuery getQuery(RequestContext ctx, String entityName, String queryName) throws TechnicalException {
		return dbFactory.getDbQuery(ctx, entityName, queryName);
	}

	public static DbQuery getLinkQuery(RequestContext ctx, Entity entity, String linkName, String queryName) throws TechnicalException {
		if (entity == null) {
			return null;
		}
		DbQuery dbQuery = dbFactory.createDbEntity().getLinkQuery(entity, linkName, queryName, (queryName == null), ctx);
		String sourceEntityName = entity.getModel().getBackRefModel(linkName).getEntityName();
		((DomainLogic<Entity>) DomainUtils.getLogic(sourceEntityName)).internalUiListPrepare(dbQuery,
				entity, linkName, ctx);
		return dbQuery;
	}

	public static DbManager createDbManager(RequestContext ctx, DbQuery query) throws TechnicalException {
		return dbFactory.createDbManager(ctx, query);
	}

	public static DbEntity createDbEntity() throws TechnicalException {
		return dbFactory.createDbEntity();
	}

	public static DbConnection createDbConnection() throws TechnicalException {
		return dbFactory.createDbConnection();
	}

	public static <E extends Entity> Entity getRef(Entity e, LinkModel linkModel, RequestContext ctx) {
		return getRef(e, linkModel, new Action(Constants.DUMMY, Constants.DUMMY), ctx);
	}

	public static <E extends Entity> Entity getRef(Entity e, LinkModel linkModel, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(linkModel.getRefEntityName()));
		E ref = (E) dbFactory.createDbEntity().getRef(e, linkModel, ctx);
		if (ref != null) {
			logic.internalDbPostLoad(ref, action, ctx);
		}
		return ref;
	}

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param e Entity linked to the domain object to retrieve.
	 * @param linkName Name of the link between the given entity and the domain object to retrieve.
	 * @param ctx Current context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 * @throws DbException Exception thrown if an error occurs.
	 */
	public static Entity getRef(Entity e, String linkName, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());
		if (!eModel.getLinkNames().contains(linkName)) {
			throw new DbException("Link " + linkName + " is not a link of " + e.name());
		}
		return getRef(e, eModel.getLinkModel(linkName), ctx);
	}

	public static Entity getRef(Entity e, String linkName, Action action, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());
		if (!eModel.getLinkNames().contains(linkName)) {
			throw new DbException("Link " + linkName + " is not a link of " + e.name());
		}
		return getRef(e, eModel.getLinkModel(linkName), action, ctx);
	}

	public static Entity getUniqueBackRef(Entity e, String backRefName, Action action, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());
		if (!eModel.getBackRefNames().contains(backRefName)) {
			throw new DbException("BackRef " + backRefName + " is not a backRef of " + e.name());
		}
		if (!e.getPrimaryKey().isFull()) {
			return null;
		}
		List<Entity> entities = DB.getLinkedEntities(e, backRefName, ctx);
		if (entities.size() > 0) {
			return entities.get(0);
		}
		return null;
	}

	/** Retrieves one entity from the database, given its primary key. */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E get(String domainName, Key primaryKey, RequestContext ctx) {
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
	public static <E extends Entity> E get(String domainName, Key primaryKey, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domainName));
		E e = (E) dbFactory.createDbEntity().get(domainName, primaryKey, ctx);
		if (e != null) {
			logic.internalDbPostLoad(e, action, ctx);
		}
		return e;
	}

	public static <E extends Entity> boolean persist(E domain, RequestContext ctx) {
		return persist(domain, new Action(Constants.ACTION_MODIFY, Constants.MODIFY), ctx);
	}

	/**
	 * Persist a domain object into database
	 * 
	 * @param domain The Domain object to persist
	 * @param ctx Current context
	 * @return <code>true</code> if domain object has been inserted, <code>false</code> if it has been updated.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> boolean persist(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(new ArrayList<Message>(ctx.getMessages()));
		}
		boolean insert = dbFactory.createDbEntity().persist(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
		return insert;
	}

	public static <E extends Entity> void insert(E domain, RequestContext ctx) {
		insert(domain, new Action(Constants.ACTION_CREATE, Constants.CREATE), ctx);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> void insert(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(new ArrayList<Message>(ctx.getMessages()));
		}
		dbFactory.createDbEntity().insert(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
	}

	public static <E extends Entity> void update(E domain, RequestContext ctx) {
		update(domain, new Action(Constants.ACTION_MODIFY, Constants.MODIFY), ctx);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> void update(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(new ArrayList<Message>(ctx.getMessages()));
		}
		dbFactory.createDbEntity().update(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
	}

	public static <E extends Entity> void remove(E domain, RequestContext ctx) {
		remove(domain, new Action(Constants.ACTION_DELETE, Constants.DELETE), ctx);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> boolean remove(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain));
		logic.internalDbOnDelete(domain, action, ctx);
		boolean delete = dbFactory.createDbEntity().remove(domain, ctx);
		logic.internalDbPostDelete(domain, action, ctx);
		return delete;
	}

	public static List<Entity> getLinkedEntities(Entity e, String linkName, RequestContext ctx) {
		return dbFactory.createDbEntity().getLinkedEntities(e, linkName, ctx);
	}

	public static List<Entity> getLinkedEntities(Entity e, String linkName, String queryName, RequestContext ctx) {
		return dbFactory.createDbEntity().getLinkedEntities(e, linkName, ctx);
	}

	public static void persistAssociations(Entity baseBean, String linkName, List<Key> selectedKeys, RequestContext ctx) throws DbException {
		dbFactory.createDbEntity().persistAssociations(baseBean, linkName, selectedKeys, ctx);
	}

	public static void removeAssociations(Entity baseBean, String linkName, List<Key> selectedKeys, RequestContext ctx) throws DbException {
		dbFactory.createDbEntity().removeAssociations(baseBean, linkName, selectedKeys, ctx);
	}

	/**
	 * Returns the number of records in database corresponding to the query
	 * 
	 * @param query Query to use for counting
	 * @param ctx Current requestContext containing database connection
	 * @return Number of rows returned when executing query
	 */
	public static int count(DbQuery query, RequestContext ctx) {
		DbQuery countQuery = query.clone();
		countQuery.setCount(true);
		int count = 0;
		DbManager mgr = createDbManager(ctx, countQuery);
		try {
			if (mgr.next()) {
				// La requête est en mode "comptage", il n'y a qu'une colonne qui
				// contient 1 seul entier, le nombre de résultats.
				count = mgr.getInt(1);
			}
		} finally {
			mgr.close();
		}
		return count;
	}

	/** Gets a LOB content from database */
	public static byte[] getLobContent(RequestContext ctx, Entity entity, String propertyName) {
		return dbFactory.createDbEntity().getLobContent(ctx, entity, propertyName);
	}
}
