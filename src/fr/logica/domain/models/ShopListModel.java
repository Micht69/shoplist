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

import fr.logica.business.DateTimeUpgraded;
import fr.logica.business.DateUtils;
import java.util.Date;

import fr.logica.business.Action;
import fr.logica.business.EntityField.Memory;
import fr.logica.business.EntityModel;
import fr.logica.business.EntityField;
import fr.logica.business.ForeignKeyModel;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.LinkModel;
import fr.logica.business.MessageUtils;

/**
 * Model class for the entity ShopList
 *
 * @author CGI
 */
public class ShopListModel extends EntityModel implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Table name for this entity */
	public static final String ENTITY_DB_NAME = "list";

	/** Name for this entity */
	public static final String ENTITY_NAME = "shopList";

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
		PRIMARY_KEY_MODEL.setName("shopListPk");
		PRIMARY_KEY_MODEL.getFields().add("id");

		
		FOREIGN_KEY = new HashMap<String, ForeignKeyModel>();

		ForeignKeyModel shopListUserFk = new ForeignKeyModel();
		shopListUserFk.setRefEntityName("shopList");
		shopListUserFk.setUnique(false);
		shopListUserFk.getFields().add("user");
		FOREIGN_KEY.put("shopListUserFk", shopListUserFk);

		ForeignKeyModel shopListPk = new ForeignKeyModel();
		shopListPk.setRefEntityName("shopList");
		shopListPk.setUnique(true);
		shopListPk.getFields().add("id");
		FOREIGN_KEY.put("shopListPk", shopListPk);


		LINK = new HashMap<String, LinkModel>();

		LinkModel shopArticleLUserLnk = new LinkModel("shopArticleLUser");
		shopArticleLUserLnk.setEntityName("shopList");
		shopArticleLUserLnk.setKeyName("shopListUserFk");
		shopArticleLUserLnk.setRefEntityName("shopUser");
		LINK.put("shopArticleLUser", shopArticleLUserLnk);

		
		BACK_REF = new HashMap<String, LinkModel>();

		LinkModel brshopListLArticleLList = new LinkModel("shopListLArticleLList");
		brshopListLArticleLList.setEntityName("shopListLArticle");
		brshopListLArticleLList.setKeyName("shopListLArticleListFk");
		brshopListLArticleLList.setRefEntityName("shopList");
		BACK_REF.put("shopListLArticleLList", brshopListLArticleLList);


		FIELDS = new HashMap<String, EntityField>();
		AUTOINCREMENT_FIELDS = new HashSet<String>();

		AUTOINCREMENT_FIELDS.add("id");

		FIELDS.put("id", new EntityField("ID", "INTEGER", 10, 0, Memory.NO, true, false, "ID"));
		FIELDS.put("name", new EntityField("NAME", "VARCHAR2", 100, 0, Memory.NO, true, false, "Nom"));
		FIELDS.put("user", new EntityField("USER", "VARCHAR2", 10, 0, Memory.NO, true, false, "Créateur"));
		FIELDS.put("createDate", new EntityField("CREATE_DATE", "DATE", 0, 0, Memory.NO, false, false, "Date de création"));
		FIELDS.put("w$Desc", new EntityField("W$_DESC", "VARCHAR2", 200, 0, Memory.SQL, false, false, "Description")); 
		FIELDS.get("w$Desc").setSqlExpr("NAME");

		FIELDS.put("articleCount", new EntityField("ARTICLE_COUNT", "INTEGER", 3, 0, Memory.ALWAYS, false, false, "Nombre d'articles"));
		ACTIONS = new HashMap<Integer, Action>();
		ACTIONS.put(0, new Action(0, 0));
		ACTIONS.put(2, new Action(2, 2));
		ACTIONS.put(4, new Action(4, 4));
		ACTIONS.put(5, new Action(5, 5));
		ACTIONS.put(50, new Action(50, 2));

	}
	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(Integer id) {
		Key key = new Key(PRIMARY_KEY_MODEL);
		key.setValue("id", id);

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
	 * Get field 'id'
	 * @return the EntityField
	 */
	public EntityField getId() {
		return FIELDS.get("id");
	}

	/**
	 * Get field 'name'
	 * @return the EntityField
	 */
	public EntityField getName() {
		return FIELDS.get("name");
	}

	/**
	 * Get field 'user'
	 * @return the EntityField
	 */
	public EntityField getUser() {
		return FIELDS.get("user");
	}

	/**
	 * Get field 'createDate'
	 * @return the EntityField
	 */
	public EntityField getCreateDate() {
		return FIELDS.get("createDate");
	}

	/**
	 * Get field 'w$Desc'
	 * @return the EntityField
	 */
	public EntityField getW$Desc() {
		return FIELDS.get("w$Desc");
	}

	/**
	 * Get field 'articleCount'
	 * @return the EntityField
	 */
	public EntityField getArticleCount() {
		return FIELDS.get("articleCount");
	}
	
	@Override
	public boolean isAutoIncrementField(String name) {
		return AUTOINCREMENT_FIELDS.contains(name);
	}


	/**
	 * Link from entity SHOP_LIST to SHOP_USER
	 * User
	 */ 
	public static final String LINK_SHOP_ARTICLE_L_USER = "shopArticleLUser";

	/**
	 * Link from entity SHOP_LIST_L_ARTICLE to SHOP_LIST
	 * Link to List
	 */ 
	public static final String LINK_SHOP_LIST_L_ARTICLE_L_LIST = "shopListLArticleLList";

}
