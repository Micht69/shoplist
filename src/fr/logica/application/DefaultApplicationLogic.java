package fr.logica.application;

import java.util.Locale; 


import javax.faces.context.FacesContext;

import fr.logica.business.Constants;
import fr.logica.jsf.controller.SessionController;
import fr.logica.jsf.utils.JSFBeanUtils;
import fr.logica.security.ApplicationUser;

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
	public String getTimeFormat() {
		return Constants.FORMAT_TIME;
	}

	@Override
	public String getTimestampFormat() {
		return Constants.FORMAT_TIMESTAMP;
	}

	@Override
	public String getPageTitle(String currentTitle) {
		return currentTitle;
	}

	/**
	 * @return {@code "/index/defaultPage"} if the {@code user} is not {@code null}, {@code "/index/login"} otherwise.
	 */
	@Override
	public String getDefaultPage(ApplicationUser user) {

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
	public OpenCriteriaBehavior getOpenCriteriaBehavior() {
		return OpenCriteriaBehavior.DEFAULT;
	}

}
