package fr.logica.security;

import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import fr.logica.business.TechnicalException;

public class SecurityUtils {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class);

	private SecurityUtils() {
		// Do not instantiate this.
	}

	private static AbstractSecurityManager sm;

	public static synchronized AbstractSecurityManager getSecurityManager() {
		if (sm == null) {
			try {
				sm = (AbstractSecurityManager) Class.forName("fr.logica.application.logic.SecurityManager").newInstance();
			} catch (InstantiationException e) {
				LOGGER.error("Error", e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Error", e);
			} catch (ClassNotFoundException e) {
				LOGGER.info("SecurityManager not found");
			}
			if (sm == null) {
				sm = new DefaultSecurityManager();
			}
		}
		return sm;
	}

	private static byte[] computeHash(String x, String algo) {
		try {
			java.security.MessageDigest d = null;
			d = java.security.MessageDigest.getInstance(algo);
			d.reset();
			d.update(x.getBytes());
			return d.digest();
		} catch (NoSuchAlgorithmException ex) {
			throw new TechnicalException("Impossible to instantiate " + algo + " Algorithm");
		}
	}

	private static String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0xff) < 0x10) {
				sb.append("0");
			}
			sb.append(Long.toString(bytes[i] & 0xff, 16));
		}
		return sb.toString();
	}

	public static String hash(String password) {
		return byteArrayToHexString(computeHash(password, "SHA-1"));
	}
	
	public static String hashMD5(String password) {
		return byteArrayToHexString(computeHash(password, "MD5"));
	}

}
