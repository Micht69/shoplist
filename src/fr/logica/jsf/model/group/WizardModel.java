package fr.logica.jsf.model.group;

import java.io.Serializable;
import java.util.Map;

import fr.logica.business.Entity;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.components.wizard.WizardEvent;
import fr.logica.jsf.controller.ViewController;

public class WizardModel extends TabPanelModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 6056000258131909343L;

	public WizardModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String panelName) {
		super(viewCtrl, store, entity, entityName, panelName);
	}

	public String checkStepFlow(WizardEvent event) {
		String result = event.getNewStep();
		RequestContext context = null;
		try {
			context = new RequestContext(viewCtrl.getSessionCtrl().getContext());
			boolean nexStepAllowed = new BusinessController().checkWizardStep(viewCtrl.getEntity(), viewCtrl.getCurrentView().getAction(),
					event.getOldStep(), event.getNewStep(), context);

			if (!nexStepAllowed) {
				result = event.getOldStep();
			}
		} finally {
			if (context != null) {
				// Close request context potential database connection
				context.close();
			}
		}
		viewCtrl.displayMessages(context);
		return result;
	}

}
