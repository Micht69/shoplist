package fr.logica.jsf.components.schedule;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;

import fr.logica.business.data.ScheduleEvent;

/**
 * Event object used by the schedule component while the user moves or resizes an event onto the schedule.
 */
public class HtmlScheduleEvent extends FacesEvent {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Event object (business data).
	 */
	private final ScheduleEvent event;

	/**
	 * Creates a new event.
	 * @param component Schedule component.
	 * @param event Event object (business data).
	 */
	public HtmlScheduleEvent(UIComponent component, ScheduleEvent event) {
		super(component);
		this.event = event;
	}

	@Override
	public boolean isAppropriateListener(FacesListener listener) {
		return false;
	}

	@Override
	public void processListener(FacesListener listener) {
	}

	public ScheduleEvent getEvent() {
		return event;
	}

}
