package fr.logica.jsf.model.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.Entity;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.MessageUtils;
import fr.logica.business.TechnicalException;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.controller.Request;
import fr.logica.business.data.ListCriteria;
import fr.logica.business.data.ListData;
import fr.logica.business.data.Row;
import fr.logica.db.DB;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.list.ListModel;
import fr.logica.reflect.DomainUtils;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;

/** Class handling the edition on a single table. */
public class TabeditModel extends ListModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -7480638462808430262L;

	public static final String CREATE_ACTION_NAME = "create";
	public static final String MODIFY_ACTION_NAME = "modify";

	private static final int NEW_LINE_ROWNUM = -1;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(TabeditModel.class);

	/* Action on the current entity. Set on a prerender event on the facelet (since this bean is generic to all editable lists). */
	private Action createAction;
	private Action modifyAction;

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

	/** Did the last action work ? */
	private boolean success;

	public TabeditModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String queryName,
			String linkName, String filterName) {
		super(viewCtrl, store, entity, entityName, queryName);
	}

	public String ajax() {
		RequestContext ctx = new RequestContext(viewCtrl.getSessionCtrl().getContext());
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
				if (CREATE_ACTION_NAME.equals(nextAction)) {
					prepareCreate(ctx);
				} else if (MODIFY_ACTION_NAME.equals(nextAction)) {
					prepareModify(ctx);
				}
			}
			viewCtrl.displayMessages(ctx);
			return null;
		} finally {
			ctx.getDbConnection().close();
		}

	}

	private boolean existsCreateAction(RequestContext ctx) {
		if (createAction == null) {
			LOGGER.error("No create action defined for current page");
			String msg = MessageUtils.getInstance(ctx).getMessage("error.bug", null);
			ctx.getMessages().add(new Message(msg, Severity.ERROR));
			return false;
		}
		return true;
	}

	private boolean existsModifyAction(RequestContext ctx) {
		if (modifyAction == null) {
			LOGGER.error("No modify action defined for current page");
			String msg = MessageUtils.getInstance(ctx).getMessage("error.bug", null);
			ctx.getMessages().add(new Message(msg, Severity.ERROR));
			return false;
		}
		return true;
	}

	/** Prepare for creation : set up a clean entity to receive the new values. */
	public void prepareCreate(RequestContext ctx) {
		if (!existsCreateAction(ctx)) {
			return;
		}
		prepareEntity(createAction, null, ctx);
		moveNextToCurrent();
	}

	/** Prepare for modification : find the entity corresponding to the next rownum, and set it up as the current entity. */
	public void prepareModify(RequestContext ctx) {
		Row row = data.getRows().get(nextRownum);
		if (!existsModifyAction(ctx)) {
			return;
		}

		prepareEntity(modifyAction, row.getPk(), ctx);
		moveNextToCurrent();
	}

	private void prepareEntity(Action action, Key keyIfNeeded, RequestContext ctx) {
		if (keyIfNeeded == null) {
			currentEntity = DomainUtils.newDomain(entityName);
		} else {
			currentEntity = DB.get(entityName, keyIfNeeded, action, ctx);
		}
	}

	public void validateCreate(RequestContext ctx) {
		if (currentEntity == null) {
			String msg = MessageUtils.getInstance(ctx).getMessage("error.bug", null);
			ctx.getMessages().add(new Message(msg, Severity.ERROR));
			return;
		}

		if (!existsCreateAction(ctx)) {
			return;
		}

		if (saveEntity(createAction, ctx)) {
			int lastRownum;
			if (data.getRows().isEmpty()) {
				lastRownum = 0;
			} else {
				lastRownum = data.getRows().size() - 1;
			}

			Row newRow = new Row();
			newRow.put(Constants.RESULT_ROWNUM, lastRownum);
			copyMainEntityOnRow(newRow, ctx);
			data.getRows().add(data.getRows().size() - 1, newRow);

			removeCurrent();
		}
	}

	public void validateModify(RequestContext ctx) {
		if (currentEntity == null) {
			String msg = MessageUtils.getInstance(ctx).getMessage("error.bug", null);
			ctx.getMessages().add(new Message(msg, Severity.ERROR));
			return;
		}

		Row row = data.getRows().get(currentRownum);

		if (row == null) {
			throw new TechnicalException("No rownum " + currentRownum + " in list of " + data.getRows());
		}

		if (!existsModifyAction(ctx)) {
			return;
		}

		if (saveEntity(modifyAction, ctx)) {
			copyMainEntityOnRow(row, ctx);
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
	private boolean saveEntity(Action action, RequestContext context) {
		/* Create a request for this update */
		Request<Entity> saveRequest = new Request<Entity>(entityName, action, null, null, null, false);
		saveRequest.setEntity(currentEntity);
		List<Key> keys = new ArrayList<Key>();
		keys.add(currentEntity.getPrimaryKey());
		saveRequest.setKeys(keys);
		saveRequest.setContext(context);

		/* Use this action page to validate the modifications */
		BusinessController business = new BusinessController();
		try {
			business.validate(saveRequest);
			success = true;
		} catch (FunctionalException e) {
			for (Message m : e.getMessages()) {
				LOGGER.error(m);
				context.getMessages().add(m);
			}
			LOGGER.error("", e);
			success = false;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			context.getMessages().add(new Message(viewCtrl.getTechnicalMessage(e), Severity.ERROR));
			success = false;
		}
		viewCtrl.displayMessages(context);
		return success;
	}

	/**
	 * Copy the fields necessary to the results from the current entity, to the row.
	 * 
	 * @param row This row will be filled by the values from the {@link #currentEntity}.
	 */
	private void copyMainEntityOnRow(Row row, RequestContext context) {
		/* Launch query with same criteria AND bean primary key so we'll get only one row */
		Entity copy = entity.clone();
		copy.setPrimaryKey(currentEntity.getPrimaryKey());
		ListData rowData = new BusinessController().getListData(copy, entityName, queryName, new ListCriteria(), viewCtrl.getCurrentView().getAction(),
				viewCtrl
						.getCurrentView().getLinkName(), viewCtrl.getCurrentView().getLinkedEntity(),
				context);
		Row newRow = rowData.getRows().get(0);
		for (Entry<String, Object> entry : newRow.entrySet()) {
			if (!Constants.RESULT_ROWNUM.equals(entry.getKey())) {
				row.put(entry.getKey(), entry.getValue());
			}
		}
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
		if (createAction == null) {
			Row row = new Row();
			row.put("$rownum", NEW_LINE_ROWNUM);
			data.getRows().add(row);
		}
		createAction = new Action(code, type);
	}

	public void setModifyAction(int code, int type) {
		modifyAction = new Action(code, type);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Overrides {@link ListModel#loadData(RequestContext)} to add create line upon new search.
	 */
	@Override
	public void loadData(RequestContext context) {
		super.loadData(context);
		if (createAction != null)
		{
			boolean hasCreateLine = false;
			List<Row> rows = data.getRows();
			if (rows.size() > 0) {
				Row lastRow = rows.get(rows.size() - 1);
				if (lastRow != null && lastRow.get("$rownum") != null)
				{
					Integer rowNum = (Integer)lastRow.get("$rownum");
					hasCreateLine = rowNum.intValue() == NEW_LINE_ROWNUM;
				}
			}
			// No create line, add new one
			if (!hasCreateLine)
			{
				Row row = new Row();
				row.put("$rownum", NEW_LINE_ROWNUM);
				rows.add(row);
			}
		}
	}
}
