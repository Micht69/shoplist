package fr.logica.jsf.components.schedule;

import static fr.logica.jsf.components.RendererUtils.getEscapedClientId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import fr.logica.business.data.ScheduleEvent;
import fr.logica.jsf.components.schedule.HtmlSchedule.PropertyKeys;

@FacesRenderer(componentFamily = HtmlSchedule.COMPONENT_FAMILY, rendererType = ScheduleRenderer.RENDERER_TYPE)
public class ScheduleRenderer extends Renderer {

	private static final String EVENT_KEY = "event-";
	private static final String AJAX_EVENT_ID_KEY = "ajax-event-id";
	private static final String AJAX_EVENT_START_KEY = "ajax-event-start";
	private static final String AJAX_EVENT_END_KEY = "ajax-event-end";
	public static final String RENDERER_TYPE = "cgi.faces.Schedule";

	@Override
	@SuppressWarnings("unchecked")
	public void decode(FacesContext context, UIComponent component) {
		Map<String, String> params = context.getExternalContext().getRequestParameterMap();
		HtmlSchedule schedule = (HtmlSchedule) component;

		if (!schedule.isReadonly() && schedule.isScheduleRequest(context)) {
			String clientId = schedule.getClientId(context);
			String eventId = params.get(clientId + "-" + AJAX_EVENT_ID_KEY);
			ScheduleEvent scheduleEvent = findEvent(schedule, eventId);

			if (scheduleEvent != null) {
				ScheduleEvent ajaxEvent = new ScheduleEvent();
				ajaxEvent.setPk(scheduleEvent.getPk());
				ajaxEvent.setStart(new Date(Long.valueOf(params.get(clientId + "-" + AJAX_EVENT_START_KEY))));
				ajaxEvent.setEnd(new Date(Long.valueOf(params.get(clientId + "-" + AJAX_EVENT_END_KEY))));
				HtmlScheduleEvent event = new HtmlScheduleEvent(schedule, ajaxEvent);
				event.setPhaseId(PhaseId.INVOKE_APPLICATION);
				schedule.queueEvent(event);
			}
		} else {
			Map<String, Object> scheduleParams = getParameters(context, schedule, params);
			schedule.setSubmittedView((String) scheduleParams.get(PropertyKeys.view.toString()));
			schedule.setSubmittedDate((String) scheduleParams.get(PropertyKeys.date.toString()));
			schedule.setSubmittedValue((List<ScheduleEvent>) scheduleParams.get(PropertyKeys.value.toString()));
		}
	}

	private ScheduleEvent findEvent(HtmlSchedule schedule, String eventId) {
		List<ScheduleEvent> existingEvents = schedule.getEvents();

		if (existingEvents != null && !existingEvents.isEmpty()) {
			for (ScheduleEvent event : schedule.getEvents()) {
				if (event.getId().equals(eventId)) {
					return event;
				}
			}
		}
		return null;
	}

	private Map<String, Object> getParameters(FacesContext context, HtmlSchedule schedule, Map<String, String> requestParams) {
		Map<String, Object> params = new HashMap<String, Object>();
		List<ScheduleEvent> events = new ArrayList<ScheduleEvent>();
		String clientId = schedule.getClientId(context);
		int idLength = clientId.length() + 1;

		for (Entry<String, String> entry : requestParams.entrySet()) {

			if (entry.getKey().startsWith(clientId)) {
				String key = entry.getKey().substring(idLength);

				if (key.startsWith(EVENT_KEY)) {
					ScheduleEvent event = findEvent(schedule, entry.getValue());
					if (event != null) {
						events.add(event);
					}
				} else {
					params.put(key, entry.getValue());
				}
			}
		}

		if (!events.isEmpty()) {
			params.put(PropertyKeys.value.toString(), events);
		}
		return params;
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		HtmlSchedule schedule = (HtmlSchedule) component;
		String clientId = schedule.getClientId(context);

		if (schedule.isScheduleRequest(context)) {
			/*
			 * During an Ajax request concerning this component, only events are rendered to improve bandwith consumption, the component will be
			 * updated by Javascript. So it is necessary to close the current update tag before writing our stuff and to open a new update tag.
			 */
			PartialResponseWriter writer = context.getPartialViewContext().getPartialResponseWriter();
			writer.endUpdate();
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("id", component.getId());
			writer.startExtension(attributes);
			String events = renderEvents(context, schedule);
			writer.writeText(events, null);
			writer.endExtension();
			writer.startUpdate(clientId);
			return;
		}
		ResponseWriter writer = context.getResponseWriter();

		// Container
		writer.writeText("\n", null);
		writer.startElement("div", component);
		writer.writeAttribute("id", clientId + "-container", null);
		writer.writeAttribute("class", "schedule", null);
		writer.writeText("\n\t", null);

		// Schedule div
		writer.startElement("div", component);
		writer.writeAttribute("id", clientId, null);
		writer.endElement("div");
		writer.writeText("\n\t", null);

		// Script
		encodeScript(context, schedule);

		// Hidden fields to keep state
		encodeInput(writer, clientId, PropertyKeys.date.toString(), (schedule.getDate() != null) ? schedule.getDate().getTime() : null, null);
		encodeInput(writer, clientId, PropertyKeys.view.toString(), schedule.getView(), null);

		List<ScheduleEvent> selectedEvents = schedule.getValue();

		if (selectedEvents != null && !selectedEvents.isEmpty()) {
			int i = 0;
			for (ScheduleEvent event : selectedEvents) {
				encodeInput(writer, clientId, String.valueOf(i++), event.getId(), "fc-event-selected");
			}
		}

		// End of container
		writer.endElement("div");
	}

	private void encodeInput(ResponseWriter writer, String clientId, String property, Object value, String styleClass)
			throws IOException {

		writer.writeText("\t", null);
		writer.startElement("input", null);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId + "-" + property, null);
		writer.writeAttribute("name", clientId + "-" + property, null);

		if (value != null) {
			writer.writeAttribute("value", value, null);
		}

		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, null);
		}
		writer.endElement("input");
		writer.writeText("\n", null);
	}

	private void encodeScript(FacesContext context, HtmlSchedule schedule) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = schedule.getClientId(context);
		String escapedClientId = getEscapedClientId(clientId);
		String filterId = escapedClientId.replace("schedule", "globalFilter_schedule");

		writer.startElement("script", null);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeText("\n$(document).ready(function() {", null);
		writer.writeText("\n$('#" + escapedClientId + "').fullCalendar({"
				+ renderStringOption("defaultView", (null != schedule.getView()) ? schedule.getView().toString() : null, "\n\t")
				+ renderFunctionOption(PropertyKeys.dayClick.toString(), schedule.getDayClick(), "\n\t",
						"function(date, allDay, jsEvent, view) {\n\t\t", "\n\t}", !schedule.isReadonly())
				+ renderOption("editable", !schedule.isReadonly(), "\n\t")
				+ renderOption(PropertyKeys.firstDay.toString(), schedule.getFirstDay(), "\n\t")
				+ renderOption(PropertyKeys.firstHour.toString(), schedule.getFirstHour(), "\n\t")
				+ renderOption(PropertyKeys.minTime.toString(), schedule.getMinTime(), "\n\t")
				+ renderOption(PropertyKeys.maxTime.toString(), schedule.getMaxTime(), "\n\t")
				+ renderOption(PropertyKeys.showWeekends.toString(), schedule.getShowWeekends(), "\n\t")
				+ renderOption(PropertyKeys.slotMinutes.toString(), schedule.getSlotMinutes(), "\n\t")
				+ renderDate(schedule.getDate(), "\n\t")
				+ "\n" + renderEvents(context, schedule) + ","
				+ "\n\t" + "viewDisplay : function(view) {"
				+ "\n\t\t" + "$('#" + escapedClientId + "-view').val(view.name);"
				+ "\n\t\t" + "$('#" + escapedClientId + "-date').val($('#" + escapedClientId + "').fullCalendar('getDate').getTime());"
				+ "\n\t" + "}"
				+ "\n});\n"
				, null);
		writer.writeText("$('#" + escapedClientId + "').fullCalendar('fetchEvents');\n", null);
		writer.writeText("filterEvents($('#" + filterId + "').val(), '" + schedule.getId() + "');\n", null);
		writer.writeText("});\n\t", null);
		writer.endElement("script");
		writer.writeText("\n", null);
	}

	private String renderStringOption(String name, String value, String prefix) {
		return (null != value && !value.isEmpty()) ? prefix + name + ": '" + escape(value) + "'," : "";
	}

	private String escape(String value) {
		if (value == null || value.length() == 0)
			return "";

		StringBuffer sb = new StringBuffer(value.length());
		char[] c = value.toCharArray();

		for (int i = 0; i < c.length; i++) {
			switch (c[i]) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\'':
				sb.append("\\\'");
				break;
			default:
				if (Character.isWhitespace(c[i])) {
					sb.append(" ");
				} else {
					sb.append(c[i]);
				}
				break;
			}
		}
		return sb.toString();
	}

	private String renderFunctionOption(String name, String value, String prefix, String functionStart, String functionEnd, boolean render) {
		return (render && null != value && !value.isEmpty()) ? prefix + name + ": " + functionStart + value + functionEnd + "," : "";
	}

	private String renderOption(String name, Object value, String prefix) {
		return (null != value) ? prefix + name + ": " + value + "," : "";
	}

	private String renderDateOption(String name, Date value, String prefix) {
		// Schedule expects a UNIX timeStamp (in seconds).
		return (null != value) ? prefix + name + ": " + value.getTime() / 1000 + "," : "";
	}

	private String renderDate(Date date, String prefix) {

		if (null != date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			String dateStr = renderOption("year", cal.get(Calendar.YEAR), prefix);
			dateStr += renderOption("month", cal.get(Calendar.MONTH), prefix);
			dateStr += renderOption("date", cal.get(Calendar.DATE), prefix);
			return dateStr;
		}
		return "";
	}

	private String renderEvents(FacesContext context, HtmlSchedule schedule) {
		List<ScheduleEvent> events = schedule.getEvents();
		String str = "";

		if (null != events && !events.isEmpty()) {
			str = "\t" + "events: [";

			for (ScheduleEvent event : events) {
				str += "\n\t\t" + "{";
				str += renderStringOption("id", event.getId(), " ");
				str += renderStringOption("title", event.getTitle(), " ");
				str += renderOption("allDay", event.isAllDay(), " ");
				str += renderDateOption("start", event.getStart(), " ");
				str += renderDateOption("end", event.getEnd(), " ");
				str += renderOption("editable", !event.isReadonly() && !schedule.isReadonly(), " ");
				str += renderStringOption("className", event.getClassName(), " ");
				str += renderStringOption("color", event.getColor(), " ");
				str += renderStringOption("backgroundColor", event.getBackgroundColor(), " ");
				str += renderStringOption("borderColor", event.getBorderColor(), " ");
				str += renderStringOption("textColor", event.getTextColor(), " ");
				str = str.substring(0, str.length() - 1) + "},";
			}
			str = str.substring(0, str.length() - 1) + "\n\t" + "]";
		}
		return str;
	}

	@Override
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		// Do nothing
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

}
