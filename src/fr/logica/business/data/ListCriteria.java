package fr.logica.business.data;

import java.io.Serializable;

import fr.logica.business.Constants;

public class ListCriteria implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6641435660404108818L;
	
	public String orderByField;
	public String orderByDirection;
	public int minRow = 0;
	public int maxRow = Constants.MAX_ROW;
	public String searchCriteria;

	public void sortBy(String field) {
		if (orderByField != null && orderByField.equals(field)) {
			if ("ASC".equals(orderByDirection)) {
				orderByDirection = "DESC";
			} else if ("DESC".equals(orderByDirection)) {
				orderByField = null;
				orderByDirection = null;
			}
		} else {
			orderByField = field;
			orderByDirection = "ASC";
		}
	}
}
