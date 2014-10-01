package fr.logica.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KeyModel implements Serializable {
	/**
	 * Serializable
	 */
	private static final long serialVersionUID = 1594216317013783414L;

	protected String name;

	private boolean isUnique;

	private List<String> fields = new ArrayList<String>();

	public KeyModel() {

	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public KeyModel(String entityName) {
		KeyModel m = EntityManager.getEntityModel(entityName).getKeyModel();
		name = m.getName();
		fields = m.getFields();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	@Override
	public int hashCode() {
		return ((name == null) ? 0 : name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof KeyModel)) {
			return false;
		}
		KeyModel other = (KeyModel) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
