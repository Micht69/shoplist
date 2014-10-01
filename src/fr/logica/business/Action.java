package fr.logica.business;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Action implements Serializable, Cloneable {

	/** serialUID */
	private static final long serialVersionUID = 1301307950031652178L;

	private Integer code;
	private Integer subActionCode;
	private Set<Integer> subActions;
	private String queryName;
	private String pageName;

	private Integer next;

	private Input input;
	private Persistence persistence;
	private UserInterface ui;
	private Process process;

	public Action(Integer code, String queryName, String pageName, Integer next, Input input,
			Persistence persistence, UserInterface ui, Process process) {
		this.code = code;
		this.queryName = queryName;
		this.pageName = pageName;
		this.next = next;
		this.input = input;
		this.persistence = persistence;
		this.ui = ui;
		this.process = process;
		this.subActions = new HashSet<Integer>();
		this.subActionCode = null;
		setTypeAttributeForCompatibility();
	}
	
	public Action(Integer code, String queryName, String pageName, Integer next, Input input,
			Persistence persistence, UserInterface ui, Process process, Integer... subActions) {
		this(code, queryName, pageName, next, input, persistence, ui, process);
		this.subActions.addAll(Arrays.asList(subActions));
	}

	public Action(Action a) {
		this.code = a.getCode();
		this.subActions = new HashSet<Integer>(a.getSubActions());
		this.queryName = a.getQueryName();
		this.pageName = a.getPageName();
		this.next = a.getNext();
		this.input = a.getInput();
		this.persistence = a.getPersistence();
		this.ui = a.getUi();
		this.process = a.getProcess();
		this.subActionCode = null;
		setTypeAttributeForCompatibility();
	}

	public Action getSubAction(Integer subActionCode) {
		if (!subActions.contains(subActionCode)) {
			return null;
		}
		Action subAction = null;
		try {
			subAction = (Action) this.clone();
			subAction.subActions = null;
			subAction.subActionCode = subActionCode;
		} catch (CloneNotSupportedException e) {

		}
		return subAction;
	}

	public static Action getListAction(String queryName, String pageName) {
		return new Action(Constants.SEARCH, queryName, pageName, null, Input.QUERY, Persistence.NONE, UserInterface.OUTPUT,
				Process.STANDARD);
	}

	public static Action getDetachLinkAction() {
		return new Action(Constants.DETACH, null, null, null, Input.ONE, Persistence.NONE, UserInterface.NONE,
				Process.STANDARD);
	}

	public static Action getDetachBackRefAction() {
		return new Action(Constants.DETACH, null, null, null, Input.MANY, Persistence.UPDATE, UserInterface.NONE,
				Process.STANDARD);
	}

	public static Action getAttachLinkAction(String queryName) {
		return new Action(Constants.SELECT, queryName, null, null, Input.QUERY, Persistence.NONE, UserInterface.INPUT,
				Process.STANDARD);
	}

	public static Action getAttachBackRefAction(String queryName) {
		return new Action(Constants.SELECT, queryName, null, null, Input.QUERY, Persistence.UPDATE, UserInterface.INPUT,
				Process.STANDARD);
	}

	public static Action getDummyAction() {
		return new Action(Constants.DUMMY, null, null, null, Input.NONE, Persistence.NONE, UserInterface.NONE,
				Process.NONE);
	}

	public boolean hasSubActions() {
		return subActions.size() > 0;
	}

	public enum Input {
		NONE, ONE, MANY, QUERY
	}

	public enum Persistence {
		NONE, INSERT, UPDATE, DELETE
	}

	public enum UserInterface {
		NONE, INPUT, READONLY, OUTPUT, OUTPUT_BLANK
	}

	public enum Process {
		NONE, STANDARD, CUSTOM, WS
	}

	public Integer getCode() {
		return code;
	}

	public Integer getSubActionCode() {
		return subActionCode;
	}

	public Set<Integer> getSubActions() {
		return subActions;
	}

	public String getQueryName() {
		return queryName;
	}

	public String getPageName() {
		return pageName;
	}

	public Integer getNext() {
		return next;
	}

	public Input getInput() {
		return input;
	}

	public Persistence getPersistence() {
		return persistence;
	}

	public UserInterface getUi() {
		return ui;
	}

	public Process getProcess() {
		return process;
	}

	/***************** Compatibility Constructor and attributes *******************/
	@Deprecated
	public int type;

	@Deprecated
	public Integer subAction;

	@Deprecated
	public Action(int code, int type) {
		this.code = Integer.valueOf(code);
		this.type = type;
		switch (type) {
		case Constants.DETACH:
			input = Input.ONE;
			persistence = Persistence.UPDATE;
			ui = UserInterface.NONE;
			process = Process.STANDARD;
			break;
		case Constants.CREATE:
			input = Input.NONE;
			persistence = Persistence.INSERT;
			ui = UserInterface.INPUT;
			process = Process.STANDARD;
			break;
		case Constants.SELECT:
			input = Input.QUERY;
			persistence = Persistence.UPDATE;
			ui = UserInterface.OUTPUT;
			process = Process.STANDARD;
			break;
		case Constants.MODIFY:
			input = Input.ONE;
			persistence = Persistence.UPDATE;
			ui = UserInterface.INPUT;
			process = Process.STANDARD;
			break;
		case Constants.COPY:
			input = Input.ONE;
			persistence = Persistence.INSERT;
			ui = UserInterface.INPUT;
			process = Process.STANDARD;
			break;
		case Constants.DELETE:
			input = Input.ONE;
			persistence = Persistence.DELETE;
			ui = UserInterface.READONLY;
			process = Process.STANDARD;
			break;
		case Constants.DISPLAY:
			input = Input.ONE;
			persistence = Persistence.NONE;
			ui = UserInterface.READONLY;
			process = Process.NONE;
			break;
		case Constants.NO_ELT_CUSTOM_ACTION:
			input = Input.NONE;
			persistence = Persistence.UPDATE;
			ui = UserInterface.NONE;
			process = Process.CUSTOM;
			break;
		case Constants.NO_ELT_CUSTOM_ACTION_DISPLAY:
			input = Input.NONE;
			persistence = Persistence.UPDATE;
			ui = UserInterface.INPUT;
			process = Process.CUSTOM;
			break;
		case Constants.CUSTOM_ACTION:
			input = Input.MANY;
			persistence = Persistence.UPDATE;
			ui = UserInterface.NONE;
			process = Process.CUSTOM;
			break;
		case Constants.CUSTOM_ACTION_DISPLAY:
			input = Input.MANY;
			persistence = Persistence.UPDATE;
			ui = UserInterface.INPUT;
			process = Process.CUSTOM;
			break;
		case Constants.SINGLE_ELT_CUSTOM_ACTION:
			input = Input.ONE;
			persistence = Persistence.UPDATE;
			ui = UserInterface.NONE;
			process = Process.CUSTOM;
			break;
		case Constants.SINGLE_ELT_CUSTOM_ACTION_DISPLAY:
			input = Input.ONE;
			persistence = Persistence.UPDATE;
			ui = UserInterface.INPUT;
			process = Process.CUSTOM;
			break;
		case Constants.DUMMY:
			input = Input.NONE;
			persistence = Persistence.NONE;
			ui = UserInterface.NONE;
			process = Process.NONE;
			break;
		default:
			throw new TechnicalException("Non retro-compatible type with Action class 3.0 : type=" + type);
		}

	}

	private void setTypeAttributeForCompatibility() {
		if (process == Process.STANDARD) {
			if (input == Input.NONE && persistence == Persistence.INSERT && ui == UserInterface.INPUT)
				type = Constants.CREATE;
			if (input == Input.ONE && persistence == Persistence.UPDATE && ui == UserInterface.INPUT)
				type = Constants.MODIFY;
			if (input == Input.ONE && persistence == Persistence.INSERT && ui == UserInterface.INPUT)
				type = Constants.COPY;
			if (input == Input.ONE && persistence == Persistence.DELETE && ui == UserInterface.READONLY)
				type = Constants.DELETE;
			if (input == Input.ONE && persistence == Persistence.NONE && ui == UserInterface.READONLY)
				type = Constants.DISPLAY;
			if (code == Constants.SELECT)
				type = Constants.SELECT;
			if (code == Constants.DETACH)
				type = Constants.DETACH;
		} else if (process == Process.CUSTOM) {
			if (input == Input.NONE) {
				if (ui == UserInterface.NONE)
					type = Constants.NO_ELT_CUSTOM_ACTION;
				if (ui == UserInterface.INPUT)
					type = Constants.NO_ELT_CUSTOM_ACTION_DISPLAY;
			}
			if (input == Input.ONE) {
				if (ui == UserInterface.NONE)
					type = Constants.SINGLE_ELT_CUSTOM_ACTION;
				if (ui == UserInterface.INPUT)
					type = Constants.SINGLE_ELT_CUSTOM_ACTION_DISPLAY;
			}
			if (input == Input.MANY) {
				if (ui == UserInterface.NONE)
					type = Constants.CUSTOM_ACTION;
				if (ui == UserInterface.INPUT)
					type = Constants.CUSTOM_ACTION_DISPLAY;
			}
		} else {
			type = Constants.DUMMY;
		}
	}

	/**
	 * Returns true if parameter code is equal to int value of attribute code
	 * 
	 * @param code The action code to check
	 * @return boolean true if code == this.code.intValue()
	 */
	public boolean is(int code) {
		return (this.code != null) && this.code.intValue() == code;
	}

	@Override
	public String toString() {
		return "Action [code=" + code + ", queryName=" + queryName + ", pageName=" + pageName + ", next=" + next + ", input=" + input
				+ ", persistence=" + persistence + ", ui=" + ui + ", process=" + process + "]";
	}
}
