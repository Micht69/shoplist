package fr.logica.business;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cette classe contient l'ensemble des méta-données d'une Entité. (Nom, champs, types de données, clés, liens, etc.).
 * 
 * @author bellangerf
 * 
 */
public abstract class EntityModel {

	public abstract String name();

	public abstract String dbName();

	/**
	 * Retrieves the database schema where the table is located.
	 * @return A schema name or an empty string.
	 */
	public abstract String getDbSchemaName();

	public abstract KeyModel getKeyModel();

	protected EntityModel getEntityModel() {
		return EntityManager.getEntityModel(name());
	}

	/**
	 * Get a Foreign Key by it's name
	 * 
	 * @param keyName : the name of the FK
	 * @return the ForeignKeyModel
	 */
	public abstract ForeignKeyModel getForeignKeyModel(String keyName);

	/**
	 * Get a Link by it's name
	 * 
	 * @param linkName : the name of the link
	 * @return the LinkModel
	 */
	public abstract LinkModel getLinkModel(String linkName);

	/**
	 * Get a Back-Link by it's name
	 * 
	 * @param linkName : the name of the back-link
	 * @return the LinkModel
	 */
	public abstract LinkModel getBackRefModel(String linkName);

	/**
	 * Get all the links (direct and backref) names
	 * 
	 * @return a list of link names
	 */
	public abstract List<String> getAllLinkNames();

	/**
	 * Get all the links (direct only) names
	 * 
	 * @return a list of link names
	 */
	public abstract List<String> getLinkNames();

	/**
	 * Get all the links (backref only) names
	 * 
	 * @return a list of link names
	 */
	public abstract List<String> getBackRefNames();

	/**
	 * Get the field definition for the given name
	 * 
	 * @param name : the name of the field
	 * @return an EntityField
	 */
	public abstract EntityField getField(String name);

	/**
	 * Get all the fields names
	 * 
	 * @return a set of field names
	 */
	public abstract Set<String> getFields();

	/**
	 * Check if a field is of type AutoIncrement
	 * 
	 * @param name : the name of the field
	 * @return true if AutoIncrement, false otherwise
	 */
	public abstract boolean isAutoIncrementField(String name);

	/**
	 * Get all the actions for this entity
	 * 
	 * @return a Collection of Action
	 */
	public abstract Collection<Action> getActions();

	/**
	 * Get an action by it's code
	 * 
	 * @param code : the code of the action
	 * @return the Action
	 */
	public abstract Action getAction(int code);

	/**
	 * Get fields used by search queries.
	 * 
	 * @return A set of field names.
	 */
	public abstract Set<String> getLookupFields();

	public Map<String, Object> enumValues(String enumName) {
		// On utilise une LinkedHashMap uniquement pour le style, parce que personne ne le fait jamais.
		// Et accessoirement, ça conserve l'ordre d'entrée dans la map.

		// On va utiliser l'ordre pour mettre les valeurs à null en premier, comme ça, on aura l'impression que ce truc marche correctement
		// lorsque "null" est une valeur possible pour les données. Si la valeur de la variable est "null", le tag JSF selectOneValue va se
		// placer sur le premier élément de la liste même si c'est le 3° élément qui correspond à la valeur "null".
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String realEnumName = enumName.substring(0, 1).toLowerCase() + enumName.substring(1);
		for (int i = 0; i < getField(realEnumName).getValues().size(); i++) {
			if (getField(realEnumName).getValues().get(i) == null) {
				map.put(MessageUtils.getInstance().getGenLabel(getField(realEnumName).getLabels().get(i), null), getField(realEnumName)
						.getValues().get(i));
			}
		}
		for (int i = 0; i < getField(realEnumName).getValues().size(); i++) {
			if (getField(realEnumName).getValues().get(i) != null) {
				map.put(MessageUtils.getInstance().getGenLabel(getField(realEnumName).getLabels().get(i), null), getField(realEnumName)
						.getValues().get(i));
			}
		}
		return map;
	}

	/**
	 * Is this entity an associative entity
	 * 
	 * @return true|false
	 */
	public boolean isAssociative() {
		return false;
	}

	/**
	 * Get the associative link name by it's name
	 * 
	 * @param linkName : the name of the link
	 * @return null if the entity isn't associative
	 */
	public String getAssociatedLink(String linkName) {
		if (!isAssociative()) {
			return null;
		}
		for (String lName : getLinkNames()) {
			if (!linkName.equals(lName)) {
				return lName;
			}
		}
		return null;
	}

	/**
	 * Check is the given key is a Strong key<br>
	 * (ie all fields are mandatory)
	 * 
	 * @param keyName : the name of the key
	 * @return true if all fields are mandatory, false otherwise
	 * @throws TechnicalException if the keyName doen't belong to this entity
	 */
	public boolean isStrongKey(String keyName) {
		if (getForeignKeyModel(keyName) == null) {
			throw new TechnicalException("Key " + keyName + " is not a key of entity " + name());
		}
		for (String field : getForeignKeyModel(keyName).getFields()) {
			if (!getField(field).isMandatory()) {
				return false;
			}
		}
		return true;
	}

	public boolean isVirtualLink(String linkName) {
		LinkModel linkModel = getLinkModel(linkName);
		ForeignKeyModel keyModel = getForeignKeyModel(linkModel.getKeyName());

		for (String field : keyModel.getFields()) {
			if (getField(field).isTransient()) {
				return true;
			}
		}
		return false;
	}

	public String getBackRefEntityName(String linkName) {
		String brName = getBackRefModel(linkName).getEntityName();
		EntityModel brMdl = EntityManager.getEntityModel(brName);
		if (brMdl.isAssociative()) {
			brName = brMdl.getLinkModel(brMdl.getAssociatedLink(linkName)).getRefEntityName();
		}
		return brName;
	}

	public boolean isAssociativeLink(String linkName) {
		LinkModel brModel = getBackRefModel(linkName);
		if (brModel != null) {
			return EntityManager.getEntityModel(brModel.getEntityName()).isAssociative();
		}
		return false;
	}

	public boolean isExternal() {
		return false;
	}

}
