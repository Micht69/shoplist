package fr.logica.business;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import fr.logica.business.MessageUtils;
import fr.logica.business.context.RequestContext;
import fr.logica.reflect.DomainUtils;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;

public class MessageUtils {

	public static final String BUNDLE_PACKAGE = "fr.logica";
	/** The server bundle is not internationalized. */
	private static ResourceBundle bundleServer = ResourceBundle.getBundle(BUNDLE_PACKAGE + ".server");
	/** Map of internationalized bundles. */
	private static Map<Locale, MessageUtils> messageUtils = new HashMap<Locale, MessageUtils>();
	/** Available languages */
	private static Set<Locale> availableLanguages;

	/**
	 * This method lists files in the BUNDLE_PACKAGE and looks for labels_*.properties files. For each available files, it creates a
	 * corresponding Locale object and adds it to the set of available languages. If the labels.properties files, the locale Locale.getDefault()
	 * will be added.
	 * 
	 * @return Available languages for labels.
	 */
	public static synchronized Set<Locale> getAvailableLanguages() {
		if (availableLanguages == null) {
			availableLanguages = new HashSet<Locale>();

			String availableLanguagesString = getServerProperty("available.languages");
			if (availableLanguagesString == null || "".equals(availableLanguagesString)) {
				availableLanguages.add(Locale.getDefault());
			} else {
				String[] langs = availableLanguagesString.split(",");
				for (String lang : langs) {
					availableLanguages.add(new Locale(lang.trim()));
				}
			}
		}
		return availableLanguages;
	}

	/**
	 * Gets an instance of MessageUtils for the current user depending on its current locale. Current locale is determined by ApplicationLogic
	 * behavior.
	 * 
	 * @param ctx Current request context
	 * @return An instance of MessageUtils based on current user locale if any, default JVM locale otherwise.
	 */
	public static MessageUtils getInstance(RequestContext ctx) {
		Locale l;
		if (ctx == null || ctx.getSessionContext() == null) {
			l = Locale.getDefault();
		} else {
			l = ctx.getSessionContext().getLocale();
		}
		return getInstance(l);
	}

	/**
	 * Gets an instance of MessageUtils for the specified locale.
	 * 
	 * @param l
	 *            Locale to use. If null, Locale.getDefault() will be used
	 * @return An instance of MessageUtils based on specified locale if any, default JVM locale otherwise.
	 */
	public static MessageUtils getInstance(Locale l) {
		if (l == null) {
			l = Locale.getDefault();
		}
		if (messageUtils.get(l) == null) {
			MessageUtils instance = new MessageUtils(l);
			messageUtils.put(l, instance);
		}
		return messageUtils.get(l);
	}

	private Hashtable<String, ResourceBundle> bundles = null;

	/**
	 * Constructor for a MessageUtils instance. Loads all bundles for locale l.
	 * 
	 * @param l
	 *            Locale to use for bundle loading.
	 */
	public MessageUtils(Locale l) {
		ResourceBundle.clearCache();
		bundles = new Hashtable<String, ResourceBundle>();
		bundles.put("messages", ResourceBundle.getBundle(BUNDLE_PACKAGE + ".messages", l));
		bundles.put("genLabels", ResourceBundle.getBundle("fr.logica.genlabels", l));
		bundles.put("genTooltips", ResourceBundle.getBundle(BUNDLE_PACKAGE + ".gentooltips", l));
		bundles.put("tooltips", ResourceBundle.getBundle(BUNDLE_PACKAGE + ".tooltips", l));
		bundles.put("labels", ResourceBundle.getBundle("fr.logica.labels", l));
		bundles.put("custom", ResourceBundle.getBundle(BUNDLE_PACKAGE + ".custom", l));
	}

	/**
	 * Récupère une valeur dans le bundle "messages".
	 * 
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	public String getMessage(String key, Object... params) {
		return getString("messages", key, params);
	}

	/**
	 * Récupère le titre d'un couple entité / action.
	 * 
	 * @param entityName
	 *            le nom de la liste
	 * @param action
	 *            le N° de l'action
	 * @return le titre
	 */
	public String getTitle(String entityName, int action) {
		return getGenLabel(DomainUtils.createDbName(entityName) + "_ACTION_" + action, (Object[]) null);
	}

	/**
	 * Récupère le titre d'un couple entité / variable.
	 * 
	 * @param entityName
	 *            le nom de la liste
	 * @param varName
	 *            le nom de la variable
	 * @return le titre
	 */
	public String getVarTitle(String entityName, String varName) {
		return getGenLabel(entityName + "." + varName, (Object[]) null);
	}

	/**
	 * Récupère le titre d'un couple query / variable.
	 * 
	 * @param queryName
	 *            le nom de la liste
	 * @param alias
	 *            l' alias de la table
	 * @param varName
	 *            le nom de la variable
	 * @return le titre
	 */
	public String getQryVarTitle(String queryName, String alias, String varName) {
		return getGenLabel(queryName + "." + alias + "." + varName, (Object[]) null);
	}

	/**
	 * Récupère le titre d'un liste.
	 * 
	 * @param listName
	 *            le nom de la liste
	 * @return le titre de la liste
	 */
	public String getListTitle(String listName) {
		return getGenLabel(listName + "_LIST", (Object[]) null);
	}

	/**
	 * Récupère une valeur dans le bundle "genLabels".
	 * 
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	public String getGenLabel(String key, Object... params) {
		return getString("genLabels", key, params);
	}

	/**
	 * Récupère une valeur dans le bundle "labels".
	 * 
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	public String getLabel(String key, Object... params) {
		return getString("labels", key, params);
	}

	/**
	 * Récupère une valeur dans le bundle "genLabels".
	 * 
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @param emptyStringForMissingResource
	 *            true si on veut récupérer une chaine vide quand le libellé n'existe pas
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	public String getGenLabel(String key, Object[] params, boolean emptyStringForMissingResource) {
		return getString("genLabels", key, params, emptyStringForMissingResource);
	}

	/**
	 * Récupère une valeur dans le bundle "custom".
	 * 
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	public String getCustom(String key, Object... params) {
		return getString("custom", key, params);
	}

	/**
	 * Récupère une valeur dans le bundle passé.
	 * 
	 * @param bundle
	 *            le ResourceBundle associé
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	private String getString(String bundleName, String key, Object params[]) {
		return getString(bundleName, key, params, false);
	}

	/**
	 * Récupère une valeur dans le bundle passé.
	 * 
	 * @param bundle
	 *            le ResourceBundle associé
	 * @param key
	 *            la clé de la valeur
	 * @param params
	 *            les paramètres à intégrer
	 * @param emptyStringForMissingResource
	 *            true si on doit renvoyer une chaine vide si la variable n'existe pas
	 * @return la valeur associée à la clé avec fusion des params si non null
	 */
	private String getString(String bundleName, String key, Object params[], boolean emptyStringForMissingResource) {
		// récupération du bon bundle
		ResourceBundle bundle = bundles.get(bundleName);

		String text = null;
		try {
			text = bundle.getString(key);
		} catch (MissingResourceException e) {
			text = "Clé " + key + " non trouvée.";
			if (emptyStringForMissingResource || "tooltips".equals(bundleName) || "genTooltips".equals(bundleName)) {
				text = "";
			}
		}

		MessageFormat mf = new MessageFormat(text);
		text = mf.format(params, new StringBuffer(), null).toString();

		return text;
	}

	/**
	 * Renvoi le libellé dans le bundle spcédifié.<br>
	 * Utilisé dans la function pour l'affichage dans JSF.
	 * 
	 * @param bundle
	 *            le nom du bundle
	 * @param key
	 *            la clé
	 * @return le libellé du bundle <b>ou</b> '???'++bundle+':'+key+'???' si le bundle n'existe pas
	 */
	public String getXhtmlLabel(String bundle, String key) {

		if ("labels".equalsIgnoreCase(bundle)) {
			return getLabel(key, (Object[]) null);
		} else if ("genLabels".equalsIgnoreCase(bundle)) {
			return getGenLabel(key, (Object[]) null);
		} else if ("messages".equalsIgnoreCase(bundle)) {
			return getMessage(key, (Object[]) null);
		} else if ("titles".equalsIgnoreCase(bundle)) {
			return getGenLabel(key, (Object[]) null);
		} else if ("tooltips".equalsIgnoreCase(bundle)) {
			String ret = getString("tooltips", key, null);
			if ("".equals(ret)) {
				ret = getString("genTooltips", key, null);
			}
			return ret;
		} else if ("custom".equalsIgnoreCase(bundle)) {
			return getCustom(key, (Object[]) null);
		}

		return "???" + bundle + ":" + key + "???";
	}

	/**
	 * Récupère la valeur d'une propriété dans le fichier server.properties.
	 * 
	 * @param key
	 *            String
	 * @return String
	 */
	public static String getServerProperty(String key) {
		try {
			return bundleServer.getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	public static Message addStringMessage(String message) {
		return new Message(message, Severity.INFO);
	}

	public static Message addStringErrorMessage(String message) {
		return new Message(message, Severity.ERROR);
	}

	public static Message addExternalizedErrorMessage(String key, Object[] params, RequestContext ctx) {
		return addStringErrorMessage(getInstance(ctx).getCustom(key, params));
	}

	public static Message addExternalizedErrorMessage(String key, RequestContext ctx) {
		return addStringErrorMessage(getInstance(ctx).getCustom(key, (Object[]) null));
	}

	public static Message addExternalizedMessage(String key, Object[] params, RequestContext ctx) {
		return addStringMessage(getInstance(ctx).getCustom(key, params));
	}

	public static Message addExternalizedMessage(String key, RequestContext ctx) {
		return addStringMessage(getInstance(ctx).getCustom(key, (Object[]) null));
	}
}
