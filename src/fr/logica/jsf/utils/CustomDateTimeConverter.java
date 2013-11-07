package fr.logica.jsf.utils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;

import com.sun.faces.util.MessageFactory;

import fr.logica.business.Constants;

public class CustomDateTimeConverter extends DateTimeConverter {

	/** Siècle par défaut. */
	private static final String DEFAULT_CENTURY = "20";

	/** Longueur des dates dont l'année est sur deux chiffres. */
	private static final int SHORT_LENGTH = 8;

	/** Index du premier caractère de l'année. */
	private static final int YEAR_BEGIN = 6;

	/**
	 * Constructeur de DateTimeConverter personnalisé.
	 */
	public CustomDateTimeConverter() {
		super();
		this.setPattern(Constants.FORMAT_DATE);
	}

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		String valeur = value;
		// try {
		// ESAPI.validator().getValidDate("DateTime", valeur, new SimpleDateFormat("dd/MM/yyyy"), true);
		// } catch (ValidationException e) {
		// throw new ValidationException("toto", null);
		// }
		if (value != null && value.length() == SHORT_LENGTH) {
			StringBuffer buffer = new StringBuffer(value.substring(0, YEAR_BEGIN));
			String year = DEFAULT_CENTURY.concat(value.substring(YEAR_BEGIN));
			buffer.append(year);
			valeur = buffer.toString();
		}
		Object res = null;
		try {
			res = super.getAsObject(facesContext, component, valeur);
		} catch (ConverterException e) {
			Object label = getAttribute(facesContext, component, "label");
			throw new ConverterException(MessageFactory.getMessage("javax.faces.converter.DateTimeConverter.DATE",
					label), e);
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
