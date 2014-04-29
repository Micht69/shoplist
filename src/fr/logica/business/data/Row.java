package fr.logica.business.data;

import java.util.HashMap;

import fr.logica.business.Constants;
import fr.logica.business.Key;

/** One element in a result list, on any list page. */
public class Row extends HashMap<String, Object> {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Current row primary key
	 * 
	 * @return Primary key of this row main entity
	 */
	public Key getPk() {
		return (Key) this.get(Constants.RESULT_PK);
	}

	public void setPk(Key pk) {
		this.put(Constants.RESULT_PK, pk);
	}

	/**
	 * Tests if the current row instance has been "checked" in UI
	 * 
	 * @return true when the current row is "checked" or "selected by user", false otherwise
	 */
	public boolean checked() {
		if (get("checked") != null && get("checked") instanceof Boolean) {
			return ((Boolean) get("checked")).booleanValue();
		}
		return false;
	}
}
