package fr.logica.security;

import java.util.ArrayList;
import java.util.List;

public class SecurityFunctionNode {
	
	private SecurityFunction sf = new SecurityFunction();
	private List<SecurityFunctionNode> childs = new ArrayList<SecurityFunctionNode>();
	
	public SecurityFunction getSf() {
		return sf;
	}
	
	public List<SecurityFunctionNode> getChilds() {
		return childs;
	}

}
