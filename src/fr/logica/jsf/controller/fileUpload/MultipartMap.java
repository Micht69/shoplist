package fr.logica.jsf.controller.fileUpload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import fr.logica.business.FileContainer;

public class MultipartMap extends HashMap<String, Object> {

	/** serialUID */
	private static final long serialVersionUID = 1578326881508948774L;

	private static final Logger LOGGER = Logger.getLogger(MultipartMap.class);

	/** Attribute name */
	private static final String ATTRIBUTE_NAME = "parts";

	/** Default encoding */
	private static final String DEFAULT_ENCODING = "UTF-8";

	/** Buffer size (10kb) */
	private static final int DEFAULT_BUFFER_SIZE = 10240;

	/** Current encoding */
	private String encoding;

	/**
	 * Construct multipart map based on the given multipart request and file upload location. When the encoding is not specified in the given
	 * request, then it will default to <tt>UTF-8</tt>.
	 * 
	 * @param multipartRequest The multipart request to construct the multipart map for.
	 * @param location The location to save uploaded files in.
	 * @throws ServletException If something fails at Servlet level.
	 * @throws IOException If something fails at I/O level.
	 */
	@SuppressWarnings("unchecked")
	// ServletFileUpload#parseRequest() isn't parameterized.
	public MultipartMap(HttpServletRequest multipartRequest, String location) throws ServletException, IOException {
		multipartRequest.setAttribute(ATTRIBUTE_NAME, this);

		this.encoding = multipartRequest.getCharacterEncoding();
		if (this.encoding == null) {
			multipartRequest.setCharacterEncoding(this.encoding = DEFAULT_ENCODING);
		}

		/* Parse GET parameters in queryString */
		if (multipartRequest.getQueryString() != null) {
			processQueryString(multipartRequest.getQueryString());
		}

		/* Parse POST parameters in Multipart */
		try {
			List<FileItem> parts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(multipartRequest);
			for (FileItem part : parts) {
				if (part.isFormField()) {
					processFormField(part);
				} else if (!part.getName().isEmpty()) {
					processFileField(part);
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Parsing multipart/form-data request failed.", e);
		}
	}

	/**
	 * Query string is something like : <code>cid=12&foo=bar</code>. It does not start with '?'.
	 * 
	 * @param queryString
	 */
	private void processQueryString(String queryString) {
		if (queryString == null)
			return;

		String[] queryParams = queryString.split("&");
		for (String param : queryParams) {
			int separation = param.indexOf("=");
			if (separation >= 0) {
				String key = param.substring(0, separation);
				String value = param.substring(separation + 1);
				put(key, new String[] { value });
			} else {
				/* no '=' character in this param, so no value */
				put(param, new String[0]);
			}
		}
	}

	// Actions
	// ------------------------------------------------------------------------------------

	@Override
	public Object get(Object key) {
		Object value = super.get(key);
		if (value instanceof String[]) {
			String[] values = (String[]) value;
			return values.length == 1 ? values[0] : Arrays.asList(values);
		} else {
			return value; // Can be File or null.
		}
	}

	/**
	 * @see ServletRequest#getParameter(String)
	 */
	public String getParameter(String name) {
		Object value = super.get(name);
		if (value instanceof String[]) {
			String[] values = (String[]) value;
			return values[0];
		}
		return null;
	}

	/**
	 * @see ServletRequest#getParameterValues(String)
	 * @throws IllegalArgumentException If this field is actually a File field.
	 */
	public String[] getParameterValues(String name) {
		Object value = super.get(name);
		if (value instanceof String[]) {
			return (String[]) value;
		}
		return null;
	}

	/**
	 * @see ServletRequest#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(keySet());
	}

	/**
	 * @see ServletRequest#getParameterMap()
	 */
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (Entry<String, Object> entry : entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String[]) {
				map.put(entry.getKey(), (String[]) value);
			} else {
				map.put(entry.getKey(), new String[] { ((File) value).getName() });
			}
		}
		return map;
	}

	/**
	 * Returns uploaded file associated with given request parameter name.
	 * 
	 * @param name Request parameter name to return the associated uploaded file for.
	 * @return Uploaded file associated with given request parameter name.
	 * @throws IllegalArgumentException If this field is actually a Text field.
	 */
	public FileContainer getFile(String name) {
		Object value = super.get(name);
		if (value instanceof String[]) {
			throw new IllegalArgumentException("This is a Text field. Use #getParameter() instead.");
		}
		return (FileContainer) value;
	}

	// Helpers
	// ------------------------------------------------------------------------------------

	/**
	 * Process given part as Text part.
	 */
	private void processFormField(FileItem part) {
		String name = part.getFieldName();
		String[] values = (String[]) super.get(name);

		if (values == null) {
			// Not in parameter map yet, so add as new value.
			String str;

			try {
				str = part.getString(DEFAULT_ENCODING);
			} catch (UnsupportedEncodingException e) {
				str = part.getString();
			}
			put(name, new String[] { str });
		} else {
			// Multiple field values, so add new value to existing array.
			int length = values.length;
			String[] newValues = new String[length + 1];
			System.arraycopy(values, 0, newValues, 0, length);
			newValues[length] = part.getString();
			put(name, newValues);
		}
	}

	/**
	 * Process given part as File part which is to be saved in temp dir with the given filename.
	 */
	private void processFileField(FileItem part) throws IOException {
		FileContainer file = new FileContainer();
		file.setName(FilenameUtils.getName(part.getName()));

		InputStream input = null;

		try {
			input = new BufferedInputStream(part.getInputStream(), DEFAULT_BUFFER_SIZE);
			byte[] content = IOUtils.toByteArray(input);
			file.setContent(content);

		} catch (IOException e) {
			LOGGER.debug(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(input);
		}
		put(part.getFieldName(), file);
		part.delete(); // Cleanup temporary storage.
	}

}
