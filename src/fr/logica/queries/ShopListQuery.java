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
 * Query class for the entity ShopList
 *
 * @author CGI
 */
public class ShopListQuery extends AbstractEntityQuery {
	/** Queries for entity ShopList */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		// Initialization of query SHOP_LIST
		DbQuery shopList = new DbQuery("shopList", "T1");
		shopList.setName("SHOP_LIST");
		shopList.addColumn("id", "T1", Visibility.INVISIBLE);
		shopList.addColumn("name", "T1");
		shopList.addColumn("createDate", "T1");
		shopList.addColumn("articleCount", "T1");
		QUERIES.put("SHOP_LIST", shopList);	

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
		/** Query SHOP_LIST */
		String QUERY_SHOP_LIST = "SHOP_LIST";
	}
	
	/** Holder for the query entities aliases */
	public interface Alias {
		/** Aliases for query SHOP_LIST */
		public interface QUERY_SHOP_LIST {
			/** Alias T1 */
			String SHOP_LIST_T1 = "T1";
		}
	}
	
	/** Set of query names */
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named SHOP_LIST
	 * 
	 * @return clone of DbQuery named SHOP_LIST
	 */
	public static DbQuery getShopListQuery() {
		return QUERIES.get(Query.QUERY_SHOP_LIST).clone();
	}


}

