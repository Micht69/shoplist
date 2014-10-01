package fr.logica.domain.constants;

/**
 * Constants interface for the entity ShopListLArticle
 *
 * @author CGI
 */
public interface ShopListLArticleConstants {
	/** Name of the entity. */
	public static final String ENTITY_NAME = "shopListLArticle";

	/** Table name for this entity or REST class name for external entity */
	public static final String ENTITY_DB_NAME = "SHOP_LIST_L_ARTICLE";

	/** Holder for the var names */
	public interface Vars {
		/** Var LIST_ID */
		String LIST_ID = "listId";
		/** Var ARTICLE_ID */
		String ARTICLE_ID = "articleId";
		/** Var QUANTITY */
		String QUANTITY = "quantity";
		/** Var STATUS */
		String STATUS = "status";
		/** Var DELETE_INFOS */
		String DELETE_INFOS = "deleteInfos";
	}

	/** Holder for the defined values */
	public interface ValueList {
		public interface STATUS {
			/** A acheter */
			String BUY = "BUY";
			/** Acheté */
			String DONE = "DONE";
		}
	}

	/** Holder for the action names */
	public interface Actions {
		/** Créer. */
		int ACTION_0 = 0;
		/** Créer. */
		int ACTION_51 = 51;
		/** Sélectionner articles. */
		int ACTION_70 = 70;
		/** Modifier. */
		int ACTION_2 = 2;
		/** Supprimer. */
		int ACTION_20 = 20;
		/** Marquer acheté. */
		int ACTION_60 = 60;
	}

	/** Holder for the link names */
	public interface Links {
		/**
		 * Liste
		 */ 
		public static final String LINK_SHOP_LIST_L_ARTICLE_L_LIST = "shopListLArticleLList";
		/**
		 * Article
		 */ 
		public static final String LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE = "shopListLArticleLArticle";
	}

	/** Holder for the page names */
	public interface Pages {
		/** Page SHOP_LIST_L_ARTICLE_DELETE. */
		public static final String SHOP_LIST_L_ARTICLE_DELETE = "SHOP_LIST_L_ARTICLE_DELETE";
		/** Page SHOP_LIST_L_ARTICLE. */
		public static final String SHOP_LIST_L_ARTICLE = "SHOP_LIST_L_ARTICLE";
	}

	/** Holder for the templates names */
	public interface Templates {
		/** Template SHOP_LIST_L_ARTICLE_DELETE. */
		public static final String SHOP_LIST_L_ARTICLE_DELETE = "shopListLArticleDelete";
		/** Template SHOP_LIST_L_ARTICLE. */
		public static final String SHOP_LIST_L_ARTICLE = "shopListLArticle";
	}

}
