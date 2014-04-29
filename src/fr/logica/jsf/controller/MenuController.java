package fr.logica.jsf.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.ui.UiManager;

public class MenuController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6417166270122663231L;

	/** business data displayed on menu entries */
	private Map<String, Integer> menuCounters = new HashMap<String, Integer>();

	/** Session controller. Holds user relative data and access rights management. */
	private SessionController sessionCtrl;

	public void executeMenuQueries() {
		Map<String, String[]> menuQueries = UiManager.getMenuQueries();
		RequestContext context = null;
		try {
			context = new RequestContext(sessionCtrl.getContext());
			menuCounters = new BusinessController().generateMenuCounters(menuQueries, context);
		} finally {
			if (context != null)
				context.close();
		}
	}

	public int getCountforMenu(String menuId) {
		Integer res = menuCounters.get(menuId);
		return res == null ? 0 : res;
	}

	public Map<String, Integer> getMenuCounters() {
		return menuCounters;
	}

	public void setMenuCounters(Map<String, Integer> menuCounters) {
		this.menuCounters = menuCounters;
	}

	public SessionController getSessionCtrl() {
		return sessionCtrl;
	}

	public void setSessionCtrl(SessionController sessionCtrl) {
		this.sessionCtrl = sessionCtrl;
	}

}
