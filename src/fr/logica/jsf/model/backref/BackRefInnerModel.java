package fr.logica.jsf.model.backref;

import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Entity;
import fr.logica.business.Link;
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
		Link backRef = entity.getBackRef(backRefName);
		if (backRef.getEntity() != null) {
			// Entity is already loaded - Check if foreign key from source entity is different. It it is, we need to reload source entity
			if (!entity.getPrimaryKey().hasSameValues(backRef.getEntity().getForeignKey(backRefName))) {
				// Loaded entity doesn't match current primary key, we need to reload the entity
				backRef.setEntity(null);
			}
		}
		if (backRef.getEntity() == null) {
			// Load inner entity
			Entity linkedEntity = new BusinessController().getUniqueBackRefInnerEntity(entity, entityName, backRefName, action, context);
			backRef.setEntity(linkedEntity);
			linkedEntity.getLink(backRefName).setEntity(entity);
		}
		if (backRef.getEntity() != null) {
			loaded = true;
			if (viewCtrl.getCurrentView().getAction().getUi() == UserInterface.READONLY) {
				readonly = true;
			}
			backRef.setApplyActionOnLink(!readonly);
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

}
