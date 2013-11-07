package fr.logica.ui;


import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.Entity;
import fr.logica.business.Results;
import fr.logica.reflect.DomainUtils;



public class ListPage<E extends Entity> extends Page<E> {

	/** SerialUID */
	private static final long serialVersionUID = 846505094991465773L;
	private static final String PAGE_NAME_DEFAULT_SUFFIX = "_LIST";

	private String queryName;
	private String pageName;

	private int maxRow;
	private String sortByField;
	private boolean sortByDesc;
	private E criteria;

	private boolean selectPage;
	private String selectLink;
	private String exportType = null;

	private boolean displayCriterias;

	/** Only for editable lists */
	private Action editListAction;

	/**
	 * Results
	 */
	public Results results;
	
	

	@Override
	public String getUrl() {
		return  "/" + getDomainName() + "/" + getPageName();
	}

	@Override
	public String getDomainName() {
		return criteria.$_getName();
	}

	public String getSortByDirection() {
		if (sortByDesc) {
			return "DESC";
		}
		return "ASC";
	}

	public Results getResults() {
		return results;
	}

	public String getQueryName() {
		return queryName;
	}

	public String getPageName() {
		if (pageName == null) {
			if (queryName != null) {
				pageName = queryName + PAGE_NAME_DEFAULT_SUFFIX;
			} else {
				pageName = DomainUtils.createDbName(getDomainName()) + PAGE_NAME_DEFAULT_SUFFIX;
			}
		}
		return pageName;
	}

	public int getMaxRow() {
		return maxRow;
	}

	public String getSortByField() {
		return sortByField;
	}

	public boolean isSortByDesc() {
		return sortByDesc;
	}

	public void setSortByDesc(boolean sortByDesc) {
		this.sortByDesc = sortByDesc;
	}

	public E getCriteria() {
		return criteria;
	}

	public void setCriteria(E criteria) {
		this.criteria = criteria;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public void setMaxRow(int maxRow) {
		this.maxRow = maxRow;
	}

	public void setSortByField(String sortByField) {
		this.sortByField = sortByField;
	}

	public void setResults(Results results) {
		this.results = results;
	}

	public boolean isSelectPage() {
		return selectPage;
	}

	public void setSelectPage(boolean selectPage) {
		this.selectPage = selectPage;
	}

	public String getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(String selectLink) {
		this.selectLink = selectLink;
	}

	@Override
	public E getBean() {
		return criteria;
	}

	public void setBean(E bean) {
		this.criteria = bean;
	}

	public boolean isDisplayCriterias() {
		return displayCriterias;
	}

	public void setDisplayCriterias(boolean displayCriterias) {
		this.displayCriterias = displayCriterias;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public Action getEditListAction() {
		return editListAction;
	}

	public void setEditListAction(Action editListAction) {
		this.editListAction = editListAction;
	}
	
	
}
