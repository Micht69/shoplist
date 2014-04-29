package fr.logica.domain.objects;

import java.io.Serializable;


import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.domain.models.ShopListLArticleModel;

/**
 * Entity ShopListLArticle definition
 * 
 * @author CGI
 */
public class ShopListLArticle extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;
	

	/** Liste */
	private Integer listId ;

	/** Article */
	private Integer articleId ;

	/** Quantité */
	private Integer quantity = 1;

	/** Statut */
	private String status = "BUY";

	/** Récapitulatif */
	private String deleteInfos ;


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
		Key primaryKey = ShopListLArticleModel.buildPrimaryKey(listId, articleId);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Load a bean from database based on its primary key
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * @param listId Liste
	 * @param articleId Article
	 * @param ctx Current context with open database connection.
	 * @return	<code>true</code> if the bean has been loaded, <code>false</code> if no entity was found.  
	 */
	public boolean find(Integer listId, Integer articleId, RequestContext ctx) {
		Key primaryKey = ShopListLArticleModel.buildPrimaryKey(listId, articleId);
		if (!primaryKey.isFull()) {
			return false;
		}
		ShopListLArticle dbInstance = DB.get(NAME, primaryKey, ctx);
		if (dbInstance != null) {
			syncFromBean(dbInstance);
			return true; 
		}
		return false; 
	}
	
	public ShopListLArticle(ShopListLArticle pShopListLArticle) {
		super(pShopListLArticle);
	}

	/** Entity name */
	@Override
	public String name() {
		return "shopListLArticle";
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
	


	/** Holder for the var names */
	public interface Var {
		/** Var LIST_ID */
		String LIST_ID = "listId";
		/** Var ARTICLE_ID */
		String ARTICLE_ID = "articleId";
		/** Var QUANTITY */
		String QUANTITY = "quantity";
		/** Var STATUS */
		String STATUS = "status";
		/** Var DELETE_INFOS */
		String DELETE_INFOS = "deleteInfos";
	}
	
	/** Holder for the defined values */
	public interface ValueList {
		public interface STATUS {
			/** A acheter */
			String BUY = "BUY";
			/** Acheté */
			String DONE = "DONE";
		}
	}

	/**
	 * Instance of ShopList matching the link ShopListLArticleLList based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopList matching the link pays if any, null otherwise.
	 */
	public ShopList getRef_ShopListLArticleListFk(RequestContext ctx) {
		return (ShopList) DB.getRef(this, ShopListLArticleModel.LINK_SHOP_LIST_L_ARTICLE_L_LIST, ctx);
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
		setForeignKey(getModel().getLinkModel(ShopListLArticleModel.LINK_SHOP_LIST_L_ARTICLE_L_LIST).getKeyName(), primaryKey);
	}

	/**
	 * Instance of ShopArticle matching the link ShopListLArticleLArticle based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopArticle matching the link pays if any, null otherwise.
	 */
	public ShopArticle getRef_ShopListLArticleArticleFk(RequestContext ctx) {
		return (ShopArticle) DB.getRef(this, ShopListLArticleModel.LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE, ctx);
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
		setForeignKey(getModel().getLinkModel(ShopListLArticleModel.LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE).getKeyName(), primaryKey);
	}


	/** Holder for the action names */
	public interface Action {
		/** Créer. */
		int ACTION_0 = 0;
		/** Créer. */
		int ACTION_51 = 51;
		/** Sélectionner articles. */
		int ACTION_70 = 70;
		/** Modifier. */
		int ACTION_2 = 2;
		/** Supprimer. */
		int ACTION_20 = 20;
		/** Marquer acheté. */
		int ACTION_60 = 60;
    }

	/** Nom de l'entité. */
	public static final String NAME = "shopListLArticle";
	
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
