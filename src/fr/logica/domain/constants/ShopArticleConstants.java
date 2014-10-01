package fr.logica.domain.constants;

/**
 * Constants interface for the entity ShopArticle
 *
 * @author CGI
 */
public interface ShopArticleConstants {
	/** Name of the entity. */
	public static final String ENTITY_NAME = "shopArticle";

	/** Table name for this entity or REST class name for external entity */
	public static final String ENTITY_DB_NAME = "ARTICLE";

	/** Holder for the var names */
	public interface Vars {
		/** Var ID */
		String ID = "id";
		/** Var NAME */
		String NAME = "name";
		/** Var DESCR */
		String DESCR = "descr";
		/** Var SHELF */
		String SHELF = "shelf";
		/** Var EAN13 */
		String EAN13 = "ean13";
		/** Var W$_DESC */
		String W$_DESC = "w$Desc";
	}

	/** Holder for the action names */
	public interface Actions {
		/** Créer. */
		int ACTION_0 = 0;
		/** Modifier. */
		int ACTION_2 = 2;
		/** Supprimer. */
		int ACTION_4 = 4;
		/** Afficher. */
		int ACTION_5 = 5;
		/** Importer depuis EAN13. */
		int ACTION_50 = 50;
	}

	/** Holder for the link names */
	public interface Links {
		/**
		 * Rayon
		 */ 
		public static final String LINK_SHOP_ARTICLE_L_SHELF = "shopArticleLShelf";
		/**
		 * Article
		 */ 
		public static final String LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE = "shopListLArticleLArticle";
	}

	/** Holder for the page names */
	public interface Pages {
		/** Page SHOP_ARTICLE_CREATE. */
		public static final String SHOP_ARTICLE_CREATE = "SHOP_ARTICLE_CREATE";
		/** Page SHOP_ARTICLE. */
		public static final String SHOP_ARTICLE = "SHOP_ARTICLE";
	}

	/** Holder for the templates names */
	public interface Templates {
		/** Template SHOP_ARTICLE_CREATE. */
		public static final String SHOP_ARTICLE_CREATE = "shopArticleCreate";
		/** Template SHOP_ARTICLE_CREATE_10_EAN_SCAN. */
		public static final String SHOP_ARTICLE_CREATE_10_EAN_SCAN = "shopArticleCreate10EanScan";
		/** Template SHOP_ARTICLE. */
		public static final String SHOP_ARTICLE = "shopArticle";
	}

}
