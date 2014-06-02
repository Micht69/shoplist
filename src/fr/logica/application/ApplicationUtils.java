package fr.logica.application;

import org.apache.log4j.Logger;

import fr.logica.business.context.ApplicationContext;

/**
 * Utility class used to retrieve {@link ApplicationLogic}.
 */
public class ApplicationUtils {

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(ApplicationUtils.class);

	/** Private constructor (utility class). */
	private ApplicationUtils() {
		// Do not instantiate this.
	}

	/** Application logic object. */
	private static AbstractApplicationLogic appLogic;

	/** Application context. There can be only one */
	private static ApplicationContext appContext;

	/**
	 * @return The application logic object.
	 */
	public static synchronized AbstractApplicationLogic getApplicationLogic() {

		if (appLogic == null) {

			try {
				appLogic = (AbstractApplicationLogic) Class.forName("fr.logica.application.logic.ApplicationLogic").newInstance();
			} catch (InstantiationException e) {
				LOGGER.error("Error", e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Error", e);
			} catch (ClassNotFoundException e) {
				LOGGER.info("ApplicationLogic not found");
			}

			if (appLogic == null) {
				appLogic = new DefaultApplicationLogic();
			}
		}
		return appLogic;
	}

	public static synchronized ApplicationContext getApplicationContext() {
		if (appContext == null) {
			appContext = new ApplicationContext();
		}
		return appContext;
	}
}
