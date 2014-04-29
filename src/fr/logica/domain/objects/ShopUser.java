package fr.logica.domain.objects;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;


import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.domain.models.ShopUserModel;

/**
 * Entity ShopUser definition
 * 
 * @author CGI
 */
public class ShopUser extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;
	

	/** Login */
	private String login ;

	/** Nom */
	private String name ;

	/** Mot de passe */
	private String password ;

	/** Profil */
	private String profile = "USER";

	/** Description */
	private String internalCaption ;


	public ShopUser() {
		// Default constructor
		super();
	}

	/**
	 * Initialize a new domain with its primary key. <br/>
	 * <b>This method does not load a bean from database.</b><br/>
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * 
	 * @param login Login

	 */
	public ShopUser(String login) {
		super();
		Key primaryKey = ShopUserModel.buildPrimaryKey(login);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Load a bean from database based on its primary key
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * @param login Login
	 * @param ctx Current context with open database connection.
	 * @return	<code>true</code> if the bean has been loaded, <code>false</code> if no entity was found.  
	 */
	public boolean find(String login, RequestContext ctx) {
		Key primaryKey = ShopUserModel.buildPrimaryKey(login);
		if (!primaryKey.isFull()) {
			return false;
		}
		ShopUser dbInstance = DB.get(NAME, primaryKey, ctx);
		if (dbInstance != null) {
			syncFromBean(dbInstance);
			return true; 
		}
		return false; 
	}
	
	public ShopUser(ShopUser pShopUser) {
		super(pShopUser);
	}

	/** Entity name */
	@Override
	public String name() {
		return "shopUser";
	}

	/** Entity description */
	@Override
	public String description() {
		return getInternalCaption(); 
	}

	/**
	 * Get the value from field Login.
	 *
	 * @return the value
	 */
	public String getLogin() {
		return this.login;
	}
 
	/**
	 * Set the value from field Login.
	 *
	 * @param login : the value to set
	 */
	public void setLogin(final String login) {
		this.login = login;
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
	 * Get the value from field Password.
	 *
	 * @return the value
	 */
	public String getPassword() {
		return this.password;
	}
 
	/**
	 * Set the value from field Password.
	 *
	 * @param password : the value to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}
	


	/**
	 * Get the value from field Profile.
	 *
	 * @return the value
	 */
	public String getProfile() {
		return this.profile;
	}
 
	/**
	 * Set the value from field Profile.
	 *
	 * @param profile : the value to set
	 */
	public void setProfile(final String profile) {
		this.profile = profile;
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
	


	/** Holder for the var names */
	public interface Var {
		/** Var LOGIN */
		String LOGIN = "login";
		/** Var NAME */
		String NAME = "name";
		/** Var PASSWORD */
		String PASSWORD = "password";
		/** Var PROFILE */
		String PROFILE = "profile";
	}
	
	/** Holder for the defined values */
	public interface ValueList {
		public interface PROFILE {
			/** Utilisateur */
			String USER = "USER";
			/** Administrateur */
			String ADMIN = "ADMIN";
			/** Acheteur */
			String BUYER = "BUYER";
		}
	}

	/**
	 * This methods gets all instances of ShopList back referenced by the current ShopUser instance via link ShopArticleLUser. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b> <br/>
	 * <i>Note: if the PK is incomplete, an empty set will be returned.</i>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopList related to the current ShopUser via link ShopArticleLUser.
	 */
	public Set<ShopList> getList_ShopArticleLUser(RequestContext ctx) {
		Set<ShopList> s = new HashSet<ShopList>();
		if (this.getPrimaryKey() == null || !this.getPrimaryKey().isFull()) {
			// Do not get linked entities if PK is incomplete
			return s;
		}
		for (Entity e : DB.getLinkedEntities(this, ShopUserModel.LINK_SHOP_ARTICLE_L_USER, ctx)) {
			s.add((ShopList) e);
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
    }

	/** Nom de l'entité. */
	public static final String NAME = "shopUser";
	
	/**
	 * Clones the current bean.
	 */
	@Override
	public ShopUser clone() {
		ShopUser clone = (ShopUser) super.clone();
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
		profile = null; 
	}
}
