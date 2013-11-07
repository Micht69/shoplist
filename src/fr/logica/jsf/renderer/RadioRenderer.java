package fr.logica.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * Renderer for UiSelectOneRadio.
 * <p>
 * This class overrides the method {@code renderOption} to select the {@code noSelection} option only if the current value is {code null}.
 * </p>
 */
public class RadioRenderer extends com.sun.faces.renderkit.html_basic.RadioRenderer {

	private static final Attribute[] ATTRIBUTES =
			AttributeManager.getAttributes(AttributeManager.Key.SELECTONERADIO);

	@Override
	protected void renderOption(FacesContext context, UIComponent component, Converter converter, SelectItem curItem, Object currentSelections,
			Object[] submittedValues, boolean alignVertical,
			int itemNumber, OptionComponentInfo optionInfo) throws IOException {

		/*
		 * For all options except the "noSelection" one, the standard behavior is executed.
		 */
		if (!curItem.isNoSelectionOption()) {
			super.renderOption(context, component, converter, curItem, currentSelections, submittedValues, alignVertical, itemNumber, optionInfo);
			return;
		}

		ResponseWriter writer = context.getResponseWriter();
		assert (writer != null);

		UISelectOne selectOne = (UISelectOne) component;
		Object curValue = selectOne.getSubmittedValue();

		if (curValue == null) {
			curValue = selectOne.getValue();
		}

		/* The fix is here : the option is checked only if the current value is null. */
		boolean checked = null == curValue;

		/* Copy/Paste from the parent class. */
		if (alignVertical) {
			writer.writeText("\t", component, null);
			writer.startElement("tr", component);
			writer.writeText("\n", component, null);
		}

		String labelClass;
		if (optionInfo.isDisabled() || curItem.isDisabled()) {
			labelClass = optionInfo.getDisabledClass();
		} else {
			labelClass = optionInfo.getEnabledClass();
		}
		writer.startElement("td", component);
		writer.writeText("\n", component, null);

		writer.startElement("input", component);
		writer.writeAttribute("type", "radio", "type");

		if (checked) {
			writer.writeAttribute("checked", Boolean.TRUE, null);
		}
		writer.writeAttribute("name", component.getClientId(context),
				"clientId");
		String idString = component.getClientId(context)
				+ UINamingContainer.getSeparatorChar(context)
				+ Integer.toString(itemNumber);
		writer.writeAttribute("id", idString, "id");

		writer.writeAttribute("value",
				(getFormattedValue(context, component,
						curItem.getValue(), converter)),
				"value");

		// Don't render the disabled attribute twice if the 'parent'
		// component is already marked disabled.
		if (!optionInfo.isDisabled()) {
			if (curItem.isDisabled()) {
				writer.writeAttribute("disabled", true, "disabled");
			}
		}
		// Apply HTML 4.x attributes specified on UISelectMany component to all
		// items in the list except styleClass and style which are rendered as
		// attributes of outer most table.
		RenderKitUtils.renderPassThruAttributes(context,
				writer,
				component,
				ATTRIBUTES,
				getNonOnClickSelectBehaviors(component));
		RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer,
				component);

		RenderKitUtils.renderSelectOnclick(context, component, false);

		writer.endElement("input");
		writer.startElement("label", component);
		writer.writeAttribute("for", idString, "for");
		// if enabledClass or disabledClass attributes are specified, apply
		// it on the label.
		if (labelClass != null) {
			writer.writeAttribute("class", labelClass, "labelClass");
		}
		String itemLabel = curItem.getLabel();
		if (itemLabel != null) {
			writer.writeText(" ", component, null);
			if (!curItem.isEscape()) {
				// It seems the ResponseWriter API should
				// have a writeText() with a boolean property
				// to determine if it content written should
				// be escaped or not.
				writer.write(itemLabel);
			} else {
				writer.writeText(itemLabel, component, "label");
			}
		}
		writer.endElement("label");
		writer.endElement("td");
		writer.writeText("\n", component, null);
		if (alignVertical) {
			writer.writeText("\t", component, null);
			writer.endElement("tr");
			writer.writeText("\n", component, null);
		}
	}

}
