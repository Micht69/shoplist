package fr.logica.domain.constants;

/**
 * Constants interface for the entity ShopShelf
 *
 * @author CGI
 */
public interface ShopShelfConstants {
	/** Name of the entity. */
	public static final String ENTITY_NAME = "shopShelf";

	/** Table name for this entity or REST class name for external entity */
	public static final String ENTITY_DB_NAME = "SHELF";

	/** Holder for the var names */
	public interface Vars {
		/** Var CODE */
		String CODE = "code";
		/** Var NAME */
		String NAME = "name";
		/** Var POSITION */
		String POSITION = "position";
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
	}

	/** Holder for the link names */
	public interface Links {
		/**
		 * Rayon
		 */ 
		public static final String LINK_SHOP_ARTICLE_L_SHELF = "shopArticleLShelf";
	}

	/** Holder for the page names */
	public interface Pages {
	}

	/** Holder for the templates names */
	public interface Templates {
	}

}
