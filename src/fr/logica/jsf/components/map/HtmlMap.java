package fr.logica.jsf.components.map;

import java.util.ArrayList;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputHidden;

@ResourceDependencies({
	@ResourceDependency(name = "map/style.css"),
	@ResourceDependency(name = "map/OpenLayers.min.js"),
	@ResourceDependency(name = "map/mapUtils.js"),
})
@FacesComponent(HtmlMap.COMPONENT_TYPE)
public class HtmlMap extends HtmlInputHidden {

	private static final String OPTIMIZED_PACKAGE = "fr.logica.jsf.components.map";
	private static final String ATTRIBUTES_THAT_ARE_SET = "javax.faces.component.UIComponentBase.attributesThatAreSet";
	private static final String DEFAULT_RENDERER = "fr.logica.jsf.components.map.MapRenderer";
	public static final String COMPONENT_TYPE = "cgi.faces.HtmlMap";

	protected enum PropertyKeys {
		disabled,
		stringValue,
		style,
		styleClass;
	}

	public HtmlMap() {
		setRendererType(DEFAULT_RENDERER);
	}

	public boolean isDisabled() {
		return (Boolean) getStateHelper().eval(PropertyKeys.disabled, Boolean.FALSE);
	}

	public void setDisabled(boolean disabled) {
		getStateHelper().put(PropertyKeys.disabled, disabled);
	}

	public String getStringValue() {
		return (String) getStateHelper().eval(PropertyKeys.stringValue, null);
	}

	public void setStringValue(String stringValue) {
		getStateHelper().put(PropertyKeys.stringValue, stringValue);
	}

	public String getStyle() {
		return (String) getStateHelper().eval(PropertyKeys.style, null);
	}

	public void setStyle(String style) {
		getStateHelper().put(PropertyKeys.style, style);
		handleAttribute(PropertyKeys.style.toString(), style);
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(PropertyKeys.styleClass, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(PropertyKeys.styleClass, styleClass);
		handleAttribute(PropertyKeys.styleClass.toString(), styleClass);
	}

	private void handleAttribute(String name, Object value) {
		@SuppressWarnings("unchecked")
		List<String> setAttributes = (List<String>) this.getAttributes().get(
				ATTRIBUTES_THAT_ARE_SET);
		if (setAttributes == null) {
			String cname = this.getClass().getName();
			if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
				setAttributes = new ArrayList<String>(2);
				this.getAttributes()
						.put(ATTRIBUTES_THAT_ARE_SET, setAttributes);
			}
		}
		if (setAttributes != null) {
			if (value == null) {
				ValueExpression ve = getValueExpression(name);
				if (ve == null) {
					setAttributes.remove(name);
				}
			} else if (!setAttributes.contains(name)) {
				setAttributes.add(name);
			}
		}
	}

}