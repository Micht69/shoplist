package fr.logica.business;

import fr.logica.business.Constants;

public class ActionUtils {

	public static boolean isKeyEditable(int action) {
		return (action == Constants.CREATE);
	}

	public static boolean isDataEditable(int typeAction) {
		return (typeAction != Constants.DISPLAY && typeAction != Constants.DELETE && typeAction != Constants.DETACH);
	}

	public static boolean isSingleProcess(int typeAction) {
		return false;
	}

	public static boolean hasNoDisplay(int typeAction) {
		if (typeAction == Constants.DETACH || typeAction == Constants.CUSTOM_ACTION || typeAction == Constants.NO_ELT_CUSTOM_ACTION
				|| typeAction == Constants.SINGLE_ELT_CUSTOM_ACTION) {
			return true;
		}
		return false;
	}

	public static boolean isCustom(int typeAction) {
		if (typeAction == Constants.NO_ELT_CUSTOM_ACTION						//  8
				|| typeAction == Constants.NO_ELT_CUSTOM_ACTION_DISPLAY			// 14
				|| typeAction == Constants.CUSTOM_ACTION						// 10
				|| typeAction == Constants.CUSTOM_ACTION_DISPLAY				// 11
				|| typeAction == Constants.SINGLE_ELT_CUSTOM_ACTION				// 16
				|| typeAction == Constants.SINGLE_ELT_CUSTOM_ACTION_DISPLAY) {	// 13
			return true;
		}
		return false;
	}

	public static boolean persistOneToOneLinks(int typeAction) {
		return (typeAction == Constants.CREATE || typeAction == Constants.MODIFY || typeAction == Constants.COPY);
	}

}
