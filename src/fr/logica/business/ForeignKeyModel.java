package fr.logica.business;

public class ForeignKeyModel extends KeyModel {
	/** */
	private static final long serialVersionUID = 2947242701105957372L;

	private String refEntityName;

	public KeyModel getRefKey() {
		return new KeyModel(refEntityName);
	}

	public String getRefEntityName() {
		return refEntityName;
	}

	public void setRefEntityName(String refEntityName) {
		this.refEntityName = refEntityName;
	}

}
