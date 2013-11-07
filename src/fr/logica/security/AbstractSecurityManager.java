package fr.logica.security;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractSecurityManager implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -5307524579033289661L;

	public abstract ApplicationUser getUser(String login, String password);

	public abstract List<SecurityFunction> getSecurity(ApplicationUser user);

	public abstract boolean disableSecurity();
}
