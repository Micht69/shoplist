
package fr.logica.jsf.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fr.logica.application.AbstractApplicationLogic.OpenCriteriaBehavior;
import fr.logica.application.ApplicationUtils;
import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FileContainer;
import fr.logica.business.Key;
import fr.logica.business.Link;
import fr.logica.business.MessageUtils;
import fr.logica.business.Results;
import fr.logica.controller.UiController;
import fr.logica.db.ConnectionObject;
import fr.logica.jsf.components.autocomplete.AutocompleteSuggestion;
import fr.logica.jsf.components.schedule.ScheduleEvent;
import fr.logica.jsf.components.wizard.WizardEvent;
import fr.logica.jsf.model.treetable.TreeTableModel;
import fr.logica.jsf.utils.CriteriaVisitor;
import fr.logica.jsf.utils.GroupVisitor;
import fr.logica.jsf.utils.LabelFunction;
import fr.logica.reflect.DomainUtils;
import fr.logica.security.ApplicationUser;
import fr.logica.ui.ActionPage;
import fr.logica.ui.CustomPage;
import fr.logica.ui.ListPage;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;
import fr.logica.ui.Page;
import fr.logica.ui.UiAccess;
import fr.logica.ui.UiLink;
import fr.logica.ui.UiLink.Type;
import fr.logica.ui.UiManager;

public class JSFController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 2965647296899059513L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(JSFController.class);

	/** Session controller. Holds user relative data and access rights management. */
	private SessionController sessionCtrl;

	/** Main business controller. Defines UI main behavior. */
	private final UiController uiController = new UiController();

	/** Current page to display. Holds all displayed data. */
	private Page<Entity> page;


	private File download;

	private String selectedRowEncodedKey;

	private String launchActionEntity;
	private String launchActionCode;
	private String launchActionType;
	private String launchActionLink;
	private String launchActionList;
	


	private Map<String, Integer> menuCounters = new HashMap<String, Integer>();

	public void reset() {

		page = null;
		sessionCtrl.reset();

		download = null;
		selectedRowEncodedKey = null;
		launchActionEntity = null;
		launchActionCode = null;
		launchActionType = null;
		launchActionLink = null;
		launchActionList = null;
	}

	/**
	 * Prépare une action sur un ensemble d'objets.
	 * 
	 * @param action
	 *            L'action à effecuter.
	 * @param pList
	 *            Les objets sélectionnés.
	 * @return La page à afficher.
	 */
	public String prepareMultiAction(String domainName, int codeAction, int typeAction, List<Key> keyList, String linkName) {
		return prepareMultiAction(domainName, codeAction, typeAction, null, keyList, linkName);
	}

	public String prepareMultiAction(String domainName, int codeAction, int typeAction, String customUrl, List<Key> keyList, String linkName) {
		Action action = new Action(codeAction, typeAction);
		ActionPage<Entity> newActionPage = new ActionPage<Entity>();
		newActionPage.setAction(action);
		newActionPage.setDomainName(domainName);
		newActionPage.setKeyList(keyList);
		newActionPage.setCustomUrl(customUrl);

		if (linkName != null && !"".equals(linkName)) {
			// Event comes from a link page
			newActionPage.setLinkName(linkName);
		}

		if (page == null) {
			page = new CustomPage("index/defaultPage");

		} else if (null != getListPage()) {
			getListPage().setDisplayCriterias(false);
		}

		return goToNextPage(newActionPage, page);

	}

	public String prepareMultiAction(String domainName, int codeAction, int typeAction, String customUrl, ScheduleEvent event, String linkName) {
		List<Key> keyList = new ArrayList<Key>();
		keyList.add(new Key(domainName, event.getId()));
		return prepareMultiAction(domainName, codeAction, typeAction, customUrl, keyList, linkName);
	}

	@SuppressWarnings("unchecked")
	public String goToNextPage(Page<?> newPage, Page<?> currentPage) {
		if (newPage instanceof CustomPage) {
			getPageMessages(((CustomPage) newPage).getMessages());

			sessionCtrl.setPage(newPage);

			return ((CustomPage) newPage).getUrl();
		} else if (newPage instanceof ActionPage<?>) {
			ApplicationUser user = sessionCtrl.getUser();

			Page<?> preparedPage = uiController.prepareAction((ActionPage<?>) newPage, currentPage, user);
			getAttachment(preparedPage);
			if (preparedPage instanceof CustomPage) {
				getPageMessages(((CustomPage) preparedPage).getMessages());
				return ((CustomPage) preparedPage).getUrl();
			} else if (preparedPage instanceof ActionPage<?>) {

				page = (ActionPage<Entity>) preparedPage;
				sessionCtrl.setPage(newPage);

				getPageMessages(page.getMessages());
				return page.getUrl();
			} else {
				return prepareList((ListPage<Entity>) preparedPage);
			}
		} else if (newPage instanceof ListPage<?>) {
			return prepareList((ListPage<Entity>) newPage);
		} else {
			return "";
		}
	}

	public String validate() {
		Page<?> nextPage = uiController.validateAction(getActionPage(), sessionCtrl.getUser());
		getAttachment(nextPage);
		getPageMessages(nextPage.getMessages());
		return goToNextPage(nextPage, null);
	}

	public String validate(Integer subAction) {
		try {
			getActionPage().getAction().subAction = subAction;
			return validate();

		} finally {
			ActionPage<?> actionPage = getActionPage();

			if (null != actionPage) {
				actionPage.getAction().subAction = null;
			}
		}
	}

	public String cancel() {
		return back(1);
	}

	public String back(int steps) {

		Page<?> nextPage = page;

		for (int i = 0; i < steps; i++)
		{
			nextPage = nextPage.getNextPage();
		}
		return goToNextPage(nextPage, null);
	}

	/**
	 * Prépare une action sur un seul objet (ex : création, action par défaut).
	 * 
	 * @param action
	 *            L'action à effectuer
	 * @param pEntity
	 *            L'entité cible.
	 * @return La page à afficher.
	 */
	public String prepareSingleAction(String entityName, int codeAction, int typeAction, Key primaryKey, String linkName) {
		return prepareSingleAction(entityName, codeAction, typeAction, primaryKey, linkName, null);
	}

	/**
	 * Prépare une action sur un seul objet (ex : création, action par défaut).
	 * 
	 * @param entityName
	 *            Nom de l'entité sur laquelle porte l'action.
	 * @param codeAction
	 *            Code de l'action à préparer.
	 * @param typeAction
	 *            Type de l'action à préparer.
	 * @param primaryKey
	 *            Clé primaire de l'entité à manipuler.
	 * @param linkName
	 *            Nom du lien impacté par l'action.
	 * @param queryName
	 *            Nom de la requête à utiliser (pour une action de sélection).
	 * @return La page à afficher.
	 */
	public String prepareSingleAction(String entityName, int codeAction, int typeAction, Key primaryKey, String linkName, String queryName) {
		List<Key> keyList = null;
		if (primaryKey != null) {
			keyList = new ArrayList<Key>();
			keyList.add(primaryKey);
		}
		if (codeAction == Constants.SELECT) {
			return prepareSelect(getActionPage(), linkName, queryName);
		} else if (codeAction == Constants.DETACH) {
			if (isListPage()) {
				return detach(linkName);
			}
			setDirty(true);
		}
		return prepareMultiAction(entityName, codeAction, typeAction, keyList, linkName);
	}

	/**
	 * Prépare une action depuis le menu.
	 * 
	 * @param action
	 *            L'action à effectuer
	 * @param pEntity
	 *            L'entité cible.
	 * @return La page à afficher.
	 */
	public String prepareSingleMenuAction(String entityName, int codeAction, int typeAction) {
		return prepareMultiAction(entityName, codeAction, typeAction, null, null);
	}

	public String prepareSingleMenuAction(String entityName, int codeAction, int typeAction, String customUrl) {
		return prepareMultiAction(entityName, codeAction, typeAction, customUrl, (List<Key>) null, null);
	}

	public void getPageMessages(List<Message> messages) {

		if (null != messages) {
			for (Message msg : messages) {
				if (msg.getSeverity() == Severity.INFO) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg.getMessage(), null));
				} else if (msg.getSeverity() == Severity.ERROR) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, msg.getMessage(), null));
				}
			}
			messages.clear();
		}
	}

	public String prepareDefaultAction(String actionsString) {
		if (actionsString == null || actionsString.trim().length() == 0) {
			return null; 
		}
		String[] actions = actionsString.split(",");
		String entityName = getResults().getEntityName();
		Key primaryKey = new Key(entityName);
		primaryKey.setEncodedValue(selectedRowEncodedKey);
		if (isSelectPage()) {
			List<Key> keys = new ArrayList<Key>();
			keys.add(primaryKey);
			return select(keys);
		}

		String entityDbName = DomainUtils.createDbName(entityName);


		for (String actionCodeString : actions) {
			Integer actionCode = null;
			try {
				actionCode = Integer.parseInt(actionCodeString);
			} catch (NumberFormatException ex) {
				FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid default action : " + actionCodeString, null));
				return null; 
			}
			if (sessionCtrl.isActionRendered(entityDbName, actionCode)) {
				Action a = EntityManager.getEntityModel(entityName).getAction(actionCode);
				return prepareSingleAction(entityName, a.code, a.type, primaryKey, null);
			}
		}
		return null;
	}

	public String prepareLinkAction() {
		Integer codeAction = Integer.parseInt(launchActionCode);
		Integer typeAction = Integer.parseInt(launchActionType);
		Key primaryKey = new Key(launchActionEntity);
		primaryKey.setEncodedValue(selectedRowEncodedKey);

		String entityDbName = DomainUtils.createDbName(launchActionEntity);

		if (!sessionCtrl.isActionRendered(entityDbName, codeAction) || listIsProtected(launchActionList)) {
			for (Action a : EntityManager.getEntityModel(launchActionEntity).getActions()) {
				if (a.type == Constants.DISPLAY && sessionCtrl.isActionRendered(entityDbName, a.code)) {
					return prepareSingleAction(launchActionEntity, a.code, Constants.DISPLAY, primaryKey, launchActionLink);
				}
			}
			// Default action not available and display action neither. Do nothing.
			return null;
		}

		return prepareSingleAction(launchActionEntity, codeAction, typeAction, primaryKey, launchActionLink);
	}

	public String prepareLinkDefaultAction(String entityName, Key primaryKey, String linkName) {
		if (isListPage() || readonly(linkName)) {
			return prepareSingleAction(entityName, Constants.ACTION_DISPLAY, Constants.DISPLAY, primaryKey, linkName);
		} else if (sessionCtrl.isActionRendered(entityName, Constants.MODIFY)) {
			return prepareSingleAction(entityName, Constants.MODIFY, Constants.MODIFY, primaryKey, linkName);
		}
		return prepareSingleAction(entityName, Constants.ACTION_DISPLAY, Constants.DISPLAY, primaryKey, linkName);
	}

	public String getTitle() {
		String title = "";

		if (page != null) {
			title = page.getTitle();
		}

		return ApplicationUtils.getApplicationLogic().getPageTitle(title);
	}

	public void getAttachment(Page<?> page) {
		if (page.getAttachment() == null) {
			return;
		}
		if (page.getAttachment() instanceof File) {
			download = (File) page.getAttachment();
			page.setAttachment(null);
		}
	}

	public String executeDownload() {
		OutputStream out = null;
		try {
			File downloadFile = download;
			FacesContext fc = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

			response.setContentLength((int) downloadFile.length());
			response.setContentType("application/zip");
			response.addHeader("Content-Disposition", "attachment; filename=\"" + downloadFile.getName() + "\"");
			response.addHeader("Cache-Control", "public");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFile.getName() + "\"");
			response.setHeader("Cache-Control", "public");

			FileInputStream stream = new FileInputStream(downloadFile);
			out = response.getOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = stream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			stream.close();

			response.getOutputStream().flush();
			response.getOutputStream().close();
			fc.responseComplete();
			download = null;
			return null;
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
		return null;
	}

	public String downloadFile(String propertyName) {
		OutputStream out = null;
		
		FileContainer file = uiController.getFile(page.getBean(), page, sessionCtrl.getUser(), propertyName);

		if (null != file && null != file.getContent()) {
			try {
				FacesContext fc = FacesContext.getCurrentInstance();
				HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

				response.setContentLength(file.getContent().length);
				response.setContentType(file.getContentType());
				String fileName = file.getRealName();
				if (fileName == null) {
					// FIXME : Handle mimetype
					fileName = propertyName+".png";
				}
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				response.addHeader("Cache-Control", "public");

				out = response.getOutputStream();
				out.write(file.getContent());
				out.flush();
				out.close();
				fc.responseComplete();
				return null;
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getMessage(), e);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
		return null;
	}

	public String deleteFile(String propertyName) {
		
		FileContainer container = (FileContainer) page.getBean().invokeGetter(propertyName + "Container");
		container.setContent(null);
		container.setNull(true);
		return null;
	}

	public String getCustomData() {
		StringBuilder cData = new StringBuilder();
		if (isActionPage()) {
			for (String key : getActionPage().getCustomData().keySet()) {
				cData.append("<div id=\"cData_key_");
				cData.append(key);
				cData.append("\">");
				cData.append(String.valueOf(getActionPage().getCustomData().get(key)));
				cData.append("</div>");
			}
		}
		return cData.toString();
	}

	public boolean readonly(String field) {
		if (getPage().getAccess() == null || getPage().getAccess().get(field) == null) {
			return false;
		}
		return getPage().getAccess().get(field).readOnly;
	}

	public boolean isVisible(String field) {
		if (getPage().getAccess() == null || getPage().getAccess().get(field) == null) {
			return true;
		}
		return getPage().getAccess().get(field).visible;
	}

	public boolean isMandatory(String field) {
		if (getActionPage() == null || getActionPage().getAccess().get(field) == null) {
			return false;
		}
		return getActionPage().getAccess().get(field).mandatory;
	}

	public String getLabel(String field, String key) {
		Page<?> page = getPage();

		if (page == null || page.getAccess() == null) {
			return LabelFunction.label("genLabels", key);
		}
		UiAccess access = page.getAccess().get(field);

		if (access == null || access.label == null) {
			return LabelFunction.label("genLabels", key);
		}
		return access.label;
	}

	public String getColumnLabel(String label, String key) {

		if (null == label || label.isEmpty()) {
			return LabelFunction.label("genLabels", key);
		}
		return label;
	}

	public boolean isValid(String field) {
		return !FacesContext.getCurrentInstance().getMessages(field).hasNext();
	}

	public boolean isNoValidateButton() {
		ActionPage<?> page = getActionPage();
		if (page == null || page.getBean() == null || page.getBean().getModel() == null || page.getAction() == null) {
			return false;
		}
		EntityModel model = page.getBean().getModel();
		Action action = page.getAction();
		return action.type == Constants.DISPLAY || model.getAction(action.code).hasSubActions;
	}

	public Map<String, String> getTabsSelectedMap() {
		return getActionPage().getTabsSelectedMap();
	}

	public boolean listIsReadOnly(String key) {
		if (isListPage()) {
			return false;
		}
		if (getActionPage().getAction().type == Constants.CREATE) {
			return true;
		}
		if (getActionPage().getAccess().get(key) == null) {
			return false;
		}
		return getActionPage().getAccess().get(key).readOnly;
	}

	public boolean listIsProtected(String key) {
		if (isListPage()) {
			return false;
		}
		if (getActionPage().getAction().type == Constants.CREATE) {
			return true;
		}
		if (getActionPage().getAccess().get(key) == null) {
			return false;
		}
		return getActionPage().getAccess().get(key).isProtected;
	}

	public String prepareCsvExport() {
		return prepareExport("csv");
	}

	public String preparePdfExport() {
		return prepareExport("pdf");
	}

	public String prepareExport(String fileType) {

		if (null != getListPage()) {
			getListPage().setDisplayCriterias(false);
		}
		page = uiController.prepareListExport(getListPage(), sessionCtrl.getUser(), fileType);
		getPageMessages(page);
		if (page.getAttachment() != null && page.getAttachment() instanceof File) {
			setDownload((File) page.getAttachment());
		}
		sessionCtrl.setPage(page);
		return page.getUrl();
	}

	/**
	 * Basic method preparing a list page.
	 * 
	 * @return
	 */
	public String prepareList() {
		getListPage().setDisplayCriterias(false);
		executeQuery(false);
		return page.getUrl();
	}

	/**
	 * 
	 * @param listPage
	 * @return
	 */
	public String prepareList(ListPage<Entity> listPage) {

		this.page = listPage;

		getPageMessages(listPage);
		executeQuery(false);
		return listPage.getUrl();
	}

	public String prepareSelect(ActionPage<Entity> actionPage, String linkName, String queryName) {
		ListPage<?> currentList = getListPage();
		page = new ListPage<Entity>();
		Entity baseBean = null;
		if (actionPage == null) {
			// Select from criteria field
			page.setNextPage(currentList);
			baseBean = currentList.getCriteria();
			currentList.setDisplayCriterias(true);
		} else {
			page.setNextPage(actionPage);
			baseBean = actionPage.getBean();
		}
		Link link = baseBean.getLink(linkName);
		String domainName = null;
		if (baseBean.$_getName().equals(link.getModel().getRefEntityName())) {
			// Multiple selection - Selected elements are the link sources.
			domainName = link.getModel().getEntityName();
			if (EntityManager.getEntityModel(domainName).isAssociative()) {
				String associatedLinkName = EntityManager.getEntityModel(domainName).getAssociatedLink(linkName);
				domainName = EntityManager.getEntityModel(domainName).getLinkModel(associatedLinkName).getRefEntityName();
			}
		} else {
			// Single selection - Selected element will be the link target.
			domainName = link.getModel().getRefEntityName();
		}

		((ListPage<Entity>) page).setSelectPage(true);
		((ListPage<Entity>) page).setSelectLink(linkName);

		((ListPage<Entity>) page).setQueryName(queryName);
		((ListPage<Entity>) page).setCriteria(DomainUtils.newDomain(domainName));
		((ListPage<Entity>) page).getCriteria().removeDefaultValues();
		((ListPage<Entity>) page).setMaxRow(Constants.MAX_ROW);

		executeQuery(true);
		return page.getUrl();
	}

	/**
	 * Prepare a new list to display. This method is called only from menu elements. It resets action stack.
	 * 
	 * @param domainName
	 * @param queryName
	 * @return
	 */
	public String prepareList(String domainName, String queryName, String pageName) {
		// Menu Call Only
		// We can reset action controller.
		page = new ListPage<Entity>();
		((ListPage<Entity>) page).setQueryName(queryName);
		((ListPage<Entity>) page).setPageName(pageName);
		((ListPage<Entity>) page).setCriteria(DomainUtils.newDomain(domainName));
		((ListPage<Entity>) page).getCriteria().removeDefaultValues();
		((ListPage<Entity>) page).setMaxRow(Constants.MAX_ROW);

		executeQuery(true);

		return page.getUrl();
	}

	/**
	 * Private method to prepare a list page.
	 * 
	 * @param initList
	 *            true if initialization of the list is needed (for instance, load linked elements in criteria)
	 */
	private void executeQuery(boolean initList) {

		if (isSelectPage()) {
			page.getNextPage().updateLinks();
		}
		page = uiController.prepareList(getListPage(), sessionCtrl.getUser(), initList);
		getPageMessages(page);
		sessionCtrl.setPage(page);

		if (initList) {
			ListPage<?> listPage = getListPage();
			// Open criteria at list opening ?
			OpenCriteriaBehavior behavior = ApplicationUtils.getApplicationLogic().getOpenCriteriaBehavior();
			if (behavior == OpenCriteriaBehavior.ALWAYS) {
				listPage.setDisplayCriterias(true);
			} else if (behavior == OpenCriteriaBehavior.NEVER) {
				listPage.setDisplayCriterias(false);
			} else {
				if (listPage.getResults().getResultSetCount() > listPage.getMaxRow()) {
					listPage.setDisplayCriterias(true);
				}
			}
		}
	}

	public String detach(String linkName) {
		Entity entity = getListPage().getCriteria();
		Link link = entity.getLink(linkName);
		link.setEncodedValue(null);
		link.updateFromUi(entity);
		entity.setForeignKey(link.getModel().getKeyName(), null);
		executeQuery(false);
		return page.getUrl();
	}

	public String select(List<Key> selectedKeys) {

		Entity baseBean = page.getNextPage().getBean();
		Link link = baseBean.getLink(getListPage().getSelectLink());
		boolean isSelfRefLink = false;
		if (baseBean.$_getName().equals(link.getModel().getRefEntityName()) && baseBean.$_getName().equals(link.getModel().getEntityName())) {
			// Self-referenced entity
			for (UiLink uiLink : link.getTemplates()) {
				if (uiLink.getType() == Type.LINK) {
					isSelfRefLink = true;
				}
			}
		}
		if (!isSelfRefLink && baseBean.$_getName().equals(link.getModel().getRefEntityName())) {
			uiController.validateSelection(baseBean, getListPage().getSelectLink(), selectedKeys, sessionCtrl.getUser());
		} else {
			// Single selection - Selected element will be the link target.
			if (selectedKeys.size() > 1) {
				page.getMessages().add(new Message("You can't select more than one element", Severity.ERROR));
				return prepareList();
			} else {
				updateKey(baseBean, link, selectedKeys.get(0));
			}
		}
		return goToNextPage(page.getNextPage(), null);
	}

	public String getCriteriaDesc() {
		if (getListPage().getCriteria() == null) {
			return "";
		}

		UIComponent c = FacesContext.getCurrentInstance().getViewRoot().findComponent("mainForm:criterias");

		if (null != c) {
			CriteriaVisitor criteriaVisitor = new CriteriaVisitor();
			c.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), criteriaVisitor);
			return criteriaVisitor.getCriterias();
		}
		return "";
	}

	public String getResultCount() {
		if (getListPage().results == null) {
			return MessageUtils.getInstance().getLabel("results.nodata", null);
		}
		if (getListPage().results.getResultCount() > 1) {
			return getListPage().results.getResultCount() + " " + MessageUtils.getInstance().getLabel("liste.result.resultats", null);
		}
		return getListPage().results.getResultCount() + " " + MessageUtils.getInstance().getLabel("liste.result.resultat", null);
	}

	public String getTotalResultCount() {
		if (getListPage().results != null && getListPage().results.getResultSetCount() > getListPage().results.getResultCount()) {
			return " " + MessageUtils.getInstance().getLabel("liste.result.total", new Object[] { getListPage().results.getResultSetCount() });
		}
		return "";
	}

	public Results getResults() {
		return getListPage().results;
	}

	public boolean sortAscBy(String field) {
		return (!getListPage().isSortByDesc() && field.equals(getListPage().getSortByField()));
	}

	public boolean sortDescBy(String field) {
		return (getListPage().isSortByDesc() && field.equals(getListPage().getSortByField()));
	}

	public String sortBy(String field) {
		if (!field.equals(getListPage().getSortByField())) {
			// New sort, will be in ASC mode
			getListPage().setSortByField(field);
			getListPage().setSortByDesc(false);
		} else {
			if (getListPage().isSortByDesc()) {
				// Reset sort (3rd clic on same collumn)
				getListPage().setSortByField(null);
				getListPage().setSortByDesc(false);
			} else {
				// Sort in DESC mode
				getListPage().setSortByDesc(true);
			}
		}
		return prepareList();
	}

	public Map<String, String> getJsFilterMap() {
		return page.getJsFilterMap();
	}

	public void getPageMessages(Page<?> page) {
		for (Message msg : page.getMessages()) {
			if (msg.getSeverity() == Severity.INFO) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg.getMessage(), null));
			} else if (msg.getSeverity() == Severity.ERROR) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, msg.getMessage(), null));
			}
		}
		page.getMessages().clear();
	}

	/* *******************************************************************
	 * Getter / setter JSF
	 */
	public SessionController getSessionCtrl() {
		return sessionCtrl;
	}

	public void setSessionCtrl(SessionController sessionCtrl) {
		this.sessionCtrl = sessionCtrl;
	}

	public void setCriteria(Entity criteria) {
		getListPage().setCriteria(criteria);
	}

	public Entity getCriteria() {
		return getListPage().getCriteria();
	}

	public Integer getMaxRow() {
		return getListPage().getMaxRow();
	}

	public void setMaxRow(Integer maxRow) {
		getListPage().setMaxRow(maxRow);
	}

	public boolean isDirty() {
		return (page != null) ? page.isDirty() : false;
	}

	public void setDirty(boolean dirty) {
		if (page != null)
			page.setDirty(dirty);
	}

	public File getDownload() {
		return download;
	}

	public void setDownload(File download) {
		this.download = download;
	}

	public String getSelectedRowEncodedKey() {
		return selectedRowEncodedKey;
	}

	public void setSelectedRowEncodedKey(String selectedRowEncodedKey) {
		this.selectedRowEncodedKey = selectedRowEncodedKey;
	}

	public String getLaunchActionEntity() {
		return launchActionEntity;
	}

	public void setLaunchActionEntity(String launchActionEntity) {
		this.launchActionEntity = launchActionEntity;
	}

	public String getLaunchActionCode() {
		return launchActionCode;
	}

	public void setLaunchActionCode(String launchActionCode) {
		this.launchActionCode = launchActionCode;
	}

	public String getLaunchActionLink() {
		return launchActionLink;
	}

	public void setLaunchActionLink(String launchActionLink) {
		this.launchActionLink = launchActionLink;
	}

	public String getLaunchActionList() {
		return launchActionList;
	}

	public void setLaunchActionList(String launchActionList) {
		this.launchActionList = launchActionList;
	}

	public String getLaunchActionType() {
		return launchActionType;
	}

	public void setLaunchActionType(String launchActionType) {
		this.launchActionType = launchActionType;
	}

	public Entity getEntity() {
		return page.getBean();
	}

	public boolean isSelect() {
		return isSelectPage();
	}

	public ListPage<Entity> getListPage() {
		if (page instanceof ListPage) {
			return (ListPage) page;
		}
		return null;
	}

	public ActionPage<Entity> getActionPage() {
		if (page instanceof ActionPage) {
			return (ActionPage) page;
		}
		return null;
	}

	public void setPage(Page<Entity> p) {
		page = p;
	}

	/**
	 * This method must remain private to avoir collision with getActionPage() when called from XHTML page
	 */
	private boolean isActionPage() {
		return (page instanceof ActionPage);
	}

	/**
	 * This method must remain private to avoir collision with getActionPage() when called from XHTML page
	 */
	private boolean isListPage() {
		return (page instanceof ListPage);
	}
	
	/**
	 * convenience method to get isActionPage(), isActionPage() must remain private. 
	 */
	public boolean pageIsActionPage() {
		return isActionPage(); 
	}

	private boolean isSelectPage() {
		if (isListPage()) {
			return getListPage().isSelectPage();
		}
		return false;
	}

	private void updateKey(Entity bean, Link link, Key key) {
		Key oldKey = bean.getForeignKey(link.getModel().getKeyName());
		if (oldKey == null || !key.getEncodedValue().equals(oldKey.getEncodedValue())) {
			bean.setForeignKey(link.getModel().getKeyName(), key);
			link.setEncodedValue(key.getEncodedValue());
			if (page.getNextPage() instanceof ActionPage<?>) {
				((ActionPage<?>) page.getNextPage()).setDirty(true);
			}
		}
	}

	public void updateLink(ValueChangeEvent event) {
		String id = event.getComponent().getId();
		id = id.substring(0, id.length() - 4);
		Entity bean = getEntity();
		Link link = bean.getLink(id.substring(id.lastIndexOf('_') + 1));

		if (null != link) {
			Key key = new Key(link.getModel().getRefEntityName(), (String) event.getNewValue());
			updateKey(bean, link, key);
		}
	}

	public List<AutocompleteSuggestion> loadQuickSearchValues(String linkName, String queryName, String criteria) {

		Map<String, String> values = uiController.loadQuickSearchValues(page.getBean(), page, sessionCtrl.getUser(), linkName, queryName,
				criteria);
		List<AutocompleteSuggestion> result = new ArrayList<AutocompleteSuggestion>();

		for (Map.Entry<String, String> e : values.entrySet()) {
			result.add(new AutocompleteSuggestion(e.getValue(), e.getKey()));
		}
		return result;
	}

	public String checkStepFlow(WizardEvent event) {
		String result = event.getNewStep();
		ActionPage<?> page = getActionPage();
		boolean nexStepAllowed = uiController.checkWizardStep(page, sessionCtrl.getUser(), event.getOldStep(), event.getNewStep());

		if (!nexStepAllowed) {
			result = event.getOldStep();
		}
		getPageMessages(page);
		return result;
	}

	public void prepareUiAccess(String rootComponentId) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		UIComponent c = facesContext.getViewRoot().findComponent(rootComponentId);

		if (null != c) {
			GroupVisitor visitor = new GroupVisitor();
			c.visitTree(VisitContext.createVisitContext(facesContext), visitor);
			uiController.prepareUiAccess(getPage(), sessionCtrl.getUser(), visitor.getGroups(), visitor.getTabPanels(), visitor.getTabs());
		}
	}

	public Page<?> getPage() {
		return page;
	}

	public void executeMenuQueries() {
		Map<String, String[]> menuQueries = UiManager.getMenuQueries();
		menuCounters = uiController.generateMenuCounters(menuQueries, sessionCtrl.getUser());
	}

	public int getCountforMenu(String menuId) {
		Integer res = menuCounters.get(menuId);
		return res == null ? 0 : res;
	}

	public String prepareAdminPage() {
		page = new Page<Entity>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Entity getBean() {
				return null;
			}

			@Override
			public String getUrl() {
				return "/index/admin";
			}

			@Override
			public String getDomainName() {
				return null;
			}
		};
		page.setTitle(LabelFunction.label("labels", "adminPage.title"));
		sessionCtrl.setPage(page);
		return page.getUrl();
	}

	public List<ConnectionObject> getListConnections() {
		return uiController.getListConnections();
	}

	public String prepareAdminPageWithClosingConnection(int id) {
		uiController.closeConnection(id);
		return prepareAdminPage();
	}

	private Map<Results, TreeTableModel> trees = new HashMap<Results, TreeTableModel>();

	public TreeTableModel treeTable(Results r) {
		if (trees.get(r) == null) {
			trees.put(r, new TreeTableModel(r));
		}
		return trees.get(r);
	}

	/**
	 * Method called when print button is called.
	 * 
	 * @return null
	 */
	public String print() {
		return null;
	}

	public boolean isSocialPanelEnabled() {
		return ApplicationUtils.getApplicationLogic().enableSocialFeatures();
	}

	public void moreLines(Results results) {
		uiController.loadMoreLinesList(getListPage(), sessionCtrl.getUser());
	}


}

