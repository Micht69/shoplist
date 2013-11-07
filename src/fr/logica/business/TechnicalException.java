package fr.logica.business;

public class TechnicalException extends LogicaException {

	private static final long serialVersionUID = 5051522312026416092L;

	public TechnicalException(String message) {
		super(message);
	}

	public TechnicalException(String message, Throwable t) {
		super(message, t);
	}
}
