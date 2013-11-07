package fr.logica.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	static { 
		QUERIES = new HashMap<String, DbQuery>();	
		
		DbQuery SHOP_ARTICLE = new DbQuery("shopArticle", "ART");
		SHOP_ARTICLE.setName("SHOP_ARTICLE");
		SHOP_ARTICLE.addEntity("shopShelf", "SH", "shopArticleLShelf", null, Join.LOOSE);
		SHOP_ARTICLE.addColumn("id", "ART", Visibility.INVISIBLE);
		SHOP_ARTICLE.addColumn("position", "SH", Visibility.INVISIBLE);
		SHOP_ARTICLE.addColumn("name", "SH");
		SHOP_ARTICLE.addColumn("name", "ART");
		SHOP_ARTICLE.addColumn("ean13", "ART");
		SHOP_ARTICLE.addSortBy("name", "SH", "ASC");
		SHOP_ARTICLE.addSortBy("name", "ART", "ASC");
		QUERIES.put("SHOP_ARTICLE", SHOP_ARTICLE);	

	}
	
	@Override
	public DbQuery getQuery(String queryName) {
		return QUERIES.get(queryName);
	}
	
	/** Holder for the query names */
	public interface Query {
		/** Query SHOP_ARTICLE */
		String QUERY_SHOP_ARTICLE = "SHOP_ARTICLE";
	}
	
	/** Holder for the query entityes aliases */
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
}

