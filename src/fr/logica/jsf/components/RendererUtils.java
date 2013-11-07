package fr.logica.jsf.components;

public class RendererUtils {

	private RendererUtils() {
	}

	/**
	 * Returns an escaped clientId, to be used as a jQuery selector.
	 * <p>
	 * As {@code '.'} and {@code ':'} are CSS selectors (for classes and pseudo-classes, they must be escaped with {@code "\\"}.<br />
	 * For example, calling this method with {@code form:input} will return {@code form\\:input}.
	 * </p>
	 * 
	 * @param clientId
	 *            ClientId to escape.
	 * @return Escaped clientId.
	 */
	public static String getEscapedClientId(String clientId) {
		return clientId.replaceAll(":", "\\\\\\\\:").replaceAll("\\.", "\\\\\\\\.");
	}

}
