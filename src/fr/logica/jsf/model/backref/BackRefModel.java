package fr.logica.jsf.model.backref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import fr.logica.business.Action;
import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.data.BackRefData;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;
import fr.logica.security.SecurityUtils;

public class BackRefModel extends DataModel {

	/** serialUID */
	private static final long serialVersionUID = 4505648376421998467L;

	protected Entity targetEntity;
	protected String entityName;
	protected String filterName;
	protected String linkName;
	protected BackRefData data;

	public BackRefModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String linkName, String filterName) {
		super(viewCtrl);
		this.targetEntity = entity;
		this.entityName = entityName;
		this.filterName = filterName;
		this.linkName = linkName;
		loadData(viewCtrl.getContext());
	}

	@Override
	public List<Key> getSelected() {
		if (data.getSourceEntity() != null) {
			List<Key> keys = new ArrayList<Key>();
			keys.add(data.getSourceEntity().getPrimaryKey());
			return keys;
		}
		return null;
	}

	@Override
	public void loadData(RequestContext context) {
		BusinessController bc = new BusinessController();
		Action action = viewCtrl.getCurrentView().getAction();
		data = bc.getUniqueBackRefData(targetEntity, entityName, linkName, action, context);
		if (action.getUi() == UserInterface.READONLY
				|| action.getPersistence() == Persistence.INSERT) {
			readonly = true;
		}
	}

	public String prepareAction(Action action) {
		return viewCtrl.prepareView(entityName, action, getSelected(), action.getInput() == Input.QUERY ? filterName : null, linkName,
				targetEntity, true);
	}

	public String prepareActionCode(Integer actionCode) {
		return prepareAction(EntityManager.getEntityModel(entityName).getAction(actionCode));
	}

	private Integer getDefaultAction(String actionsString, boolean readonlyAction) {
		if (actionsString == null || actionsString.trim().length() == 0) {
			return null;
		}
		String[] actions = actionsString.split(",");
		for (String actionCodeString : actions) {
			Integer actionCode = null;
			try {
				actionCode = Integer.parseInt(actionCodeString);
			} catch (NumberFormatException ex) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid default action : " + actionCodeString, null));
				return null;
			}
			EntityModel model = EntityManager.getEntityModel(entityName);
			Action action = model.getAction(actionCode);
			if (action.getInput() != Input.ONE) {
				// Input MUST be the current target entity, there can be only one
				continue;
			}
			if (!SecurityUtils.getSecurityManager().isActionRendered(model.name(), action.getCode(),
					viewCtrl.getSessionCtrl().getContext())) {
				continue;
			}
			if (!readonlyAction || (action.getUi() == UserInterface.READONLY && action.getPersistence() == Persistence.NONE)) {
				return action.getCode();
			}
		}
		return null;
	}

	public boolean hasDefaultDisplayAction(String actionsString) {
		return (getDefaultAction(actionsString, true) != null);
	}

	public boolean hasDefaultAction(String actionsString) {
		return (getDefaultAction(actionsString, readonly) != null);
	}

	public String prepareDefaultDisplayAction(String actionsString) {
		return prepareActionCode(getDefaultAction(actionsString, true));
	}

	public String prepareDefaultAction(String actionsString) {
		return prepareActionCode(getDefaultAction(actionsString, readonly));
	}

	/**
	 * Shortcut method to detach current element. No persistence.
	 * 
	 * @return Page to display for detach action.
	 */
	public String detach() {
		return prepareAction(Action.getDetachLinkAction());
	}

	/**
	 * Shortcut method to attach a new element. No persistence.
	 * 
	 * @return Page to display for attach action.
	 */
	public String attach() {
		return prepareAction(Action.getAttachLinkAction(filterName));
	}

	public Entity getSourceEntity() {
		return data.getSourceEntity();
	}

	public String getDescription() {
		return data.getDescription();
	}

}
