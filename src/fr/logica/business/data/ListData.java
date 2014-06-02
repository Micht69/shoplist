package fr.logica.business.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.logica.business.EntityManager;
import fr.logica.business.KeyModel;

public class ListData implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -5861359143964068300L;

	protected final String entityName;

	protected final KeyModel keyModel;

	protected List<Row> rows;

	protected int totalRowCount;

	protected Map<String, ColumnData> columns;

	protected boolean isProtected = false;
	
	protected boolean isReadOnly = false;

	public List<Row> getRows() {
		return rows;
	}

	public ListData(String entityName) {
		rows = new ArrayList<Row>();
		columns = new HashMap<String, ColumnData>();
		this.entityName = entityName;
		this.keyModel = EntityManager.getEntityModel(entityName).getKeyModel();
	}

	public void add(Row row) {
		rows.add(row);
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
		return rows.size();
	}

	public Map<String, ColumnData> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, ColumnData> columns) {
		this.columns = columns;
	}

	public String getEntityName() {
		return entityName;
	}

	public int getTotalRowCount() {
		return totalRowCount;
	}

	public void setTotalRowCount(int totalRowCount) {
		this.totalRowCount = totalRowCount;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}
	
	public boolean isReadOnly(){
		return isReadOnly;
	}
	
	public void setReadOnly(boolean readOnly){
		isReadOnly = readOnly;
	}
}
