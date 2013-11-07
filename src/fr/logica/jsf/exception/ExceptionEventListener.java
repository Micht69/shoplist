package fr.logica.jsf.exception;

import java.util.Map;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.apache.log4j.Logger;

public class ExceptionEventListener implements SystemEventListener {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(ExceptionEventListener.class);

	/**
	 * Renvoie toujours <code>true</code> : on veut �couter tous les objets.
	 * 
	 * @see javax.faces.event.SystemEventListener#isListenerForSource(java.lang.Object)
	 * @param sender
	 *            Emetteur de l'�v�nement.
	 * @return Toujours <code>true</code>
	 */
	@Override
	public boolean isListenerForSource(final Object sender) {
		/* Int�ress� par tout et n'importe quoi */
		return true;
	}

	/**
	 * Traite un �v�nement re�u. Si l'�v�nement est une instance de
	 * ExceptionQueuedEvent, l'exception mise en queue est logg�e.
	 * 
	 * @see javax.faces.event.SystemEventListener#processEvent(javax.faces.event.SystemEvent)
	 * @param uncastEvent
	 *            Ev�nement re�u.
	 */
	@Override
	public void processEvent(final SystemEvent uncastEvent) {
		if (uncastEvent instanceof ExceptionQueuedEvent) {
			ExceptionQueuedEvent event = (ExceptionQueuedEvent) uncastEvent;
			Throwable ex = event.getContext().getException();
			LOGGER.error("Exception received by JSF", ex);
			FacesContext fc = FacesContext.getCurrentInstance();
			Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();

			ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication()
					.getNavigationHandler();
			requestMap.put(LogicaExceptionHandler.REQUEST_MAP_EXCEPTION_KEY, ex);
			nav.performNavigation("error");
		} else {
			LOGGER.error("Received event which was NOT an exception : " + uncastEvent);
		}
	}

}
