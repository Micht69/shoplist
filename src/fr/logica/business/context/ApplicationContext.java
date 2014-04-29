package fr.logica.business.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -1304368564807700534L;

	private String appName = "demo";

	private String appDescription = "Application de DEMO";

	private String appVersion = "3.0.0";

	private Map<String, Object> attributes = new HashMap<String, Object>();

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
