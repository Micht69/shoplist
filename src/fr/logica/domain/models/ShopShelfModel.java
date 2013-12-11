package fr.logica.domain.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.logica.business.Action;
import fr.logica.business.EntityField;
import fr.logica.business.EntityField.Memory;
import fr.logica.business.EntityModel;
import fr.logica.business.ForeignKeyModel;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.LinkModel;

/**
 * Model class for the entity ShopShelf
 *
 * @author CGI
 */
public class ShopShelfModel extends EntityModel implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Table name for this entity */
	public static final String ENTITY_DB_NAME = "SHELF";

	/** Name for this entity */
	public static final String ENTITY_NAME = "shopShelf";

	/** PK definition */
	private static final KeyModel PRIMARY_KEY_MODEL;

	/** FKs definitions */
	private static final Map<String, ForeignKeyModel> FOREIGN_KEY;

	/** Links definitions */
	private static final Map<String, LinkModel> LINK;

	/** Back-Links definitions */
	private static final Map<String, LinkModel> BACK_REF;

	/** Entity fields definitions */
	private static final Map<String, EntityField> FIELDS;

	/** Entity autoincrement-fields definitions */
	private static final Set<String> AUTOINCREMENT_FIELDS;

	/** Entity actions definitions */
	private static final Map<Integer, Action> ACTIONS;
	
	static {
		// PK for this entity
		PRIMARY_KEY_MODEL = new KeyModel();
		PRIMARY_KEY_MODEL.setName("shopShelfPk");
		PRIMARY_KEY_MODEL.getFields().add("code");

		
		FOREIGN_KEY = new HashMap<String, ForeignKeyModel>();

		ForeignKeyModel shopShelfPk = new ForeignKeyModel();
		shopShelfPk.setRefEntityName("shopShelf");
		shopShelfPk.setUnique(true);
		shopShelfPk.getFields().add("code");
		FOREIGN_KEY.put("shopShelfPk", shopShelfPk);


		LINK = new HashMap<String, LinkModel>();

		
		BACK_REF = new HashMap<String, LinkModel>();

		LinkModel brshopArticleLShelf = new LinkModel("shopArticleLShelf");
		brshopArticleLShelf.setEntityName("shopArticle");
		brshopArticleLShelf.setKeyName("shopArticleRShelf");
		brshopArticleLShelf.setRefEntityName("shopShelf");
		BACK_REF.put("shopArticleLShelf", brshopArticleLShelf);


		FIELDS = new HashMap<String, EntityField>();
		AUTOINCREMENT_FIELDS = new HashSet<String>();

		FIELDS.put("code", new EntityField("CODE", "VARCHAR2", 10, 0, Memory.NO, true, false, "Code"));
		FIELDS.put("name", new EntityField("NAME", "VARCHAR2", 100, 0, Memory.NO, true, false, "Nom"));
		FIELDS.put("position", new EntityField("POSITION", "INTEGER", 3, 0, Memory.NO, true, false, "Position"));
		FIELDS.put("w$Desc", new EntityField("W$_DESC", "VARCHAR2", 100, 0, Memory.SQL, false, false, "Description")); 
		FIELDS.get("w$Desc").setSqlExpr(":tableAlias.NAME");

		ACTIONS = new HashMap<Integer, Action>();
		ACTIONS.put(0, new Action(0, 0));
		ACTIONS.put(2, new Action(2, 2));
		ACTIONS.put(4, new Action(4, 4));
		ACTIONS.put(5, new Action(5, 5));

	}
	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(String code) {
		Key key = new Key(PRIMARY_KEY_MODEL);
		key.setValue("code", code);

		return key;
	}

	/** Name of the entity */
	@Override
	public String $_getName() {
		return ENTITY_NAME;
	}
	
	/** Name of the entity DB table */
	@Override
	public String $_getDbName() {
		return ENTITY_DB_NAME;
	}

	@Override
	public ForeignKeyModel getForeignKeyModel(String keyName) {
		return FOREIGN_KEY.get(keyName);
	}
	@Override
	public KeyModel getKeyModel() {
		return PRIMARY_KEY_MODEL;
	}
	@Override
	public LinkModel getLinkModel(String linkName) {
		return LINK.get(linkName);
	}
	@Override
	public LinkModel getBackRefModel(String linkName) {
		return BACK_REF.get(linkName);
	}
	@Override
	public List<String> getAllLinkNames() {
		List<String> linkNames = new ArrayList<String>();
		linkNames.addAll(LINK.keySet());
		linkNames.addAll(BACK_REF.keySet());
		return linkNames;
	}
	@Override
	public List<String> getLinkNames() {
		return new ArrayList<String>(LINK.keySet());
	}
	@Override
	public List<String> getBackRefNames() {
		return new ArrayList<String>(BACK_REF.keySet());
	}
	@Override
	public EntityField getField(String name) {
		return FIELDS.get(name);
	}
	
	@Override
	public Set<String> getFields() {
		return FIELDS.keySet();
	}
	
	@Override 
	public Collection<Action> getActions() {
		return ACTIONS.values();
	}
	
	@Override 
	public Action getAction(int code) {
		return ACTIONS.get(code);
	}

	@Override
	public Set<String> getLookupFields() {
		Set<String> lookupFields = new HashSet<String>();

		for (Entry<String, EntityField> field : FIELDS.entrySet()) {

			if (field.getValue().isLookupField()) {
				lookupFields.add(field.getKey());
			}
		}
		return lookupFields;
	}


	/**
	 * Get field 'code'
	 * @return the EntityField
	 */
	public EntityField getCode() {
		return FIELDS.get("code");
	}

	/**
	 * Get field 'name'
	 * @return the EntityField
	 */
	public EntityField getName() {
		return FIELDS.get("name");
	}

	/**
	 * Get field 'position'
	 * @return the EntityField
	 */
	public EntityField getPosition() {
		return FIELDS.get("position");
	}

	/**
	 * Get field 'w$Desc'
	 * @return the EntityField
	 */
	public EntityField getW$Desc() {
		return FIELDS.get("w$Desc");
	}
	
	@Override
	public boolean isAutoIncrementField(String name) {
		return AUTOINCREMENT_FIELDS.contains(name);
	}


	/**
	 * Link from entity SHOP_ARTICLE to SHOP_SHELF
	 * Rayon
	 */ 
	public static final String LINK_SHOP_ARTICLE_L_SHELF = "shopArticleLShelf";

}
