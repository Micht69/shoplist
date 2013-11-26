package fr.logica.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		DbQuery SHOP_USER = new DbQuery("shopUser", "USR");
		SHOP_USER.setName("SHOP_USER");
		SHOP_USER.addColumn("login", "USR");
		SHOP_USER.addColumn("name", "USR");
		SHOP_USER.addColumn("profile", "USR");
		SHOP_USER.addSortBy("login", "USR", "ASC");
		QUERIES.put("SHOP_USER", SHOP_USER);	

	}
	
	@Override
	public DbQuery getQuery(String queryName) {
		return QUERIES.get(queryName);
	}
	
	/** Holder for the query names */
	public interface Query {
		/** Query SHOP_USER */
		String QUERY_SHOP_USER = "SHOP_USER";
	}
	
	/** Holder for the query entityes aliases */
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
}

