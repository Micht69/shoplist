package fr.logica.jsf.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;

import com.sun.faces.util.MessageFactory;

import fr.logica.jsf.components.calendar.Calendar;

public class CustomDateTimeConverter extends DateTimeConverter {

	public CustomDateTimeConverter() {
		super();
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		if (value != null && !value.isEmpty()) {
			if (component instanceof Calendar) {
				Calendar calendar = (Calendar) component;
				String pattern = calendar.getPattern().replace('l', 'S');
				DateFormat format = new SimpleDateFormat(pattern);
				try {
					return format.parse(value);
				} catch (ParseException pe) {
					String precision = calendar.getPrecision();
					String key;

					if ("date".equals(precision)) {
						key = "javax.faces.converter.DateTimeConverter.DATE";

					} else if ("datetime".equals(precision)) {
						key = "javax.faces.converter.DateTimeConverter.DATETIME";

					} else if ("timestamp".equals(precision)) {
						key = "javax.faces.converter.DateTimeConverter.TIMESTAMP";

					} else {
						key = "javax.faces.converter.DateTimeConverter.TIME";
					}
					throw new ConverterException(MessageFactory.getMessage(key, calendar.getLabel(), format.format(new Date())), pe);
				}
			}
		}
		return super.getAsObject(context, component, value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		if (value instanceof Date && component instanceof Calendar) {
			Calendar calendar = (Calendar) component;
			String pattern = calendar.getPattern().replace('l', 'S');
			DateFormat format = new SimpleDateFormat(pattern);
			return format.format((Date) value);
		}
		return super.getAsString(context, component, value);
	}

}
