package fr.logica.jsf.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import fr.logica.application.ApplicationUtils;
import fr.logica.business.Action;
import fr.logica.business.Context;
import fr.logica.business.Entity;
import fr.logica.business.EntityField;
import fr.logica.business.Key;
import fr.logica.business.MessageUtils;
import fr.logica.business.Result;
import fr.logica.business.Results;
import fr.logica.business.TechnicalException;
import fr.logica.controller.UiController;
import fr.logica.db.DB;
import fr.logica.security.ApplicationUser;
import fr.logica.ui.ActionPage;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;
import fr.logica.ui.Page;

/** Class handling the edition on a single table. */
public class TableEditor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CREATE_ACTION_NAME = "create";
	public static final String MODIFY_ACTION_NAME = "modify";

	private static final int NEW_LINE_ROWNUM = -1;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(TableEditor.class);

	/** Handles ui calls */
	private TabeditController tabeditCtrl = new TabeditController();

	/** Handles ui calls */
	private UiController uiCtrl = new UiController();

	private ApplicationUser currentUser;

	private Page<?> currentPage;

	/** If the table being edited represents a link between objects, this is the link name. Is null on list pages. */
	private String linkName;

	/* Action on the current entity. Set on a prerender event on the facelet (since this bean is generic to all editable lists). */
	private Action createAction;
	private Action modifyAction;
	private Action selectAction;

	/** Row being edited on the UI */
	private Entity currentEntity = null;

	/** The row that is currently edited on the client side */
	private int currentRownum = -1;

	/** The newt row that needs to be set editable */
	private int nextRownum = -1;

	/** The action to perform on the current entity */
	private String currentAction;

	/** The action to prepare for the next entity */
	private String nextAction;

	/** The results being currently displayed */
	private Results results;

	/** Did the last action work ? */
	private boolean success;

	public TableEditor(TabeditController tabeditCtrl, ApplicationUser currentUser, Page<?> currentPage, String linkName) {
		this.tabeditCtrl = tabeditCtrl;
		this.currentUser = currentUser;
		this.currentPage = currentPage;
		this.linkName = linkName;
	}

	public Entity getCurrentEntity() {
		return currentEntity;
	}

	public int getNextRownum() {
		return nextRownum;
	}

	public void setNextRownum(int nextRownum) {
		this.nextRownum = nextRownum;
	}

	public int getCurrentRownum() {
		return currentRownum;
	}

	public void setCurrentRownum(int currentRownum) {
		this.currentRownum = currentRownum;
	}

	public String getCurrentAction() {
		return currentAction;
	}

	public void setCurrentAction(String currentAction) {
		this.currentAction = currentAction;
	}

	public String getNextAction() {
		return nextAction;
	}

	public void setNextAction(String nextAction) {
		this.nextAction = nextAction;
	}

	public void setCreateAction(int code, int type) {
		createAction = new Action(code, type);
	}

	public void setModifyAction(int code, int type) {
		modifyAction = new Action(code, type);
	}

	public void setSelectAction(int code, int type) {
		selectAction = new Action(code, type);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<Result> workWith(Results results, Entity entity) {
		this.results = results;
		List<Result> list;
		if (results==null) {
			list = new ArrayList<Result>(1);
		} else {
			list = new ArrayList<Result>(results.getList());
		}
		if (createAction != null) {
			list.add(new Result(NEW_LINE_ROWNUM));
		}
		return list;
	}

	public List<Result> workWith(Results results) {
		return workWith(results, null);
	}

	public String ajax() {
		Context ctx = new Context(DB.createDbConnection(), currentUser);
		try {
			/* Order matters ! First validate any current entity, then prepare the next one - which will replace the current entity. */
			if (CREATE_ACTION_NAME.equals(currentAction)) {
				validateCreate(ctx);
			} else if (MODIFY_ACTION_NAME.equals(currentAction)) {
				validateModify(ctx);
			} else {
				/* nothing to do, hard to fail... */
				success = true;
			}

			if (success) {
				currentPage.setDirty(false);
				if (CREATE_ACTION_NAME.equals(nextAction)) {
					prepareCreate(ctx);
				} else if (MODIFY_ACTION_NAME.equals(nextAction)) {
					prepareModify(ctx);
				}
			}

			tabeditCtrl.displayMessages(ctx);
			return null;
		} finally {
			ctx.getConnection().close();
		}

	}

	private boolean existsCreateAction(Context ctx) {
		if (createAction == null) {
			LOGGER.error("No create action defined for current page");
			String msg = MessageUtils.getInstance().getMessage("error.bug", null);
			addMessage(new Message(msg, Severity.ERROR), ctx);
			return false;
		}
		return true;
	}

	private boolean existsModifyAction(Context ctx) {
		if (modifyAction == null) {
			LOGGER.error("No modify action defined for current page");
			String msg = MessageUtils.getInstance().getMessage("error.bug", null);
			addMessage(new Message(msg, Severity.ERROR), ctx);
			return false;
		}
		return true;
	}

	/** Prepare for creation : set up a clean entity to receive the new values. */
	public void prepareCreate(Context ctx) {
		if (!existsCreateAction(ctx)) {
			return;
		}
		prepareEntity(createAction, null, ctx);
		moveNextToCurrent();
	}

	/** Prepare for modification : find the entity corresponding to the next rownum, and set it up as the current entity. */
	public void prepareModify(Context ctx) {
		Result row = results.getList().get(nextRownum);

		if (!existsModifyAction(ctx)) {
			return;
		}

		prepareEntity(modifyAction, row.getPk(), ctx);
		moveNextToCurrent();
	}

	private void prepareEntity(Action action, Key keyIfNeeded, Context ctx) {

		/* Create an action page for this preparation */
		ActionPage<Entity> page = new ActionPage<Entity>();
		page.setDomainName(getDomainName());
		page.setAction(action);
		page.setLinkName(linkName);
		if (keyIfNeeded != null) {
			page.setKeyList(Arrays.asList(keyIfNeeded));
		}

		/* Use this action page to prepare the bean */
		Page<?> virtualPage = uiCtrl.prepareAction(page, currentPage, ctx);

		if (virtualPage instanceof ActionPage) {
			currentEntity = virtualPage.getBean();
		} else {
			/* something went wrong */
			addMessages(virtualPage.getMessages(), ctx);
		}
	}

	public void validateCreate(Context ctx) {
		if (currentEntity == null) {
			String msg = MessageUtils.getInstance().getMessage("error.bug", null);
			addMessage(new Message(msg, Severity.ERROR), ctx);
			return;
		}

		if (!existsCreateAction(ctx)) {
			return;
		}

		if (saveEntity(createAction, ctx)) {
			List<Result> resultsList = results.getList();

			int lastRownum;
			if (resultsList.isEmpty()) {
				lastRownum = 0;
			} else {
				lastRownum = resultsList.get(resultsList.size() - 1).getRownum();
			}

			Result newRow = new Result(lastRownum + 1);
			copyMainEntityOnRow(newRow);
			resultsList.add(newRow);

			removeCurrent();
		}
	}

	public void validateModify(Context ctx) {
		if (currentEntity == null) {
			String msg = MessageUtils.getInstance().getMessage("error.bug", null);
			addMessage(new Message(msg, Severity.ERROR), ctx);
			return;
		}

		Result row = null;
		for (Result r : results.getList()) {
			if (currentRownum == r.getRownum()) {
				row = r;
				break;
			}
		}

		if (row == null) {
			throw new TechnicalException("No rownum " + currentRownum + " in list of " + results);
		}

		if (!existsModifyAction(ctx)) {
			return;
		}

		if (saveEntity(modifyAction, ctx)) {
			copyMainEntityOnRow(row);
			removeCurrent();
		}

	}

	private void moveNextToCurrent() {
		currentAction = nextAction;
		currentRownum = nextRownum;

		nextAction = null;
		nextRownum = -1;
	}

	private void removeCurrent() {
		currentAction = null;
		currentRownum = -1;
		currentEntity = null;
	}

	/**
	 * Save an entity. Return true if it worked.
	 * 
	 * @param ctx
	 */
	private boolean saveEntity(Action action, Context ctx) {
		// Clear entity links because we currently do not use them.
		currentEntity.getLinks().clear();

		/* Create an action page for this update */
		ActionPage<Entity> page = new ActionPage<Entity>();
		page.setBean(currentEntity);
		page.setDomainName(currentEntity.$_getName());
		page.setAction(action);
		page.setNextPage(currentPage);
		page.setLinkName(linkName);

		List<Key> keys = new ArrayList<Key>();
		keys.add(currentEntity.getPrimaryKey());
		page.setKeyList(keys);

		/* Use this action page to validate the modifications */
		Page<?> nextPage = uiCtrl.validateAction(page, ctx);
		addMessages(nextPage.getMessages(), ctx);

		success = nextPage.isLastActionSuccess();
		if (success) {
			/* Reload the entity from database */
			currentEntity = getEntity(currentUser, currentEntity.getPrimaryKey(), currentEntity.$_getName(), selectAction, ctx);
		}

		return success;
	}

	/**
	 * Copy the fields necessary to the results from the current entity, to the row.
	 * 
	 * @param row
	 *            This row will be filled by the values from the {@link #currentEntity}.
	 */
	private void copyMainEntityOnRow(Result row) {
		/* copy primary key */
		row.setPk(currentEntity.getPrimaryKey());

		/* Port changes on the result line */
		for (Entry<String, String> entry : results.getMainEntityColumnsByField().entrySet()) {
			String field = entry.getKey();
			String column = entry.getValue();
			Object value = currentEntity.invokeGetter(field);

			EntityField model = currentEntity.getModel().getField(field);

			if (value instanceof Date) {
				String type = model.getSqlType();
				SimpleDateFormat formatter = new SimpleDateFormat(ApplicationUtils.getApplicationLogic().getDateTimeFormatFor(type));
				value = formatter.format((Date) value);

			} else if (model.hasDefinedValues()) {
				value = model.getDefinedLabel(value);
			}

			row.put(column, value);
		}

	}

	private String getDomainName() {
		return results.getEntityName();
	}

	private static Entity getEntity(ApplicationUser user, Key primaryKey, String entityName, Action action, Context ctx) {
		return DB.get(entityName, primaryKey, action, ctx);

		/* TODO renouxg do the stuff when loading query */
	}

	private void addMessage(Message msg, Context ctx) {
		currentPage.getMessages().add(msg);
		ctx.getMessages().add(msg);
	}

	private void addMessages(Collection<Message> messages, Context ctx) {
		currentPage.getMessages().addAll(messages);
		ctx.getMessages().addAll(messages);
	}

}
