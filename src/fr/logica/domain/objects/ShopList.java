package fr.logica.domain.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import fr.logica.business.DateTimeUpgraded;
import java.util.Date;

import fr.logica.business.Action.*;
import fr.logica.business.Entity;
import fr.logica.business.EntityField.SqlTypes;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.domain.annotations.*;
import fr.logica.domain.constants.ShopListConstants;

/**
 * Entity ShopList definition
 * 
 * @author CGI
 */
@EntityDef(dbName = "LIST", primaryKey = { "id" })
@Links({
	@Link(name = "shopArticleLUser", targetEntity = "shopUser", fields = { "user" })
})
@Actions({
	@Action(code = 0, pageName = "SHOP_LIST_CREATE", input = Input.NONE, persistence = Persistence.INSERT),
	@Action(code = 2, persistence = Persistence.UPDATE),
	@Action(code = 4, persistence = Persistence.DELETE, ui = UserInterface.READONLY),
	@Action(code = 5, persistence = Persistence.NONE, ui = UserInterface.READONLY),
	@Action(code = 50, pageName = "SHOP_LIST_SHOPPING", persistence = Persistence.UPDATE)
})
public class ShopList extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** ID */
	@EntityField(sqlName = "ID", sqlType = SqlTypes.INTEGER, sqlSize = 10, isMandatory = true, isAutoIncrementField = true)
	private Integer id;

	/** Titre */
	@EntityField(sqlName = "NAME", sqlType = SqlTypes.VARCHAR2, sqlSize = 100, isMandatory = true)
	private String name;

	/** Créateur */
	@EntityField(sqlName = "USER", sqlType = SqlTypes.VARCHAR2, sqlSize = 10, isMandatory = true)
	private String user;

	/** Date de création */
	@EntityField(sqlName = "CREATE_DATE", sqlType = SqlTypes.DATE, sqlSize = 0)
	private Date createDate = fr.logica.business.DateUtils.today();

	/** Description */
	@EntityField(sqlName = "W$_DESC", sqlType = SqlTypes.VARCHAR2, sqlSize = 200, memory = fr.logica.business.EntityField.Memory.SQL, sqlExpr = ":tableAlias.NAME")
	private String internalCaption;

	/** Nbr d'articles */
	@EntityField(sqlName = "ARTICLE_COUNT", sqlType = SqlTypes.INTEGER, sqlSize = 3, memory = fr.logica.business.EntityField.Memory.ALWAYS)
	private Integer articleCount;

	/**
	 * Initialize a new ShopList.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
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
		Key primaryKey = buildPrimaryKey(id);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new ShopList from an existing ShopList.<br/>
	 * <b>All fields value are copied.</b>
	 */
	public ShopList(ShopList pShopList) {
		super(pShopList);
	}

	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(Integer id) {
		KeyModel pkModel = new KeyModel(ShopListConstants.ENTITY_NAME);
		// FIXME : Récupérer la PK ...
		Key key = new Key(pkModel);
		key.setValue("id", id);

		return key;
	}

	/** Entity name */
	@Override
	public String name() {
		return ShopListConstants.ENTITY_NAME;
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

	/**
	 * Instance of ShopUser matching the link ShopArticleLUser based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopUser matching the link pays if any, null otherwise.
	 */
	public ShopUser getRef_ShopListUserFk(RequestContext ctx) {
		return (ShopUser) DB.getRef(this, ShopListConstants.Links.LINK_SHOP_ARTICLE_L_USER, ctx);
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
		setForeignKey(ShopListConstants.Links.LINK_SHOP_ARTICLE_L_USER, primaryKey);
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
		for (Entity e : DB.getLinkedEntities(this, ShopListConstants.Links.LINK_SHOP_LIST_L_ARTICLE_L_LIST, ctx)) {
			s.add((ShopListLArticle) e);
		}
		return s;
	}

	
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
