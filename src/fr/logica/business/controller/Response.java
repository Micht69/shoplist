package fr.logica.business.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.ui.UiAccess;

public class Response<E extends Entity> implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -3242223539701244408L;

	protected String entityName;
	protected E entity;
	protected Action action;
	protected List<Key> keys;
	protected String pageName;
	protected String queryName;
	protected String title;
	protected Map<String, UiAccess> uiAccess;
	protected Map<String, Object> customData;

	protected String linkName;
	protected Entity linkedEntity;
	protected boolean backRef;

	protected String remEntityName;
	protected Action remAction;
	protected List<Key> remKeys;

	public Response() {

	}

	public Response(Request<E> request) {
		this.entityName = request.getEntityName();
		this.action = request.getAction();
		this.entity = request.getEntity();
		this.keys = request.getKeys();
		this.queryName = request.getQueryName();
		this.linkName = request.getLinkName();
		this.linkedEntity = request.getLinkedEntity();
		this.backRef = request.isBackRef();
	}

	/**
	 * Creates a request for business controller to ask server validation of the current view.
	 * 
	 * @param context The requestContext to put in the request
	 * @return A Request containing view metadata and possible user input.
	 */
	public Request<E> toValidationRequest(RequestContext context) {
		Request<E> request = new Request<E>(entityName, action, keys, queryName, linkName, backRef);
		request.setEntity(entity);
		request.setLinkedEntity(linkedEntity);
		request.setContext(context);
		return request;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public List<Key> getKeys() {
		return keys;
	}

	public void setKeys(List<Key> keys) {
		this.keys = keys;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public Entity getLinkedEntity() {
		return linkedEntity;
	}

	public void setLinkedEntity(Entity linkedEntity) {
		this.linkedEntity = linkedEntity;
	}

	public String getRemEntityName() {
		return remEntityName;
	}

	public void setRemEntityName(String remEntityName) {
		this.remEntityName = remEntityName;
	}

	public Action getRemAction() {
		return remAction;
	}

	public void setRemAction(Action remAction) {
		this.remAction = remAction;
	}

	public List<Key> getRemKeys() {
		return remKeys;
	}

	public void setRemKeys(List<Key> remKeys) {
		this.remKeys = remKeys;
	}

	public E getEntity() {
		return entity;
	}

	public void setEntity(E entity) {
		this.entity = entity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, UiAccess> getUiAccess() {
		return uiAccess;
	}

	public void setUiAccess(Map<String, UiAccess> uiAccess) {
		this.uiAccess = uiAccess;
	}

	public boolean isBackRef() {
		return backRef;
	}

	public void setBackRef(boolean backRef) {
		this.backRef = backRef;
	}

	public Map<String, Object> getCustomData() {
		return customData;
	}

	public void setCustomData(Map<String, Object> customData) {
		this.customData = customData;
	}

}
