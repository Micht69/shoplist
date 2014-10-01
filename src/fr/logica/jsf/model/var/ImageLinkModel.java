package fr.logica.jsf.model.var;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.logica.business.Entity;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.utils.ImageLinkResource;

public class ImageLinkModel extends FileUploadModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -8810681580833532186L;

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(ImageLinkModel.class);

	public ImageLinkModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String varName) {
		super(viewCtrl, store, entity, entityName, varName);
	}

	public String getResourceId() {
		return ImageLinkResource.getResourceId(entity.name(), entity.getPrimaryKey().getEncodedValue(), varName);
	}
}
