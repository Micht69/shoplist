package fr.logica.domain.constants;

/**
 * Constants interface for the entity ShopList
 *
 * @author CGI
 */
public interface ShopListConstants {
	/** Name of the entity. */
	public static final String ENTITY_NAME = "shopList";

	/** Table name for this entity or REST class name for external entity */
	public static final String ENTITY_DB_NAME = "LIST";

	/** Holder for the var names */
	public interface Vars {
		/** Var ID */
		String ID = "id";
		/** Var NAME */
		String NAME = "name";
		/** Var USER */
		String USER = "user";
		/** Var CREATE_DATE */
		String CREATE_DATE = "createDate";
		/** Var W$_DESC */
		String W$_DESC = "w$Desc";
		/** Var ARTICLE_COUNT */
		String ARTICLE_COUNT = "articleCount";
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
		/** Réaliser les course. */
		int ACTION_50 = 50;
	}

	/** Holder for the link names */
	public interface Links {
		/**
		 * Utilisateur
		 */ 
		public static final String LINK_SHOP_ARTICLE_L_USER = "shopArticleLUser";
		/**
		 * Liste
		 */ 
		public static final String LINK_SHOP_LIST_L_ARTICLE_L_LIST = "shopListLArticleLList";
	}

	/** Holder for the page names */
	public interface Pages {
		/** Page SHOP_LIST. */
		public static final String SHOP_LIST = "SHOP_LIST";
		/** Page SHOP_LIST_CREATE. */
		public static final String SHOP_LIST_CREATE = "SHOP_LIST_CREATE";
		/** Page SHOP_LIST_SHOPPING. */
		public static final String SHOP_LIST_SHOPPING = "SHOP_LIST_SHOPPING";
	}

	/** Holder for the templates names */
	public interface Templates {
		/** Template SHOP_LIST. */
		public static final String SHOP_LIST = "shopList";
		/** Template SHOP_LIST_SHOPPING. */
		public static final String SHOP_LIST_SHOPPING = "shopListShopping";
		/** Template SHOP_LIST_INFOS2. */
		public static final String SHOP_LIST_INFOS2 = "shopListInfos2";
		/** Template SHOP_LIST_INFOS. */
		public static final String SHOP_LIST_INFOS = "shopListInfos";
	}

}
