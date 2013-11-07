
/**
 * 
 */
package fr.logica.jsf.exception;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.apache.log4j.Logger;

import fr.logica.business.FunctionalException;

/**
 * @author bellangerf
 * 
 */
public class LogicaExceptionHandler extends ExceptionHandlerWrapper {

	/** Clé pour l'exception dans la RequestMap de l'ExternalContext. */
	public static final String REQUEST_MAP_EXCEPTION_KEY = "exception";

	/** Exception handler d'origine. */
	private final ExceptionHandler wrapped;

	/** Logger de la classe. */
	private static final Logger LOGGER = Logger.getLogger(LogicaExceptionHandler.class);

	/**
	 * Encapsule l'{@link ExceptionHandler} d'origine.
	 * 
	 * @param wrapped
	 *            {@link ExceptionHandler} renvoyé par l'implémentation JSF
	 */
	public LogicaExceptionHandler(final ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	/**
	 * @see ExceptionHandlerWrapper#getWrapped()
	 * @return {@link ExceptionHandler} décoré
	 */
	@Override
	public ExceptionHandler getWrapped() {
		return this.wrapped;
	}

	/**
	 * Traite toutes les exceptions de la file en redirigeant l'utilisateur vers une page adaptée à la première
	 * erreur rencontrée.
	 * 
	 * @see ExceptionHandler#handle()
	 */
	@Override
	public void handle() {
		Iterator<ExceptionQueuedEvent> itEx = getUnhandledExceptionQueuedEvents().iterator();

		while (itEx.hasNext()) {

			ExceptionQueuedEvent event = itEx.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable th = context.getException();

			handleThrowable(th, itEx);

		}

		/*
		 * La queue ne contient plus rien, normalement. On la transmet donc au parent : a priori inutile, mais limite
		 * les impacts si changement dans la gestion des exceptions (toutes les exceptions ne sont plus gérées dans la
		 * méthode handleThrowable).
		 */
		getWrapped().handle();

	}

	/**
	 * Gère une exception dans la queue des exceptions. Redirige l'utilisateur vers la page adaptée.
	 * 
	 * Chaque exception traitée donne lieu à un renvoi vers une page, mais seul le premier renvoi est effectif (on
	 * indique au contexte JSF d'afficher la réponse, la suite est donc ignorée).
	 * 
	 * @param th
	 *            Exception dans la queue.
	 * @param itEx
	 *            Itérateur positionné dans la queue (pour suppression suite au traitement).
	 */
	private static void handleThrowable(final Throwable th, final Iterator<ExceptionQueuedEvent> itEx) {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext externalContext = fc.getExternalContext();
		Map<String, Object> requestMap = externalContext.getRequestMap();

		String errorPage = null;
		String errorMessage = null;
		if (th instanceof ViewExpiredException) {
			LOGGER.info("ViewExpiredException", th);

			errorPage = "timeout";
			errorMessage = "Timeout de la session utilisateur";
		} else if (th instanceof Error) {
			LOGGER.error("Instance of Error", th);
			errorPage = "jvmError";
		} else if (th instanceof ELException && ((ELException) th).getCause() instanceof FunctionalException) {
			// Cas de tentative d'accès à une liste interdite.
			LOGGER.error("Tentative d'accès à une liste interdite", th);
			errorPage = "error";
			errorMessage = ((FunctionalException) ((ELException) th).getCause()).getMessage();
		} else {
			LOGGER.error("Error", th);
			errorPage = "error";
		}

		// Traitement réel de l'erreur
		try {
			StringWriter sw = new StringWriter();
			th.printStackTrace(new PrintWriter(sw, true));
			String exceptionAsString = sw.toString().replaceAll("\n", "<br />").replaceAll("Caused by:", "<br />Caused by:");
			requestMap.put("currentView", th.getMessage());
			requestMap.put(REQUEST_MAP_EXCEPTION_KEY, th + exceptionAsString);

			if (fc.getViewRoot() == null) {
				UIViewRoot root = new UIViewRoot();
				root.setViewId("");
				fc.setViewRoot(root);
			}

			if (errorMessage != null) {
				fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, errorMessage, null));
			}


			NavigationHandler nav = fc.getApplication().getNavigationHandler();
			nav.handleNavigation(fc, null, errorPage);

			fc.renderResponse();
		} finally {
			itEx.remove();
		}
	}
}
