package fr.logica.business.data;

import java.io.Serializable;

import fr.logica.business.Entity;

public class BackRefData implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -8032354830494378725L;

	private String entityName;

	private Entity sourceEntity;

	private String description;

	public BackRefData(String entityName, Entity sourceEntity, String description) {
		this.entityName = entityName;
		this.sourceEntity = sourceEntity;
		this.description = description;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Entity getSourceEntity() {
		return sourceEntity;
	}

	public void setSourceEntity(Entity sourceEntity) {
		this.sourceEntity = sourceEntity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
