package fr.logica.domain.objects;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

import java.util.Date;
import fr.logica.business.DateUtils;

import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.domain.models.ShopListModel;

/**
 * Entity ShopList definition
 * 
 * @author CGI
 */
public class ShopList extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;
	

	/** ID */
	private Integer id ;

	/** Titre */
	private String name ;

	/** Créateur */
	private String user ;

	/** Date de création */
	private Date createDate = fr.logica.business.DateUtils.today();

	/** Description */
	private String internalCaption ;

	/** Nbr d'articles */
	private Integer articleCount ;


	public ShopList() {
		// Default constructor
		super();
	}

	/**
	 * Initialize a new domain with its primary key. <br/>
	 * <b>This method does not load a bean from database.</b><br/>
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * 
	 * @param id ID

	 */
	public ShopList(Integer id) {
		super();
		Key primaryKey = ShopListModel.buildPrimaryKey(id);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Load a bean from database based on its primary key
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * @param id ID
	 * @param ctx Current context with open database connection.
	 * @return	<code>true</code> if the bean has been loaded, <code>false</code> if no entity was found.  
	 */
	public boolean find(Integer id, RequestContext ctx) {
		Key primaryKey = ShopListModel.buildPrimaryKey(id);
		if (!primaryKey.isFull()) {
			return false;
		}
		ShopList dbInstance = DB.get(NAME, primaryKey, ctx);
		if (dbInstance != null) {
			syncFromBean(dbInstance);
			return true; 
		}
		return false; 
	}
	
	public ShopList(ShopList pShopList) {
		super(pShopList);
	}

	/** Entity name */
	@Override
	public String name() {
		return "shopList";
	}

	/** Entity description */
	@Override
	public String description() {
		return getInternalCaption(); 
	}

	/**
	 * Get the value from field Id.
	 *
	 * @return the value
	 */
	public Integer getId() {
		return this.id;
	}
 
	/**
	 * Set the value from field Id.
	 *
	 * @param id : the value to set
	 */
	public void setId(final Integer id) {
		this.id = id;
	}
	


	/**
	 * Get the value from field Name.
	 *
	 * @return the value
	 */
	public String getName() {
		return this.name;
	}
 
	/**
	 * Set the value from field Name.
	 *
	 * @param name : the value to set
	 */
	public void setName(final String name) {
		this.name = name;
	}
	


	/**
	 * Get the value from field User.
	 *
	 * @return the value
	 */
	public String getUser() {
		return this.user;
	}
 
	/**
	 * Set the value from field User.
	 *
	 * @param user : the value to set
	 */
	public void setUser(final String user) {
		this.user = user;
	}
	


	/**
	 * Get the value from field CreateDate.
	 *
	 * @return the value
	 */
	public Date getCreateDate() {
		return this.createDate;
	}
 
	/**
	 * Set the value from field CreateDate.
	 *
	 * @param createDate : the value to set
	 */
	public void setCreateDate(final Date createDate) {
		this.createDate = createDate;
	}
	


	/**
	 * Gets the value from field InternalCaption. This getter respects real Java naming convention. 
	 *
	 * @return the value
	 */
	public String getw$Desc() {
		return getInternalCaption();
	}
 
	/**
	 * Sets the value from field InternalCaption. This setter respects real Java naming convention
	 *
	 * @param internalCaption : the value to set
	 */
	public void setw$Desc(final String internalCaption) {
		setInternalCaption(internalCaption); 
	}

	/**
	 * Get the value from field InternalCaption.
	 *
	 * @return the value
	 */
	public String getInternalCaption() {
		return this.internalCaption;
	}
 
	/**
	 * Set the value from field InternalCaption.
	 *
	 * @param internalCaption : the value to set
	 */
	public void setInternalCaption(final String internalCaption) {
		this.internalCaption = internalCaption;
	}
	


	/**
	 * Get the value from field ArticleCount.
	 *
	 * @return the value
	 */
	public Integer getArticleCount() {
		return this.articleCount;
	}
 
	/**
	 * Set the value from field ArticleCount.
	 *
	 * @param articleCount : the value to set
	 */
	public void setArticleCount(final Integer articleCount) {
		this.articleCount = articleCount;
	}
	


	/** Holder for the var names */
	public interface Var {
		/** Var ID */
		String ID = "id";
		/** Var NAME */
		String NAME = "name";
		/** Var USER */
		String USER = "user";
		/** Var CREATE_DATE */
		String CREATE_DATE = "createDate";
		/** Var ARTICLE_COUNT */
		String ARTICLE_COUNT = "articleCount";
	}
	

	/**
	 * Instance of ShopUser matching the link ShopArticleLUser based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopUser matching the link pays if any, null otherwise.
	 */
	public ShopUser getRef_ShopListUserFk(RequestContext ctx) {
		return (ShopUser) DB.getRef(this, ShopListModel.LINK_SHOP_ARTICLE_L_USER, ctx);
	}
	
	/**
	 * This method sets all variables of foreign key in the current ShopList object to match the primary key of the ShopUser instance. <br/>
	 * If the ShopUser instance is null, all foreign key variables will be set to null. <br/>
	 * <b>This method does not access database and won't automatically update database link.</b>
	 * 
	 * @param pShopUser The newly targeted bean of link ShopArticleLUser.
	 */
	public void setRef_ShopListUserFk(ShopUser pShopUser) {
		Key primaryKey = null;
		if (pShopUser != null) {
			primaryKey = pShopUser.getPrimaryKey();
		}
		setForeignKey(getModel().getLinkModel(ShopListModel.LINK_SHOP_ARTICLE_L_USER).getKeyName(), primaryKey);
	}

	/**
	 * This methods gets all instances of ShopListLArticle back referenced by the current ShopList instance via link ShopListLArticleLList. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b> <br/>
	 * <i>Note: if the PK is incomplete, an empty set will be returned.</i>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopListLArticle related to the current ShopList via link ShopListLArticleLList.
	 */
	public Set<ShopListLArticle> getList_ShopListLArticleLList(RequestContext ctx) {
		Set<ShopListLArticle> s = new HashSet<ShopListLArticle>();
		if (this.getPrimaryKey() == null || !this.getPrimaryKey().isFull()) {
			// Do not get linked entities if PK is incomplete
			return s;
		}
		for (Entity e : DB.getLinkedEntities(this, ShopListModel.LINK_SHOP_LIST_L_ARTICLE_L_LIST, ctx)) {
			s.add((ShopListLArticle) e);
		}
		return s;
	}


	/** Holder for the action names */
	public interface Action {
		/** Créer. */
		int ACTION_0 = 0;
		/** Modifier. */
		int ACTION_2 = 2;
		/** Supprimer. */
		int ACTION_4 = 4;
		/** Afficher. */
		int ACTION_5 = 5;
		/** Réaliser les course. */
		int ACTION_50 = 50;
    }

	/** Nom de l'entité. */
	public static final String NAME = "shopList";
	
	/**
	 * Clones the current bean.
	 */
	@Override
	public ShopList clone() {
		ShopList clone = (ShopList) super.clone();
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
		createDate = null; 
	}
}
