package fr.logica.jsf.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import fr.logica.business.DateUtils;

public class LogicaResource extends ResourceWrapper {

	private final Resource resource;

	public LogicaResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getWrapped() {
		return this.resource;
	}

	@Override
	public boolean userAgentNeedsUpdate(FacesContext context) {
		return true;
	}

	@Override
	public Map<String, String> getResponseHeaders() {
		Map<String, String> result = new HashMap<String, String>(6, 1.0f);
		result.put("Last-Modified", DateUtils.formatDate(new Date()));
		return result;
	}

	@Override
	public URL getURL() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		StringBuilder buf = new StringBuilder(context.getRequestServletPath());
		// buf.append(context.getRequestServerName());
		// if (context.getRequestServerPort() != 80 && context.getRequestServerPort() != 443) {
		// buf.append(":").append(context.getRequestServerPort());
		// }
		buf.append(getRequestPath());
		URL url = null;
		try {
			url = new URL(buf.toString());
		} catch (java.net.MalformedURLException e) {
			// log.error("Unable to create URL for ProgramResource " + this.toString(), e);
		}
		return url;
	}

	@Override
	public String getRequestPath() {
		StringBuilder buf = new StringBuilder(FacesContext.getCurrentInstance().getExternalContext()
				.getRequestContextPath());
		buf.append(ResourceHandler.RESOURCE_IDENTIFIER);
		buf.append("/").append("chart").append(".jsf?ln=").append("epsilon");
		return buf.toString();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
		ByteArrayOutputStream os = (ByteArrayOutputStream) requestMap.get("chart");
		if (os != null) {
			return new ByteArrayInputStream(os.toByteArray());
		}
		return super.getInputStream();
	}
}
