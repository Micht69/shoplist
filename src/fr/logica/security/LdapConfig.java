package fr.logica.security;

public class LdapConfig {
	public String url = null;
	public String authentication = "simple";
	public String principal = null;
	public String credentials = null;
	public String userBaseDn = "";
	public String userSubsearch = "true";
	public String userLoginAttr = "cn";
	public String userDnAttr = "dn";
	public String userOtherAttr = null;
}
