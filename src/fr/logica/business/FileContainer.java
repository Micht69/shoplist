package fr.logica.business;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * File object.
 */
public class FileContainer implements Serializable {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Logger. */
	private static final transient Logger LOGGER = Logger.getLogger(FileContainer.class);

	/** File name. */
	private String name;

	/** File content. */
	private byte[] content;

	/** Is the content null ? (not link to content size to avoid loading content if not needed) */
	private boolean isNull;

	public String getName() {
		return (name != null) ? name : "untitled";
	}

	public String getRealName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] data) {
		if (data == null) {
			this.content = null;
		} else {
			this.content = Arrays.copyOf(data, data.length);
		}
	}

	public boolean isNull() {
		return isNull;
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	/**
	 * Returns the content type of the file (from file extension).
	 * 
	 * @return Content type of the file or {@code application/octet-stream} if the file name is {@code null}.
	 */
	public String getContentType() {

		if (null != name) {
			MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();
			return mimeTypes.getContentType(name);
		}
		return "application/octet-stream";
	}

	public static void createTempFile(FileContainer container) {
		String filename = FilenameUtils.getName(container.getName());

		File file = null;

		try {
			file = File.createTempFile(filename, null);

		} catch (IOException ioEx) {
			LOGGER.error("Error while writting file " + container.getName(), ioEx);
		}

		if (file != null && container.getContent() != null) {

			try {
				FileUtils.writeByteArrayToFile(file, container.getContent());

			} catch (IOException ioEx) {
				LOGGER.error("Error while writting file " + container.getName(), ioEx);
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(content);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FileContainer other = (FileContainer) obj;
		if (!Arrays.equals(content, other.content)) {
			return false;
		}
		if (isNull != other.isNull) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
