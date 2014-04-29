package fr.logica.jsf.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import fr.logica.business.context.RequestContext;

public class RequestController {

	/** Session controller. Holds user relative data and access rights management. */
	private SessionController sessionCtrl;

	private RequestContext requestContext;

	private ViewController attachedView;

	@PostConstruct
	public void initializeContext() {
		requestContext = new RequestContext(sessionCtrl.getContext());
	}

	@PreDestroy
	public void closeContext() {
		if (requestContext != null) {
			requestContext.close();
		}
		if (attachedView != null) {
			attachedView.setContext(null);
		}
		attachedView = null;
	}

	public SessionController getSessionCtrl() {
		return sessionCtrl;
	}

	public void setSessionCtrl(SessionController sessionCtrl) {
		this.sessionCtrl = sessionCtrl;
	}

	public void initializeViewContext(ViewController viewCtrl) {
		if (attachedView == null || attachedView.getContext() == null) {
			attachedView = viewCtrl;
			viewCtrl.setContext(requestContext);
		}
	}
}
