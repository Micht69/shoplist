package fr.logica.business.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import fr.logica.application.ApplicationUtils;

public class ApplicationContext implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -1304368564807700534L;

	private String appName = "";

	private String appDescription = "";

	private String appVersion = "";

	private Map<String, Object> attributes = new HashMap<String, Object>();

	public ApplicationContext() {
		ApplicationUtils.getApplicationLogic().initializeApplication(this);
	}

	public void finalize() {
		ApplicationUtils.getApplicationLogic().finalizeApplication(this);
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppDescription() {
		return appDescription;
	}

	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

}
