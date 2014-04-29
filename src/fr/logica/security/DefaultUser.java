package fr.logica.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extensible class to store User specific information. If used, this class must be instantiated by SecurityManager in
 * the getUser() method.
 * 
 * @author bellangerf
 * 
 */
public class DefaultUser implements Serializable {

	/** serial UID */
	private static final long serialVersionUID = 5435236179352893354L;

	/** Access rights on lists **/
	protected Set<String> lists;

	/** Access rights on Actions **/
	protected Map<String, Set<Integer>> actions;
	
	/** Access rights on Menus **/
	protected Set<String> menus;
	
	/** Access rights on Menu Options **/
	protected Set<String> menusOptions;

	protected String login;
	protected HashMap<String, Object> userData = new HashMap<String, Object>();

	public DefaultUser() {
		this.lists = new HashSet<String>();
		this.actions = new HashMap<String, Set<Integer>>();
		this.menus = new HashSet<String>();
		this.menusOptions = new HashSet<String>();
	}

	public DefaultUser(String login) {
		this();
		this.login = login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getLogin() {
		return login;
	}

	public HashMap<String, Object> getUserData() {
		return userData;
	}

	public void setUserData(HashMap<String, Object> userData) {
		this.userData = userData;
	}

	public Map<String, Set<Integer>> getActions(){
		return actions;
	}
	
	public Set<String> getLists(){
		 return lists;
	}

	public Set<String> getMenusOptions() {
		return menusOptions;
	}

	public Set<String> getMenus() {
		return menus;
	}
}
