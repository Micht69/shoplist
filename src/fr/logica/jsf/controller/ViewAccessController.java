package fr.logica.jsf.controller;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import fr.logica.application.ApplicationUtils;
import fr.logica.application.logic.ApplicationLogic;
import fr.logica.business.Constants;
import fr.logica.business.FunctionalException;
import fr.logica.business.controller.Request;
import fr.logica.jsf.utils.FacesMessagesUtils;

/**
 * JSF Controller to allow permalink to work
 * 
 * @author bellangerf
 * 
 */
public class ViewAccessController {

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(ViewAccessController.class);

	/**
	 * Injects View controller to reuse navigation methods
	 */
	private ViewController jsfCtrl;

	/** Internal Request built from user parameters */
	private Request<?> request = null;

	/** Parses GET parameters and prepares a view */
	public void initializeViewAccess() {
		FacesContext fc = FacesContext.getCurrentInstance();
		// Check permalink feature activation
		if (!ApplicationUtils.getApplicationLogic().enablePermalink()) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Permalink feature is not enabled on this application", null));
			return;
		}

		// Check user authentication
		if (jsfCtrl.getContext().getSessionContext().getUser() == null) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You need to log in before reaching a view", null));
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.putAll(fc.getExternalContext().getRequestParameterMap());
			jsfCtrl.getContext().getSessionContext().getAttributes().put(Constants.PERMALINK_LOGIN_KEY, parameters);
			fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "/index/login");
			return;
		}

		ApplicationLogic appLogic = new ApplicationLogic();
		try {
			request = appLogic.getPermalinkRequest(fc.getExternalContext().getRequestParameterMap(), jsfCtrl.getContext());
		} catch (FunctionalException fe) {
            LOGGER.debug(fe.getMessage(), fe);
            request = null;
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, fe.getMessage(), null));
        } catch (Exception e) {
			LOGGER.error("Error handling directViewAccess: " + e.getMessage(), e);
			request = null;
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, FacesMessagesUtils.getTechnicalMessage(e), null));
		}
	}

	/**
	 * @return the request
	 */
	public Request<?> getRequest() {
		return request;
	}

	/**
	 * Resets conversation and access the prepared view
	 * 
	 * @return View URL or null (reloads current view) if view is not prepared
	 */
	public String viewAccess() {
		if (request == null) {
			// Request is not loaded / we'll display the page with potential error messages.
			return null;
		}
		jsfCtrl.reset();
		return jsfCtrl.prepareView(request);
	}

	/**
	 * @return the jsfCtrl
	 */
	public ViewController getJsfCtrl() {
		return jsfCtrl;
	}

	/**
	 * @param jsfCtrl the jsfCtrl to set
	 */
	public void setJsfCtrl(ViewController jsfCtrl) {
		this.jsfCtrl = jsfCtrl;
	}
}
