package fr.logica.jsf.listener;

import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

import fr.logica.business.MessageUtils;

public class BrowserNavigationListener implements PhaseListener {

	/** serialUID */
	private static final long serialVersionUID = -8643555466449131186L;

	public static final String EXPIRED_VIEW_TOKEN = "EXPIRED_VIEW_TOKEN";

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		HttpServletResponse response = (HttpServletResponse) facesContext
				.getExternalContext().getResponse();
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "must-revalidate");
		response.addHeader("Expires", "Mon, 1 Jan 1970 10:00:00 GMT");

		// Handle expired view exception
		if (facesContext.getExternalContext().getSessionMap().containsKey(EXPIRED_VIEW_TOKEN)) {
			facesContext.getExternalContext().getSessionMap().remove(EXPIRED_VIEW_TOKEN);
			facesContext.addMessage(null,
					new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR,
							MessageUtils.getInstance(Locale.getDefault()).getMessage("error.viewExpired", (Object[]) null),
							null));
		}
	}

	@Override
	public void afterPhase(PhaseEvent event) {
		// Nothing to do
	}
}
