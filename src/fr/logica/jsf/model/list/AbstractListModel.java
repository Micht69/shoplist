package fr.logica.jsf.model.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import fr.logica.business.Action;
import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Constants;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.MessageUtils;
import fr.logica.business.TechnicalException;
import fr.logica.business.context.RequestContext;
import fr.logica.business.data.ColumnData;
import fr.logica.business.data.ListCriteria;
import fr.logica.business.data.ListData;
import fr.logica.business.data.Row;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;
import fr.logica.security.SecurityUtils;

public abstract class AbstractListModel extends DataModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -4595841921651081630L;

	protected ListData data;

	protected String entityName;

	protected String selectedRowEncodedKey;

	/** Store javascript filter fields data */
	protected String jsFilter;
	private static final String JS_FILTER = "jsFilter";

	protected ListCriteria criteria;

	public AbstractListModel(ViewController viewCtrl, Map<String, String> store, String entityName) {
		super(viewCtrl);
		this.entityName = entityName;
		this.criteria = new ListCriteria();
		this.jsFilter = store.get(JS_FILTER);
		Action currentAction = viewCtrl.getCurrentView().getAction();
		if (currentAction.getPersistence() == Persistence.DELETE || currentAction.getUi() == UserInterface.READONLY) {
			isProtected = true;
		}
	}

	@Override
	public List<Key> getSelected() {
		List<Key> keys = new ArrayList<Key>();
		if (selectedRowEncodedKey != null) {
			// Line selected via default action
			Key primaryKey = new Key(entityName);
			primaryKey.setEncodedValue(selectedRowEncodedKey);
			keys.add(primaryKey);
			selectedRowEncodedKey = null;
		} else {
			for (Row row : data.getRows()) {
				if (row.checked()) {
					keys.add(row.getPk());
				}
			}
		}
		return keys;
	}

	public void sortBy(String field) {
		criteria.sortBy(field);
		reload();
	}

	public String sortedStyle(String field) {
		if (field.equals(criteria.orderByField)) {
			return "datatable-header-sort-" + criteria.orderByDirection.toLowerCase();
		}
		return "datatable-header-sort-no";
	}

	public String getResultCount() {
		if (data.getRows().size() == 0) {
			return MessageUtils.getInstance(viewCtrl.getContext()).getLabel("results.nodata", (Object[]) null);
		}
		if (data.getRows().size() == 1) {
			return "1 " + MessageUtils.getInstance(viewCtrl.getContext()).getLabel("liste.result.resultat", (Object[]) null);
		}
		return data.getRows().size() + " " + MessageUtils.getInstance(viewCtrl.getContext()).getLabel("liste.result.resultats", (Object[]) null);
	}

	public String getResultCountStyle() {
		if (data.getRows().size() == 0) {
			return "results-nodata";
		}
		return "";
	}

	public String getTotalResultCount() {
		if (data.getRows().size() > 0 && data.getTotalRowCount() > data.getRows().size()) {
			return " " + MessageUtils.getInstance(viewCtrl.getContext()).getLabel("liste.result.total", new Object[] { Integer.valueOf(data.getTotalRowCount()) });
		}
		return "";
	}

	/**
	 * Returns <code>true</code> if at least one of the actions passed (as space-separated codes) is available (especially regarding the
	 * current's user authorizations).
	 */
	public boolean hasAvailableActionIn(String actionsString) {
		return viewCtrl.isSelect() || (getDefaultAction(actionsString) != null);
	}

	protected Action getDefaultAction(String actionsString) {
		return getDefaultAction(actionsString, Input.ONE, Input.MANY);
	}

	protected Action getDefaultAction(String actionsString, Input... inputType) {
		if (actionsString == null || actionsString.trim().length() == 0) {
			return null;
		}
		String[] actions = actionsString.split(",");
		List<Input> inputTypes = Arrays.asList(inputType);
		RequestContext context = new RequestContext(viewCtrl.getSessionCtrl().getContext());
		try {
			for (String actionCodeString : actions) {
				Integer actionCode = null;
				try {
					actionCode = Integer.parseInt(actionCodeString);
				} catch (NumberFormatException ex) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid default action : " + actionCodeString, null));
					return null;
				}
				if (actionCode == Constants.DETACH) {
					continue;
				}

				EntityModel model = EntityManager.getEntityModel(entityName);
				Action action = model.getAction(actionCode);
				if (isProtected && (action.getUi() != UserInterface.READONLY || action.getPersistence() != Persistence.NONE)) {
					continue;
				}
				if (!inputTypes.contains(action.getInput())) {
					continue;
				}
				if (!SecurityUtils.getSecurityManager().isActionRendered(model.name(), action.getCode(),
						viewCtrl.getSessionCtrl().getContext())) {
					continue;
				}
				return action;
			}
		} finally {
			context.close();
		}
		return null;
	}

	@Override
	public void storeViewData(Map<String, String> store) {
		store.put(JS_FILTER, jsFilter);
	}

	/**
	 * Exports current list data
	 * 
	 * @param exportType Export type to use (CSV, XLS, etc.)
	 * @return Return value is JSF compliant, null when HTTP response has been sent with data, currentView.getURL() if we need to reload current
	 *         view with an error message.
	 */
	public String export(String exportType) {
		RequestContext context = null;
		try {
			context = new RequestContext(viewCtrl.getSessionCtrl().getContext());
			export(exportType, context);
			// Exports ended smoothly, HTTP response contains the data, we won't redirect anywhere.
			return null;
		} catch (FunctionalException fEx) {
			viewCtrl.displayMessages(fEx);
		} catch (TechnicalException tEx) {
			viewCtrl.displayMessages(tEx);
		} finally {
			if (context != null) {
				viewCtrl.displayMessages(context);
				context.close();
			}
		}
		// An exception occurred, we reload current view
		return viewCtrl.getCurrentView().getURL();
	}
	public abstract void export(String exportType, RequestContext context);

	public Integer getMaxRow() {
		return criteria.maxRow;
	}

	public void setMaxRow(Integer maxRow) {
		if (maxRow == null) {
			this.criteria.maxRow = Constants.MAX_ROW;
		} else if (maxRow <= 0 || maxRow > Constants.MAX_ROW_ABSOLUTE) {
			this.criteria.maxRow = Constants.MAX_ROW_ABSOLUTE;
		} else {
			this.criteria.maxRow = maxRow;
		}
	}

	public List<Row> getRows() {
		return data.getRows();
	}

	public Map<String, ColumnData> getColumns() {
		return data.getColumns();
	}

	public String getJsFilter() {
		return jsFilter;
	}

	public void setJsFilter(String jsFilter) {
		this.jsFilter = jsFilter;
	}

	public String getSelectedRowEncodedKey() {
		return selectedRowEncodedKey;
	}

	public void setSelectedRowEncodedKey(String selectedRowEncodedKey) {
		this.selectedRowEncodedKey = selectedRowEncodedKey;
	}

	public String getSearchCriteria() {
		return criteria.searchCriteria;
	}

	public void setSearchCriteria(String searchCriteria) {
		criteria.searchCriteria = searchCriteria;
	}

}
