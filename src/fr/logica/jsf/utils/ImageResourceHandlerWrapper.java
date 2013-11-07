/**
 * 
 */
package fr.logica.jsf.utils;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;

/**
 * @author zuberl
 * 
 */
public class ImageResourceHandlerWrapper extends ResourceHandlerWrapper {

	private ResourceHandler wrapped;

	public ImageResourceHandlerWrapper(ResourceHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ResourceHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public Resource createResource(final String resourceName,
			final String libraryName) {
		if ("ImageLink".equals(libraryName)) {
			return new ImageLinkResource(resourceName);
		} else {
			return super.createResource(resourceName, libraryName);
		}
	}

	/**
	 * @see javax.faces.application.ResourceHandlerWrapper#libraryExists(java.lang.String)
	 */
	@Override
	public boolean libraryExists(final String libraryName) {
		if ("ImageLink".equals(libraryName)) {
			return true;
		} else {
			return super.libraryExists(libraryName);
		}
	}


}
