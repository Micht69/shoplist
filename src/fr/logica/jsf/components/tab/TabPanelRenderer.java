package fr.logica.jsf.components.tab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.html_basic.HtmlBasicInputRenderer;

@FacesRenderer(componentFamily = HtmlTabPanel.COMPONENT_FAMILY, rendererType = TabPanelRenderer.RENDERER_TYPE)
public class TabPanelRenderer extends HtmlBasicInputRenderer {

	public static final String RENDERER_TYPE = "cgi.faces.TabPanel";

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		HtmlTabPanel tabPanel = (HtmlTabPanel) component;
		ResponseWriter writer = context.getResponseWriter();

		writer.startElement("div", component);
		writer.writeAttribute("id", tabPanel.getId() + "-tabs", null);

		if (null != tabPanel.getStyle()) {
			writer.writeAttribute("style", tabPanel.getStyle(), null);
		}

		if (null != tabPanel.getStyleClass()) {
			writer.writeAttribute("class", tabPanel.getStyleClass(), null);
		}
		writer.writeText("\n", null);
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		HtmlTabPanel tabPanel = (HtmlTabPanel) component;
		ResponseWriter writer = context.getResponseWriter();
		String clientId = tabPanel.getClientId(context);

		writer.startElement("input", null);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("name", clientId, null);

		if (null != tabPanel.getValue()) {
			writer.writeAttribute("value", tabPanel.getValue(), null);
		}
		writer.endElement("input");
		writer.writeText("\n", null);

		writer.endElement("div");
	}

	@Override
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		HtmlTabPanel tabPanel = (HtmlTabPanel) component;
		ResponseWriter writer = context.getResponseWriter();
		List<HtmlTab> children = new ArrayList<HtmlTab>(tabPanel.getChildCount());

		writer.startElement("ul", null);

		for (UIComponent child : tabPanel.getChildren()) {
			if (child instanceof HtmlTab && child.isRendered()) {
				HtmlTab tab = (HtmlTab) child;
				children.add(tab);

				writer.startElement("li", null);
				writer.startElement("a", null);
				writer.writeAttribute("href", "#tabs-" + tab.getId(), null);
				writer.writeText(tab.getTitle(), null);
				writer.endElement("a");
				writer.writeText("\n", null);
				writer.endElement("li");
				writer.writeText("\n", null);
			}
		}
		writer.endElement("ul");
		writer.writeText("\n", null);

		for (HtmlTab tab : children) {
			writer.startElement("div", tab);
			writer.writeAttribute("id", "tabs-" + tab.getId(), null);
			// Title
			writer.startElement("div", tab);
			writer.writeAttribute("class", "lineTitle tab_title", null);
			writer.writeText(tab.getTitle(), null);
			writer.endElement("div");
			// Content
			tab.encodeChildren(context);
			writer.endElement("div");
			writer.writeText("\n", null);
		}
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

}
