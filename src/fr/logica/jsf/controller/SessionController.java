package fr.logica.jsf.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import fr.logica.application.ApplicationUtils;
import fr.logica.application.logic.ApplicationLogic;
import fr.logica.application.logic.User;
import fr.logica.business.Constants;
import fr.logica.business.FunctionalException;
import fr.logica.business.MessageUtils;
import fr.logica.business.context.RequestContext;
import fr.logica.business.context.SessionContext;
import fr.logica.business.controller.Request;
import fr.logica.jsf.utils.JSFBeanUtils;
import fr.logica.jsf.webflow.View;
import fr.logica.security.AbstractSecurityManager;
import fr.logica.security.SecurityUtils;

public class SessionController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -7648293653392389886L;

	private LoginForm form;

	private static AbstractSecurityManager sm = SecurityUtils.getSecurityManager();

	/** Admin controls **/
	private Boolean disableJS = Boolean.FALSE;
	private Boolean disableCustom = Boolean.FALSE;
	private Boolean disableCSS = Boolean.FALSE;

	/** Session context */
	private SessionContext context;

	/** Application Controller */ 
	private ApplicationController applicationCtrl;

	/**
	 * Gets application version number.
	 * 
	 * @return Version number located in server.properties
	 */
	public String getVersion() {
		return MessageUtils.getServerProperty("version");
	}

	public String redirectFromAccueil() {
		reset();
		return getDefaultPage();
	}

	public String getDefaultPage() {
		return ApplicationUtils.getApplicationLogic().getDefaultPage(context.getUser());
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

		User user;
		user = sm.getUser(login, password);

		if (user != null) {
			context.setUser(user);
			sm.initializeAccessRights(context);
			if (context.getAttributes().get(Constants.PERMALINK_LOGIN_KEY) != null) {
				Map<String, String> parameters = (Map<String, String>) context.getAttributes().get(Constants.PERMALINK_LOGIN_KEY);
				context.getAttributes().remove(Constants.PERMALINK_LOGIN_KEY);
				RequestContext requestContext = new RequestContext(context);
				ViewController viewController = (ViewController) JSFBeanUtils.getManagedBean(ctx, "jsfCtrl");
				viewController.setContext(requestContext);
				try {
					Request<?> request = new ApplicationLogic().getPermalinkRequest(parameters, requestContext);
					return viewController.prepareView(request);
				} catch (FunctionalException ex) {
					ctx.addMessage(null, new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, ex.getMessage(),
							null));
					return getDefaultPage();
				}
			}
			return getDefaultPage();
		} else {
			ctx.addMessage(null, new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, "Utilisateur / Mot de passe incorrect",
					null));
		}
		// On reste sur la page de login.
		return "/index/login.jsf?faces-redirect=true";
	}

	

	public String logout() throws FunctionalException {
		reset();
		context.setUser(null); 
		return getDefaultPage();
	}

	/**
	 * Retourne le lien vers la page d'aide. On peut ajouter #truc en fonction de la page courante pour aller à une ancre directement.
	 * 
	 * @return
	 */
	public String getHelp(String conversationId) {
		String url = "static/aide.html";
		View currentView = getCurrentView(conversationId);
		if (currentView != null) {
			return url + currentView.getURLNoParam();
		}
		return url;
	}

	public void reset() {
	
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
	
	public Boolean getDisableJS() {
		return disableJS;
	}

	public void setDisableJS(Boolean disableJS) {
		this.disableJS = disableJS;
	}

	public Boolean getDisableCustom() {
		return disableCustom;
	}

	public void setDisableCustom(Boolean disableCustom) {
		this.disableCustom = disableCustom;
	}

	public Boolean getDisableCSS() {
		return disableCSS;
	}

	public void setDisableCSS(Boolean disableCSS) {
		this.disableCSS = disableCSS;
	}
	
	
	public SessionContext getContext() {
		return context;
	}

	public void setContext(SessionContext context) {
		this.context = context;
	}
	
	
	public ApplicationController getApplicationCtrl() {
		return applicationCtrl;
	}

	public void setApplicationCtrl(ApplicationController applicationCtrl) {
		this.applicationCtrl = applicationCtrl;
	}

	@PostConstruct
	public void initializeContext() {
		context = new SessionContext(applicationCtrl.getContext());
	}

	@PreDestroy
	public void closeContext() {
		if (context != null) {
			context.close();
		}
	}
	

	private Map<String, View> conversations;
	private Map<String, String> viewConversations;


	private Map<String, View> getConversations() {
		if (conversations == null) {
			conversations = new HashMap<String, View>();
		}
		return conversations;
	}

	public String getNewConversationId() {
		return String.valueOf(getConversations().size());
	}

	public Map<String, String> getViewConversations() {
		if (viewConversations == null) {
			viewConversations = new HashMap<String, String>();
		}
		return viewConversations;
	}

	public View getCurrentView(String cID) {
		return getConversations().get(cID);
	}

	public void setCurrentView(String cID, View v) {
		getConversations().put(cID, v);
	}
	
	public View getView(String vID) {
		return conversations.get(viewConversations.get(vID));
	}

	/** @deprecated use {@link AbstractSecurityManager#isActionRendered(String, int, SessionContext)} */
	@Deprecated
	public boolean isActionRendered(String entityName, Integer code) {
		return sm.isActionRendered(entityName, code, context);
	}

	/** @deprecated use {@link AbstractSecurityManager#isListRendered(String, SessionContext)} */
	@Deprecated
	public boolean isListRendered(String queryName) {
		return sm.isListRendered(queryName, context);
	}

	/** @deprecated use {@link AbstractSecurityManager#isDisplayActionRendered(String, int, SessionContext)} */
	@Deprecated
	public boolean isDisplayActionRendered(String entityName, int action) {
		return sm.isDisplayActionRendered(entityName, action, context);
	}

	/** @deprecated use {@link AbstractSecurityManager#isNoDefaultActionRendered(String, int, SessionContext)} */
	@Deprecated
	public boolean isNoDefaultActionRendered(String entityName, int action) {
		return sm.isNoDefaultActionRendered(entityName, action, context);
	}
	
	/** @deprecated use {@link AbstractSecurityManager#isOptionMenuRendered(String, SessionContext)} */
	@Deprecated
	public boolean isOptionMenuRendered(String optMenuName) {
		return sm.isOptionMenuRendered(optMenuName, context);
	}
}
