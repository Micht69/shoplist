package fr.logica.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.logica.db.DB;
import fr.logica.db.DbConnection;
import fr.logica.security.ApplicationUser;
import fr.logica.ui.Message;
import fr.logica.ui.Message.Severity;

public class Context implements Serializable {

	/** SerialUID */
	private static final long serialVersionUID = 6641644701381353915L;

	private final DbConnection connection;
	private final ApplicationUser user;
	private final List<Message> messages = new ArrayList<Message>();
	private Object attachment;

	public Context(ApplicationUser user) {
		this(DB.createDbConnection(), user);
	}

	public Context(DbConnection connection, ApplicationUser user) {
		this.connection = connection;
		this.user = user;
	}

	/**
	 * Rollback associated db connection & close it
	 */
	public void close() {
		getConnection().rollback();
		getConnection().close();
	}

	public List<Message> getMessages() {
		return messages;
	}

	/** Add a list of messages while changing their severity. */
	public void addMessagesAs(List<Message> newMessages, Severity severity) {
		for (Message msg : newMessages) {
			messages.add(new Message(msg.getMessage(), severity));
		}
	}

	public DbConnection getConnection() {
		return connection;
	}

	public ApplicationUser getUser() {
		return user;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}
}
