package fr.logica.business;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.business.controller.Response;
import fr.logica.business.data.ListData;
import fr.logica.business.data.ScheduleEvent;
import fr.logica.business.DateTimeUpgraded;
import fr.logica.business.EntityField;
import fr.logica.business.MessageUtils;
import fr.logica.db.DB;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.geocoding.MapGeocoding;
import fr.logica.reflect.DomainUtils;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;

@SuppressWarnings("unused")
public abstract class AbstractDomainLogic<E extends Entity> {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AbstractDomainLogic.class);

	public final List<Key> internalDoCustomAction(Request<E> request, E entity, List<Key> keys, RequestContext ctx) {
		try {
			return doCustomAction(request, entity, keys, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	public final List<Key> internalDoCustomAction(Request<E> request, E entity, RequestContext ctx) {
		try {
			return doCustomAction(request, entity, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Custom action on one / many elements. This method is called once for all selected item.
	 * 
	 * @param bean Entity to process.
	 * @param action Action called on entity.
	 * @param keys Remaining keys to process.
	 * @param ctx Current applicative context.
	 * @return Remaining keys to process. If null, batch processing will stop after this element.
	 */
	public abstract List<Key> doCustomAction(Request<E> request, E entity, RequestContext ctx);

	public abstract List<Key> doCustomAction(Request<E> request, E entity, List<Key> keys, RequestContext ctx);

    public final boolean internalDoCheck(E bean, Action action, RequestContext ctx) {
		boolean errors = false;
		try {
			for (String varName : bean.getModel().getFields()) {
				errors |= internalDoVarCheck(bean, action, varName, ctx);
			}
			errors |= doCheck(bean, action, ctx);
		} catch (FunctionalException ex) {
			LOGGER.error(ex);
			ctx.getMessages().addAll(ex.getMessages());
			errors = true;
		}
		return errors;
    }

    /**
     * Check consistency of an entity. Default behavior will call varCheck on every variable.
     * 
     * @param bean
     *            Entity instance to check.
     * @param action
     *            Action to check.
     * @param ctx
     *            Current context.
     * @throws FunctionalException
     *             Exception thrown when entity is invalid.
     * 
     */
    public abstract boolean doCheck(E bean, Action action, RequestContext ctx) throws FunctionalException;

    public final String internalDoDescription(E bean, RequestContext ctx) {
		try {
	        return doDescription(bean, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Description of the current entity instance.
     * 
     * @param bean
     *            Entity instance (can be null).
     * @param ctx
     *            Current applicative Context
     * @return String describing the entity instance. Default behavior will look for a descriptionField declared in entity, if no description
     *         field exists, it will detail primary key fields.
     */
    public abstract String doDescription(E bean, RequestContext ctx);

    public final boolean internalDoVarCheck(E bean, Action action, String varName, RequestContext ctx) {
		boolean errors = false;
		try {
	        // Check not-null fields
	        if (bean.invokeGetter(varName) == null && !bean.getModel().isAutoIncrementField(varName)
	                && (bean.getModel().getField(varName).isMandatory() || internalDoVarIsMandatory(bean, varName, action, ctx))) {
	            String varLabel = internalUiVarCaption(bean, varName, action, ctx);
	
	            if (null == varLabel) {
	                varLabel = MessageUtils.getInstance(ctx).getVarTitle(bean.name(), varName);
	            }
	            ctx.getMessages().add(new Message(varLabel + " : Vous devez indiquer une valeur.", Severity.ERROR));
	            errors = true;
	        }
	        // Validate allowed values
	 		EntityField field = bean.getModel().getField(varName);
	 		if (field.hasDefinedValues()) {
	 			Object value = bean.invokeGetter(varName);
	 			boolean isDefinedValue = false;
	 			for (Object definedObj : field.getValues()) {
	 				if (value == null && definedObj == null || value != null && value.equals(definedObj)) {
	 					// Input value exists amongst defined values
	 					isDefinedValue = true;
	 					break;
	 				}
	 			}
	 			if (!isDefinedValue) {
	 				ctx.getMessages().add(new Message(MessageUtils.getInstance(ctx).getMessage(
	 						"uiControlerModel.fieldhasNotAllowedValue",
	 						new Object[] { (Object) field.getDisplayText() }), Severity.ERROR));
	 				errors = true;
	 			}
	 		}
            errors |= doVarCheck(bean, varName, action, ctx);
        } catch (FunctionalException ex) {
            LOGGER.error(ex);
            ctx.getMessages().add(new Message(ex.getMessage(), Severity.ERROR));
            errors = true;
		} 
		return errors; 
	}

    /**
     * Variable check. Default behavior will check mandatory variables and datatypes.
     * 
     * @param bean
     *            Current bean instance.
     * @param varName
     *            Checked variable.
     * @param action
     *            Current Action
     * @param ctx
     *            Current applicative context.
     * @throws FunctionalException
     *             If var is invalid.
     */
    public abstract boolean doVarCheck(E bean, String varName, Action action, RequestContext ctx) throws FunctionalException;

    public final boolean internalDoVarIsMandatory(E bean, String varName, Action action, RequestContext ctx) {
		try {
			return doVarIsMandatory(bean, varName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines if a variable is mandatory in a specific action. Default behavior will rely on entity model definition.
     * 
     * @param bean
     *            Current entity instance.
     * @param varName
     *            Checked variable
     * @param action
     *            Current action
     * @param ctx
     *            Current applicative context
     * @return <code>true</code> when varName is mandatory, <code>false</code> otherwise.
     */
    public abstract boolean doVarIsMandatory(E bean, String varName, Action action, RequestContext ctx);

    @Deprecated
    public final Object internalDoVarValue(Map<String, Object> vars, String domainName, String varName, RequestContext ctx) {
		try {
	        return doVarValue(vars, domainName, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Get calculated variable value from current entity or a query result set. This method is deprecated and will be deleted in future releases.
     * You should use the following methods instead<br/>
     * - Memory variables in domain objects, usually used in action pages, are computed in doVarValue(E bean, String varName, RequestContext ctx)<br/>
     * - Memory variables displayed in lists are computed in uiListVarValue(Map<String, Object> vars, String queryName, String domainName, String
     * varName, RequestContext ctx)
     * 
     * @param vars Variables used for computation
     * @param varName Calculated Variable
     * @param ctx Current applicative context
     * @return Object containing variable value.
     */
    @Deprecated
    public abstract Object doVarValue(Map<String, Object> vars, String domainName, String varName, RequestContext ctx);

    public final Object internalUiListVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, RequestContext ctx) {
		try {
			return uiListVarValue(vars, queryName, domainName, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Computes a memory variable in a list. This method is called once for each row for each memory variable of the row.
     * 
     * @param vars All data contained in the row of the result set.
     * @param queryName the query name
     * @param varName Calculated Variable
     * @param ctx Current applicative context
     * @return Object containing variable value.
     */
    public abstract Object uiListVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, RequestContext ctx);

    public final Object internalDoVarValue(E bean, String varName, RequestContext ctx) {
		try {
			return doVarValue(bean, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Computes a memory variable value for a domain object instance.
     * 
     * @param vars The current domain object instance.
     * @param varName Calculated Variable
     * @param ctx Current applicative context
     * @return Object containing variable value.
     */
    public abstract Object doVarValue(E bean, String varName, RequestContext ctx);

    public final void internalDbSecure(DbQuery query, RequestContext ctx) {
		try {
	        dbSecure(query, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Query data secure. This method will be called on every query execution.
     * 
     * @param query
     *            Current executed query.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void dbSecure(DbQuery query, RequestContext ctx);

    public final void internalDbOnSave(E bean, Action action, RequestContext ctx) {
		try {
	        for (Field field : bean.getFields()) {
	            String fieldName = field.getName();
	
	            if (Geometry.class.equals(field.getType()) && null == bean.invokeGetter(fieldName)) {
	                String locationFieldName = Constants.GEOMETRY_ADDRESS + fieldName.substring(0, 1).toUpperCase()
	                        + fieldName.substring(1);
	
	                if (null == bean.getModel().getField(locationFieldName)) {
	                    continue;
	                }
	                String location = (String) bean.invokeGetter(locationFieldName);
	
	                if (null == location || location.isEmpty()) {
	                    location = (String) internalDoVarValue(bean, locationFieldName, ctx);
	                    if (location == null || location.isEmpty()) {
	                        location = (String) internalDoVarValue(bean.dump(), bean.name(), locationFieldName, ctx);
	                    }
	                }
	
	                if (null != location && !location.isEmpty()) {
	                    MapGeocoding geocodingService = new MapGeocoding();
	                    Geometry geom = geocodingService.getGeometryPoint(location);
	
	                    if (null != geom) {
	                        bean.invokeSetter(fieldName, geom);
	                    }
	                }
	            }
	        }
	        for (String fieldName : bean.getModel().getFields()) {
				EntityField field = bean.getModel().getField(fieldName);
				if (field.getDefaultValue() != null && bean.invokeGetter(fieldName) == null) {
					Object defaultValue = field.getDefaultValue();
					// FIXME move the following code into getDefaultValue() in EntityField
					if ("DATE".equals(field.getSqlType())) {
						bean.invokeSetter(fieldName, ((DateTimeUpgraded) defaultValue).getDate());
					} else if ("TIME".equals(field.getSqlType())) {
						bean.invokeSetter(fieldName, ((DateTimeUpgraded) defaultValue).getTime());
					} else if ("TIMESTAMP".equals(field.getSqlType())) {
						bean.invokeSetter(fieldName, ((DateTimeUpgraded) defaultValue).getTimestamp());
					} else {
						bean.invokeSetter(fieldName, defaultValue);
					}
				}
			}
	        dbOnSave(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Called before entity persistance.
     * 
     * @param bean
     *            Entity instance to be persisted.
     * @param action
     *            Current action.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void dbOnSave(E bean, Action action, RequestContext ctx);

    public final void internalDbOnDelete(E bean, Action action, RequestContext ctx) {
		try {
			dbOnDelete(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Called before entity removal.
     * 
     * @param bean
     *            Entity instance to be removed.
     * @param action
     *            Current action.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void dbOnDelete(E bean, Action action, RequestContext ctx);

    public final void internalDbPostLoad(E bean, Action action, RequestContext ctx) {
		try {
			if (action.getInput() == Input.ONE && action.getPersistence() == Persistence.INSERT) {
				for (String fieldName : bean.getPrimaryKey().getModel().getFields()) {
					if (bean.getModel().isAutoIncrementField(fieldName)) {
						// Copy action with a auto increment field, we set it to null
						bean.invokeSetter(fieldName, null);
					}
				}
			}
			dbPostLoad(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Called after entity loading from database.
     * 
     * @param bean
     *            Entity instance loaded from database.
     * @param action
     *            Current action.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void dbPostLoad(E bean, Action action, RequestContext ctx);

    public final void internalDbPostSave(E bean, Action action, RequestContext ctx) {
		try {
	        dbPostSave(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Called after entity persistance into database.
     * 
     * @param bean
     *            Entity instance persisted into database.
     * @param action
     *            Current action.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void dbPostSave(E bean, Action action, RequestContext ctx);

    public final void internalDbPostDelete(E bean, Action action, RequestContext ctx) {
		try {
	        dbPostDelete(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Called after entity removal from database.
     * 
     * @param bean
     *            Entity instance removed from database.
     * @param action
     *            Current action.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void dbPostDelete(E bean, Action action, RequestContext ctx);

    public final String internalUiActionTitle(Response<E> response, RequestContext ctx) {
		try {
	        return uiActionTitle(response, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Get the title of an action page
     * 
     * @param bean
     *            Current entity instance
     * @param action
     *            Current action
     * @param ctx
     *            Current applicative context
     * @return String displayed as action page title. Default behavior is "<Action> on entity <EntityDescription>"
     */
    public abstract String uiActionTitle(Response<E> response, RequestContext ctx);

    public final String internalUiVarCaption(E bean, String varName, Action action, RequestContext ctx) {
		try {
			return uiVarCaption(bean, varName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Variable caption text displayed on an action page.
     * 
     * @param bean
     *            Current entity instance.
     * @param varName
     *            Variable to process
     * @param action
     *            Current action
     * @param ctx
     *            Current applicative context.
     * @return String containing the caption displayed on the variable element in action page. Return <code>null</code> to keep default value.
     */
    public abstract String uiVarCaption(E bean, String varName, Action action, RequestContext ctx);

    public final String internalUiLinkCaption(E bean, String linkName, Action action, RequestContext ctx) {
		try {
	        return uiLinkCaption(bean, linkName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    public abstract String uiLinkCaption(E bean, String linkName, Action action, RequestContext ctx);

    public final String internalUiListColumnCaption(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		try {
			return uiListColumnCaption(query, link, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Column caption text displayed on list / link list.
     * 
     * @param query
     *            Executed query.
     * @param link
     *            Link (link list only)
     * @param varName
     *            Variable
     * @param ctx
     *            Current applicative context.
     * @return String containing the caption displayed on the column header element of a list. Return <code>null</code> to keep default value.
     */
    public abstract String uiListColumnCaption(DbQuery query, LinkModel link, String varName, RequestContext ctx);

	public final boolean internalUiListColumnIsVisible(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		try {
			return uiListColumnIsVisible(query, link, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines if a variable is displayed into a list.
     * 
     * @param query
     *            Executed query.
     * @param link
     *            Link (link list only).
     * @param varName
     *            Variable.
     * @param ctx
     *            Current applicative context.
     * @return {@code true} is the variable should be displayed, {@code false} otherwise.
     */
    public abstract boolean uiListColumnIsVisible(DbQuery query, LinkModel link, String varName, RequestContext ctx);

	public final boolean internalUiVarIsVisible(Entity bean, String varName, Action action, RequestContext ctx) {
		try {
			return uiVarIsVisible(bean, varName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}
	
	/**
	 * Defines if a variable is displayed in an action page.
	 * 
	 * @param bean Current entity instance.
	 * @param varName Variable to check.
	 * @param action Current action.
	 * @param ctx Current applicative context.
	 * @return <code>true</code> when varName is visible, <code>false</code> otherwise.
	 */
	public abstract boolean uiVarIsVisible(Entity bean, String varName, Action action, RequestContext ctx);
	
    public final boolean internalUiLinkIsVisible(Entity entity, String linkName, Action action, RequestContext ctx) {
		try {
			return uiLinkIsVisible(entity, linkName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines if a link is displayed in an action page.
     * 
     * @param bean
     *            Current entity instance.
     * @param link
     *            Link to check.
     * @param action
     *            Current action.
     * @param ctx
     *            Current applicative context.
     * @return <code>true</code> when link is visible, <code>false</code> otherwise.
     */
    public abstract boolean uiLinkIsVisible(Entity entity, String linkName, Action action, RequestContext ctx);

	public final boolean internalUiGroupIsVisible(Entity bean, String groupName, Action action, RequestContext ctx) {
		try {
			return uiGroupIsVisible(bean, groupName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Defines if a block / group is displayed in a page.
	 * 
	 * @param page Page to display.
	 * @param bean Current entity instance.
	 * @param groupName Group to check.
	 * @param ctx Current applicative context.
	 * @return <code>true</code> when group is visible, <code>false</code> otherwise.
	 */
	public abstract boolean uiGroupIsVisible(Entity bean, String groupName, Action action, RequestContext ctx);

    public final boolean internalUiListIsProtected(Entity targetEntity, String linkName, String queryName, Action action, RequestContext ctx) {
		try {
			return uiListIsProtected(targetEntity, linkName, queryName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines if a <b>link</b> list is protected (data cannot be modified).
     * 
     * @param bean
     *            Current entity instance
     * @param queryName
     *            Query name used to display list
     * @param link
     *            Base link of link list
     * @param action
     *            Current action
     * @param ctx
     *            Current Applicative Context
     * @return <code>true</code> when list is protected, <code>false</code> otherwise.
     */
    public abstract boolean uiListIsProtected(Entity targetEntity, String linkName, String queryName, Action action, RequestContext ctx);

    public final boolean internalUiListIsReadOnly(E bean, String linkName, String queryName, Action action, RequestContext ctx) {
		try {
			return uiListIsReadOnly(bean, linkName, queryName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines if a <b>link</b> list is readonly (no action).
     * 
     * @param bean
     *            Current entity instance
     * @param queryName
     *            Query name used to display list
     * @param link
     *            Base link of link list
     * @param action
     *            Current action
     * @param ctx
     *            Current Applicative Context
     * @return <code>true</code> when list is protected, <code>false</code> otherwise.
     */
    public abstract boolean uiListIsReadOnly(E bean, String linkName, String queryName, Action action, RequestContext ctx);


	public final boolean internalUiVarIsProtected(Entity bean, String varName, Action action, RequestContext ctx) {
		try {
			if (action.getUi() == UserInterface.READONLY) {
				return true;
			}
			return uiVarIsProtected(bean, varName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Defines if a variable is protected in a specific action. Default behavior depends on action type.
	 * 
	 * @param bean Current entity instance.
	 * @param varName Checked variable
	 * @param action Current action
	 * @param ctx Current applicative context
	 * @return <code>true</code> when varName is protected, <code>false</code> otherwise.
	 */
	public abstract boolean uiVarIsProtected(Entity bean, String varName, Action action, RequestContext ctx);

    public final void internalUiActionOnLoad(Response<E> response, RequestContext ctx) {
		try {
			uiActionOnLoad(response, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}
    
    /**
     * Allows custom behavior before page display.
     * 
     * @param bean
     *            Current page
     * @param ctx
     *            Current applicative context
     */
    public abstract void uiActionOnLoad(Response<E> response, RequestContext ctx);

    public final void internalUiActionOnValidation(Request<E> request, RequestContext ctx) {
		try {
	        uiActionOnValidation(request, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Allows custom behavior before action page validation
     * 
     * @param bean
     *            Current entity instance
     * @param action
     *            Current action
     * @param ctx
     *            Current applicative context
     */
    public abstract void uiActionOnValidation(Request<E> request, RequestContext ctx);

	public final boolean internalUiTabIsVisible(Entity bean, String tabName, Action action, RequestContext ctx) {
		try {
			return uiTabIsVisible(bean, tabName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Defines if a tab is visible in an action page.
	 * 
	 * @param page Page to display.
	 * @param bean Current entity instance.
	 * @param tab Identifier of the tab to check.
	 * @param ctx Current applicative context
	 * @return <code>true</code> when tab is visible, <code>false</code> otherwise.
	 */
	public abstract boolean uiTabIsVisible(Entity bean, String tabName, Action action, RequestContext ctx);

	public final String internalUiTabToOpen(Entity bean, String tabPanelName, Action action, RequestContext ctx) {
		try {
			return uiTabToOpen(bean, tabPanelName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Defines the tab to open in an action page containing tabs.
	 * 
	 * @param page Page to display.
	 * @param bean Current entity instance.
	 * @param tabPanelName Identifier of the tab panel.
	 * @param ctx Current applicative context.
	 * @return Tab identifier of the tab to display on page loading.
	 */
	public abstract String uiTabToOpen(Entity bean, String tabPanelName, Action action, RequestContext ctx);

    public final Map<Key, String> internalUiLinkLoadCombo(Entity bean, LinkModel link, DbQuery filterQuery, Action action, RequestContext ctx) {
		try {
			return uiLinkLoadCombo(bean, link, filterQuery, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}
    
    /**
     * Method called to load a combobox-based link.
     * 
     * @param bean
     *            Current entity instance. (null if list page criteria).
     * @param link
     *            Link name between entity and comboboxed-entity.
     * @param filterQuery
     *            Optional filter query, allow users to filter displayed data (null if not supplied).
     * @param page
     *            Current page.
     * @param ctx
     *            Current applicative context.
     * @return Map containing primary key of each element displayed in the combobox and element descriptions.
     */
    public abstract Map<Key, String> uiLinkLoadCombo(Entity bean, LinkModel link, DbQuery filterQuery, Action action, RequestContext ctx);

	public final Request<?> internalUiCtrlNextAction(Request<E> request, RequestContext ctx) {
		try {
	        return uiCtrlNextAction(request, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines the next action to occur after current action completion.
     * 
     * @param bean
     *            Current entity instance.
     * @param action
     *            Current action
     * @param ctx
     *            Current applicative context
     * @return New action to start. Return null when no custom action should occurs after current action completion.
     */
    public abstract Request<?> uiCtrlNextAction(Request<E> request, RequestContext ctx);

    public final Response<?> internalUiCtrlOverrideAction(Response<E> response, RequestContext ctx) {
		try {
	        return uiCtrlOverrideAction(response, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Allows action override. This method is called before any other customizable method in the process.
     * 
     * @param page
     *            Current loaded action page
     * @param ctx
     *            Current applicative context
     * @return ActionPage to start instead of current action. Return null if current action should be executed.
     */
    public abstract Response<?> uiCtrlOverrideAction(Response<E> response, RequestContext ctx);

    public final List<Key> internalUiCtrlMenuAction(Action action, RequestContext ctx) {
		try {
			return uiCtrlMenuAction(action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Defines the key to use when action is launched from menu.
     * 
     * @param action
     *            Action launched from application menu.
     * @param ctx
     *            Current applicative context.
     * @return Key to use in action processing. Return null will end in a new instance of the entity.
     */
    public abstract List<Key> uiCtrlMenuAction(Action action, RequestContext ctx);

	public final void internalUiListPrepare(DbQuery query, E entity, Action action, String linkName, Entity linkedEntity, RequestContext ctx) {
		try {
			if (!uiListPrepare(query, entity, action, linkName, linkedEntity, ctx)) {
				defaultUiListPrepare(query, entity, action);
			}
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Allows modifications on a list page query before execution.
     * 
     * @param query
     *            The list page query.
     * @param criteria
     *            Criteria bean with user defined values.
     * @param ctx
     *            Current applicative context.
     * 
     * @return <code>true</code> if default query preparation should be skipped, <code>false</code> if query should still be prepared with user
     *         input in Criteria object.
     */
	public abstract boolean uiListPrepare(DbQuery query, E entity, Action action, String linkName, Entity linkedEntity, RequestContext ctx);

	public final void internalUiListPrepare(DbQuery query, String criteria, Action action, String linkName, Entity linkedEntity, RequestContext ctx) {
		try {
			if (!uiListPrepare(query, criteria, action, linkName, linkedEntity, ctx)) {
				defaultUiListPrepare(query, criteria, action);
			}
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Allows modifications on a list page query before execution.
     * 
     * @param query
     *            The list page query.
     * @param criteria
     *            Criteria Search string.
     * @param ctx
     *            Current applicative context.
     * 
     * @return <code>true</code> if default query preparation should be skipped, <code>false</code> if query should still be prepared with user
     *         input in Criteria object.
     */
	public abstract boolean uiListPrepare(DbQuery query, String criteria, Action action, String linkName, Entity linkedEntity, RequestContext ctx);

    public final void internalUiListPrepare(DbQuery query, Entity parentEntity, String linkName, RequestContext ctx) {
		try {
	        uiListPrepare(query, parentEntity, linkName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Allows modifications on a link query before execution.
     * 
     * @param query
     *            The link query.
     * @param bean
     *            Referenced bean.
     * @param ctx
     *            Current applicative context.
     */
    public abstract void uiListPrepare(DbQuery query, Entity parentEntity, String linkName, RequestContext ctx);

    /**
     * Not Yet Implemented - ToyEasyLnkList
     */
    public final boolean uiEasyLnkListGoToNextLine(E bean, LinkModel link) {
        throw new TechnicalException("Not yet implemented");
    }

    /**
     * Not Yet Implemented - ToyLnkListExclude
     */
    public final Action uiListLinkAttach(E bean, LinkModel link) {
        throw new TechnicalException("Not yet implemented");
    }

    /**
     * Not Yet Implemented - ToyLnkCodLib
     */
    public final Key uiLnkKey(E bean, LinkModel link) {
        throw new TechnicalException("Not yet implemented");
    }

    /**
     * Not Yet Implemented - ToyLnkCodLib
     */
    public final boolean uiLnkKeyVarIsProtected(E bean, LinkModel link, String varName) {
        throw new TechnicalException("Not yet implemented");
    }

    /**
     * Not Yet Implemented - ToyLnkCodLib
     */
    public final Key uiLnkKeyVarIsVisible(E bean, LinkModel link, String varName) {
        throw new TechnicalException("Not yet implemented");
    }

	private void defaultUiListPrepare(DbQuery query, E critEntity, Action action) {
		// process simple variables
		if (critEntity == null) {
			// No criteria
			return;
		}
		for (String fieldName : critEntity.getModel().getFields()) {
			EntityField field = critEntity.getModel().getField(fieldName);
			if (!field.isFromDatabase()) {
				// Not a SQL variable, skip it
				continue;
			}

			String alias = query.getAlias(query.getMainEntity().name());
			Object value1 = critEntity.invokeGetter(fieldName);

			if (!fieldName.toUpperCase().endsWith("CritEnd") && critEntity.getModel().getField(fieldName + "CritEnd") != null) {

				Object value2 = critEntity.invokeGetter(fieldName + "CritEnd");
				if (value1 == null && value2 != null) {
					query.addCondLE(fieldName, alias, value2);
				} else if (value1 != null && value2 == null) {
					query.addCondGE(fieldName, alias, value1);
				} else if (value1 != null && value2 != null) {
					query.addCondBetween(fieldName, alias, value1, value2);
				}

			} else if (value1 != null) {
				if ("VARCHAR2".equals(field.getSqlType())) {
					query.addCondLike(fieldName, alias, "%" + value1 + "%");
				} else {
					query.addCondEq(fieldName, alias, value1);
				}
			}
		}
		// process links
		for (String linkName : critEntity.getLinks().keySet()) {
			if (!critEntity.getModel().isVirtualLink(linkName) && critEntity.getLink(linkName).getKey() != null) {
				Key refPrimaryKey = critEntity.getLink(linkName).getKey();
				Key foreignKey = EntityManager.buildForeignKey(critEntity.name(), refPrimaryKey, linkName);
				query.addCondKey(foreignKey, query.getMainEntityAlias());
			}
		}
	}

	private void defaultUiListPrepare(DbQuery query, String criteria, Action action) {
		if (criteria == null) {
			// No criteria
			return;
		}
		query.setCaseInsensitiveSearch(true);

		List<? extends DbQuery.Var> columns = query.getOutVars();
		List<String> colAliases = new ArrayList<String>();
		List<String> tableAliases = new ArrayList<String>();

		for (DbQuery.Var var : columns) {
			if (!var.model.isTransient() && var.model.isLookupField()) {
				colAliases.add(var.name);
				tableAliases.add(var.tableId);
			}
		}

		if (colAliases.isEmpty()) {
			for (DbQuery.Var var : columns) {
				if (!var.model.isTransient() && var.model.isAlpha()) {
					colAliases.add(var.name);
					tableAliases.add(var.tableId);
				}
			}
		}
		StringTokenizer st = new StringTokenizer(criteria, " ");

		while (st.hasMoreTokens() && !colAliases.isEmpty()) {
			String sText = st.nextToken();
			query.addCondLikeConcat(colAliases, tableAliases, sText, false);
		}
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Map<Key, String> internalUiLinkLoadValues(Entity bean, LinkModel linkModel, DbQuery filterQuery, boolean limitSize,
            RequestContext ctx) {
		try {
	        String mainAlias = "T1";
	        DbQuery query = DB.createQuery(ctx, linkModel.getRefEntityName(), mainAlias);
	
	        if (filterQuery != null) {
	            query = filterQuery;
	            mainAlias = query.getMainEntityAlias();
	            // By default all columns are selected to allow doDescription() to work.
	            query.addAllColumns(mainAlias);
	        }
        
	        // Prepare partial key conditions
			Key fk = bean.getForeignKey(linkModel.getKeyName());
			if (!fk.isFull() && !fk.isNull()) {
				// Multi fields key partially filled
				query.addCondKey(fk, query.getAlias(linkModel.getRefEntityName()));
			}
        
			DomainLogic targetDomainLogic = DomainUtils.getLogic(query.getMainEntity().name());
			targetDomainLogic.internalUiListPrepare(query, bean, linkModel.getLinkName(), ctx);

			if (limitSize && query.getMaxRownum() <= 0) {
				query.setMaxRownum(Constants.MAX_ROW);
			}
			Map<Key, String> map = new LinkedHashMap<Key, String>();
			DbManager dbManager = null;

			try {
				dbManager = DB.createDbManager(ctx, query);
				while (dbManager.next()) {
					Entity e = DomainUtils.newDomain(linkModel.getRefEntityName());
					e = dbManager.getEntity(mainAlias, e, ctx);
					map.put(e.getPrimaryKey(), targetDomainLogic.internalDoDescription(e, ctx));
				}
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}

	        if (!limitSize && map.size() > Constants.MAX_ROW) {
	            LOGGER.warn("Performance issue, loading combo with "+map.size()+" elements !");
	        }
        
	        return map;
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    public final boolean internalUiWizardCheckStep(E bean, Action action, String currentStep, String nextStep, RequestContext ctx) {
		try {
			return uiWizardCheckStep(bean, action, currentStep, nextStep, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

    /**
     * Determines if the user can navigate through steps in a wizard.
     * 
     * @param bean
     *            Current entity instance.
     * @param action
     *            Current action.
     * @param currentStep
     *            Current step in the wizard.
     * @param nextStep
     *            The step that the user want to display.
     * @param ctx
     *            Current applicative context.
     * @return {@code true} is the user is allowed to see the next step, {@code false} otherwise.
     */
    public abstract boolean uiWizardCheckStep(E bean, Action action, String currentStep, String nextStep, RequestContext ctx);

	public final ListData internalExtQueryLoad(RequestContext context, String entityName, String queryName) {
		return extQueryLoad(context, entityName, queryName);
	}

	public abstract ListData extQueryLoad(RequestContext context, String entityName, String queryName);

	public final E internalExtActionLoad(String domainName, Key primaryKey, Action action, RequestContext context) {
		return extActionLoad(domainName, primaryKey, action, context);
	}

	public abstract E extActionLoad(String domainName, Key primaryKey, Action action, RequestContext context);

	public final void internalExtActionExecute(Request<E> request, RequestContext context) {
		extActionExecute(request, context);
	}

	public abstract void extActionExecute(Request<E> request, RequestContext context);

	/**
	 * Transforms an entity instance into an event.
	 * @param entity Entity to transform.
	 * @param entityName Entity name.
	 * @param ctx Current applicative context.
	 * @return An event representation of the entity.
	 */
	public final ScheduleEvent internalUiSchedulePrepareEvent(E entity, String entityName, RequestContext ctx) {
		ScheduleEvent event = new ScheduleEvent();
		event.setPk(entity.getPrimaryKey());
		event.setTitle(internalDoDescription(entity, ctx));
		uiSchedulePrepareEvent(event, entity, entityName, ctx);
		return event;
	}

	/**
	 * Customizes the event properties.
	 * <p>
	 * By default, these properties are already set :
	 * <ul>
	 * <li>pk : entity's primary key, it will be used as the event id.</li>
	 * <li>title : this property is set with the entity description ({@link #doDescription(Entity, RequestContext)}).</li>
	 * <li>start : Event start date (automatically populated with the property {@link #uiScheduleEventDateStartName()}).</li>
	 * <li>end : Event end date (automatically populated with the property {@link #uiScheduleEventDateEndName()}).</li>
	 * </ul>
	 * </p>
	 * 
	 * @param event Event to customize.
	 * @param entity Entity represented by the event.
	 * @param entityName Entity name.
	 * @param ctx Current applicative context.
	 */
	public abstract void uiSchedulePrepareEvent(ScheduleEvent event, E entity, String entityName, RequestContext ctx);

	public final String internalUiScheduleEventStartName() {
		return uiScheduleEventStartName();
	}

	/**
	 * Retrieves the name of the entity property used as the event start.
	 * @return A valid entity property name.
	 */
	public abstract String uiScheduleEventStartName();
	
	public final String internalUiScheduleEventEndName() {
		return uiScheduleEventEndName();
	}

	/**
	 * Retrieves the name of the entity property used as the event end.
	 * @return A valid entity property name.
	 */
	public abstract String uiScheduleEventEndName();

}

