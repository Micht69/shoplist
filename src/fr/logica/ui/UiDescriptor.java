package fr.logica.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.logica.ui.UiElement.Type;

public class UiDescriptor {

	private static final Map<String, UiPage> pages;
	private static final Map<String, Map<Integer, String>> actionPages;
	private static final Map<String, List<UiElement>> criteriaLinks;
	private static final Map<String, String[]> menuQueries;

	static {
		pages = new HashMap<String, UiPage>();
		actionPages = new HashMap<String, Map<Integer, String>>();	
		criteriaLinks = new HashMap<String, List<UiElement>>();
		menuQueries = new HashMap<String, String[]>();

		Map<Integer, String> actPagesShopArticle = new HashMap<Integer, String>();
		actPagesShopArticle.put(0, "SHOP_ARTICLE");
		actPagesShopArticle.put(2, "SHOP_ARTICLE");
		actPagesShopArticle.put(4, "SHOP_ARTICLE");
		actPagesShopArticle.put(5, "SHOP_ARTICLE");
		actPagesShopArticle.put(50, "SHOP_ARTICLE_CREATE");
		actionPages.put("shopArticle", actPagesShopArticle);

		UiPage SHOP_ARTICLE_CREATE = new UiPage();
		pages.put("SHOP_ARTICLE_CREATE", SHOP_ARTICLE_CREATE);

		UiPage SHOP_ARTICLE = new UiPage();
		UiElement SHOP_ARTICLE_SHOP_ARTICLE_L_SHELF_null = new UiElement("shopShelf", "shopArticleLShelf", null, null, Type.LINK_COMBO); 
		SHOP_ARTICLE.elements.add(SHOP_ARTICLE_SHOP_ARTICLE_L_SHELF_null);
		pages.put("SHOP_ARTICLE", SHOP_ARTICLE);

		Map<Integer, String> actPagesShopList = new HashMap<Integer, String>();
		actPagesShopList.put(0, "SHOP_LIST_CREATE");
		actPagesShopList.put(2, "SHOP_LIST");
		actPagesShopList.put(4, "SHOP_LIST");
		actPagesShopList.put(5, "SHOP_LIST");
		actPagesShopList.put(50, "SHOP_LIST_SHOPPING");
		actionPages.put("shopList", actPagesShopList);

		UiPage SHOP_LIST = new UiPage();
		UiElement SHOP_LIST_SHOP_ARTICLE_L_USER = new UiElement("shopUser", "shopArticleLUser", null, null, Type.LINK); 
		SHOP_LIST.elements.add(SHOP_LIST_SHOP_ARTICLE_L_USER);
		UiElement SHOP_LIST_SHOP_LIST_L_ARTICLE_L_LIST_SHOP_ARTICLES2 = new UiElement("shopListLArticle", "shopListLArticleLList", "SHOP_ARTICLES2", null, Type.BACK_REF_LIST); 
		SHOP_LIST.elements.add(SHOP_LIST_SHOP_LIST_L_ARTICLE_L_LIST_SHOP_ARTICLES2);
		pages.put("SHOP_LIST", SHOP_LIST);

		UiPage SHOP_LIST_CREATE = new UiPage();
		UiElement SHOP_LIST_CREATE_SHOP_ARTICLE_L_USER = new UiElement("shopUser", "shopArticleLUser", null, null, Type.LINK); 
		SHOP_LIST_CREATE.elements.add(SHOP_LIST_CREATE_SHOP_ARTICLE_L_USER);
		pages.put("SHOP_LIST_CREATE", SHOP_LIST_CREATE);

		UiPage SHOP_LIST_SHOPPING = new UiPage();
		UiElement SHOP_LIST_SHOPPING_SHOP_LIST_L_ARTICLE_L_LIST_SHOP_ARTICLES_SHOPPING = new UiElement("shopListLArticle", "shopListLArticleLList", "SHOP_ARTICLES_SHOPPING", null, Type.BACK_REF_LIST); 
		SHOP_LIST_SHOPPING.elements.add(SHOP_LIST_SHOPPING_SHOP_LIST_L_ARTICLE_L_LIST_SHOP_ARTICLES_SHOPPING);
		pages.put("SHOP_LIST_SHOPPING", SHOP_LIST_SHOPPING);

		Map<Integer, String> actPagesShopListLArticle = new HashMap<Integer, String>();
		actPagesShopListLArticle.put(0, "SHOP_LIST_L_ARTICLE");
		actPagesShopListLArticle.put(2, "SHOP_LIST_L_ARTICLE");
		actPagesShopListLArticle.put(4, "SHOP_LIST_L_ARTICLE");
		actPagesShopListLArticle.put(5, "SHOP_LIST_L_ARTICLE");
		actPagesShopListLArticle.put(50, "SHOP_LIST_L_ARTICLE");
		actPagesShopListLArticle.put(51, "SHOP_LIST_L_ARTICLE");
		actionPages.put("shopListLArticle", actPagesShopListLArticle);

		UiPage SHOP_LIST_L_ARTICLE = new UiPage();
		UiElement SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_L_LIST_null = new UiElement("shopList", "shopListLArticleLList", null, null, Type.LINK_COMBO); 
		SHOP_LIST_L_ARTICLE.elements.add(SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_L_LIST_null);
		UiElement SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_L_ARTICLE = new UiElement("shopArticle", "shopListLArticleLArticle", null, null, Type.LINK); 
		SHOP_LIST_L_ARTICLE.elements.add(SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_L_ARTICLE);
		pages.put("SHOP_LIST_L_ARTICLE", SHOP_LIST_L_ARTICLE);

		Map<Integer, String> actPagesShopShelf = new HashMap<Integer, String>();
		actPagesShopShelf.put(0, "SHOP_SHELF");
		actPagesShopShelf.put(2, "SHOP_SHELF");
		actPagesShopShelf.put(4, "SHOP_SHELF");
		actPagesShopShelf.put(5, "SHOP_SHELF");
		actionPages.put("shopShelf", actPagesShopShelf);

		UiPage SHOP_SHELF = new UiPage();
		pages.put("SHOP_SHELF", SHOP_SHELF);

		UiPage SHOP_SHELF_READ = new UiPage();
		pages.put("SHOP_SHELF_READ", SHOP_SHELF_READ);

		Map<Integer, String> actPagesShopUser = new HashMap<Integer, String>();
		actPagesShopUser.put(0, "SHOP_USER");
		actPagesShopUser.put(2, "SHOP_USER");
		actPagesShopUser.put(4, "SHOP_USER");
		actPagesShopUser.put(5, "SHOP_USER");
		actionPages.put("shopUser", actPagesShopUser);

		UiPage SHOP_USER = new UiPage();
		pages.put("SHOP_USER", SHOP_USER);

		UiPage SHOP_USER_READ = new UiPage();
		pages.put("SHOP_USER_READ", SHOP_USER_READ);

	}

	public static UiPage getPage(String pageName) {
		return pages.get(pageName);
	}

	public static String getActionPage(String entityName, Integer codeAction) {
		Map<Integer, String> actionPage = actionPages.get(entityName);

		if (null != actionPage) {
			return actionPages.get(entityName).get(codeAction);
		}
		return null;
	}

	public static List<UiElement> getCriteriaLinks(String queryName) {
		return criteriaLinks.get(queryName);
	}

	public static Map<String, String[]> getMenuQueries() {
		return menuQueries;
	}
}
