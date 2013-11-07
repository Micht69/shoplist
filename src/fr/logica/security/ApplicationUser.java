package fr.logica.security;

import java.io.Serializable;
import java.util.HashMap;

public class ApplicationUser implements Serializable {
	/** Serial UID */
	private static final long serialVersionUID = -6727149830876438487L;

	private final String login;
	private HashMap<String, Object> customData = new HashMap<String, Object>();

	public ApplicationUser(String login) {
		this.login = login;
	}

	public String getLogin() {
		return login;
	}

	public HashMap<String, Object> getCustomData() {
		return customData;
	}

	public void setCustomData(HashMap<String, Object> customData) {
		this.customData = customData;
	}

}
