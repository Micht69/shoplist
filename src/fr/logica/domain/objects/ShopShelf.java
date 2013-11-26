package fr.logica.domain.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


import fr.logica.business.Context;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.db.DB;
import fr.logica.domain.models.ShopShelfModel;

/**
 * Entity ShopShelf definition
 * 
 * @author CGI
 */
public class ShopShelf extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;
	

	/** Code */
	private String code ;

	/** Nom */
	private String name ;

	/** Position */
	private Integer position ;

	/** Description */
	private String w$Desc ;


	public ShopShelf() {
		// Default constructor
		super();
	}

	/**
	 * Initialize a new domain with its primary key. <br/>
	 * <b>This method does not load a bean from database.</b><br/>
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * 
	 * @param code Code

	 */
	public ShopShelf(String code) {
		super();
		Key primaryKey = ShopShelfModel.buildPrimaryKey(code);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Load a bean from database based on its primary key
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * @param code Code

	 * @param ctx Current context with open database connection.
	 * @return	<code>true</code> if the bean has been loaded, <code>false</code> if no entity was found.  
	 */
	public boolean find(String code, Context ctx) {
		Key primaryKey = ShopShelfModel.buildPrimaryKey(code);
		if (!primaryKey.isFull()) {
			return false;
		}
		ShopShelf dbInstance = DB.get($NAME, primaryKey, ctx);
		if (dbInstance != null) {
			syncFromBean(dbInstance);
			return true; 
		}
		return false; 
	}
	
	public ShopShelf(ShopShelf pShopShelf) {
		super(pShopShelf);
	}

	/** Entity name */
	@Override
	public String $_getName() {
		return "shopShelf";
	}


	/** Entity description */
	@Override
	public String $_getDesc() {
		return "name";
	}

	/**
	 * Get the value from field Code.
	 *
	 * @return the value
	 */
	public String getCode() {
		return this.code;
	}
 
	/**
	 * Set the value from field Code.
	 *
	 * @param code : the value to set
	 */
	public void setCode(final String code) {
		this.code = code;
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
	 * Get the value from field Position.
	 *
	 * @return the value
	 */
	public Integer getPosition() {
		return this.position;
	}
 
	/**
	 * Set the value from field Position.
	 *
	 * @param position : the value to set
	 */
	public void setPosition(final Integer position) {
		this.position = position;
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
		/** Var CODE */
		String CODE = "code";
		/** Var NAME */
		String NAME = "name";
		/** Var POSITION */
		String POSITION = "position";
	}
	

	/**
	 * This methods gets all instances of ShopArticle back referenced by the current ShopShelf instance via link ShopArticleLShelf. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopArticle related to the current ShopShelf via link ShopArticleLShelf.
	 */
	public Set<ShopArticle> getList_ShopArticleLShelf(Context ctx) {
		Set<ShopArticle> s = new HashSet<ShopArticle>();
		for (Entity e : DB.getLinkedEntities(this, ShopShelfModel.LINK_SHOP_ARTICLE_L_SHELF, ctx)) {
			s.add((ShopArticle) e);
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
    }

	/** Nom de l'entité. */
	public static final String $NAME = "shopShelf";
}
