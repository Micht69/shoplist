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
import fr.logica.business.EntityField.Memory;
import fr.logica.business.EntityModel;
import fr.logica.business.EntityField;
import fr.logica.business.ForeignKeyModel;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.LinkModel;
import fr.logica.business.MessageUtils;

/**
 * Model class for the entity ShopUser
 *
 * @author CGI
 */
public class ShopUserModel extends EntityModel implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Table name for this entity */
	public static final String ENTITY_DB_NAME = "user";

	/** Name for this entity */
	public static final String ENTITY_NAME = "shopUser";

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
		PRIMARY_KEY_MODEL.setName("shopUserPk");
		PRIMARY_KEY_MODEL.getFields().add("login");

		
		FOREIGN_KEY = new HashMap<String, ForeignKeyModel>();

		ForeignKeyModel shopUserPk = new ForeignKeyModel();
		shopUserPk.setRefEntityName("shopUser");
		shopUserPk.setUnique(true);
		shopUserPk.getFields().add("login");
		FOREIGN_KEY.put("shopUserPk", shopUserPk);


		LINK = new HashMap<String, LinkModel>();

		
		BACK_REF = new HashMap<String, LinkModel>();

		LinkModel brshopArticleLUser = new LinkModel("shopArticleLUser");
		brshopArticleLUser.setEntityName("shopList");
		brshopArticleLUser.setKeyName("shopListUserFk");
		brshopArticleLUser.setRefEntityName("shopUser");
		BACK_REF.put("shopArticleLUser", brshopArticleLUser);


		FIELDS = new HashMap<String, EntityField>();
		AUTOINCREMENT_FIELDS = new HashSet<String>();

		FIELDS.put("login", new EntityField("LOGIN", "VARCHAR2", 10, 0, Memory.NO, true, false, "Login"));
		FIELDS.put("name", new EntityField("NAME", "VARCHAR2", 100, 0, Memory.NO, true, false, "Nom"));
		FIELDS.put("profile", new EntityField("PROFILE", "VARCHAR2", 10, 0, Memory.NO, false, false, "Profil"));
		FIELDS.get("profile").setDefaultValue("USER", "USER"); 
		FIELDS.get("profile").getCodes().add("USER");
		FIELDS.get("profile").getLabels().add(ENTITY_NAME + ".profile.USER");
		FIELDS.get("profile").getValues().add("USER"); 
		FIELDS.get("profile").getCodes().add("ADMIN");
		FIELDS.get("profile").getLabels().add(ENTITY_NAME + ".profile.ADMIN");
		FIELDS.get("profile").getValues().add("ADMIN"); 
		FIELDS.put("w$Desc", new EntityField("W$_DESC", "VARCHAR2", 100, 0, Memory.SQL, false, false, "Description")); 
		FIELDS.get("w$Desc").setSqlExpr("NAME");

		ACTIONS = new HashMap<Integer, Action>();
		ACTIONS.put(0, new Action(0, 0));
		ACTIONS.put(2, new Action(2, 2));
		ACTIONS.put(4, new Action(4, 4));
		ACTIONS.put(5, new Action(5, 5));

	}
	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(String login) {
		Key key = new Key(PRIMARY_KEY_MODEL);
		key.setValue("login", login);

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
	 * Get field 'login'
	 * @return the EntityField
	 */
	public EntityField getLogin() {
		return FIELDS.get("login");
	}

	/**
	 * Get field 'name'
	 * @return the EntityField
	 */
	public EntityField getName() {
		return FIELDS.get("name");
	}

	/**
	 * Get field 'profile'
	 * @return the EntityField
	 */
	public EntityField getProfile() {
		return FIELDS.get("profile");
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
	 * Link from entity SHOP_LIST to SHOP_USER
	 * Utilisateur
	 */ 
	public static final String LINK_SHOP_ARTICLE_L_USER = "shopArticleLUser";

}
