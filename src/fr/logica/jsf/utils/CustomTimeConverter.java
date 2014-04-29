package fr.logica.jsf.utils;

import java.sql.Time;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class CustomTimeConverter extends CustomDateTimeConverter {

	public CustomTimeConverter() {
        super();
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Date result = (Date) super.getAsObject(context, component, value);

		if (null != result) {
			return new Time(result.getTime());
		}
		return result;
	}

}
