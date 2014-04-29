package fr.logica.jsf.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.ResponseWriter;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.html_basic.TableRenderer;

public class DatatableRenderer extends TableRenderer {

	private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.DATATABLE);

	@Override
	protected void renderRowStart(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

		if (isVanilla(table)) {
			super.renderRowStart(context, table, writer);

		} else {
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
	}

	@Override
	protected void renderColumnGroups(FacesContext context, UIComponent table) throws IOException {

		if (isVanilla(table)) {
			super.renderColumnGroups(context, table);

		} else {
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
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

		if (isVanilla(component)) {
			super.encodeBegin(context, component);

		} else if (isTabEditAjaxRequest(context, component)) {
			/*
			 * Sole the tbody must be updated while validating a creation into an editable list. To do so, current update tag (for the datatable)
			 * is closed and a new update tag is opened for the tbody. The created update tag will be closed by JSF (instead of closing the one
			 * for the datatable).
			 */
			PartialResponseWriter writer = context.getPartialViewContext().getPartialResponseWriter();
			writer.endUpdate();
			writer.startUpdate(component.getClientId() + "-body");

		} else {
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
			renderTableStart(context, component, writer, ATTRIBUTES);

			// Render the caption (if any)
			renderCaption(context, data, writer);

			// Render column groups (if any)
			renderColumnGroups(context, data);

			// Render header for column alignment
			renderHeader(context, component, writer);

			// Render the footer facets (if any)
			renderFooter(context, component, writer);
		}
	}

	@Override
	protected void renderTableBodyStart(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

		if (isVanilla(table)) {
			super.renderTableBodyStart(context, table, writer);

		} else {
			writer.startElement("tbody", table);
			writer.writeAttribute("id", table.getClientId() + "-body", null);
			writer.writeText("\n", table, null);
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

		if (isVanilla(component)) {
			super.encodeEnd(context, component);

		} else if (!isTabEditAjaxRequest(context, component)) {
			super.encodeEnd(context, component);
			ResponseWriter writer = context.getResponseWriter();
			writer.endElement("div");
		}
	}

	/**
	 * Indicates whether the component is a Vanilla table or not.
	 * <p>
	 * Vanilla table uses the parent renderer.
	 * </p>
	 * 
	 * @param component
	 *            Component to test.
	 * @return {@code true} if the component contains the attribute {@code vanilla} with value {@code true}; {@code false} otherwise.
	 */
	private boolean isVanilla(UIComponent component) {
		return "true".equals(component.getAttributes().get("vanilla"));
	}

	/**
	 * Indicates whether the current request concerns an editable list row creation.
	 * 
	 * @param context
	 *            Current context.
	 * @param component
	 *            Component to test.
	 * @return {@code true} if the request contains the parameter {@code javax.faces.partial.ajax} and the ({@code javax.faces.source} of the
	 *         request concerns the component; {@code false} otherwise.
	 */
	private boolean isTabEditAjaxRequest(FacesContext context, UIComponent component) {
		Map<String, String> requestParameters = context.getExternalContext().getRequestParameterMap();
		return (requestParameters.get("javax.faces.partial.ajax") != null
		&& requestParameters.get("javax.faces.source").endsWith("tabedit-hidden-ajax-button-" + component.getId()));
	}

}
