package fr.logica.jsf.utils;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.sun.faces.util.MessageUtils;

public class UrlPatternValidator implements Validator {

	private static final String FORMAT_INVALID_MESSAGE_ID = "validator.url";

	public UrlPatternValidator() {
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

		// Test
		String value = toValidate.toString();
		if ("".equals(value)) {
			return;
		}

		Pattern urlPattern = Pattern.compile("^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\.[a-z\\.]{2,6}(:[\\d]*)?/?$", Pattern.CASE_INSENSITIVE);

		if (!urlPattern.matcher(value).find()) {
			FacesMessage errMsg = MessageUtils.getExceptionMessage(FORMAT_INVALID_MESSAGE_ID, (new Object[] { label }));
			throw new ValidatorException(errMsg);
		}
	}

}
