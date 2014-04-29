package fr.logica.business.controller;

import java.io.Serializable;
import java.util.List;

import fr.logica.business.Action;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;

public class Request<E extends Entity> implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -3242223539701244408L;

	private String entityName;
	private E entity;

	private Action action;
	private List<Key> keys;
	private String queryName;

	private String linkName;
	private boolean backRef;
	private Entity linkedEntity;

	private RequestContext context;

	public Request() {

	}

	public Request(String entityName, Action action, List<Key> keys, String queryName, String linkName, boolean backRef) {
		this.entityName = entityName;
		this.action = action;
		this.keys = keys;
		this.queryName = queryName;
		this.linkName = linkName;
		this.backRef = backRef;
	}

	public Request(Response<E> response, RequestContext context) {
		this.entityName = response.getEntityName();
		this.action = response.getAction();
		this.keys = response.getKeys();
		this.queryName = response.getQueryName();
		this.linkName = response.getLinkName();
		this.linkedEntity = response.getLinkedEntity();
		this.entity = response.getEntity();
		this.backRef = response.isBackRef();
		this.context = context;
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

	public RequestContext getContext() {
		return context;
	}

	public void setContext(RequestContext context) {
		this.context = context;
	}

	public E getEntity() {
		return entity;
	}

	public void setEntity(E entity) {
		this.entity = entity;
	}

	public boolean isBackRef() {
		return backRef;
	}

	public void setBackRef(boolean backRef) {
		this.backRef = backRef;
	}

}
