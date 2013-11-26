package fr.logica.application;

import javax.faces.context.FacesContext;

import fr.logica.domain.models.ShopListModel;
import fr.logica.jsf.controller.JSFController;
import fr.logica.jsf.utils.JSFBeanUtils;
import fr.logica.queries.ShopListQuery;
import fr.logica.security.ApplicationUser;

/**
 * Class used to store application logic methods.
 */
public class ApplicationLogic extends DefaultApplicationLogic {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Override
	public String getDefaultPage(ApplicationUser user) {
		if (user != null) {
			JSFController jsfCtrl = ((JSFController) JSFBeanUtils.getManagedBean(FacesContext.getCurrentInstance(), "jsfCtrl"));

			return jsfCtrl.prepareList(ShopListModel.ENTITY_NAME, ShopListQuery.Query.QUERY_SHOP_LIST, "SHOP_LIST_LIST");
		}
		return "/index/login";
	}
}
