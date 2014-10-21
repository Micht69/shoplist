package fr.logica.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SecurityFunctionTree {
	
	private static List<SecurityFunctionNode> elements = new ArrayList<SecurityFunctionNode>();
	
	private static Map<Integer, SecurityFunctionNode> mapSfn = new HashMap<Integer, SecurityFunctionNode>();
	
	static {
		insertMenu();
		insertEntities1to1000();
	}
	
	public static void insertMenu() {
		mapSfn.put(0, new SecurityFunctionNode());
		mapSfn.get(0).getSf().setMenuOption("SHOP_LISTS");
		elements.add(mapSfn.get(0));
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setQuery("SHOP_LIST");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));

		mapSfn.put(0, new SecurityFunctionNode());
		mapSfn.get(0).getSf().setMenuOption("SHOP_ARTICLES");
		elements.add(mapSfn.get(0));
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setQuery("SHOP_ARTICLE");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));

		mapSfn.put(0, new SecurityFunctionNode());
		mapSfn.get(0).getSf().setMenu("SHOP_ADMIN");
		elements.add(mapSfn.get(0));
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setMenuOption("SHOP_SHELFS");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setQuery("SHOP_SHELF");
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setMenuOption("SHOP_USERS");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setQuery("SHOP_USER");
		mapSfn.get(1).getChilds().add(mapSfn.get(2));


	}
	
	public static void insertEntities1to1000() {
		mapSfn.put(0, new SecurityFunctionNode());
		elements.add(mapSfn.get(0));

		insertNewEntity("shopArticle", 0, 2, 4, 5, 50);
		insertNewEntity("shopList", 0, 2, 4, 5, 50);
		insertNewEntity("shopListLArticle", 0, 51, 70, 71, 2, 20, 60);
		insertNewEntity("shopShelf", 0, 2, 4, 5);
		insertNewEntity("shopUser", 0, 2, 4, 5);
	}
	
	private static void insertNewEntity(String entityName, int... actions) {
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setEntite(entityName);
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		
		for(int action : actions) {
			mapSfn.put(2, new SecurityFunctionNode());
			mapSfn.get(2).getSf().setEntite(entityName);
			mapSfn.get(2).getSf().setAction(action);
			mapSfn.get(1).getChilds().add(mapSfn.get(2));
		}
	}
	
	public static List<SecurityFunctionNode> getElements() {
		return elements;
	}

}


