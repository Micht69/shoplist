package fr.logica.business;



import java.util.List;
import java.util.Map;


import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Var;

import fr.logica.reflect.DomainUtils;

import fr.logica.ui.ActionPage;
import fr.logica.ui.ListPage;

import fr.logica.ui.Page;

public abstract class DomainLogic<E extends Entity> extends AbstractDomainLogic<E> {

	@Override
	public List<Key> doCustomAction(ActionPage<E> page, List<Key> keys, Context ctx) {
		return null;
	}

	/**
	 * This method returns true if the bean is invalid. Be aware that no error message will be added to the context if this method returns
	 * <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the context.
	 */
	@Override
	public boolean doCheck(E bean, Action action, Context ctx) throws FunctionalException {
		return false;
	}

	@Override
	public String doDescription(E bean, Context ctx) {
		if (bean == null) {
			return "";
		}
		if (bean.$_getDesc() != null) {
			return String.valueOf(bean.invokeGetter(bean.$_getDesc()));
		}
		return bean.getPrimaryKey().getEncodedValue();
	}

	/**
	 * This method returns true if the variable is invalid in this specific situation. Be aware that no error message will be added to the
	 * context if this method returns <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the
	 * context.
	 */
	@Override
	public boolean doVarCheck(E bean, String varName, Action action, Context ctx) throws FunctionalException {
		return false;
	}

	/**
	 * You do not have to add a message to explain the problem in the context, one will be added automatically.
	 */
	@Override
	public boolean doVarIsMandatory(E bean, String varName, Action action, Context ctx) {
		return false;
	}

	@Override
	public Object doVarValue(Map<String, Object> vars, String domainName, String varName, Context ctx) {
		if ("w$Desc".equals(varName)) {
			Entity e = DomainUtils.newDomain(domainName);
			if (e.$_getDesc() != null && vars != null) {
				return vars.get(e.$_getDesc());
			}
		}
		return null;
	}

	@Override
	public Object doVarValue(E bean, String varName, Context ctx) {
		if ("w$Desc".equals(varName)) {
			if (bean != null && bean.$_getDesc() != null) {
				return bean.invokeGetter(bean.$_getDesc());
			}
		}
		return null;
	}

	@Override
	public Object uiListVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, Context ctx) {
		return null;
	}

	@Override
	public void dbSecure(DbQuery query, Context ctx) {

	}

	@Override
	public void dbOnSave(E bean, Action action, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbOnDelete(E bean, Action action, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbPostLoad(E bean, Action action, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbPostSave(E bean, Action action, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void dbPostDelete(E bean, Action action, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public String uiActionTitle(ActionPage<E> page, Context ctx) {
		return MessageUtils.getInstance().getTitle(page.getDomainName(), page.getAction().code);
	}

	@Override
	public String uiVarCaption(E bean, String varName, Action action, Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String uiLinkCaption(E bean, String linkName, Action action, Context ctx) {
		return null;
	}

	@Override
	public String uiListColumnCaption(DbQuery query, LinkModel link, String varName, Context ctx) {
		Var var = query.getOutVar(varName);
		
		String key = query.getName() + "." + var.tableId + "." + var.name;
		return MessageUtils.getInstance().getGenLabel(key, null);
	}

	@Override
	public boolean uiVarIsVisible(Page<E> page, Entity bean, String varName, Context ctx) {
		return true;
	}

	@Override
	public boolean uiLinkIsVisible(Page<E> page, String linkname, Action action, Context ctx) {
		return true;
	}

	@Override
	public boolean uiGroupIsVisible(Page<E> page, Entity bean, String groupName, Context ctx) {
		return true;
	}

	@Override
	public boolean uiListIsProtected(E bean, String linkName, String queryName, Action action, Context ctx) {
		if (action.type == Constants.DISPLAY) {
			return true;
		}
		return false;
	}

	@Override
	public boolean uiListIsReadOnly(E bean, String linkName, String queryName, Action action, Context ctx) {
		return false;
	}

	@Override
	public boolean uiVarIsProtected(Page<E> page, Entity bean, String varName, Context ctx) {

		if (page instanceof ListPage) {
			return false;
		}
		ActionPage<?> actionPage = (ActionPage<?>) page;

		if (!ActionUtils.isDataEditable(actionPage.getAction().type)) {
			return true;
		}
		if (bean == null) {
			return true;
		}
		if (bean.getPrimaryKey().getModel().getFields().contains(varName) && actionPage.getAction().type == Constants.MODIFY) {
			return true;
		}
		return false;
	}

	@Override
	public void uiActionOnLoad(ActionPage<E> page, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public void uiActionOnValidation(ActionPage<E> page, Context ctx) {
		// Nothing to do on default behavior.
	}

	@Override
	public boolean uiTabIsVisible(Page<E> page, Entity bean, String tabName, Context ctx) {
		return true;
	}

	@Override
	public String uiTabToOpen(Page<E> page, Entity bean, String tabPanelName, Context ctx) {
		return null;
	}

	@Override
	public Map<Key, String> uiLinkLoadCombo(E bean, LinkModel linkModel, DbQuery filterQuery, Page<?> page, Context ctx) {
		return internalUiLinkLoadValues(bean, linkModel, filterQuery, page, false, ctx);
	}

	@Override
	public ActionPage<?> uiCtrlNextAction(ActionPage<E> page, Context ctx) {
		Action a = EntityManager.getEntityModel(page.getDomainName()).getAction(page.getAction().code);
		if (a == null || a.next == null) {
			return null;
		}
		Action nextAction = EntityManager.getEntityModel(page.getDomainName()).getAction(a.next);
		ActionPage<E> nextPage = page.clone();
		if (nextPage.getKeyList() != null && nextPage.getKeyList().size() == 0) {
			if (page.getBean() != null) {
				nextPage.getKeyList().add(page.getBean().getPrimaryKey());
			}
		}
		nextPage.getAction().code = nextAction.code;
		nextPage.getAction().type = nextAction.type;
		return nextPage;
	}

	@Override
	public ActionPage<?> uiCtrlOverrideAction(ActionPage<E> page, Context ctx) {
		return null;
	}

	@Override
	public Key uiCtrlMenuAction(Action action, Context ctx) {
		// No default selection on default behavior
		return null;
	}

	@Override
	public boolean uiListPrepare(DbQuery query, ListPage<E> page, Context ctx) {
		return false;
	}

	@Override
	public void uiListPrepare(DbQuery query, E bean, LinkModel link, Context ctx) {
		// Nothing to do on default behavior
	}

	@Override
	public boolean uiWizardCheckStep(E bean, Action action, String currentStep, String nextStep, Context ctx) {
		// Navigation is allowed on default behavior.
		return true;
	}

	
}

