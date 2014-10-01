package fr.logica.jsf.model.link;

import java.util.Map;
import java.util.Map.Entry;

import fr.logica.business.Action;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Entity;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;
import fr.logica.ui.UiAccess;

public class LinkInnerModel extends DataModel {

	/** serialUID */
	private static final long serialVersionUID = 3280924431563956416L;

	private boolean loaded;

	private Entity entity;
	private String entityName;
	private String linkName;
	private Action action;

	public LinkInnerModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String linkname) {
		super(viewCtrl);
		this.entity = entity;
		this.entityName = entityName;
		this.linkName = linkname;
		this.action = viewCtrl.getCurrentView().getAction();
		loadData(viewCtrl.getContext());
	}

	@Override
	public void loadData(RequestContext context) {
		if (entity.getLink(linkName).getEntity() != null) {
			// Entity is already loaded - Check if foreign key from source entity is different. It it is, we need to reload target entity
			if (!entity.getForeignKey(linkName).hasSameValues(entity.getLink(linkName).getEntity().getPrimaryKey())) {
				// Loaded entity doesn't match current foreign key, we need to reload the entity
				entity.getLink(linkName).setEntity(null);
			}
		}
		if (entity.getLink(linkName).getEntity() == null) {
			// Load target entity
			BusinessController bc = new BusinessController();
			Entity linkEntity = bc.getLinkInnerEntity(entity, entityName, linkName, action, context);
			Map<String, UiAccess> access = bc.getEntityUiAccess(linkEntity, action, context);
			for (Entry<String, UiAccess> e : access.entrySet()) {
				viewCtrl.getCurrentView().getUiAccess().put(linkName + "." + e.getKey(), e.getValue());
			}
			entity.getLink(linkName).setEntity(linkEntity);
			// Default link protection
			if (linkName.equals(viewCtrl.getCurrentView().getLinkName())
					|| viewCtrl.getCurrentView().getAction().getUi() == UserInterface.READONLY) {
				readonly = true;
			}
			if (!entity.getModel().isStrongKey(linkName)) {
				readonly = true;
			}
			entity.getLink(linkName).setApplyActionOnLink(!readonly);
		}
		if (entity.getLink(linkName).getEntity() != null) {
			Entity linkedEntity = entity.getLink(linkName).getEntity();
			linkedEntity.getBackRef(linkName).setEntity(entity);
			loaded = true;
		}
	}

	public boolean isLoaded() {
		return loaded;
	}
}
