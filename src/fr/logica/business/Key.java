package fr.logica.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Key implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(Key.class);

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
			Object v = values.get(fieldName);
			String sValue = String.valueOf(v);
			if (v == null) {
				encodedKey.append("N");
				sValue = "";
			} else if (v instanceof Integer) {
				encodedKey.append("I");
			} else if (v instanceof String) {
				encodedKey.append("S");
			} else if (v instanceof Long) {
				encodedKey.append("L");
			} else if (v instanceof Boolean) {
				encodedKey.append("B");
			} else if (v instanceof BigDecimal) {
				encodedKey.append("F");
			} else if (v instanceof Date) {
				sValue = String.valueOf(((Date) v).getTime());
				if (v instanceof Timestamp) {
					encodedKey.append("T");
				} else if (v instanceof Time) {
					encodedKey.append("H");
				} else if (v instanceof Date) {
					encodedKey.append("D");
				}
			} else {
				LOGGER.error("Error in Key " + model == null ? "<keyModel is null>" : model.getName());
				LOGGER.error("Unable to serialize Key value " + sValue + " of type " + v.getClass().getSimpleName() + " in field " + fieldName);
			}
			encodedKey.append(sValue);
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
				String sValue = valTab[1];
				if (sValue.length() == 0) {
					LOGGER.error("Error in Key " + model == null ? "<keyModel is null>" : model.getName());
					LOGGER.error("Unable to deserialize Key value for field " + field);
				} else {
					String sType = sValue.substring(0, 1);
					sValue = sValue.substring(1);

					Object value = null;

					if ("N".equals(sType)) {
						// NULL value
						value = null;
					} else if ("S".equals(sType)) {
						value = sValue;
					} else if ("I".equals(sType)) {
						value = Integer.parseInt(sValue);
					} else if ("L".equals(sType)) {
						value = Long.parseLong(sValue);
					} else if ("B".equals(sType)) {
						value = Boolean.valueOf(sValue);
					} else if ("F".equals(sType)) {
						value = new BigDecimal(sValue);
					} else if ("D".equals(sType) || "H".equals(sType) || "T".equals(sType)) {
						long ts = Long.parseLong(sValue);
						if ("D".equals(sType)) {
							value = new Date(ts);
						} else if ("H".equals(sType)) {
							value = new Time(ts);
						} else if ("T".equals(sType)) {
							value = new Timestamp(ts);
						}
					} else {
						LOGGER.error("Error in Key " + model == null ? "<keyModel is null>" : model.getName());
						LOGGER.error("Unable to deserialize Key value for field " + field + " - unknown field type " + sType);
					}
					values.put(field, value);
				}
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
