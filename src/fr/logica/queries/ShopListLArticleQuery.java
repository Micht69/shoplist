package fr.logica.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		DbQuery SHOP_ARTICLES_SHOPPING = new DbQuery("shopListLArticle", "T1");
		SHOP_ARTICLES_SHOPPING.setName("SHOP_ARTICLES_SHOPPING");
		SHOP_ARTICLES_SHOPPING.addEntity("shopArticle", "T4", "shopListLArticleLArticle", null, Join.LOOSE);
		SHOP_ARTICLES_SHOPPING.addEntity("shopShelf", "T3", "shopArticleLShelf", null, Join.LOOSE);
		SHOP_ARTICLES_SHOPPING.addColumn("listId", "T1", Visibility.INVISIBLE);
		SHOP_ARTICLES_SHOPPING.addColumn("articleId", "T1", Visibility.INVISIBLE);
		SHOP_ARTICLES_SHOPPING.addColumn("position", "T3", Visibility.INVISIBLE);
		SHOP_ARTICLES_SHOPPING.addColumn("name", "T3");
		SHOP_ARTICLES_SHOPPING.addColumn("name", "T4");
		SHOP_ARTICLES_SHOPPING.addColumn("quantity", "T1");
		SHOP_ARTICLES_SHOPPING.addColumn("status", "T1");
		SHOP_ARTICLES_SHOPPING.addCondEq("status", "T1", "BUY");
		SHOP_ARTICLES_SHOPPING.addSortBy("position", "T3", "ASC");
		SHOP_ARTICLES_SHOPPING.addSortBy("name", "T4", "ASC");
		QUERIES.put("SHOP_ARTICLES_SHOPPING", SHOP_ARTICLES_SHOPPING);	
		
		DbQuery SHOP_ARTICLES2 = new DbQuery("shopListLArticle", "T1");
		SHOP_ARTICLES2.setName("SHOP_ARTICLES2");
		SHOP_ARTICLES2.addEntity("shopArticle", "T4", "shopListLArticleLArticle", null, Join.LOOSE);
		SHOP_ARTICLES2.addEntity("shopShelf", "T3", "shopArticleLShelf", null, Join.LOOSE);
		SHOP_ARTICLES2.addColumn("listId", "T1", Visibility.INVISIBLE);
		SHOP_ARTICLES2.addColumn("articleId", "T1", Visibility.INVISIBLE);
		SHOP_ARTICLES2.addColumn("position", "T3", Visibility.INVISIBLE);
		SHOP_ARTICLES2.addColumn("name", "T3");
		SHOP_ARTICLES2.addColumn("name", "T4");
		SHOP_ARTICLES2.addColumn("quantity", "T1");
		SHOP_ARTICLES2.addColumn("status", "T1");
		SHOP_ARTICLES2.addSortBy("status", "T1", "ASC");
		SHOP_ARTICLES2.addSortBy("position", "T3", "ASC");
		SHOP_ARTICLES2.addSortBy("name", "T4", "ASC");
		QUERIES.put("SHOP_ARTICLES2", SHOP_ARTICLES2);	
				
		DbQuery SHOP_LIST_L_ARTICLE = new DbQuery("shopListLArticle", "T1");
		SHOP_LIST_L_ARTICLE.setName("SHOP_LIST_L_ARTICLE");
		SHOP_LIST_L_ARTICLE.addColumn("listId", "T1"); 
		SHOP_LIST_L_ARTICLE.addColumn("articleId", "T1"); 
		SHOP_LIST_L_ARTICLE.addColumn("quantity", "T1"); 
		SHOP_LIST_L_ARTICLE.addColumn("status", "T1"); 
		QUERIES.put("SHOP_LIST_L_ARTICLE", SHOP_LIST_L_ARTICLE);	
	

	}
	
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
	
	/** Holder for the query entityes aliases */
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
}

