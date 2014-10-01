package fr.logica.domain.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import fr.logica.business.Action.*;
import fr.logica.business.Entity;
import fr.logica.business.EntityField.SqlTypes;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.domain.annotations.*;
import fr.logica.domain.constants.ShopUserConstants;

/**
 * Entity ShopUser definition
 * 
 * @author CGI
 */
@EntityDef(dbName = "USER", primaryKey = { "login" })
@Links({
})
@Actions({
	@Action(code = 0, input = Input.NONE, persistence = Persistence.INSERT),
	@Action(code = 2, persistence = Persistence.UPDATE),
	@Action(code = 4, persistence = Persistence.DELETE, ui = UserInterface.READONLY),
	@Action(code = 5, persistence = Persistence.NONE, ui = UserInterface.READONLY)
})
public class ShopUser extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Login */
	@EntityField(sqlName = "LOGIN", sqlType = SqlTypes.VARCHAR2, sqlSize = 10, isMandatory = true)
	private String login;

	/** Nom */
	@EntityField(sqlName = "NAME", sqlType = SqlTypes.VARCHAR2, sqlSize = 100, isMandatory = true)
	private String name;

	/** Mot de passe */
	@EntityField(sqlName = "PASSWORD", sqlType = SqlTypes.VARCHAR2, sqlSize = 100)
	private String password;

	/** Profil */
	@EntityField(sqlName = "PROFILE", sqlType = SqlTypes.VARCHAR2, sqlSize = 10, defaultValue = "USER")
	@DefinedValues({
			@DefinedValue(code = "USER", label = "shopUser.profile.USER", value = "USER", isDefault = true), // Utilisateur
			@DefinedValue(code = "ADMIN", label = "shopUser.profile.ADMIN", value = "ADMIN"), // Administrateur
			@DefinedValue(code = "BUYER", label = "shopUser.profile.BUYER", value = "BUYER") // Acheteur
	})
	private String profile;

	/** Description */
	@EntityField(sqlName = "W$_DESC", sqlType = SqlTypes.VARCHAR2, sqlSize = 100, memory = fr.logica.business.EntityField.Memory.SQL, sqlExpr = ":tableAlias.NAME")
	private String internalCaption;

	/**
	 * Initialize a new ShopUser.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
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
		Key primaryKey = buildPrimaryKey(login);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new ShopUser from an existing ShopUser.<br/>
	 * <b>All fields value are copied.</b>
	 */
	public ShopUser(ShopUser pShopUser) {
		super(pShopUser);
	}

	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(String login) {
		KeyModel pkModel = new KeyModel(ShopUserConstants.ENTITY_NAME);
		// FIXME : Récupérer la PK ...
		Key key = new Key(pkModel);
		key.setValue("login", login);

		return key;
	}

	/** Entity name */
	@Override
	public String name() {
		return ShopUserConstants.ENTITY_NAME;
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
		for (Entity e : DB.getLinkedEntities(this, ShopUserConstants.Links.LINK_SHOP_ARTICLE_L_USER, ctx)) {
			s.add((ShopList) e);
		}
		return s;
	}

	
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
