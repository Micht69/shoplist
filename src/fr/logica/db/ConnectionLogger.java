package fr.logica.db;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Log all Db Connections.
 */
public class ConnectionLogger {

	/** ArrayList who save all connections opened. */
	private Map<Integer, ConnectionObject> mapConnections = new HashMap<Integer, ConnectionObject>();

	/** Next Connection id. */
	private int nextId = 1;

	/** Private constructor. */
	private ConnectionLogger() {
	}

	/** Unique instance of the class. */
	private static ConnectionLogger INSTANCE = new ConnectionLogger();

	/** Entry point to the unique instance of the class. */
	public static ConnectionLogger getInstance() {
		return INSTANCE;
	}

	/**
	 * Register a new DB connection and save the stackTrace's five first line.
	 * 
	 * @return An unique id for the connection.
	 */
	public int register(DbConnection conn) {
		int id = nextId++;
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		ConnectionObject c = new ConnectionObject(id, conn, Arrays.copyOfRange(stackTrace, 1, stackTrace.length - 1));
		mapConnections.put(id, c);
		return id;
	}

	/**
	 * Drop an already registered connection.
	 * 
	 * @param id
	 *            The unique id returned by the register method.
	 */
	public void drop(int id) {
		mapConnections.remove(id);
	}

	/**
	 * Map all opened connections.
	 */
	public Map<Integer, ConnectionObject> getOpenedConnections() {
		return mapConnections;
	}

	/**
	 * Close a connection
	 * 
	 * @param id
	 *            The connection's id.
	 */
	public void closeConnection(int id) {
		if (mapConnections.containsKey(id)) {
			ConnectionObject conn = mapConnections.get(id);
			if (conn != null) {
				if (conn.getConnection() instanceof DbConnection) {
					((DbConnection) conn.getConnection()).close();
				}
			}
		}
	}
}
