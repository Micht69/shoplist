package fr.logica.business;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Key implements Serializable {

	private static final long serialVersionUID = 3861723578927708930L;
	private static final String FIELD_SEPARATOR = ";;;";
	private static final String KEY_VALUE_SEPARATOR = ":::";

	private KeyModel model;
	private Map<String, Object> values = new HashMap<String, Object>();

	/**
	 * Default constructor used by Jersey (Web Services). Use with caution.
	 */
	public Key() {
	}

	public Key(String entityName) {
		model = new KeyModel(entityName);
	}

	public Key(String entityName, String encodedKey) {
		model = new KeyModel(entityName);
		setEncodedValue(encodedKey);
	}

	public Key(KeyModel keyModel) {
		model = keyModel;
	}

	public Object getValue(String field) {
		return values.get(field);
	}

	/**
	 * Fixe les valeurs de la clé courante avec les valeurs d'une autre clé dont les noms de champs sont potentiellement différents. Cette
	 * méthode sert à fabriquer une clé primaire à partir d'une clé étrangère qui la référence ou l'inverse.
	 * 
	 * @param key Clé étrangère qui référence la clé primaire, ou l'inverse.
	 */
	public void setValue(Key key) {
		if (key.getModel().getFields().size() != getModel().getFields().size()) {
			throw new TechnicalException("Les clés n'ont pas le même nombre de champ.");
		}
		for (int i = 0; i < getModel().getFields().size(); i++) {
			setValue(getModel().getFields().get(i), key.getValue(key.getModel().getFields().get(i)));
		}
	}

	public void setValue(String field, Object val) {
		values.put(field, val);
	}

	public KeyModel getModel() {
		return model;
	}

	public String getEncodedValue() {
		StringBuilder encodedKey = new StringBuilder();
		boolean first = true;
		for (String fieldName : values.keySet()) {
			if (!first) {
				encodedKey.append(FIELD_SEPARATOR);
			}
			encodedKey.append(fieldName);
			encodedKey.append(KEY_VALUE_SEPARATOR);
			encodedKey.append(values.get(fieldName));
			first = false;
		}
		return encodedKey.toString();
	}

	public void setEncodedValue(String encodedString) {
		if (encodedString == null) {
			values.clear();
			return;
		}
		String[] fieldValues = encodedString.split(FIELD_SEPARATOR);
		values.clear();
		for (int i = 0; i < fieldValues.length; i++) {
			String[] valTab = fieldValues[i].split(KEY_VALUE_SEPARATOR);
			if (valTab.length > 1) {
				String field = valTab[0];
				Object value = valTab[1];
				values.put(field, value);
			}
		}
	}

	public void setEncodedValueNull(String encodedString) {
		String[] fieldValues = encodedString.split(FIELD_SEPARATOR);
		values.clear();
		for (int i = 0; i < fieldValues.length; i++) {
			if (fieldValues[i].split(KEY_VALUE_SEPARATOR).length > 1) {
				String field = fieldValues[i].split(KEY_VALUE_SEPARATOR)[0];
				String value = fieldValues[i].split(KEY_VALUE_SEPARATOR)[1];
				if ("null".equals(fieldValues[i].split(KEY_VALUE_SEPARATOR)[1])) {
					value = null;
				}
				values.put(field, value);
			}
		}
	}

	/**
	 * All key variables are null
	 * 
	 * @return <code>true</code> if the key is null, every single variable is null. <code>false</code> otherwise
	 */
	public boolean isNull() {
		for (Object value : values.values()) {
			if (value != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * None of key variables is null
	 * 
	 * @return <code>true</code> if the key is full, no variable is null. <code>false</code> otherwise
	 */
	public boolean isFull() {
		for (Object value : values.values()) {
			if (value == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the current key contains all values of the partial key passed in parameter. Null values of the partial key aren't checked. For
	 * instance, if key is (var1='pouet' and var2='toto') and partialKey is (var1=null and var2='toto'), it will return true.
	 * 
	 * 
	 * @param partialKey A key with the same fields, but not all values.
	 * @return <code>true</code> if all values of the partial Key are in the current key
	 */
	public boolean contains(Key partialKey) {
		for (String field : model.getFields()) {
			if (partialKey.getValue(field) == null) {
				continue;
			}
			if (!partialKey.getValue(field).equals(getValue(field))) {
				return false;
			}
		}
		return true;
	}

	public void nullify() {
		values.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Key)) {
			return false;
		}
		Key other = (Key) obj;
		if (model == null) {
			if (other.model != null) {
				return false;
			}
		} else if (!model.equals(other.model)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

	/**
	 * Compares values of current key instance with another key values. This method ensures that both keys have the same number of fields and
	 * compares them based on field order. This method can be used to check if a foreign key values equals a primary key values.
	 * 
	 * @param otherKey The key to compare
	 * @return true if both keys have the same number of fields and same values, false otherwise
	 */
	public boolean hasSameValues(Key otherKey) {
		if (otherKey == null) {
			return false;
		}
		if (otherKey.getModel().getFields().size() != this.model.getFields().size()) {
			return false;
		}

		for (int i = 0; i < model.getFields().size(); i++) {
			Object value = this.getValue(this.getModel().getFields().get(i));
			Object otherValue = otherKey.getValue(otherKey.getModel().getFields().get(i));
			if (value == null && otherValue != null) {
				return false;
			}
			if (value != null && !value.equals(otherValue)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Key [model=" + model + ", values=" + values + "]";
	}
}
