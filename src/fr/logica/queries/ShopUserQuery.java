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
				
		DbQuery SHOP_USER = new DbQuery("shopUser", "T1");
		SHOP_USER.setName("SHOP_USER");
		SHOP_USER.addColumn("login", "T1"); 
		SHOP_USER.addColumn("name", "T1"); 
		SHOP_USER.addColumn("profile", "T1"); 
		SHOP_USER.addColumn("w$Desc", "T1"); 
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
			/** Alias T1 */
			String SHOP_USER_T1 = "T1";
		}
	}
	
	/** Set of query names */
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}
}

