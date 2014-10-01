package fr.logica.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntityField {

	/** Supported SQL data types */
	public enum SqlTypes {
		BLOB,
		BOOLEAN,
		CHAR,
		CLOB,
		DATE,
		DECIMAL,
		INTEGER,
		TIME,
		TIMESTAMP,
		VARCHAR,
		VARCHAR2
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

	/** SQL field name */
	private final String sqlName;
	/** SQL type */
	private final SqlTypes sqlType;
	/** SQL field size */
	private int sqlSize = -1;
	/** SQL decimal accuracy */
	private int sqlAccuracy = -1;
	/** Memory variable type */
	private Memory memory;
	/** SQL expression */
	private String sqlExpr;
	/** Default value */
	private Object defaultValue;

	private boolean isMandatory = false;
	private boolean isLookupField;

	private final List<DefinedValue> definedValues;

	public EntityField(String sqlName, SqlTypes sqlType, int sqlSize, int sqlAccuracy, Memory memory, boolean isMandatory, boolean isLookupField) {
		super();
		this.sqlName = sqlName;
		this.sqlType = sqlType;
		this.sqlSize = sqlSize;
		this.sqlAccuracy = sqlAccuracy;
		this.memory = memory;
		this.isMandatory = isMandatory;
		this.isLookupField = isLookupField;
		this.definedValues = new ArrayList<DefinedValue>();
	}

	public String getSqlName() {
		return sqlName;
	}

	public SqlTypes getSqlType() {
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

	public List<DefinedValue> getDefinedValues() {
		return definedValues;
	}

	public List<Object> getOldValues() {
		List<Object> values = new ArrayList<Object>();
		for (DefinedValue defVal : definedValues)
			values.add(defVal.getValue());

		return values;
	}

	public Object getDefValValue(int index) {
		return definedValues.get(index).getValue();
	}

	public String getDefValLabel(int index) {
		return definedValues.get(index).getLabel();
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean hasDefinedValues() {
		return (definedValues.size() > 0);
	}

	public int nbDefinedValues() {
		return definedValues.size();
	}

	public String getDefinedLabel(Object value, Locale l) {
		for (DefinedValue defVal : definedValues) {
			if (value == null && defVal.getValue() == null) {
				return MessageUtils.getInstance(l).getGenLabel(defVal.getLabel(), (Object[]) null);
			} else if (defVal.getValue() == null) {
				continue;
			}
			if (defVal.getValue().equals(value)) {
				return MessageUtils.getInstance(l).getGenLabel(defVal.getLabel(), (Object[]) null);
			}
		}
		return null;
	}

	public String getDefLabel(String code, Locale l) {
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return MessageUtils.getInstance(l).getGenLabel(defVal.getLabel(), (Object[]) null);
			}
		}
		return null;
	}

	public boolean isDefValue(Object value) {
		for (DefinedValue defVal : definedValues) {
			if (value == null && defVal.getValue() == null) {
				return true;
			} else if (defVal.getValue() == null) {
				continue;
			}
			if (defVal.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public boolean isDefCode(String code) {
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	public String getDefValue(String code) {
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return defVal.getValue().toString();
			}
		}
		return null;
	}

	public Boolean getBooleanDefValue(String code) {
		if (code == null) {
			return (Boolean) getDefaultValue();
		}
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return (Boolean) defVal.getValue();
			}
		}
		return Boolean.FALSE;
	}

	public String getSqlExpr() {
		return sqlExpr;
	}

	public void setSqlExpr(String sqlExpr) {
		this.sqlExpr = sqlExpr;
	}

	public boolean isAlpha() {
		return SqlTypes.VARCHAR2.equals(sqlType) || SqlTypes.CHAR.equals(sqlType) || SqlTypes.CLOB.equals(sqlType);
	}

}
