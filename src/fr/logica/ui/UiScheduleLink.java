package fr.logica.ui;

import java.util.ArrayList;
import java.util.List;

import fr.logica.business.Key;
import fr.logica.jsf.components.schedule.ScheduleModel;

public class UiScheduleLink extends UiLink {

	private final ScheduleModel model;
	private final String entityName;

	public UiScheduleLink(Type type, String entityName) {
		super(type);
		model = new ScheduleModel();
		this.entityName = entityName; 
	}

	public ScheduleModel getModel() {
		return model;
	}

	public List<Key> getSelectedPk() {
		List<Key> keys = new ArrayList<Key>();
		Key key = new Key(entityName, model.getSelectedEvent().getId());
		keys.add(key);
		return keys;
	}

	public String getEntityName() {
		return entityName;
	}

}
