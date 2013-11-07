package fr.logica.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		DbQuery SHOP_SHELF = new DbQuery("shopShelf", "SH");
		SHOP_SHELF.setName("SHOP_SHELF");
		SHOP_SHELF.addColumn("code", "SH");
		SHOP_SHELF.addColumn("name", "SH");
		SHOP_SHELF.addColumn("position", "SH");
		SHOP_SHELF.addSortBy("position", "SH", "ASC");
		QUERIES.put("SHOP_SHELF", SHOP_SHELF);	

	}
	
	@Override
	public DbQuery getQuery(String queryName) {
		return QUERIES.get(queryName);
	}
	
	/** Holder for the query names */
	public interface Query {
		/** Query SHOP_SHELF */
		String QUERY_SHOP_SHELF = "SHOP_SHELF";
	}
	
	/** Holder for the query entityes aliases */
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
}

