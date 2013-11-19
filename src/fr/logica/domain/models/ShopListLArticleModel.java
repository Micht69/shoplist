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
 * Model class for the entity ShopListLArticle
 *
 * @author CGI
 */
public class ShopListLArticleModel extends EntityModel implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Table name for this entity */
	public static final String ENTITY_DB_NAME = "SHOP_LIST_L_ARTICLE";

	/** Name for this entity */
	public static final String ENTITY_NAME = "shopListLArticle";

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
		PRIMARY_KEY_MODEL.setName("shopListLArticlePk");
		PRIMARY_KEY_MODEL.getFields().add("listId");
		PRIMARY_KEY_MODEL.getFields().add("articleId");

		
		FOREIGN_KEY = new HashMap<String, ForeignKeyModel>();

		ForeignKeyModel shopListLArticlePk = new ForeignKeyModel();
		shopListLArticlePk.setRefEntityName("shopListLArticle");
		shopListLArticlePk.setUnique(true);
		shopListLArticlePk.getFields().add("listId");
		shopListLArticlePk.getFields().add("articleId");
		FOREIGN_KEY.put("shopListLArticlePk", shopListLArticlePk);

		ForeignKeyModel shopListLArticleListFk = new ForeignKeyModel();
		shopListLArticleListFk.setRefEntityName("shopListLArticle");
		shopListLArticleListFk.setUnique(false);
		shopListLArticleListFk.getFields().add("listId");
		FOREIGN_KEY.put("shopListLArticleListFk", shopListLArticleListFk);

		ForeignKeyModel shopListLArticleArticleFk = new ForeignKeyModel();
		shopListLArticleArticleFk.setRefEntityName("shopListLArticle");
		shopListLArticleArticleFk.setUnique(false);
		shopListLArticleArticleFk.getFields().add("articleId");
		FOREIGN_KEY.put("shopListLArticleArticleFk", shopListLArticleArticleFk);


		LINK = new HashMap<String, LinkModel>();

		LinkModel shopListLArticleLListLnk = new LinkModel("shopListLArticleLList");
		shopListLArticleLListLnk.setEntityName("shopListLArticle");
		shopListLArticleLListLnk.setKeyName("shopListLArticleListFk");
		shopListLArticleLListLnk.setRefEntityName("shopList");
		LINK.put("shopListLArticleLList", shopListLArticleLListLnk);

		LinkModel shopListLArticleLArticleLnk = new LinkModel("shopListLArticleLArticle");
		shopListLArticleLArticleLnk.setEntityName("shopListLArticle");
		shopListLArticleLArticleLnk.setKeyName("shopListLArticleArticleFk");
		shopListLArticleLArticleLnk.setRefEntityName("shopArticle");
		LINK.put("shopListLArticleLArticle", shopListLArticleLArticleLnk);

		
		BACK_REF = new HashMap<String, LinkModel>();


		FIELDS = new HashMap<String, EntityField>();
		AUTOINCREMENT_FIELDS = new HashSet<String>();

		FIELDS.put("listId", new EntityField("LIST_ID", "INTEGER", 10, 0, Memory.NO, true, false, "Liste"));
		FIELDS.put("articleId", new EntityField("ARTICLE_ID", "INTEGER", 10, 0, Memory.NO, true, false, "Article"));
		FIELDS.put("quantity", new EntityField("QUANTITY", "INTEGER", 3, 0, Memory.NO, true, false, "Quantité"));
		FIELDS.put("status", new EntityField("STATUS", "VARCHAR2", 5, 0, Memory.NO, false, false, "Statut"));
		FIELDS.get("status").setDefaultValue("BUY", "BUY"); 
		FIELDS.get("status").getCodes().add("BUY");
		FIELDS.get("status").getLabels().add(ENTITY_NAME + ".status.BUY");
		FIELDS.get("status").getValues().add("BUY"); 
		FIELDS.get("status").getCodes().add("DONE");
		FIELDS.get("status").getLabels().add(ENTITY_NAME + ".status.DONE");
		FIELDS.get("status").getValues().add("DONE"); 
		ACTIONS = new HashMap<Integer, Action>();
		ACTIONS.put(0, new Action(0, 0, 51));
		ACTIONS.put(2, new Action(2, 2));
		ACTIONS.put(4, new Action(4, 4));
		ACTIONS.put(5, new Action(5, 5));
		ACTIONS.put(50, new Action(50, 10));
		ACTIONS.put(51, new Action(51, 0, 0));

	}
	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(Integer listId, Integer articleId) {
		Key key = new Key(PRIMARY_KEY_MODEL);
		key.setValue("listId", listId);
		key.setValue("articleId", articleId);

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
	 * Get field 'listId'
	 * @return the EntityField
	 */
	public EntityField getListId() {
		return FIELDS.get("listId");
	}

	/**
	 * Get field 'articleId'
	 * @return the EntityField
	 */
	public EntityField getArticleId() {
		return FIELDS.get("articleId");
	}

	/**
	 * Get field 'quantity'
	 * @return the EntityField
	 */
	public EntityField getQuantity() {
		return FIELDS.get("quantity");
	}

	/**
	 * Get field 'status'
	 * @return the EntityField
	 */
	public EntityField getStatus() {
		return FIELDS.get("status");
	}
	
	@Override
	public boolean isAutoIncrementField(String name) {
		return AUTOINCREMENT_FIELDS.contains(name);
	}


	/**
	 * Link from entity SHOP_LIST_L_ARTICLE to SHOP_LIST
	 * Liste
	 */ 
	public static final String LINK_SHOP_LIST_L_ARTICLE_L_LIST = "shopListLArticleLList";

	/**
	 * Link from entity SHOP_LIST_L_ARTICLE to SHOP_ARTICLE
	 * Article
	 */ 
	public static final String LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE = "shopListLArticleLArticle";

}
