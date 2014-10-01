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
import fr.logica.domain.constants.ShopShelfConstants;

/**
 * Entity ShopShelf definition
 * 
 * @author CGI
 */
@EntityDef(dbName = "SHELF", primaryKey = { "code" })
@Links({
})
@Actions({
	@Action(code = 0, input = Input.NONE, persistence = Persistence.INSERT),
	@Action(code = 2, persistence = Persistence.UPDATE),
	@Action(code = 4, persistence = Persistence.DELETE, ui = UserInterface.READONLY),
	@Action(code = 5, persistence = Persistence.NONE, ui = UserInterface.READONLY)
})
public class ShopShelf extends Entity implements Serializable {
	/** serialVersionUID */
	public static final long serialVersionUID = 1L;

	/** Code */
	@EntityField(sqlName = "CODE", sqlType = SqlTypes.VARCHAR2, sqlSize = 10, isMandatory = true)
	private String code;

	/** Nom */
	@EntityField(sqlName = "NAME", sqlType = SqlTypes.VARCHAR2, sqlSize = 100, isMandatory = true)
	private String name;

	/** Position */
	@EntityField(sqlName = "POSITION", sqlType = SqlTypes.INTEGER, sqlSize = 3, isMandatory = true)
	private Integer position;

	/** Description */
	@EntityField(sqlName = "W$_DESC", sqlType = SqlTypes.VARCHAR2, sqlSize = 100, memory = fr.logica.business.EntityField.Memory.SQL, sqlExpr = ":tableAlias.NAME")
	private String internalCaption;

	/**
	 * Initialize a new ShopShelf.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
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
		Key primaryKey = buildPrimaryKey(code);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new ShopShelf from an existing ShopShelf.<br/>
	 * <b>All fields value are copied.</b>
	 */
	public ShopShelf(ShopShelf pShopShelf) {
		super(pShopShelf);
	}

	/**
	 * Generate a primary key for the entity
	 */
	public static synchronized Key buildPrimaryKey(String code) {
		KeyModel pkModel = new KeyModel(ShopShelfConstants.ENTITY_NAME);
		// FIXME : Récupérer la PK ...
		Key key = new Key(pkModel);
		key.setValue("code", code);

		return key;
	}

	/** Entity name */
	@Override
	public String name() {
		return ShopShelfConstants.ENTITY_NAME;
	}

	/** Entity description */
	@Override
	public String description() {
		return getInternalCaption(); 
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
	 * This methods gets all instances of ShopArticle back referenced by the current ShopShelf instance via link ShopArticleLShelf. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b> <br/>
	 * <i>Note: if the PK is incomplete, an empty set will be returned.</i>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every ShopArticle related to the current ShopShelf via link ShopArticleLShelf.
	 */
	public Set<ShopArticle> getList_ShopArticleLShelf(RequestContext ctx) {
		Set<ShopArticle> s = new HashSet<ShopArticle>();
		if (this.getPrimaryKey() == null || !this.getPrimaryKey().isFull()) {
			// Do not get linked entities if PK is incomplete
			return s;
		}
		for (Entity e : DB.getLinkedEntities(this, ShopShelfConstants.Links.LINK_SHOP_ARTICLE_L_SHELF, ctx)) {
			s.add((ShopArticle) e);
		}
		return s;
	}

	
	/**
	 * Clones the current bean.
	 */
	@Override
	public ShopShelf clone() {
		ShopShelf clone = (ShopShelf) super.clone();
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
