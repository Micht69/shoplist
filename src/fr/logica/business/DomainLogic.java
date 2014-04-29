package fr.logica.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.business.controller.Response;
import fr.logica.business.data.ListData;
import fr.logica.business.data.ScheduleEvent;
import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Var;
import fr.logica.reflect.DomainUtils;

public abstract class DomainLogic<E extends Entity> extends AbstractDomainLogic<E> {

	@Override
	public List<Key> doCustomAction(Request<E> request, E entity, RequestContext ctx) {
		return null;
	}

	@Override
	public List<Key> doCustomAction(Request<E> request, E entity, List<Key> keys, RequestContext ctx) {
		return null;
	}
	
	/**
	 * This method returns true if the bean is invalid. Be aware that no error message will be added to the context if this method returns
	 * <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the context.
	 */
	@Override
	public boolean doCheck(E bean, Action action, RequestContext ctx) throws FunctionalException {
		return false;
	}

	@Override
	public String doDescription(E bean, RequestContext ctx) {
		if (bean == null) {
			return "";
		}
		if (bean.description() != null) {
			return bean.description();
		}
		return bean.getPrimaryKey().getEncodedValue();
	}

	/**
	 * This method returns true if the variable is invalid in this specific situation. Be aware that no error message will be added to the
	 * context if this method returns <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the
	 * context.
	 */
	@Override
	public boolean doVarCheck(E bean, String varName, Action action, RequestContext ctx) throws FunctionalException {
		return false;
	}

	/**
	 * You do not have to add a message to explain the problem in the context, one will be added automatically.
	 */
	@Override
	public boolean doVarIsMandatory(E bean, String varName, Action action, RequestContext ctx) {
		return bean.getModel().getField(varName).isMandatory();
	}

	@Deprecated
	@Override
	public Object doVarValue(Map<String, Object> vars, String domainName, String varName, RequestContext ctx) {
		return null;
	}

	@Override
	public Object doVarValue(E bean, String varName, RequestContext ctx) {
		if ("internalCaption".equals(varName)) {
			if (bean != null && bean.description() != null) {
				return bean.invokeGetter(bean.description());
			}
		}
		return null;
	}

	@Override
	public Object uiListVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, RequestContext ctx) {
		return null;
	}

	@Override
	public void dbSecure(DbQuery query, RequestContext ctx) {

	}

	@Override
	public void dbOnSave(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbOnDelete(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbPostLoad(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbPostSave(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbPostDelete(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public String uiActionTitle(Response<E> response, RequestContext ctx) {
		if (response.getAction() == null) {
			return null;
		}
		Action action = response.getAction();
		if (action.getInput() == Input.QUERY && action.getUi() == UserInterface.OUTPUT) {
			return MessageUtils.getInstance().getListTitle(response.getQueryName());
		}
		return MessageUtils.getInstance().getTitle(response.getEntityName(), response.getAction().getCode());
	}


	@Override
	public String uiVarCaption(E bean, String varName, Action action, RequestContext ctx) {
		return null;
	}

	@Override
	public String uiLinkCaption(E bean, String linkName, Action action, RequestContext ctx) {
		return null;
	}

	@Override
	public String uiListColumnCaption(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		Var var = query.getOutVar(varName);
		return MessageUtils.getInstance().getQryVarTitle(query.getName(), var.tableId, var.name);
	}

	@Override
	public boolean uiListColumnIsVisible(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		return true;
	}

	@Override
	public boolean uiVarIsVisible(Entity bean, String varName, Action action, RequestContext ctx) {
		return true;
	}

	@Override
	public boolean uiLinkIsVisible(Entity entity, String linkName, Action action, RequestContext ctx) {
		return true;
	}

	@Override
	public boolean uiGroupIsVisible(Entity bean, String groupName, Action action, RequestContext ctx) {
		return true;
	}

	@Override
	public boolean uiListIsProtected(Entity targetEntity, String linkName, String queryName, Action action, RequestContext ctx) {
		if (action.getUi() == UserInterface.READONLY) {
			return true;
		}
		return false;
	}

	@Override
	public boolean uiListIsReadOnly(E bean, String linkName, String queryName, Action action, RequestContext ctx) {
		return false;
	}

	@Override
	public boolean uiVarIsProtected(Entity bean, String varName, Action action, RequestContext ctx) {
		if (bean.getPrimaryKey().getModel().getFields().contains(varName)) {
			if (action.getPersistence() != Persistence.INSERT && action.getPersistence() != Persistence.NONE) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void uiActionOnLoad(Response<E> response, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void uiActionOnValidation(Request<E> request, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public boolean uiTabIsVisible(Entity bean, String tabName, Action action, RequestContext ctx) {
		return true;
	}

	@Override
	public String uiTabToOpen(Entity bean, String tabPanelName, Action action, RequestContext ctx) {
		return null;
	}

	@Override
	public Map<Key, String> uiLinkLoadCombo(Entity bean, LinkModel linkModel, DbQuery filterQuery, Action action, RequestContext ctx) {
		return internalUiLinkLoadValues(bean, linkModel, filterQuery, false, ctx);
	}

	public Request<?> uiCtrlNextAction(Request<E> request, RequestContext ctx) {
		Action action = request.getAction();
		if (action == null || action.getNext() == null) {
			return null;
		}
		Action nextAction = EntityManager.getEntityModel(request.getEntityName()).getAction(action.getNext());

		Request<E> nextRequest = new Request<E>();
		nextRequest.setAction(nextAction);
		nextRequest.setBackRef(request.isBackRef());
		// We don't set nextRequest entity to force entity full reload in next action. 
		// This will force entity post-load logic call in next action processing.
		nextRequest.setEntity(null);
		nextRequest.setEntityName(request.getEntityName());
		nextRequest.setKeys(request.getKeys());
		if (action.getInput() == Input.NONE && nextAction.getInput() == Input.ONE) {
			// Previous action was an INPUT NONE, but now we may have an input
			if (request.getEntity() != null && request.getEntity().getPrimaryKey().isFull()) {
				if (nextRequest.getKeys() == null) {
					nextRequest.setKeys(new ArrayList<Key>());
				}
				nextRequest.getKeys().add(request.getEntity().getPrimaryKey());
			}
		}
		nextRequest.setLinkedEntity(request.getLinkedEntity());
		nextRequest.setLinkName(request.getLinkName());
		nextRequest.setQueryName(request.getQueryName());
		nextRequest.setContext(request.getContext());
		return nextRequest;
	}


	@Override
	public Response<?> uiCtrlOverrideAction(Response<E> response, RequestContext ctx) {
		return null;
	}

	@Override
	public List<Key> uiCtrlMenuAction(Action action, RequestContext ctx) {
		// No default selection on default behavior
		return null;
	}

	@Override
	public boolean uiListPrepare(DbQuery query, E criteria, Action action, String linkName, Entity linkedEntity, RequestContext ctx) {
		return false;
	}

	@Override
	public void uiListPrepare(DbQuery query, Entity parentEntity, String linkName, RequestContext ctx) {
		// Nothing to do on default behavior
	}

	@Override
	public boolean uiWizardCheckStep(E bean, Action action, String currentStep, String nextStep, RequestContext ctx) {
		// Navigation is allowed on default behavior.
		return true;
	}

	@Override
	public ListData extQueryLoad(RequestContext context, String entityName, String queryName) {
		return new ListData(entityName);
	}

	@Override
	public E extActionLoad(String domainName, Key primaryKey, Action action, RequestContext context) {
		@SuppressWarnings("unchecked")
		E bean = (E) DomainUtils.newDomain(domainName);
		return bean;
	}

	@Override
	public void extActionExecute(Request<E> request, RequestContext context) {

	}
	
	@Override
	public void uiSchedulePrepareEvent(ScheduleEvent event, E entity, String entityName, RequestContext ctx) {
		EntityModel model = entity.getModel();

		String start = uiScheduleEventStartName();
		if (model.getField(start) != null) {
			event.setStart((Date) entity.invokeGetter(start));
		} else {
			throw new TechnicalException("Variable " + start + " not found in the entity " + model.name());
		}

		String end = uiScheduleEventEndName();
		if (model.getField(end) != null) {
			event.setEnd((Date) entity.invokeGetter(end));
		} else {
			throw new TechnicalException("Variable " + end + " not found in the entity " + model.name());			
		}
	}

	/**
	 * {@inheritDoc}
	 * @return {@link Constants#EVENT_DATE_START}
	 */
	@Override
	public String uiScheduleEventStartName() {
		return Constants.EVENT_DATE_START;
	}

	/**
	 * {@inheritDoc}
	 * @return {@link Constants#EVENT_DATE_END}
	 */
	@Override
	public String uiScheduleEventEndName() {
		return Constants.EVENT_DATE_END;
	}
	
}

