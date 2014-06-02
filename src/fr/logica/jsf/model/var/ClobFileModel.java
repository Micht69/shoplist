package fr.logica.jsf.model.var;

import java.io.Serializable;
import java.util.Map;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ValueChangeEvent;

import fr.logica.business.Entity;
import fr.logica.business.FileContainer;
import fr.logica.jsf.controller.ViewController;

public class ClobFileModel extends FileModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 2455347317913279429L;

	public ClobFileModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String varName) {
		super(viewCtrl, store, entity, entityName, varName);
	}

	public void retrieveContent(ComponentSystemEvent event) {
		if (isHasFile()) {
			downloadFile();
		}
	}

	@Override
	protected void postDownload(FileContainer file) {

		if (null != file && null != file.getContent()) {
			entity.invokeSetter(varName + "Container", file);
			container = file;
		}
	}

	public void valueChanged(ValueChangeEvent event) {
		if (event.getNewValue() == null || ((String) event.getNewValue()).isEmpty()) {
			deleteFile();
		}
	}

}
