package fr.logica.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class StandaloneDbConnection {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(StandaloneDbConnection.class);
	public static final String CONTEXT_ROOT = "java:";
	public static final String DEFAULT_JNDI_ROOT = "jdbc/TOY";

	private Connection cnx;
	private Statement stm;

	public StandaloneDbConnection(String jdbcString, String user, String password) throws ClassNotFoundException {
		try {
			cnx = DriverManager.getConnection(jdbcString, user, password);
			cnx.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException("Connection failed.", e);
		}
	}

	public Connection getCnx() {
		return cnx;
	}

	public void close() {
		if (cnx != null) {
			try {
				cnx.close();
				cnx = null;
			} catch (SQLException e) {
				LOGGER.error("Error closing connection.", e);
			}
		}
	}

	public boolean execute(String sql) {
		if (stm == null) {
			try {
				stm = cnx.createStatement();
			} catch (SQLException e) {
				LOGGER.fatal("Error preparing statement.", e);
				throw new RuntimeException("Create Statement failed.", e);
			}
		}
		try {
			return !stm.execute(sql);
		} catch (SQLException e) {
			LOGGER.error("Error executing query.", e);
			return false; // Failsafe
		}
	}

	public void execute(String sql, String message) {
		if (execute(sql)) {
			LOGGER.info(message + ": ok");
		} else {
			LOGGER.warn("ERREUR " + message);
		}
	}

	/**
	 * commit de la transaction
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		this.getCnx().commit();
	}

	/**
	 * Rollback de la transaction
	 * 
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		this.getCnx().rollback();
	}
}
