package fr.logica.domain.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import java.util.Date;

import fr.logica.business.Context;
import fr.logica.business.DateUtils;
import fr.logica.business.Entity;
import fr.logica.business.Key;
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

	/** Nom */
	private String name ;

	/** Créateur */
	private String user ;

	/** Date de création */
	private Date createDate = DateUtils.today();

	/** Description */
	private String w$Desc ;

	/** Nombre d'articles */
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
	public boolean find(Integer id, Context ctx) {
		Key primaryKey = ShopListModel.buildPrimaryKey(id);
		if (!primaryKey.isFull()) {
			return false;
		}
		ShopList dbInstance = DB.get($NAME, primaryKey, ctx);
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
	public String $_getName() {
		return "shopList";
	}


	/** Entity description */
	@Override
	public String $_getDesc() {
		return "name";
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
	 * Get the value from field W$Desc.
	 *
	 * @return the value
	 */
	public String getW$Desc() {
		return this.w$Desc;
	}
 
	/**
	 * Set the value from field W$Desc.
	 *
	 * @param w$Desc : the value to set
	 */
	public void setW$Desc(final String w$Desc) {
		this.w$Desc = w$Desc;
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
	public ShopUser getRef_ShopListUserFk(Context ctx) {
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
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopListLArticle related to the current ShopList via link ShopListLArticleLList.
	 */
	public Set<ShopListLArticle> getList_ShopListLArticleLList(Context ctx) {
		Set<ShopListLArticle> s = new HashSet<ShopListLArticle>();
		for (Entity e : DB.getLinkedEntities(this, ShopListModel.LINK_SHOP_LIST_L_ARTICLE_L_LIST, ctx)) {
			s.add((ShopListLArticle) e);
		}
		return s;
	}


	/** Holder for the action names */
	public interface Action {
		/** Create. */
		int ACTION_0 = 0;
		/** Modify. */
		int ACTION_2 = 2;
		/** Delete. */
		int ACTION_4 = 4;
		/** Display. */
		int ACTION_5 = 5;
		/** Shopping. */
		int ACTION_50 = 50;
    }

	/** Nom de l'entité. */
	public static final String $NAME = "shopList";
}
