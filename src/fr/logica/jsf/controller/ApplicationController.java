package fr.logica.jsf.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import fr.logica.application.ApplicationUtils;
import fr.logica.business.context.ApplicationContext;
import fr.logica.db.ConnectionLogger;
import fr.logica.db.ConnectionObject;

public class ApplicationController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 726513615928591066L;

	@PostConstruct
	public void initialize() {
		// Init context
		getContext();
	}

	@PreDestroy
	public void finalizeController() {
		// Close connections
		for (ConnectionObject conn : getListConnections()) {
			conn.close();
		}
		// Close context
		getContext().finalizeContext();
	}

	public ApplicationContext getContext() {
		return ApplicationUtils.getApplicationContext();
	}

	public List<ConnectionObject> getListConnections() {
		return ConnectionLogger.getInstance().getListConnections();
	}

	public String closeConnection(int id) {
		ConnectionLogger.getInstance().closeConnection(id);
		return null;
	}

	public boolean isDisplaySelectActions() {
		return ApplicationUtils.getApplicationLogic().enableSelectActions();
	}

	public boolean isDisplayXlsExport() {
		return ApplicationUtils.getApplicationLogic().enableXlsExport();
	}

	public boolean isDisplayPermalink() {
		return ApplicationUtils.getApplicationLogic().enablePermalink();
	}
}
