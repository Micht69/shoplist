package fr.logica.business;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fr.logica.security.ApplicationUser;

public final class ConcurrencyManager {
	/** Instance du manager */
	private static final ConcurrencyManager _instance = new ConcurrencyManager();
	/** Dur�e maximale du lock (en minutes) */
	private static final int LOCK_MAX_TIME = 5;

	/** La table des locks */
	private final HashMap<ConcurrencyManager.CmKey, ConcurrencyManager.CmToken> concurrencyMap;

	/**
	 * Constructeur
	 */
	public ConcurrencyManager() {
		concurrencyMap = new HashMap<ConcurrencyManager.CmKey, ConcurrencyManager.CmToken>();
	}

	/**
	 * R�cup�ration de l'instance courante.
	 * 
	 * @return le ConcurrencyManager courant.
	 */
	public static synchronized ConcurrencyManager getInstance() {
		return _instance;
	}

	/**
	 * R�cup�ration d'une valeur dans les donn�es de concurrence
	 * 
	 * @param key
	 *            la cl� de recherche
	 * @return la valeur du token ou null
	 */
	private synchronized CmToken getFromConcurrencyMap(CmKey key) {
		return this.concurrencyMap.get(key);
	}

	/**
	 * Insertion d'une valeur dans les donn�es de concurrence
	 * 
	 * @param key
	 *            la cl� de recherche
	 * @param token
	 *            la valeur du token
	 */
	private synchronized void putInConcurrencyMap(CmKey key, CmToken token) {
		this.concurrencyMap.put(key, token);
	}

	/**
	 * Insertion d'une valeur dans les donn�es de concurrence
	 * 
	 * @param key
	 *            la cl� de recherche
	 * @param token
	 *            la valeur du token
	 */
	private synchronized void removeFromConcurrencyMap(CmKey key) {
		this.concurrencyMap.remove(key);
	}

	/**
	 * Ajout d'un lock sur l'entit� pass�e pour le user pass�.
	 * 
	 * @param entityName
	 *            le nom de l'entit�
	 * @param actionCode
	 *            le code de l'action
	 * @param entityKey
	 *            la cl� de l'entit�
	 * @param user
	 *            le user connect�
	 */
	public void lockEntity(String entityName, int actionCode, Key entityKey, ApplicationUser user) {
		putInConcurrencyMap(new CmKey(entityName, entityKey), new CmToken(user, actionCode));
	}

	/**
	 * Suppression du lock.
	 * 
	 * @param entityName
	 *            le nom de l'entit�
	 * @param actionCode
	 *            le code de l'action
	 * @param entityKey
	 *            la cl� de l'entit�
	 */
	public void unlockEntity(String entityName, int actionCode, Key entityKey) {
		CmToken token = getFromConcurrencyMap(new CmKey(entityName, entityKey));
		if (token != null) {
			if (token.actionCode == actionCode) {
				// C'est la bonne action
				removeFromConcurrencyMap(new CmKey(entityName, entityKey));
			}
		}
	}

	/**
	 * V�rifie si l'entit� est lock�e par un <b>autre</b> user.
	 * 
	 * @param entityName
	 *            le nom de l'entit�
	 * @param entityKey
	 *            la cl� de l'entit�
	 * @param user
	 *            le user connect�
	 * @return le NNI du user ayant lock� l'entit�.
	 */
	public String getEntityLockUser(String entityName, Key entityKey, ApplicationUser user) {
		CmToken token = getFromConcurrencyMap(new CmKey(entityName, entityKey));
		if (token == null) {
			// Pas de lock
			return null;
		} else if (token.userNni.equals(user.getLogin()) || !token.isStillValid()) {
			// Lock appartient au m�me user
			return null;
		}

		// Sinon, c'est lock�
		return token.userNni;
	}

	/**
	 * V�rifie si un token est toujours valide, ie r�ponds aux crit�res suivants :
	 * <ul>
	 * <li>Un token existe pour l'entit� pass�e.</li>
	 * <li>Le token appartient au user pass�.</li>
	 * <li>Le token existe depuis moins de {@value #LOCK_MAX_TIME} minutes.</li>
	 * </ul>
	 * 
	 * @param entityName
	 *            le nom de l'entit�
	 * @param entityKey
	 *            la cl� de l'entit�
	 * @param user
	 *            le user connect�
	 * @return true si le token est toujours valide, false sinon.
	 */
	public boolean isTokenStillValid(String entityName, Key entityKey, ApplicationUser user) {
		CmToken token = getFromConcurrencyMap(new CmKey(entityName, entityKey));
		if (token == null) {
			// Pas de lock
			return false;
		} else if (!token.userNni.equals(user.getLogin())) {
			// Lock appartient au m�me user
			return false;
		}

		return token.isStillValid();
	}

	/**
	 * Supprime tous les locks du user.
	 * 
	 * @param user
	 *            le user connect�
	 */
	public synchronized void resetUser(ApplicationUser user) {
		Iterator<CmToken> iterator = concurrencyMap.values().iterator();
		Set<CmToken> toRemove = new HashSet<CmToken>();
		while (iterator.hasNext()) {
			CmToken token = iterator.next();
			if (token.userNni.equals(user.getLogin())) {
				toRemove.add(token);
			}
		}
		for (CmToken tokenToRemove : toRemove) {
			concurrencyMap.values().remove(tokenToRemove);
		}
	}

	/**
	 * Cl� de lock.
	 */
	private class CmKey {
		public final String entity;
		public final String ident;

		public CmKey(String entityName, Key entityKey) {
			this.entity = entityName;
			this.ident = (entityKey == null ? null : entityKey.getEncodedValue());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entity == null) ? 0 : entity.hashCode());
			result = prime * result + ((ident == null) ? 0 : ident.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof CmKey)) {
				return false;
			}
			CmKey other = (CmKey) obj;
			if (entity == null) {
				if (other.entity != null) {
					return false;
				}
			} else if (!entity.equals(other.entity)) {
				return false;
			}
			if (ident == null) {
				if (other.ident != null) {
					return false;
				}
			} else if (!ident.equals(other.ident)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "CmKey [entity=" + entity + ", ident=" + ident + "]";
		}
	}

	/**
	 * Token de lock.
	 */
	private class CmToken {
		public final String userNni;
		public final int actionCode;
		public final Calendar date;

		public CmToken(ApplicationUser pUser, int actionCode) {
			this.userNni = pUser.getLogin();
			this.actionCode = actionCode;
			this.date = new GregorianCalendar();
		}

		public boolean isStillValid() {
			date.add(Calendar.MINUTE, LOCK_MAX_TIME);

			return date.after(new GregorianCalendar());
		}

		@Override
		public String toString() {
			return "CmToken [userNni=" + userNni + ", actionCode=" + actionCode + ", date=" + date + "]";
		}
	}
}
