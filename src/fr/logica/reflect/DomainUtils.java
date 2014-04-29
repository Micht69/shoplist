package fr.logica.reflect;

import javax.faces.context.FacesContext;

import fr.logica.business.Constants;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.TechnicalException;
import fr.logica.jsf.controller.SessionController;
import fr.logica.jsf.utils.JSFBeanUtils;

public class DomainUtils {
	
	/** Attention, méthode assez lente : chargement du SessionController depuis le contexte à chaque appel. */
	private static SessionController getSessionCtrl() {
		return (SessionController) JSFBeanUtils.getManagedBean(FacesContext.getCurrentInstance(), "sessionCtrl");
	}

	/**
	 * Retourne la classe associée à la classe passée en paramêtre
	 * 
	 * @param Domain
	 *            object dont on veut la logique
	 * @return Instance de CustomClass
	 */
	public static DomainLogic<? extends Entity> getLogic(Entity entity) {
		return internalGetLogic(entity.getClass().getSimpleName());
	}

	/**
	 * Retourne la classe associée à la classe passée en paramètre
	 * 
	 * @param Nom
	 *            de l'entité dont on veut la customClass
	 * @return Instance de CustomClass
	 */
	public static DomainLogic<? extends Entity> getLogic(String entityName) {
		return internalGetLogic(entityName.substring(0, 1).toUpperCase() + entityName.substring(1));
	}

	/**
	 * Instancie une custom class, en cas d'erreur, retourne une CustomEntityDefault.
	 * 
	 * @param Nom
	 *            de la classe dont on veut la custom class correspondante.
	 * @return Instance de CustomClass, ou instance de CustomEntityDefault si une erreur est survenue.
	 */
	private static DomainLogic<? extends Entity> internalGetLogic(String className) {
		String fullClassName = Constants.DOMAIN_LOGIC_PACKAGE + "." + className + Constants.EXTENSION_LOGIC;
		try {

			SessionController sessionCtrl = getSessionCtrl();
			if (sessionCtrl != null && sessionCtrl.getDisableCustom() != null && sessionCtrl.getDisableCustom())
			{
				try {
					return (DomainLogic<?>) Class.forName("fr.logica.business.DefaultLogic").newInstance();
				} catch (Exception ex) {
					throw new TechnicalException("Impossible d'instancier la classe générique.", ex);
				}
			}

			return (DomainLogic<?>) Class.forName(fullClassName).newInstance();
			/* si l'objet custom n'existe pas, création d'un custom générique */
		} catch (InstantiationException e) {
			try {
				return (DomainLogic<?>) Class.forName("fr.logica.business.DefaultLogic").newInstance();
			} catch (Exception ex) {
				throw new TechnicalException("Impossible d'instancier la classe custom " + fullClassName, e);
			}
		} catch (IllegalAccessException e) {
			try {
				return (DomainLogic<?>) Class.forName("fr.logica.business.DefaultLogic").newInstance();
			} catch (Exception ex) {
				throw new TechnicalException("Impossible d'instancier la classe custom " + fullClassName, e);
			}
		} catch (ClassNotFoundException e) {
			try {
				return (DomainLogic<?>) Class.forName("fr.logica.business.DefaultLogic").newInstance();
			} catch (Exception ex) {
				throw new TechnicalException("Impossible d'instancier la classe custom " + fullClassName, e);
			}
		}
	}

	/**
	 * Create a new domain object of class domainName
	 * 
	 * @param domainName
	 *            domain object to instantiate
	 * @return a domainName instance
	 */
	public static Entity newDomain(String domainName) {

		if (domainName == null || "".equals(domainName)) {
			throw new TechnicalException("Domain object not found " + domainName);
		}
		String className = Constants.DOMAIN_OBJECT_PACKAGE + "." + domainName.substring(0, 1).toUpperCase()
				+ domainName.substring(1);

		try {
			return (Entity) Class.forName(className).newInstance();

		} catch (InstantiationException e) {
			throw new TechnicalException("Domain object not found " + domainName);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Domain object not found " + domainName);
		} catch (ClassNotFoundException e) {
			throw new TechnicalException("Domain object not found " + domainName);
		}
	}

	/**
	 * Création du nom de classe ou d'attribut java à partir du nom de la table
	 * 
	 * @param name
	 * @param isClass
	 * @return
	 */
	public static String createJavaName(String name, boolean isClass) {
		char[] strLower = name.toLowerCase().toCharArray();
		char[] strUpper = name.toUpperCase().toCharArray();
		char[] result = new char[name.length()];
		int j = 1;

		if (isClass) {
			result[0] = strUpper[0];
		} else {
			result[0] = strLower[0];
		}

		boolean toUpper = false;
		for (int i = 1; i < strLower.length; i++) {
			if ((strLower[i] == '_' || strLower[i] == ' ')) {
				toUpper = true;
				continue;
			}

			if (toUpper) {
				result[j] = strUpper[i];
				toUpper = false;
			} else {
				result[j] = strLower[i];
			}
			j++;
		}
		return String.valueOf(result).trim();
	}

	/**
	 * Création du nom de la table ou du champ à partir du nom passé en paramètre
	 * 
	 * @param name
	 * @return
	 */
	public static String createDbName(String name) {
		char[] strName = name.toCharArray();
		char[] strUpper = name.toUpperCase().toCharArray();
		char[] result = new char[name.length() * 2];
		int j = 0;

		for (int i = 0; i < strName.length; i++) {

			if (Character.isUpperCase(strName[i]) && i > 0) {
				result[j] = '_';
				j++;
			}

			result[j] = strUpper[i];

			j++;
		}
		return String.valueOf(result).trim();
	}

}
