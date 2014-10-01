package fr.logica.domain.constants;

/**
 * Constants interface for the entity ShopUser
 *
 * @author CGI
 */
public interface ShopUserConstants {
	/** Name of the entity. */
	public static final String ENTITY_NAME = "shopUser";

	/** Table name for this entity or REST class name for external entity */
	public static final String ENTITY_DB_NAME = "USER";

	/** Holder for the var names */
	public interface Vars {
		/** Var LOGIN */
		String LOGIN = "login";
		/** Var NAME */
		String NAME = "name";
		/** Var PASSWORD */
		String PASSWORD = "password";
		/** Var PROFILE */
		String PROFILE = "profile";
		/** Var W$_DESC */
		String W$_DESC = "w$Desc";
	}

	/** Holder for the defined values */
	public interface ValueList {
		public interface PROFILE {
			/** Utilisateur */
			String USER = "USER";
			/** Administrateur */
			String ADMIN = "ADMIN";
			/** Acheteur */
			String BUYER = "BUYER";
		}
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
	}

	/** Holder for the link names */
	public interface Links {
		/**
		 * Utilisateur
		 */ 
		public static final String LINK_SHOP_ARTICLE_L_USER = "shopArticleLUser";
	}

	/** Holder for the page names */
	public interface Pages {
		/** Page SHOP_USER. */
		public static final String SHOP_USER = "SHOP_USER";
	}

	/** Holder for the templates names */
	public interface Templates {
		/** Template SHOP_USER. */
		public static final String SHOP_USER = "shopUser";
	}

}
