package fr.logica.db;

import java.util.Arrays;

public class ConnectionObject {
	private int id;
	private Object connection;
	private StackTraceElement[] stackTrace;

	public ConnectionObject(int idConn, Object conn, StackTraceElement[] stack) {
		this.id = idConn;
		this.connection = conn;
		if (stack != null) {
			this.stackTrace = Arrays.copyOf(stack, stack.length);
		}
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

	public void close() {
		if (connection instanceof DbConnection) {
			((DbConnection) connection).close();
		}
	}
}
