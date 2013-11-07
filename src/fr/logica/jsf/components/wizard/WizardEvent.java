package fr.logica.jsf.components.wizard;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;

public class WizardEvent extends FacesEvent {

	private static final long serialVersionUID = 1L;
	private final String oldStep;	
	private final String newStep;

	public WizardEvent(UIComponent component, String oldStep, String newStep) {
		super(component);
		this.oldStep = oldStep;
		this.newStep = newStep;
	}

	@Override
	public boolean isAppropriateListener(FacesListener listener) {
		return false;
	}

	@Override
	public void processListener(FacesListener listener) {
	}

	public String getOldStep() {
		return oldStep;
	}

	public String getNewStep() {
		return newStep;
	}

}
