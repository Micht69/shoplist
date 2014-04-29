package fr.logica.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import javax.faces.context.FacesContext;

import fr.logica.application.logic.User;
import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.context.ApplicationContext;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.db.DB;
import fr.logica.jsf.controller.SessionController;
import fr.logica.jsf.utils.JSFBeanUtils;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;

/**
 * Default implementation for the application logic methods.
 */
public class DefaultApplicationLogic extends AbstractApplicationLogic {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Override
	public String getDateFormat() {
		return Constants.FORMAT_DATE;
	}

	@Override
	public String getDatetimeFormat() {
		return Constants.FORMAT_DATE + " " + Constants.FORMAT_HOUR;
	}

	@Override
	public String getTimeFormat() {
		return Constants.FORMAT_TIME;
	}

	@Override
	public String getTimestampFormat() {
		return Constants.FORMAT_TIMESTAMP;
	}

	@Override
	public String getPageTitle(String currentTitle) {
		if (currentTitle == null) {
			return "Application title";
		}
		return currentTitle;
	}

	/**
	 * @return {@code "/index/defaultPage"} if the {@code user} is not {@code null}, {@code "/index/login"} otherwise.
	 */
	@Override
	public String getDefaultPage(User user) {

		if (user != null) {
			return "/index/defaultPage";
		}
		return "/index/login";
	}
	
	@Override
	public Locale getCurrentUserLocale() {
		
		//FIXME bellangerf renouxg Reprendre pour ne pas appeler JSF 2, cette méthode sert dans les WS par exemple.
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx != null) {
			SessionController sessionCtrl = (SessionController) JSFBeanUtils.getManagedBean(FacesContext.getCurrentInstance(), "sessionCtrl");
			if (sessionCtrl != null) {
				return sessionCtrl.getCurrentUserLocale();
			}
		}
		return Locale.getDefault();
	}

	@Override
	public boolean enableSocialFeatures() {
		return false;
	}

	@Override
	public boolean enableComments() {
		return false;
	}

	@Override
	public boolean enableSelectActions() {
		return true;
	}

	@Override
	public boolean enableXlsExport() {
		return true;
	}
	
	@Override
	public boolean enablePermalink() {
		return false;
	}
	
	@Override
	public OpenCriteriaBehavior getOpenCriteriaBehavior() {
		return OpenCriteriaBehavior.DEFAULT;
	}

	@Override
	public void initializeApplication(ApplicationContext context) {

	}

	@Override
	public void finalizeApplication(ApplicationContext context) {

	}

		@Override
	public Request<?> getPermalinkRequest(Map<String, String> parameters, RequestContext context) {

		// Get user parameters
		String actionCode = parameters.get("actionCode");
		String entityName = parameters.get("entityName");
		String queryName = parameters.get("queryName");

		// Optional page name for special lists
		String pageName = parameters.get("pageName");

		// Optional input key list
		String encodedKeyList = parameters.get("encodedKeyList");

		Request<?> request = new Request();
		if (entityName == null) {
			throw new FunctionalException(new Message("entityName is not defined", Severity.ERROR));
		}
		EntityModel eModel = EntityManager.getEntityModel(entityName);
		if (eModel == null) {
			throw new FunctionalException(new Message("entityName " + entityName + " is invalid", Severity.ERROR));
		}
		request.setEntityName(entityName);

		if (actionCode == null && queryName == null) {
			throw new FunctionalException(new Message("actionCode and queryName are undefined", Severity.ERROR));
		}

		if (actionCode != null) {
			Action action = null;
			try {
				action = eModel.getAction(Integer.parseInt(actionCode));
			} catch (NumberFormatException e) {
				throw new FunctionalException(new Message("actionCode " + actionCode + " is invalid - must be a valid positive integer",
						Severity.ERROR));
			}
			if (action == null) {
				throw new FunctionalException(new Message("actionCode " + actionCode + " is invalid - must be a valid action for entity "
						+ entityName, Severity.ERROR));
			}
			request.setAction(action);
		} else {
			if (DB.getQuery(null, entityName, queryName) == null) {
				throw new FunctionalException(new Message("query " + queryName + " is invalid for entity " + entityName, Severity.ERROR));
			}
			request.setAction(Action.getListAction(queryName, pageName));
			request.setQueryName(queryName);
		}

		if (encodedKeyList != null) {
			List<Key> keyList = new ArrayList<Key>();
			String[] encodedKeys = encodedKeyList.split("\\|\\|\\|");
			for (String encodedKey : encodedKeys) {
				keyList.add(new Key(entityName, encodedKey));
			}
			request.setKeys(keyList);
		}
		return request;
	}

}
