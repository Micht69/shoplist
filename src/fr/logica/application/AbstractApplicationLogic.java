package fr.logica.application;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import fr.logica.application.logic.User;
import fr.logica.business.context.ApplicationContext;
import fr.logica.business.context.RequestContext;
import fr.logica.business.context.SessionContext;
import fr.logica.business.controller.Request;

/**
 * Class used to store application logic methods.
 */
public abstract class AbstractApplicationLogic implements Serializable {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns the format for a date object of the given type. The type is the one returned by
	 * {@link fr.logica.business.EntityField#getSqlType()}.
	 */
	public final String getDateTimeFormatFor(String type) {
		if ("DATE".equals(type)) {
			return getDateFormat();
		} else if ("TIME".equals(type)) {
			return getTimeFormat();
		} else if ("TIMESTAMP".equals(type)) {
			return getTimestampFormat();
		} else {
			throw new IllegalArgumentException(type);
		}
	}

	/**
	 * @return The format used to display date objects.
	 */
	public abstract String getDateFormat();

	/**
	 * @return The format used to display date objects with time.
	 */
	public abstract String getDatetimeFormat();

	/**
	 * @return The format used to display time objects.
	 */
	public abstract String getTimeFormat();

	/**
	 * @return The format used to display timestamp objects.
	 */
	public abstract String getTimestampFormat();

	/**
	 * @param currentTitle Current view's title. May be null if there's no view object to display.
	 * @return The page title.
	 */
	public abstract String getPageTitle(String currentTitle);

	/**
	 * @param user
	 *            Current authenticated user (can be {@code null}).
	 * @return The default page identifier.
	 */
	public abstract String getDefaultPage(User user);

	/**
	 * Delegates the user locale management. This method is called when a new sessionContext is created to get the current user Locale. <br/>
	 * Default application logic will get available languages from server.properties. Il requested locale is present amongst available languages,
	 * it is choosen. Otherwise, it will use the first available language.
	 * 
	 * Choosen locale is set into SessionContext
	 */
	public abstract void setDefaultLocale(SessionContext context, Locale requestedLocale);

	/**
	 * Are the social features (google +1, Facebook Like) enabled on the application.
	 * 
	 * @return true if enabled
	 */
	public abstract boolean enableSocialFeatures();

	/**
	 * Are the comments enabled on the application.
	 * 
	 * @return true if enabled
	 */
	public abstract boolean enableComments();

	/**
	 * Should we display actions in select list pages ?
	 * 
	 * @return true if enabled -> action buttons will be displayed on select pages
	 */
	public abstract boolean enableSelectActions();

	/**
	 * Should we enable the "Export" button on all lists
	 * 
	 * @return true if enabled
	 */
	public abstract boolean enableXlsExport();
	
	/**
	 * Should we enable the "Permalink" feature
	 * 
	 * @return true if enabled
	 */
	public abstract boolean enablePermalink();
	
	/**
	 * Allows specific criteria behavior on lists page opening.
	 * @return OpenCriteriaBehavior. DEFAULT : criteria will be open if there are more results than Constants.MAX_ROW<br/>
	 * 		ALWAYS : criteria will always be open<br/>
	 * 		NEVER : criteria will never be open
	 */
	public abstract OpenCriteriaBehavior getOpenCriteriaBehavior();

	public enum OpenCriteriaBehavior {
		DEFAULT,
		ALWAYS,
		NEVER
	}
	
	/**
	 * Initializes application context. This method is called on application startup.
	 * 
	 * @param context
	 *            The application context. This context persists as long as the application runs on application server.
	 */
	public abstract void initializeApplication(ApplicationContext context);

	/**
	 * Finalizes application context. This method is called on application shutdown.
	 * 
	 * @param context
	 *            The application context to finalize. This method should close all resources stored in application context and ensure that no
	 *            system lock remains.
	 */
	public abstract void finalizeApplication(ApplicationContext context);
	
	/**
	 * Create the permalink to display to the user from base url and url parameters *
	 * 
	 * @param baseUrl
	 *            the server url (starts with http and ends <b>without</b> '?')
	 * @param urlParams
	 *            a map with all url parameters [key can be : entityName, queryName, actionCode, encodedKeyList]
	 * @return the permalink to display to the user
	 */
	public abstract String getPermaLink(String baseUrl, Map<String, String> urlParams);

	/**
	 * Parses current HTTP Request parameters to build a user Request in order to directly access to a specific view. <br/>
	 * Default handled parameters are built through the "Permalink" menu in footer of any page. This method can be overriden to handle any
	 * parameter <br/>
	 * 
	 * @param parameters
	 *            HTTP Request parameters
	 * @param context
	 *            Request Context that will be attached to the request on processing. Contains a link towards SessionContext of the current user.
	 * @return A valid Request with entityName, action and all needed parameters (queryName, keys, page, etc.)
	 */
	public abstract Request<?> getPermalinkRequest(Map<String, String> parameters, RequestContext context);

	/**
	 * Formats unhandled exception thrown by internal framework / domain logic code to display / explain them to the end user. Default formatting
	 * will display the exception message + 4 firsts elements of stacktrace.
	 * 
	 * @param e
	 *            Exception to format.
	 * @return A formatted String (with 
 end of lines) that will be displayed to the end user.
	 */
	public abstract String formatExceptionToString(Exception e);
	
	/** 
	 * <p>Returns a String description of the exception to be displayed to the front user.</p>
	 * <p>It will usually be between the full stack trace of the exception, and a standard message.
	 * The more information is given, the easier it is to debug the application when the only 
	 * information the developer gets is a snapshot. However, higher detail also means a higher 
	 * security risk, as the inner workings of the application are displayed to just about anyone.
	 * </p>
	 * <p>The description is in HTML format, with &lt;br /&gt; tags to mark the new lines.</p>
	 * @param th 
	 * 			The exception whose description will be returned
	 * @return The exception description, in HTML
	 */
	public abstract String getExceptionHtmlDescription(Throwable th);
}
