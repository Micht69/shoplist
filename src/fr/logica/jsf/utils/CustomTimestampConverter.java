package fr.logica.jsf.utils;

import java.sql.Timestamp;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class CustomTimestampConverter extends CustomDateTimeConverter {

	public CustomTimestampConverter() {
		super();
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Date result = (Date) super.getAsObject(context, component, value);

		if (null != result) {
			return new Timestamp(result.getTime());
		}
		return result;
	}

}
