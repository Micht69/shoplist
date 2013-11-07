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
 * Model class for the entity ShopArticle
 *
 * @author CGI
 */
public class ShopArticleModel extends EntityModel implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Table name for this entity */
	public static final String ENTITY_DB_NAME = "article";

	/** Name for this entity */
	public static final String ENTITY_NAME = "shopArticle";

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
		PRIMARY_KEY_MODEL.setName("shopArticlePk");
		PRIMARY_KEY_MODEL.getFields().add("id");

		
		FOREIGN_KEY = new HashMap<String, ForeignKeyModel>();

		ForeignKeyModel shopArticlePk = new ForeignKeyModel();
		shopArticlePk.setRefEntityName("shopArticle");
		shopArticlePk.setUnique(true);
		shopArticlePk.getFields().add("id");
		FOREIGN_KEY.put("shopArticlePk", shopArticlePk);

		ForeignKeyModel shopArticleRShelf = new ForeignKeyModel();
		shopArticleRShelf.setRefEntityName("shopArticle");
		shopArticleRShelf.setUnique(false);
		shopArticleRShelf.getFields().add("shelf");
		FOREIGN_KEY.put("shopArticleRShelf", shopArticleRShelf);


		LINK = new HashMap<String, LinkModel>();

		LinkModel shopArticleLShelfLnk = new LinkModel("shopArticleLShelf");
		shopArticleLShelfLnk.setEntityName("shopArticle");
		shopArticleLShelfLnk.setKeyName("shopArticleRShelf");
		shopArticleLShelfLnk.setRefEntityName("shopShelf");
		LINK.put("shopArticleLShelf", shopArticleLShelfLnk);

		
		BACK_REF = new HashMap<String, LinkModel>();

		LinkModel brshopListLArticleLArticle = new LinkModel("shopListLArticleLArticle");
		brshopListLArticleLArticle.setEntityName("shopListLArticle");
		brshopListLArticleLArticle.setKeyName("shopListLArticleArticleFk");
		brshopListLArticleLArticle.setRefEntityName("shopArticle");
		BACK_REF.put("shopListLArticleLArticle", brshopListLArticleLArticle);


		FIELDS = new HashMap<String, EntityField>();
		AUTOINCREMENT_FIELDS = new HashSet<String>();

		AUTOINCREMENT_FIELDS.add("id");

		FIELDS.put("id", new EntityField("ID", "INTEGER", 10, 0, Memory.NO, true, false, "ID"));
		FIELDS.put("name", new EntityField("NAME", "VARCHAR2", 128, 0, Memory.NO, true, true, "Nom"));
		FIELDS.put("descr", new EntityField("DESCR", "VARCHAR2", 500, 0, Memory.NO, false, false, "Description"));
		FIELDS.put("shelf", new EntityField("SHELF", "VARCHAR2", 10, 0, Memory.NO, true, false, "Rayon"));
		FIELDS.put("ean13", new EntityField("EAN13", "VARCHAR2", 13, 0, Memory.NO, false, false, "Ean13"));
		FIELDS.put("w$Desc", new EntityField("W$_DESC", "VARCHAR2", 128, 0, Memory.SQL, false, false, "Description")); 
		FIELDS.get("w$Desc").setSqlExpr("NAME");

		ACTIONS = new HashMap<Integer, Action>();
		ACTIONS.put(0, new Action(0, 0));
		ACTIONS.put(2, new Action(2, 2));
		ACTIONS.put(4, new Action(4, 4));
		ACTIONS.put(5, new Action(5, 5));
		ACTIONS.put(50, new Action(50, 0));

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
	 * Get field 'descr'
	 * @return the EntityField
	 */
	public EntityField getDescr() {
		return FIELDS.get("descr");
	}

	/**
	 * Get field 'shelf'
	 * @return the EntityField
	 */
	public EntityField getShelf() {
		return FIELDS.get("shelf");
	}

	/**
	 * Get field 'ean13'
	 * @return the EntityField
	 */
	public EntityField getEan13() {
		return FIELDS.get("ean13");
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
	 * Shelf
	 */ 
	public static final String LINK_SHOP_ARTICLE_L_SHELF = "shopArticleLShelf";

	/**
	 * Link from entity SHOP_LIST_L_ARTICLE to SHOP_ARTICLE
	 * Link to Article
	 */ 
	public static final String LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE = "shopListLArticleLArticle";

}
