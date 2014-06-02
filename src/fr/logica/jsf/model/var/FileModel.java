package fr.logica.jsf.model.var;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.logica.business.Action;
import fr.logica.business.Entity;
import fr.logica.business.FileContainer;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;

public class FileModel extends VarModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6507861309611216298L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(FileModel.class);

	protected FileContainer container;

	public FileModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String varName) {
		super(viewCtrl, entity, varName);
		this.container = (FileContainer) entity.invokeGetter(varName + "Container");
	}

	public boolean isHasFile() {
		return container != null && !container.isNull();
	}

	public void downloadFile() {
		RequestContext context = new RequestContext(viewCtrl.getSessionCtrl().getContext());

		try {
			Action action = viewCtrl.getCurrentView().getAction();
			FileContainer file = new BusinessController().getFile(entity, varName, action, context);
			postDownload(file);

		} finally {
			try {
				context.close();
			} catch (RuntimeException rex) {
				LOGGER.error(rex);
			}
		}
	}

	protected void postDownload(FileContainer file) {
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
