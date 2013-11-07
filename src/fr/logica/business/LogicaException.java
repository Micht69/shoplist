package fr.logica.business;

public class LogicaException extends RuntimeException {

	private static final long serialVersionUID = 5768520360438197655L;

	public LogicaException() {
		super();
	}

	public LogicaException(String message) {
		super(message);
	}

	public LogicaException(String message, Throwable cause) {
		super(message, cause);
	}

}
