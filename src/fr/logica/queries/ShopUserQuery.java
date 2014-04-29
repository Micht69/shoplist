package fr.logica.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.logica.business.TechnicalException;
import fr.logica.db.AbstractEntityQuery;
import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Join;
import fr.logica.db.DbQuery.Visibility;


/**
 * Query class for the entity ShopUser
 *
 * @author CGI
 */
public class ShopUserQuery extends AbstractEntityQuery {
	/** Queries for entity ShopUser */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		// Initialization of query SHOP_USER
		DbQuery shopUser = new DbQuery("shopUser", "USR");
		shopUser.setName("SHOP_USER");
		shopUser.addColumn("login", "USR");
		shopUser.addColumn("name", "USR");
		shopUser.addColumn("profile", "USR");
		shopUser.addSortBy("login", "USR", "ASC");
		QUERIES.put("SHOP_USER", shopUser);	

	}
	
	/**
	 * Gets a query from internal query cache
	 * @param queryName query name
	 * @return Cached query
	 */
	@Override
	public DbQuery getQuery(String queryName) {
		return QUERIES.get(queryName);
	}
	
	/** Holder for the query names */
	public interface Query {
		/** Query SHOP_USER */
		String QUERY_SHOP_USER = "SHOP_USER";
	}
	
	/** Holder for the query entities aliases */
	public interface Alias {
		/** Aliases for query SHOP_USER */
		public interface QUERY_SHOP_USER {
			/** Alias USR */
			String SHOP_USER_USR = "USR";
		}
	}
	
	/** Set of query names */
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named SHOP_USER
	 * 
	 * @return clone of DbQuery named SHOP_USER
	 */
	public static DbQuery getShopUserQuery() {
		return QUERIES.get(Query.QUERY_SHOP_USER).clone();
	}


}

