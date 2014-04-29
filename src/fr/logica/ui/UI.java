package fr.logica.ui;


/**
 * Holder for the template names.
 * 
 * @author CGI
 */
public interface UI {

	/** ShopArticle's template names. */
	public interface ShopArticle {
		/** SHOP_ARTICLE_CREATE. */
		String SHOP_ARTICLE_CREATE = "shopArticleCreate";
		/** SHOP_ARTICLE_CREATE_10_EAN_SCAN. */
		String SHOP_ARTICLE_CREATE_10_EAN_SCAN = "shopArticleCreate10EanScan";
		/** SHOP_ARTICLE. */
		String SHOP_ARTICLE = "shopArticle";
	}

	/** ShopList's template names. */
	public interface ShopList {
		/** SHOP_LIST. */
		String SHOP_LIST = "shopList";
		/** SHOP_LIST_SHOPPING. */
		String SHOP_LIST_SHOPPING = "shopListShopping";
		/** SHOP_LIST_INFOS2. */
		String SHOP_LIST_INFOS2 = "shopListInfos2";
		/** SHOP_LIST_INFOS. */
		String SHOP_LIST_INFOS = "shopListInfos";
	}

	/** ShopListLArticle's template names. */
	public interface ShopListLArticle {
		/** SHOP_LIST_L_ARTICLE_DELETE. */
		String SHOP_LIST_L_ARTICLE_DELETE = "shopListLArticleDelete";
		/** SHOP_LIST_L_ARTICLE. */
		String SHOP_LIST_L_ARTICLE = "shopListLArticle";
	}

	/** ShopUser's template names. */
	public interface ShopUser {
		/** SHOP_USER. */
		String SHOP_USER = "shopUser";
	}

}
