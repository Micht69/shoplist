package fr.logica.jsf.components.map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.sun.faces.util.MessageFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@FacesConverter(value = GeometryConverter.CONVERTER_ID)
public class GeometryConverter implements Converter {

	public static final String CONVERTER_ID = "cgi.faces.Geometry";
	private static final String GEOMETRY_ID = "cgi.faces.converter.GeometryConverter.GEOMETRY";
	private static final String STRING_ID = "javax.faces.converter.STRING";

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		if (null != value) {

			try {
				// format should be Well Known Text representation (http://portal.opengeospatial.org/files/?artifact_id=25355 p52).
				return new WKTReader().read(value);

			} catch (ParseException exception) {
				throw new ConverterException(
						MessageFactory.getMessage(context, GEOMETRY_ID, value, MessageFactory.getLabel(context, component)), exception);
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		if (null != value) {

			if (!(value instanceof Geometry)) {
				throw new ConverterException(
						MessageFactory.getMessage(context, STRING_ID, value, MessageFactory.getLabel(context, component)));
			}
			return ((Geometry) value).toText();
		}
		return null;
	}

}
