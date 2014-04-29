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
	
	/** Menu */
	private String menu;
	
	/** Menu Option */
	private String menuOption;

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

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public String getMenuOption() {
		return menuOption;
	}

	public void setMenuOption(String menuOption) {
		this.menuOption = menuOption;
	}
}
