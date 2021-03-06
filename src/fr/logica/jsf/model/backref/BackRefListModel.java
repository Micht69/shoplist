package fr.logica.jsf.model.backref;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.list.AbstractListModel;

public class BackRefListModel extends AbstractListModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -772858503436166694L;

	protected Entity entity;
	protected String queryName;
	protected String linkName;
	protected String filterName;

	public BackRefListModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String queryName,
			String linkName, String filterName) {
		super(viewCtrl, store, entityName);
		this.entity = entity;
		this.queryName = queryName;
		this.linkName = linkName;
		this.filterName = filterName;
		loadData(viewCtrl.getContext());
	}

	public String prepareAction(Action action) {
		String query;

		if (action.getQueryName() != null) {
			query = action.getQueryName();
		} else {
			query = queryName;
		}
		return viewCtrl.prepareView(entityName, action, getSelected(), query, linkName, entity, true);
	}

	public String prepareActionCode(Integer actionCode) {
		return prepareAction(EntityManager.getEntityModel(entityName).getAction(actionCode));
	}

	public String prepareDefaultAction(String actionsString) {
		Action defaultAction = getDefaultAction(actionsString);
		if (defaultAction != null) {
			return viewCtrl.prepareView(entityName, defaultAction, getSelected(), null,
					linkName, entity, true);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.logica.jsf.model.DataModel#loadData()
	 */
	@Override
	public void loadData(RequestContext context) {
		data = new BusinessController().getBackRefListData(entity, entityName, linkName, queryName, criteria, viewCtrl.getCurrentView()
				.getAction(), context);
		if (viewCtrl.getCurrentView().getAction().getPersistence() == Persistence.INSERT) {
			// Current entity is not persisted yet, it can't be linked to anything
			readonly = true;
		} else {
			readonly = data.isReadOnly();
		}
		isProtected = data.isProtected();
		if (viewCtrl.getCurrentView().getAction().getPersistence() == Persistence.INSERT) {
			// Current entity is not persisted yet, it can't be linked to anything
			readonly = true;
		} else {
			readonly = data.isReadOnly();
		}
		isProtected = data.isProtected();
	}

	/**
	 * Shortcut method to detach backref. Detachment will be persisted.
	 * 
	 * @return Page to display for detach action.
	 */
	public String detach() {
		return prepareAction(Action.getDetachBackRefAction());
	}

	/**
	 * Shortcut method to attach new elements. Attachment will be persisted.
	 * 
	 * @return Page to display for attach action.
	 */
	public String attach() {
		return prepareAction(Action.getAttachBackRefAction(filterName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.logica.jsf.model.list.AbstractListModel#export()
	 */
	@Override
	public void export(String exportType, RequestContext context) {
		File export = null;
		BusinessController bc = new BusinessController();
		if ("xls".equals(exportType)) {
			export = bc.getBackRefListExportXls(entity, entityName, linkName, queryName, context);
		}
		if (export != null) {
			downloadFile(export);
		}
	}

}
