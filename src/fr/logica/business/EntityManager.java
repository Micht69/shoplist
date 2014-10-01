package fr.logica.business;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.logica.application.ApplicationDescriptor;
import fr.logica.domain.annotations.EntityDef;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EntityManager {

	private final static Map<String, EntityModel> models;

	static {
		models = new HashMap<String, EntityModel>();

		// Load all models
		Map<String, LinkModel> backRefs = new HashMap<String, LinkModel>();
		for (String entityName : ApplicationDescriptor.getDomains()) {
			// Class name is same as entity name with upper first letter
			String entityClassName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

			try {
				Class entityClass = Class.forName(Constants.DOMAIN_OBJECT_PACKAGE + "." + entityClassName);
				// Get main annotation
				EntityDef entityDef = (EntityDef) entityClass.getAnnotation(EntityDef.class);
				if (entityDef != null) {
					// Init EntityModel with class properties
					EntityModel entityModel = new EntityModel(entityName, entityDef);

					// Get links
					fr.logica.domain.annotations.Links entityLinks = (fr.logica.domain.annotations.Links) entityClass
							.getAnnotation(fr.logica.domain.annotations.Links.class);
					for (fr.logica.domain.annotations.Link link : entityLinks.value()) {
						// Add Link to model
						LinkModel linkModel = new LinkModel(link.name());
						linkModel.setEntityName(entityName);
						linkModel.setRefEntityName(link.targetEntity());
						linkModel.setFields(Arrays.asList(link.fields()));
						entityModel.addNewLink(linkModel);

						// Save backRef for later
						backRefs.put(link.name(), linkModel);
					}

					// Get actions
					fr.logica.domain.annotations.Actions entityActions = (fr.logica.domain.annotations.Actions) entityClass
							.getAnnotation(fr.logica.domain.annotations.Actions.class);
					for (fr.logica.domain.annotations.Action action : entityActions.value()) {
						String queryName = action.queryName();
						if ("".equals(queryName)) {
							// If no query, we should use null value
							queryName = null;
						}
						String pageName = action.pageName();
						if ("".equals(pageName)) {
							// No page name, get the default one
							pageName = getPageName(entityName);
						}

						// Add the action to the model
						entityModel.addNewAction(
								action.code(),
								queryName,
								pageName,
								action.nextAction(),
								action.input(),
								action.persistence(),
								action.ui(),
								action.process(),
								action.subActions());
					}

					// Get fields
					for (Field classField : entityClass.getDeclaredFields()) {
						fr.logica.domain.annotations.EntityField entityField = classField
								.getAnnotation(fr.logica.domain.annotations.EntityField.class);
						if (entityField != null) {
							// Its a defined field
							String fieldName = classField.getName();
							String className = classField.getType().getName();
							// Create field from annotation values
							EntityField field = new EntityField(entityField.sqlName(),
									entityField.sqlType(),
									entityField.sqlSize(),
									entityField.sqlAccuracy(),
									entityField.memory(),
									entityField.isMandatory(),
									entityField.isLookupField());
							if (!"".equals(entityField.sqlExpr())) {
								field.setSqlExpr(entityField.sqlExpr());
							}
							boolean hasDefaultValue = false;
							if (!"$-$".equals(entityField.defaultValue())) {
								field.setDefaultValue(convert(entityField.defaultValue(), className));
								hasDefaultValue = true;
							}

							// Add field to model
							entityModel.addNewField(fieldName,
									field,
									entityField.isLookupField(),
									entityField.isAutoIncrementField());

							// Check for defined values
							fr.logica.domain.annotations.DefinedValues definedValues = classField
									.getAnnotation(fr.logica.domain.annotations.DefinedValues.class);
							if (definedValues != null) {
								// Field as defined value

								for (fr.logica.domain.annotations.DefinedValue definedValue : definedValues.value()) {
									Object val = convert(definedValue.value(), className);
									field.getDefinedValues().add(new DefinedValue(definedValue.code(), definedValue.label(), val));
								}

								if (!hasDefaultValue) {
									throw new TechnicalException("Field " + fieldName + " for entity " + entityName
											+ " has defined values but no default one !");
								}
							}
						}
					}

					models.put(entityName, entityModel);
				}
			} catch (ClassNotFoundException e) {
				throw new TechnicalException("Impossible de trouver la classe " + entityName, e);
			}
		}

		// Store BackRefs
		for (Map.Entry<String, LinkModel> backRef : backRefs.entrySet()) {
			LinkModel linkModel = backRef.getValue();
			EntityModel entityModel = models.get(linkModel.getRefEntityName());

			// Add Link to model
			entityModel.addNewBackRef(linkModel);
		}
	}

	/**
	 * Convert entityName to a page name. (internal use only)
	 */
	private static String getPageName(String entityName) {
		if (entityName == null || "".equals(entityName))
			return entityName;

		char[] strName = entityName.toCharArray();
		char[] strUpper = entityName.toUpperCase().toCharArray();
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < strName.length; i++) {
			if (Character.isUpperCase(strName[i]) && i > 0) {
				result.append('_');
			}

			result.append(strUpper[i]);
		}

		return result.toString();
	}

	/**
	 * Convert the given String to the correct Object type. (internal use only)
	 */
	private static Object convert(String from, String className) {
		if (from == null || "".equals(from))
			return null;

		if (from.startsWith("*") || from.equalsIgnoreCase("NOW")) {
			// Value is a special keywork (*BLANK, *NOW, *TODAY, *TYPE)
			return from;
		}

		try {
			if (String.class.getName().equals(className)) {
				return from;
			} else if (Date.class.getName().equals(className)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				return sdf.parse(from);
			} else if (Timestamp.class.getName().equals(className)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				return new Timestamp(sdf.parse(from).getTime());
			} else if (Time.class.getName().equals(className)) {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				return new Time(sdf.parse(from).getTime());
			} else if (Integer.class.getName().equals(className)) {
				return Integer.valueOf(from);
			} else if (Long.class.getName().equals(className)) {
				return Long.valueOf(from);
			} else if (BigDecimal.class.getName().equals(className)) {
				return BigDecimal.valueOf(Double.valueOf(from));
			} else if (Boolean.class.getName().equals(className)) {
				return "true".equalsIgnoreCase(from);
			}
		} catch (ParseException pe) {
			return null;
		}

		return from; // Should not happen
	}

	/**
	 * Get the EntityModel class for the given model name.<br/>
	 * EntityModels are loaded from entity's annotations upon application start.
	 * 
	 * @param entityName
	 *            the name of the entity
	 * @return the current entityModel
	 * @throws TechnicalException
	 *             if entity model not found
	 */
	public static synchronized EntityModel getEntityModel(String entityName) {
		if (entityName == null || "".equals(entityName)) {
			return null;
		}
		if (models.get(entityName) == null) {
			throw new TechnicalException("Impossible de récupérer le Model de la classe " + entityName);
		}
		return models.get(entityName);
	}

	/**
	 * Construit une clé étrangère de l'entité à partir d'une clé primaire d'une entité liée.
	 * 
	 * @param refPrimaryKey
	 *            Clé primaire d'une autre entité liée à l'entité courante.
	 * @return Clé étrangère avec les mêmes valeurs que refPrimaryKey.
	 */
	public static Key buildForeignKey(String entityName, Key refPrimaryKey, LinkModel linkModel) {
		Key fk = new Key(linkModel);
		for (int i = 0; i < fk.getModel().getFields().size(); i++) {
			fk.setValue(fk.getModel().getFields().get(i),
					refPrimaryKey.getValue(refPrimaryKey.getModel().getFields().get(i)));
		}
		return fk;
	}

	public static Key buildForeignKey(String entityName, Key refPrimaryKey, String linkName) {
		EntityModel model = EntityManager.getEntityModel(entityName);
		LinkModel linkModel = model.getLinkModel(linkName);
		if (linkModel == null)
			linkModel = model.getBackRefModel(linkName);
		return buildForeignKey(entityName, refPrimaryKey, linkModel);
	}
}
