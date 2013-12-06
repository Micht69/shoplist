package fr.logica.security;

import fr.logica.domain.objects.ShopUser;


/**
 * Extensible class to store User specific information. If used, this class must be instantiated by SecurityManager in
 * the getUser() method.
 * 
 * @author bellangerf
 * 
 */
public class User extends ApplicationUser {
	/**
	 * serial UID
	 */
	private static final long serialVersionUID = 1L;

	public final String profile;
	public String eanCode;
	public String eanShelf;

	public User(String login) {
		super(login);
		profile = ShopUser.ValueList.PROFILE.USER;
	}

	public User(ShopUser dbUser) {
		super(dbUser.getLogin());
		profile = dbUser.getProfile();
	}
}

