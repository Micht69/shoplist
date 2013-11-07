package fr.logica.jsf.utils;

import java.sql.Time;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;

import com.sun.faces.util.MessageFactory;

import fr.logica.business.Constants;

public class CustomTimeConverter extends DateTimeConverter {

	/**
	 * Constructeur de DateTimeConverter personnalisé.
	 */
	public CustomTimeConverter() {
		super();
		this.setPattern(Constants.FORMAT_TIME);
	}

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		Object obj = null;
		try {
			obj = super.getAsObject(facesContext, component, value);
		} catch (ConverterException e) {
			Object label = getAttribute(facesContext, component, "label");
			throw new ConverterException(MessageFactory.getMessage("javax.faces.converter.TimeConverter.TIME", label),
					e);
		}
		if (obj != null && obj instanceof Date) {
			obj = new Time(((Date) obj).getTime());
		}
		return obj;
	}

	Object getAttribute(FacesContext c, UIComponent component, String name) {
		Object result = component.getAttributes().get(name);
		if (result == null && component.getParent() != null) {
			result = getAttribute(c, component.getParent(), name);
		}
		return result;
	}
}
