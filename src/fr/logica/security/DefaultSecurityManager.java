package fr.logica.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import fr.logica.application.ApplicationUtils;
import fr.logica.application.logic.User;
import fr.logica.business.Constants;
import fr.logica.business.MessageUtils;
import fr.logica.business.context.RequestContext;
import fr.logica.business.context.SessionContext;

public class DefaultSecurityManager extends AbstractSecurityManager {

	/** serialUID */
	private static final long serialVersionUID = 4883968451152865426L;

	@Override
	public User getUser(String login, String password, RequestContext ctx) {
		User user = null;

		String hash = SecurityUtils.hash(password);
		String adminHash = SecurityUtils.hash("admin");
		if (adminHash.equals(hash)) {
			user = new User(MessageUtils.getInstance(ctx).getLabel("login.root", new Object[0]));
		}
		return user;
	}

	@Override
	public List<SecurityFunction> getSecurity(User user, RequestContext ctx) {
		List<SecurityFunction> fonctions = new ArrayList<SecurityFunction>();
		return fonctions;
	}

	@Override
	public boolean disableSecurity() {
		return false;
	}

	/**
	 * This method is called to load everything needed in user session. For instance application access to lists and actions.
	 */
	@Override
	public void initializeAccessRights(RequestContext context) {
		User user = context.getSessionContext().getUser();

		// No user => No access
		if (user == null) {
			return;
		}

		List<SecurityFunction> fonctions = getSecurity(user, context);

		for (SecurityFunction f : fonctions) {
			if (f.getAction() != null) {
				HashSet<Integer> act = (HashSet<Integer>) user.getActions().get(f.getEntite());
				if (act == null) {
					act = new HashSet<Integer>();
				}
				act.add(f.getAction());
				user.getActions().put(f.getEntite(), act);
			} else if (f.getQuery() != null) {
				user.getLists().add(f.getQuery());
			} else if (f.getMenu() != null) {
				user.getMenus().add(f.getMenu());
			} else if (f.getMenuOption() != null) {
				user.getMenusOptions().add(f.getMenuOption());
			}
		}
	}

	/**
	 * Current user has rights to see the list queryName ?
	 * 
	 * @param queryName
	 *            Query to display
	 * @return booléen
	 */
	@Override
	public boolean isListRendered(String queryName, SessionContext ctx) {
		User user = ctx.getUser();
		if (user.getLists() != null && user.getLists().contains(queryName)) {
			return true;
		}
		return disableSecurity();
	}

	/**
	 * Access right on an action
	 * 
	 * @param entityName
	 *            Action entity
	 * @param action
	 *            Action code
	 * @return booléen true if current user has access and that it's not inside a display action
	 */
	@Override
	public boolean isActionRendered(String entityName, int action, SessionContext context) {
		User user = context.getUser();
		if (action == Constants.SELECT || action == Constants.DETACH
				|| action == Constants.SELECT_BR || action == Constants.DETACH_BR) {
			// If user is allowed to modify the target entity, he is able to modify its links.
			if (isActionRendered(entityName, Constants.MODIFY, context)) {
				return true;
			}
		}
		if (user.getActions() != null && user.getActions().get(entityName) != null && user.getActions().get(entityName).contains(action)) {
			return true;
		}
		return disableSecurity();
	}

	@Override
	public boolean isDisplayActionRendered(String entityName, int action, SessionContext context) {
		if (!isActionRendered(entityName, action, context) && isActionRendered(entityName, Constants.DISPLAY, context)) {
			// L'action par défaut est désactivée. On a le droit d'afficher l'action de consultation. On remplace.
			return true;
		}
		return false;
	}

	@Override
	public boolean isNoDefaultActionRendered(String entityName, int action, SessionContext context) {
		if (!isActionRendered(entityName, action, context)
				&& !isActionRendered(entityName, Constants.DISPLAY, context)) {
			// L'action par défaut est désactivée et on a pas le droit d'afficher l'action de consultation.
			// On affiche pas de lien d'action dans la liste.
			return true;
		}
		return false;
	}

	@Override
	public boolean isOptionMenuRendered(String optMenuName, SessionContext ctx) {
		User user = ctx.getUser();
		if (user.getMenusOptions() != null && user.getMenusOptions().contains(optMenuName)) {
			return true;
		}
		return disableSecurity();
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
		if (query != null)
			sf.setQuery(query);
		if (action != null)
			sf.setAction(action);
		return sf;
	}

	/**
	 * Instantiates a stub context that can be used for authentication purpose. Do not forget to CLOSE IT !
	 * 
	 * @return A request context based on a fake sessionContext.
	 */
	protected RequestContext getAuthContext() {
		SessionContext sessionContext = new SessionContext(ApplicationUtils.getApplicationContext());
		RequestContext requestContext = new RequestContext(sessionContext);
		return requestContext;
	}
}
