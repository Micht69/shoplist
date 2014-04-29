package fr.logica.ui;

import java.io.Serializable;

public class Message implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 8337856000006122499L;

	/**
	 * Severity levels : <br/>
	 * - INFO : Informative message to the user, non blocking process <br/>
	 * - ERROR : Error message, blocking current process <br/>
	 */
	public enum Severity {
		INFO, ERROR
	}

	/** Message Severity */
	private Severity severity;

	/** Message text */
	private String message;

	/**
	 * Default constructor for serialization
	 */
	public Message() {

	}

	/**
	 * Creates a Message with severity pSev and
	 * 
	 * @param pMessage
	 * @param pSev
	 */
	public Message(String pMessage, Severity pSev) {
		message = pMessage;
		severity = pSev;
	}

	/**
	 * Gets Severity of the message
	 * 
	 * @return Message severity
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * Gets value of Message
	 * 
	 * @return The message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(message);
		builder.append(" [").append(severity).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((severity == null) ? 0 : severity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (severity != other.severity)
			return false;
		return true;
	}

}
