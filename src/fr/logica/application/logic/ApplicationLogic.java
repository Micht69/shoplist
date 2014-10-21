package fr.logica.application.logic;

import java.util.Map;

import javax.faces.context.FacesContext;

import fr.logica.application.DefaultApplicationLogic;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.domain.constants.ShopListConstants;
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

			return viewCtrl.prepareView(ShopListConstants.ENTITY_NAME, ShopListQuery.Query.QUERY_SHOP_LIST, "SHOP_LIST_LIST");
		}
		return "/index/login";
	}

	@Override
	public boolean enableXlsExport() {
		return false;
	}
	
	@Override
	public boolean enablePermalink() {
		return true;
	}
	
	@Override
	public Request<?> getPermalinkRequest(Map<String, String> parameters, RequestContext context) {
		// Store custom data
		User user = context.getSessionContext().getUser();
		String code = parameters.get("code");
		if (code != null && !"".equals(code)) {
			user.eanCode = code;
		}
		String shelf = parameters.get("shelf");
		if (shelf != null && !"".equals(shelf)) {
			user.eanShelf = shelf;
		}
		
		return super.getPermalinkRequest(parameters, context);
	}
}
