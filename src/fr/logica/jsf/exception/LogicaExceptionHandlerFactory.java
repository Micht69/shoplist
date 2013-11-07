package fr.logica.jsf.exception;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class LogicaExceptionHandlerFactory extends ExceptionHandlerFactory {

	/** {@link ExceptionHandlerFactory} d'origine encapsulé dans celle-ci. */
	private final ExceptionHandlerFactory parent;

	/**
	 * Constructeur public du {@link DosiapExceptionHandlerFactory}.
	 * 
	 * @param parent
	 *            {@link ExceptionHandlerFactory} fourni par l'implémentation
	 *            JSF, à encapsuler ici
	 */
	public LogicaExceptionHandlerFactory(final ExceptionHandlerFactory parent) {
		this.parent = parent;
	}

	@Override
	public ExceptionHandler getExceptionHandler() {
		ExceptionHandler exHandler = parent.getExceptionHandler();
		return new LogicaExceptionHandler(exHandler);
	}

}
