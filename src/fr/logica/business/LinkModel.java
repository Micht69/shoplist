package fr.logica.business;


public class LinkModel extends KeyModel {

	private static final long serialVersionUID = 475286302295352786L;

	private String entityName;

	private String refEntityName;

	public LinkModel(String linkName) {
		super();
		this.name = linkName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getRefEntityName() {
		return refEntityName;
	}

	public void setRefEntityName(String refEntityName) {
		this.refEntityName = refEntityName;
	}

	public boolean isMandatory() {
		EntityModel mdl = EntityManager.getEntityModel(entityName);
		for (String field : getFields()) {
			if (mdl.getField(field).isMandatory()) {
				return true;
			}
		}
		return false;
	}

	public String getLinkName() {
		return name;
	}

	public void setLinkName(String linkName) {
		this.name = linkName;
	}
}
