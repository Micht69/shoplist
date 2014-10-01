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
	private transient DbConnection dbConnection;

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
			dbConnection = null; 
		}
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	/**
	 * @return true if a DbConnection object is associated to this request
	 */
	public boolean hasDbConnection() {
		return dbConnection != null;
	}

	/**
	 * Always return a DbConnection. If needed, a new DbConnection is created.
	 * 
	 * @return never null
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

	/**
	 * Removes a user custom data from current request context
	 * @param key	Unique identifier for custom data
	 */
	public void removeCustomData(String key) {
		String newKey = Constants.CUSTOM_DATA + key;
		attributes.remove(newKey);
	}

	/**
	 * Stores a user custom data inside current context. If this request's response has a User Interface, this custom data will be dump into final HTML DOM in the custom data div. <br/><br/>
	 * <pre>
	 * Example: 
	 * - When Domain Logic method uiActionOnLoad() adds custom data: 
	 * 		context.putCustomData("color", "blue")
	 * - Final DOM will contain a div like this: 
	 * 		&lt;div style="display: none;"&gt;
	 * 			&lt;div id="cData_key_color"&gt;blue&lt;/div&gt;
	 * 		&lt;/div&gt;
	 * </pre>
	 * Request Context will add "cData_key_" prefix to all stored custom data. 
	 * @param key	Unique identifier for custom data
	 * @param data  Custom data to store inside context
	 */
	public void putCustomData(String key, Object data) {
		String newKey = Constants.CUSTOM_DATA + key;
		attributes.put(newKey, data);
	}
	
	/**
	 * Gets a user stored custom data inside current context. 
	 * @param key	Unique identifier for custom data
	 * @return Custom data stored inside context
	 */
	public Object getCustomData(String key) {
		return attributes.get(Constants.CUSTOM_DATA + key); 
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
	 * Return an <b>unmodifiable</b> map with custom data to modify it use <br/>
	 * {@link #putCustomData(String, Object)} or {@link #removeCustomData(String)}
	 * 
	 * @return an unmodifiable map with custom data
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
