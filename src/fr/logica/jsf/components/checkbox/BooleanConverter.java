package fr.logica.jsf.components.checkbox;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.sun.faces.util.MessageFactory;

@FacesConverter(value = BooleanConverter.CONVERTER_ID)
public class BooleanConverter implements Converter {

	public static final String CONVERTER_ID = "cgi.faces.Boolean";
	public static final String BOOLEAN_ID = "javax.faces.converter.BooleanConverter.BOOLEAN";
	public static final String STRING_ID = "javax.faces.converter.STRING";

	private Object checkedValue;
	private Object uncheckedValue;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (context == null || component == null) {
			throw new NullPointerException();
		}

		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		try {
			Object result;

			if (Boolean.valueOf(value)) {
				if (null != checkedValue) {
					result = checkedValue;
				} else {
					result = Boolean.TRUE;
				}
			} else {
				if (null != uncheckedValue) {
					result = uncheckedValue;
				} else {
					result = Boolean.FALSE;
				}
			}
			return result;

		} catch (Exception e) {
			throw new ConverterException(MessageFactory.getMessage(context, BOOLEAN_ID, value, MessageFactory.getLabel(context, component)), e);
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (context == null || component == null) {
			throw new NullPointerException();
		}

		if (value == null) {
			return "";
		}

		try {
			String result;

			if (null != checkedValue) {
				result = Boolean.toString(checkedValue.equals(value));
			} else {
				result = Boolean.toString(Boolean.TRUE.equals(value));
			}
			return result;

		} catch (Exception e) {
			throw new ConverterException(MessageFactory.getMessage(context, STRING_ID, value, MessageFactory.getLabel(context, component)), e);
		}
	}

	public Object getCheckedValue() {
		return checkedValue;
	}

	public void setCheckedValue(Object checkedValue) {
		this.checkedValue = checkedValue;
	}

	public Object getUncheckedValue() {
		return uncheckedValue;
	}

	public void setUncheckedValue(Object uncheckedValue) {
		this.uncheckedValue = uncheckedValue;
	}

}
