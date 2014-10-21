package fr.logica.domain.objects;

import java.io.Serializable;

import fr.logica.business.Action.*;
import fr.logica.business.Entity;
import fr.logica.business.EntityField.SqlTypes;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.domain.annotations.*;
import fr.logica.domain.constants.ShopListLArticleConstants;

/**
 * Entity ShopListLArticle definition
 * 
 * @author CGI
 */
@EntityDef(dbName = "SHOP_LIST_L_ARTICLE", primaryKey = { "listId", "articleId" })
@Links({
	@Link(name = "shopListLArticleLList", targetEntity = "shopList", fields = { "listId" }),
	@Link(name = "shopListLArticleLArticle", targetEntity = "shopArticle", fields = { "articleId" })
})
@Actions({
	@Action(code = 0, nextAction = 51, input = Input.NONE, persistence = Persistence.INSERT),
	@Action(code = 51, nextAction = 0, input = Input.NONE, persistence = Persistence.INSERT),
	@Action(code = 70, input = Input.NONE, persistence = Persistence.UPDATE, ui = UserInterface.NONE, process = fr.logica.business.Action.Process.CUSTOM),
	@Action(code = 71, pageName = "SHOP_LIST_L_ARTICLE_SCAN", input = Input.NONE, persistence = Persistence.INSERT, process = fr.logica.business.Action.Process.CUSTOM),
	@Action(code = 2, persistence = Persistence.UPDATE),
	@Action(code = 20, pageName = "SHOP_LIST_L_ARTICLE_DELETE", input = Input.MANY, persistence = Persistence.DELETE, ui = UserInterface.READONLY, process = fr.logica.business.Action.Process.CUSTOM),
	@Action(code = 60, persistence = Persistence.UPDATE, ui = UserInterface.NONE, process = fr.logica.business.Action.Process.CUSTOM)
})
public class ShopListLArticle extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Liste */
	@EntityField(sqlName = "LIST_ID", sqlType = SqlTypes.INTEGER, sqlSize = 10, isMandatory = true)
	private Integer listId;

	/** Article */
	@EntityField(sqlName = "ARTICLE_ID", sqlType = SqlTypes.INTEGER, sqlSize = 10, isMandatory = true)
	private Integer articleId;

	/** Quantité */
	@EntityField(sqlName = "QUANTITY", sqlType = SqlTypes.INTEGER, sqlSize = 3, isMandatory = true)
	private Integer quantity = 1;

	/** Statut */
	@EntityField(sqlName = "STATUS", sqlType = SqlTypes.VARCHAR2, sqlSize = 5, defaultValue = "BUY")
	@DefinedValues({
			@DefinedValue(code = "BUY", label = "shopListLArticle.status.BUY", value = "BUY", isDefault = true), // A acheter
			@DefinedValue(code = "DONE", label = "shopListLArticle.status.DONE", value = "DONE") // Acheté
	})
	private String status;

	/** Récapitulatif */
	@EntityField(sqlName = "DELETE_INFOS", sqlType = SqlTypes.VARCHAR2, sqlSize = 1000, memory = fr.logica.business.EntityField.Memory.ALWAYS)
	private String deleteInfos;

	/** Ean13 */
	@EntityField(sqlName = "ITEM_EAN13", sqlType = SqlTypes.VARCHAR2, sqlSize = 13, memory = fr.logica.business.EntityField.Memory.NEVER)
	private String itemEan13;

	/**
	 * Initialize a new ShopListLArticle.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
	public ShopListLArticle() {
		// Default constructor
		super();
	}

	/**
	 * Initialize a new domain with its primary key. <br/>
	 * <b>This method does not load a bean from database.</b><br/>
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * 
	 * @param listId Liste
	 * @param articleId Article

	 */
	public ShopListLArticle(Integer listId, Integer articleId) {
		super();
		Key primaryKey = buildPrimaryKey(listId, articleId);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new ShopListLArticle from an existing ShopListLArticle.<br/>
	 * <b>All fields value are copied.</b>
	 */
	public ShopListLArticle(ShopListLArticle pShopListLArticle) {
		super(pShopListLArticle);
	}

	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(Integer listId, Integer articleId) {
		KeyModel pkModel = new KeyModel(ShopListLArticleConstants.ENTITY_NAME);
		// FIXME : Récupérer la PK ...
		Key key = new Key(pkModel);
		key.setValue("listId", listId);
		key.setValue("articleId", articleId);

		return key;
	}

	/** Entity name */
	@Override
	public String name() {
		return ShopListLArticleConstants.ENTITY_NAME;
	}

	/** Entity description */
	@Override
	public String description() {
		return null;
	}

	/**
	 * Get the value from field ListId.
	 *
	 * @return the value
	 */
	public Integer getListId() {
		return this.listId;
	}

	/**
	 * Set the value from field ListId.
	 *
	 * @param listId : the value to set
	 */
	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	/**
	 * Get the value from field ArticleId.
	 *
	 * @return the value
	 */
	public Integer getArticleId() {
		return this.articleId;
	}

	/**
	 * Set the value from field ArticleId.
	 *
	 * @param articleId : the value to set
	 */
	public void setArticleId(final Integer articleId) {
		this.articleId = articleId;
	}

	/**
	 * Get the value from field Quantity.
	 *
	 * @return the value
	 */
	public Integer getQuantity() {
		return this.quantity;
	}

	/**
	 * Set the value from field Quantity.
	 *
	 * @param quantity : the value to set
	 */
	public void setQuantity(final Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * Get the value from field Status.
	 *
	 * @return the value
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * Set the value from field Status.
	 *
	 * @param status : the value to set
	 */
	public void setStatus(final String status) {
		this.status = status;
	}

	/**
	 * Get the value from field DeleteInfos.
	 *
	 * @return the value
	 */
	public String getDeleteInfos() {
		return this.deleteInfos;
	}

	/**
	 * Set the value from field DeleteInfos.
	 *
	 * @param deleteInfos : the value to set
	 */
	public void setDeleteInfos(final String deleteInfos) {
		this.deleteInfos = deleteInfos;
	}

	/**
	 * Get the value from field ItemEan13.
	 *
	 * @return the value
	 */
	public String getItemEan13() {
		return this.itemEan13;
	}

	/**
	 * Set the value from field ItemEan13.
	 *
	 * @param itemEan13 : the value to set
	 */
	public void setItemEan13(final String itemEan13) {
		this.itemEan13 = itemEan13;
	}

	/**
	 * Instance of ShopList matching the link ShopListLArticleLList based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopList matching the link pays if any, null otherwise.
	 */
	public ShopList getRef_ShopListLArticleListFk(RequestContext ctx) {
		return (ShopList) DB.getRef(this, ShopListLArticleConstants.Links.LINK_SHOP_LIST_L_ARTICLE_L_LIST, ctx);
	}
	
	/**
	 * This method sets all variables of foreign key in the current ShopListLArticle object to match the primary key of the ShopList instance. <br/>
	 * If the ShopList instance is null, all foreign key variables will be set to null. <br/>
	 * <b>This method does not access database and won't automatically update database link.</b>
	 * 
	 * @param pShopList The newly targeted bean of link ShopListLArticleLList.
	 */
	public void setRef_ShopListLArticleListFk(ShopList pShopList) {
		Key primaryKey = null;
		if (pShopList != null) {
			primaryKey = pShopList.getPrimaryKey();
		}
		setForeignKey(ShopListLArticleConstants.Links.LINK_SHOP_LIST_L_ARTICLE_L_LIST, primaryKey);
	}

	/**
	 * Instance of ShopArticle matching the link ShopListLArticleLArticle based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopArticle matching the link pays if any, null otherwise.
	 */
	public ShopArticle getRef_ShopListLArticleArticleFk(RequestContext ctx) {
		return (ShopArticle) DB.getRef(this, ShopListLArticleConstants.Links.LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE, ctx);
	}
	
	/**
	 * This method sets all variables of foreign key in the current ShopListLArticle object to match the primary key of the ShopArticle instance. <br/>
	 * If the ShopArticle instance is null, all foreign key variables will be set to null. <br/>
	 * <b>This method does not access database and won't automatically update database link.</b>
	 * 
	 * @param pShopArticle The newly targeted bean of link ShopListLArticleLArticle.
	 */
	public void setRef_ShopListLArticleArticleFk(ShopArticle pShopArticle) {
		Key primaryKey = null;
		if (pShopArticle != null) {
			primaryKey = pShopArticle.getPrimaryKey();
		}
		setForeignKey(ShopListLArticleConstants.Links.LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE, primaryKey);
	}

	
	/**
	 * Clones the current bean.
	 */
	@Override
	public ShopListLArticle clone() {
		ShopListLArticle clone = (ShopListLArticle) super.clone();
		clone.removeDefaultValues();
		for (String f : getModel().getFields()) {
			clone.invokeSetter(f, invokeGetter(f));
		}
		clone.resetLinksAndBackRefs();
		return clone;
	}

	/** 
	 * Removes all initial values from the bean and sets everything to null
	 */
	@Override
	public void removeDefaultValues() {
		quantity = null; 
		status = null; 
	}
}
