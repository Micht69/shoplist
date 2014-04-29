package fr.logica.business.context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.logica.business.Constants;
import fr.logica.db.DB;
import fr.logica.db.DbConnection;
import fr.logica.ui.Message;

public class RequestContext implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 8336236497355320836L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(RequestContext.class);

	/** Messages (information and errors). This is a LinkedHashSet because we want to keep the message order, but they should be unique. */
	private final Set<Message> messages = new LinkedHashSet<Message>();

	/** Request attributes. This may be used to store anything during request */
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	/** Session context. This is the "parent" context. */
	private SessionContext sessionContext;

	/** Database connection. Lazy opening. */
	private DbConnection dbConnection;

	/** File attached to the Request. If a File object goes up to the UI layer of the application, it should start a download of this file. */
	private File attachment = null;
	/** Attachement name is the filename to use if there's a file to download */
	private String attachmentName = null;

	/**
	 * Creates a new Request Context linked to a Session Context.
	 * 
	 * @param sessionContext Current session in which we launched a request.
	 */
	public RequestContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public void close() {
		if (dbConnection != null) {
			dbConnection.close();
		}
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	/**
	 * Lazy opening on DbConnection
	 * 
	 * @return
	 */
	public DbConnection getDbConnection() {
		if (dbConnection == null) {
			dbConnection = DB.createDbConnection();
		}
		return dbConnection;
	}

	public void setDbConnection(DbConnection dbConnection) {
		this.dbConnection = dbConnection;
	}

	public Set<Message> getMessages() {
		return messages;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void removeCustomData(String key) {
		String newKey = Constants.CUSTOM_DATA + key;
		attributes.remove(newKey);
	}

	public void putCustomData(String key, Object data) {
		String newKey = Constants.CUSTOM_DATA + key;
		attributes.put(newKey, data);
	}

	public void setAttachment(File file) {
		this.attachment = file;
	}

	public void setAttachment(byte[] data, String filename) {
		if (filename == null) {
			filename = "tmpAttachment";
		}
		setAttachmentName(filename);
		FileOutputStream out;
		try {
			attachment = File.createTempFile(filename, ".tmp");
			out = new FileOutputStream(attachment);
			out.write(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public File getAttachment() {
		return attachment;
	}

	/**
	 * Return an unmodifiable map with custom data, to modify it use putCustomData() or removeCustomData()
	 * 
	 * @return
	 */
	public Map<String, Object> getCustomData() {
		Map<String, Object> customAttributes = new HashMap<String, Object>();
		for (String key : attributes.keySet()) {
			if (key.startsWith(Constants.CUSTOM_DATA)) {
				customAttributes.put(key, attributes.get(key));
			}
		}
		customAttributes = Collections.unmodifiableMap(customAttributes);
		return customAttributes;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

}
