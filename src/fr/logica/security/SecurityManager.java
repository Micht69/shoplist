package fr.logica.security;


/**
 * Extensible class to store Security specific behavior. It must implement AbstractSecurityManager. 
 * 
 * @author bellangerf
 * 
 */
public class SecurityManager extends DefaultSecurityManager {
	/**
	 * serial UID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean disableSecurity() {
		return true;
	}
}
