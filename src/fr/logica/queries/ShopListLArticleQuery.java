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
 * Query class for the entity ShopListLArticle
 *
 * @author CGI
 */
public class ShopListLArticleQuery extends AbstractEntityQuery {
	/** Queries for entity ShopListLArticle */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		// Initialization of query SHOP_ARTICLES2
		DbQuery shopArticles2 = new DbQuery("shopListLArticle", "T1");
		shopArticles2.setName("SHOP_ARTICLES2");
		shopArticles2.addEntity("shopArticle", "T4", "shopListLArticleLArticle", null, Join.LOOSE);
		shopArticles2.addEntity("shopShelf", "T3", "shopArticleLShelf", null, Join.LOOSE);
		shopArticles2.addColumn("listId", "T1", Visibility.INVISIBLE);
		shopArticles2.addColumn("articleId", "T1", Visibility.INVISIBLE);
		shopArticles2.addColumn("position", "T3", Visibility.INVISIBLE);
		shopArticles2.addColumn("name", "T3");
		shopArticles2.addColumn("name", "T4");
		shopArticles2.addColumn("quantity", "T1");
		shopArticles2.addColumn("status", "T1");
		shopArticles2.addSortBy("status", "T1", "ASC");
		shopArticles2.addSortBy("position", "T3", "ASC");
		shopArticles2.addSortBy("name", "T4", "ASC");
		QUERIES.put("SHOP_ARTICLES2", shopArticles2);	
		
		// Initialization of query SHOP_ARTICLES_SHOPPING
		DbQuery shopArticlesShopping = new DbQuery("shopListLArticle", "T1");
		shopArticlesShopping.setName("SHOP_ARTICLES_SHOPPING");
		shopArticlesShopping.addEntity("shopArticle", "T4", "shopListLArticleLArticle", null, Join.LOOSE);
		shopArticlesShopping.addEntity("shopShelf", "T3", "shopArticleLShelf", null, Join.LOOSE);
		shopArticlesShopping.addColumn("listId", "T1", Visibility.INVISIBLE);
		shopArticlesShopping.addColumn("articleId", "T1", Visibility.INVISIBLE);
		shopArticlesShopping.addColumn("position", "T3", Visibility.INVISIBLE);
		shopArticlesShopping.addColumn("name", "T3");
		shopArticlesShopping.addColumn("name", "T4");
		shopArticlesShopping.addColumn("quantity", "T1");
		shopArticlesShopping.addColumn("status", "T1");
		shopArticlesShopping.addCondEq("status", "T1", "BUY");
		shopArticlesShopping.addSortBy("position", "T3", "ASC");
		shopArticlesShopping.addSortBy("name", "T4", "ASC");
		QUERIES.put("SHOP_ARTICLES_SHOPPING", shopArticlesShopping);	
				
		DbQuery shopListLArticle = new DbQuery("shopListLArticle", "T1");
		shopListLArticle.setName("SHOP_LIST_L_ARTICLE");
		shopListLArticle.addColumn("listId", "T1"); 
		shopListLArticle.addColumn("articleId", "T1"); 
		shopListLArticle.addColumn("quantity", "T1"); 
		shopListLArticle.addColumn("status", "T1"); 
		shopListLArticle.addColumn("deleteInfos", "T1"); 
		QUERIES.put("SHOP_LIST_L_ARTICLE", shopListLArticle);	
	

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
		/** Query SHOP_ARTICLES2 */
		String QUERY_SHOP_ARTICLES2 = "SHOP_ARTICLES2";
		/** Query SHOP_ARTICLES_SHOPPING */
		String QUERY_SHOP_ARTICLES_SHOPPING = "SHOP_ARTICLES_SHOPPING";
		/** Query SHOP_LIST_L_ARTICLE */
		String QUERY_SHOP_LIST_L_ARTICLE = "SHOP_LIST_L_ARTICLE";
	}
	
	/** Holder for the query entities aliases */
	public interface Alias {
		/** Aliases for query SHOP_ARTICLES2 */
		public interface QUERY_SHOP_ARTICLES2 {
			/** Alias T1 */
			String SHOP_LIST_L_ARTICLE_T1 = "T1";
			/** Alias T3 */
			String SHOP_SHELF_T3 = "T3";
			/** Alias T4 */
			String SHOP_ARTICLE_T4 = "T4";
		}
		/** Aliases for query SHOP_ARTICLES_SHOPPING */
		public interface QUERY_SHOP_ARTICLES_SHOPPING {
			/** Alias T1 */
			String SHOP_LIST_L_ARTICLE_T1 = "T1";
			/** Alias T3 */
			String SHOP_SHELF_T3 = "T3";
			/** Alias T4 */
			String SHOP_ARTICLE_T4 = "T4";
		}
		/** Aliases for query SHOP_LIST_L_ARTICLE */
		public interface QUERY_SHOP_LIST_L_ARTICLE {
			/** Alias T1 */
			String SHOP_LIST_L_ARTICLE_T1 = "T1";
		}
	}
	
	/** Set of query names */
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named SHOP_ARTICLES2
	 * 
	 * @return clone of DbQuery named SHOP_ARTICLES2
	 */
	public static DbQuery getShopArticles2Query() {
		return QUERIES.get(Query.QUERY_SHOP_ARTICLES2).clone();
	}

	/**
	 * Returns a clone of DbQuery named SHOP_ARTICLES_SHOPPING
	 * 
	 * @return clone of DbQuery named SHOP_ARTICLES_SHOPPING
	 */
	public static DbQuery getShopArticlesShoppingQuery() {
		return QUERIES.get(Query.QUERY_SHOP_ARTICLES_SHOPPING).clone();
	}

	/**
	 * Returns a clone of DbQuery named SHOP_LIST_L_ARTICLE
	 * 
	 * @return clone of DbQuery named SHOP_LIST_L_ARTICLE
	 */
	public static DbQuery getShopListLArticleQuery() {
		return QUERIES.get(Query.QUERY_SHOP_LIST_L_ARTICLE).clone();
	}

}

