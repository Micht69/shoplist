package fr.logica.jsf.components.schedule;

import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.MetaRuleset;

import com.sun.faces.facelets.tag.MethodRule;

import fr.logica.business.data.ScheduleEvent;

/**
 * Handler to retrieve correctly the method to call while the user updates (moving or resizing) an event into the schedule.
 */
public class ScheduleHandler extends ComponentHandler {

	/**
	 * Creates a new handler.
	 * @param config Configuration.
	 */
	public ScheduleHandler(ComponentConfig config) {
		super(config);
	}

	@SuppressWarnings("rawtypes")
	protected MetaRuleset createMetaRuleset(Class type) {
		MetaRuleset metaRuleset = super.createMetaRuleset(type);
		metaRuleset.addRule(new MethodRule("updateListener", Void.class, new Class[] { ScheduleEvent.class }));
		return metaRuleset;
	}

}
