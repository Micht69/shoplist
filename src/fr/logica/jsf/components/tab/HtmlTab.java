package fr.logica.jsf.components.tab;

import java.util.ArrayList;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIPanel;

@FacesComponent(HtmlTab.COMPONENT_TYPE)
public class HtmlTab extends UIPanel {

	private static final String OPTIMIZED_PACKAGE = "fr.logica.jsf.components.tab";
	private static final String ATTRIBUTES_THAT_ARE_SET = "javax.faces.component.UIComponentBase.attributesThatAreSet";
	public static final String COMPONENT_TYPE = "cgi.faces.HtmlTab";

	protected enum PropertyKeys {
		style,
		styleClass,
		title;
	}

	public HtmlTab() {
		setRendererType(null);
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

	public String getTitle() {
		return (String) getStateHelper().eval(PropertyKeys.title, null);
	}

	public void setTitle(String title) {
		getStateHelper().put(PropertyKeys.title, title);
		handleAttribute(PropertyKeys.title.toString(), title);
	}

	private void handleAttribute(String name, Object value) {
		@SuppressWarnings("unchecked")
		List<String> setAttributes = (List<String>) this.getAttributes().get(ATTRIBUTES_THAT_ARE_SET);
		if (setAttributes == null) {
			String cname = this.getClass().getName();
			if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
				setAttributes = new ArrayList<String>(3);
				this.getAttributes().put(ATTRIBUTES_THAT_ARE_SET, setAttributes);
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
