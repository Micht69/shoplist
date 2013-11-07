package fr.logica.db;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.logica.business.Constants;
import fr.logica.business.Context;
import fr.logica.business.TechnicalException;
import fr.logica.reflect.DomainUtils;

public class DbFactory {

	private final static Map<String, AbstractEntityQuery> QUERIES;
	private final static Map<String, String> QUERY_MAIN_ENTITY;
	private static boolean initialized = false;

	static {
		QUERIES = new HashMap<String, AbstractEntityQuery>();
		QUERY_MAIN_ENTITY = new HashMap<String, String>();
	}

	@SuppressWarnings("unchecked")
	private static void initialize() {
		if (DbConnection.dbType == null) {
			// dbType is not set yet, we'll initialize later
			return;
		}
		Class<?> appDescriptor;
		try {
			appDescriptor = Class.forName("fr.logica.application.ApplicationDescriptor");
			Set<String> domains = (Set<String>) appDescriptor.getMethod("getDomains").invoke(appDescriptor);
			for (String domain : domains) {
				String className = Constants.QUERIES_PACKAGE + "." + domain.substring(0, 1).toUpperCase() + domain.substring(1)
						+ Constants.EXTENSION_QUERY;

				AbstractEntityQuery qryClass = (AbstractEntityQuery) Class.forName(className).newInstance();
				for (String queryName : qryClass.getQueryNames()) {
					QUERY_MAIN_ENTITY.put(queryName, domain);
				}
			}
			initialized = true;
		} catch (ClassNotFoundException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		} catch (SecurityException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new TechnicalException("Impossible d'initialiser DbFactory : " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a new query.
	 * 
	 * @param ctx
	 *            Current context.
	 * @param entityName
	 *            Domain object's name to query.
	 * @param queryName
	 *            Name of the query to execute.
	 * @return a new DbQuery object.
	 */
	public DbEntity createDbEntity() {
		return new DbEntity();
	}

	/**
	 * Creates a new connection object.
	 * 
	 * @return A new DbConnection object.
	 */
	public DbConnection createDbConnection() {
		return new DbConnection();
	}

	/**
	 * Creates a new object to query database.
	 * 
	 * @param ctx
	 *            Current context.
	 * @param query
	 *            Query to execute.
	 * @return A new IDbManager object.
	 */
	public DbManager createDbManager(Context ctx, DbQuery query) {
		return new DbManager(ctx, query);
	}

	/**
	 * Creates a new query.
	 * 
	 * @param ctx
	 *            Current context.
	 * @param entityName
	 *            Domain object's name to query.
	 * @return a new DbQuery object.
	 */
	public DbQuery createDbQuery(Context ctx, String entityName) {
		return new DbQuery(entityName);
	}

	/**
	 * Creates a new query.
	 * 
	 * @param ctx
	 *            Current context.
	 * @param entityName
	 *            Domain object's name to query.
	 * @param alias
	 *            Alias to use in the query.
	 * @return a new DbQuery object.
	 */
	public DbQuery createDbQuery(Context ctx, String entityName, String alias) {
		return new DbQuery(entityName, alias);
	}

	/**
	 * Creates a new query.
	 * 
	 * @param ctx
	 *            Current context.
	 * @param entityName
	 *            Domain object's name to query.
	 * @param queryName
	 *            Name of the query to execute.
	 * @return a new DbQuery object.
	 */
	public synchronized DbQuery getQuery(Context ctx, String entityName) {
		return getDbQuery(ctx, entityName, DomainUtils.createDbName(entityName));
	}

	public synchronized DbQuery getDbQuery(Context ctx, String entityName, String queryName) {
		if (entityName == null || "".equals(entityName)) {
			return null;
		}
		if (queryName == null || "".equals(queryName)) {
			return getQuery(ctx, entityName);
		}
		if (QUERIES.get(entityName) == null) {
			String className = Constants.QUERIES_PACKAGE + "." + entityName.substring(0, 1).toUpperCase() + entityName.substring(1)
					+ Constants.EXTENSION_QUERY;

			try {
				QUERIES.put(entityName, (AbstractEntityQuery) Class.forName(className).newInstance());

			} catch (InstantiationException e) {
				throw new TechnicalException("Impossible d'instancier la classe de requêtes " + entityName, e);
			} catch (IllegalAccessException e) {
				throw new TechnicalException("Impossible d'instancier la classe de requêtes " + entityName, e);
			} catch (ClassNotFoundException e) {
				throw new TechnicalException("Impossible d'instancier la classe de requêtes " + entityName, e);
			}
		}
		try {
			if ((DbQuery) QUERIES.get(entityName).getQuery(queryName) == null) {
				if (!initialized) {
					initialize();
				}
				String qMainEntity = QUERY_MAIN_ENTITY.get(queryName);
				return (DbQuery) getDbQuery(ctx, qMainEntity, queryName);
			}
			return (DbQuery) QUERIES.get(entityName).getQuery(queryName).clone();
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException("Impossible de cloner la requête demandée : " + entityName + " - " + queryName, e);
		} catch (NullPointerException e) {
			throw new TechnicalException("La requête demandée : " + entityName + " - " + queryName + " est introuvable. ");
		}
	}

}
