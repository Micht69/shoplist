package fr.logica.business.data;

import java.util.Date;
import java.util.Map;

import fr.logica.business.Key;

/**
 * Event object used by the schedule component.
 */
public class ScheduleEvent extends Row {

	/** SerialVersionUID. */
	private static final long serialVersionUID = -2650502843761183532L;

	public static final String EVENT_TITLE = "event-title";
	public static final String EVENT_ALL_DAY = "event-allDay";
	public static final String EVENT_READ_ONLY = "event-readOnly";
	public static final String EVENT_DATE_START = "event-dateStart";
	public static final String EVENT_DATE_END = "event-dateEnd";
	public static final String EVENT_CSS_CLASSNAME = "event-className";
	public static final String EVENT_CSS_COLOR = "event-color";
	public static final String EVENT_CSS_BACKGROUND_COLOR = "event-backgroundColor";
	public static final String EVENT_CSS_BORDER_COLOR = "event-borderColor";
	public static final String EVENT_CSS_TEXT_COLOR = "event-textColor";

	/**
	 * @deprecated Use specific setters instead.
	 */
	@Override
	public Object put(String key, Object value) {
		return super.put(key, value);
	}

	/**
	 * @deprecated Use specific setters instead.
	 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		super.putAll(m);
	}

	public String getId() {
		String id;
		Key pk = getPk();

		if (null != pk) {
			id = pk.getEncodedValue();
		} else {
			id = null;
		}
		return id;
	}

	public String getTitle() {
		return (String) super.get(EVENT_TITLE);
	}

	public void setTitle(String title) {
		super.put(EVENT_TITLE, title);
	}

	public boolean isAllDay() {
		boolean isAllDay = false;
		Boolean allDay = (Boolean) super.get(EVENT_ALL_DAY);

		if (null != allDay) {
			isAllDay = allDay.booleanValue();
		}
		return isAllDay;
	}

	public void setAllDay(boolean allDay) {
		super.put(EVENT_ALL_DAY, allDay);
	}

	public Date getStart() {
		return (Date) super.get(EVENT_DATE_START);
	}

	public void setStart(Date start) {
		super.put(EVENT_DATE_START, start);
	}

	public Date getEnd() {
		return (Date) super.get(EVENT_DATE_END);
	}

	public void setEnd(Date end) {
		super.put(EVENT_DATE_END, end);
	}

	public boolean isReadonly() {
		boolean isReadonly = false;
		Boolean readonly = (Boolean) super.get(EVENT_READ_ONLY);

		if (null != readonly) {
			isReadonly = readonly.booleanValue();
		}
		return isReadonly;
	}

	public void setReadonly(boolean readonly) {
		super.put(EVENT_READ_ONLY, readonly);
	}

	public String getClassName() {
		return (String) super.get(EVENT_CSS_CLASSNAME);
	}

	public void setClassName(String className) {
		super.put(EVENT_CSS_CLASSNAME, className);
	}

	public String getColor() {
		return (String) super.get(EVENT_CSS_COLOR);
	}

	public void setColor(String color) {
		super.put(EVENT_CSS_COLOR, color);
	}

	public String getBackgroundColor() {
		return (String) super.get(EVENT_CSS_BACKGROUND_COLOR);
	}

	public void setBackgroundColor(String backgroundColor) {
		super.put(EVENT_CSS_BACKGROUND_COLOR, backgroundColor);
	}

	public String getBorderColor() {
		return (String) super.get(EVENT_CSS_BORDER_COLOR);
	}

	public void setBorderColor(String borderColor) {
		super.put(EVENT_CSS_BORDER_COLOR, borderColor);
	}

	public String getTextColor() {
		return (String) super.get(EVENT_CSS_TEXT_COLOR);
	}

	public void setTextColor(String textColor) {
		super.put(EVENT_CSS_TEXT_COLOR, textColor);
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
		String id = getId();
		String otherId = other.getId();
		Date start = getStart();
		Date otherStart = other.getStart();
		Date end = getEnd();
		Date otherEnd = other.getEnd();

		return (id == null && otherId == null || id != null && id.equals(otherId))
				&& (start == null && otherStart == null || start != null && start.equals(otherStart))
				&& (end == null && otherEnd == null || end != null && end.equals(otherEnd));
	}

	@Override
	public int hashCode() {
		String id = getId();
		Date start = getStart();
		Date end = getEnd();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ScheduleEvent [id=" + getId() + ", title=" + getTitle() + ", allDay=" + isAllDay() + ", start="
				+ getStart() + ", end=" + getEnd() + ", readonly=" + isReadonly() + "]";
	}

}
