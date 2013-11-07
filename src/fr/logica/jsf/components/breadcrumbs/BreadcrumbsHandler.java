package fr.logica.jsf.components.breadcrumbs;

import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.MetaRuleset;

import com.sun.faces.facelets.tag.MethodRule;

public class BreadcrumbsHandler extends ComponentHandler {

	public BreadcrumbsHandler(ComponentConfig config) {
		super(config);
	}

	@SuppressWarnings("rawtypes")
	protected MetaRuleset createMetaRuleset(Class type) {
		MetaRuleset metaRuleset = super.createMetaRuleset(type);
		metaRuleset.addRule(new MethodRule("method", String.class, new Class[] { int.class }));
		return metaRuleset;
	}

}
