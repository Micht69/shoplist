package fr.logica.utils;

import java.lang.reflect.Field;

public final class ReflectionUtils {

	private ReflectionUtils() {
		// not used
	}

	/** Set the value of a field on an object. All fields are affected, no matter what their visibility or level of definition is */
	public static void setFieldValue(Object target, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {
		Field f = getAnyField(target.getClass(), fieldName);
		f.setAccessible(true);
		f.set(target, value);
	}

	/** Get the value of a field on an object. All fields are affected, no matter what their visibility or level of definition is */
	public static Object getFieldValue(Object target, String fieldName) throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {
		Field f = getAnyField(target.getClass(), fieldName);
		f.setAccessible(true);
		return f.get(target);
	}

	public static Field getAnyField(Class<?> clazz, String name) throws SecurityException, NoSuchFieldException {
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException ex) {
			Class<?> superclazz = clazz.getSuperclass();

			if (superclazz == null) {
				throw ex;
			}

			return getAnyField(superclazz, name);
		}
	}

	/** Same method as on the englobing class, but all exceptions are unchecked */
	public static class UncheckedExceptions {

		public static void setFieldValue(Object target, String fieldName, Object value) {
			try {
				ReflectionUtils.setFieldValue(target, fieldName, value);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}

		public static Object getFieldValue(Object target, String fieldName) {
			try {
				return ReflectionUtils.getFieldValue(target, fieldName);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}

		public static Field getAnyField(Class<?> clazz, String name) {
			try {
				return ReflectionUtils.getAnyField(clazz, name);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
