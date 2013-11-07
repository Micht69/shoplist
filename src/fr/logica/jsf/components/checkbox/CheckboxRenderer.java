package fr.logica.jsf.components.checkbox;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;

@FacesRenderer(componentFamily = HtmlSelectStringCheckbox.COMPONENT_FAMILY, rendererType = HtmlSelectStringCheckbox.RENDERER_TYPE)
public class CheckboxRenderer extends com.sun.faces.renderkit.html_basic.CheckboxRenderer {

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {

		if (submittedValue instanceof Boolean) {
			Converter converter = ((ValueHolder) component).getConverter();

			if (null != converter) {
				return converter.getAsObject(context, component, submittedValue.toString());
			}
		}
		return super.getConvertedValue(context, component, submittedValue);
	}

}
