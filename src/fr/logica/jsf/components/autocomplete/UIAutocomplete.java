package fr.logica.jsf.components.autocomplete;

import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;

import fr.logica.jsf.model.link.LinkQuickSearchModel;

@FacesComponent(UIAutocomplete.COMPONENT_TYPE)
public class UIAutocomplete extends HtmlInputText {

	public static final String COMPONENT_TYPE = "logica.faces.autocomplete";

	protected enum PropertyKeys {
		model,
		minLength;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {
		}

		public String toString() {
			return ((toString != null) ? toString : super.toString());
		}

	}

	public UIAutocomplete() {
		super();
		setRendererType("logica.faces.AutocompleteRenderer");
	}

	public LinkQuickSearchModel getModel() {
		return (LinkQuickSearchModel) getStateHelper().eval(PropertyKeys.model);
	}

	public void setModel(LinkQuickSearchModel model) {
		getStateHelper().put(PropertyKeys.model, model);
	}

	public Integer getMinLength() {
		return (Integer) getStateHelper().eval(PropertyKeys.minLength);
	}

	public void setMinLength(Integer minLength) {
		getStateHelper().put(PropertyKeys.minLength, minLength);
	}

}
