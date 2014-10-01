package fr.logica.application;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

import fr.logica.business.Constants;
import fr.logica.domain.annotations.EntityDef;

/**
 * Application descriptor
 * 
 * @author CGI
 */
public class ApplicationDescriptor implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * Entities' name.
	 */
	public static final Set<String> domains;

	static {
		Reflections reflections = new Reflections(Constants.DOMAIN_OBJECT_PACKAGE);
		Set<Class<?>> entities = reflections.getTypesAnnotatedWith(EntityDef.class);

		domains = new HashSet<String>(entities.size());

		for (Class<?> clazz : entities) {
			String entityClassName = clazz.getSimpleName();
			String entityName = entityClassName.substring(0, 1).toLowerCase() + entityClassName.substring(1);
			domains.add(entityName);
		}
	}

	/**
	 * @return Entities' name.
	 */
	public static Set<String> getDomains() {
		return domains;
	}

}
