package fr.logica.jsf.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fr.logica.application.ApplicationUtils;
import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.Entity;
import fr.logica.business.EntityManager;
import fr.logica.business.EntityModel;
import fr.logica.business.FunctionalException;
import fr.logica.business.Key;
import fr.logica.business.MessageUtils;
import fr.logica.business.TechnicalException;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.controller.Request;
import fr.logica.business.controller.Response;
import fr.logica.jsf.listener.BrowserNavigationListener;
import fr.logica.jsf.model.DataModel;
import fr.logica.jsf.model.DataModel.UiTemplate;
import fr.logica.jsf.model.group.GroupModel;
import fr.logica.jsf.model.group.TabPanelModel;
import fr.logica.jsf.model.group.WizardModel;
import fr.logica.jsf.model.list.ListModel;
import fr.logica.jsf.model.var.ClobFileModel;
import fr.logica.jsf.model.var.FileUploadModel;
import fr.logica.jsf.model.var.ImageLinkModel;
import fr.logica.jsf.utils.FacesMessagesUtils;
import fr.logica.jsf.webflow.View;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;
import fr.logica.ui.UiAccess;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ViewController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -5106894104296875661L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(ViewController.class);

	/** View data */
	private View currentView;

	/** View DataModels */
	private Map<String, DataModel> viewModels;
	
	/** Set containing identifier of components with error at loading */
	private Set<String> errorModels;	

	/** Session controller. Holds user relative data and access rights management. */
	private SessionController sessionCtrl;

	/** Current request context */
	private RequestContext context;

	/** Business Controller that process UI requests, gets data, calls business logic, etc. */
	private BusinessController business = new BusinessController();

	/**
	 * This method is called from xhtml views with a preRenderView even. It stacks the new view in the current conversation.
	 * 
	 * @param event PreRendering event
	 */
	public void initializeView(ComponentSystemEvent event) {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (context.getSessionContext().getUser() == null) {
			// No user found in Session, go back to login
			LOGGER.info("User not logged in, back to login");
			fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "/index/login");
			return;
		}
		if (fc.isValidationFailed()) {
			// Validation error, reload current view without modifications
			LOGGER.debug("Validation error, reload current view");
			return;
		}
		if ("true".equals(fc.getExternalContext().getRequestParameterMap().get("javax.faces.partial.ajax"))) {
			// Ajax call, no need to initialize
			LOGGER.debug("Ajax call");
			return;
		}
		String vID = fc.getExternalContext().getRequestParameterMap().get("vID");
		if (vID == null) {
			// Direct access, new View
			LOGGER.info("Direct access on a view URL should be done through viewAccess servlet only. Redirect to default page");
			fc.getApplication().getNavigationHandler().handleNavigation(fc, null, getDefaultPage());
			return;
		}
		String cID = sessionCtrl.getViewConversations().get(vID);
		View<?> v = sessionCtrl.getCurrentView(cID);
		while (!v.getvID().equals(vID)) {
			v = v.getNextView();
			if (v == null) {
				// Expired View - we set a marker in sessionMap before redirect because request messages won't be keep.
				// This marker will be read and removed by the BrowserNavigationListener.
				LOGGER.warn("Client asked for expired view");
				fc.getExternalContext().getSessionMap()
						.put(BrowserNavigationListener.EXPIRED_VIEW_TOKEN, BrowserNavigationListener.EXPIRED_VIEW_TOKEN);
				fc.getApplication().getNavigationHandler().handleNavigation(fc, null, sessionCtrl.getCurrentView(cID).getURL());
				return;
			}
		}
		currentView = v;
		sessionCtrl.setCurrentView(cID, currentView);
		context.getAttributes().putAll(currentView.getCustomData());
		this.viewModels = new HashMap<String, DataModel>();
	}

	/**
	 * Stores view models data inside the last displayed view. This is done before we switch to another view, so we may restore these data when
	 * we'll get back here.
	 * 
	 * @param v The view that was displayed before and that will be displayed again (in a few moment).
	 */
	private void storeViewData(View<?> v) {
		if (v != null && viewModels != null) {
			for (Entry<String, DataModel> e : viewModels.entrySet()) {
				Map<String, String> store = new HashMap<String, String>();
				e.getValue().storeViewData(store);
				v.getViewModelsData().put(e.getKey(), store);
			}
		}
	}

	public DataModel uiImageLink(String entityPath, String entityName, String varName) {
		return getVarModel(UiTemplate.VAR_IMAGE_LINK, entityPath, entityName, varName);
	}

	public DataModel uiFileUpload(String entityPath, String entityName, String varName) {
		return getVarModel(UiTemplate.VAR_FILE_UPLOAD, entityPath, entityName, varName);
	}

	public DataModel uiClob(String entityPath, String entityName, String varName) {
		return getVarModel(UiTemplate.VAR_CLOB, entityPath, entityName, varName);
	}

	private DataModel getVarModel(UiTemplate ui, String entityPath, String entityName, String varName) {
		String uniqueIdentifier = ui + "_" + entityPath + "_" + entityName + "_" + varName;
		Entity e = getEntity(entityPath);
		if (viewModels.get(uniqueIdentifier) == null) {
			Map<String, String> modelStore = (Map<String, String>) currentView.getViewModelsData().get(uniqueIdentifier);
			if (modelStore == null) {
				modelStore = new HashMap<String, String>();
			}
			if (ui == UiTemplate.VAR_IMAGE_LINK) {
				viewModels.put(uniqueIdentifier, new ImageLinkModel(this, modelStore, e, entityName, varName));
			} else if (ui == UiTemplate.VAR_FILE_UPLOAD) {
				viewModels.put(uniqueIdentifier, new FileUploadModel(this, modelStore, e, entityName, varName));
			} else if (ui == UiTemplate.VAR_CLOB) {
				viewModels.put(uniqueIdentifier, new ClobFileModel(this, modelStore, e, entityName, varName));
			} else {
				LOGGER.error("DataModel " + ui + " not supported. ");
			}
		}
		return viewModels.get(uniqueIdentifier);
	}

	public DataModel uiLinkInner(String linkPath, String entityName) {
		return getDataModel(UiTemplate.LINK_INNER, linkPath, entityName, "", "");
	}

	public DataModel uiBackRefInner(String linkPath, String entityName) {
		return getDataModel(UiTemplate.BACK_REF_INNER, linkPath, entityName, "", "");
	}

	public DataModel uiGroupPanel(String entityPath, String entityName, String groupName) {
		return getGroupModel(UiTemplate.GROUP, entityPath, entityName, groupName);
	}

	public DataModel uiTabPanel(String entityPath, String entityName, String groupName) {
		return getGroupModel(UiTemplate.GROUP_TABS, entityPath, entityName, groupName);
	}

	public DataModel uiWizardPanel(String entityPath, String entityName, String groupName) {
		return getGroupModel(UiTemplate.GROUP_WIZARD, entityPath, entityName, groupName);
	}
	
	public DataModel uiList(String queryName) {
		return uiList(queryName, false);
	}

	public DataModel uiList(String queryName, boolean globalSearch) {
		return getDataModel(null, UiTemplate.LIST, null, currentView.getEntityName(), "", queryName, "", globalSearch);
	}

	public DataModel uiTabeditList(String queryName) {
		return uiTabeditList(queryName, false);
	}

	public DataModel uiTabeditList(String queryName, boolean globalSearch) {
		return getDataModel(null, UiTemplate.LIST_TAB_EDIT, null, currentView.getEntityName(), "", queryName, "", globalSearch);
	}

	public DataModel uiListCategory(String queryName) {
		return uiListCategory(queryName, false);
	}

	public DataModel uiListCategory(String queryName, boolean globalSearch) {
		return getDataModel(null, UiTemplate.LIST_CATEGORY, null, currentView.getEntityName(), "", queryName, "", globalSearch);
	}

	public DataModel uiBackRefList(String linkPath, String entityName, String queryName, String filterName) {
		return getDataModel(UiTemplate.BACK_REF_LIST, linkPath, entityName, queryName, filterName);
	}

	public DataModel uiBackRefListCategory(String linkPath, String entityName, String queryName, String filterName) {
		return getDataModel(UiTemplate.BACK_REF_LIST_CATEGORY, linkPath, entityName, queryName, filterName);
	}

	public DataModel uiBackRefSchedule(String linkPath, String entityName, String queryName) {
		// Schedule does not use filterName
		return getDataModel(UiTemplate.BACK_REF_LIST_SCHEDULE, linkPath, entityName, queryName, "");
	}

	public DataModel uiBackRefTabeditList(String linkPath, String entityName, String queryName, String filterName) {
		return getDataModel(UiTemplate.BACK_REF_LIST_TAB_EDIT, linkPath, entityName, queryName, filterName);
	}

	public DataModel uiLink(String linkPath, String entityName, String filterName) {
		// Link don't use QueryName
		return getDataModel(UiTemplate.LINK, linkPath, entityName, "", filterName);
	}

	public DataModel uiBackRef(String linkPath, String entityName, String filterName) {
		// Link don't use QueryName
		return getDataModel(UiTemplate.BACK_REF, linkPath, entityName, "", filterName);
	}

	public DataModel uiLinkQuickSearch(String linkPath, String entityName, String filterName) {
		// QuickSearch don't use QueryName
		return getDataModel(UiTemplate.LINK_QUICK_SEARCH, linkPath, entityName, "", filterName);
	}

	public DataModel uiLinkMultiCombo(String linkPath, String entityName) {
		// Multi combos don't use query name nor filter name
		return getDataModel(UiTemplate.LINK_MULTI_COMBO, linkPath, entityName, "", "");
	}

	public DataModel uiLinkCombo(String linkPath, String entityName, String filterName) {
		// Combos don't use QueryName
		return getDataModel(UiTemplate.LINK_COMBO, linkPath, entityName, "", filterName);
	}
	
	/**
	 * Loads a link combo inside an editable table or gets it from cache
	 * 
	 * @param tabEditPath
	 *            The tabEdit link path that uniquely identifies it
	 * @param tabEditEntity
	 *            The current entity instance modified in editable table
	 * @param linkName
	 *            Link between entity instance and combo elements
	 * @param entityName
	 *            Entity name of the link
	 * @param filterName
	 *            Query to use to filter combobox elements
	 * @return LinkCombo model loaded
	 */
	public DataModel uiTabEditLinkCombo(String tabEditPath, Entity tabEditEntity, String linkName, String entityName, String filterName) {
		// Combos don't use QueryName
		String uniqueIdentifier = UiTemplate.LINK_COMBO + "_" + tabEditPath + "_" + entityName + "_" + linkName + "_" + filterName;
		try {
			if (viewModels.get(uniqueIdentifier) == null) {
				Map<String, String> modelStore = (Map<String, String>) currentView.getViewModelsData().get(uniqueIdentifier);
				if (modelStore == null) {
					modelStore = new HashMap<String, String>();
				}
				modelStore.put(ListModel.GLOBAL_SEARCH, "false");
				DataModel model = DataModel.build(null, UiTemplate.LINK_COMBO, entityName, linkName, "", filterName, tabEditEntity, modelStore,
						this);
				viewModels.put(uniqueIdentifier, model);
				// Get datamodel initialization messages
				displayMessages(context);
			}
		} catch (Exception e) {
			handleModelException(uniqueIdentifier, e);
		}
		return viewModels.get(uniqueIdentifier);
	}

	public DataModel uiCustomModel(Class<?> clazz, String linkPath, String entityName, String queryName, String filterName) {
		return getDataModel(clazz, UiTemplate.CUSTOM, linkPath, entityName, queryName, filterName);
	}

	private DataModel getDataModel(UiTemplate ui, String linkPath, String entityName, String queryName, String filterName) {
		return getDataModel(null, ui, linkPath, entityName, queryName, filterName);
	}

	private DataModel getDataModel(Class<?> clazz, UiTemplate ui, String linkPath, String entityName, String queryName, String filterName) {
		String entityPath = null;
		String linkName = linkPath;
		if (linkPath.contains(".")) {
			linkName = linkPath.substring(linkPath.lastIndexOf(".") + 1);
			entityPath = linkPath.substring(0, linkPath.lastIndexOf("."));
		}
		return getDataModel(clazz, ui, entityPath, entityName, linkName, queryName, filterName, false);
	}

	private DataModel getGroupModel(UiTemplate ui, String entityPath, String entityName, String groupName) {
		String uniqueIdentifier = ui + "_" + entityPath + "_" + entityName + "_" + groupName;
		Entity e = getEntity(entityPath);
		if (viewModels.get(uniqueIdentifier) == null) {
			Map<String, String> modelStore = (Map<String, String>) currentView.getViewModelsData().get(uniqueIdentifier);
			if (modelStore == null) {
				modelStore = new HashMap<String, String>();
			}
			if (ui == UiTemplate.GROUP_TABS) {
				viewModels.put(uniqueIdentifier, new TabPanelModel(this, modelStore, e, entityName, groupName));
			} else if (ui == UiTemplate.GROUP_WIZARD) {
				viewModels.put(uniqueIdentifier, new WizardModel(this, modelStore, e, entityName, groupName));
			} else if (ui == UiTemplate.GROUP) {
				viewModels.put(uniqueIdentifier, new GroupModel(this, modelStore, e, entityName, groupName));
			} else {
				LOGGER.error("DataModel " + ui + " not supported. ");
			}
		}
		return viewModels.get(uniqueIdentifier);
	}

	/**
	 * Returns a fully loaded DataModel component for display. This may be a list template, a link, or any other component. All parameters should
	 * be empty string when they're not relevant. parameters are used to build unique string identifier.
	 * 
	 * @param ui UI Type (List, Link, Back-Reference, Combobox, Planning, etc.)
	 * @param entityPath Path of the template "main entity". In a list page, this will be the criteria, in a default action page, this will be
	 *        the action entity, and in a more complex page with inner templates, this will reference the main entity of the current component's
	 *        direct parent template. <br/>
	 * 
	 * @param entityName Name of component's manipulated entity. In a combobox, the name of the entity type inside the combo. In a backRefList,
	 *        the name of the main entity of the related query.
	 * @param linkName Link used between main entity / source entity (describe by entityPath) and template entity (described by entityName). This
	 *        can be empty in list pages. This linkName may be a list of link names separated by # when component is a "multi-link" template,
	 *        like linkMultiCombo for instance.
	 * @param queryName Query used by the data model. (in backref list for instance)
	 * @param filterName Filter used by data model. (In combobox for instance)
	 * @param globalSearch Is global search activated.
	 * @return Loaded DataModel.
	 */
	private DataModel getDataModel(Class<?> clazz, UiTemplate ui, String entityPath, String entityName, String linkName, String queryName,
			String filterName, boolean globalSearch) {
		String uniqueIdentifier = (clazz != null ? clazz.getSimpleName() : ui) + "_" + entityPath + "_" + entityName + "_" + linkName + "_"
				+ queryName + "_" + filterName;
		try {
			Entity e = getEntity(entityPath);
			if (viewModels.get(uniqueIdentifier) == null) {
				Map<String, String> modelStore = (Map<String, String>) currentView.getViewModelsData().get(uniqueIdentifier);
				if (modelStore == null) {
					modelStore = new HashMap<String, String>();
				}
				modelStore.put(ListModel.GLOBAL_SEARCH, globalSearch ? "true" : "false");
				DataModel model = DataModel.build(clazz, ui, entityName, linkName, queryName, filterName, e, modelStore, this);
				viewModels.put(uniqueIdentifier, model);
				// Get datamodel initialization messages
				displayMessages(context);
			}
		} catch (Exception e) {
			handleModelException(uniqueIdentifier, e);
		}
		return viewModels.get(uniqueIdentifier);
	}

	/**
	 * An error occured on loading component "uniqueIdentifier"<br/>
	 * - If this is the first error at component loading, we log and display an error message. <br/>
	 * - If this is not the first error, we'll log it in debug mode, but we won't display it because it's probable an NPE or something due to the
	 * first error
	 * 
	 * @param uniqueIdentifier
	 *            component identifier loaded when error happened
	 * @param e
	 *            Exception that happened
	 */
	private void handleModelException(String uniqueIdentifier, Exception e) {
		if (errorModels == null) {
			errorModels = new HashSet<String>();
		}
		if (errorModels.contains(uniqueIdentifier)) {
			LOGGER.debug(e.getMessage(), e);
		} else {
			errorModels.add(uniqueIdentifier);
			context.getMessages().add(new Message(FacesMessagesUtils.getTechnicalMessage(e), Severity.ERROR));
			LOGGER.error("Error loading component " + uniqueIdentifier);
			LOGGER.error(e.getMessage(), e);
			displayMessages(e);
		}
	}

	/**
	 * Prepares a view. Direct access on an entity action from the menu.
	 * 
	 * @param entityName Entity on which we launched an action
	 * @param actionCode Action code - unique action identifier
	 * @return URL of the destination view
	 */
	public String prepareView(String entityName, Integer actionCode) {
		Action action = EntityManager.getEntityModel(entityName).getAction(actionCode);
		Request request = new Request(entityName, action, null, null, null, false);
		return prepareView(request);
	}

	/**
	 * Prepares a view. Direct access on a list page from the menu.
	 * 
	 * @param entityName Entity on which we launched an action
	 * @param queryName Query to use to get data
	 * @param pageName Destination page name, usually something like : "QUERY_NAME_LIST" or "QUERY_NAME_EDITABLE_LIST". Suffix will give
	 *        information on list type.
	 * @return URL of the destination view
	 */
	public String prepareView(String entityName, String queryName, String pageName) {
		Action action = Action.getListAction(queryName, pageName);
		Request request = new Request(entityName, action, null, queryName, null, false);
		return prepareView(request);
	}

	/**
	 * Prepares a view. Direct access on a list page from the menu.
	 * 
	 * @param entityName Entity on which we launched an action
	 * @param action Action to use. This action belongs to the entity entityName
	 * @param keyList List of primary keys of entities to process (may be empty or null depending on action type)
	 * @param queryName Query to use to prepare the view (may be null depending on action type)
	 * @param linkName If view has been asked through a link / backref template, this is the name of the link
	 * @param linkedEntity If view has been asked through a link template, this is the entity source entity. If view has been asked through a
	 *        backRef template, this is the target entity.
	 * @param backRef boolean : true if view has been asked through a backRef, false otherwise
	 * @return URL of the destination view
	 */
	public String prepareView(String entityName, Action action, List<Key> keyList, String queryName, String linkName, Entity linkedEntity,
			boolean backRef) {
		// Update linked entities
		for (DataModel dataModel : viewModels.values()) {
			dataModel.validateView(currentView);
		}
		Request request = new Request(entityName, action, keyList, queryName, linkName, backRef);
		request.setLinkedEntity(linkedEntity);
		return prepareView(request);
	}

	/**
	 * Send a request to business controller.
	 * 
	 * @param request The request to send to the business controller.
	 * @return URL to the next view if there's any. null if we need to reload the current page.
	 */
	public String prepareView(Request<?> request) {
		Response<?> response = null;
		Map<String, Object> customData = null;
		if (context == null) {
			context = new RequestContext(sessionCtrl.getContext());
		}
		try {
			try {
				request.setContext(context);
				storeViewData(currentView);
				response = business.process(request);
			} catch (FunctionalException fEx) {
				// Business error - reload page and display error message to user
				context.getMessages().addAll(fEx.getMessages());
			} catch (TechnicalException tEx) {
				context.getMessages().add(new Message(tEx.getMessage(), Severity.ERROR));
				LOGGER.error(tEx.getMessage(), tEx);
			} catch (Exception e) {
				context.getMessages().add(new Message(FacesMessagesUtils.getTechnicalMessage(e), Severity.ERROR));
				LOGGER.error(e.getMessage(), e);
			} finally {
				// Display potential messages from context
				displayMessages(context);
				customData = context.getCustomData();
			}
			if (currentView != null) {
				currentView.setAttachment(context.getAttachment());
				currentView.setAttachmentName(context.getAttachmentName());
			}
			if (response == null) {
				// Refresh current view
				if (currentView == null) {
					return getDefaultPage();
				}
				business.loadUi(currentView, context);
				currentView.setCustomData(context.getCustomData());
				return currentView.getURL();
			} else {
				response.setCustomData(customData);
				return goToNextView(response);
			}
		} finally {
			// Close request context potential database connection
			context.close();
		}
	}

	/**
	 * Redirects the user to prepared next view
	 * 
	 * @param response Fully prepared response to send to the user. We may add attachments to the view if there are any. 
	 * @return Next view URL
	 */
	public String goToNextView(Response response) {
		// We go to next view following response parameters
		View v = sessionCtrl.getCurrentView(conversation);
		View newView = new View(conversation, response);
		newView.setNextView(v);
		newView.setAttachment(context.getAttachment());
		newView.setAttachmentName(context.getAttachmentName());
		sessionCtrl.setCurrentView(conversation, newView);
		return newView.getURL();
	}

	public String cancel() {
		View v = sessionCtrl.getCurrentView(conversation);
		if (v.getNextView() == null) {
			return getDefaultPage();
		}
		return v.getNextView().getURL();
	}

	public String validate() {
		return validate(null);
	}

	public String validate(Integer subActionCode) {
		try {
			Action subAction = null;
			if (subActionCode != null) {
				subAction = currentView.getAction().getSubAction(subActionCode);
				if (subAction == null) {
					LOGGER.error("Sub action " + subActionCode + " does not exist in action " + currentView.getAction().getCode());
				}
			}
			if (context == null) {
				context = new RequestContext(sessionCtrl.getContext());
			}

			for (DataModel dataModel : viewModels.values()) {
				dataModel.validateView(currentView);
			}
			Request request = currentView.toValidationRequest(context);
			if (subAction != null) {
				request.setAction(subAction);
			}
			Response response = business.validate(request);

			// View has been validated, we remove it from top of the stack
			View nextView = currentView.getNextView();
			sessionCtrl.setCurrentView(conversation, nextView);
			if (response == null) {
				if (currentView.getRemKeys() != null) {
					// Remaining actions to launch
					return prepareView(currentView.getRemEntityName(), currentView.getRemAction(),
							currentView.getRemKeys(),
							currentView.getQueryName(), currentView.getLinkName(), currentView.getEntity(), currentView.isBackRef());
				} else if (nextView != null) {
					business.loadUi(nextView, context);
					nextView.setAttachment(context.getAttachment());
					nextView.setAttachmentName(context.getAttachmentName());
					nextView.setCustomData(context.getCustomData());
					return nextView.getURL();
				}
			} else {
				response.setCustomData(context.getCustomData());
				return goToNextView(response);
			}
			return getDefaultPage();
		} catch (FunctionalException fEx) {
			// all(?) msg from functional exception already stored in context
			// will be displayed in finally block
			LOGGER.debug(fEx);
		} catch (TechnicalException tEx) {
			displayMessages(tEx);
			LOGGER.error(tEx.getMessage(), tEx);
		} catch (Exception e) {
			displayMessages(e);
			LOGGER.fatal(e.getMessage(), e);
		} finally {
			if (context != null) {
				context.close();
			}
			// Display potential messages from context
			displayMessages(context);
			// currentView is null if reset has been called into getDefaultPage or somewhere else.
			if (currentView != null) {
				// Merge customData map
				Map<String, Object> mergedCustomData = new HashMap<String, Object>();
				if (currentView.getCustomData() != null) {
					// These custom data come from previous currentView, they must be kept in case of exception
					mergedCustomData.putAll(currentView.getCustomData());
				}
				// These custom data come from normal processing, they may override existing custom data
				mergedCustomData.putAll(context.getCustomData());
				currentView.setCustomData(mergedCustomData);
			}
		}
		return currentView.getURL();
	}

	public Entity getCriteria() {
		return currentView.getEntity();
	}

	public String getTitle() {
		String title = null;
		if (currentView != null) {
			title = currentView.getTitle();
		}
		return ApplicationUtils.getApplicationLogic().getPageTitle(title);
	}

	public boolean isVisible(String field) {
		if (currentView != null && currentView.getUiAccess() != null) {
			if (currentView.getUiAccess().get(field) != null) {
				return ((Map<String, UiAccess>) currentView.getUiAccess()).get(field).visible;
			}
		}
		return true;
	}

	public boolean readonly(String linkPath, String entityName, String varName) {
		String field = entityName + "." + varName;
		if (!"".equals(linkPath)) {
			// check if it's inner link or inner back ref
			String lastLink = linkPath;
			if (linkPath.contains(".")) {
				lastLink = linkPath.substring(linkPath.lastIndexOf("."), linkPath.length());
			}
			DataModel dataModel = null;
			if (EntityManager.getEntityModel(entityName).getLinkModel(lastLink) != null) {
				dataModel = uiBackRefInner(linkPath, entityName);
			} else {
				dataModel = uiLinkInner(linkPath, entityName);
			}
			if (dataModel.isReadonly()) {
				return true;
			}
			field = linkPath + "." + field;
		}
		return readonly(field);
	}

	public boolean readonly(String field) {
		if (currentView != null && currentView.getUiAccess() != null) {
			if (currentView.getUiAccess().get(field) != null) {
				return ((Map<String, UiAccess>) currentView.getUiAccess()).get(field).readOnly;
			}
		}
		return false;
	}

	public boolean isMandatory(String field) {
		if (currentView != null && currentView.getUiAccess() != null) {
			if (currentView.getUiAccess().get(field) != null) {
				return ((Map<String, UiAccess>) currentView.getUiAccess()).get(field).mandatory;
			}
		}
		return false;
	}

	/**
	 * Gets current label to display for a variable with defined values (in combobox for instance)
	 * 
	 * @param e
	 *            Entity containing the field
	 * @param fieldName
	 *            Field name
	 * @param currentValue
	 *            Current value
	 * @return Label associated to current value on entity e for field fieldName
	 */
	public String getDefinedLabel(Entity e, String fieldName, Object currentValue) {
		return e.getModel().getField(fieldName).getDefinedLabel(currentValue, sessionCtrl.getContext().getLocale());
	}

	public String getLabel(String field, String key) {
		if (currentView != null && currentView.getUiAccess() != null) {
			if (currentView.getUiAccess().get(field) != null && ((Map<String, UiAccess>) currentView.getUiAccess()).get(field).label != null) {
				return ((Map<String, UiAccess>) currentView.getUiAccess()).get(field).label;
			}
		}
		return getXhtmlLabel("genLabels", key);
	}

	/**
	 * Gets a label from MessageUtils bundles based on user's current Locale
	 * 
	 * @param bundle
	 *            Bundle where to get the text
	 * @param key
	 *            Label's unique identifier
	 * @return Label from bundle for specified key
	 */
	public String getXhtmlLabel(String bundle, String key) {
		return MessageUtils.getInstance(sessionCtrl.getContext().getLocale()).getXhtmlLabel(bundle, key);
	}

	/**
	 * Gets enumeration labels for a specific enum in an entity in the current session Locale
	 * 
	 * @param e
	 *            Entity containing the enumeration
	 * @param enumName
	 *            enumeration identifier
	 * @return Map containing labels to display inside a combobox / radiobutton and associated values
	 */
	public Map<String, Object> enumValues(Entity e, String enumName) {
		return e.getModel().enumValues(enumName, sessionCtrl.getContext().getLocale());
	}

	/**
	 * Returns true if current view has sub actions to display
	 * 
	 * @return true if currentView has subactions, false otherwise
	 */
	public boolean displaySubActions() {
		return (currentView != null && currentView.getAction() != null && currentView.getAction().hasSubActions());
	}

	/**
	 * Returns true if current view is based on the action actionCode
	 * 
	 * @param actionCode Unique code of the action
	 * @return true if the current action is actionCode
	 */
	public boolean displaySubActions(Integer actionCode) {
		return (currentView != null && currentView.getAction() != null && currentView.getAction().getCode().equals(actionCode));
	}

	/**
	 * Resets metadata on menu click to start a new Conversation.
	 */
	public void reset() {
		sessionCtrl.setCurrentView(conversation, null);
		currentView = null;
	}

	/**
	 * Current view is a selection action
	 * 
	 * @return returns true if the current view is a selection action
	 */
	public boolean isSelect() {
		if (currentView != null) {
			Action a = currentView.getAction();
			return (a != null && a.getCode().intValue() == Constants.SELECT);
		}
		return false;
	}

	public void displayMessages(FunctionalException fEx) {
		FacesMessagesUtils.displayMessages(fEx);
	}

	public void displayMessages(Exception e) {
		FacesMessagesUtils.displayMessages(e);
	}

	public void displayMessages(RequestContext context) {
		FacesMessagesUtils.displayMessages(context);
	}

	public void displayMessages() {
		displayMessages(this.context);
	}

	public boolean isValid(String field) {
		return !FacesContext.getCurrentInstance().getMessages(field).hasNext();
	}

	/**
	 * Returns custom data to be included in pages as {@code div} tag:<br>
	 * {@code <div id=key>value</div>}
	 * 
	 * @return HTML string for custom data include
	 */
	public String getCustomData() {
		String cData = "";
		if (currentView != null && currentView.getCustomData() != null) {
			// get custom data from the response
			cData = buildCustomData(currentView.getCustomData().entrySet());
		}
		if (cData.isEmpty() && context != null) {
			// if no data, try the request
			cData = buildCustomData(context.getCustomData().entrySet());
		}
		return cData;
	}

	/**
	 * @param customData
	 * @return HTML string from custom data
	 */
	private String buildCustomData(Set<Entry<String, Object>> customData) {
		StringBuilder cData = new StringBuilder();
		for (Entry<String, Object> entry : customData) {
			cData.append("<div id=\"");
			cData.append(entry.getKey());
			cData.append("\">");
			cData.append(String.valueOf(entry.getValue()));
			cData.append("</div>");
		}
		return cData.toString();
	}

	public Entity getEntity(String entityPath) {
		if (entityPath == null || "".equals(entityPath)) {
			return getEntity();
		}
		Entity e = getEntity();
		for (String linkName : entityPath.split("\\.")) {
			if (e.getModel().getLinkNames().contains(linkName)) {
				e = e.getLink(linkName).getEntity();
			} else if (e.getModel().getBackRefNames().contains(linkName)) {
				e = e.getBackRef(linkName).getEntity();
			}
		}
		return e;
	}

	public void executeDownload() {
		FileInputStream stream = null;
		OutputStream out = null;
		try {
			File downloadFile = currentView.getAttachment();
			String filename = currentView.getAttachmentName();
			if (filename == null) {
				filename = downloadFile.getName();
			}
			FacesContext fc = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

			response.setContentLength((int) downloadFile.length());
			response.setContentType("application/zip");
			response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.addHeader("Cache-Control", "public");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setHeader("Cache-Control", "public");

			stream = new FileInputStream(downloadFile);
			out = response.getOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = stream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			stream.close();
			out.flush();
			out.close();
			stream = null;
			out = null;

			fc.responseComplete();
			currentView.setAttachment(null);
			currentView.setAttachmentName(null);
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	public File getDownload() {
		if (currentView != null) {
			return currentView.getAttachment();
		}
		return null;
	}

	public RequestContext getContext() {
		return context;
	}

	public void setContext(RequestContext context) {
		this.context = context;
	}

	public Entity getEntity() {
		if (currentView == null) {
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.getExternalContext().getSessionMap().put(BrowserNavigationListener.EXPIRED_VIEW_TOKEN, BrowserNavigationListener.EXPIRED_VIEW_TOKEN);
			throw new ViewExpiredException();
		}
		return currentView.getEntity();
	}

	public void setEntity(Entity entity) {
		currentView.setEntity(entity);
	}

	public SessionController getSessionCtrl() {
		return sessionCtrl;
	}

	public void setSessionCtrl(SessionController sessionCtrl) {
		this.sessionCtrl = sessionCtrl;
	}

	public void setDirty(boolean dirty) {
		if (currentView == null) {
			return;
		}
		currentView.setDirty(dirty);
	}

	public boolean isDirty() {
		if (currentView == null) {
			return false;
		}
		return currentView.isDirty();
	}

	public String getViewInformation() {
		if (currentView != null) {
			return currentView.toString();
		}
		return MessageUtils.getInstance(context).getMessage("error.noCurrentView", (Object[]) null);
	}

	public View getCurrentView() {
		return currentView;
	}

	public String back(int steps) {
		View nextView = currentView;
		for (int i = 0; i < steps; i++) {
			nextView = nextView.getNextView();
		}
		return nextView.getURL();
	}

	private String conversation;
	public boolean newConversation = false;

	/**
	 * Handle conversations with window.name in browser requests. We store different conversation stacks in session controller.
	 */
	public void attachToConversation() {
		if (conversation == null && currentView != null) {
			String oldConversationId = sessionCtrl.getViewConversations().get(currentView.getvID());
			if (oldConversationId != null) {
				LOGGER.debug("View openend via new tab, view stack may be broken.");
			}
			// Put in new conversation
			conversation = sessionCtrl.getNewConversationId();
			sessionCtrl.getViewConversations().put(currentView.getvID(), conversation);
			sessionCtrl.setCurrentView(conversation, sessionCtrl.getCurrentView(oldConversationId));
			sessionCtrl.setCurrentView(oldConversationId, null);
			newConversation = true;
		}
	}

	public String getDefaultPage() {
		return ApplicationUtils.getApplicationLogic().getDefaultPage(sessionCtrl.getContext().getUser());
	}
	
	public String getHelp() {
		return sessionCtrl.getHelp(conversation);
	}

	public String getConversation() {
		return conversation;
	}

	public void setConversation(String conversation) {
		this.conversation = conversation;
	}

	public boolean isNewConversation() {
		return newConversation;
	}

	public void setNewConversation(boolean newConversation) {
		this.newConversation = newConversation;
	}

	protected Collection<DataModel> getDataModels() {
		return viewModels.values();
	}

	/**
	 * Builds a permalink towards the current view
	 * 
	 * @return A permalink towards the current view like this : <br/>
	 *         protocol://server:port/app/directAccessServlet?entityName=...&actionCode=...&queryName=...&encodedKeyList=...
	 */
	public String getPermalink() {
		ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
		String protocol = ext.getRequestScheme();
		String server = ext.getRequestServerName();
		String port = String.valueOf(ext.getRequestServerPort());
		String context = ext.getRequestContextPath();
		String servlet = "/index/viewAccess.jsf";

		// Get base url
		String baseUrl = protocol + "://" + server + ":" + port + context;
		if (currentView == null) {
			return baseUrl + getDefaultPage() + ".jsf";
		}
		baseUrl = baseUrl + servlet;

		// Get url parameters
		Map<String, String> urlParams = new HashMap<String, String>(3);
		urlParams.put("entityName", currentView.getEntityName());
		if (currentView.getAction().getQueryName() != null) {
			urlParams.put("queryName", currentView.getAction().getQueryName());
		} else {
			urlParams.put("actionCode", currentView.getAction().getCode().toString());
			if (currentView.getKeys() != null) {
				StringBuilder keys = new StringBuilder();
				for (Key k : (List<Key>) currentView.getKeys()) {
					if (keys.length() > 0) {
						keys.append("|||");
					}
					keys.append(k.getEncodedValue());
				}
				if (keys.length() > 0) {
					urlParams.put("encodedKeyList", keys.toString());
				}
			}
		}

		return ApplicationUtils.getApplicationLogic().getPermaLink(baseUrl, urlParams);
	}

	/**
	 * Utility method to access an entity model
	 * 
	 * @param entityName
	 *            Entity name
	 * @return Entity Model of entity entityName
	 */
	public EntityModel getEntityModel(String entityName) {
		return EntityManager.getEntityModel(entityName);
	}

}
