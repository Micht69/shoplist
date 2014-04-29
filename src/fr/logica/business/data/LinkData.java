package fr.logica.business.data;

import java.io.Serializable;

import fr.logica.business.Entity;

public class LinkData implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -8032354830494378725L;

	private String entityName;

	private Entity targetEntity;

	private String targetDescription;

	public LinkData(String entityName, Entity targetEntity, String targetDescription) {
		this.entityName = entityName;
		this.targetEntity = targetEntity;
		this.targetDescription = targetDescription;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Entity getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(Entity targetEntity) {
		this.targetEntity = targetEntity;
	}

	public String getTargetDescription() {
		return targetDescription;
	}

	public void setTargetDescription(String targetDescription) {
		this.targetDescription = targetDescription;
	}
}
