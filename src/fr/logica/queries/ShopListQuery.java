package fr.logica.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		DbQuery SHOP_LIST = new DbQuery("shopList", "T1");
		SHOP_LIST.setName("SHOP_LIST");
		SHOP_LIST.addColumn("id", "T1", Visibility.INVISIBLE);
		SHOP_LIST.addColumn("name", "T1");
		SHOP_LIST.addColumn("createDate", "T1");
		SHOP_LIST.addColumn("articleCount", "T1");
		QUERIES.put("SHOP_LIST", SHOP_LIST);	

	}
	
	@Override
	public DbQuery getQuery(String queryName) {
		return QUERIES.get(queryName);
	}
	
	/** Holder for the query names */
	public interface Query {
		/** Query SHOP_LIST */
		String QUERY_SHOP_LIST = "SHOP_LIST";
	}
	
	/** Holder for the query entityes aliases */
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
}

