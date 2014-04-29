package fr.logica.application.logic;

import javax.faces.context.FacesContext;

import fr.logica.application.DefaultApplicationLogic;
import fr.logica.domain.models.ShopListModel;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.utils.JSFBeanUtils;
import fr.logica.queries.ShopListQuery;

/**
 * Class used to store application logic methods.
 */
public class ApplicationLogic extends DefaultApplicationLogic {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Override
	public String getDefaultPage(User user) {
		if (user != null) {
			ViewController viewCtrl = ((ViewController) JSFBeanUtils.getManagedBean(FacesContext.getCurrentInstance(), "jsfCtrl"));
			viewCtrl.reset();

			return viewCtrl.prepareView(ShopListModel.ENTITY_NAME, ShopListQuery.Query.QUERY_SHOP_LIST, "SHOP_LIST_LIST");
		}
		return "/index/login";
	}

	@Override
	public boolean enableXlsExport() {
		return false;
	}
}
