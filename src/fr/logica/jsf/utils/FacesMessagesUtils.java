package fr.logica.jsf.utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import fr.logica.application.ApplicationUtils;
import fr.logica.business.FunctionalException;
import fr.logica.business.context.RequestContext;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;

/**
 * Utility class to transfert messages from our framework to JSF.
 * 
 * @author schmittse
 * 
 */
public final class FacesMessagesUtils {
	/** Hide constructor */
	private FacesMessagesUtils() {
	}

	/**
	 * Get messages from given FunctionalException and add them to the current FacesContext
	 * 
	 * @param fEx
	 *            the FunctionalException
	 */
	public static void displayMessages(FunctionalException fEx) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		for (Message msg : fEx.getMessages()) {
			addMessage(ctx, msg);
		}
	}

	/**
	 * Display the given Exception as a FacesContext error message.
	 * 
	 * @param e
	 *            the Exception
	 */
	public static void displayMessages(Exception e) {
		String message = getTechnicalMessage(e);
		addErrorMessage(FacesContext.getCurrentInstance(), message);
	}

	/**
	 * Get messages from given RequestContext and add them to the current FacesContext
	 * 
	 * @param context
	 *            the current RequestContext
	 */
	public static void displayMessages(RequestContext context) {
		displayMessages(FacesContext.getCurrentInstance(), context);
	}

	/**
	 * Get messages from given RequestContext and add them to given FacesContext
	 * 
	 * @param ctx
	 *            the target FacesContext
	 * @param context
	 *            the current RequestContext
	 */
	public static void displayMessages(FacesContext ctx, RequestContext context) {
		if (null != context.getMessages()) {
			for (Message msg : context.getMessages()) {
				addMessage(ctx, msg);
			}
			context.getMessages().clear();
		}
	}

	/**
	 * Creates a human readable (at least developper readable) message from a technical exception. We'll extract the first 4 lines of stacktrace
	 * and exception message. This method is public because it might be used by UI Components (DataModels).
	 * 
	 * @param e
	 *            Catched exception we want to display.
	 * @return String containing exception message and first 4 lines of exception stacktrace.
	 */
	public static String getTechnicalMessage(Exception e) {
		return ApplicationUtils.getApplicationLogic().formatExceptionToString(e);
	}

	private static void addInfoMessage(FacesContext ctx, String msgText) {
		ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msgText, null));
	}

	private static void addErrorMessage(FacesContext ctx, String msgText) {
		ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msgText, null));
	}
	
	private static void addMessage(FacesContext ctx, Message msg) {
		if (msg.getSeverity() == Severity.INFO) {
			addInfoMessage(ctx, msg.getMessage());
		} else if (msg.getSeverity() == Severity.ERROR) {
			addErrorMessage(ctx, msg.getMessage());
		}
	}
}
