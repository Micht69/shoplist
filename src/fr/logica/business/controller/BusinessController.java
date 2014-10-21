package fr.logica.business.controller;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import fr.logica.business.Action;
import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.Process;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Constants;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.FileContainer;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.Link;
import fr.logica.business.LinkModel;
import fr.logica.business.MessageUtils;
import fr.logica.business.TechnicalException;
import fr.logica.business.context.RequestContext;
import fr.logica.business.data.BackRefData;
import fr.logica.business.data.ColumnData;
import fr.logica.business.data.ComboData;
import fr.logica.business.data.LinkData;
import fr.logica.business.data.ListCriteria;
import fr.logica.business.data.ListData;
import fr.logica.business.data.ScheduleEvent;
import fr.logica.db.DB;
import fr.logica.db.DbEntity;
import fr.logica.db.DbException;
import fr.logica.db.DbFactory;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Var;
import fr.logica.export.ExcelWriter;
import fr.logica.reflect.DomainUtils;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;
import fr.logica.ui.UiAccess;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BusinessController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 6503760540891178991L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(BusinessController.class);

	/**
	 * Process a request from a web client. This is usually called when the user starts an action. This method may : <br/>
	 * - Call custom code to get the input key if needed <br/>
	 * - load an entity from database <br/>
	 * - split the request in many individual requests <br/>
	 * - override the request with custom code to start a different request instead <br/>
	 * - load User Interface if any and send the response to user<br/>
	 * - start a validation request if there's no user interface to display <br/>
	 * 
	 * @param request The request to process.
	 * @return The Response to display if any, null otherwise.
	 */
	public Response process(Request request) {
		RequestContext context = request.getContext();

		// When an INPUT_ONE or INPUT_MANY action starts from a menu clic (or somewhere in a custom controller where no PK is provided), we call
		// a custom method to get it programatically
		checkMenuAction(request);

		// Initialization of response to send to this request
		Response response = new Response(request);

		// If response's main entity is not loaded, we load it
		if (response.getEntity() == null) {
			response.setEntity(getEntity(response, context));
		}

		// When action starts through links, we may attach related entity to the current main entity
		attachLinkedEntity(response);

		// When action is an INPUT_ONE, we'll handle ONE key at a time. Other selected keys, if any, are kept for later in response and they will
		// be processed when current request processing ends.
		if (response.getAction().getInput() == Input.ONE && response.getKeys() != null && response.getKeys().size() > 1) {
			splitProcessedKeys(response);
		}

		// Call custom method uiCtrlOverrideAction until there's no override to do. Response may be completely different.
		response = overrideResponse(response, context);

		// Response is now initialized. If response has a UserInterface, we'll load it and send it back to UI level.
		if (response.getAction().getUi() != UserInterface.NONE) {
			// Loads UI components of the view (title, visible and protected attributes, etc.)
			loadUi(response, context);
			return response;
		}

		// Response has no UI, we start validation right now
		Request validationRequest = new Request(response, context);
		Response validationResponse = validate(validationRequest);
		if (validationResponse != null) {
			// Keep potential previously selected keys.
			validationResponse.setRemKeys(response.getRemKeys());
			validationResponse.setRemAction(response.getRemAction());
			validationResponse.setRemKeys(response.getRemKeys());
			return validationResponse;
		} else if (response.getRemKeys() != null && response.getRemKeys().size() > 0) {
			// We finished current action, there are remaining keys that were selected but not processed (typically, with a INPUT ONE / DISPLAY
			// NONE action). We create a new request and start processing.
			Request remRequest = new Request<Entity>(response.getRemEntityName(), response.getRemAction(), response.getRemKeys(),
					request.getQueryName(), request.getLinkName(), request.isBackRef());
			return process(remRequest);
		} else {
			// Nothing more to do.
			return null;
		}
	}

	/**
	 * When request has been launched through a (non associative) back reference, we'll link the response entity to its parent entity
	 * "in memory". This will allow domain logic code to access parent memory objects.
	 * 
	 * @param response Response to send to the user.
	 */
	private void attachLinkedEntity(Response<?> response) {
		if (response.getLinkName() != null && response.isBackRef() && response.getLinkedEntity() != null
				&& !response.getLinkedEntity().getModel().isAssociativeLink(response.getLinkName())
				&& (response.getAction().getInput() == Input.ONE || response.getAction().getInput() == Input.NONE)) {
			// Avoid N-N links
			response.getEntity().getLink(response.getLinkName()).setEntity(response.getLinkedEntity());
		}
	}

	/**
	 * Loads a User Interface calling UI associated domain logic methods : <br/>
	 * - uiActionTitle<br/>
	 * - uiActionOnLoad<br/>
	 * - uiVarIsVisible<br/>
	 * - uiVarIsProtected<br/>
	 * - uiVarCaption<br/>
	 * - doVarIsMandatory (cause there may be a UI marker to display when mandatory)<br/>
	 * 
	 * @param response Response that defines the UI to display to the user
	 * @param context Current request context containing a pointer to User SessionContext and database connection
	 */
	public void loadUi(Response response, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName());
		String title = domainLogic.internalUiActionTitle(response, context);
		response.setTitle(title);
		domainLogic.internalUiActionOnLoad(response, context);
		response.setUiAccess(getEntityUiAccess(response.getEntity(), response.getAction(), context));
	}

	private void checkMenuAction(Request request) {
		Action action = request.getAction();
		if (request.getKeys() == null && (action.getInput() == Input.ONE || action.getInput() == Input.MANY)) {
			DomainLogic domainLogic = DomainUtils.getLogic(request.getEntityName());
			List<Key> keys = domainLogic.internalUiCtrlMenuAction(action, request.getContext());
			if (keys == null) {
				String errorMsg = MessageUtils.getInstance(request.getContext()).getMessage("error.menu.action.no.elt",
						new Object[] { request.getEntityName(), request.getEntityName() });
				throw new TechnicalException(errorMsg);
			}
			request.setKeys(keys);
		}
	}

	/**
	 * Splits a list of keys in one key + remaining keys. This method is used when response's action is an INPUT_ONE action (one entity processed
	 * at a time), and there are more than one key selected. We'll keep only the first selected key as selected key, and store remaining keys for
	 * later processing.
	 * 
	 * @param response A response with more than on key in keys list and an INPUT_ONE action. Method will keep only one key in its keys list, and
	 *        other keys in remaining keys list (remKeys). remAction and remEntityName will store initial response information that could will
	 *        allow to process these keys later.
	 */
	private void splitProcessedKeys(Response response) {
		response.setRemAction(response.getAction());
		response.setRemEntityName(response.getEntityName());
		List<Key> remKeys = response.getKeys().subList(1, response.getKeys().size());
		response.setRemKeys(remKeys);
		response.setKeys(response.getKeys().subList(0, 1));
	}

	private Response overrideResponse(Response response, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName());
		Response overrideResponse = domainLogic.internalUiCtrlOverrideAction(response, context);
		while (overrideResponse != null) {
			response = overrideResponse;
			if (response.getEntity() == null) {
				response.setEntity(getEntity(response, context));
			}
			domainLogic = DomainUtils.getLogic(overrideResponse.getEntityName());
			overrideResponse = domainLogic.internalUiCtrlOverrideAction(response, context);
		}
		return response;
	}

	/**
	 * Validates a user request. This method will call : <br/>
	 * - onValidation domain logic method<br/>
	 * - store main entity backup<br/>
	 * - validation on inner link entities if any<br/>
	 * - a validation sub-method (validateStandard, validateCustom, validateWebService) according to action type<br/>
	 * - validation on inner back ref entities if any<br/>
	 * - commit transaction if action persistence type is not NONE<br/>
	 * - uiCtrlNextAction domain logic method<br/>
	 * 
	 * When there's an error during validation, transaction is not commited and entities are rollbacked.
	 * 
	 * @param request The request to validate
	 * @return Response to send to the client when there is a "next action" to start. null if there's no nextAction.
	 */
	public <E extends Entity> Response<?> validate(Request<E> request) {
		RequestContext context = request.getContext();
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(request.getEntityName());
		Action action = request.getAction();

		E mainEntityBackup = (E) request.getEntity().clone();
		backupInner(request.getEntity(), mainEntityBackup);

		try {
			domainLogic.internalUiActionOnValidation(request, context);
			validateInner(request, request.getEntity(), false);

			if (action.getProcess() == Process.STANDARD) {
				validateStandard(request);
			} else if (action.getProcess() == Process.CUSTOM) {
				validateCustom(request);
			} else if (action.getProcess() == Process.WS) {
				validateWebService(request);
			}

			validateInner(request, request.getEntity(), true);
		} catch (RuntimeException e) {
			restoreBackup(request, mainEntityBackup);
			throw e;
		}
		if (action.getPersistence() != Persistence.NONE) {
			try {
				context.getDbConnection().commit();
			} catch (DbException sqlEx) {
				restoreBackup(request, mainEntityBackup);
				throw new TechnicalException(sqlEx.getMessage(), sqlEx);
			}
		}

		// Check if there's a next action and send it to UI layer.
		// It's called AFTER commit so entity links can be accessed in next action
		Request<?> nextRequest = domainLogic.internalUiCtrlNextAction(request, context);
		if (nextRequest != null) {
			return process(nextRequest);
		}
		return null;
	}

	/**
	 * Backups main entity values and linked entities values. This method is used to backup entity to the state it is before dbOnSave call.
	 * 
	 * @param entity Entity to backup
	 * @param entityBackup Backup
	 */
	private <E extends Entity> void backupInner(E entity, E entityBackup) {
		for (String linkName : entity.getBackRefs().keySet()) {
			Link backRef = entity.getBackRef(linkName);
			if (backRef.isApplyActionOnLink()) {
				Entity backRefBackup = backRef.getEntity().clone();
				entityBackup.getBackRef(linkName).setEntity(backRefBackup);
				backupInner(backRef.getEntity(), backRefBackup);
			}
		}
		for (String linkName : entity.getLinks().keySet()) {
			Link link = entity.getLink(linkName);
			if (link.isApplyActionOnLink()) {
				Entity linkBackup = link.getEntity().clone();
				entityBackup.getLink(linkName).setEntity(linkBackup);
				backupInner(link.getEntity(), linkBackup);
			}
		}
	}

	/**
	 * Restores main entity values and linked entities values with backup values. This method is used to "reset" entity to the state it was
	 * before dbOnSave call.
	 * 
	 * @param request Current validation request that provoke an error
	 * @param mainEntityBackup Main page entity backup to restore
	 */
	private <E extends Entity> void restoreBackup(Request<E> request, E mainEntityBackup) {
		request.getEntity().syncFromBean(mainEntityBackup);
		restoreInnerBackup(request, request.getEntity(), mainEntityBackup);
	}

	/**
	 * Restores innerLinks and innerBackrefs backups. This method is called recursively.
	 * 
	 * @param request Current validation request that provoke an error
	 * @param entity Entity to reset
	 * @param entityBackup Entity backup to restore
	 */
	private <E extends Entity> void restoreInnerBackup(Request<?> request, E entity, E entityBackup) {
		for (String linkName : entity.getLinks().keySet()) {
			Link link = entity.getLink(linkName);
			if (link.isApplyActionOnLink()) {
				Entity linkBackup = entityBackup.getLink(linkName).getEntity();
				link.getEntity().syncFromBean(linkBackup);
				restoreInnerBackup(request, link.getEntity(), linkBackup);
			}
		}
		for (String linkName : entity.getBackRefs().keySet()) {
			Link backRef = entity.getBackRef(linkName);
			if (backRef.isApplyActionOnLink()) {
				Entity backRefBackup = entityBackup.getBackRef(linkName).getEntity();
				backRef.getEntity().syncFromBean(backRefBackup);
				restoreInnerBackup(request, backRef.getEntity(), backRefBackup);
			}
		}
	}

	public <E extends Entity> void validateStandard(Request<E> request) {
		RequestContext context = request.getContext();
		Action action = request.getAction();
		Entity entity = request.getEntity();
		List<Key> keys = request.getKeys();
		Entity linkedEntity = request.getLinkedEntity();
		String linkName = request.getLinkName();

		if (action.getCode() == Constants.SELECT && request.isBackRef()) {
			selectBackRef(linkedEntity, action, linkName, keys, context);
		} else if (action.getCode() == Constants.DETACH && request.isBackRef()) {
			detachBackRef(linkedEntity, action, linkName, keys, context);
		} else if (action.getPersistence() == Persistence.INSERT) {
			DB.insert(entity, action, context);
		} else if (action.getPersistence() == Persistence.DELETE) {
			DB.remove(entity, action, context);
		} else if (action.getPersistence() == Persistence.UPDATE) {
			DB.update(entity, action, context);
		}

		// Action has been started through a link, we may need to update linked entity based on what have been done on the current entity.
		// (created an element, modified the primary key, attached or detached an element)
		if (linkName != null && !request.isBackRef()) {
			if (action.getCode() == Constants.SELECT) {
				linkedEntity.setForeignKey(linkName, keys.get(0));
			} else if (action.getPersistence() == Persistence.INSERT) {
				linkedEntity.setForeignKey(linkName, entity.getPrimaryKey());
			} else if (action.getPersistence() == Persistence.DELETE || action.getCode() == Constants.DETACH) {
				linkedEntity.setForeignKey(linkName, null);
			}
		}
	}

	private <E extends Entity> void validateInner(Request<?> request, E entity, boolean validateBackRef) {
		if (validateBackRef) {
			for (String linkName : entity.getBackRefs().keySet()) {
				Link backRef = entity.getBackRef(linkName);
				if (backRef.isApplyActionOnLink()) {
					validateInner(request, backRef.getEntity(), false);
					// Execution of an action on a backRef, so we may have to set / update the foreign key in child entity to make it reference
					// our entity's primary key
					if (request.getAction().getPersistence() == Persistence.INSERT || request.getAction().getPersistence() == Persistence.UPDATE) {
						backRef.getEntity().setForeignKey(backRef.getModel().getLinkName(), entity.getPrimaryKey());
					}
					applyActionOnLink(request, backRef.getEntity());
					validateInner(request, backRef.getEntity(), true);
				}
			}
		} else {
			for (String linkName : entity.getLinks().keySet()) {
				Link link = entity.getLink(linkName);
				if (link.isApplyActionOnLink()) {
					validateInner(request, link.getEntity(), false);
					applyActionOnLink(request, link.getEntity());
					// We executed an action on a link, so we can now update the foreign key in child entity
					if (request.getAction().getPersistence() == Persistence.INSERT || request.getAction().getPersistence() == Persistence.UPDATE) {
						entity.setForeignKey(linkName, link.getEntity().getPrimaryKey());
					} else if (request.getAction().getPersistence() == Persistence.DELETE) {
						entity.setForeignKey(linkName, null);
					}
					validateInner(request, link.getEntity(), true);
				}
			}
		}
	}

	private void applyActionOnLink(Request<?> request, Entity entity) {
		Action action = request.getAction();
		if (action.getPersistence() == Persistence.INSERT || action.getPersistence() == Persistence.UPDATE) {
			DB.persist(entity, action, request.getContext());
		} else if (action.getPersistence() == Persistence.DELETE) {
			DB.remove(entity, action, request.getContext());
		}
	}

	public <E extends Entity> void validateCustom(Request<E> request) {
		RequestContext context = request.getContext();
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(request.getEntityName());
		Action action = request.getAction();
		if (action.getInput() == Input.NONE || action.getInput() == Input.ONE) {
			domainLogic.internalDoCustomAction(request, request.getEntity(), context);
		} else {
			domainLogic.internalDoCustomAction(request, request.getEntity(), request.getKeys(), context);
		}
	}

	public <E extends Entity> void validateWebService(Request<E> request) {
		RequestContext context = request.getContext();
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(request.getEntityName());
		domainLogic.internalExtActionExecute(request, context);
	}

	public void selectBackRef(Entity linkedEntity, Action action, String linkName, List<Key> keys, RequestContext context) {
		Link link = linkedEntity.getBackRef(linkName);
		if (EntityManager.getEntityModel(link.getModel().getEntityName()).isAssociative()) {
			DB.persistAssociations(linkedEntity, linkName, keys, context);
		} else {
			for (Key selectedKey : keys) {
				Entity selectedEntity = DB.get(link.getModel().getEntityName(), selectedKey, action, context);
				selectedEntity.setForeignKey(link.getModel().getLinkName(), linkedEntity.getPrimaryKey());
				selectedEntity.getLink(linkName).setEntity(linkedEntity);
				DB.persist(selectedEntity, action, context);
			}
		}
	}

	public void detachBackRef(Entity linkedEntity, Action action, String linkName, List<Key> keys, RequestContext context) {
		Link link = linkedEntity.getBackRef(linkName);
		if (EntityManager.getEntityModel(link.getModel().getEntityName()).isAssociative()) {
			DB.removeAssociations(linkedEntity, linkName, keys, context);
		} else {
			for (Key selectedKey : keys) {
				Entity selectedEntity = DB.get(link.getModel().getEntityName(), selectedKey, action, context);
				selectedEntity.setForeignKey(link.getModel().getLinkName(), null);
				selectedEntity.getLink(linkName).setEntity(linkedEntity);
				DB.persist(selectedEntity, action, context);
			}
		}

	}

	public Entity getEntity(Response response, RequestContext context) {
		Entity entity = null;
		DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName());
		Action action = response.getAction();
		if (action == null) {
			return null;
		}
		if (action.getInput() == Input.ONE) {
			// load bean
			Key pk = ((List<Key>) response.getKeys()).get(0);
			if (EntityManager.getEntityModel(response.getEntityName()).isExternal()) {
				entity = domainLogic.internalExtActionLoad(response.getEntityName(), pk, action, context);
			} else {
				entity = DB.get(response.getEntityName(), pk, action, context);
			}
		} else if (action.getInput() == Input.QUERY) {
			// Criteria
			entity = DomainUtils.newDomain(response.getEntityName());
			entity.removeDefaultValues();
			domainLogic.internalDbPostLoad(entity, action, context);
		} else {
			// initializes an empty bean ("creation action" or "multi process at a time")
			entity = DomainUtils.newDomain(response.getEntityName());
			// keep default values on create
			if (action.getInput() != Input.NONE)
				entity.removeDefaultValues();
			if (response.getLinkName() != null && response.isBackRef() && response.getLinkedEntity() != null) {
				// Avoid N-N links
				if (!response.getLinkedEntity().getModel().isAssociativeLink(response.getLinkName())) {
					// Action launched through back ref template --> Initialize foreign key
					entity.setForeignKey(response.getLinkName(), response.getLinkedEntity().getPrimaryKey());
				}
			}
			domainLogic.internalDbPostLoad(entity, action, context);
		}
		return entity;
	}

	public <E extends Entity> Entity getLinkInnerEntity(Entity entity, String entityName, String linkName, Action action, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(entityName);
		Entity innerEntity = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			innerEntity = domainLogic.internalExtActionLoad(entityName, entity.getForeignKey(linkName), action, context);
		} else {
			innerEntity = DB.getRef(entity, linkName, action, context);
			if (innerEntity == null) {
				innerEntity = DomainUtils.newDomain(entityName);
				innerEntity.removeDefaultValues();
				domainLogic.internalDbPostLoad(innerEntity, action, context);
			}
		}
		return innerEntity;
	}

	public Entity getUniqueBackRefInnerEntity(Entity entity, String entityName, String backRefName, Action action, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(entityName);
		Entity innerEntity = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			innerEntity = domainLogic.internalExtActionLoad(entityName, entity.getForeignKey(EntityManager.getEntityModel(entityName).getBackRefModel(backRefName).getLinkName()), action, context);
		} else {
			innerEntity = DB.getUniqueBackRef(entity, backRefName, action, context);
			if (innerEntity == null) {
				innerEntity = DomainUtils.newDomain(entityName);
				innerEntity.removeDefaultValues();
				domainLogic.internalDbPostLoad(innerEntity, action, context);
			}
		}
		return innerEntity;
	}

	public Map<String, UiAccess> getEntityUiAccess(Entity entity, Action action, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(entity.name());
		Map<String, UiAccess> uiAccess = new HashMap<String, UiAccess>();
		for (String varName : entity.getModel().getFields()) {
			String key = entity.name() + "." + varName;

			// Default values depending on action type
			boolean visible = true;
			boolean readOnly = action.getUi() == UserInterface.READONLY;
			boolean mandatory = false;

			if (action.getInput() != Input.QUERY) {
				visible = domainLogic.internalUiVarIsVisible(entity, varName, action, context);
				readOnly = readOnly || domainLogic.internalUiVarIsProtected(entity, varName, action, context);
				mandatory = !readOnly && entity.getModel().getField(varName).isMandatory()
						|| domainLogic.internalDoVarIsMandatory(entity, varName, action, context);
			}
			String label = domainLogic.internalUiVarCaption(entity, varName, action, context);
			UiAccess access = new UiAccess(key, visible, readOnly, label, mandatory);
			uiAccess.put(key, access);
		}

		for (String linkName : entity.getModel().getLinkNames()) {
			boolean readOnly = action.getUi() == UserInterface.READONLY;
			boolean mandatory = false;
			readOnly = readOnly || isLinkProtected(entity, linkName, action, context);
			mandatory = !readOnly && isLinkMandatory(entity, linkName, action, context);
			boolean visible = isLinkVisible(entity, linkName, action, context);
			String label = getLinkLabel(entity, linkName, action, context);
			UiAccess access = new UiAccess(linkName, visible, readOnly, label, mandatory);
			uiAccess.put(linkName, access);
		}
		
		for (String backRefName : entity.getModel().getBackRefNames()) {
			// We won't check for back ref protection here cause it's done later, when ui model is loaded
			boolean readOnly = action.getUi() == UserInterface.READONLY;
			boolean mandatory = false;
			boolean visible = isLinkVisible(entity, backRefName, action, context);
			String label = getLinkLabel(entity, backRefName, action, context);
			UiAccess access = new UiAccess(backRefName, visible, readOnly, label, mandatory);
			uiAccess.put(backRefName, access);
		}

		return uiAccess;
	}

	/**
	 * Prepares data for a list
	 * 
	 * @param entity Main entity
	 * @param entityName Entity name
	 * @param queryName Query Name
	 * @param criteria criteria list
	 * @param action Current action
	 * @param linkName Link name
	 * @param linkedEntity Linked entity
	 * @param globalSearch is global search
	 * @param context Current request context
	 * @return ListData
	 */
	public <E extends Entity> ListData getListData(E entity, String entityName, String queryName, ListCriteria criteria, Action action,
			String linkName, Entity linkedEntity, boolean globalSearch, RequestContext context) {
		ListData data = null;
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(entityName);
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			data = domainLogic.internalExtQueryLoad(context, entityName, queryName);
		} else {
			// Apply user criteria to query
			DbQuery query = DB.getQuery(context, entityName, queryName);
			query.setMinRownum(criteria.minRow);
			query.setMaxRownum(criteria.maxRow);
			if (criteria.orderByField != null) {
				Var v = query.getOutVar(criteria.orderByField);
				query.addSortBy(v.name, v.tableId, criteria.orderByDirection, true);
			}
			try { 
				if (globalSearch) {
					domainLogic.internalUiListPrepare(query, criteria.searchCriteria, action, linkName, linkedEntity, context);
				} else {
					domainLogic.internalUiListPrepare(query, entity, action, linkName, linkedEntity, context);
				}
				data = getListData(query, true, context);
			} catch (FunctionalException e) {
				data = getEmptyListData(query, context); // Prevent from having a null ListData
			} catch (TechnicalException e) {
				data = getEmptyListData(query, context); // Prevent from having a null ListData
				context.getMessages().add(
						new Message(
								MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError",
										new Object[] { (Object) e.getMessage() }),
								Severity.ERROR));
				LOGGER.error(e.getMessage(), e);
			}
		}
		return data;
	}

	private ListData getListData(DbQuery query, boolean count, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity().name());
		ListData data = null;
		DbManager dbManager = null;

		// Get data from base
		try {
			dbManager = DB.createDbManager(context, query);
			data = dbManager.getListData();
			if (count) {
				data.setTotalRowCount(dbManager.count());
			}

			// Put list metadata
			for (DbQuery.Var var : query.getOutVars()) {
				String columnKey = var.tableId + "_" + var.name;
				ColumnData columnData = new ColumnData();
				columnData.setTitle(domainLogic.internalUiListColumnCaption(query, null, columnKey, context));
				columnData.setVisible(domainLogic.internalUiListColumnIsVisible(query, null, columnKey, context));
				data.getColumns().put(columnKey, columnData);
			}
		} catch (FunctionalException e) {
			data = getEmptyListData(query, context); // Prevent from having a null ListData
		} catch (TechnicalException e) {
			data = getEmptyListData(query, context); // Prevent from having a null ListData
			context.getMessages().add(new Message(
					MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError", new Object[] { (Object) e.getMessage() }),
					Severity.ERROR));
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (dbManager != null) {
				dbManager.close();
			}
		}

		return data;
	}

	private ListData getEmptyListData(DbQuery query, RequestContext context) {
		ListData data = new ListData(query.getMainEntity().name());

		// Put list metadata (either if success and error)
		DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity().name());
		for (DbQuery.Var var : query.getOutVars()) {
			String columnKey = var.tableId + "_" + var.name;
			ColumnData columnData = new ColumnData();
			columnData.setTitle(domainLogic.internalUiListColumnCaption(query, null, columnKey, context));
			columnData.setVisible(domainLogic.internalUiListColumnIsVisible(query, null, columnKey, context));
			data.getColumns().put(columnKey, columnData);
		}

		return data;
	}

	public <E extends Entity> ListData getBackRefListData(E targetEntity, String entityName, String linkName, String queryName, Action action,
			RequestContext context) {
		return getBackRefListData(targetEntity, entityName, linkName, queryName, null, action, context);
	}

	public <E extends Entity> ListData getBackRefListData(E targetEntity, String entityName, String linkName, String queryName,
			ListCriteria criteria, Action action, RequestContext context) {
		DbQuery query = new DbEntity().getLinkQuery(targetEntity, linkName, queryName, false, context);
		// If parent primary key is not full, back ref list is necessarily empty
		if (!targetEntity.getPrimaryKey().isFull()) {
			return getEmptyListData(query, context);
		}
		if (criteria != null && criteria.orderByField != null) {
			Var v = query.getOutVar(criteria.orderByField);
			query.addSortBy(v.name, v.tableId, criteria.orderByDirection, true);
		}
		DomainLogic sourceDomainLogic = DomainUtils.getLogic(query.getMainEntity().name());
		ListData data = null; 
		try {
			sourceDomainLogic.internalUiListPrepare(query, targetEntity, linkName, context);
			data = getListData(query, false, context);
		} catch (FunctionalException e) {
			data = getEmptyListData(query, context); // Prevent from having a null ListData
		} catch (TechnicalException e) {
			data = getEmptyListData(query, context); // Prevent from having a null ListData
			context.getMessages().add(
					new Message(
							MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError",
									new Object[] { (Object) e.getMessage() }),
							Severity.ERROR));
			LOGGER.error(e.getMessage(), e);
		}
		// ListIsProtected is called on target entity
		DomainLogic targetDomainLogic = DomainUtils.getLogic(targetEntity.name());
		data.setProtected(targetDomainLogic.internalUiListIsProtected(targetEntity, linkName, queryName, action, context));
		data.setReadOnly(targetDomainLogic.internalUiListIsReadOnly(targetEntity, linkName, queryName, action, context));
		return data;
	}

	public <E extends Entity> ListData getBackRefListDataSingleElement(E targetEntity, String entityName, String linkName, String queryName,
			Key pk,
			RequestContext context) {
		DbQuery query = new DbEntity().getLinkQuery(targetEntity, linkName, queryName, false, context);
		// If parent primary key is not full, back ref list is necessarily empty
		if (!targetEntity.getPrimaryKey().isFull()) {
			return getEmptyListData(query, context);
		}
		query.addCondKey(pk, query.getMainEntityAlias());
		DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity());
		domainLogic.internalUiListPrepare(query, targetEntity, linkName, context);
		return getListData(query, false, context);
	}

	public ComboData getLinkComboData(Entity sourceEntity, String entityName, String linkName, String filterName, Action action,
			RequestContext context) {
		Link link = sourceEntity.getLink(linkName);
		DomainLogic domainLogic = DomainUtils.getLogic(sourceEntity.name());
		DbQuery filterQuery = null;
		if (filterName != null) {
			filterQuery = DB.getQuery(context, entityName, filterName);
		}
		Map<Key, String> comboValues = domainLogic.internalUiLinkLoadCombo(sourceEntity, link.getModel(), filterQuery, action, context);
		ComboData comboData = new ComboData(link.getModel().getRefEntityName(), comboValues);
		return comboData;
	}

	public LinkData getLinkData(Entity sourceEntity, String entityName, String linkName, Action action, RequestContext context) {
		if (sourceEntity == null) {
			return new LinkData(entityName, null, null);
		}
		Entity targetEntity = null;
		DomainLogic<Entity> targetEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entityName);
		String description = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			targetEntity = targetEntityLogic.internalExtActionLoad(entityName, sourceEntity.getForeignKey(linkName), action, context);
		} else {
			targetEntity = DB.getRef(sourceEntity, linkName, action, context);
		}
		if (targetEntity != null) {
			description = targetEntityLogic.internalDoDescription(targetEntity, context);
		}
		return new LinkData(entityName, targetEntity, description);
	}

	public BackRefData getUniqueBackRefData(Entity targetEntity, String entityName, String backRefName, Action action, RequestContext context) {
		if (targetEntity == null || !targetEntity.getPrimaryKey().isFull()) {
			return new BackRefData(entityName, null, null);
		}
		DomainLogic sourceEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entityName);
		Entity sourceEntity = null;
		String description = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			// We don't know source key, we give the target PK to the method
			sourceEntity = sourceEntityLogic.internalExtActionLoad(entityName, targetEntity.getPrimaryKey(), action, context);
		}
		else {
			sourceEntity = DB.getUniqueBackRef(targetEntity, backRefName, action, context);
		}
		if (sourceEntity != null) {
			description = sourceEntityLogic.internalDoDescription(sourceEntity, context);
		}
		return new BackRefData(entityName, sourceEntity, description);
	}

	public Map<String, String> getLinkQuickSearchData(Entity sourceEntity, String entityName, String linkName, String filterName,
			String criteria,
			RequestContext context) {
		Map<String, String> result = new HashMap<String, String>();
		Link link = sourceEntity.getLink(linkName);
		DbQuery filterQuery = null;

		filterQuery = DB.getQuery(context, entityName, filterName);
		filterQuery.setCaseInsensitiveSearch(true);

		List<? extends Var> columns = filterQuery.getOutVars();
		List<String> colAliases = new ArrayList<String>();
		List<String> tableAliases = new ArrayList<String>();
		Set<String> lookupFields = EntityManager.getEntityModel(entityName).getLookupFields();

		if (lookupFields.isEmpty()) {
			for (Var var : columns) {
				if (!var.model.isTransient() && var.model.isAlpha()) {
					colAliases.add(var.name);
					tableAliases.add(var.tableId);
				}
			}
		} else {
			String tableAlias = filterQuery.getMainEntityAlias();
			for (String field : lookupFields) {
				colAliases.add(field);
				tableAliases.add(tableAlias);
			}
		}

		StringTokenizer st = new StringTokenizer(criteria, " ");

		while (st.hasMoreTokens()) {
			String sText = st.nextToken();
			filterQuery.addCondLikeConcat(colAliases, tableAliases, sText, false);
		}
		DomainLogic domainLogic = (DomainLogic) DomainUtils.getLogic(sourceEntity.name());
		try {
			Map<Key, String> values = domainLogic.internalUiLinkLoadValues(sourceEntity, link.getModel(), filterQuery, true, context);
			if (null == values || values.isEmpty()) {
				result.put("-1", MessageUtils.getInstance(context).getMessage("autocomplete.noResult", (Object[]) null));
			} else if (values.size() > Constants.AUTOCOMPLETE_MAX_ROW) {
				result.put("-1", MessageUtils.getInstance(context).getMessage("autocomplete.tooManyResults", new Object[] { values.size() }));
			} else {
				for (Entry<Key, String> e : values.entrySet()) {
					result.put(e.getKey().getEncodedValue(), e.getValue());
				}
			}
		} catch (TechnicalException exception) {
			result.put("-1", MessageUtils.getInstance(context).getMessage("autocomplete.error", (Object[]) null));
		}
		return result;
	}

	public List<String> getVisibleTabs(Entity entity, String entityName, String tabPanelName, List<String> tabNames, Action action,
			RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(entityName);
		List<String> visibleTabs = new ArrayList<String>();
		boolean groupIsVisible = domainLogic.internalUiGroupIsVisible(entity, tabPanelName, action, context);
		if (groupIsVisible) {
			for (String tabName : tabNames) {
				if (domainLogic.internalUiTabIsVisible(entity, tabName, action, context)) {
					visibleTabs.add(tabName);
				}
				String tabToOpen = domainLogic.internalUiTabToOpen(entity, tabPanelName, action, context);
				if (tabToOpen != null) {
					visibleTabs.remove(tabToOpen);
					visibleTabs.add(0, tabToOpen);
				}
			}
		}
		return visibleTabs;
	}

	public Map<String, Integer> generateMenuCounters(Map<String, String[]> menuQueries, RequestContext context) {
		Map<String, Integer> menuCounters = new HashMap<String, Integer>();
		for (Entry<String, String[]> menuEntry : menuQueries.entrySet()) {
			DbManager dbManager = null;
			try {
				DbQuery query = DB.getQuery(context, menuEntry.getValue()[0], menuEntry.getValue()[1]);
				query.setCount(true);
				dbManager = DB.createDbManager(context, query);
				menuCounters.put(menuEntry.getKey(), dbManager.count());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}
		}
		return menuCounters;
	}

	public FileContainer getFile(Entity entity, String varName, Action action, RequestContext context) {
		// get the current value (already computed if transient var)
		FileContainer container = (FileContainer) entity.invokeGetter(varName + "Container");

		if (!entity.getModel().getField(varName).isTransient()) {
			// db value is lazy loaded here
			DbEntity dbEntity = new DbFactory().createDbEntity();
			byte[] content = dbEntity.getLobContent(context, entity, varName);
			container.setContent(content);
		}
		return container;
	}

	public <E extends Entity> boolean isGroupVisible(E bean, String groupName, Action action, RequestContext ctx) {
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.name());
		return domainLogic.internalUiGroupIsVisible(bean, groupName, action, ctx);
	}

	/**
	 * Check if a link is visible

	 * @param sourceEntity Entity
	 * @param linkName Link name
	 * @param action Current action
	 * @param context Current request context
	 * @return true if link is visible, else false
	 */
	public Boolean isLinkVisible(Entity sourceEntity, String linkName, Action action, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(sourceEntity.name());
		return domainLogic.internalUiLinkIsVisible(sourceEntity, linkName, action, context);
	}

	public Boolean isLinkProtected(Entity sourceEntity, String linkName, Action action, RequestContext context) {
		LinkModel linkModel = sourceEntity.getModel().getLinkModel(linkName);
		DomainLogic domainLogic = DomainUtils.getLogic(sourceEntity.name());
		for (String varName : linkModel.getFields()) {
			if (domainLogic.internalUiVarIsProtected(sourceEntity, varName, action, context)) {
				// If at least one variable of the link is protected, link is protected.
				return true;
			}
		}
		return false;
	}

	public Boolean isLinkMandatory(Entity sourceEntity, String linkName, Action action, RequestContext context) {
		LinkModel linkModel = sourceEntity.getModel().getLinkModel(linkName);
		DomainLogic domainLogic = DomainUtils.getLogic(sourceEntity.name());
		for (String varName : linkModel.getFields()) {
			if (!domainLogic.internalDoVarIsMandatory(sourceEntity, varName, action, context)) {
				// If at least one variable of the link is not mandatory, link is not mandatory.
				return false;
			}
		}
		return true;
	}

	public String getLinkLabel(Entity sourceEntity, String linkName, Action action, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(sourceEntity.name());
		return domainLogic.internalUiLinkCaption(sourceEntity, linkName, action, context);
	}

	public <E extends Entity, LE extends Entity> ListData getBackRefScheduleData(E entity, String entityName, String linkName,
			String queryName, Action action, RequestContext context) {

		List<Entity> linkedEntities = DB.getLinkedEntities(entity, linkName, queryName, context);
		DomainLogic<LE> domainLogic = (DomainLogic<LE>) DomainUtils.getLogic(entityName);
		ListData data = new ListData(entityName);

		for (Entity linkedEntity : linkedEntities) {
			ScheduleEvent event = domainLogic.internalUiSchedulePrepareEvent((LE) linkedEntity, entityName, context);
			data.add(event);
		}
		data.setProtected(domainLogic.internalUiListIsProtected(entity, linkName, queryName, action, context));
		return data;
	}

	/**
	 * Initialize bean's start and end properties for the creation page.
	 * 
	 * @param bean Bean to update.
	 * @param action Current action
	 * @param selectedDate Selected date into the schedule in the previous page.
	 * @param context Current request context.
	 */
	public <E extends Entity> void initEventCreation(E bean, Action action, Date selectedDate, RequestContext context) {
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.name());
		bean.invokeSetter(domainLogic.uiScheduleEventStartName(), selectedDate);
		bean.invokeSetter(domainLogic.uiScheduleEventEndName(), selectedDate);
		// Cette méthode reprend la méthode qui était présente dans uiController mais plus simple.
		// Ce n'est peut-être pas la peine de créer n méthodes dans domainLogic pour la initialiser correctement les dates
		// car cela peut être fait dans dbPostLoad...
		domainLogic.internalDbPostLoad(bean, action, context);
	}

	/**
	 * Updates an event.
	 * @param entityName Name of the entity to update.
	 * @param event Event corresponding to the entity.
	 * @param context Current request context.
	 */
	public void updateEvent(String entityName, ScheduleEvent event, RequestContext context) {
		Entity bean = DB.get(entityName, event.getPk(), context);
		DomainLogic domainLogic = (DomainLogic) DomainUtils.getLogic(bean.name());
		bean.invokeSetter(domainLogic.uiScheduleEventStartName(), event.getStart());
		bean.invokeSetter(domainLogic.uiScheduleEventEndName(), event.getEnd());
		DB.update(bean, context);
	}

	public <E extends Entity> File getListExportXls(E entity, String entityName, String queryName, ListCriteria criteria, Action action,
			String linkName, Entity linkedEntity,
			RequestContext context) {
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(entityName);
		DbQuery query = DB.getQuery(context, entityName, queryName);
		if (criteria.orderByField != null) {
			Var v = query.getOutVar(criteria.orderByField);
			query.addSortBy(v.name, v.tableId, criteria.orderByDirection, true);
		}
		context.getAttributes().put("EXPORT_TYPE", "xls");

		domainLogic.internalUiListPrepare(query, entity, action, linkName, linkedEntity, context);
		if (DB.count(query, context) > Constants.MAX_ROW_EXCEL_EXPORT) {
			throw new FunctionalException(new Message(
					MessageUtils.getInstance(context).getMessage("xls.export.error.max", (Object[]) null), Severity.ERROR));
		}
		ListData data = getListData(query, false, context);
		return prepareExcelSheet(query, data, context);
	}

	public <E extends Entity> File getBackRefListExportXls(E entity, String entityName, String linkName, String queryName, RequestContext context) {
		DbQuery query = new DbEntity().getLinkQuery(entity, linkName, queryName, false, context);
		DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity().name());
		domainLogic.internalUiListPrepare(query, entity, linkName, context);
		if (DB.count(query, context) > Constants.MAX_ROW_EXCEL_EXPORT) {
			throw new FunctionalException(new Message(
					MessageUtils.getInstance(context).getMessage("xls.export.error.max", (Object[]) null), Severity.ERROR));
		}
		ListData data = getListData(query, false, context);
		return prepareExcelSheet(query, data, context);
	}

	public File prepareExcelSheet(DbQuery query, ListData data, RequestContext ctx) {
		File excelFile = null;
		try {
			excelFile = File.createTempFile(query.getName(), ".xls");
			new ExcelWriter().export(excelFile, query, data, ctx);
		} catch (Exception e) {
			ctx.getMessages().add(new Message(
					MessageUtils.getInstance(ctx).getMessage("uiControlerModel.cvsExportError", new Object[] { (Object) e.getMessage() }),
					Severity.ERROR));
		}
		return excelFile;
	}

	public <E extends Entity> boolean checkWizardStep(E bean, Action action, String currentStep, String nextStep, RequestContext context) {
		boolean allowed = true;

		try {
			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.name());
			allowed = domainLogic.internalUiWizardCheckStep(bean, action, currentStep, nextStep, context);
		} catch (Exception e) {
//			context.getMessages().add(new Message(
//					MessageUtils.getInstance().getMessage("uiControlerModel.queryExecError", new Object[] { (Object) e.getMessage() }),
//					Severity.ERROR));
			LOGGER.error(e.getMessage(), e);
		}
		return allowed;
	}

	public Set<String> getEditableListVarIsProtected(Entity entity, Action action, String queryName, RequestContext context) {
		Set<String> protectedVars = new HashSet<String>();
		DomainLogic domainLogic = DomainUtils.getLogic(entity.name());
		context.putCustomData("editableList", queryName);
		for (String varName : entity.getModel().getFields()) {
			if (domainLogic.uiVarIsProtected(entity, varName, action, context)) {
				protectedVars.add(varName);
			}
		}
		context.removeCustomData("editableList");
		return protectedVars;
	}

}

