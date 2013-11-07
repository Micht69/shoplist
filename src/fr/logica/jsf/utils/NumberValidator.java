package fr.logica.jsf.utils;

import java.math.BigDecimal;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.sun.faces.util.MessageUtils;

public class NumberValidator implements Validator {

	private static final String FORMAT_INVALID_MESSAGE_ID = "validator.number";

	public NumberValidator() {
		super();
	}

	@Override
	public void validate(FacesContext context, UIComponent component, Object toValidate) {
		if (toValidate == null) {
			return;
		}

		// Récupération des attributs
		String label = (String) component.getAttributes().get("label");
		if (label == null) {
			label = component.getId();
		}
		int precision = 0;
		int scale = 0;
		try {
			precision = Integer.parseInt((String) component.getAttributes().get("precision"));
		} catch (NumberFormatException nfe) {
			// raf
		}
		try {
			scale = Integer.parseInt((String) component.getAttributes().get("scale"));
		} catch (NumberFormatException nfe) {
			// raf
		}
		int nbInteger = precision - scale;
		int nbFraction = scale;

		if (!(toValidate instanceof BigDecimal)) {
			FacesMessage errMsg = MessageUtils.getExceptionMessage(FORMAT_INVALID_MESSAGE_ID, (new Object[] { label, nbInteger, nbFraction }));
			throw new ValidatorException(errMsg);
		}

		// Test
		String value = toValidate.toString();
		if ("".equals(value)) {
			return;
		}
		String int0 = null;
		String int1 = null;
		int idx = value.indexOf('.');
		if (idx > -1) {
			// Décimal
			int0 = value.substring(0, idx);
			int1 = value.substring(idx + 1);
		} else {
			// Entier
			int0 = value;
		}

		if (int0.length() > nbInteger || (int1 != null && int1.length() > nbFraction)) {
			FacesMessage errMsg = MessageUtils.getExceptionMessage(FORMAT_INVALID_MESSAGE_ID, (new Object[] { label, nbInteger, nbFraction }));
			throw new ValidatorException(errMsg);
		}
	}

}
