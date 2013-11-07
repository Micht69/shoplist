/**
 * 
 */
package fr.logica.business;

import java.util.Date;
import java.util.HashMap;

/**
 * @author logica
 * 
 */
public class Criteria extends HashMap<String, Object> {

	/** serialVersionUID */
	private static final long serialVersionUID = -3054986026570929881L;

	private static String CRITERIA_SEPARATOR = "|||";
	private static String CRITERIA_ESCAPED_SEPARATOR = "\\|\\|\\|";

	private static String KEY_VALUE_SEPARATOR = "###";

	private static String ARRAY_SEPARATOR = ",,,";

	private static String ARRAY_TYPE = "__A__";
	private static String DATE_TYPE = "__D__";

	private String orderByField;
	private String orderByDirection;

	public static String MAX_ROW = "maxRow";

	public Criteria() {
		this.put(MAX_ROW, "200");
	}

	public Criteria(String serializedCriteria) {
		clear();
		String[] keyValueList = serializedCriteria.split(CRITERIA_ESCAPED_SEPARATOR);

		for (int i = 0; i < keyValueList.length; i++) {
			if (keyValueList[i].split(KEY_VALUE_SEPARATOR).length > 1) {
				String key = keyValueList[i].split(KEY_VALUE_SEPARATOR)[0];
				String value = keyValueList[i].split(KEY_VALUE_SEPARATOR)[1];

				if (value.startsWith(ARRAY_TYPE)) {
					String[] arrayValues = value.substring(5).split(ARRAY_SEPARATOR);
					put(key, arrayValues);
				} else if (value.startsWith(DATE_TYPE)) {
					Date d = new Date(Long.valueOf(value.substring(5)));
					put(key, d);
				} else {
					put(key, value);
				}
			}
		}
	}

	public String serialize() {
		StringBuilder s = new StringBuilder();
		for (String key : keySet()) {
			if (s.length() > 0) {
				s.append(CRITERIA_SEPARATOR);
			}

			s.append(key);
			s.append(KEY_VALUE_SEPARATOR);

			Object o = this.get(key);
			if (o instanceof String) {
				s.append(o);
			} else if (o instanceof Date) {
				s.append(DATE_TYPE);
				s.append(((Date) o).getTime());
			} else if (o instanceof String[]) {
				String[] array = (String[]) o;
				s.append(ARRAY_TYPE);
				for (int i = 0; i < array.length; i++) {
					if (i > 0) {
						s.append(ARRAY_SEPARATOR);
					}
					s.append(array[i]);
				}
			}
		}
		return s.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object put(String key, Object value) {
		if (MAX_ROW.equals(key)) {
			try {
				Integer.parseInt((String) value);
			} catch (NumberFormatException ex) {
				value = "200";
			}
		}
		if (value != null && !(value instanceof String[] && ((String[]) value).length == 0)) {
			return super.put(key, value);
		} else {
			if (containsKey(key)) {
				remove(key);
			}
			return null;
		}
	}

	public String getOrderByField() {
		return orderByField;
	}

	public void setOrderByField(String orderByField) {
		this.orderByField = orderByField;
	}

	public String getOrderByDirection() {
		return orderByDirection;
	}

	public void setOrderByDirection(String orderByDirection) {
		this.orderByDirection = orderByDirection;
	}

}
