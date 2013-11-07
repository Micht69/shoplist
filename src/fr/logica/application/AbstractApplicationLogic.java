package fr.logica.application;

import java.io.Serializable;
import java.util.Locale;

import fr.logica.security.ApplicationUser;

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
	 * @return The format used to display time objects.
	 */
	public abstract String getTimeFormat();

	/**
	 * @return The format used to display timestamp objects.
	 */
	public abstract String getTimestampFormat();

	/**
	 * @param currentTitle
	 *            Current page's title.
	 * @return The page title.
	 */
	public abstract String getPageTitle(String currentTitle);

	/**
	 * @param user
	 *            Current authenticated user (can be {@code null}).
	 * @return The default page identifier.
	 */
	public abstract String getDefaultPage(ApplicationUser user);

	/**
	 * Delegates the user locale management. This method is called by MessageUtils to get the current user Locale. Default application logic will
	 * always sends back default JVM locale (Locale.getDefault()). Locale management depends on user session management.
	 * 
	 * @return A Locale to use
	 */
	public abstract Locale getCurrentUserLocale();

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
}
