package fr.logica.business;

import java.util.ArrayList;
import java.util.List;

import fr.logica.ui.Message;

public class FunctionalException extends LogicaException {

	private static final long serialVersionUID = -4392654603736653249L;

	private List<Message> messages = new ArrayList<Message>();

	@Override
	public String getMessage() {
		if (messages.size() > 0) {
			return messages.get(0).getMessage();
		}
		return super.getMessage();
	}

	public List<Message> getMessages() {
		return messages;
	}

	public FunctionalException(List<Message> errors) {
		super();
		messages.addAll(errors);
	}

	public FunctionalException(Message m) {
		super();
		messages.add(m);
	}

}
