package fr.logica.business;

import java.io.Serializable;

public class Action implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6563225624826701462L;

	public int code;

	public int type;

	public Integer next;

	public Integer subAction;

	public boolean hasSubActions;

	public Action(int codeAction, int typeAction, int nextAction) {
		this(codeAction, typeAction);
		next = nextAction;
	}

	public Action(int codeAction, int typeAction) {
		code = codeAction;
		type = typeAction;
		// FIXME Action Type of Detach Action is 1, but we use action type in ActionUtils to define if the action has a display or not.
		// So we need to force action type to -1
		if (code == Constants.DETACH && typeAction == Constants.SELECT) {
			type = Constants.DETACH;
		}
	}

	public Action(int codeAction, int typeAction, boolean hasSubActions) {
		this(codeAction, typeAction);
		this.hasSubActions = hasSubActions;
	}

	public Action(int codeAction, int typeAction, int nextAction, boolean hasSubActions) {
		this(codeAction, typeAction, nextAction);
		this.hasSubActions = hasSubActions;
	}

	@Override
	public String toString() {
		return "Action [code=" + code + ", type=" + type + "]";
	}

}
