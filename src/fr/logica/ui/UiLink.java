package fr.logica.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.logica.business.Key;
import fr.logica.business.Results;

public class UiLink {

	public enum Type {
		COMBO, LINK, LINK_LIST, BACK_REF
	}

	private Type type;

	private Map<Key, String> comboValues;
	private Results results;
	private String description;
	private String queryName;
	private String searchQueryName;

	public String getSearchQueryName() {
		return searchQueryName;
	}

	public void setSearchQueryName(String searchQueryName) {
		this.searchQueryName = searchQueryName;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UiLink(Type t) {
		type = t;
	}

	public void setComboValues(Map<Key, String> cv) {
		this.comboValues = cv;
	}

	public Map<String, String> getComboValues() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (comboValues != null) {
			for (Key k : comboValues.keySet()) {
				map.put(comboValues.get(k), k.getEncodedValue());
			}
		}
		return map;
	}

	public Map<String, String> getFilteredComboValues(Key partialKey) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (comboValues != null) {
			for (Key k : comboValues.keySet()) {
				if (k.contains(partialKey)) {
					map.put(comboValues.get(k), k.getEncodedValue());
				}
			}
		}
		return map;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Results getResults() {
		return results;
	}

	public void setResults(Results results) {
		this.results = results;
	}

}
