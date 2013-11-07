package fr.logica.jsf.components.schedule;

import java.io.Serializable;
import java.util.Date;

/**
 * Event object used by the schedule component.
 */
public class ScheduleEvent implements Serializable {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Event id. */
	private String id;

	/** The text diplayed on event's element. */
	private String title;

	/** Indicates if this event lasts all day. */
	private boolean allDay;

	/** Start time. */
	private Date start;

	/** End time. */
	private Date end;

	/** Indicates if this event is protected. */
	private boolean readonly;

	/** CSS class name for this event's element. */
	private String className;

	/** Background and border color for this event's element. */
	private String color;

	/** Background for this event's element. */
	private String backgroundColor;

	/** Border color for this event's element. */
	private String borderColor;

	/** Text color for this event's element. */
	private String textColor;

	/** Indicates whether this event has been updated. */
	private boolean updated;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		ScheduleEvent other = (ScheduleEvent) obj;

		return (id == null && other.id == null || id != null && id.equals(other.id))
				&& (start == null && other.start == null || start != null && start.equals(other.start))
				&& (end == null && other.end == null || end != null && end.equals(other.end));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ScheduleEvent [id=" + id + ", title=" + title + ", allDay=" + allDay + ", start="
				+ start + ", end=" + end + ", readonly=" + readonly + "]";
	}

}
