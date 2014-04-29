package fr.logica.business.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListCategoryData extends ListData implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -5861359143964068300L;

	/**
	 * Names of columns that are category breaks for categorized lists.
	 */
	private List<String> categoryBreak = new ArrayList<String>();

	public ListCategoryData(String entityName) {
		super(entityName);
		categoryBreak = new ArrayList<String>();
	}

	/**
	 * Constructor that may be used to build ListCategoryData from a ListData, this may prevent problems in UI when trying to display a
	 * categorized list with no category in the query.
	 * 
	 * @param listData
	 */
	public ListCategoryData(ListData listData) {
		super(listData.getEntityName());
		categoryBreak = new ArrayList<String>();
		this.setColumns(listData.getColumns());
		this.setTotalRowCount(listData.getTotalRowCount());
		for (Row row : listData.getRows()) {
			this.add(row);
		}
		this.setProtected(listData.isProtected());
	}

	public List<String> getCategoryBreak() {
		return categoryBreak;
	}

	public void setCategoryBreak(List<String> categoryBreak) {
		this.categoryBreak = categoryBreak;
	}
}
