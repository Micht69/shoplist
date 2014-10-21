package fr.logica.jsf.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import fr.logica.jsf.utils.FacesMessagesUtils;
import fr.logica.jsf.utils.JSFBeanUtils;
import fr.logica.jsf.webflow.View;
import fr.logica.security.AbstractSecurityManager;
import fr.logica.security.SecurityUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
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
			return "/index/login.jsf?faces-redirect=true";
		}

		RequestContext requestContext = new RequestContext(context);
		try {
			User user = sm.getUser(login, password, requestContext);

			if (user != null) {
				context.setUser(user);
				sm.initializeAccessRights(requestContext);
				if (context.getAttributes().get(Constants.PERMALINK_LOGIN_KEY) != null) {
					Map<String, String> parameters = (Map<String, String>) context.getAttributes().get(Constants.PERMALINK_LOGIN_KEY);
					context.getAttributes().remove(Constants.PERMALINK_LOGIN_KEY);
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
				// Display user messages
				FacesMessagesUtils.displayMessages(ctx, requestContext);
				// Add login error message
				ctx.addMessage(null, new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR,
						"Utilisateur / Mot de passe incorrect",
						null));
			}
		} catch (FunctionalException fe) {
			// Display user messages
			FacesMessagesUtils.displayMessages(ctx, requestContext);
			FacesMessagesUtils.displayMessages(fe);
		} finally {
			requestContext.close();
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
	 * Get the link to help page.<br/>
	 * We can add #something with current page to go direct to a specific anchor.
	 * 
	 * @return link to help page
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

	public List<Map.Entry<String, String>> getLanguages() {
		loadLanguages();
		return new ArrayList(availableLanguages.entrySet());
	}

	public String getLanguage() {
		return context.getLocale().getLanguage();
	}

	public void setLanguage(String language) {
		if (language != null) {
			context.setLocale(new Locale(language));
			loadLanguages();
		}
	}

	public String selectLanguage(String language) {
		setLanguage(language);
		return redirectFromAccueil();
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
		// Initialize locale. We request HTTP Request Locale if available
		ApplicationUtils.getApplicationLogic().setDefaultLocale(context,
				FacesContext.getCurrentInstance().getExternalContext().getRequestLocale());
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
