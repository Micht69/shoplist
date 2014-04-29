package fr.logica.jsf.components.schedule;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import fr.logica.business.data.ScheduleEvent;

/**
 * Data model used by the schedule component.
 */
public class ScheduleModel implements Serializable {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * The list of events to display.
	 */
	private List<ScheduleEvent> events;

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

	public List<ScheduleEvent> getEvents() {
		return events;
	}

	public void setEvents(List<ScheduleEvent> events) {
		this.events = events;
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

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder("ScheduleModel [selectedDate=").append(selectedDate).append(", selectedEvent=")
				.append(selectedEvent).append(", selectedView=").append(selectedView).append(", events={");
		if (null != events) {
			for (ScheduleEvent event : events) {
				toString.append("\n").append(event.toString());
			}
		}
		toString.append("}]");
		return toString.toString();
	}

}
