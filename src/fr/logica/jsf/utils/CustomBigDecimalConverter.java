package fr.logica.jsf.utils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.ConverterException;

import com.sun.faces.util.MessageFactory;

public class CustomBigDecimalConverter extends BigDecimalConverter {

	public CustomBigDecimalConverter() {

	}

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		String valeur = value;
		Object res = null;
		try {
			res = super.getAsObject(facesContext, component, valeur);
		} catch (ConverterException e) {
			Object label = getAttribute(facesContext, component, "label");
			throw new ConverterException(MessageFactory.getMessage("javax.faces.converter.BigDecimalConverter.DECIMAL",
					label, "198.23"), e);
		}
		return res;
	}

	Object getAttribute(FacesContext c, UIComponent component, String name) {
		Object result = component.getAttributes().get(name);
		if (result == null && component.getParent() != null) {
			result = getAttribute(c, component.getParent(), name);
		}
		return result;
	}

}
