package fr.logica.jsf.components.checkbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehaviorHolder;

@FacesComponent(HtmlSelectStringCheckbox.COMPONENT_TYPE)
public class HtmlSelectStringCheckbox extends UIInput implements ClientBehaviorHolder {

	private static final String OPTIMIZED_PACKAGE = "javax.faces.component.";
	private static final String ATTRIBUTES_THAT_ARE_SET = "javax.faces.component.UIComponentBase.attributesThatAreSet";
	private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList("blur", "change", "click",
			"dblclick", "focus", "keydown", "keypress", "keyup", "mousedown", "mousemove", "mouseout", "mouseover", "mouseup", "select"));
	public static final String COMPONENT_FAMILY = "cgi.faces.SelectString";
	public static final String COMPONENT_TYPE = "cgi.faces.HtmlSelectStringCheckbox";
	public static final String RENDERER_TYPE = "javax.faces.Checkbox";

	public HtmlSelectStringCheckbox() {
		super();
		setRendererType(RENDERER_TYPE);
	}

	protected enum PropertyKeys {
		accesskey,
		dir,
		disabled,
		label,
		lang,
		onblur,
		onchange,
		onclick,
		ondblclick,
		onfocus,
		onkeydown,
		onkeypress,
		onkeyup,
		onmousedown,
		onmousemove,
		onmouseout,
		onmouseover,
		onmouseup,
		onselect,
		readonly,
		style,
		styleClass,
		tabindex,
		title;
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	public String getAccesskey() {
		return (String) getStateHelper().eval(PropertyKeys.accesskey);
	}

	public void setAccesskey(String accesskey) {
		getStateHelper().put(PropertyKeys.accesskey, accesskey);
		handleAttribute(PropertyKeys.accesskey, accesskey);
	}

	public String getDir() {
		return (String) getStateHelper().eval(PropertyKeys.dir);
	}

	public void setDir(String dir) {
		getStateHelper().put(PropertyKeys.dir, dir);
		handleAttribute(PropertyKeys.dir, dir);
	}

	public boolean isDisabled() {
		return (Boolean) getStateHelper().eval(PropertyKeys.disabled, false);
	}

	public void setDisabled(boolean disabled) {
		getStateHelper().put(PropertyKeys.disabled, disabled);
	}

	public String getLabel() {
		return (String) getStateHelper().eval(PropertyKeys.label);
	}

	public void setLabel(String label) {
		getStateHelper().put(PropertyKeys.label, label);
	}

	public String getLang() {
		return (String) getStateHelper().eval(PropertyKeys.lang);
	}

	public void setLang(String lang) {
		getStateHelper().put(PropertyKeys.lang, lang);
		handleAttribute(PropertyKeys.lang, lang);
	}

	public String getOnblur() {
		return (String) getStateHelper().eval(PropertyKeys.onblur);
	}

	public void setOnblur(String onblur) {
		getStateHelper().put(PropertyKeys.onblur, onblur);
		handleAttribute(PropertyKeys.onblur, onblur);
	}

	public String getOnchange() {
		return (String) getStateHelper().eval(PropertyKeys.onchange);
	}

	public void setOnchange(String onchange) {
		getStateHelper().put(PropertyKeys.onchange, onchange);
		handleAttribute(PropertyKeys.onchange, onchange);
	}

	public String getOnclick() {
		return (String) getStateHelper().eval(PropertyKeys.onclick);
	}

	public void setOnclick(String onclick) {
		getStateHelper().put(PropertyKeys.onclick, onclick);
	}

	public String getOndblclick() {
		return (String) getStateHelper().eval(PropertyKeys.ondblclick);
	}

	public void setOndblclick(String ondblclick) {
		getStateHelper().put(PropertyKeys.ondblclick, ondblclick);
		handleAttribute(PropertyKeys.ondblclick, ondblclick);
	}

	public String getOnfocus() {
		return (String) getStateHelper().eval(PropertyKeys.onfocus);
	}

	public void setOnfocus(String onfocus) {
		getStateHelper().put(PropertyKeys.onfocus, onfocus);
		handleAttribute(PropertyKeys.onfocus, onfocus);
	}

	public String getOnkeydown() {
		return (String) getStateHelper().eval(PropertyKeys.onkeydown);
	}

	public void setOnkeydown(String onkeydown) {
		getStateHelper().put(PropertyKeys.onkeydown, onkeydown);
		handleAttribute(PropertyKeys.onkeydown, onkeydown);
	}

	public String getOnkeypress() {
		return (String) getStateHelper().eval(PropertyKeys.onkeypress);
	}

	public void setOnkeypress(String onkeypress) {
		getStateHelper().put(PropertyKeys.onkeypress, onkeypress);
		handleAttribute(PropertyKeys.onkeypress, onkeypress);
	}

	public String getOnkeyup() {
		return (String) getStateHelper().eval(PropertyKeys.onkeyup);
	}

	public void setOnkeyup(String onkeyup) {
		getStateHelper().put(PropertyKeys.onkeyup, onkeyup);
		handleAttribute(PropertyKeys.onkeyup, onkeyup);
	}

	public String getOnmousedown() {
		return (String) getStateHelper().eval(PropertyKeys.onmousedown);
	}

	public void setOnmousedown(String onmousedown) {
		getStateHelper().put(PropertyKeys.onmousedown, onmousedown);
		handleAttribute(PropertyKeys.onmousedown, onmousedown);
	}

	public String getOnmousemove() {
		return (String) getStateHelper().eval(PropertyKeys.onmousemove);
	}

	public void setOnmousemove(String onmousemove) {
		getStateHelper().put(PropertyKeys.onmousemove, onmousemove);
		handleAttribute(PropertyKeys.onmousemove, onmousemove);
	}

	public String getOnmouseout() {
		return (String) getStateHelper().eval(PropertyKeys.onmouseout);
	}

	public void setOnmouseout(String onmouseout) {
		getStateHelper().put(PropertyKeys.onmouseout, onmouseout);
		handleAttribute(PropertyKeys.onmouseout, onmouseout);
	}

	public String getOnmouseover() {
		return (String) getStateHelper().eval(PropertyKeys.onmouseover);
	}

	public void setOnmouseover(String onmouseover) {
		getStateHelper().put(PropertyKeys.onmouseover, onmouseover);
		handleAttribute(PropertyKeys.onmouseover, onmouseover);
	}

	public String getOnmouseup() {
		return (String) getStateHelper().eval(PropertyKeys.onmouseup);
	}

	public void setOnmouseup(String onmouseup) {
		getStateHelper().put(PropertyKeys.onmouseup, onmouseup);
		handleAttribute(PropertyKeys.onmouseup, onmouseup);
	}

	public String getOnselect() {
		return (String) getStateHelper().eval(PropertyKeys.onselect);
	}

	public void setOnselect(String onselect) {
		getStateHelper().put(PropertyKeys.onselect, onselect);
		handleAttribute(PropertyKeys.onselect, onselect);
	}

	public boolean isReadonly() {
		return (Boolean) getStateHelper().eval(PropertyKeys.readonly, false);
	}

	public void setReadonly(boolean readonly) {
		getStateHelper().put(PropertyKeys.readonly, readonly);
	}

	public String getStyle() {
		return (String) getStateHelper().eval(PropertyKeys.style);
	}

	public void setStyle(String style) {
		getStateHelper().put(PropertyKeys.style, style);
		handleAttribute(PropertyKeys.style, style);
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(PropertyKeys.styleClass);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(PropertyKeys.styleClass, styleClass);
	}

	public String getTabindex() {
		return (String) getStateHelper().eval(PropertyKeys.tabindex);
	}

	public void setTabindex(String tabindex) {
		getStateHelper().put(PropertyKeys.tabindex, tabindex);
		handleAttribute(PropertyKeys.tabindex, tabindex);
	}

	public String getTitle() {
		return (String) getStateHelper().eval(PropertyKeys.title);
	}

	public void setTitle(String title) {
		getStateHelper().put(PropertyKeys.title, title);
		handleAttribute(PropertyKeys.title, title);
	}

	public Collection<String> getEventNames() {
		return EVENT_NAMES;
	}

	public String getDefaultEventName() {
		return "change";
	}

	private void handleAttribute(PropertyKeys key, Object value) {
		@SuppressWarnings("unchecked")
		List<String> setAttributes = (List<String>) this.getAttributes().get(ATTRIBUTES_THAT_ARE_SET);
		if (setAttributes == null) {
			String cname = this.getClass().getName();
			if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
				setAttributes = new ArrayList<String>();
				this.getAttributes().put(ATTRIBUTES_THAT_ARE_SET, setAttributes);
			}
		}
		if (setAttributes != null) {
			String name = key.toString();
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
