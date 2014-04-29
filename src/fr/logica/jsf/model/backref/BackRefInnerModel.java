package fr.logica.jsf.model.backref;

import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Entity;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;

public class BackRefInnerModel extends DataModel {

	/** serialUID */
	private static final long serialVersionUID = 1269965693581913166L;

	private boolean loaded;

	private Entity entity;
	private String entityName;
	private String backRefName;
	private Action action;

	public BackRefInnerModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String backRefName) {
		super(viewCtrl);
		this.entity = entity;
		this.entityName = entityName;
		this.backRefName = backRefName;
		this.action = viewCtrl.getCurrentView().getAction();
		loadData(viewCtrl.getContext());
	}

	@Override
	public void loadData(RequestContext context) {
		// Load inner entity
		Entity linkedEntity = new BusinessController().getUniqueBackRefInnerEntity(entity, entityName, backRefName, action, context);
		entity.getBackRef(backRefName).setEntity(linkedEntity);
		linkedEntity.getLink(backRefName).setEntity(entity);

		loaded = true;
		if (viewCtrl.getCurrentView().getAction().getUi() == UserInterface.READONLY) {
			readonly = true;
		}
		entity.getBackRef(backRefName).setApplyActionOnLink(!readonly);
	}

	public boolean isLoaded() {
		return loaded;
	}

}
