package fr.logica.ui;

import java.io.Serializable;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8337856000006122499L;

	public enum Severity {
		INFO, ERROR
	}

	private Severity severity;

	private String message;

	public Message() {

	}

	public Message(String pMessage, Severity pSev) {
		message = pMessage;
		severity = pSev;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}
}
