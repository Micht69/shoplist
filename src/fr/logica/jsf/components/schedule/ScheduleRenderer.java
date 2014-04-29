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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import fr.logica.business.data.ScheduleEvent;
import fr.logica.jsf.components.schedule.HtmlSchedule.PropertyKeys;

@FacesRenderer(componentFamily = HtmlSchedule.COMPONENT_FAMILY, rendererType = ScheduleRenderer.RENDERER_TYPE)
public class ScheduleRenderer extends Renderer {

	private static final String EVENT_KEY = "event-";
	public static final String RENDERER_TYPE = "cgi.faces.Schedule";

	@Override
	@SuppressWarnings("unchecked")
	public void decode(FacesContext context, UIComponent component) {
		ExternalContext external = context.getExternalContext();
		HtmlSchedule schedule = (HtmlSchedule) component;
		String clientId = schedule.getClientId(context);
		Map<String, Object> scheduleParams = getParameters(clientId, external.getRequestParameterMap());
		schedule.setSubmittedView((String) scheduleParams.get(PropertyKeys.view.toString()));
		schedule.setSubmittedDate((String) scheduleParams.get(PropertyKeys.date.toString()));
		schedule.setSubmittedSelectedEvent((String) scheduleParams.get(PropertyKeys.selectedEvent.toString()));

		if (!schedule.isReadonly()) {
			List<ScheduleEvent> events = parseEvents(schedule, (Map<String, ScheduleEvent>) scheduleParams.get(PropertyKeys.value.toString()));
			schedule.setSubmittedValue(events);
		}
	}

	private List<ScheduleEvent> parseEvents(HtmlSchedule schedule, Map<String, ScheduleEvent> submittedEvents) {

		if (null == submittedEvents) {
			return null;
		}
		List<ScheduleEvent> existingEvents = (List<ScheduleEvent>) schedule.getValue();
		List<ScheduleEvent> events = new ArrayList<ScheduleEvent>(existingEvents.size());

		for (ScheduleEvent event : existingEvents) {
			ScheduleEvent submittedEvent = submittedEvents.get(event.getId());

			if (null != submittedEvent) {
				event.setStart(submittedEvent.getStart());
				event.setEnd(submittedEvent.getEnd());
			}
			events.add(event);
		}
		return events;
	}

	private Map<String, Object> getParameters(String clientId, Map<String, String> requestParams) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, ScheduleEvent> events = new HashMap<String, ScheduleEvent>();
		int idLength = clientId.length() + 1;

		for (Entry<String, String> entry : requestParams.entrySet()) {

			if (entry.getKey().startsWith(clientId)) {
				String key = entry.getKey().substring(idLength);

				if (key.startsWith(EVENT_KEY)) {
					ScheduleEvent event = new ScheduleEvent();
					int dashIndex = entry.getValue().indexOf('-');
					event.setStart(new Date(Long.valueOf(entry.getValue().substring(0, dashIndex))));
					event.setEnd(new Date(Long.valueOf(entry.getValue().substring(dashIndex + 1))));
					events.put(event.getId(), event);
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
		ResponseWriter writer = context.getResponseWriter();
		String clientId = schedule.getClientId(context);

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
		encodeInput(writer, schedule, clientId, PropertyKeys.date.toString(), (schedule.getDate() != null) ? schedule.getDate().getTime() : null);
		encodeInput(writer, schedule, clientId, PropertyKeys.selectedEvent.toString(), (schedule.getSelectedEvent() != null) ? schedule
				.getSelectedEvent().getId() : null);
		encodeInput(writer, schedule, clientId, PropertyKeys.view.toString(), schedule.getView());

		// End of container
		writer.endElement("div");
	}

	private void encodeInput(ResponseWriter writer, HtmlSchedule schedule, String clientId, String property, Object value) throws IOException {
		writer.writeText("\t", null);
		writer.startElement("input", null);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId + "-" + property, null);
		writer.writeAttribute("name", clientId + "-" + property, null);

		if (null != value) {
			writer.writeAttribute("value", value, null);
		}
		writer.endElement("input");
		writer.writeText("\n", null);
	}

	private void encodeScript(FacesContext context, HtmlSchedule schedule) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = schedule.getClientId(context);
		String escapedClientId = getEscapedClientId(clientId);
		String filterId = escapedClientId.replace("schedule", "globalFilter");

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
				+ renderStringOption(PropertyKeys.onchange.toString(), schedule.getOnchange(), "\n\t")
				+ renderOption(PropertyKeys.showWeekends.toString(), schedule.getShowWeekends(), "\n\t")
				+ renderOption(PropertyKeys.slotMinutes.toString(), schedule.getSlotMinutes(), "\n\t")
				+ renderDate(schedule.getDate(), "\n\t")
				// TODO bazint schedule Gérer la locale.
				+ "\n" + renderEvents(context, schedule)
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
				case '"' :
					sb.append("\\\"");
					break;
				case '\\' :
					sb.append("\\\\");
					break;
				case '\'' :
					sb.append("\\\'");
					break;
				default :
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
		List<ScheduleEvent> events = (List<ScheduleEvent>) schedule.getValue();
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
			str = str.substring(0, str.length() - 1) + "\n\t" + "],";
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
