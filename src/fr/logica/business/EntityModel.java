package fr.logica.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.Process;
import fr.logica.business.Action.UserInterface;
import fr.logica.domain.annotations.EntityDef;

/**
 * This class contains all metadata from an entity (fields, links, ...).<br/>
 * This class is instanciated on application start using entity annotations.
 * 
 * @author CGI
 */
public class EntityModel {

	/** Table name for this entity or REST class name for external entity */
	private String dbTableName;

	/** Database schema name. */
	private String dbSchemaName;

	/** Name for this entity */
	private String entityName;

	/** Is an associative entity */
	private boolean isAssociative = false;

	/** Is an external entity */
	private boolean isExternal = false;

	/** Name of the sequence */
	private String sequenceName;

	/** PK definition */
	private KeyModel primaryKeyModel;
	/** Constraints definitions */
	// TODO : private Map<String, ForeignKeyModel> constraints;
	/** Links definitions */
	private Map<String, LinkModel> links;
	/** Back-Links definitions */
	private Map<String, LinkModel> backRefs;
	/** Entity fields definitions */
	private Map<String, EntityField> fields;
	/** Entity autoincrement-fields definitions */
	private Set<String> autoIncrementFields;
	/** Entity lookup fields definitions */
	private Set<String> lookupFields;
	/** Entity actions definitions */
	private Map<Integer, Action> actions;
	
	public EntityModel() {
		primaryKeyModel = new KeyModel();
		primaryKeyModel.setUnique(true);
		//foreignKeys = new HashMap<String, ForeignKeyModel>();
		links = new HashMap<String, LinkModel>();
		backRefs = new HashMap<String, LinkModel>();
		fields = new HashMap<String, EntityField>();
		autoIncrementFields = new HashSet<String>();
		lookupFields = new HashSet<String>();
		actions = new HashMap<Integer, Action>();
	}
	
	EntityModel(String entityName, EntityDef entityDef) {
		this();
		this.entityName = entityName;
		this.dbTableName = entityDef.dbName();
		this.dbSchemaName = entityDef.schemaId();
		this.sequenceName = entityDef.sequenceName();
		this.isAssociative = entityDef.isAssociative();
		this.isExternal = entityDef.isExternal();
		
		this.primaryKeyModel.setFields(Arrays.asList(entityDef.primaryKey()));
	}

	/**
	 * Retreive the name of the entity
	 */
	public String name() {
		return entityName;
	}

	/**
	 * Retreive the name of the entity DB table
	 */
	public String dbName() {
		return dbTableName;
	}

	/**
	 * Retrieves the database schema where the table is located.
	 * 
	 * @return A schema name or an empty string.
	 */
	public String getDbSchemaName() {
		return dbSchemaName;
	}

	/**
	 * Retrieves the entity primary key meta model
	 * 
	 * @return primary key model
	 */
	public KeyModel getKeyModel() {
		return primaryKeyModel;
	}

	protected EntityModel getEntityModel() {
		return EntityManager.getEntityModel(name());
	}

	/**
	 * Get a Link by it's name
	 * 
	 * @param linkName
	 *            : the name of the link
	 * @return the LinkModel
	 */
	public LinkModel getLinkModel(String linkName) {
		return links.get(linkName);
	}

	/**
	 * Get a Back-Link by it's name
	 * 
	 * @param linkName
	 *            : the name of the back-link
	 * @return the LinkModel
	 */
	public LinkModel getBackRefModel(String linkName) {
		return backRefs.get(linkName);
	}

	/**
	 * Get all the links (direct and backref) names
	 * 
	 * @return a list of link names
	 */
	public List<String> getAllLinkNames() {
		List<String> linkNames = new ArrayList<String>();
		linkNames.addAll(links.keySet());
		linkNames.addAll(backRefs.keySet());
		return linkNames;
	}

	/**
	 * Get all the links (direct only) names
	 * 
	 * @return a list of link names
	 */
	public List<String> getLinkNames() {
		return new ArrayList<String>(links.keySet());
	}

	/**
	 * Get all the links (backref only) names
	 * 
	 * @return a list of link names
	 */
	public List<String> getBackRefNames() {
		return new ArrayList<String>(backRefs.keySet());
	}

	/**
	 * Get the field definition for the given name
	 * 
	 * @param name
	 *            : the name of the field
	 * @return an EntityField
	 */
	public EntityField getField(String fieldname) {
		return fields.get(fieldname);
	}

	/**
	 * Get all the fields names
	 * 
	 * @return a set of field names
	 */
	public Set<String> getFields() {
		return fields.keySet();
	}

	/**
	 * Check if a field is of type AutoIncrement
	 * 
	 * @param name
	 *            : the name of the field
	 * @return true if AutoIncrement, false otherwise
	 */
	public boolean isAutoIncrementField(String name) {
		return autoIncrementFields.contains(name);
	}

	/**
	 * Get all the actions for this entity
	 * 
	 * @return a Collection of Action
	 */
	public Collection<Action> getActions() {
		return actions.values();
	}

	/**
	 * Get an action by it's code
	 * 
	 * @param code
	 *            : the code of the action
	 * @return the Action
	 */
	public Action getAction(int code) {
		return actions.get(code);
	}

	/**
	 * Get fields used by search queries.
	 * 
	 * @return A set of field names.
	 */
	public Set<String> getLookupFields() {
		return lookupFields;
	}

	public Map<String, Object> enumValues(String enumName, Locale l) {
		// On utilise une LinkedHashMap uniquement pour le style, parce que personne ne le fait jamais.
		// Et accessoirement, ça conserve l'ordre d'entrée dans la map.

		// On va utiliser l'ordre pour mettre les valeurs à null en premier, comme ça, on aura l'impression que ce truc marche correctement
		// lorsque "null" est une valeur possible pour les données. Si la valeur de la variable est "null", le tag JSF selectOneValue va se
		// placer sur le premier élément de la liste même si c'est le 3° élément qui correspond à la valeur "null".
		String realEnumName = enumName.substring(0, 1).toLowerCase() + enumName.substring(1);
		EntityField entityField = getField(realEnumName);

		int nbDefVal = entityField.nbDefinedValues();
		Map<String, Object> mapNull = new LinkedHashMap<String, Object>(nbDefVal);
		Map<String, Object> mapNotNull = new LinkedHashMap<String, Object>(nbDefVal);
		for (int i = 0; i < nbDefVal; i++) {
			Object val = entityField.getDefValValue(i);
			if (val == null) {
				mapNull.put(MessageUtils.getInstance(l).getGenLabel(entityField.getDefValLabel(i), (Object[]) null), val);
			} else {
				mapNotNull.put(MessageUtils.getInstance(l).getGenLabel(entityField.getDefValLabel(i), (Object[]) null), val);
			}
		}

		// Contact maps
		mapNull.putAll(mapNotNull);

		return mapNull;
	}

	/**
	 * Is this entity an associative entity
	 * 
	 * @return true|false
	 */
	public boolean isAssociative() {
		return isAssociative;
	}

	/**
	 * Get the associative link name by it's name
	 * 
	 * @param linkName
	 *            : the name of the link
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
	 * Check is the given link key is a Strong key<br>
	 * (ie all fields are mandatory)
	 * 
	 * @param linkName
	 *            : the name of the link
	 * @return true if all fields are mandatory, false otherwise
	 * @throws TechnicalException
	 *             if the linkName doesn't belong to this entity
	 */
	public boolean isStrongKey(String linkName) {
		// FIXME : Rework this with constraints ?
		LinkModel linkModel = getLinkModel(linkName);
		if (linkModel == null) {
			throw new TechnicalException("Link " + linkName + " is not a link of entity " + name());
		}
		for (String field : linkModel.getFields()) {
			if (!getField(field).isMandatory()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check is the given link is a Virtual<br>
	 * (ie one of the fields is transient)
	 * 
	 * @param linkName
	 *            : the name of the link
	 * @return true if one field is transient, false otherwise
	 * @throws TechnicalException
	 *             if the linkName doesn't belong to this entity
	 */
	public boolean isVirtualLink(String linkName) {
		LinkModel linkModel = getLinkModel(linkName);
		if (linkModel == null) {
			throw new TechnicalException("Link " + linkName + " is not a link of entity " + name());
		}
		for (String field : linkModel.getFields()) {
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
		return isExternal;
	}

	/**
	 * Returns this entity's sequence name for auto-increment
	 * @return Sequence name if any, null otherwise
	 */
	public String getSequenceName() { 
		return sequenceName; 
	}

	/**
	 * Add a new link to the model (used only for internal init)
	 */
	void addNewLink(LinkModel linkModel) {
		// Create link
		links.put(linkModel.getLinkName(), linkModel);
	}
	/**
	 * Add a new action to the model (used only for internal init)
	 */
	void addNewAction(int code, String queryName, String pageName, int pNext, Input input,
			Persistence persistence, UserInterface ui, Process process, int[] pSubActions) {
		Integer next = null;
		if (pNext != -1)
			next = Integer.valueOf(pNext);
		
		Action action = null;
		if (pSubActions.length == 0) {
			action = new Action(code, queryName, pageName, next, input, persistence, ui, process);
		} else {
			Integer[] subActions = new Integer[pSubActions.length];
			for (int i=0; i<pSubActions.length; i++) {
				subActions[i] = Integer.valueOf(pSubActions[i]);
			}
			action = new Action(code, queryName, pageName, next, input, persistence, ui, process, subActions);
		}
		actions.put(code, action);
	}
	
	/**
	 * Add a new field to the model (used only for internal init)
	 */
	void addNewField(String fieldName, EntityField field, boolean isLookupField, boolean isAutoIncrementField) {
		fields.put(fieldName, field);
		
		if (isLookupField)
			lookupFields.add(fieldName);
		
		if (isAutoIncrementField)
			autoIncrementFields.add(fieldName);
	}

	/**
	 * Add a new backref to the model (used only for internal init)
	 */
	public void addNewBackRef(LinkModel linkModel) {
		backRefs.put(linkModel.getLinkName(), linkModel);
	}
}
