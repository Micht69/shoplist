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
import fr.logica.domain.constants.ShopArticleConstants;

/**
 * Entity ShopArticle definition
 * 
 * @author CGI
 */
@EntityDef(dbName = "ARTICLE", primaryKey = { "id" })
@Links({
	@Link(name = "shopArticleLShelf", targetEntity = "shopShelf", fields = { "shelf" })
})
@Actions({
	@Action(code = 0, input = Input.NONE, persistence = Persistence.INSERT),
	@Action(code = 2, persistence = Persistence.UPDATE),
	@Action(code = 4, persistence = Persistence.DELETE, ui = UserInterface.READONLY),
	@Action(code = 5, persistence = Persistence.NONE, ui = UserInterface.READONLY),
	@Action(code = 50, pageName = "SHOP_ARTICLE_CREATE", input = Input.NONE, persistence = Persistence.INSERT)
})
public class ShopArticle extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** ID */
	@EntityField(sqlName = "ID", sqlType = SqlTypes.INTEGER, sqlSize = 10, isMandatory = true, isAutoIncrementField = true)
	private Integer id;

	/** Nom */
	@EntityField(sqlName = "NAME", sqlType = SqlTypes.VARCHAR2, sqlSize = 128, isMandatory = true, isLookupField = true)
	private String name;

	/** Description */
	@EntityField(sqlName = "DESCR", sqlType = SqlTypes.VARCHAR2, sqlSize = 500)
	private String descr;

	/** Rayon */
	@EntityField(sqlName = "SHELF", sqlType = SqlTypes.VARCHAR2, sqlSize = 10, isMandatory = true)
	private String shelf;

	/** Ean13 */
	@EntityField(sqlName = "EAN13", sqlType = SqlTypes.VARCHAR2, sqlSize = 13)
	private String ean13;

	/** Description */
	@EntityField(sqlName = "W$_DESC", sqlType = SqlTypes.VARCHAR2, sqlSize = 128, memory = fr.logica.business.EntityField.Memory.SQL, sqlExpr = ":tableAlias.NAME")
	private String internalCaption;

	/**
	 * Initialize a new ShopArticle.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
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
		Key primaryKey = buildPrimaryKey(id);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new ShopArticle from an existing ShopArticle.<br/>
	 * <b>All fields value are copied.</b>
	 */
	public ShopArticle(ShopArticle pShopArticle) {
		super(pShopArticle);
	}

	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(Integer id) {
		KeyModel pkModel = new KeyModel(ShopArticleConstants.ENTITY_NAME);
		// FIXME : Récupérer la PK ...
		Key key = new Key(pkModel);
		key.setValue("id", id);

		return key;
	}

	/** Entity name */
	@Override
	public String name() {
		return ShopArticleConstants.ENTITY_NAME;
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
	 * Instance of ShopShelf matching the link ShopArticleLShelf based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of ShopShelf matching the link pays if any, null otherwise.
	 */
	public ShopShelf getRef_ShopArticleRShelf(RequestContext ctx) {
		return (ShopShelf) DB.getRef(this, ShopArticleConstants.Links.LINK_SHOP_ARTICLE_L_SHELF, ctx);
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
		setForeignKey(ShopArticleConstants.Links.LINK_SHOP_ARTICLE_L_SHELF, primaryKey);
	}

	/**
	 * This methods gets all instances of ShopListLArticle back referenced by the current ShopArticle instance via link ShopListLArticleLArticle. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b> <br/>
	 * <i>Note: if the PK is incomplete, an empty set will be returned.</i>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopListLArticle related to the current ShopArticle via link ShopListLArticleLArticle.
	 */
	public Set<ShopListLArticle> getList_ShopListLArticleLArticle(RequestContext ctx) {
		Set<ShopListLArticle> s = new HashSet<ShopListLArticle>();
		if (this.getPrimaryKey() == null || !this.getPrimaryKey().isFull()) {
			// Do not get linked entities if PK is incomplete
			return s;
		}
		for (Entity e : DB.getLinkedEntities(this, ShopArticleConstants.Links.LINK_SHOP_LIST_L_ARTICLE_L_ARTICLE, ctx)) {
			s.add((ShopListLArticle) e);
		}
		return s;
	}

	
	/**
	 * Clones the current bean.
	 */
	@Override
	public ShopArticle clone() {
		ShopArticle clone = (ShopArticle) super.clone();
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
	}
}
