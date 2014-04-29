package fr.logica.business.data;

import java.io.Serializable;
import java.util.Map;

import fr.logica.business.Key;

public class ComboData implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 4405265421014571902L;

	private String entityName;

	private Map<Key, String> comboValues;

	public ComboData(String entityName, Map<Key, String> comboValues) {
		this.entityName = entityName;
		this.comboValues = comboValues;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Map<Key, String> getComboValues() {
		return comboValues;
	}

	public void setComboValues(Map<Key, String> comboValues) {
		this.comboValues = comboValues;
	}

}
