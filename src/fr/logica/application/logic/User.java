package fr.logica.application.logic;

import java.io.Serializable;

import fr.logica.domain.constants.ShopUserConstants;
import fr.logica.domain.objects.ShopUser;
import fr.logica.security.DefaultUser;

/**
 * Extensible class to store User specific information. If used, this class must be instantiated by SecurityManager in
 * the getUser() method.
 * 
 * @author bellangerf
 * 
 */
public class User extends DefaultUser implements Serializable {

	/** serial UID */
	private static final long serialVersionUID = 5435236179352893354L;

	public String profile;
	public String eanCode;
	public String eanShelf;
	public Integer listId;

	public User() {
		super();
		this.profile = ShopUserConstants.ValueList.PROFILE.USER;
	}

	public User(String login) {
		super(login);
		this.profile = ShopUserConstants.ValueList.PROFILE.USER;
	}
	
	public User(ShopUser dbUser) {
		super(dbUser.getLogin());
		this.profile = dbUser.getProfile();
	}
}

