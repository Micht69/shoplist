package fr.logica.jsf.components.wizard;

import static fr.logica.jsf.components.RendererUtils.getEscapedClientId;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.html_basic.HtmlBasicInputRenderer;

import fr.logica.jsf.components.tab.HtmlTab;

@FacesRenderer(componentFamily = HtmlWizard.COMPONENT_FAMILY, rendererType = WizardRenderer.RENDERER_TYPE)
public class WizardRenderer extends HtmlBasicInputRenderer {

	public static final String RENDERER_TYPE = "cgi.faces.Wizard";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		HtmlWizard wizard = (HtmlWizard) component;

		if (wizard.isWizardRequest(context)) {
			Map<String, String> params = context.getExternalContext().getRequestParameterMap();
			String clientId = wizard.getClientId(context);
			String stepToGo = params.get(clientId + "_nextStepRequest");
			String currentStep = (String) wizard.getValue();

			WizardEvent event = new WizardEvent(wizard, currentStep, stepToGo);
			event.setPhaseId(PhaseId.INVOKE_APPLICATION);
			wizard.queueEvent(event);

		} else {
			super.decode(context, component);
		}
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		HtmlWizard wizard = (HtmlWizard) component;

		if (!wizard.isWizardRequest(context)) {
			ResponseWriter writer = context.getResponseWriter();
			String clientId = wizard.getClientId(context);

			// Container
			writer.writeText("\n", null);
			writer.startElement("div", component);
			writer.writeAttribute("id", clientId + "-container", null);
			writer.writeText("\n", null);

			// Wizard div
			writer.startElement("div", component);
			writer.writeAttribute("id", clientId + "-wizard", null);

			if (null != wizard.getStyle()) {
				writer.writeAttribute("style", wizard.getStyle(), null);
			}

			if (null != wizard.getStyleClass()) {
				writer.writeAttribute("class", wizard.getStyleClass(), null);
			}
			writer.writeText("\n", null);
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		HtmlWizard wizard = (HtmlWizard) component;

		if (!wizard.isWizardRequest(context)) {
			ResponseWriter writer = context.getResponseWriter();
			String clientId = wizard.getClientId(context);

			writer.endElement("div");
			writer.writeText("\n", null);

			writer.startElement("input", null);
			writer.writeAttribute("type", "hidden", null);
			writer.writeAttribute("id", clientId, null);
			writer.writeAttribute("name", clientId, null);

			if (null != wizard.getValue()) {
				writer.writeAttribute("value", wizard.getValue(), null);
			}
			writer.endElement("input");
			writer.writeText("\n", null);

			encodeScript(context, wizard);

			writer.endElement("div");
		}
	}

	@Override
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		HtmlWizard wizard = (HtmlWizard) component;

		if (wizard.isWizardRequest(context)) {
			int i = 0;
			String stepId = (String) wizard.getValue();
			Iterator<UIComponent> iter = wizard.getChildren().iterator();
			UIComponent stepToRender = null;

			while (iter.hasNext() && null == stepToRender) {
				UIComponent step = iter.next();
				i++;

				if (stepId.equals(step.getId())) {
					stepToRender = step;
				}
				encodeStep(context, wizard, stepToRender, i, true);
			}

		} else {
			int i = 1;
			String stepId = (String) wizard.getValue();
			boolean renderChildren = true;

			for (UIComponent child : wizard.getChildren()) {
				renderChildren = ((null == stepId && i == 1) || (null != stepId && stepId.equals(child.getId())));
				i = encodeStep(context, wizard, child, i, renderChildren);
			}
		}
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	private int encodeStep(FacesContext context, HtmlWizard wizard, UIComponent child, int index, boolean renderChildren) throws IOException {

		if (child instanceof HtmlTab && child.isRendered()) {
			HtmlTab tab = (HtmlTab) child;
			ResponseWriter writer = context.getResponseWriter();
			writer.startElement("div", tab);
			writer.writeAttribute("id", tab.getClientId(context), null);

			if (null != tab.getTitle() && !tab.getTitle().isEmpty()) {
				writer.writeAttribute("data-jwizard-title", tab.getTitle(), null);
			} else {
				writer.writeAttribute("data-jwizard-title", "Page #" + index, null);
			}

			if (null != tab.getStyle()) {
				writer.writeAttribute("style", tab.getStyle(), null);
			}

			if (null != tab.getStyleClass()) {
				writer.writeAttribute("class", tab.getStyleClass(), null);
			}

			if (renderChildren) {
				tab.encodeChildren(context);
			}
			writer.endElement("div");
			writer.writeText("\n", null);
			return ++index;
		}
		return index;
	}

	private void encodeScript(FacesContext context, HtmlWizard wizard) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = wizard.getClientId(context);
		String escapedClientId = getEscapedClientId(clientId);
		writer.startElement("script", null);
		writer.writeAttribute("type", "text/javascript", null);
		String script = "";
		script += "\n" + "$(document).ready(function() {";
		script += "\n\t" + "var wiz = $('#" + escapedClientId + "-wizard');";
		script += "\n\t" + "wiz.jWizard(";

		if (null != wizard.getValue()) {
			script += "{initialStep: wiz.find('div[id$=\"" + wizard.getValue() + "\"]')}";
		}
		script += ")";
		script += "\n\t" + ".bind(\"stepshown\", function(event, ui) {";
		script += "\n\t\t" + "$('#" + escapedClientId + "').val($(event.target).attr('id').replace('mainForm:', ''));";
		script += "\n\t" + "});";
		script += "\n" + "});\n";
		writer.writeText(script, null);
		writer.endElement("script");
		writer.writeText("\n", null);
	}

}
