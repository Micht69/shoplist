package fr.logica.jsf.utils;

import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;

public class CriteriaVisitor implements VisitCallback {

	private final StringBuilder criterias;

	public CriteriaVisitor() {
		criterias = new StringBuilder();
	}

	@Override
	public VisitResult visit(VisitContext ctx, UIComponent component) {

		if (!component.isRendered()) {
			return VisitResult.REJECT;
		}

		if (component instanceof UIInput) {
			UIInput input = (UIInput) component;

			if (null != input.getValue()) {

				if (input instanceof HtmlInputText) {
					criterias.append(((HtmlInputText) input).getLabel()).append(" : ");
					criterias.append(getValue(ctx, input)).append(", ");

				} else if (input instanceof HtmlSelectOneMenu) {
					criterias.append(((HtmlSelectOneMenu) input).getLabel()).append(" : ");
					criterias.append(getSelectedItem(input, input.getValue())).append(", ");

				} else if (input instanceof HtmlSelectOneRadio) {
					criterias.append(((HtmlSelectOneRadio) input).getLabel()).append(" : ");
					criterias.append(getSelectedItem(input, input.getValue())).append(", ");
				}
			}
			return VisitResult.REJECT;

		} else if (component instanceof HtmlOutputText) {
			HtmlOutputText text = (HtmlOutputText) component;

			if (null != text.getTitle() && null != text.getValue()) {
				criterias.append(text.getTitle()).append(" : ");
				criterias.append(text.getValue()).append(", ");
			}
			return VisitResult.REJECT;
		}
		return VisitResult.ACCEPT;
	}

	private String getValue(VisitContext ctx, UIInput input) {

		if (null != input.getConverter()) {
			return input.getConverter().getAsString(ctx.getFacesContext(), input, input.getValue());
		} else {
			return input.getValue().toString();
		}
	}

	private String getSelectedItem(UIInput component, Object value) {

		for (UIComponent subComponent : component.getChildren()) {

			if (subComponent instanceof UISelectItems) {
				@SuppressWarnings("unchecked")
				Map<String, Object> values = (Map<String, Object>) ((UISelectItems) subComponent).getValue();

				for (Entry<String, Object> item : values.entrySet()) {

					if (null == value && null == item.getValue() || null != value && value.equals(item.getValue())) {
						return item.getKey();
					}
				}
			}
		}
		return null;
	}

	public String getCriterias() {
		return criterias.length() > 0 ? criterias.substring(0, criterias.length() - 2) : "";
	}

}
