package fr.logica.jsf.controller.fileUpload;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import fr.logica.business.FileContainer;

/**
 * This class represents a multipart request. It not only abstracts the
 * <code>javax.mail.Part</code> away, but it also provides direct access to the
 * <code>{@link MultipartMap}</code>, so that one can get the uploaded files out
 * of it.
 * 
 * @author BalusC
 */
public class MultipartRequest extends HttpServletRequestWrapper {

	// Vars
	// ---------------------------------------------------------------------------------------

	private MultipartMap multipartMap;

	// Constructors
	// -------------------------------------------------------------------------------

	/**
	 * Construct MultipartRequest based on the given HttpServletRequest.
	 * 
	 * @param request
	 *            HttpServletRequest to be wrapped into a MultipartRequest.
	 * @param location
	 *            The location to save uploaded files in.
	 * @throws IOException
	 *             If something fails at I/O level.
	 * @throws ServletException
	 *             If something fails at Servlet level.
	 */
	public MultipartRequest(HttpServletRequest request, String location) throws ServletException, IOException {
		super(request);
		this.multipartMap = new MultipartMap(request, location);
	}

	// Actions
	// ------------------------------------------------------------------------------------

	@Override
	public String getParameter(String name) {
		return multipartMap.getParameter(name);
	}

	@Override
	public String[] getParameterValues(String name) {
		return multipartMap.getParameterValues(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return multipartMap.getParameterNames();
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return multipartMap.getParameterMap();
	}

	/**
	 * @see MultipartMap#getFile(String)
	 */
	public FileContainer getFile(String name) {
		return multipartMap.getFile(name);
	}

}
