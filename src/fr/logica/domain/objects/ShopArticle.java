package fr.logica.domain.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


import fr.logica.business.Context;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.db.DB;
import fr.logica.domain.models.ShopArticleModel;

/**
 * Entity ShopArticle definition
 * 
 * @author CGI
 */
public class ShopArticle extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;
	

	/** ID */
	private Integer id ;

	/** Nom */
	private String name ;

	/** Description */
	private String descr ;

	/** Rayon */
	private String shelf ;

	/** Ean13 */
	private String ean13 ;

	/** Description */
	private String w$Desc ;


	public ShopArticle() {
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
	public ShopArticle(Integer id) {
		super();
		Key primaryKey = ShopArticleModel.buildPrimaryKey(id);
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
		Key primaryKey = ShopArticleModel.buildPrimaryKey(id);
		if (!primaryKey.isFull()) {
			return false;
		}
		ShopArticle dbInstance = DB.get($NAME, primaryKey, ctx);
		if (dbInstance != null) {
			syncFromBean(dbInstance);
			return true; 
		}
		return false; 
	}
	
	public ShopArticle(ShopArticle pShopArticle) {
		super(pShopArticle);
	}

	/** Entity name */
	@Override
	public String $_getName() {
		return "shopArticle";
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
	 * Get the value from field Descr.
	 *
	 * @return the value
	 */
	public String getDescr() {
		return this.descr;
	}
 
	/**
	 * Set the value from field Descr.
	 *
	 * @param descr : the value to set
	 */
	public void setDescr(final String descr) {
		this.descr = descr;
	}
	/**
	 * Get the value from field Shelf.
	 *
	 * @return the value
	 */
	public String getShelf() {
		return this.shelf;
	}
 
	/**
	 * Set the value from field Shelf.
	 *
	 * @param shelf : the value to set
	 */
	public void setShelf(final String shelf) {
		this.shelf = shelf;
	}
	/**
	 * Get the value from field Ean13.
	 *
	 * @return the value
	 */
	public String getEan13() {
		return this.ean13;
	}
 
	/**
	 * Set the value from field Ean13.
	 *
	 * @param ean13 : the value to set
	 */
	public void setEan13(final String ean13) {
		this.ean13 = ean13;
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
	/** Holder for the var names */
	public interface Var {
		/** Var ID */
		String ID = "id";
		/** Var NAME */
		String NAME = "name";
		/** Var DESCR */
		String DESCR = "descr";
		/** Var SHELF */
		String SHELF = "shelf";
		/** Var EAN13 */
		String EAN13 = "ean13";
	}
	

	/**
	 * Instance of ShopShelf matching the link ShopArticleLShelf based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopShelf matching the link pays if any, null otherwise.
	 */
	public ShopShelf getRef_ShopArticleRShelf(Context ctx) {
		return (ShopShelf) DB.getRef(this, ShopArticleModel.LINK_SHOP_ARTICLE_L_SHELF, ctx);
	}
	
	/**
	 * This method sets all variables of foreign key in the current ShopArticle object to match the primary key of the ShopShelf instance. <br/>
	 * If the ShopShelf instance is null, all foreign key variables will be set to null. <br/>
	 * <b>This method does not access database and won't automatically update database link.</b>
	 * 
	 * @param pShopShelf The newly targeted bean of link ShopArticleLShelf.
	 */
	public void setRef_ShopArticleRShelf(ShopShelf pShopShelf) {
		Key primaryKey = null;
		if (pShopShelf != null) {
			primaryKey = pShopShelf.getPrimaryKey();
		}
		setForeignKey(getModel().getLinkModel(ShopArticleModel.LINK_SHOP_ARTICLE_L_SHELF).getKeyName(), primaryKey);
	}

	/**
	 * This methods gets all instances of ShopListLArticle back referenced by the current ShopArticle instance via link ShopListLArticleLArticle. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopListLArticle related to the current ShopArticle via link ShopListLArticleLArticle.
	 */
	public Set<ShopListLArticle> getList_ShopListLArticleLArticle(Context ctx) {
		Set<ShopListLArticle> s = new HashSet<ShopListLArticle>();
		for (Entity e : DB.getLinkedEntities(this, ShopArticleModel.LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE, ctx)) {
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
		/** Import using EAN13. */
		int ACTION_50 = 50;
    }

	/** Nom de l'entité. */
	public static final String $NAME = "shopArticle";
}
