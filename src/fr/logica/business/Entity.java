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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.logica.db.DB;

public abstract class Entity {

	/** Logger */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Entity.class);

	private final Links links = new Links(getModel());

	public abstract String $_getName();

	public abstract String $_getDesc();

	public Entity() {
		// Default constructor
	}

	public Entity(Entity e) {
		for (String f : getModel().getFields()) {
			invokeSetter(f, e.invokeGetter(f));
		}
	}
	
	public EntityModel getModel() {
		return EntityManager.getEntityModel($_getName());
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
		String ret = "[";
		for (String fieldName : getModel().getFields()) {
			if (!"[".equals(ret)) {
				ret += ",";
			}
			try {
				ret += fieldName + "=" + invokeGetter(fieldName);
			} catch (Exception e) {
			}
		}
		ret += "]";
		return ret;
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
			}
			key.setValue(key.getModel().getFields().get(i), value);
		}
		return key;
	}

	/**
	 * Fixe les valeurs des champs de l'entité qui correspondent à une clé étrangère.
	 * 
	 * @param keyName
	 *            Le nom de la clé étrangère de l'entité courante que l'on va fixer.
	 * @param key
	 *            La nouvelle clé primaire valorisée que l'on va référencer avec notre clé étrangère.
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
			Object value = key.getValue(key.getModel().getFields().get(i));
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
			try {
				val = Integer.parseInt((String) value);
			} catch (NumberFormatException ex) {
				throw new TechnicalException("Impossible de convertir " + value + " en un entier pour l'assigner à " + fieldName);
			}
		} else if (Long.class.equals(f.getGenericType()) && value instanceof String) {
			try {
				val = Long.parseLong((String) value);
			} catch (NumberFormatException ex) {
				throw new TechnicalException("Impossible de convertir " + value + " en un Long pour l'assigner à " + fieldName);
			}
		} else if (Timestamp.class.equals(f.getGenericType()) && value instanceof Date) {
			val = new Timestamp(((Date) value).getTime());
		} else if (Date.class.equals(f.getGenericType()) && value instanceof String) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				val = sdf.parse((String) value);
			} catch (ParseException e) {
				throw new TechnicalException("Impossible de convertir " + value + " en une Date pour l'assigner à " + fieldName);
			}
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

	public Link setLink(String linkName, Link link) {
		return links.put(linkName, link);
	}

	public void removeDefaultValues() {
		for (String fieldName : getModel().getFields()) {
			invokeSetter(fieldName, null);
		}
	}
	
	/**
	 * Loads the current bean with all values of the bean instance given in parameter 
	 * @param e		Bean to load values from
	 */
	protected void syncFromBean(Entity e) {
		for (String f : getModel().getFields()) {
			invokeSetter(f, e.invokeGetter(f));
		}
	}
	
	/**
	 * Inserts the current bean in database
	 * 
	 * @param ctx Current context with opened database connection
	 */
	public void insert(Context ctx) {
		DB.insert(this, ctx);
	}

	/**
	 * Persists the current bean in database
	 * 
	 * @param ctx Current context with opened database connection
	 */
	public void persist(Context ctx) {
		DB.persist(this, ctx);
	}

	/**
	 * Removes the current bean in database
	 * 
	 * @param ctx Current context with opened database connection
	 */
	public void remove(Context ctx) {
		DB.remove(this, ctx);
	}
	
	/**
	 * Finds and loads bean from database based on its current primary key.
	 * 
	 * @param ctx Current context with opened database connection
	 * @return <code>true</code> if the bean has been found and loaded, <code>false</code> if primary key is not full or if no matching bean has
	 *         been found.
	 */
	public boolean find(Context ctx) {
		if (!getPrimaryKey().isFull()) {
			return false;
		}
		Entity dbInstance = DB.get($_getName(), getPrimaryKey(), ctx);
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
	 * @param ctx
	 *            Current context with opened database connection.
	 * @return <code>true</code> if some data is different between current Entity and database value, <code>false</code> if they are the same.
	 *         This method returns <code>true</code> if the current instance has no primary key or if there is no matching database instance.
	 */
	public boolean hasChanged(Context ctx) {
		Key pk = getPrimaryKey();
		if (!pk.isFull()) {
			return true;
		}

		Entity dbEntity = DB.get($_getName(), pk, ctx);
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

	public String serialize() {
		String serialized = "";
		for (Field field : getAllFields()) {
			Object obj = invokeGetter(field.getName());
			String res = serializeObject(obj);
			if (res != null && !res.isEmpty()) {
				if (!serialized.isEmpty()) {
					serialized += ",";
				}
				serialized += field.getName() + "='" + res.replace("'", "\\'") + "'";
			}
		}
		for (String key : links.keySet()) {
			Link link = links.get(key);
			if (link != null && link.getEncodedValue() != null && !link.getEncodedValue().isEmpty()) {
				if (!serialized.isEmpty()) {
					serialized += ",";
				}
				serialized += "links." + key + "='" + link.getEncodedValue().replace("'", "\\'") + "'";
			}
		}
		return serialized;
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
}
