package fr.logica.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SecurityFunctionTree {
	
	private static List<SecurityFunctionNode> elements = new ArrayList<SecurityFunctionNode>();
	
	static {
		Map<Integer, SecurityFunctionNode> mapSfn = new HashMap<Integer, SecurityFunctionNode>();
		
		mapSfn.put(0, new SecurityFunctionNode());
		mapSfn.get(0).getSf().setMenu("SHOP_LISTS");
		elements.add(mapSfn.get(0));

		mapSfn.put(0, new SecurityFunctionNode());
		mapSfn.get(0).getSf().setMenu("SHOP_ARTICLES");
		elements.add(mapSfn.get(0));

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


		mapSfn.put(0, new SecurityFunctionNode());
		elements.add(mapSfn.get(0));

		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setEntite("shopArticle");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopArticle");
		mapSfn.get(2).getSf().setAction(0);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopArticle");
		mapSfn.get(2).getSf().setAction(2);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopArticle");
		mapSfn.get(2).getSf().setAction(4);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopArticle");
		mapSfn.get(2).getSf().setAction(5);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopArticle");
		mapSfn.get(2).getSf().setAction(50);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));

		
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setEntite("shopList");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopList");
		mapSfn.get(2).getSf().setAction(0);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopList");
		mapSfn.get(2).getSf().setAction(2);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopList");
		mapSfn.get(2).getSf().setAction(4);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopList");
		mapSfn.get(2).getSf().setAction(5);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopList");
		mapSfn.get(2).getSf().setAction(50);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));

		
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setEntite("shopListLArticle");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopListLArticle");
		mapSfn.get(2).getSf().setAction(0);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopListLArticle");
		mapSfn.get(2).getSf().setAction(51);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopListLArticle");
		mapSfn.get(2).getSf().setAction(70);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopListLArticle");
		mapSfn.get(2).getSf().setAction(2);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopListLArticle");
		mapSfn.get(2).getSf().setAction(20);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopListLArticle");
		mapSfn.get(2).getSf().setAction(60);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));

		
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setEntite("shopShelf");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopShelf");
		mapSfn.get(2).getSf().setAction(0);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopShelf");
		mapSfn.get(2).getSf().setAction(2);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopShelf");
		mapSfn.get(2).getSf().setAction(4);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopShelf");
		mapSfn.get(2).getSf().setAction(5);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));

		
		mapSfn.put(1, new SecurityFunctionNode());
		mapSfn.get(1).getSf().setEntite("shopUser");
		mapSfn.get(0).getChilds().add(mapSfn.get(1));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopUser");
		mapSfn.get(2).getSf().setAction(0);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopUser");
		mapSfn.get(2).getSf().setAction(2);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopUser");
		mapSfn.get(2).getSf().setAction(4);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));
		mapSfn.put(2, new SecurityFunctionNode());
		mapSfn.get(2).getSf().setEntite("shopUser");
		mapSfn.get(2).getSf().setAction(5);
		mapSfn.get(1).getChilds().add(mapSfn.get(2));

		
	}
	
	public static List<SecurityFunctionNode> getElements() {
		return elements;
	}

}

