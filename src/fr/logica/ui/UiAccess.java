package fr.logica.ui;

import java.io.Serializable;

public class UiAccess implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 8012941316941268894L;

	public String name;
	public boolean visible;
	public boolean readOnly;
	public String label;
	public boolean mandatory;

	public UiAccess(String name, boolean isVisible, boolean isReadOnly, String label, boolean mandatory) {
		this.name = name;
		this.visible = isVisible;
		this.readOnly = isReadOnly;
		this.label = label;
		this.mandatory = mandatory;
	}

}
