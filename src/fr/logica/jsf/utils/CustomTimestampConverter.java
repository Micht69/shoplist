package fr.logica.jsf.utils;

import java.sql.Timestamp;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;

import com.sun.faces.util.MessageFactory;

import fr.logica.business.Constants;

public class CustomTimestampConverter extends DateTimeConverter {

	/**
	 * Constructeur de DateTimeConverter personnalisé.
	 */
	public CustomTimestampConverter() {
		super();
		this.setPattern(Constants.FORMAT_DATE + " " + Constants.FORMAT_HOUR);
	}

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		Object obj = null;
		try {
			obj = super.getAsObject(facesContext, component, value);
		} catch (ConverterException e) {
			Object label = getAttribute(facesContext, component, "label");
			throw new ConverterException(MessageFactory.getMessage("javax.faces.converter.DateTimestampConverter.DATE", label), e);
		}
		if (obj != null && obj instanceof Date) {
			obj = new Timestamp(((Date) obj).getTime());
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
