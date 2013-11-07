package fr.logica.business;

import java.io.Serializable;

public class LinkModel implements Serializable {

	private static final long serialVersionUID = 475286302295352786L;

	private String entityName;

	private String keyName;

	private String refEntityName;

	private String linkName;

	public LinkModel(String linkName) {
		this.linkName = linkName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getRefEntityName() {
		return refEntityName;
	}

	public void setRefEntityName(String refEntityName) {
		this.refEntityName = refEntityName;
	}

	public boolean isMandatory() {
		EntityModel mdl = EntityManager.getEntityModel(entityName);
		for (String field : mdl.getForeignKeyModel(keyName).getFields()) {
			if (mdl.getField(field).isMandatory()) {
				return true;
			}
		}
		return false;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}
}
