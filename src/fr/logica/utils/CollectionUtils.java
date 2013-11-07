package fr.logica.utils;

import java.util.Collection;
import java.util.Iterator;

public final class CollectionUtils {

	private CollectionUtils() {
		// not used
	}

	public static String join(Collection<String> strings, String separator) {
		if (strings.isEmpty())
			return "";

		Iterator<String> it = strings.iterator();
		StringBuilder result = new StringBuilder(it.next());
		while (it.hasNext()) {
			result.append(separator).append(it.next());
		}

		return result.toString();
	}

}
