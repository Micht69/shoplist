package fr.logica.business;

import fr.logica.business.Action;
import fr.logica.business.Constants;

public class ActionUtils {

	public static boolean isKeyEditable(int action) {
		return (action == Constants.CREATE);
	}

	public static boolean isDataEditable(int typeAction) {
		return (typeAction != Constants.DISPLAY && typeAction != Constants.DELETE
				&& typeAction != Constants.DETACH && typeAction != Constants.DETACH_BR);
	}

	public static boolean isSingleProcess(int typeAction) {
		return false;
	}

	public static boolean hasNoDisplay(int typeAction) {
		if (typeAction == Constants.DETACH || typeAction == Constants.DETACH_BR || typeAction == Constants.CUSTOM_ACTION
				|| typeAction == Constants.NO_ELT_CUSTOM_ACTION || typeAction == Constants.SINGLE_ELT_CUSTOM_ACTION) {
			return true;
		}
		return false;
	}

	public static boolean isCustom(int typeAction) {
		if (typeAction == Constants.NO_ELT_CUSTOM_ACTION // 8
				|| typeAction == Constants.NO_ELT_CUSTOM_ACTION_DISPLAY // 14
				|| typeAction == Constants.CUSTOM_ACTION // 10
				|| typeAction == Constants.CUSTOM_ACTION_DISPLAY // 11
				|| typeAction == Constants.SINGLE_ELT_CUSTOM_ACTION // 16
				|| typeAction == Constants.SINGLE_ELT_CUSTOM_ACTION_DISPLAY) { // 13
			return true;
		}
		return false;
	}

	public static boolean persistOneToOneLinks(int typeAction) {
		return (typeAction == Constants.CREATE || typeAction == Constants.MODIFY || typeAction == Constants.COPY);
	}

	public static boolean isSingleProcess(Action action) {
		if (action.type == Constants.MODIFY
				|| action.type == Constants.COPY
				|| action.type == Constants.DELETE
				|| action.type == Constants.DISPLAY
				|| action.type == Constants.CUSTOM_ACTION
				|| action.type == Constants.CUSTOM_ACTION_DISPLAY) {
			return true;
		}
		return false;
	}

	public static boolean isNoneProcess(Action action) {
		if (action.type == Constants.CREATE
				|| action.type == Constants.NO_ELT_CUSTOM_ACTION
				|| action.type == Constants.NO_ELT_CUSTOM_ACTION_DISPLAY) {
			return true;
		}
		return false;
	}

	public static boolean hasDisplay(Action action) {
		if (action.type == Constants.DETACH
				|| action.type == Constants.NO_ELT_CUSTOM_ACTION
				|| action.type == Constants.SINGLE_ELT_CUSTOM_ACTION
				|| action.type == Constants.CUSTOM_ACTION
				|| action.type == -2) {
			return false;
		}
		return true;
	}

	public static boolean isSelectAction(Action action) {
		return (action.type == Constants.SELECT || action.type == -3);
	}

}
