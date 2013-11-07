package fr.logica.business;

import java.util.HashMap;
import java.util.Map;

public class EntityManager {

	private final static Map<String, EntityModel> models;

	static {
		models = new HashMap<String, EntityModel>();
	}

	public static synchronized EntityModel getEntityModel(String entityName) {
		if (entityName == null || "".equals(entityName)) {
			return null;
		}
		if (models.get(entityName) == null) {
			String className = Constants.DOMAIN_MODELS_PACKAGE + "." + entityName.substring(0, 1).toUpperCase()
					+ entityName.substring(1) + Constants.EXTENSION_MODEL;
			try {
				models.put(entityName.substring(0, 1).toLowerCase() + entityName.substring(1), (EntityModel) Class
						.forName(className).newInstance());
			} catch (InstantiationException e) {
				throw new TechnicalException("Impossible d'instancier le Model de la classe " + entityName);
			} catch (IllegalAccessException e) {
				throw new TechnicalException("Impossible d'instancier le Model de la classe " + entityName);
			} catch (ClassNotFoundException e) {
				throw new TechnicalException("Impossible d'instancier le Model de la classe " + entityName);
			}
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
		EntityModel model = EntityManager.getEntityModel(entityName);

		Key fk = new Key(model.getForeignKeyModel(linkModel.getKeyName()));
		for (int i = 0; i < fk.getModel().getFields().size(); i++) {
			fk.setValue(fk.getModel().getFields().get(i),
					refPrimaryKey.getValue(refPrimaryKey.getModel().getFields().get(i)));
		}
		return fk;
	}

	public static Key buildForeignKey(String entityName, Key refPrimaryKey, String linkName) {
		EntityModel model = EntityManager.getEntityModel(entityName);
		LinkModel linkModel = model.getLinkModel(linkName);
		return buildForeignKey(entityName, refPrimaryKey, linkModel);
	}
}
