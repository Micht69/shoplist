package fr.logica.security;

import java.io.Serializable;
import java.util.List;

import fr.logica.application.logic.User;
import fr.logica.business.context.SessionContext;

public abstract class AbstractSecurityManager implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -5307524579033289661L;

	public abstract User getUser(String login, String password);

	public abstract List<SecurityFunction> getSecurity(User user);

	public abstract boolean disableSecurity();

	public abstract boolean isListRendered(String queryName, SessionContext ctx);

	public abstract boolean isActionRendered(String entityName, int action, SessionContext context);

	public abstract boolean isDisplayActionRendered(String entityName, int action, SessionContext context);

	public abstract boolean isNoDefaultActionRendered(String entityName, int action, SessionContext context);
	
	public abstract boolean isOptionMenuRendered(String optMenuName, SessionContext ctx);
	
	public abstract void initializeAccessRights(SessionContext context);
}
