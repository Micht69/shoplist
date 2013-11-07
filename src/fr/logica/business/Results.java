/**
 * 
 */
package fr.logica.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object is a wrapper for query results designed to be displayed in a list / link list. It holds a list of Result object that contains row
 * data.
 * 
 */
public class Results implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = -1090916590095719442L;

	private int resultSetCount;

	private List<Result> results;

	/**
	 * For each field of the main entity, give the corresponding column on these results. Nothing is returned if the field is not present.
	 */
	private Map<String, String> mainEntityColumnsByField = new HashMap<String, String>();

	private final KeyModel keyModel;

	private final String entityName;

	private Map<String, String> titles;

	/**
	 * Names of columns that are category breaks for categorized lists.
	 */
	private List<String> categoryBreak = new ArrayList<String>();

	public List<Result> getList() {
		return results;
	}

	public void add(Result r) {
		results.add(r);
	}

	public Key getSelectedPk(Result selectedResult) {
		return selectedResult.getPk();
	}

	public List<Key> getAllPk() {
		List<Key> keys = new ArrayList<Key>();
		for (Result result : results) {
			keys.add(result.getPk());
		}
		return keys;
	}

	public List<Key> getSelectedPk() {
		List<Key> keys = new ArrayList<Key>();
		for (Result result : results) {
			if ((Boolean) result.get("checked")) {
				keys.add(result.getPk());
			}
		}
		return keys;
	}

	public Results(Entity e) {
		results = new ArrayList<Result>();
		titles = new HashMap<String, String>();
		this.entityName = e.$_getName();
		this.keyModel = e.getModel().getKeyModel();
	}

	/**
	 * @return the keyFields
	 */
	public KeyModel getKeyModel() {
		return keyModel;
	}

	/**
	 * @return the resultCount
	 */
	public Integer getResultCount() {
		return results.size();
	}

	public int getResultSetCount() {
		return resultSetCount;
	}

	public void setResultSetCount(int resultSetCount) {
		this.resultSetCount = resultSetCount;
	}

	public Map<String, String> getTitles() {
		return titles;
	}

	public void setTitles(Map<String, String> titles) {
		this.titles = titles;
	}

	public void limitResults(int max) {
		if (results.size() > max) {
			results = results.subList(0, max);
		}
	}

	public String getEntityName() {
		return entityName;
	}

	public Map<String, String> getMainEntityColumnsByField() {
		return mainEntityColumnsByField;
	}

	public String getColumnForMainEntityField(String fieldName) {
		return mainEntityColumnsByField.get(fieldName);
	}

	public void addMainEntityField(String field, String columnName) {
		this.mainEntityColumnsByField.put(field, columnName);
	}

	public List<String> getCategoryBreak() {
		return categoryBreak;
	}

	public void setCategoryBreak(List<String> categoryBreak) {
		this.categoryBreak = categoryBreak;
	}
}
