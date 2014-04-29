package fr.logica.jsf.components.autocomplete;

import static fr.logica.jsf.components.RendererUtils.getEscapedClientId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.MethodNotFoundException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.html_basic.TextRenderer;

import fr.logica.business.MessageUtils;
import fr.logica.jsf.components.autocomplete.AutocompleteSuggestion;
import fr.logica.jsf.model.link.LinkQuickSearchModel;

@FacesRenderer(componentFamily = UIAutocomplete.COMPONENT_FAMILY, rendererType = HtmlAutocompleteRenderer.RENDERER_TYPE)
public class HtmlAutocompleteRenderer extends TextRenderer {

	public static final String RENDERER_TYPE = "logica.faces.AutocompleteRenderer";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		super.decode(context, component);
		Map<String, String> requestParameters = context.getExternalContext().getRequestParameterMap();

		if (requestParameters.get("javax.faces.partial.ajax") != null) {
			PartialViewContext pvc = context.getPartialViewContext();
			pvc.getRenderIds().add(component.getClientId(context));
			context.renderResponse();
		}
	}

	@Override
	protected void getEndTextToRender(FacesContext context, UIComponent component, String value) throws IOException {
		Map<String, String> requestParameters = context.getExternalContext().getRequestParameterMap();
		String clientId = component.getClientId(context);
		UIAutocomplete comp = (UIAutocomplete) component;

		if (requestParameters.get("javax.faces.partial.ajax") != null) {
			ResponseWriter writer = context.getResponseWriter();
			String criteria = new String(requestParameters.get(clientId).getBytes(), "UTF-8");
			List<AutocompleteSuggestion> suggestions = getItems(context, comp, criteria);

			writer.write("[");

			if (null != suggestions) {
				Iterator<AutocompleteSuggestion> iter = suggestions.iterator();

				while (iter.hasNext()) {
					AutocompleteSuggestion suggestion = iter.next();
					writer.write("{\"label\" : \"");
					writer.write(suggestion.getLabel());
					writer.write("\", \"value\" : \"");
					writer.write(suggestion.getValue());
					writer.write("\"}");

					if (iter.hasNext()) {
						writer.write(", ");
					}
				}
			}
			writer.write("]");

		} else {
			super.getEndTextToRender(context, component, value);
			insertScript(clientId, context.getResponseWriter(), comp);
		}
	}

	private void insertScript(String clientId, ResponseWriter writer, UIAutocomplete component) throws IOException {
		String escapedClientId = getEscapedClientId(clientId);

		writer.startElement("script", component);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeText("$(document).ready(function() {"
				+ "$(\"#" + escapedClientId + "\").autocomplete({"
				+ "\ndelay: 500,"
				+ "\nminLength: " + getMinLength(component) + ","
				+ "\nfocus: function(event, ui) { return false; },"
				+ "\nopen: function(event, ui) {"
				+ "\nvar autocompleteUL = $(event.target).autocomplete('widget');"
				+ "\nautocompleteUL.width('auto');"
				+ "\nautocompleteUL.css('display', 'inline-block');"
				+ "\n},"
				+ "\nsource: function(request, response) {"
				+ "\n$.ajax({"
				+ "\nurl: $('#mainForm').attr('action'),"
				+ "\ndata: {'" + clientId + "': request.term,"
				+ "\n'javax.faces.partial.ajax': true,"
				+ "\n'javax.faces.partial.event': 'keypress',"
				+ "\n'javax.faces.partial.execute': '" + clientId + " @component',"
				+ "\n'javax.faces.partial.render': '@component',"
				+ "\n'javax.faces.source': '" + clientId + "',"
				+ "\n'javax.faces.ViewState': document.getElementById('javax.faces.ViewState').value"
				+ "\n},"
				+ "\nheaders: {'Faces-Request': 'partial/ajax'},"
				+ "\ndataType: 'xml',"
				+ "\nerror: function(xhr, status, error) {"
				+ "\nresponse({});"
				+ "\n},"
				+ "\nsuccess: function(data, status, xhr) {"
				+ "\nvar xmlDoc = $(data);"
				+ "\nvar updates = xmlDoc.find(\"update\")"
				+ "\nvar viewState = updates.last();"
				+ "\ndocument.getElementById('javax.faces.ViewState').value = viewState.text();"
				+ "\nresponse(updates.first());"
				+ "\n}"
				+ "\n});"
				+ "\n}"
				+ "\n});"
				+ "\n});", null);
		writer.endElement("script");
	}

	private Integer getMinLength(UIAutocomplete component) {
		Integer minLength = component.getMinLength();
		return (null != minLength && minLength > -1) ? minLength : 2;
	}

	@SuppressWarnings("unchecked")
	private List<AutocompleteSuggestion> getItems(FacesContext facesContext, UIAutocomplete component, String value) {
		List<AutocompleteSuggestion> result = null;
		LinkQuickSearchModel model = component.getModel();

		if (null != model) {

			try {
				result = model.quickSearch(value);
			} catch (MethodNotFoundException mnfe) {
				result = new ArrayList<AutocompleteSuggestion>();
				result.add(new AutocompleteSuggestion(MessageUtils.getInstance().getMessage("autocomplete.error", null), "-1"));

			} catch (ELException ele) {
				result = new ArrayList<AutocompleteSuggestion>();
				result.add(new AutocompleteSuggestion(MessageUtils.getInstance().getMessage("autocomplete.error", null), "-1"));
			}
		}
		return result;
	}

}
