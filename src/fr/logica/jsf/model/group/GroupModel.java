package fr.logica.jsf.model.group;

import java.io.Serializable;
import java.util.Map;

import fr.logica.business.Entity;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;

public class GroupModel extends DataModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -5192489614208601436L;

	protected boolean visible;

	public GroupModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String panelName) {
		super(viewCtrl);
		loadVisibility(store, entity, entityName, panelName);
	}

	protected void loadVisibility(Map<String, String> store, Entity entity, String entityName, String panelName) {
		this.visible = new BusinessController().isGroupVisible(entity, panelName, this.viewCtrl.getCurrentView().getAction(),
				this.viewCtrl.getContext());
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
