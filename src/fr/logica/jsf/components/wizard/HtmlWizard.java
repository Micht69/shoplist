package fr.logica.jsf.components.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;

import fr.logica.jsf.components.tab.HtmlTab;

@ResourceDependencies({
		@ResourceDependency(name = "wizard/jquery.jWizard.css"),
		@ResourceDependency(name = "wizard/jquery.jWizard.js")
})
@FacesComponent(HtmlWizard.COMPONENT_TYPE)
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class HtmlWizard extends UIInput {

	private static final String OPTIMIZED_PACKAGE = "fr.logica.jsf.components.wizard";
	private static final String ATTRIBUTES_THAT_ARE_SET = "javax.faces.component.UIComponentBase.attributesThatAreSet";
	private static final String DEFAULT_RENDERER = "fr.logica.jsf.components.wizard.WizardRenderer";
	public static final String COMPONENT_TYPE = "cgi.faces.HtmlWizard";

	protected enum PropertyKeys {
		style,
		styleClass,
		locale,
		stepFlowListener;
	}

	private HtmlTab currentStep;

	public HtmlWizard() {
		setRendererType(DEFAULT_RENDERER);
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

	public Locale getLocale() {
		return (Locale) getStateHelper().eval(PropertyKeys.locale, null);
	}

	public void setLocale(Locale locale) {
		getStateHelper().put(PropertyKeys.locale, locale);
	}

	public javax.el.MethodExpression getStepFlowListener() {
		return (javax.el.MethodExpression) getStateHelper().eval(PropertyKeys.stepFlowListener, null);
	}

	public void setStepFlowListener(javax.el.MethodExpression stepFlowListener) {
		getStateHelper().put(PropertyKeys.stepFlowListener, stepFlowListener);
		handleAttribute(PropertyKeys.stepFlowListener.toString(), stepFlowListener);
	}

	public void processDecodes(FacesContext context) {
		this.decode(context);

		if (!isBackRequest(context)) {
			getStepToProcess().processDecodes(context);
		}
	}

	public void processValidators(FacesContext context) {
		if (!isBackRequest(context)) {
			currentStep.processValidators(context);
		}
	}

	public void processUpdates(FacesContext context) {
		if (!isBackRequest(context)) {
			currentStep.processUpdates(context);
		}
	}

	public HtmlTab getStepToProcess() {

		if (currentStep == null) {
			String currentStepId = (String) getValue();

			for (UIComponent child : getChildren()) {
				if ((null == currentStepId && child instanceof HtmlTab && child.isRendered()) || child.getId().equals(currentStepId)) {
					currentStep = (HtmlTab) child;
					return currentStep;
				}
			}
		}
		return currentStep;
	}

	public boolean isWizardRequest(FacesContext context) {
		return context.getExternalContext().getRequestParameterMap().containsKey(getClientId(context) + "_nextStepRequest");
	}

	public boolean isBackRequest(FacesContext context) {
		return context.getExternalContext().getRequestParameterMap().containsKey(getClientId(context) + "_backRequest");
	}

	@Override
	public void broadcast(FacesEvent event) throws AbortProcessingException {
		super.broadcast(event);

		if (event instanceof WizardEvent) {
			WizardEvent flowEvent = (WizardEvent) event;
			FacesContext context = getFacesContext();
			MethodExpression me = getStepFlowListener();

			if (me != null) {
				String step = (String) me.invoke(context.getELContext(), new Object[] { event });
				setValue(step);
			} else {
				setValue(flowEvent.getNewStep());
			}
		}
	}

	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {

		if (event instanceof PostAddToViewEvent) {
			FacesContext context = FacesContext.getCurrentInstance();
			Locale locale = getLocale();

			if (null == locale) {
				locale = context.getApplication().getDefaultLocale();
			}

			if (Locale.FRANCE.getLanguage().equals(locale.getLanguage())) {
				UIOutput resource = new UIOutput();
				resource.getAttributes().put("name", "wizard/jquery.jWizard.fr.js");
				resource.setRendererType("javax.faces.resource.Script");
				context.getViewRoot().addComponentResource(context, resource);
			}
		}
		super.processEvent(event);
	}

	private void handleAttribute(String name, Object value) {
		@SuppressWarnings("unchecked")
		List<String> setAttributes = (List<String>) this.getAttributes().get(ATTRIBUTES_THAT_ARE_SET);
		if (setAttributes == null) {
			String cname = this.getClass().getName();
			if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
				setAttributes = new ArrayList<String>(2);
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
