package fr.logica.db;

public class ConnectionObject {
	private int id;
	private Object connection;
	private StackTraceElement[] stackTrace;

	public ConnectionObject(int idConn, Object conn, StackTraceElement[] stack) {
		this.id = idConn;
		this.connection = conn;
		this.stackTrace = stack;
	}

	public int getId() {
		return this.id;
	}

	public StackTraceElement[] getStackTrace() {
		return this.stackTrace;
	}

	public Class<?> getConnectionClass() {
		return this.connection.getClass();
	}

	public Object getConnection() {
		return this.connection;
	}
}