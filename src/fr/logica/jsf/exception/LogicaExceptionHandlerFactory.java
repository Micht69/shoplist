package fr.logica.jsf.exception;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class LogicaExceptionHandlerFactory extends ExceptionHandlerFactory {

	/** {@link ExceptionHandlerFactory} d'origine encapsul� dans celle-ci. */
	private final ExceptionHandlerFactory parent;

	/**
	 * Constructeur public
	 * 
	 * @param parent
	 *            {@link ExceptionHandlerFactory} fourni par l'impl�mentation JSF, � encapsuler ici
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
