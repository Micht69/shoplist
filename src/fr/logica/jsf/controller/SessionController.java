package fr.logica.jsf.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import fr.logica.application.ApplicationUtils;
import fr.logica.business.Constants;
import fr.logica.business.EntityManager;
import fr.logica.business.FunctionalException;
import fr.logica.business.MessageUtils;
import fr.logica.business.TechnicalException;
import fr.logica.reflect.DomainUtils;
import fr.logica.security.AbstractSecurityManager;
import fr.logica.security.ApplicationUser;
import fr.logica.security.SecurityFunction;
import fr.logica.security.SecurityUtils;
import fr.logica.ui.ActionPage;
import fr.logica.ui.ListPage;
import fr.logica.ui.Page;

public class SessionController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -7648293653392389886L;

	private ApplicationUser user = new ApplicationUser("admin");

	private LoginForm form;

	private static AbstractSecurityManager sm = SecurityUtils.getSecurityManager();

	private Page<?> currentPage;

	/** Access rights on lists **/
	private Set<String> lists;

	/** Access rights on Actions **/
	private Map<String, Set<Integer>> actions;

	/**
	 * Gets application version number.
	 * 
	 * @return Version number located in server.properties
	 */
	public String getVersion() {
		return MessageUtils.getServerProperty("version");
	}

	public Page<?> getPage() {
		return currentPage;
	}

	public void setPage(Page<?> page) {
		currentPage = page;
	}

	public String redirectFromAccueil() {
		reset();
		return getDefaultPage();
	}

	public String getDefaultPage() {
		return ApplicationUtils.getApplicationLogic().getDefaultPage(user);
	}

	public String login() throws FunctionalException {
		FacesContext ctx = FacesContext.getCurrentInstance();

		// Nettoyage de l'historique
		reset();

		String login = form.getLogin();
		String password = form.getPassword();

		if (password == null || "".equals(password)) {
			ctx.addMessage(null, new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, "Veuillez renseigner le mot de passe",
					null));
			return null;
		}

		ApplicationUser user;
		user = sm.getUser(login, password);

		if (user != null) {
			this.user = user;
			initializeAccessRights();
			return getDefaultPage();
		} else {
			ctx.addMessage(null, new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, "Utilisateur / Mot de passe incorrect",
					null));
		}
		// On reste sur la page de login.
		return null;
	}

	/**
	 * This method is called to load everything needed in user session. For instance application access to lists and actions.
	 */
	public void initializeAccessRights() {
		lists = new HashSet<String>();
		actions = new HashMap<String, Set<Integer>>();

		// No user => No access
		if (user == null) {
			return;
		}

		List<SecurityFunction> fonctions = sm.getSecurity(user);

		for (SecurityFunction f : fonctions) {
			if (f.getAction() != null) {
				Set<Integer> act = actions.get(f.getEntite());
				if (act == null) {
					act = new HashSet<Integer>();
				}
				act.add(f.getAction());
				actions.put(f.getEntite(), act);
			} else {
				lists.add(f.getQuery());
			}
		}
	}

	public String logout() throws FunctionalException {
		reset();
		lists = new HashSet<String>();
		actions = new HashMap<String, Set<Integer>>();
		form = new LoginForm();
		// user = null;
		return getDefaultPage();
	}

	/**
	 * Retourne le lien vers la page d'aide. On peut ajouter #truc en fonction de la page courante pour aller à une ancre directement.
	 * 
	 * @return
	 */
	public String getHelp() {
		String url = "static/aide.html";
		if (currentPage instanceof ListPage) {
			url += "#" + ((ListPage<?>) currentPage).getQueryName();
		}
		if (currentPage instanceof ActionPage) {
			url += "#" + currentPage.getDomainName() + "_" + ((ActionPage<?>) currentPage).getAction().code;
		}
		return url.toString();
	}

	public void reset() {
		currentPage = null;
	}

	/**
	 * Current user has rights to see the list queryName ?
	 * 
	 * @param queryName
	 *            Query to display
	 * @return booléen
	 */
	public boolean isListRendered(String queryName) {
		if (lists != null && lists.contains(queryName)) {
			return true;
		}
		return sm.disableSecurity();
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
	public boolean isActionRendered(String entityName, int action) {
		if (currentPage instanceof ActionPage) {
			if (((ActionPage<?>) currentPage).getAction().type == Constants.DISPLAY) {
				try {
					if (action == Constants.SELECT
							|| action == Constants.DETACH
							|| EntityManager.getEntityModel(DomainUtils.createJavaName(entityName, false)).getAction(action).type != Constants.DISPLAY) {
						return false;
					}
				} catch (TechnicalException ex) {
					if (EntityManager.getEntityModel(entityName).getAction(action).type != Constants.DISPLAY) {
						return false;
					}
				}
			}
		}
		if (action == Constants.SELECT || action == Constants.DETACH) {
			// If user is allowed to modify the target entity, he is able to modify its links.
			if (isActionRendered(entityName, Constants.MODIFY)) {
				return true;
			}
		}
		if (actions != null && actions.get(entityName) != null && actions.get(entityName).contains(action)) {
			return true;
		}
		return sm.disableSecurity();
	}

	public boolean isDisplayActionRendered(String entityName, int action) {
		if (!isActionRendered(entityName, action) && isActionRendered(entityName, Constants.DISPLAY)) {
			// L'action par défaut est désactivée. On a le droit d'afficher l'action de consultation. On remplace.
			return true;
		}
		return false;
	}

	public boolean isNoDefaultActionRendered(String entityName, int action) {
		if (!isActionRendered(entityName, action) && !isActionRendered(entityName, Constants.DISPLAY)) {
			// L'action par défaut est désactivée et on a pas le droit d'afficher l'action de consultation.
			// On affiche pas de lien d'action dans la liste.
			return true;
		}
		return false;
	}

	public ApplicationUser getUser() {
		return user;
	}

	public void setUser(ApplicationUser user) {
		this.user = user;
	}

	public LoginForm getForm() {
		if (form == null) {
			form = new LoginForm();
		}
		return form;
	}

	public void setForm(LoginForm form) {
		this.form = form;
	}

	public class LoginForm implements Serializable {
		/** serialUID */
		private static final long serialVersionUID = 161326522864293816L;

		private String login;
		private String password;

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public Map<Object, Object> getAdminData() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return ctx.getAttributes();
	}	

		private Locale currentUserLocale;

	public Locale getCurrentUserLocale() {
		if (currentUserLocale == null) {
			// Initialization
			currentUserLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
			FacesContext.getCurrentInstance().getViewRoot().setLocale(currentUserLocale);
		}
		return currentUserLocale;
	}

	private Map<String, String> availableLanguages;

	public void loadLanguages() {
		Set<Locale> languages = MessageUtils.getAvailableLanguages();

		availableLanguages = new HashMap<String, String>();
		for (Locale l : languages) {
			String displayName = l.getDisplayLanguage(l);
			if (displayName.length() > 0) {
				availableLanguages.put(displayName.substring(0, 1).toUpperCase() + displayName.substring(1), l.getLanguage());
			}
		}
	}

	public Map<String, String> getLanguages() {
		loadLanguages();
		return availableLanguages;
	}

	public String getLanguage() {
		return getCurrentUserLocale().getLanguage();
	}

	public void setLanguage(String language) {
		if (language != null) {
			currentUserLocale = new Locale(language);
			FacesContext.getCurrentInstance().getViewRoot().setLocale(currentUserLocale);
			loadLanguages();
		}
	}

	public boolean isLanguageSelector() {
		return (getLanguages().size() > 1);
	}
}


