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

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.sun.faces.util.Util;

import fr.logica.business.Context;
import fr.logica.business.FileContainer;
import fr.logica.db.DB;
import fr.logica.jsf.controller.JSFController;

/**
 * @author zuberl
 * 
 */
public class ImageLinkResource extends Resource {

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
			byte[] content = null;
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			JSFController bean = (JSFController) request.getSession().getAttribute("jsfCtrl");
			fr.logica.business.FileContainer container = (FileContainer) bean.getEntity().invokeGetter(mediaId + "Container");

			if (!container.isNull()) {
				Context ctx = null;
				try {
					ctx = new Context(bean.getSessionCtrl().getUser());
					content = DB.createDbEntity().getLobContent(ctx, bean.getEntity(), mediaId);
					bean.getEntity().invokeSetter(mediaId, content);
					container.setContent(content);
				} finally {
					if (ctx != null) {
						ctx.close();
					}
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
