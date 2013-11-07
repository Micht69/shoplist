package fr.logica.ui;

import java.util.ArrayList;
import java.util.List;

public class UiElement {

	public List<UiElement> elements = new ArrayList<UiElement>();

	public enum Type {
		/** Link simple reference */
		LINK,
		/** Link simple reference and quick search */
		LINK_QUICK_SEARCH,
		/** Link in a combobox */
		LINK_COMBO,
		/** Link in a multi-combobox */
		LINK_MULTI_COMBO,
		/** Link in an inner template */
		LINK_INNER,
		/** Backref simple reference */
		BACK_REF,
		/** Backref list */
		BACK_REF_LIST,
		/** Backref list with input */
		BACK_REF_LIST_INPUT,
		/** Backref list displayed as a schedule */
		BACK_REF_LIST_SCHEDULE,
		/** Backref in an inner template */
		BACK_REF_INNER
	}

	public String entityName;
	public String linkName;
	public String queryName;
	public String searchQueryName;
	public Type type;

	public UiElement(String pEntityName, String pLinkName, String pQueryName, String pSearchQueryName, Type pType) {
		entityName = pEntityName;
		linkName = pLinkName;
		queryName = pQueryName;
		searchQueryName = pSearchQueryName;
		type = pType;
	}

	@Override
	public String toString() {
		return "UiElement [entityName=" + entityName + ", linkName=" + linkName + ", queryName=" + queryName + ", searchQueryName="
				+ searchQueryName + ", type=" + type + ", " + elements.size() + " elements]";
	}
}
