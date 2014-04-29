package fr.logica.jsf.model.var;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fr.logica.business.Action;
import fr.logica.business.Entity;
import fr.logica.business.FileContainer;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;

public class FileUploadModel extends VarModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -8810681580833532186L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(FileUploadModel.class);

	protected FileContainer container;

	public FileUploadModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String varName) {
		super(viewCtrl, entity, varName);
		this.container = (FileContainer) entity.invokeGetter(varName + "Container");
	}

	public boolean isHasFile() {
		return container != null && !container.isNull();
	}

	public void downloadFile() {
		OutputStream out = null;
		RequestContext context = new RequestContext(viewCtrl.getSessionCtrl().getContext());
		try {
			Action action = viewCtrl.getCurrentView().getAction();

			FileContainer file = new BusinessController().getFile(entity, varName, action, context);

			if (null != file && null != file.getContent()) {
				FacesContext fc = FacesContext.getCurrentInstance();
				HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

				response.setContentLength(file.getContent().length);
				response.setContentType(file.getContentType());
				String fileName = file.getRealName();
				if (fileName == null) {
					// FIXME : Handle mimetype
					fileName = varName + ".png";
				}
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				response.addHeader("Cache-Control", "public");

				out = response.getOutputStream();
				out.write(file.getContent());
				out.flush();
				out.close();
				fc.responseComplete();
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				context.close();
			} catch (RuntimeException rex) {
				LOGGER.error(rex);
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	/**
	 * Empty current container
	 */
	public void deleteFile() {
		container.setContent(null);
		container.setNull(true);
		container.setName(null);
	}
}
