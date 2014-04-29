package fr.logica.jsf.webflow;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.faces.context.FacesContext;

import fr.logica.business.Entity;
import fr.logica.business.controller.Response;
import fr.logica.jsf.controller.SessionController;
import fr.logica.jsf.utils.JSFBeanUtils;

public class View<E extends Entity> extends Response<E> implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -2365831858864815356L;

	private static final String LIST_SUFFIX = "_LIST";

	/** current view ID */
	private String vID;

	private View<?> nextView;

	private boolean dirty;

	private File attachment;
	private String attachmentName;

	private Map<String, Map<String, String>> viewModelsData;

	public View(String cID) {
		vID = UUID.randomUUID().toString();
		SessionController sessionCtrl = (SessionController) JSFBeanUtils.getManagedBean(FacesContext.getCurrentInstance(), "sessionCtrl");
		sessionCtrl.getViewConversations().put(vID, cID);
		viewModelsData = new HashMap<String, Map<String, String>>();
	}

	public View(String cID, Response<E> response) {
		this(cID);
		this.action = response.getAction();
		this.keys = response.getKeys();
		this.entityName = response.getEntityName();
		this.entity = response.getEntity();
		this.queryName = response.getQueryName();
		if (response.getPageName() != null) {
			this.pageName = response.getPageName();
		} else if (response.getAction() != null) {
			this.pageName = response.getAction().getPageName();
		}
		this.linkName = response.getLinkName();
		this.linkedEntity = response.getLinkedEntity();
		this.backRef = response.isBackRef();
		this.remEntityName = response.getRemEntityName();
		this.remAction = response.getRemAction();
		this.remKeys = response.getRemKeys();
		this.title = response.getTitle();
		this.uiAccess = response.getUiAccess();
		this.customData = response.getCustomData();
	}

	public String getURL() {
		Map<String, String> params = new HashMap<String, String>();
		if (vID != null) {
			params.put("vID", vID);
		}
		return getURL(params);
	}

	public String getURLNoParam() {
		return getURL(null);
	}

	/**
	 * Returns this view's URL
	 * 
	 * @param params GET Parameters to add
	 * @return URL of the current view instance + get parameters. null if there's no response data.
	 */
	private String getURL(Map<String, String> params) {
		String baseURL = null;
		// Response has a page name, go to the page
		if (pageName != null) {
			baseURL = "/" + entityName + "/" + pageName;
		} else if (queryName != null) {
			baseURL = "/" + entityName + "/" + queryName + LIST_SUFFIX;
		}

		// Add get parameters
		StringBuilder httpGetParameters = new StringBuilder("?faces-redirect=true");
		if (params != null) {
			for (Entry<String, String> e : params.entrySet()) {
				httpGetParameters.append("&").append(e.getKey()).append("=").append(e.getValue());
			}
		}
		return baseURL + httpGetParameters;
	}

	public View<?> getNextView() {
		return nextView;
	}

	public void setNextView(View<?> nextView) {
		this.nextView = nextView;
	}

	public String getvID() {
		return vID;
	}

	public void setvID(String vID) {
		this.vID = vID;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Map<String, Map<String, String>> getViewModelsData() {
		return viewModelsData;
	}

	public void setViewModelsData(Map<String, Map<String, String>> viewModelsData) {
		this.viewModelsData = viewModelsData;
	}

	public void setAttachment(File file) {
		attachment = file;
	}

	public File getAttachment() {
		return attachment;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

}
