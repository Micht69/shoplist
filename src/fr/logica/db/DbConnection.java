package fr.logica.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import fr.logica.business.MessageUtils;
import fr.logica.business.TechnicalException;

public class DbConnection {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(DbConnection.class);

	public enum Type {
		ORACLE, MySQL, PostgreSQL, DB2
	}

	public static final String CONTEXT_ROOT = "java:comp/env";
	public static final String DATASOURCE = "datasource";
	protected DataSource dataSource = null;

	protected Connection cnx;

	private static Type dbType;

	/** Unique id of the connection (for the ConnectionLogger). */
	private int id;

	public DbConnection() {
		try {
			Context context = (Context) new InitialContext().lookup(CONTEXT_ROOT);
			dataSource = (DataSource) context.lookup(MessageUtils.getServerProperty(DATASOURCE));
			cnx = dataSource.getConnection();
			cnx.setAutoCommit(false);

			// Register the connection's opening.
			id = ConnectionLogger.getInstance().register(this);
		} catch (SQLException e) {
			LOGGER.fatal("Connection failed.", e);
			throw new DbException("Connection failed.", e);
		} catch (NamingException e) {
			LOGGER.fatal("Datasource not found", e);
			throw new Error("ERROR: Datasource not found: " + e, e);
		}
	}

	private static synchronized void initializeDbType(Connection cnx) {
		DbConnection localDbConnection = null;
		Connection localConnection = null;
		if (cnx != null) {
			localConnection = cnx;
		} else {
			localDbConnection = new DbConnection();
			localConnection = localDbConnection.getCnx();
		}
		try {
			String driverName = null;
			driverName = localConnection.getMetaData().getDriverName().toUpperCase();
			if (driverName.contains("ORACLE")) {
				DbConnection.dbType = Type.ORACLE;
			} else if (driverName.contains("MYSQL")) {
				DbConnection.dbType = Type.MySQL;
			} else if (driverName.contains("POSTGRESQL")) {
				DbConnection.dbType = Type.PostgreSQL;
			} else if (driverName.contains("DB2") || driverName.contains("IBM")) {
				DbConnection.dbType = Type.DB2;
			} else {
				throw new TechnicalException("Driver type not supported : " + driverName);
			}
		} catch (SQLException e) {
			LOGGER.fatal("Connection failed.", e);
			throw new DbException("Connection failed.", e);
		} finally {
			if (localDbConnection != null) {
				localDbConnection.close();
			}
		}

	}

	public static synchronized DbConnection.Type getDbType() {
		if (dbType == null) {
			initializeDbType(null);
		}
		return dbType;
	}

	public DbConnection(StandaloneDbConnection standalone) {
		cnx = standalone.getCnx();
		if (dbType == null) {
			initializeDbType(cnx);
		}
	}

	public DbConnection(Connection connection) {
		cnx = connection;
	}

	public Connection getCnx() {
		return cnx;
	}

	public void close() throws DbException {
		if (cnx != null) {
			try {
				rollback();
			} finally {
				try {
					cnx.close();
					cnx = null;
					ConnectionLogger.getInstance().drop(id);
				} catch (SQLException e) {
					LOGGER.error("Error closing connection.", e);
					throw new DbException(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * commit de la transaction
	 * 
	 * @throws SQLException
	 */
	public void commit() throws DbException {
		try {
			this.getCnx().commit();
		} catch (SQLException e) {
			LOGGER.error("Error committing connection.", e);
			throw new DbException(e.getMessage(), e);
		}
	}

	/**
	 * Rollback de la transaction
	 * 
	 * @throws SQLException
	 */
	public void rollback() throws DbException {
		try {
			this.getCnx().rollback();
		} catch (SQLException e) {
			LOGGER.error("Error rollbacking connection.", e);
			throw new DbException(e.getMessage(), e);
		}
	}
}
