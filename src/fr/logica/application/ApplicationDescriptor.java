package fr.logica.application;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Application SHOPLIST descriptor
 *
 * @author CGI
 */
public class ApplicationDescriptor implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/** Application name */
	public static final String NAME = "SHOPLIST";

	public static final Set<String> domains;
	
	static {
		domains = new HashSet<String>();
		domains.add("shopArticle");
		domains.add("shopList");
		domains.add("shopListLArticle");
		domains.add("shopShelf");
		domains.add("shopUser");

	}
	
	public static Set<String> getDomains() {
		return domains;
	}
}
