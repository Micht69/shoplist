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
 * Query class for the entity ShopArticle
 *
 * @author CGI
 */
public class ShopArticleQuery extends AbstractEntityQuery {
	/** Queries for entity ShopArticle */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		// Initialization of query SHOP_ARTICLE
		DbQuery shopArticle = new DbQuery("shopArticle", "ART");
		shopArticle.setName("SHOP_ARTICLE");
		shopArticle.addEntity("shopShelf", "SH", "shopArticleLShelf", null, Join.LOOSE);
		shopArticle.addColumn("id", "ART", Visibility.INVISIBLE);
		shopArticle.addColumn("position", "SH", Visibility.INVISIBLE);
		shopArticle.addColumn("name", "SH");
		shopArticle.addColumn("name", "ART");
		shopArticle.addColumn("ean13", "ART");
		shopArticle.addSortBy("name", "SH", "ASC");
		shopArticle.addSortBy("name", "ART", "ASC");
		QUERIES.put("SHOP_ARTICLE", shopArticle);	

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
		/** Query SHOP_ARTICLE */
		String QUERY_SHOP_ARTICLE = "SHOP_ARTICLE";
	}
	
	/** Holder for the query entities aliases */
	public interface Alias {
		/** Aliases for query SHOP_ARTICLE */
		public interface QUERY_SHOP_ARTICLE {
			/** Alias ART */
			String SHOP_ARTICLE_ART = "ART";
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
	 * Returns a clone of DbQuery named SHOP_ARTICLE
	 * 
	 * @return clone of DbQuery named SHOP_ARTICLE
	 */
	public static DbQuery getShopArticleQuery() {
		return QUERIES.get(Query.QUERY_SHOP_ARTICLE).clone();
	}


}

