package fr.logica.security;

import fr.logica.business.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultSecurityManager extends AbstractSecurityManager {

	/** serialUID */
	private static final long serialVersionUID = 1785320680227766863L;

	@Override
	public ApplicationUser getUser(String login, String password) {
		ApplicationUser user = null;

		String hash = SecurityUtils.hash(password);
		String adminHash = SecurityUtils.hash("admin");
		if (adminHash.equals(hash)) {
			user = new ApplicationUser(MessageUtils.getInstance().getLabel("login.root", new Object[0]));
		}
		return user;
	}

	@Override
	public List<SecurityFunction> getSecurity(ApplicationUser user) {
		List<SecurityFunction> fonctions = new ArrayList<SecurityFunction>();

		return fonctions;
	}

	@Override
	public boolean disableSecurity() {
		return false;
	}
	
	protected SecurityFunction getQuerySecurityFunction(String entite, String query) {
		return getSecurityFunction(entite, null, query);
	}

	protected SecurityFunction getActionSecurityFunction(String entite, Integer action) {
		return getSecurityFunction(entite, action, null);
	}
	
	protected SecurityFunction getSecurityFunction(String entite, Integer action, String query) {
		SecurityFunction sf = new SecurityFunction();
		sf.setEntite(entite);
		if (query != null) sf.setQuery(query);
		if (action != null) sf.setAction(action);
		return sf;
	}
}
