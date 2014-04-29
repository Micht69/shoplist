package fr.logica.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.logica.business.TechnicalException;

public class UiManager {

	private static Class<?> uiDescriptor;

	private static Class<?> getUiDescriptor() throws ClassNotFoundException {
		if (uiDescriptor == null) {
			uiDescriptor = Class.forName("fr.logica.ui.UiDescriptor");
		}
		return uiDescriptor;
	}


	@SuppressWarnings("unchecked")
	public static Map<String, String[]> getMenuQueries() {
		try {

			Method getMenuQueriesMethod = getUiDescriptor().getMethod("getMenuQueries");
			return (Map<String, String[]>) getMenuQueriesMethod.invoke(getUiDescriptor());

		} catch (IllegalAccessException e) {
			throw new TechnicalException(e.getMessage(), e);

		} catch (ClassNotFoundException e) {
			throw new TechnicalException(e.getMessage(), e);

		} catch (IllegalArgumentException e) {
			throw new TechnicalException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new TechnicalException(e.getMessage(), e);
		} catch (SecurityException e) {
			throw new TechnicalException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new TechnicalException(e.getMessage(), e);
		}
	}

}
