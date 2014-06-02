package fr.logica.jsf.model.var;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fr.logica.business.Entity;
import fr.logica.business.FileContainer;
import fr.logica.jsf.controller.ViewController;

public class FileUploadModel extends FileModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -8810681580833532186L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(FileUploadModel.class);

	public FileUploadModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String varName) {
		super(viewCtrl, store, entity, entityName, varName);
	}

	@Override
	protected void postDownload(FileContainer file) {

		if (null != file && null != file.getContent()) {
			OutputStream out = null;

			try {
				FacesContext fc = FacesContext.getCurrentInstance();
				HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
				response.setContentLength(file.getContent().length);
				response.setContentType(file.getContentType());
				String fileName = file.getRealName();
				if (fileName == null) {
					// FIXME : Handle mimetype
					fileName = varName;
				}
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				response.addHeader("Cache-Control", "public");

				out = response.getOutputStream();
				out.write(file.getContent());
				out.flush();
				out.close();
				fc.responseComplete();

			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);

			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
	}

}
