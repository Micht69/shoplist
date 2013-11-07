package fr.logica.jsf.components.wizard;

import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.MetaRuleset;

import com.sun.faces.facelets.tag.MethodRule;

public class WizardHandler extends ComponentHandler {

	public WizardHandler(ComponentConfig config) {
		super(config);
	}

	@SuppressWarnings("rawtypes")
	protected MetaRuleset createMetaRuleset(Class type) {
		MetaRuleset metaRuleset = super.createMetaRuleset(type);
		metaRuleset.addRule(new MethodRule("stepFlowListener", String.class, new Class[] { WizardEvent.class }));
		return metaRuleset;
	}

}
