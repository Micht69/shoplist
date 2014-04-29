package fr.logica.jsf.model.var;

import java.io.Serializable;

import fr.logica.business.Entity;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;

public class VarModel extends DataModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 998471568930837379L;

	protected Entity entity;

	protected String varName;

	protected Object value;

	public VarModel(ViewController viewCtrl, Entity entity, String varName) {
		super(viewCtrl);
		this.entity = entity;
		this.varName = varName;
		if (entity != null) {
			this.value = entity.invokeGetter(varName);
		}
	}
}
