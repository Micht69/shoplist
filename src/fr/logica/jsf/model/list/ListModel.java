package fr.logica.jsf.model.list;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;

import fr.logica.application.AbstractApplicationLogic.OpenCriteriaBehavior;
import fr.logica.application.ApplicationUtils;
import fr.logica.business.Action;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.data.ListCriteria;
import fr.logica.business.data.ListData;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.utils.CriteriaVisitor;
import fr.logica.jsf.webflow.View;

public class ListModel extends AbstractListModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 5100787640158223457L;

	protected Entity entity;
	protected String queryName;

	protected boolean displayCriterias;

	private int currentRowCount;

	private static final String DISPLAY_CRITERIA = "displayCriterias";

	public ListModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String queryName) {
		super(viewCtrl, store, entityName);
		this.entity = entity;
		this.queryName = queryName;
		loadData(viewCtrl.getContext());
		// Do we have to open criteria at list opening ?
		if (store.get(DISPLAY_CRITERIA) != null) {
			displayCriterias = "true".equals(store.get(DISPLAY_CRITERIA));
		} else {
			OpenCriteriaBehavior behavior = ApplicationUtils.getApplicationLogic().getOpenCriteriaBehavior();
			if (behavior == OpenCriteriaBehavior.ALWAYS) {
				displayCriterias = true;
			} else if (behavior == OpenCriteriaBehavior.NEVER) {
				displayCriterias = false;
			} else {
				if (data.getTotalRowCount() > criteria.maxRow) {
					displayCriterias = true;
				}
			}
		}
	}

	@Override
	public void storeViewData(Map<String, String> store) {
		super.storeViewData(store);
		store.put(DISPLAY_CRITERIA, displayCriterias ? "true" : "false");
	}

	public String prepareAction(Integer actionCode, String queryName) {
		return viewCtrl.prepareView(entityName, EntityManager.getEntityModel(entityName).getAction(actionCode), getSelected(), queryName,
				null, null, false);
	}

	public String prepareActionCode(Integer actionCode) {
		return prepareAction(actionCode, null);
	}

	public String prepareDefaultAction(String actionsString) {
		if (viewCtrl.isSelect()) {
			// Default action is the selection of an element for current selection action
			return viewCtrl.validate();
		}
		Action defaultAction = getDefaultAction(actionsString);
		if (defaultAction != null) {
			return viewCtrl.prepareView(entityName, defaultAction, getSelected(), null,
					null, null, false);
		}
		return null;
	}

	@Override
	public void loadData(RequestContext context) {
		data = new BusinessController().getListData(entity, entityName, queryName, criteria, viewCtrl.getCurrentView().getAction(), viewCtrl
				.getCurrentView().getLinkName(), viewCtrl.getCurrentView().getLinkedEntity(), context);
	}

	public void moreLines() {
		RequestContext context = null;
		try {
			context = new RequestContext(viewCtrl.getSessionCtrl().getContext());

			if (currentRowCount == 0) {
				currentRowCount = criteria.maxRow;
			}

			ListCriteria moreCriteria = new ListCriteria();
			moreCriteria.orderByField = criteria.orderByField;
			moreCriteria.orderByDirection = criteria.orderByDirection;
			moreCriteria.minRow = criteria.minRow + currentRowCount;
			moreCriteria.maxRow = criteria.maxRow; // maxRow is used as the number of row to fetch from DB, not the last row number
			ListData moreData = new BusinessController().getListData(entity, entityName, queryName, moreCriteria, viewCtrl.getCurrentView()
					.getAction(), viewCtrl
					.getCurrentView().getLinkName(), viewCtrl.getCurrentView().getLinkedEntity(), context);
			data.getRows().addAll(moreData.getRows());
			currentRowCount = currentRowCount + criteria.maxRow;
		} finally {
			if (context != null) {
				// Close request context potential database connection
				context.close();
			}
		}
		viewCtrl.displayMessages(context);
	}

	public boolean hasMoreRows() {
		return (data.getRows().size() < data.getTotalRowCount());
	}

	@Override
	public void validateView(View currentView) {
		if (viewCtrl.isSelect()) {
			currentView.setKeys(getSelected());
		}
		super.validateView(currentView);
	}

	public String getCriteriaDesc() {
		UIComponent c = FacesContext.getCurrentInstance().getViewRoot().findComponent("mainForm:criterias");
		if (null != c) {
			CriteriaVisitor criteriaVisitor = new CriteriaVisitor();
			c.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), criteriaVisitor);
			return criteriaVisitor.getCriterias();
		}
		return "";
	}

	public void export(String exportType, RequestContext context) {
		File export = null;
		BusinessController bc = new BusinessController();
		if ("xls".equals(exportType)) {
			export = bc.getListExportXls(entity, entityName, queryName, criteria, viewCtrl.getCurrentView().getAction(), viewCtrl
					.getCurrentView().getLinkName(), viewCtrl.getCurrentView().getLinkedEntity(), context);
		}
		if (export != null) {
			downloadFile(export);
		}
	}

	public boolean isDisplayCriterias() {
		return displayCriterias;
	}

	public void setDisplayCriterias(boolean displayCriterias) {
		this.displayCriterias = displayCriterias;
	}
}
