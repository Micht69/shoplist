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
 * Query class for the entity ShopShelf
 *
 * @author CGI
 */
public class ShopShelfQuery extends AbstractEntityQuery {
	/** Queries for entity ShopShelf */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		// Initialization of query SHOP_SHELF
		DbQuery shopShelf = new DbQuery("shopShelf", "SH");
		shopShelf.setName("SHOP_SHELF");
		shopShelf.addColumn("code", "SH");
		shopShelf.addColumn("name", "SH");
		shopShelf.addColumn("position", "SH");
		shopShelf.addSortBy("position", "SH", "ASC");
		QUERIES.put("SHOP_SHELF", shopShelf);	

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
		/** Query SHOP_SHELF */
		String QUERY_SHOP_SHELF = "SHOP_SHELF";
	}
	
	/** Holder for the query entities aliases */
	public interface Alias {
		/** Aliases for query SHOP_SHELF */
		public interface QUERY_SHOP_SHELF {
			/** Alias SH */
			String SHOP_SHELF_SH = "SH";
		}
	}
	
	/** Set of query names */
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named SHOP_SHELF
	 * 
	 * @return clone of DbQuery named SHOP_SHELF
	 */
	public static DbQuery getShopShelfQuery() {
		return QUERIES.get(Query.QUERY_SHOP_SHELF).clone();
	}


}

