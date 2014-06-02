package fr.logica.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntityField {
	/** SQL field name */
	private final String sqlName;
	/** SQL type */
	private final String sqlType;
	/** SQL field size */
	private int sqlSize = -1;
	/** SQL decimal accuracy */
	private int sqlAccuracy = -1;
	/** Memory variable type */
	private Memory memory;
	/** SQL expression */
	private String sqlExpr;
	/** Display text */
	private String displayText = "";
	/** Default value */
	private String defaultCode;
	private Object defaultValue;

	private boolean isMandatory = false;
	private boolean isLookupField;

	private final List<String> codes;
	private final List<Object> values;
	private final List<String> labels;

	public EntityField(String sqlName, String sqlType, int sqlSize, int sqlAccuracy, Memory memory, boolean isMandatory, boolean isLookupField,
			String displayText) {
		this(sqlName, sqlType, sqlSize, sqlAccuracy, memory, isMandatory, isLookupField);
		this.displayText = displayText;
	}

	public EntityField(String sqlName, String sqlType, int sqlSize, int sqlAccuracy, Memory memory, boolean isMandatory, boolean isLookupField) {
		super();
		this.sqlName = sqlName;
		this.sqlType = sqlType;
		this.sqlSize = sqlSize;
		this.sqlAccuracy = sqlAccuracy;
		this.memory = memory;
		this.isMandatory = isMandatory;
		this.isLookupField = isLookupField;
		this.codes = new ArrayList<String>();
		this.values = new ArrayList<Object>();
		this.labels = new ArrayList<String>();
	}

	/** Behavior for in-memory (calculated) variables */
	public enum Memory {
		/** Not an in-memory variable : persisted in database */
		NO,

		/** Always recaculated each time we get a chance */
		ALWAYS,

		/** Never calculated : used to pass parameters to custom actions */
		NEVER,

		/** SQL scalar expression */
		SQL
	}

	public String getSqlName() {
		return sqlName;
	}

	/**
	 * FIXME independant of db ?<br>
	 * TODO define constants to manipulate types
	 * 
	 * @return the sqlType
	 */
	public String getSqlType() {
		return sqlType;
	}

	public int getSqlSize() {
		return sqlSize;
	}

	public int getSqlAccuracy() {
		return sqlAccuracy;
	}

	/**
	 * @return Memory variable type.
	 */
	public Memory getMemory() {
		return memory;
	}

	/** Returns <code>true</code> for in-memory (calculated) variables */
	public boolean isTransient() {
		return memory != Memory.NO;
	}

	/** Returns <code>true</code> for SQL variables (either true columns or SQL expression) */
	public boolean isFromDatabase() {
		return memory == Memory.NO || memory == Memory.SQL;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public boolean isLookupField() {
		return isLookupField;
	}

	public List<String> getCodes() {
		return codes;
	}

	public List<Object> getValues() {
		return values;
	}

	public List<String> getLabels() {
		return labels;
	}

	public String getDefaultCode() {
		return defaultCode;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultCode, Object defaultValue) {
		this.defaultCode = defaultCode;
		this.defaultValue = defaultValue;
	}

	public boolean hasDefinedValues() {
		return (codes.size() > 0);
	}

	public String getCode(boolean value) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).equals(value)) {
				return codes.get(i);
			}
		}
		return getDefaultCode();
	}

	public String getBooleanCode(boolean value) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).equals(Boolean.valueOf(value))) {
				return codes.get(i);
			}
		}
		return getDefaultCode();
	}

	public String getCode(String value) {
		for (int i = 0; i < values.size(); i++) {
			// SSCH : Transformation des valeurs en String pour comparaison
			if (values.get(i).toString().equals(value)) {
				return codes.get(i);
			}
		}
		return getDefaultCode();
	}

	public String getDefinedLabel(Object value, Locale l) {
		for (int i = 0; i < values.size(); i++) {
			if (value == null && values.get(i) == null) {
				return MessageUtils.getInstance(l).getGenLabel(labels.get(i), null);
			} else if (values.get(i) == null) {
				continue;
			}
			if (values.get(i).equals(value)) {
				return MessageUtils.getInstance(l).getGenLabel(labels.get(i), null);
			}
		}
		return null;
	}

	public String getDefLabel(String code, Locale l) {
		for (int i = 0; i < codes.size(); i++) {
			if (codes.get(i).equals(code)) {
				return MessageUtils.getInstance(l).getGenLabel(labels.get(i), null);
			}
		}
		if (code == null) {
			return getDefaultCode();
		}
		return null;
	}

	public boolean isCode(String code) {
		for (int i = 0; i < codes.size(); i++) {
			if (codes.get(i).equals(code)) {
				return true;
			}
		}
		return false;
	}

	public String getDefValue(String code) {
		for (int i = 0; i < codes.size(); i++) {
			if (codes.get(i).equals(code)) {
				return values.get(i).toString();
			}
		}
		if (code == null) {
			return getDefaultCode();
		}
		return null;
	}

	public Boolean getBooleanDefValue(String code) {
		if (code == null) {
			code = getDefaultCode();
		}
		for (int i = 0; i < codes.size(); i++) {
			if (codes.get(i).equals(code)) {
				return (Boolean) values.get(i);
			}
		}
		return Boolean.FALSE;
	}

	public String getDisplayText() {
		return this.displayText;
	}

	public String getSqlExpr() {
		return sqlExpr;
	}

	public void setSqlExpr(String sqlExpr) {
		this.sqlExpr = sqlExpr;
	}

	public boolean isAlpha() {
		return "VARCHAR2".equals(sqlType) || "CHAR".equals(sqlType) || "CLOB".equals(sqlType);
	}

}
