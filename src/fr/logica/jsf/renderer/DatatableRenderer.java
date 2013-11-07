package fr.logica.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.html_basic.TableRenderer;

public class DatatableRenderer extends TableRenderer {

	private static final Attribute[] ATTRIBUTES =
			AttributeManager.getAttributes(AttributeManager.Key.DATATABLE);

	@Override
	protected void renderRowStart(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {
		TableMetaInfo info = getMetaInfo(context, table);
		writer.startElement("tr", table);
		if (info.rowClasses.length > 0) {
			writer.writeAttribute("class", info.getCurrentRowClass(), "rowClasses");
		}
		Boolean readOnly = (Boolean) table.getAttributes().get("readOnly");
		Object onRowClick = table.getAttributes().get("onRowClick");
		if (onRowClick != null && (null == readOnly || !readOnly)) {
			writer.writeAttribute("onclick", onRowClick, null);
		}
		writer.writeText("\n", table, null);
	}

	@Override
	protected void renderColumnGroups(FacesContext context, UIComponent table)
			throws IOException {
		// Render the beginning of the table
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("colgroup", null);
		TableMetaInfo info = getMetaInfo(context, table);
		boolean first = true;
		for (UIColumn col : info.columns) {
			writer.startElement("col", col);
			if (first) {
				writer.writeAttribute("class", "first", null);
				first = false;
			}
			writer.endElement("col");
		}
		writer.endElement("colgroup");
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		rendererParamsNotNull(context, component);

		if (!shouldEncode(component)) {
			return;
		}

		UIData data = (UIData) component;
		data.setRowIndex(-1);

		// Render the beginning of the table
		ResponseWriter writer = context.getResponseWriter();
		// Gets Datatable unique identifier
		String id = (String) component.getAttributes().get("id");
		id = id.substring(5); // ID looks like table_REAL_IDENTIFIER, we only need REAL_IDENTIFIER

		boolean ajaxRequest = "true".equals(context.getExternalContext().getRequestParameterMap().get("javax.faces.partial.ajax"));

		// When request is an ajax request, we refresh results only
		if (!ajaxRequest) {
			// We split datatable in two separate parts so headers won't scroll away
			writer.startElement("div", null);
			writer.writeAttribute("id", "datatable-div-header-" + id, null);
			writer.writeAttribute("class", "datatable-div-header", null);
			writer.writeAttribute("style", "visibility: hidden", null);
			writer.startElement("table", null);
			writer.writeAttribute("id", "datatable-table-header-" + id, null);
			writer.writeAttribute("class", "datatable-table-header", null);
			// Render the header facets (if any)
			renderColumnGroups(context, data);
			renderHeader(context, component, writer);
			writer.endElement("table");
			writer.endElement("div");

			writer.startElement("div", component);
			writer.writeAttribute("id", "datatable-div-data-" + id, null);
			writer.writeAttribute("class", "datatable-div-data", null);
			writer.writeAttribute("style", "visibility: hidden", null);
		}
		renderTableStart(context, component, writer, ATTRIBUTES);

		// Render the caption (if any)
		renderCaption(context, data, writer);

		// Render column groups (if any)
		renderColumnGroups(context, data);

		// Render header for column alignment
		renderHeader(context, component, writer);

		// Render the footer facets (if any)
		renderFooter(context, component, writer);

		if (ajaxRequest) {
			writer.startElement("script", null);
			writer.writeAttribute("type", "text/javascript", null);
			writer.write("$(document).ready(function() {");
			writer.write("$('td[class=\"first\"]').click(function(event) { event.stopPropagation(); });");
			writer.write("datatableAlignColumns('" + id + "', true);");
			writer.write("});");
			writer.endElement("script");
		}
		if (!ajaxRequest) {
			writer.endElement("div");
		}
	}
}
