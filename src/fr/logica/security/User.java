package fr.logica.security;


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

	public User(String login) {
		super(login);
	}
}

