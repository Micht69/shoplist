package fr.logica.jsf.components.autocomplete;

import javax.el.MethodExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;

@FacesComponent(UIAutocomplete.COMPONENT_TYPE)
public class UIAutocomplete extends HtmlInputText {

    public static final String COMPONENT_TYPE = "logica.faces.autocomplete";

    protected enum PropertyKeys {
        autocompleteMethod,
        minLength;

        String toString;
        PropertyKeys(String toString) { this.toString = toString; }
        PropertyKeys() { }

        public String toString() {
            return ((toString != null) ? toString : super.toString());
        }

    }

    public UIAutocomplete() {
        super();
        setRendererType("logica.faces.AutocompleteRenderer");
    }

    public MethodExpression getAutocompleteMethod() {
        return (MethodExpression) getStateHelper().eval(PropertyKeys.autocompleteMethod);
    }

    public void setAutocompleteMethod(MethodExpression autocompleteMethod) {
        getStateHelper().put(PropertyKeys.autocompleteMethod, autocompleteMethod);
    }

    public Integer getMinLength() {
        return (Integer) getStateHelper().eval(PropertyKeys.minLength);
    }

    public void setMinLength(Integer minLength) {
        getStateHelper().put(PropertyKeys.minLength, minLength);
    }

}
