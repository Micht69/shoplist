package fr.logica.controller;

import java.io.File;
import java.io.Serializable;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import fr.logica.business.Action;
import fr.logica.business.ActionUtils;
import fr.logica.business.Constants;
import fr.logica.business.Context;
import fr.logica.business.DateTimeUpgraded;
import fr.logica.business.DomainLogic;
import fr.logica.business.Entity;
import fr.logica.business.EntityField;
import fr.logica.business.EntityField.Memory;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FileContainer;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.Link;
import fr.logica.business.LinkModel;
import fr.logica.business.MessageUtils;
import fr.logica.business.Results;
import fr.logica.business.TechnicalException;
import fr.logica.db.ConnectionLogger;
import fr.logica.db.ConnectionObject;
import fr.logica.db.DB;
import fr.logica.db.DbConnection;
import fr.logica.db.DbEntity;
import fr.logica.db.DbException;
import fr.logica.db.DbFactory;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Var;

import fr.logica.export.ExcelWriter;
import fr.logica.jsf.components.schedule.ScheduleEvent;
import fr.logica.reflect.DomainUtils;
import fr.logica.security.ApplicationUser;
import fr.logica.ui.ActionPage;
import fr.logica.ui.ListPage;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;
import fr.logica.ui.Page;
import fr.logica.ui.UiAccess;
import fr.logica.ui.UiElement;
import fr.logica.ui.UiLink;
import fr.logica.ui.UiLink.Type;
import fr.logica.ui.UiManager;
import fr.logica.ui.UiPage;
import fr.logica.ui.UiScheduleLink;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class UiController implements Serializable {

	/** SerialUID */
	private static final long serialVersionUID = -697861000588895326L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(UiController.class);

	/**
	 * Prepare a listPage for display
	 * 
	 * @param <E>
	 *            Main domain of the list.
	 * @param page
	 *            ListPage to prepare.
	 * @param user
	 *            Current user
	 * @param init
	 *            ListPage initialization. Used to load criteria template links.
	 * @return Prepared ListPage.
	 */
	public <E extends Entity> ListPage<E> prepareList(ListPage<E> page, ApplicationUser user, boolean init) {
		Context ctx = null;
		try {

			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(page.getDomainName());
			ctx = new Context(DB.createDbConnection(), user);
			Map<String, UiAccess> pageAccess = new HashMap<String, UiAccess>();
			prepareEntityAccess(page, pageAccess, null, page.getCriteria(), domainLogic, false, ctx);
			page.setAccess(pageAccess);

			List<UiElement> links = UiManager.getCriteriaLinks(page.getPageName());
			if (init) {
				// Load criteria links
				for (UiElement element : links) {
					Link link = getLink(page.getCriteria(), element.linkName, ctx);
					UiLink uiLink;

					if (UiElement.Type.LINK_COMBO.equals(element.type)) {
						uiLink = new UiLink(Type.COMBO);
						DbQuery filterQuery = null;
						if (element.searchQueryName != null) {
							filterQuery = DB.getQuery(ctx, link.getModel().getRefEntityName(), element.searchQueryName);
						}
						uiLink.setComboValues(domainLogic.internalUiLinkLoadCombo(page.getCriteria(), link.getModel(), filterQuery, page, ctx));
					} else {
						uiLink = new UiLink(Type.LINK);
					}
					link.getTemplates().add(uiLink);
					page.getCriteria().setLink(element.linkName, link);
				}
				// implement user saved Criteria here if needed
			}

			// Load criteria links
			for (UiElement element : links) {
				Link link = page.getCriteria().getLink(element.linkName);
				// Selected key in combo is different from key in Link
				link.updateFromUi(page.getCriteria());

				// Handle simple link in criteria
				if (link.getEncodedValue() != null) {
					link.setEntity(DB.getRef(page.getCriteria(), link.getModel(), ctx));
					DomainLogic<Entity> linkEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(link.getEntity().$_getName());
					link.getTemplates().get(0).setDescription(linkEntityLogic.internalDoDescription(link.getEntity(), ctx));
				}
				boolean visible = domainLogic.internalUiLinkIsVisible(page, element.linkName, null, ctx);
				String label = domainLogic.internalUiLinkCaption(page.getCriteria(), element.linkName, null, ctx);
				UiAccess access = new UiAccess(element.linkName, visible, false, label, false);
				pageAccess.put(element.linkName, access);
			}

			DbQuery query = DB.getQuery(ctx, page.getDomainName(), page.getQueryName());
			if (page.getMaxRow() > 0) {
				query.setMaxRownum(page.getMaxRow());
			}
			if (page.getSortByField() != null) {
				// Sort by the desired field in first position in the query
				query.addSortBy(page.getSortByField(), null, page.getSortByDirection(), true);
			}

			domainLogic.internalUiListPrepare(query, page, ctx);

			DbManager dbManager = null;
			try {
				dbManager = DB.createDbManager(ctx, query);
				Results queryResults = dbManager.toResults();
				queryResults.setResultSetCount(dbManager.count());

				for (DbQuery.Var var : query.getOutVars()) {
					String columnKey = var.tableId + "_" + var.name;
					queryResults.getTitles().put(columnKey, domainLogic.internalUiListColumnCaption(query, null, columnKey, ctx));
				}
				page.setResults(queryResults);
				page.setLastActionSuccess(true);
			} catch (TechnicalException e) {
				String errorMsg = MessageUtils.getInstance().getMessage("uiControlerModel.queryExecError",
						new Object[] { (Object) e.getMessage() });
				LOGGER.error(errorMsg, e);
				ctx.getMessages().add(new Message(
						errorMsg,
						Severity.ERROR));
				page.setLastActionSuccess(false);
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}

			page.setMessages(ctx.getMessages());
			String pageTitle = MessageUtils.getInstance().getListTitle(page.getQueryName());
			if (page.getQueryName() == null) {
				pageTitle = MessageUtils.getInstance().getListTitle(DomainUtils.createDbName(page.getDomainName()));
			}
			page.setTitle(pageTitle);
			
			
			
			return page;
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	/**
	 * Prepare ListPage for a file export (csv, pdf, xls, etc.)
	 * 
	 * @param <E>
	 *            Main domain of the list.
	 * @param page
	 *            ListPage from which we prepare the export
	 * @param user
	 *            Current User
	 * @param typeExport
	 *            Export type (csv, xls, pdf, etc.)
	 * @return ListPage prepared for next display (export will be proposed on download after come back to list page)
	 */
	public <E extends Entity> ListPage<E> prepareListExport(ListPage<E> page, ApplicationUser user, String typeExport) {
		Context ctx = null;
		try {

			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(page.getDomainName());

			ctx = new Context(DB.createDbConnection(), user);

			List<UiElement> links = UiManager.getCriteriaLinks(page.getPageName());

			// Load criteria links
			for (UiElement element : links) {
				Link link = page.getCriteria().getLink(element.linkName);
				// Selected key in combo is different from key in Link
				link.updateFromUi(page.getCriteria());
			}

			DbQuery query = DB.getQuery(ctx, page.getDomainName(), page.getQueryName());
			if (page.getSortByField() != null) {
				query.addSortBy(page.getSortByField(), null, page.getSortByDirection());
			}

			page.setExportType(typeExport);
			domainLogic.internalUiListPrepare(query, page, ctx);
			page.setExportType(null);

			DbManager dbManager = null;
			try {
				dbManager = DB.createDbManager(ctx, query);
				Results queryResults = dbManager.toResults();
				queryResults.setResultSetCount(dbManager.count());

				for (DbQuery.Var var : query.getOutVars()) {
					String columnKey = var.tableId + "_" + var.name;
					queryResults.getTitles().put(columnKey, domainLogic.internalUiListColumnCaption(query, null, columnKey, ctx));
				}

				if (typeExport.equals("xls")) {
					page.setAttachment(prepareExcelSheet(query, queryResults, page.getCriteria(), ctx));
				}

				page.setLastActionSuccess(true);
			} catch (TechnicalException e) {
				String errorMsg = MessageUtils.getInstance().getMessage("uiControlerModel.queryExecError",
						new Object[] { (Object) e.getMessage() });
				LOGGER.error(errorMsg, e);
				ctx.getMessages().add(new Message(
						errorMsg,
						Severity.ERROR));
				page.setLastActionSuccess(false);
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}
			
			page.setMessages(ctx.getMessages());
			page.setTitle(MessageUtils.getInstance().getListTitle(page.getQueryName()));
			return page;
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	public File prepareExcelSheet(DbQuery query, Results results, Entity criteria, Context ctx) {
		File excelFile = null;

		try {
			excelFile = File.createTempFile(query.getName(), ".xls");
			new ExcelWriter().export(excelFile, query, results, criteria);

		} catch (Exception e) {
			ctx.getMessages().add(new Message(
					MessageUtils.getInstance().getMessage("uiControlerModel.cvsExportError", new Object[] { (Object) e.getMessage() }),
					Severity.ERROR));
		}
		return excelFile;
	}


	/**
	 * Preparation of an action page. This method is a wrapper that builds a new Context around the current user.
	 * 
	 * @param <E>
	 *            Action page main Entity type
	 * @param page
	 *            ActionPage to prepare. Contains entity type, action code and type, possibly a linkName from which we launched the action.
	 * @param previousPage
	 *            PreviousPage, this is where we'll go back after action validation if there's no error nor nextAction.
	 * @param user
	 *            The current User
	 * @return The preparedPage. Not necessarily an action (could be the previousPage, could be a custom page, etc.).
	 */
	public <E extends Entity> Page<?> prepareAction(ActionPage<E> page, Page<?> previousPage, ApplicationUser user) {
		Context ctx = null;
		try {
			ctx = new Context(DB.createDbConnection(), user);
			return prepareAction(page, previousPage, ctx);
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	/**
	 * Preparation of an action page.
	 * 
	 * @param <E>
	 *            Action page main Entity type
	 * @param page
	 *            ActionPage to prepare. Contains entity type, action code and type, possibly a linkName from which we launched the action.
	 * @param previousPage
	 *            PreviousPage, this is where we'll go back after action validation if there's no error nor nextAction.
	 * @param ctx
	 *            Context containing current user, messages and database connection.
	 * @return The preparedPage. Not necessarily an action (could be the previousPage, could be a custom page, etc.).
	 */
	public <E extends Entity> Page<?> prepareAction(ActionPage<E> page, Page<?> previousPage, Context ctx) {
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(page.getDomainName());

		try {
			if (previousPage != null) {
				page.setNextPage(previousPage);
			}
			if (ActionUtils.isCustom(page.getAction().type)) {
				if (page.getBean() == null) {
					// We're on a custom page for the first time. We need to load the bean.
					if (page.getAction().type == Constants.SINGLE_ELT_CUSTOM_ACTION
							|| page.getAction().type == Constants.SINGLE_ELT_CUSTOM_ACTION_DISPLAY) {
						if (page.getKeyList().size() != 1) {
							throw new FunctionalException(new Message(
									MessageUtils.getInstance().getMessage("uiControlerModel.selectedElements", null), 
									Severity.ERROR));
						}
						Key pk = page.getKeyList().get(0);
						page.setBean((E) DB.get(page.getDomainName(), pk, page.getAction(), ctx));
					} else {
						// Mutliple bean or no bean customAction
						// We set a bean for easier use of domain logic methods
						page.setBean((E) DomainUtils.newDomain(page.getDomainName()));
						domainLogic.internalDbPostLoad(page.getBean(), page.getAction(), ctx);
					}
				} else {
					// Bean already loaded, maybe we're back here from a validation error or a action through a link
					for (String transientField : page.getBean().getTransientFields()) {
						if (page.getBean().getModel().getField(transientField).getMemory() == Memory.ALWAYS) {
							Object memoryValue = domainLogic.internalDoVarValue(page.getBean(), transientField, ctx);
							if (memoryValue == null) {
								Map<String, Object> vars = page.getBean().dump();
								memoryValue = domainLogic.internalDoVarValue(vars, page.getDomainName(), transientField, ctx);
							}
							page.getBean().invokeSetter(transientField, memoryValue);
						}
					}
				}
			} else if (page.getAction().type != Constants.SELECT && page.getAction().type != Constants.DETACH) {
				// Multiple selected elements will become as many action pages.
				List<Key> keyList = page.getKeyList();
				Key pk = null;
				if (keyList != null) {
					if (keyList.size() > 1 && page.getBean() == null) {
						// Initialize of multiple line selection, one action per
						// line
						Key primaryKey = keyList.remove(0);
						ActionPage<E> clonedPage = page.clone();
						page.getKeyList().clear();
						page.getKeyList().add(primaryKey);
						page.setNextPage(clonedPage);
					}
					pk = keyList.get(0);
				}

				if (page.getBean() == null) {
					if (page.getAction().type == Constants.CREATE) {
						page.setBean((E) DomainUtils.newDomain(page.getDomainName()));
						domainLogic.internalDbPostLoad(page.getBean(), page.getAction(), ctx);
					} else if (pk != null && pk.isFull()) {
						page.setBean((E) DB.get(page.getDomainName(), pk, page.getAction(), ctx));

						if (page.getAction().type == Constants.COPY) {
							Key beanPk = page.getBean().getPrimaryKey();
							beanPk.nullify();
							page.getBean().setPrimaryKey(beanPk);
						}
					} else {
						// Action launched without primary key from menu.
						// Pk must be choosen within custom code
						pk = domainLogic.internalUiCtrlMenuAction(page.getAction(), ctx);
						if (pk == null) {
							throw new TechnicalException("You must implement uiCtrlMenuAction in " + page.getDomainName()
									+ "Logic to select a primary key for preparation of " + page.getAction().toString());
						}
						page.setBean((E) DB.get(page.getDomainName(), pk, page.getAction(), ctx));
					}
				} else {
					// Bean already loaded, maybe we're back here from a validation error or a action through a link
					for (String transientField : page.getBean().getTransientFields()) {
						if (page.getBean().getModel().getField(transientField).getMemory() == Memory.ALWAYS) {
							Object memoryValue = domainLogic.internalDoVarValue(page.getBean(), transientField, ctx);
							if (memoryValue == null) {
								Map<String, Object> vars = page.getBean().dump();
								memoryValue = domainLogic.internalDoVarValue(vars, page.getDomainName(), transientField, ctx);
							}
							page.getBean().invokeSetter(transientField, memoryValue);
						}
					}
				}
			}

			ActionPage<?> overridePage = domainLogic.internalUiCtrlOverrideAction(page, ctx);
			page.setLastActionSuccess(true);
			if (overridePage != null) {
				// Prepare new page
				return prepareAction(overridePage, overridePage.getNextPage(), ctx);
			}

			// If no UI to display
			if (ActionUtils.hasNoDisplay(page.getAction().type)) {
				return validateAction(page, ctx);
			}

			// Load links
			String pageName = page.getPageName();
			UiPage uiPage = UiManager.getPage(pageName);
			if (uiPage == null) {
				throw new FunctionalException(new Message(
					MessageUtils.getInstance().getMessage("uiControlerModel.pageNotFound", new Object[]{(Object)pageName}), 
					Severity.ERROR));
			}

			if (page.getLinkName() != null && (page.getAction().type == Constants.CREATE || page.getAction().type == Constants.NO_ELT_CUSTOM_ACTION || page.getAction().type == Constants.NO_ELT_CUSTOM_ACTION_DISPLAY)) {
				// Action launched via a link
				if (page.getBean().getModel().getLinkModel(page.getLinkName()) != null) {
					// Creation launched from parent, through a link list for instance
					Entity previousActionBean = ((ActionPage<?>) page.getNextPage()).getBean();
					String fkName = page.getBean().getModel().getLinkModel(page.getLinkName()).getKeyName();
					page.getBean().setForeignKey(fkName, previousActionBean.getPrimaryKey());

					if (page.getAction().type == Constants.CREATE && null != previousPage
							&& Boolean.TRUE.equals(previousPage.getCustomData().get("scheduleLink"))) {
						// Creation launched from a schedule list, new entity start time and end time are initialized
						initEventCreation(page.getBean(), domainLogic, previousPage.getCustomData(), ctx);
					}
				} else {
					// Creation launched from child, through simple link for instance
					// Nothing to do here, foreign key on previousBean will be set after action validation (when created bean is persisted) and
					// commit is delayed on previous action validation .
				}
			}

			for (UiElement elt : uiPage.elements) {
				prepareLink(page, page.getBean(), elt, ctx);
			}

			Map<String, UiAccess> pageAccess = new HashMap<String, UiAccess>();

			for (UiElement elt : uiPage.elements) {
				prepareLinkAccess(page, pageAccess, elt, null, page.getBean(), domainLogic, !ActionUtils.isDataEditable(page.getAction().type),
						ctx);
			}
			prepareEntityAccess(page, pageAccess, null, page.getBean(), domainLogic, !ActionUtils.isDataEditable(page.getAction().type), ctx);
			page.setAccess(pageAccess);

			// Implement group templates custom hooks
			// call internalUiGroupIsVisible(bean, groupName, action)
			// call internalUiTabIsVisible(bean, tabName, action)
			// call internalUiTabToOpen(bean, action)
			page.setTitle(domainLogic.internalUiActionTitle(page, ctx));
			domainLogic.internalUiActionOnLoad(page, ctx);
			page.setMessages(ctx.getMessages());
		} catch (FunctionalException ex) {
			LOGGER.info(ex.getMessage(), ex);
			if (previousPage!=null)
				previousPage.setMessages(ex.getMessages());
			page.setLastActionSuccess(false);
			return previousPage;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			ctx.getMessages().add(new Message(ex.toString(), Severity.ERROR));
			if (previousPage!=null)
				previousPage.setMessages(ctx.getMessages());
			page.setLastActionSuccess(false);
			return previousPage;
		}
		return page;
	}

	/**
	 * Validate an action page.
	 * 
	 * @param <E>
	 *            ActionPage main entity.
	 * @param page
	 *            ActionPage to validate. It contains the bean, action, links, and possibly user values entered in the action form.
	 * @param ctx
	 *            Context containing current user and database connection.
	 * @return Next page to display, or current page if there are any validation errors.
	 */
	public <E extends Entity> Page<?> validateAction(ActionPage<E> page, Context ctx) {
		try {

			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(page.getDomainName());
			domainLogic.internalUiActionOnValidation(page, ctx);

			if (ActionUtils.isDataEditable(page.getAction().type) && !ActionUtils.hasNoDisplay(page.getAction().type)) {
				page.updateLinks();
			}

			if (page.getAction().type == Constants.DETACH) {
				Entity baseBean = ((ActionPage<?>) page.getNextPage()).getBean();
				Link link = baseBean.getLink(page.getLinkName());
				if (baseBean.$_getName().equals(link.getModel().getRefEntityName())) {

					if (EntityManager.getEntityModel(link.getModel().getEntityName()).isAssociative()) {
						DB.removeAssociations(baseBean, page.getLinkName(), page.getKeyList(), ctx);
					} else {
						for (Key selectedKey : page.getKeyList()) {
							Entity selectedEntity = DB.get(link.getModel().getEntityName(), selectedKey, page.getAction(), ctx);
							selectedEntity.setForeignKey(link.getModel().getKeyName(), null);
							DB.persist(selectedEntity, page.getAction(), ctx);
						}
					}
					for (UiLink uiLink : link.getTemplates()) {
						if (uiLink.getType().equals(Type.LINK_LIST)) {
							Results results = DB.getList(baseBean, page.getLinkName(), uiLink.getQueryName(), ctx);
							uiLink.setResults(results);
						}
					}

				} else {
					link.setEntity(null);
					if (link.getTemplates().size() > 0) {
						link.getTemplates().get(0).setDescription(null);
					}
					baseBean.setForeignKey(link.getModel().getKeyName(), null);
				}
			}

			// Save strongly linked entities.
			ActionPage<E> errorPage = persistOneToOneLinks(page, ctx, false);
			if (null != errorPage) {
				return errorPage;
			}

			if (ActionUtils.isDataEditable(page.getAction().type)) {
				// Save schedule events.
				persistUpdatedScheduleLinks(page, ctx);
			}

			if (page.getAction().type == Constants.CREATE || page.getAction().type == Constants.COPY) {
				DB.insert(page.getBean(), page.getAction(), ctx);
			} else if (page.getAction().type == Constants.DELETE) {
				DB.remove(page.getBean(), page.getAction(), ctx);
				page.setBean(null);
			} else if (page.getAction().type == Constants.MODIFY) {
				DB.update(page.getBean(), page.getAction(), ctx);
			} else if (ActionUtils.isCustom(page.getAction().type)) {
				// Custom action. Runtime does nothing on its own
				domainLogic.internalDoCustomAction(page, page.getKeyList(), ctx);
			}

			// Save strongly linked entities (back references).
			errorPage = persistOneToOneLinks(page, ctx, true);
			if (null != errorPage) {
				return errorPage;
			}

			EntityModel mdl = EntityManager.getEntityModel(page.getDomainName());
			if (page.getLinkName() != null && mdl.getBackRefModel(page.getLinkName()) != null) {
				// Action launched via a link, from child entity, through simple link for instance, we set the new foreign key in child
				Entity previousActionBean = ((ActionPage<?>) page.getNextPage()).getBean();
				String fkName = mdl.getBackRefModel(page.getLinkName()).getKeyName();
				if (page.getAction().type == Constants.DELETE) {
					previousActionBean.setForeignKey(fkName, null);
				} else if (page.getAction().type != Constants.DETACH) {
					previousActionBean.setForeignKey(fkName, page.getBean().getPrimaryKey());
				}
			}

			try {
				ctx.getConnection().commit();
			} catch (DbException sqlEx) {
				throw new TechnicalException(sqlEx.getMessage(), sqlEx);
			}

			page.setLastActionSuccess(true);

			// Next Action
			// It's called AFTER commit so entity links can be accessed in next
			// action
			ActionPage<?> nextActionPage = domainLogic.internalUiCtrlNextAction(page, ctx);
			if (nextActionPage != null) {
				nextActionPage.setNextPage(page.getNextPage());
				return prepareAction(nextActionPage, page.getNextPage(), ctx);
			} else if (page.getNextPage() instanceof ActionPage && ActionUtils.hasNoDisplay(page.getAction().type)) {
				// No display action requires to update previous page if it is an action page (list pages are always updated).
				return prepareAction((ActionPage) page.getNextPage(), null, ctx);
			}

			page.getNextPage().setMessages(ctx.getMessages());
			page.getNextPage().setAttachment(ctx.getAttachment());

			return page.getNextPage();
		} catch (FunctionalException ex) {
			if (ex.getMessages() != null && ctx.getMessages().containsAll(ex.getMessages())) {
				// Messages already in context
			} else {
				ctx.addMessagesAs(ex.getMessages(), Severity.ERROR);
			}
		} catch (TechnicalException ex) {
			LOGGER.error("", ex);
			ctx.getMessages().add(new Message(ex.getMessage(), Severity.ERROR));
		} catch (Throwable t) {
			LOGGER.error("", t);
			ctx.getMessages().add(new Message(t.toString(), Severity.ERROR));
		}
		// En cas d'erreur sur une page custom sans affichage.
		if (ActionUtils.hasNoDisplay(page.getAction().type)) {
			page.getNextPage().setMessages(ctx.getMessages());
			return page.getNextPage();
		}
		page.setMessages(ctx.getMessages());
		page.setLastActionSuccess(false);
		return page;
	}

	/**
	 * Validate an action page. This is a wrapper for the validateAction(ActionPage<E> page, Context ctx) method. It builds a new Context
	 * 
	 * @param <E>
	 *            ActionPage main entity.
	 * @param page
	 *            ActionPage to validate. It contains the bean, action, links, and possibly user values entered in the action form.
	 * @param ctx
	 *            Context containing current user and database connection.
	 * @return Next page to display, or current page if there are any validation errors.
	 */
	public <E extends Entity> Page<?> validateAction(ActionPage<E> page, ApplicationUser user) {
		Context ctx = null;
		try {
			ctx = new Context(DB.createDbConnection(), user);
			return validateAction(page, ctx);
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	/**
	 * Validates a selection action on one or many elements.
	 * 
	 * @param baseBean
	 *            The reference bean
	 * @param linkName
	 *            The link name
	 * @param selectedKeys
	 * @param user
	 */
	public void validateSelection(Entity baseBean, String linkName, List<Key> selectedKeys, ApplicationUser user) {
		Context ctx = null;
		try {
			ctx = new Context(DB.createDbConnection(), user);
			Link link = baseBean.getLink(linkName);

			// Multiple selection - Selected elements are the link sources.
			if (EntityManager.getEntityModel(link.getModel().getEntityName()).isAssociative()) {
				DB.persistAssociations(baseBean, linkName, selectedKeys, ctx);
			} else {
				Key referenceKey = baseBean.getPrimaryKey();
				for (Key selectedKey : selectedKeys) {
					Entity selectedEntity = DB.get(link.getModel().getEntityName(), selectedKey, new Action(Constants.SELECT, Constants.SELECT),
							ctx);
					selectedEntity.setForeignKey(link.getModel().getKeyName(), referenceKey);
					DB.persist(selectedEntity, new Action(Constants.SELECT, Constants.SELECT), ctx);
				}
			}
			ctx.getConnection().commit();
		} catch (DbException e) {
			LOGGER.error(e.getMessage(), e);
			throw new TechnicalException(e.getMessage(), e);
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	/**
	 * Prepare ui template links (content, description, etc.)
	 * 
	 * @param <E>
	 *            Link main entity. Not necessarily the page main entity type if there are inner templates.
	 * @param page
	 *            The main ActionPage to prepare / display
	 * @param bean
	 *            The current bean from which we'll load a link
	 * @param elt
	 *            The current UiElement to display. This will describe the link to use, the display type and possible queries to use.
	 * @param ctx
	 *            The main context containing current User, Messages and DbConnection.
	 */
	private <E extends Entity> void prepareLink(ActionPage<?> page, E bean, UiElement elt, Context ctx) {
		Link link = getLink(bean, elt.linkName, ctx);
		if ((elt.type == UiElement.Type.BACK_REF_INNER || elt.type == UiElement.Type.BACK_REF)) {
			List<Entity> linkedEntities = DB.getLinkedEntities(bean, elt.linkName, ctx);
			if (linkedEntities != null && linkedEntities.size() > 0) {
				// This kind of single back reference shouldn't be allowed on non unique foreign keys.
				link.setEntity(linkedEntities.get(0));
			} else {
				if (elt.type == UiElement.Type.BACK_REF_INNER) {
					// Inner template NEEDS an empty bean. BackRef doesn't.
					link.setEntity(DomainUtils.newDomain(elt.entityName));
					// Set parent bean
					link.getEntity().setForeignKey(link.getModel().getKeyName(), bean.getPrimaryKey());
				} else {
					link.setEntity(null);
				}
			}
		}
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.$_getName());

		if (elt.type == UiElement.Type.LINK || elt.type == UiElement.Type.LINK_QUICK_SEARCH || elt.type == UiElement.Type.BACK_REF) {
			UiLink uiLink = new UiLink(Type.LINK);
			if (elt.type == UiElement.Type.BACK_REF) {
				uiLink = new UiLink(Type.BACK_REF);
			}
			if (link.getEntity() != null) {
				DomainLogic<Entity> linkEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(link.getEntity().$_getName());
				uiLink.setDescription(linkEntityLogic.internalDoDescription(link.getEntity(), ctx));
			}
			uiLink.setSearchQueryName(elt.searchQueryName);
			link.getTemplates().add(uiLink);
		}
		if (elt.type == UiElement.Type.LINK_INNER || elt.type == UiElement.Type.BACK_REF_INNER) {
			if (null == link.getEntity()) {
				if (elt.type == UiElement.Type.LINK_INNER) {
					link.setEntity(DomainUtils.newDomain(link.getModel().getRefEntityName()));
				} else if (elt.type == UiElement.Type.BACK_REF_INNER) {
					link.setEntity(DomainUtils.newDomain(link.getModel().getEntityName()));
				}
			}
			for (UiElement childElt : elt.elements) {
				prepareLink(page, link.getEntity(), childElt, ctx);
			}
		}
		if (elt.type == UiElement.Type.BACK_REF_LIST || elt.type == UiElement.Type.BACK_REF_LIST_SCHEDULE
				|| elt.type == UiElement.Type.BACK_REF_LIST_INPUT) {
			UiLink uiLink;

			if (elt.type == UiElement.Type.BACK_REF_LIST || elt.type == UiElement.Type.BACK_REF_LIST_INPUT) {
				uiLink = new UiLink(Type.LINK_LIST);
				Results results = null;
				if (!(page.getAction().type == Constants.CREATE && bean.equals(page.getBean()))) {
					results = DB.getList(bean, elt.linkName, elt.queryName, ctx);
				} else {
					results = new Results(DomainUtils.newDomain(bean.getModel().getBackRefEntityName(elt.linkName)));
				}
				uiLink.setResults(results);
			} else {
				uiLink = new UiScheduleLink(Type.LINK_LIST, elt.entityName);
				if (!(page.getAction().type == Constants.CREATE && bean.equals(page.getBean()))) {
					((UiScheduleLink) uiLink).getModel().setEvents(retrieveLinkEvents(page, bean, elt, ctx));
				} else {
					((UiScheduleLink) uiLink).getModel().setEvents(new ArrayList<ScheduleEvent>());
				}
			}
			if (elt.queryName != null) {
				uiLink.setQueryName(elt.queryName);
			} else {
				String brName = bean.getModel().getBackRefEntityName(elt.linkName);
				EntityModel brMdl = EntityManager.getEntityModel(brName);
				// Default query
				uiLink.setQueryName(brMdl.$_getDbName());
			}
			if (elt.searchQueryName != null) {
				uiLink.setSearchQueryName(elt.searchQueryName);
			} else {
				uiLink.setSearchQueryName(uiLink.getQueryName());
			}
			link.getTemplates().add(uiLink);
			if (bean.getLink(elt.linkName).getTemplates().size() > 0) {
				link.getTemplates().addAll(bean.getLink(elt.linkName).getTemplates());
			}
		}
		if (elt.type == UiElement.Type.LINK_COMBO || elt.type == UiElement.Type.LINK_MULTI_COMBO) {
			UiLink uiLink = new UiLink(Type.COMBO);
			DbQuery filterQuery = null;
			if (elt.searchQueryName != null) {
				filterQuery = DB.getQuery(ctx, link.getModel().getRefEntityName(), elt.searchQueryName);
			}
			uiLink.setComboValues(domainLogic.internalUiLinkLoadCombo(bean, link.getModel(), filterQuery, page, ctx));
			link.getTemplates().add(uiLink);
		}
		bean.setLink(elt.linkName, link);
	}

	public <E extends Entity> void prepareLinkListExport(ActionPage<?> page, E bean, String linkName, String queryName, String exportType,
			ApplicationUser user) {

		Context ctx = null;
		try {
			ctx = new Context(DB.createDbConnection(), user);
			DbManager dbManager = null;

			try {
				DbQuery query = DB.getLinkQuery(ctx, bean, linkName, queryName);
				dbManager = new DbManager(ctx, query);
				Results results = dbManager.toResults();
				results.setResultSetCount(dbManager.count());

				if (exportType.equals("xls")) {
					page.setAttachment(prepareExcelSheet(query, results, bean, ctx));
				}
				page.setMessages(ctx.getMessages());

			} catch (Exception exception) {
				throw new TechnicalException(exception.getMessage(), exception);

			} finally {
				if (null != dbManager) {
					dbManager.close();
				}
			}

		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	/**
	 * Loads a quickSearch link template for UI
	 * 
	 * @param <E>
	 *            Link main entity
	 * @param bean
	 *            The current bean from which we'll load a link
	 * @param page
	 *            The main ActionPage to prepare / display
	 * @param user
	 *            The current User
	 * @param linkName
	 *            Link to load with quickSearch template
	 * @param queryName
	 *            Name of the query used to retrieve suggestions
	 * @param criteria
	 *            User input, criteria used in search query
	 * @return The Map of results we'll display in the quick search template
	 */
	public <E extends Entity> Map<String, String> loadQuickSearchValues(E bean, Page<?> page, ApplicationUser user, String linkName,
			String queryName, String criteria) {

		Map<String, String> result = new HashMap<String, String>();
		Context ctx = null;

		try {
			ctx = new Context(DB.createDbConnection(), user);
			Link link = bean.getLink(linkName);
			DbQuery filterQuery = null;
			String refEntityName = link.getModel().getRefEntityName();

			if (queryName != null) {
				filterQuery = DB.getQuery(ctx, refEntityName, queryName);
			} else {
				filterQuery = DB.createQuery(ctx, refEntityName, "T01");
			}
			filterQuery.setCaseInsensitiveSearch(true);

			List<? extends Var> columns = filterQuery.getOutVars();
			List<String> colAliases = new ArrayList<String>();
			List<String> tableAliases = new ArrayList<String>();
			Set<String> lookupFields = EntityManager.getEntityModel(refEntityName).getLookupFields();

			if (lookupFields.isEmpty()) {

				for (Var var : columns) {
					colAliases.add(var.name);
					tableAliases.add(var.tableId);
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
			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.$_getName());

			try {
				Map<Key, String> values = domainLogic.internalUiLinkLoadValues(bean, link.getModel(), filterQuery, page, true, ctx);

				if (null == values || values.isEmpty()) {
					result.put("-1", MessageUtils.getInstance().getMessage("autocomplete.noResult", null));

				} else if (values.size() > Constants.AUTOCOMPLETE_MAX_ROW) {
					result.put("-1", MessageUtils.getInstance().getMessage("autocomplete.tooManyResults", new Object[] { values.size() }));

				} else {
					for (Entry<Key, String> e : values.entrySet()) {
						result.put(e.getKey().getEncodedValue(), e.getValue());
					}
				}
				page.setLastActionSuccess(true);

			} catch (TechnicalException exception) {
				result.put("-1", MessageUtils.getInstance().getMessage("autocomplete.error", null));
				page.setLastActionSuccess(false);
			}
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
		return result;
	}

	/**
	 * initialize a link
	 * 
	 * @param e
	 *            Main entity
	 * @param linkName
	 *            Link to initialize
	 * @param ctx
	 *            Context containing current User, DbConnection and messages
	 * @return	Initialized link with possible entity set
	 */
	private Link getLink(Entity e, String linkName, Context ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.$_getName());
		if (!eModel.getAllLinkNames().contains(linkName)) {
			throw new TechnicalException("Link " + linkName + " is not a link nor a backRef of " + e.$_getName());
		}

		LinkModel linkModel = eModel.getLinkModel(linkName);
		if (linkModel != null) {
			Link link = new Link(linkModel);
			link.setEntity(DB.getRef(e, linkModel, ctx));
			return link;
		} else {
			linkModel = eModel.getBackRefModel(linkName);
			Link backRef = new Link(linkModel);
			backRef.setEntity(e);
			return backRef;
		}
	}

	/**
	 * Prepares link access (visibility, modification)
	 * 
	 * @param <E>
	 *            Main entity
	 * @param page
	 *            action page to prepare / display
	 * @param pageAccess
	 *            Map to fill with all access
	 * @param elt
	 *            UiElement describing the link in current UI
	 * @param linkNamePrefix
	 *            Link prefix, in case of inner templates
	 * @param bean
	 *            Current bean from which we load the link
	 * @param domainLogic
	 *            DomainLogic class to use for custom visibility / disable on elements
	 * @param parentProtected
	 *            true if the parent object is already protected
	 * @param ctx
	 *            Context containing current User, DbConnection and messages
	 */
	private <E extends Entity> void prepareLinkAccess(ActionPage<?> page, Map<String, UiAccess> pageAccess, UiElement elt,
			String linkNamePrefix, E bean, DomainLogic domainLogic, boolean parentProtected, Context ctx) {
		String key = elt.linkName;
		if (linkNamePrefix != null) {
			key = linkNamePrefix + "." + key;
		}
		if (elt.type == UiElement.Type.LINK_COMBO || elt.type == UiElement.Type.LINK || elt.type == UiElement.Type.LINK_QUICK_SEARCH || elt.type == UiElement.Type.LINK_MULTI_COMBO) {
			boolean visible = domainLogic.internalUiLinkIsVisible(page, elt.linkName, page.getAction(), ctx);
			boolean readOnly = false;
			if (page.getLinkName() != null && page.getLinkName().equals(elt.linkName)) {
				readOnly = true;
			}
			boolean mandatory = false;
			if (bean.getModel().getLinkModel(elt.linkName) != null) {
				// Link source is the current entity.
				String fKeyName = bean.getLink(elt.linkName).getModel().getKeyName();
				mandatory = bean.getModel().isStrongKey(fKeyName);
				for (String varName : bean.getModel().getForeignKeyModel(fKeyName).getFields()) {
					if (parentProtected || domainLogic.internalUiVarIsProtected(page, bean, varName, ctx)) {
						// If at least one variable of the link is protected,
						// link is protected.
						readOnly = true;
					}
					mandatory |= domainLogic.internalDoVarIsMandatory(bean, varName, page.getAction(), ctx);
				}
			}
			String label = domainLogic.internalUiLinkCaption(bean, elt.linkName, page.getAction(), ctx);
			UiAccess access = new UiAccess(key, visible, readOnly, label, mandatory);
			pageAccess.put(key, access);

		}
		if (elt.type == UiElement.Type.BACK_REF_LIST || elt.type == UiElement.Type.BACK_REF_LIST_SCHEDULE) {
			// Link is a linked list
			key = key + "_" + elt.queryName;
			// TODO : implement internalUiListIsVisible()
			// boolean visible = internalUiListIsVisible(bean, 
			// elt.linkName, elt.queryName, page.getAction())
			boolean visible = true;
			boolean readOnly = domainLogic.internalUiListIsReadOnly(bean, elt.linkName, elt.queryName, page.getAction(), ctx);
			UiAccess access = new UiAccess(key, visible, readOnly, null, false);
			access.isProtected = domainLogic.internalUiListIsProtected(bean, elt.linkName, elt.queryName, page.getAction(), ctx);
			pageAccess.put(key, access);
		}
		if (elt.type == UiElement.Type.LINK_INNER || elt.type == UiElement.Type.BACK_REF_INNER) {
			// If link is strong-key based, not everything is read only...
			Entity innerEntity = bean.getLink(elt.linkName).getEntity();
			if (innerEntity == null) {
				innerEntity = DomainUtils.newDomain(elt.entityName);
			}
			DomainLogic innerDomainLogic = DomainUtils.getLogic(innerEntity.$_getName());
			boolean isProtected = parentProtected;
			if (!isProtected) {
				if (bean.getModel().getLinkModel(elt.linkName) != null) {
					// INNER on classic link
					if (!bean.getModel().isStrongKey(bean.getModel().getLinkModel(elt.linkName).getKeyName())) {
						isProtected = true;
					}
				} else if (bean.getModel().getBackRefModel(elt.linkName) != null) {
					// Reverse inner link with (hopefully) a unique Foreign Key
					// Constraint
					// Yes, this is possible.
					// No, this is NOT a good idea.
					EntityModel mdl = EntityManager.getEntityModel(bean.getModel().getBackRefModel(elt.linkName).getEntityName());
					if (!mdl.isStrongKey(mdl.getLinkModel(elt.linkName).getKeyName())) {
						isProtected = true;
					}
				}
				// If current action doesn't exist on target bean, it must be
				// protected
				if (innerEntity.getModel().getAction(page.getAction().code) == null) {
					isProtected = true;
				}
			}
			prepareEntityAccess(page, pageAccess, key, innerEntity, innerDomainLogic, isProtected, ctx);
			for (UiElement childElt : elt.elements) {
				prepareLinkAccess(page, pageAccess, childElt, key, innerEntity, innerDomainLogic, isProtected, ctx);
			}
		}
	}

	/**
	 * Prepare access on an entity
	 * 
	 * @param <E>
	 *            Main entity
	 * @param page
	 *            Page to prepare / display
	 * @param pageAccess
	 *            Map to fill with all access
	 * @param linkNamePrefix
	 *            Link prefix, in case of inner templates
	 * @param bean
	 *            Current bean from which we load the link
	 * @param domainLogic
	 *            DomainLogic class to use for custom visibility / disable on elements
	 * @param parentProtected
	 *            true if the parent object is already protected
	 * @param ctx
	 *            Context containing current User, DbConnection and messages
	 */
	private <E extends Entity> void prepareEntityAccess(Page<?> page, Map<String, UiAccess> pageAccess, String linkNamePrefix, E bean,
			DomainLogic domainLogic, boolean parentProtected, Context ctx) {
		for (String varName : bean.getModel().getFields()) {
			String key = bean.$_getName() + "." + varName;
			if (linkNamePrefix != null) {
				key = linkNamePrefix + "." + key;
			}

			// Default values for filter template in list page
			boolean visible = true;
			boolean readOnly = false;
			boolean mandatory = false;

			Action action = null;
			boolean actionPage = (page instanceof ActionPage);
			if (actionPage) {
				action = ((ActionPage<?>) page).getAction();
				visible = domainLogic.internalUiVarIsVisible(page, bean, varName, ctx);
				readOnly = parentProtected || domainLogic.internalUiVarIsProtected(page, bean, varName, ctx);
				mandatory = (actionPage && bean.getModel().getField(varName).isMandatory())
						|| domainLogic.internalDoVarIsMandatory(bean, varName, action, ctx);
			}
			String label = domainLogic.internalUiVarCaption(bean, varName, action, ctx);
			UiAccess access = new UiAccess(key, visible, readOnly, label, mandatory);
			pageAccess.put(key, access);
		}
	}

	/**
	 * Persists strongly linked entities displayed in the same page as a persisted entity
	 * 
	 * @param page
	 *            The current action page
	 * @param ctx
	 *            Context containing current User, DbConnection and messages
	 * @param backRef
	 *            Indicates whether links or backRefs should be persisted
	 * @return The current action page if an error occurs, {@code null} otherwise
	 */
	private <E extends Entity> ActionPage<E> persistOneToOneLinks(ActionPage<E> page, Context ctx, boolean backRef) {
		if (ActionUtils.persistOneToOneLinks(page.getAction().type)) {
			String pageName = page.getPageName();
			UiPage uiPage = UiManager.getPage(pageName);
			if (uiPage == null) {
				throw new TechnicalException(MessageUtils.getInstance().getMessage("uiControlerModel.pageNotFound", new Object[]{(Object)pageName}));
			}
			for (UiElement elt : uiPage.elements) {
				try {
					persistOneToOneLinks(page, page.getBean(), elt, ctx, backRef);
				} catch (FunctionalException ex) {
					page.setMessages(ex.getMessages());
					page.setLastActionSuccess(false);
					return page;
				}
			}
		}
		return null;
	}

	/**
	 * Persists strongly linked entities displayed in the same page as a persisted entity
	 * 
	 * @param page
	 *            The current actionpage
	 * @param bean
	 *            The main bean
	 * @param elt
	 *            Description of the link element to possibly persist
	 * @param ctx
	 *            Context containing current User, DbConnection and messages
	 * @param backRef
	 *            Indicates whether links or backRefs should be persisted
	 */
	private void persistOneToOneLinks(ActionPage<?> page, Entity bean, UiElement elt, Context ctx, boolean backRef) {
		if ((backRef && elt.type == UiElement.Type.LINK_INNER) || (!backRef && elt.type == UiElement.Type.BACK_REF_INNER)) {
			return;
		}
		if (elt.type == UiElement.Type.LINK_INNER || elt.type == UiElement.Type.BACK_REF_INNER) {
			if (bean.getModel().getLinkModel(elt.linkName) != null) {
				// INNER on classic link
				if (!bean.getModel().isStrongKey(bean.getModel().getLinkModel(elt.linkName).getKeyName())) {
					// Not a Strong key --> No modification on linked entity
					// neither its children elements
					return;
				}
				if (page.getLinkName() != null && page.getLinkName().equals(elt.linkName)) {
					// link used to launch action, no modification allowed
					return;
				}
			} else if (bean.getModel().getBackRefModel(elt.linkName) != null) {
				// Reverse inner link with (hopefully) a unique Foreign Key
				// Constraint
				EntityModel mdl = EntityManager.getEntityModel(bean.getModel().getBackRefModel(elt.linkName).getEntityName());
				if (!mdl.isStrongKey(mdl.getLinkModel(elt.linkName).getKeyName())) {
					// Not a Strong key --> No modification on linked entity
					// neither its children elements
					return;
				}
			}

			Entity innerEntity = bean.getLink(elt.linkName).getEntity();

			// If we got here, we were in CREATE or MODIFY action type.
			if (page.getAction().type == Constants.CREATE) {
				if (backRef) {
					innerEntity.setForeignKey(bean.getModel().getBackRefModel(elt.linkName).getKeyName(), bean.getPrimaryKey());
				}
				DB.insert(innerEntity, page.getAction(), ctx);
				if (!backRef) {
					bean.setForeignKey(bean.getModel().getLinkModel(elt.linkName).getKeyName(), innerEntity.getPrimaryKey());
				}
			} else if (page.getAction().type == Constants.MODIFY) {
				DB.persist(innerEntity, page.getAction(), ctx);
			} else {
				// Won't happen in classic runtime mode, but possible with custom dynamic type modification.
				// I can't see why it would be needed, but it should work.
			}

			for (UiElement childElt : elt.elements) {
				persistOneToOneLinks(page, innerEntity, childElt, ctx, backRef);
			}
		}
	}

	public FileContainer getFile(Entity bean, Page<?> page, ApplicationUser user, String propertyName) {
		Context ctx = null;

		try {
			ctx = new Context(DB.createDbConnection(), user);
			FileContainer container = (FileContainer) bean.invokeGetter(propertyName + "Container");

			if (!bean.getModel().getField(propertyName).isTransient()) {
				DbEntity dbEntity = new DbFactory().createDbEntity();
				byte[] content = dbEntity.getLobContent(ctx, bean, propertyName);
				container.setContent(content);

			} else {
				Action action = new Action(Constants.DISPLAY, Constants.DISPLAY_FILE);
				Entity entity = DB.get(bean.$_getName(), bean.getPrimaryKey(), action, ctx);

				if (null != entity) {
					Object file = entity.invokeGetter(propertyName);
	
					if (file instanceof FileContainer) {
						container = (FileContainer) file;
					}
				}			
			}
			page.setLastActionSuccess(true);
			return container;
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
	}
		}
	}

	public <E extends Entity> void deleteFile(E bean, Page<?> page, ApplicationUser user, String propertyName) {
		Context ctx = null;

		try {
			ctx = new Context(DB.createDbConnection(), user);
			Action action = new Action(Constants.ACTION_MODIFY, Constants.DELETE_FILE);
			bean.invokeSetter(propertyName, null);
			DB.update(bean, action, ctx);

			try {
				ctx.getConnection().commit();
				page.setLastActionSuccess(true);
			} catch (DbException sqlEx) {
				throw new TechnicalException(sqlEx.getMessage(), sqlEx);
			}
		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}

	/**
	 * Retrieve link entities as events to be displayed into a schedule.
	 * @param page Current action page.
	 * @param bean Current bean which events are linked to.
	 * @param elt Object which contains link information (link name, query name, etc).
	 * @param ctx Context.
	 * @return A list of {@link ScheduleEvent} or an empty list.
	 */
	private <E extends Entity> List<ScheduleEvent> retrieveLinkEvents(ActionPage<?> page, E bean, UiElement elt, Context ctx) {
		List<Entity> linkedEntities = DB.getLinkedEntities(bean, elt.linkName, elt.queryName, ctx);
		List<ScheduleEvent> events = new ArrayList<ScheduleEvent>(linkedEntities.size());
		String entityName = elt.entityName;
		DomainLogic<Entity> linkedEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entityName);

		for (Entity entity : linkedEntities) {
			Map<String, Object> vars = entity.dump();
			ScheduleEvent event = new ScheduleEvent();
			event.setId(entity.getPrimaryKey().getEncodedValue());
			event.setTitle(linkedEntityLogic.internalDoDescription(entity, ctx));
			event.setReadonly(linkedEntityLogic.internalUiVarIsProtected((ActionPage<Entity>) page, entity, Constants.EVENT, ctx));
			String startField = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_DATE_START, ctx);
			if (startField == null) {
				startField = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_DATE_START, ctx);
			}
			if (startField == null) {
				startField = Constants.EVENT_DATE_START;
			}
			event.setStart((Date) entity.invokeGetter(startField));

			String endField = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_DATE_END, ctx);
			if (endField == null) {
				endField = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_DATE_END, ctx);
			}
			if (endField == null) {
				endField = Constants.EVENT_DATE_END;
			}
			event.setEnd((Date) entity.invokeGetter(endField));

			String className = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_CSS_CLASSNAME, ctx);
			if (className == null) {
				className = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_CSS_CLASSNAME, ctx);
			}
			event.setClassName(className);

			String color = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_CSS_COLOR, ctx);
			if (color == null) {
				color = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_CSS_COLOR, ctx);
			}
			event.setColor(color);

			String backgroundColor = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_CSS_BACKGROUND_COLOR, ctx);
			if (backgroundColor == null) {
				backgroundColor = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_CSS_BACKGROUND_COLOR, ctx);
			}
			event.setBackgroundColor(backgroundColor);

			String borderColor = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_CSS_BORDER_COLOR, ctx);
			if (borderColor == null) {
				borderColor = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_CSS_BORDER_COLOR, ctx);
			}
			event.setBorderColor(borderColor);

			String textColor = (String) linkedEntityLogic.internalDoVarValue(entity, Constants.EVENT_CSS_TEXT_COLOR, ctx);
			if (textColor == null) {
				textColor = (String) linkedEntityLogic.internalDoVarValue(vars, entityName, Constants.EVENT_CSS_TEXT_COLOR, ctx);
			}
			event.setTextColor(textColor);
			events.add(event);
		}
		return events;
	}
	/**
	 * Initialize bean's start and end variable for creation page.
	 * @param bean Bean to update.
	 * @param domainLogic DomainLogic used to retrieve start and end variable.
	 * @param customData Custom data from the previous page used to get selected date.
	 * @param ctx Context.
	 */
	private <E extends Entity> void initEventCreation(E bean, DomainLogic<E> domainLogic, Map<String, Object> customData, Context ctx) {
		Map<String, Object> beanData = bean.dump();
		String beanEntityName = bean.$_getName();
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime((Date) customData.get("scheduleDate"));
		startCalendar.set(Calendar.SECOND, 0);
		startCalendar.set(Calendar.MILLISECOND, 0);
		beanData.put(Constants.EVENT_CREATE_DATE_START, startCalendar);
		Calendar eventStartTime = (Calendar) domainLogic.internalDoVarValue(bean, Constants.EVENT_CREATE_DATE_START, ctx);
		if (eventStartTime == null) {
			eventStartTime = (Calendar) domainLogic.internalDoVarValue(beanData, beanEntityName, Constants.EVENT_CREATE_DATE_START, ctx);
		}
		if (null != eventStartTime) {
			startCalendar.set(Calendar.HOUR_OF_DAY, eventStartTime.get(Calendar.HOUR_OF_DAY));
			startCalendar.set(Calendar.MINUTE, eventStartTime.get(Calendar.MINUTE));
			startCalendar.set(Calendar.SECOND, eventStartTime.get(Calendar.SECOND));
		}
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(startCalendar.getTime());

		Calendar eventEndTime = (Calendar) domainLogic.internalDoVarValue(bean, Constants.EVENT_CREATE_DATE_END, ctx);
		if (eventStartTime == null) {
			eventEndTime = (Calendar) domainLogic.internalDoVarValue(beanData, beanEntityName, Constants.EVENT_CREATE_DATE_END, ctx);
		}
		if (null != eventEndTime) {
			endCalendar.set(Calendar.HOUR_OF_DAY, eventEndTime.get(Calendar.HOUR_OF_DAY));
			endCalendar.set(Calendar.MINUTE, eventEndTime.get(Calendar.MINUTE));
			endCalendar.set(Calendar.SECOND, eventEndTime.get(Calendar.SECOND));
		} else {
			endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY) + Constants.EVENT_CREATE_DEFAULT_DURATION);
		}
		updateEventDates(bean, beanData, domainLogic, startCalendar.getTime(), endCalendar.getTime(), ctx);
	}

	/**
	 * Updates event's start and end time.
	 * @param bean Bean to update.
	 * @param beanData Bean object as a map.
	 * @param logic DomainLogic used to retrieve start and end variables' name.
	 * @param start New start time.
	 * @param end New end time.
	 * @param ctx Context.
	 */
	private <E extends Entity> void updateEventDates(E bean, Map<String, Object> beanData, DomainLogic<E> logic, Date start, Date end,
			Context ctx) {
		String startField = (String) logic.internalDoVarValue(bean, Constants.EVENT_DATE_START, ctx);
		if (startField == null) {
			startField = (String) logic.internalDoVarValue(beanData, bean.$_getName(), Constants.EVENT_DATE_START, ctx);
		}
		if (null == startField) {
			startField = Constants.EVENT_DATE_START;
		}
		bean.invokeSetter(startField, start);

		String endField = (String) logic.internalDoVarValue(bean, Constants.EVENT_DATE_END, ctx);
		if (endField == null) {
			endField = (String) logic.internalDoVarValue(beanData, bean.$_getName(), Constants.EVENT_DATE_END, ctx);
		}
		if (null == endField) {
			endField = Constants.EVENT_DATE_END;
		}
		bean.invokeSetter(endField, end);
	}

	/**
	 * Runs through bean's links of the page given in parameter to find schedule events to update (these links can be updated directly by resizing or dragging).
	 * @param page Current page.
	 * @param ctx Context.
	 */
	private <E extends Entity> void persistUpdatedScheduleLinks(ActionPage<E> page, Context ctx) {
		for (Link link : page.getBean().getLinks().values()) {
			for (UiLink template : link.getTemplates()) {
				if (template instanceof UiScheduleLink) {
					UiScheduleLink scheduleLink = (UiScheduleLink) template;
					for (ScheduleEvent event : scheduleLink.getModel().getEvents()) {
						if (event.isUpdated()) {
							Entity linkedEntity = DB.get(scheduleLink.getEntityName(), new Key(scheduleLink.getEntityName(), event.getId()), ctx);
							DomainLogic<Entity> linkedEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(linkedEntity.$_getName());
							Map<String, Object> beanData = linkedEntity.dump();
							updateEventDates(linkedEntity, beanData, linkedEntityLogic, event.getStart(), event.getEnd(), ctx);
							DB.update(linkedEntity, page.getAction(), ctx);
						}
					}
				}
			}
		}
	}

	public <E extends Entity> boolean checkWizardStep(ActionPage<E> page, ApplicationUser user, String currentStep, String nextStep) {
		DbConnection connection = null;

		try {
			connection = DB.createDbConnection();
			Context context = new Context(connection, user);
			E bean = page.getBean();
			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.$_getName());
			boolean allowed = domainLogic.internalUiWizardCheckStep(bean, page.getAction(), currentStep, nextStep, context);
			page.getMessages().addAll(context.getMessages());
			page.setLastActionSuccess(true);
			return allowed;
		} finally {

			if (null != connection) {
				connection.close();
			}
		}
	}

	public <E extends Entity> void prepareUiAccess(Page<E> page, ApplicationUser user, List<String> groups, List<String> tabPanels,
			List<String> tabs) {

		DbConnection connection = null;

		try {
			connection = DB.createDbConnection();
			Context context = new Context(connection, user);
			E bean = page.getBean();
			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(bean.$_getName());

			for (String tabId : tabs) {
				boolean visible = domainLogic.internalUiTabIsVisible(page, bean, tabId, context);
				UiAccess access = new UiAccess(tabId, visible, false, null, false);
				page.getAccess().put(tabId, access);
			}

			for (String groupId : groups) {
				prepareUiAccess(page, domainLogic, context, groupId);
			}

			for (String id : tabPanels) {
				prepareUiAccess(page, domainLogic, context, id);

				if (null == page.getTabsSelectedMap().get(id)) {
					String tabToOpen = domainLogic.internalUiTabToOpen(page, bean, id, context);
					page.getTabsSelectedMap().put(id, tabToOpen);
				}
			}
			
			page.setLastActionSuccess(true);

		} finally {

			if (null != connection) {
				connection.close();
			}
		}
	}

	private <E extends Entity> void prepareUiAccess(Page<E> page, DomainLogic<E> domainLogic, Context context, String id) {
		boolean visible = domainLogic.internalUiGroupIsVisible(page, page.getBean(), id, context);
		UiAccess access = new UiAccess(id, visible, false, null, false);
		page.getAccess().put(id, access);
	}

	public Map<String, Integer> generateMenuCounters(Map<String, String[]> menuQueries, ApplicationUser user) {
		Map<String, Integer> menuCounters = new HashMap<String, Integer>();
		for (String menuId : menuQueries.keySet()) {
			String[] queryInfos = menuQueries.get(menuId);
			Context ctx = null;
			try {
				ctx = new Context(DB.createDbConnection(), user);
				DbQuery query = DB.getQuery(ctx, queryInfos[0], queryInfos[1]);
				query.setCount(true);
				DbManager dbManager = DB.createDbManager(ctx, query);
				if (dbManager.next()) {
					menuCounters.put(menuId, dbManager.getInt(1));
				}
				dbManager.close();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				if (ctx != null)
					ctx.close();
			}
		}
		return menuCounters;
	}

	public List<ConnectionObject> getListConnections() {
		List<ConnectionObject> list = new ArrayList<ConnectionObject>();
		list.addAll(ConnectionLogger.getInstance().getOpenedConnections().values());
		Collections.sort(list, new Comparator<ConnectionObject>() {
			@Override
			public int compare(ConnectionObject o1, ConnectionObject o2) {
				return o1.getId() - o2.getId();
			}
		});
		return list;
	}

	public void closeConnection(int id) {
		ConnectionLogger.getInstance().closeConnection(id);
	}

	public <E extends Entity> void loadMoreLinesList(ListPage<E> page, ApplicationUser user) {
		Context ctx = null;
		try {
			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(page.getDomainName());
			ctx = new Context(DB.createDbConnection(), user);

			DbQuery query = DB.getQuery(ctx, page.getDomainName(), page.getQueryName());
			if (page.getMaxRow() > 0) {
				query.setMaxRownum(page.getMaxRow());
			}
			query.setMinRownum(page.getResults().getList().size());
			if (page.getSortByField() != null) {
				// Sort by the desired field in first position in the query
				query.addSortBy(page.getSortByField(), null, page.getSortByDirection(), true);
			}

			domainLogic.internalUiListPrepare(query, page, ctx);

			DbManager dbManager = null;
			try {
				dbManager = DB.createDbManager(ctx, query);
				Results queryResults = dbManager.toResults();
				page.getResults().getList().addAll(queryResults.getList());
				page.setLastActionSuccess(true);
			} catch (TechnicalException e) {
				LOGGER.error("",e);
				page.setLastActionSuccess(false);
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}

		} finally {
			if (ctx != null && ctx.getConnection() != null) {
				ctx.getConnection().close();
			}
		}
	}
	
	
}
