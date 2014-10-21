

package fr.logica.business.context;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.logica.application.logic.User;

public class SessionContext implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -7704108463339522771L;

	private ApplicationContext appContext;

	private User user;

	private Locale locale;
	private Map<String, Object> attributes = new HashMap<String, Object>();


	public SessionContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	
	public ApplicationContext getAppContext() {
		return appContext;
	}

	public void setAppContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public void close() {
		
	}

}
