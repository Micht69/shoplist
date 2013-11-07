package fr.logica.jsf.controller;

import java.io.Serializable;
import java.util.HashMap;

import fr.logica.business.Context;

public class TabeditController extends HashMap<String, TableEditor> implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 2965647296899059513L;

	/** Injected */
	private SessionController sessionCtrl;

	/** Injected */
	private JSFController jsfCtrl;

	public SessionController getSessionCtrl() {
		return sessionCtrl;
	}

	public void setSessionCtrl(SessionController sessionCtrl) {
		this.sessionCtrl = sessionCtrl;
	}

	public JSFController getJsfCtrl() {
		return jsfCtrl;
	}

	public void setJsfCtrl(JSFController jsfCtrl) {
		this.jsfCtrl = jsfCtrl;
	}

	/** Register a TableEditor for a table on the page. */
	public void register(String key) {
		register(key, null);
	}

	/** Register a TableEditor for a table on the page. */
	public void register(String key, String linkName) {
		if (!this.containsKey(key)) {
			TableEditor editor = new TableEditor(this, sessionCtrl.getUser(), jsfCtrl.getPage(), linkName);
			this.put(key, editor);
		}
	}
	
	public void displayMessages(Context ctx) {
		jsfCtrl.getPageMessages(ctx.getMessages());
	}

}
