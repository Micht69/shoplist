package fr.logica.jsf.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class PostRedirectGetListener implements PhaseListener {

	/** serialUID */
	private static final long serialVersionUID = 1791693801309939608L;

	private static final String ALL_FACES_MESSAGES_ID = "PostRedirectGetListener.allFacesMessages";

	@Override
	public void afterPhase(PhaseEvent phase) {
		if ("true".equals(phase.getFacesContext().getExternalContext().getRequestParameterMap().get("javax.faces.partial.ajax"))) {
			return;
		}
		if (phase.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
			if (phase.getFacesContext().getMessageList().size() > 0) {
				saveFacesMessages(phase.getFacesContext());
			}
		}
	}

	@Override
	public void beforePhase(PhaseEvent phase) {
		if ("true".equals(phase.getFacesContext().getExternalContext().getRequestParameterMap().get("javax.faces.partial.ajax"))) {
			return;
		}
		if (phase.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			if (phase.getFacesContext().getExternalContext().getSessionMap().get(ALL_FACES_MESSAGES_ID) != null) {
				restoreFacesMessages(phase.getFacesContext());
			}
		}
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/**
	 * Save all facesmessages of the given facescontext in session. This is done so because the facesmessages are purely request scoped and would
	 * be lost in the new GET request otherwise.
	 * 
	 * @param facesContext The involved facescontext.
	 */
	private static void saveFacesMessages(FacesContext facesContext) {

		// Prepare the facesmessages holder in the sessionmap. The LinkedHashMap has precedence over
		// HashMap, because in a LinkedHashMap the FacesMessages will be kept in order, which can be
		// very useful for certain error and focus handlings. Anyway, it's just your design choice.
		Map<String, List<FacesMessage>> allFacesMessages =
				new LinkedHashMap<String, List<FacesMessage>>();
		facesContext.getExternalContext().getSessionMap()
				.put(ALL_FACES_MESSAGES_ID, allFacesMessages);

		// Get client ID's of all components with facesmessages.
		Iterator<String> clientIdsWithMessages = facesContext.getClientIdsWithMessages();
		while (clientIdsWithMessages.hasNext()) {
			String clientIdWithMessage = clientIdsWithMessages.next();

			// Prepare client-specific facesmessages holder in the main facesmessages holder.
			List<FacesMessage> clientFacesMessages = new ArrayList<FacesMessage>();
			allFacesMessages.put(clientIdWithMessage, clientFacesMessages);

			// Get all messages from client and add them to the client-specific facesmessage list.
			Iterator<FacesMessage> facesMessages = facesContext.getMessages(clientIdWithMessage);
			while (facesMessages.hasNext()) {
				clientFacesMessages.add(facesMessages.next());
			}
		}
	}

	/**
	 * Restore any facesmessages from session in the given FacesContext.
	 * 
	 * @param facesContext The involved FacesContext.
	 */
	@SuppressWarnings("unchecked")
	private static void restoreFacesMessages(FacesContext facesContext) {

		// Remove all facesmessages from session.
		Map<String, List<FacesMessage>> allFacesMessages = (Map<String, List<FacesMessage>>)
				facesContext.getExternalContext().getSessionMap().remove(ALL_FACES_MESSAGES_ID);

		// Restore them in the given facescontext.
		for (Entry<String, List<FacesMessage>> entry : allFacesMessages.entrySet()) {
			for (FacesMessage clientFacesMessage : entry.getValue()) {
				facesContext.addMessage(entry.getKey(), clientFacesMessage);
			}
		}
	}
}
