package fr.logica.jsf.utils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ToLowerCaseConverter implements Converter {

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return (String) value;
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return (value != null) ? value.toLowerCase() : null;
	}

}
