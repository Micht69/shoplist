package fr.logica.ui;

import java.util.HashMap;
import java.util.Map;

public class UiDescriptor {

	private static final Map<String, String[]> MENU_QUERIES;

	static {
		MENU_QUERIES = new HashMap<String, String[]>();
	}

	public static Map<String, String[]> getMenuQueries() {
		return MENU_QUERIES;
	}
}
