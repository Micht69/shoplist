package fr.logica.security;

import java.io.Serializable;

public class Profile implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/** Profile */
	private String profile;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}


}
