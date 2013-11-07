package fr.logica.jsf.controller.fileUpload;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.TextRenderer;

import fr.logica.business.FileContainer;

/**
 * Faces renderer for <code>input type="file"</code> field.
 * 
 * @author BalusC
 */
@FacesRenderer(componentFamily = "javax.faces.Input", rendererType = "javax.faces.File")
public class FileRenderer extends TextRenderer {

	private static final String EMPTY_STRING = "";
	private static final Attribute[] INPUT_ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.INPUTTEXT);

	@Override
	protected void getEndTextToRender(FacesContext context, UIComponent component, String currentValue)
			throws IOException {

		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId(context);

		/* Span component to display selected file name. */
		writer.startElement("span", null);
		writer.writeAttribute("id", clientId + "-span", null);

		if (null != currentValue) {
			writer.writeText(currentValue, null);
		}
		writer.endElement("span");

		writer.startElement("input", component);
		writeIdAttributeIfNecessary(context, writer, component);
		writer.writeAttribute("type", "file", null);
		writer.writeAttribute("name", clientId, "clientId");

		String styleClass = (String) component.getAttributes().get("styleClass");
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}

		// Render standard HTMLattributes expect of styleClass.
		RenderKitUtils.renderPassThruAttributes(context, writer, component, INPUT_ATTRIBUTES,
				getNonOnChangeBehaviors(component));
		RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);
		RenderKitUtils.renderOnchange(context, component, false);
		writer.endElement("input");
	}

	@Override
	public void decode(FacesContext context, UIComponent component) {
		rendererParamsNotNull(context, component);
		if (!shouldDecode(component)) {
			return;
		}
		String clientId = decodeBehaviors(context, component);
		if (clientId == null) {
			clientId = component.getClientId(context);
		}

		if (context.getExternalContext().getRequest() instanceof MultipartRequest) {
			FileContainer file = ((MultipartRequest) context.getExternalContext().getRequest()).getFile(clientId);

			// If no file is specified, old value is used instead.
			((UIInput) component).setSubmittedValue((file != null) ? file : component.getAttributes().get("value"));
		} else {
			((UIInput) component).setSubmittedValue(component.getAttributes().get("value"));
		}
	}

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
			throws ConverterException {

		/* This component always returns a FileContainer object. */
		return (submittedValue != EMPTY_STRING) ? submittedValue : null;
	}

}
