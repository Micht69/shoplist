package fr.logica.jsf.components.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UpdateModelException;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.ListenerFor;
import javax.faces.event.PhaseId;
import javax.faces.event.PostAddToViewEvent;

import com.sun.faces.util.MessageFactory;

import fr.logica.business.data.ScheduleEvent;

@ResourceDependencies({
		@ResourceDependency(name = "schedule/fullcalendar.css"),
		@ResourceDependency(name = "schedule/fullcalendar.js", target = "body")
})
@FacesComponent(HtmlSchedule.COMPONENT_TYPE)
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class HtmlSchedule extends UIInput {

	private static final String OPTIMIZED_PACKAGE = "fr.logica.jsf.components.schedule";
	private static final String ATTRIBUTES_THAT_ARE_SET = "javax.faces.component.UIComponentBase.attributesThatAreSet";
	private static final String DEFAULT_RENDERER = "fr.logica.jsf.components.schedule.ScheduleRenderer";
	public static final String COMPONENT_TYPE = "cgi.faces.HtmlSchedule";

	protected enum PropertyKeys {
		date,
		dayClick,
		firstDay,
		firstHour,
		locale,
		localDateSet,
		localSelectedEventSet,
		localViewSet,
		maxTime,
		minTime,
		onchange,
		readonly,
		selectedEvent,
		showWeekends,
		slotMinutes,
		value,
		view;
	}

	private String submittedDate = null;
	private String submittedSelectedEvent = null;
	private String submittedView = null;

	public HtmlSchedule() {
		setRendererType(DEFAULT_RENDERER);
	}

	public Date getDate() {
		return (Date) getStateHelper().eval(PropertyKeys.date, null);
	}

	public void setDate(Date date) {
		getStateHelper().put(PropertyKeys.date, date);
		setLocalDateSet(true);
	}

	public String getDayClick() {
		return (String) getStateHelper().eval(PropertyKeys.dayClick, null);
	}

	public void setDayClick(String dayClick) {
		getStateHelper().put(PropertyKeys.dayClick, dayClick);
		handleAttribute(PropertyKeys.dayClick.toString(), dayClick);
	}

	public Integer getFirstDay() {
		return (Integer) getStateHelper().eval(PropertyKeys.firstDay, null);
	}

	public void setFirstDay(Integer firstDay) {
		getStateHelper().put(PropertyKeys.firstDay, firstDay);
		handleAttribute(PropertyKeys.firstDay.toString(), firstDay);
	}

	public Integer getFirstHour() {
		return (Integer) getStateHelper().eval(PropertyKeys.firstHour, null);
	}

	public void setFirstHour(Integer firstHour) {
		getStateHelper().put(PropertyKeys.firstHour, firstHour);
		handleAttribute(PropertyKeys.firstHour.toString(), firstHour);
	}

	public Locale getLocale() {
		return (Locale) getStateHelper().eval(PropertyKeys.locale, null);
	}

	public void setLocale(Locale locale) {
		getStateHelper().put(PropertyKeys.locale, locale);
	}

	public Integer getMaxTime() {
		return (Integer) getStateHelper().eval(PropertyKeys.maxTime, null);
	}

	public void setMaxTime(Integer maxTime) {
		getStateHelper().put(PropertyKeys.maxTime, maxTime);
		handleAttribute(PropertyKeys.maxTime.toString(), maxTime);
	}

	public Integer getMinTime() {
		return (Integer) getStateHelper().eval(PropertyKeys.minTime, null);
	}

	public void setMinTime(Integer minTime) {
		getStateHelper().put(PropertyKeys.minTime, minTime);
		handleAttribute(PropertyKeys.minTime.toString(), minTime);
	}

	public String getOnchange() {
		return (String) getStateHelper().eval(PropertyKeys.onchange, null);

	}

	public void setOnchange(String onchange) {
		getStateHelper().put(PropertyKeys.onchange, onchange);
		handleAttribute(PropertyKeys.onchange.toString(), onchange);
	}

	public boolean isReadonly() {
		return (Boolean) getStateHelper().eval(PropertyKeys.readonly, Boolean.FALSE);
	}

	public void setReadonly(boolean readonly) {
		getStateHelper().put(PropertyKeys.readonly, readonly);
	}

	public ScheduleEvent getSelectedEvent() {
		return (ScheduleEvent) getStateHelper().eval(PropertyKeys.selectedEvent, null);
	}

	public void setSelectedEvent(ScheduleEvent selectedEvent) {
		getStateHelper().put(PropertyKeys.selectedEvent, selectedEvent);
		setLocalSelectedEventSet(true);
	}

	public Boolean getShowWeekends() {
		return (Boolean) getStateHelper().eval(PropertyKeys.showWeekends, Boolean.TRUE);
	}

	public void setShowWeekends(Boolean showWeekends) {
		getStateHelper().put(PropertyKeys.showWeekends, showWeekends);
		handleAttribute(PropertyKeys.showWeekends.toString(), showWeekends);
	}

	public Integer getSlotMinutes() {
		return (Integer) getStateHelper().eval(PropertyKeys.slotMinutes, null);
	}

	public void setSlotMinutes(Integer slotMinutes) {
		getStateHelper().put(PropertyKeys.slotMinutes, slotMinutes);
		handleAttribute(PropertyKeys.slotMinutes.toString(), slotMinutes);
	}

	@SuppressWarnings("unchecked")
	public List<ScheduleEvent> getValue() {
		return (List<ScheduleEvent>) getStateHelper().eval(PropertyKeys.value, null);
	}

	public void setValue(List<ScheduleEvent> events) {
		getStateHelper().put(PropertyKeys.value, events);
	}

	public ScheduleView getView() {
		return (ScheduleView) getStateHelper().eval(PropertyKeys.view, null);
	}

	public void setView(ScheduleView view) {
		getStateHelper().put(PropertyKeys.view, view);
		setLocalViewSet(true);
	}

	public boolean isLocalDateSet() {
		return (Boolean) getStateHelper().eval(PropertyKeys.localDateSet, false);
	}

	public void setLocalDateSet(boolean localDateSet) {
		getStateHelper().put(PropertyKeys.localDateSet, localDateSet);
	}

	public boolean isLocalSelectedEventSet() {
		return (Boolean) getStateHelper().eval(PropertyKeys.localSelectedEventSet, false);
	}

	public void setLocalSelectedEventSet(boolean localSelectedEventSet) {
		getStateHelper().put(PropertyKeys.localSelectedEventSet, localSelectedEventSet);
	}

	public boolean isLocalViewSet() {
		return (Boolean) getStateHelper().eval(PropertyKeys.localViewSet, false);
	}

	public void setLocalViewSet(boolean localViewSet) {
		getStateHelper().put(PropertyKeys.localViewSet, localViewSet);
	}

	public String getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(String submittedDate) {
		this.submittedDate = submittedDate;
	}

	public String getSubmittedSelectedEvent() {
		return submittedSelectedEvent;
	}

	public void setSubmittedSelectedEvent(String submittedSelectedEvent) {
		this.submittedSelectedEvent = submittedSelectedEvent;
	}

	public String getSubmittedView() {
		return submittedView;
	}

	public void setSubmittedView(String submittedView) {
		this.submittedView = submittedView;
	}

	@Override
	public void processUpdates(FacesContext context) {
		super.processUpdates(context);

		ValueExpression viewVe = getValueExpression(PropertyKeys.view.toString());

		if (null != viewVe) {
			viewVe.setValue(context.getELContext(), getView());
		}

		ValueExpression dateVe = getValueExpression(PropertyKeys.date.toString());

		if (null != dateVe) {
			dateVe.setValue(context.getELContext(), getDate());
		}
	}

	@Override
	public void updateModel(FacesContext context) {
		super.updateModel(context);

		// Selected date.
		if (updateProperty(context, PropertyKeys.date, isLocalDateSet(), getDate())) {
			setDate(null);
			setLocalDateSet(false);
		}

		// Selected event.
		if (updateProperty(context, PropertyKeys.selectedEvent, isLocalSelectedEventSet(), getSelectedEvent())) {
			setSelectedEvent(null);
			setLocalSelectedEventSet(false);
		}

		// Selected view.
		if (updateProperty(context, PropertyKeys.view, isLocalViewSet(), getView())) {
			setView(null);
			setLocalViewSet(false);
		}

	}

	private boolean updateProperty(FacesContext context, PropertyKeys key, boolean localValueSet, Object value) {

		if (!localValueSet) {
			return false;
		}

		ValueExpression ve = getValueExpression(key.toString());
		Throwable caught = null;

		if (null != ve) {
			FacesMessage message = null;

			try {
				ve.setValue(context.getELContext(), value);

			} catch (ELException e) {
				caught = e;
				String messageStr = e.getMessage();
				Throwable result = e.getCause();

				while (null != result && result.getClass().isAssignableFrom(ELException.class)) {
					messageStr = result.getMessage();
					result = result.getCause();
				}

				if (null == messageStr) {
					message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
				} else {
					message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageStr, messageStr);
				}
				setValid(false);

			} catch (Exception e) {
				caught = e;
				message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
				setValid(false);
			}

			if (null != caught) {
				UpdateModelException toQueue = new UpdateModelException(message, caught);
				ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext(context, toQueue, this, PhaseId.UPDATE_MODEL_VALUES);
				context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, eventContext);
			}
		}
		return (null != caught);
	}

	@Override
	public void validate(FacesContext context) {
		// Events
		super.validate(context);

		// Selected date.
		String submittedDate = getSubmittedDate();

		if (null != submittedDate && !submittedDate.isEmpty()) {

			try {
				Date date = new Date(Long.parseLong(submittedDate));
				setDate(date);
				setSubmittedDate(null);
			} catch (NumberFormatException e) {
				FacesMessage message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
				context.addMessage(getClientId(context), message);
	            setValid(false);
			}
		}

		// Selected event.
		String submittedSelectedEvent = getSubmittedSelectedEvent();

		if (null != submittedSelectedEvent && !submittedSelectedEvent.isEmpty()) {
			List<ScheduleEvent> events = getValue();
			ScheduleEvent selectedEvent = null;

			if (null != events) {
				Iterator<ScheduleEvent> iter = events.iterator();

				while (null == selectedEvent && iter.hasNext()) {
					ScheduleEvent tmp = iter.next();

					if (null != tmp && submittedSelectedEvent.equals(tmp.getId())) {
						selectedEvent = tmp;
					}
				}
				setSelectedEvent(selectedEvent);
				setSubmittedSelectedEvent(null);
			}

			if (null == selectedEvent) {
				FacesMessage message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
				context.addMessage(getClientId(context), message);
	            setValid(false);
			}
		}

		// Selected view.
		String submittedView = getSubmittedView();

		if (null != submittedView && !submittedView.isEmpty()) {

			try {
				ScheduleView view = ScheduleView.valueOf(submittedView);
				setView(view);
				setSubmittedView(null);

			} catch (IllegalArgumentException e) {
				FacesMessage message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
				context.addMessage(getClientId(context), message);
	            setValid(false);
			}
		}
	}

	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {

		if (event instanceof PostAddToViewEvent) {
			FacesContext context = FacesContext.getCurrentInstance();
			Locale locale = getLocale();

			if (null == locale) {
				locale = context.getApplication().getDefaultLocale();
			}

			if (Locale.FRANCE.getLanguage().equals(locale.getLanguage())) {
				UIOutput resource = new UIOutput();
				resource.getAttributes().put("name", "schedule/fullcalendar.fr.js");
				resource.setRendererType("javax.faces.resource.Script");
				context.getViewRoot().addComponentResource(context, resource, "body");
			}
		}
		super.processEvent(event);
	}

	private void handleAttribute(String name, Object value) {
		@SuppressWarnings("unchecked")
		List<String> setAttributes = (List<String>) this.getAttributes().get(ATTRIBUTES_THAT_ARE_SET);
		if (setAttributes == null) {
			String cname = this.getClass().getName();
			if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
				setAttributes = new ArrayList<String>(9);
				this.getAttributes().put(ATTRIBUTES_THAT_ARE_SET, setAttributes);
			}
		}
		if (setAttributes != null) {
			if (value == null) {
				ValueExpression ve = getValueExpression(name);
				if (ve == null) {
					setAttributes.remove(name);
				}
			} else if (!setAttributes.contains(name)) {
				setAttributes.add(name);
			}
		}
	}

}
