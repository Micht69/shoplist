/**
 * 
 */
package fr.logica.jsf.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.sun.faces.util.Util;

import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.jsf.controller.SessionController;
import fr.logica.reflect.DomainUtils;

/**
 * @author zuberl
 * 
 */
public class ImageLinkResource extends Resource {

	/** Mapped characters in resource names */
	private static final HashMap<Character, Character> mappedCars = new HashMap<Character, Character>();

	/** Builds a resource unique ID */
	public static String getResourceId(String entityName, String encodedKey, String varName) {
		if (mappedCars.size() == 0) {
			mappedCars.put('é', 'e');
			mappedCars.put('è', 'e');
			mappedCars.put('ê', 'e');
			mappedCars.put('ë', 'e');
			mappedCars.put('é', 'e');
			mappedCars.put('à', 'a');
			mappedCars.put('ä', 'a');
			mappedCars.put('â', 'a');
			mappedCars.put('ï', 'i');
			mappedCars.put('î', 'i');
			mappedCars.put('ô', 'o');
			mappedCars.put('ö', 'o');
		}
		String id = "ImageLink:" + entityName + "/" + encodedKey.replace(":::", "=").replace(";;;", "&") + "/" + varName;
		for (Entry<Character, Character> e : mappedCars.entrySet()) {
			id = id.replace(e.getKey(), e.getValue());
		}
		return id;
	}


	private final String mediaId;

	public ImageLinkResource(final String mediaId) {
		setLibraryName("ImageLink");
		setResourceName(mediaId);
		setContentType("image/png");
		this.mediaId = mediaId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.Resource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		ByteArrayInputStream result = null;
		if (mediaId != null) {
			String[] mediaParts = mediaId.split("/");
			if (mediaId.startsWith("ImageLink:")) {
				mediaParts = mediaId.substring("ImageLink:".length()).split("/");
			}
			String entityName = mediaParts[0];
			String jsfEncodedKey = mediaParts[1];
			String varName = mediaParts[2];

			byte[] content = null;
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

			Entity e = DomainUtils.newDomain(entityName);
			Key pk = new Key(entityName);
			pk.setEncodedValue(jsfEncodedKey.replace("=", ":::").replace("&", ";;;"));
			e.setPrimaryKey(pk);
			SessionController sessionCtrl = (SessionController) request.getSession().getAttribute("sessionCtrl");

			RequestContext ctx = null;
			try {
				ctx = new RequestContext(sessionCtrl.getContext());
				content = DB.createDbEntity().getLobContent(ctx, e, varName);
			} finally {
				if (ctx != null) {
					ctx.close();
				}
			}

			if (content != null) {
				result = new ByteArrayInputStream(content);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.Resource#getRequestPath()
	 */
	@Override
	public String getRequestPath() {
		final FacesContext context = FacesContext.getCurrentInstance();
		return context
				.getApplication()
				.getViewHandler()
				.getResourceURL(
						context,
						ResourceHandler.RESOURCE_IDENTIFIER + "/" + mediaId + Util.getFacesMapping(context)
								+ "?ln=" + "ImageLink");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.Resource#getResponseHeaders()
	 */
	@Override
	public Map<String, String> getResponseHeaders() {
		return new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.Resource#getURL()
	 */
	@Override
	public URL getURL() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.Resource#userAgentNeedsUpdate(javax.faces.context .FacesContext)
	 */
	@Override
	public boolean userAgentNeedsUpdate(FacesContext arg0) {
		return true;
	}

}
