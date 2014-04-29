package fr.logica.jsf.model.backref;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.Action.Input;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.controller.Request;
import fr.logica.business.data.Row;
import fr.logica.business.data.ScheduleEvent;
import fr.logica.jsf.components.schedule.ScheduleView;
import fr.logica.jsf.controller.ViewController;
import fr.logica.reflect.DomainUtils;

/**
 * List model to display a back reference into a schedule.
 */
public class BackRefScheduleModel extends BackRefListModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 8323608159115549555L;

	/**
	 * The initial date to display or current selected date.
	 */
	private Date selectedDate;

	/**
	 * The selected event.
	 */
	private ScheduleEvent selectedEvent;

	/**
	 * The initial view to display or current selected view.
	 */
	private ScheduleView selectedView;

	public BackRefScheduleModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String queryName,
			String linkName) {
		super(viewCtrl, store, entity, entityName, queryName, linkName, null);
		String date = store.get("selectedDate");
		if (null != date) {
			selectedDate = new Date(Long.valueOf(date));
		}
		String view = store.get("selectedView");
		if (null != view) {
			selectedView = ScheduleView.valueOf(view);
		}
	}

	@Override
	public void loadData(RequestContext context) {
		Action action = viewCtrl.getCurrentView().getAction();
		data = new BusinessController().getBackRefScheduleData(entity, entityName, linkName, queryName, action, context);
	}

	@Override
	public void storeViewData(Map<String, String> store) {
		super.storeViewData(store);
		store.put("selectedDate", selectedDate != null ? String.valueOf(selectedDate.getTime()) : null);
		store.put("selectedView", selectedView != null ? selectedView.toString() : null);
	}

	@Override
	public List<Key> getSelected() {
		List<Key> keys = new ArrayList<Key>();
		if (null != getSelectedEvent()) {
			keys.add(getSelectedEvent().getPk());
		}
		return keys;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String prepareDefaultAction(String actions) {
		Action defaultAction = null;
		if (getSelected().size() == 0) {
			defaultAction = getDefaultAction(actions, Input.NONE);
		} else {
			defaultAction = getDefaultAction(actions);
		}
		if (defaultAction != null) {
			if (Action.Persistence.INSERT == defaultAction.getPersistence()) {
				// Est-ce �l�gant car cela duplique la logique de ViewController ?
				Request request = new Request(entityName, defaultAction, getSelected(), null, linkName, true);
				request.setContext(new RequestContext(viewCtrl.getSessionCtrl().getContext()));
				Entity newEvent = DomainUtils.newDomain(entityName);
				// et un peu de BusinessController...
				String keyName = newEvent.getModel().getLinkModel(linkName).getKeyName();
				newEvent.setForeignKey(keyName, entity.getPrimaryKey());
				// Bref ce serait peut-�tre plus �l�gant de faire des m�thodes sp�cifiques dans ViewController et BusinessController.
				new BusinessController().initEventCreation(newEvent, defaultAction, getSelectedDate(), request.getContext());
				request.setEntity(newEvent);
				request.setLinkedEntity(entity);
				return viewCtrl.prepareView(request);

			} else {
				return viewCtrl.prepareView(entityName, defaultAction, getSelected(), null,
						linkName, entity, true);
			}
		}
		return null;
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	public ScheduleEvent getSelectedEvent() {
		return selectedEvent;
	}

	public void setSelectedEvent(ScheduleEvent selectedEvent) {
		this.selectedEvent = selectedEvent;
	}

	public ScheduleView getSelectedView() {
		return selectedView;
	}

	public void setSelectedView(ScheduleView selectedView) {
		this.selectedView = selectedView;
	}

	public void setRows(List<Row> rows) {
		// Do nothing. This method exists because hh:schedule inherits from UIInput.
	}
}
