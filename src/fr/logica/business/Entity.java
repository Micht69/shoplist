package fr.logica.business;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.utils.Diff;

public abstract class Entity implements Cloneable {

	/** Logger */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Entity.class);

	private Links links = new Links(getModel(), false);
	private Links backRefs = new Links(getModel(), true);

	/**
	 * Copy of Primary Key with "initial" values, just after we got the bean out of database
	 */
	private Key initialKey = null;

	public abstract String name();

	public abstract String description();

	public Entity() {
		// Default constructor
	}

	public Entity(Entity e) {
		for (String f : getModel().getFields()) {
			invokeSetter(f, e.invokeGetter(f));
		}
	}

	public EntityModel getModel() {
		return EntityManager.getEntityModel(name());
	}

	public List<Field> getAllFields() {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Field> fList = new ArrayList<Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!Modifier.isFinal(field.getModifiers())) {
				fList.add(field);
			}
		}
		return fList;
	}

	public List<Field> getFields() {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Field> fList = new ArrayList<Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!Modifier.isFinal(field.getModifiers()) && getModel().getField(field.getName()).isFromDatabase()) {
				fList.add(field);
			}
		}
		return fList;
	}

	/** Returns the names of all calculated fields on the entity */
	public List<String> getTransientFields() {
		List<String> fList = new ArrayList<String>();
		for (String fieldName : getModel().getFields()) {
			if (getModel().getField(fieldName).isTransient()) {
				fList.add(fieldName);
			}
		}
		return fList;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("[");
		for (String fieldName : getModel().getFields()) {
			if (ret.length() > 1) {
				ret.append(",");
			}
			ret.append(fieldName).append("=").append(invokeGetter(fieldName));
		}
		ret.append("]");
		return ret.toString();
	}

	/** Creates a map from the entity fields. */
	public Map<String, Object> dump() {
		Map<String, Object> dump = new HashMap<String, Object>();
		for (String fieldName : getModel().getFields()) {
			Object v = invokeGetter(fieldName);
			if (v != null) {
				dump.put(fieldName, v);
			}
		}
		return dump;
	}

	public final Key getPrimaryKey() {
		KeyModel km = getModel().getKeyModel();
		Key key = new Key(km);
		for (String field : km.getFields()) {
			Object value = null;
			try {
				value = invokeGetter(field);
			} catch (Exception e) {
				value = null;
			}
			key.setValue(field, value);
		}
		return key;
	}

	public final Key getForeignKey(String keyName) {
		String refEntityName = null;
		for (String linkName : getModel().getLinkNames()) {
			if (getModel().getLinkModel(linkName).getKeyName().equals(keyName)) {
				refEntityName = getModel().getLinkModel(linkName).getRefEntityName();
			}
		}
		if (refEntityName == null) {
			return null;
		}

		Key key = new Key(refEntityName);
		for (int i = 0; i < getModel().getForeignKeyModel(keyName).getFields().size(); i++) {
			Object value = null;
			try {
				value = invokeGetter(getModel().getForeignKeyModel(keyName).getFields().get(i));
			} catch (Exception e) {
				value = null;
			}
			key.setValue(key.getModel().getFields().get(i), value);
		}
		return key;
	}

	/**
	 * Fixe les valeurs des champs de l'entité qui correspondent à une clé étrangère.
	 * 
	 * @param keyName Le nom de la clé étrangère de l'entité courante que l'on va fixer.
	 * @param key La nouvelle clé primaire valorisée que l'on va référencer avec notre clé étrangère.
	 */
	public void setForeignKey(String keyName, Key key) {
		ForeignKeyModel fk = getModel().getForeignKeyModel(keyName);
		for (int i = 0; i < fk.getFields().size(); i++) {
			Object value = null;
			if (key != null) {
				value = key.getValue(key.getModel().getFields().get(i));
			}
			invokeSetter(fk.getFields().get(i), value);
		}
	}

	public void setPrimaryKey(Key key) {
		KeyModel pkModel = getModel().getKeyModel();
		for (int i = 0; i < pkModel.getFields().size(); i++) {
			Object value = null;
			if (key != null) {
				value = key.getValue(key.getModel().getFields().get(i));
			}
			invokeSetter(pkModel.getFields().get(i), value);
		}
	}

	public void invokeSetter(String fieldName, Object value) {
		Object val;
		Field f;
		try {
			f = this.getClass().getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		} catch (NoSuchFieldException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		}
		if (Integer.class.equals(f.getGenericType()) && value instanceof String) {
			if ("null".equals(value)) {
				val = null;
			} else {
				try {
					val = Integer.parseInt((String) value);
				} catch (NumberFormatException ex) {
					throw new TechnicalException("Impossible de convertir " + value + " en un entier pour l'assigner à " + fieldName);
				}
			}
		} else if (Long.class.equals(f.getGenericType()) && value instanceof String) {
			if ("null".equals(value)) {
				val = null;
			} else {
				try {
					val = Long.parseLong((String) value);
				} catch (NumberFormatException ex) {
					throw new TechnicalException("Impossible de convertir " + value + " en un Long pour l'assigner à " + fieldName);
				}
			}
		} else if (Timestamp.class.equals(f.getGenericType()) && value instanceof Date) {
			val = new Timestamp(((Date) value).getTime());
		} else if (Date.class.equals(f.getGenericType()) && value instanceof String) {
			if ("null".equals(value)) {
				val = null;
			} else {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					val = sdf.parse((String) value);
				} catch (ParseException e) {
					throw new TechnicalException("Impossible de convertir " + value + " en une Date pour l'assigner à " + fieldName);
				}
			}
		} else if (Boolean.class.equals(f.getGenericType()) && value instanceof String) {
			val = Boolean.valueOf((String) value);
		} else {
			val = value;
		}
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Method method = null;
		if (val != null) {
			try {
				method = this.getClass().getMethod(methodName, f.getType());
			} catch (SecurityException e) {
				throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
			} catch (NoSuchMethodException e) {
				throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
			}
		} else {
			for (int i = 0; i < this.getClass().getDeclaredMethods().length; i++) {
				Method m = this.getClass().getDeclaredMethods()[i];
				if (methodName.equals(m.getName())) {
					method = m;
				}
			}
		}
		try {
			method.invoke(this, val);
		} catch (SecurityException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		} catch (InvocationTargetException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		}
	}

	public Object invokeGetter(String fieldName) {
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Object result = null;
		try {
			Method method = this.getClass().getMethod(methodName);

			result = method.invoke(this);
		} catch (SecurityException e) {
			throw new TechnicalException("Impossible d'invoquer la methode " + methodName, e);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException("Impossible d'invoquer la methode " + methodName, e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Impossible d'invoquer la methode " + methodName, e);
		} catch (InvocationTargetException e) {
			throw new TechnicalException("Impossible d'invoquer la methode " + methodName, e);
		} catch (NoSuchMethodException e) {
			throw new TechnicalException("Impossible d'invoquer la methode " + methodName, e);
		}
		return result;
	}

	public Map<String, Object> enumValues(String enumName) {
		return getModel().enumValues(enumName);
	}

	public String label(String fieldName) {
		return getModel().getField(fieldName).getDefinedLabel(invokeGetter(fieldName));
	}

	public Links getLinks() {
		return links;
	}

	public Link getLink(String linkName) {
		return links.get(linkName);
	}

	public Links getBackRefs() {
		return backRefs;
	}

	public Link getBackRef(String linkName) {
		return backRefs.get(linkName);
	}

	public void removeDefaultValues() {
		for (String fieldName : getModel().getFields()) {
			invokeSetter(fieldName, null);
		}
	}

	/**
	 * Loads the current bean with all values of the bean instance given in parameter
	 * 
	 * @param e Bean to load values from
	 */
	public void syncFromBean(Entity e) {
		for (String f : getModel().getFields()) {
			invokeSetter(f, e.invokeGetter(f));
		}
	}

	/**
	 * Inserts the current bean in database
	 * 
	 * @param ctx Current context with opened database connection
	 */
	public void insert(RequestContext ctx) {
		DB.insert(this, ctx);
	}

	/**
	 * Persists the current bean in database
	 * 
	 * @param ctx Current context with opened database connection
	 */
	public void persist(RequestContext ctx) {
		DB.persist(this, ctx);
	}

	/**
	 * Removes the current bean in database
	 * 
	 * @param ctx Current context with opened database connection
	 */
	public void remove(RequestContext ctx) {
		DB.remove(this, ctx);
	}

	/**
	 * Finds and loads bean from database based on its current primary key.
	 * 
	 * @param ctx Current context with opened database connection
	 * @return <code>true</code> if the bean has been found and loaded, <code>false</code> if primary key is not full or if no matching bean has
	 *         been found.
	 */
	public boolean find(RequestContext ctx) {
		if (!getPrimaryKey().isFull()) {
			return false;
		}
		Entity dbInstance = DB.get(name(), getPrimaryKey(), ctx);
		if (dbInstance != null) {
			syncFromBean(dbInstance);
			return true;
		}
		return false;
	}

	/**
	 * Compares the current instance to corresponding data in database. This method will access database using context ctx and compare all fields
	 * stored in database. BLOB and CLOB fields are ignored. transient variables are ignored.
	 * 
	 * @param ctx Current context with opened database connection.
	 * @return <code>true</code> if some data is different between current Entity and database value, <code>false</code> if they are the same.
	 *         This method returns <code>true</code> if the current instance has no primary key or if there is no matching database instance.
	 */
	public boolean hasChanged(RequestContext ctx) {
		Key pk = getPrimaryKey();
		if (!pk.isFull()) {
			return true;
		}

		Entity dbEntity = DB.get(name(), pk, ctx);
		if (dbEntity == null) { // No matching instance in database
			return true;
		}

		for (String fieldName : getModel().getFields()) {
			EntityField fieldMetadata = getModel().getField(fieldName);
			if (fieldMetadata.isTransient()) {
				continue; // We don't care about transient data
			}
			if ("BLOB".equals(fieldMetadata.getSqlType())) {
				continue; // We don't care about BLOBs
			}
			Object obj = invokeGetter(fieldName);
			Object dbObj = dbEntity.invokeGetter(fieldName);
			if (obj == null && dbObj != null || obj != null && !obj.equals(dbObj)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculate the difference between this entity and another one. The current entity is "mine" and the other entity is "theirs" (in the
	 * {@link Diff} object). Only the fields wich are actually different are present in the map.
	 * 
	 * @return A map where each entry associates a field to the difference in value.
	 */
	public Map<String, Diff> difference(Entity that) {
		if (that == null || !that.getClass().equals(this.getClass())) {
			throw new IllegalArgumentException(String.valueOf(that));
		}

		Map<String, Diff> result = new HashMap<String, Diff>();

		for (String fieldName : getModel().getFields()) {

			EntityField fieldMetadata = getModel().getField(fieldName);
			if (fieldMetadata.isTransient()) {
				continue; // We don't care about transient data
			}

			Object thisValue = this.invokeGetter(fieldName);
			Object thatValue = that.invokeGetter(fieldName);
			if (thisValue == null && thatValue != null || thisValue != null && !thisValue.equals(thatValue)) {
				String label = MessageUtils.getInstance().getGenLabel(this.getModel().name() + "." + fieldName);
				if ("BLOB".equals(fieldMetadata.getSqlType()) || "CLOB".equals(fieldMetadata.getSqlType())) {
					result.put(fieldName, new Diff(label));
				} else {
					result.put(fieldName, new Diff(label, thisValue, thatValue));
				}
			}
		}

		return result;
	}

	public String serialize() {
		StringBuilder serialized = new StringBuilder();
		for (Field field : getAllFields()) {
			Object obj = invokeGetter(field.getName());
			String res = serializeObject(obj);
			if (res != null && !res.isEmpty()) {
				if (serialized.length() > 0) {
					serialized.append(",");
				}
				serialized.append(field.getName()).append("='").append(res.replace("'", "\\'")).append("'");
			}
		}
		for (String key : links.keySet()) {
			Link link = links.get(key);
			if (link != null && link.getEncodedValue() != null && !link.getEncodedValue().isEmpty()) {
				if (serialized.length() > 0) {
					serialized.append(",");
				}
				serialized.append("links.").append(key).append("='").append(link.getEncodedValue().replace("'", "\\'")).append("'");
			}
		}
		for (String key : backRefs.keySet()) {
			Link link = backRefs.get(key);
			if (link != null && link.getEncodedValue() != null && !link.getEncodedValue().isEmpty()) {
				if (serialized.length() > 0) {
					serialized.append(",");
				}
				serialized.append("backRefs.").append(key).append("='").append(link.getEncodedValue().replace("'", "\\'")).append("'");
			}
		}
		return serialized.toString();
	}

	private static String serializeObject(Object obj) {
		if (obj == null) {
			return null;
		}
		String res = null;
		if (obj instanceof String) {
			res = (String) obj;
		} else if (obj instanceof Integer) {
			res = String.valueOf((Integer) obj);
		} else if (obj instanceof Date) {
			res = DateUtils.formatDate((Date) obj);
		} else if (obj instanceof BigDecimal) {
			res = String.valueOf((BigDecimal) obj);
		} else if (obj instanceof Boolean) {
			res = String.valueOf((Boolean) obj);
		}
		return res;
	}

	public Object deserializeValue(String fieldName, String value) {
		Object val = value;
		Field f;
		try {
			f = this.getClass().getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		} catch (NoSuchFieldException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		}
		if (Date.class.equals(f.getGenericType())) {
			try {
				val = DateUtils.stringToDate(value);
			} catch (Exception ex) {
				throw new TechnicalException("Impossible de convertir " + value + " en date pour l'assigner à " + fieldName);
			}
		}
		if (BigDecimal.class.equals(f.getGenericType())) {
			try {
				val = new BigDecimal(value);
			} catch (Exception ex) {
				throw new TechnicalException("Impossible de convertir " + value + " en décimal pour l'assigner à " + fieldName);
			}
		}
		return val;
	}

	/**
	 * Clones current bean instance. This method uses the abstract entity constructor that copy the current entity fields.
	 */
	@Override
	public Entity clone() {
		try {
			return (Entity) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException(e.getMessage(), e);
		}
	}

	public void resetLinksAndBackRefs() {
		links = new Links(getModel(), false);
		backRefs = new Links(getModel(), true);
	}

	public Key getInitialKey() {
		return initialKey;
	}

	public void setInitialKey(Key initialKey) {
		this.initialKey = initialKey;
	}
}
