/**
 * 
 */
package fr.logica.business;

/**
 * Class representing an allowed value for an EntityField.<br/>
 * This is a treple with a code, a label and a value.
 * 
 * @author CGI
 */
public class DefinedValue {
	/** Programmer code for the defined value */
	final private String code;
	/** Display label (onto screen) for this allowed value */
	final private String label;
	/** Actual value object for this allowed value */
	final private Object value;

	/**
	 * Create a new allowed value
	 * 
	 * @param code
	 *            String representing programmer code
	 * @param label
	 *            String matching property key for screen display
	 * @param value
	 *            Value Object
	 */
	public DefinedValue(String code, String label, Object value) {
		super();
		this.code = code;
		this.label = label;
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public Object getValue() {
		return value;
	}

}
