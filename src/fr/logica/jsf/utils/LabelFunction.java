package fr.logica.jsf.utils;

import fr.logica.business.MessageUtils;

public class LabelFunction {

	public static String label(String bundle, String key) {
		return MessageUtils.getInstance().getXhtmlLabel(bundle, key);
	}
}
