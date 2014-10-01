package fr.logica.security;

import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import fr.logica.application.logic.SecurityManager;
import fr.logica.business.TechnicalException;

public class SecurityUtils {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class);

	/** Private constructor for utility class */
	private SecurityUtils() {
		// Do not instantiate this.
	}

	/** SecurityManager singleton */
	private static AbstractSecurityManager sm;

	/** Gets security manager current instance */ 
	public static synchronized AbstractSecurityManager getSecurityManager() {
		if (sm == null) {
			LOGGER.debug("initialize SecurityManager"); 
			sm = new SecurityManager();
		}
		return sm;
	}

	/** 
	 * Computes Hash with SHA-1 algorithm
	 * @param x	String to hash
	 * @return	SHA-1 hash of x
	 */
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

	/**
	 * Converts a byte array into string
	 * @param bytes	to convert	
	 * @return String representing bytes
	 */
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

	/**
	 * Hashes a password using SHA-1 algorithm
	 * @param password	Password to hash
	 * @return	Hashed password
	 */
	public static String hash(String password) {
		return byteArrayToHexString(computeHash(password, "SHA-1"));
	}
	
	public static String hashMD5(String password) {
		return byteArrayToHexString(computeHash(password, "MD5"));
	}

}
