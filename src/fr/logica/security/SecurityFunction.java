package fr.logica.security;

import java.io.Serializable;

public class SecurityFunction implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/** Entité */
	private String entite;

	/** Action */
	private Integer action;

	/** Query */
	private String query;

	public String getEntite() {
		return entite;
	}

	public void setEntite(String entite) {
		this.entite = entite;
	}

	public Integer getAction() {
		return action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public String toString() {
		return "SecurityFunction [entite=" + entite + ", action=" + action + ", query=" + query + "]";
	}
}
