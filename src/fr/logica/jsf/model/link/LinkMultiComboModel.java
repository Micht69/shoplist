package fr.logica.jsf.model.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.logica.business.Action;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.data.ComboData;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;
import fr.logica.jsf.webflow.View;

public class LinkMultiComboModel extends DataModel {

	/** serialUID */
	private static final long serialVersionUID = -8974959372490165560L;

	/** Main entity */
	private Entity sourceEntity;

	/** Final entityName */
	private String entityName;

	/** Links between reference entity and multi combo deepest entity */
	private List<String> linkNames;

	/** Combo data for each link */
	private Map<String, ComboData> data;

	/** currently selected value (encoded Key) for each link */
	private Map<String, String> selectedValue;

	public LinkMultiComboModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String multiCombolinkName) {
		super(viewCtrl);
		this.sourceEntity = entity;
		this.entityName = entityName;

		this.linkNames = new ArrayList<String>();
		this.data = new HashMap<String, ComboData>();
		this.selectedValue = new HashMap<String, String>();

		for (String linkName : multiCombolinkName.split("#")) {
			linkNames.add(linkName);
			Key fk = sourceEntity.getForeignKey(linkName);
			if (fk.isFull()) {
				this.selectedValue.put(linkName, fk.getEncodedValue());
			}
		}

		// Build different sub-combos data structure
		loadData(viewCtrl.getContext());
	}

	@Override
	public void loadData(RequestContext context) {
		int nbLinks = linkNames.size();
		BusinessController bc = new BusinessController();
		Action action = viewCtrl.getCurrentView().getAction();

		// load root combo once and for all
		String rootLinkName = linkNames.get(nbLinks - 1);
		if (data.get(rootLinkName) == null) {
			data.put(rootLinkName, bc.getLinkComboData(sourceEntity, entityName, rootLinkName, null, action, context));
		}

		Key fk = sourceEntity.getForeignKey(linkNames.get(0));
		sourceEntity.setForeignKey(linkNames.get(0), null);
		for (int i = nbLinks - 1; i > 0; i--) {
			// load every combo when parent combo has a selected value
			String linkName = linkNames.get(i - 1);
			if (selectedValue.get(linkNames.get(i)) != null) {
				KeyModel fkModel = sourceEntity.getForeignKey(linkNames.get(i)).getModel();
				Key foreignKey = new Key(fkModel);
				foreignKey.setEncodedValue(selectedValue.get(linkNames.get(i)));
				sourceEntity.setForeignKey(linkNames.get(i), foreignKey);
				data.put(linkName, bc.getLinkComboData(sourceEntity, entityName, linkName, null, action, context));
			} else {
				data.put(linkName, null);
			}
		}
		sourceEntity.setForeignKey(linkNames.get(0), fk);

		if (linkNames.get(linkNames.size() - 1).equals(viewCtrl.getCurrentView().getLinkName())
				|| viewCtrl.getCurrentView().getAction().getUi() == UserInterface.READONLY) {
			readonly = true;
		} else {
			readonly = bc.isLinkProtected(sourceEntity, linkNames.get(linkNames.size() - 1), action, context);
		}
	}

	public void updateKey(String linkName) {
		// Clear all fields from foreign key
		sourceEntity.setForeignKey(linkNames.get(0), null);

		Key fk = null;
		KeyModel fkModel = sourceEntity.getForeignKey(linkName).getModel();
		if (selectedValue.get(linkName) != null) {
			fk = new Key(fkModel);
			fk.setEncodedValue(selectedValue.get(linkName));
		}
		// Set foreign key selected via combo
		sourceEntity.setForeignKey(linkName, fk);

		// After updating a combo, clear all sub-combos
		boolean parentChanged = false;
		for (int i = linkNames.size() - 1; i >= 0; i--) {
			if (parentChanged) {
				selectedValue.put(linkNames.get(i), null);
			}
			if (linkNames.get(i).equals(linkName)) {
				parentChanged = true;
			}
		}
		reload();
	}

	@Override
	public void validateView(View currentView) {
		// Nothing to do, entity is updated via updateKey method
	}

	/** Values displayed in comboboxes */
	public Map<String, String> getComboValues(String linkName) {
		// Combos are "reversed", labels are the keys and keys are values. This is the JSF2 way to ensure unicity of labels.
		Map<String, String> map = new LinkedHashMap<String, String>();
		// Handle labels that appear more than once
		Set<String> existingLabels = new HashSet<String>();
		if (data.get(linkName) != null) {
			for (Entry<Key, String> e : data.get(linkName).getComboValues().entrySet()) {
				if (map.containsKey(e.getValue())) {
					// This label already exists in map, flag it as a
					existingLabels.add(e.getValue());
					// Get existing label value
					String existingKey = map.get(e.getValue());
					// Build a new unique label
					String replacementLabel = e.getValue() + " (" + existingKey + ")";
					// Remove from map and put new unique label inside
					map.remove(e.getValue());
					map.put(replacementLabel, existingKey);
					// Add the new label
					String uniqueLabel = e.getValue() + " (" + e.getKey().getEncodedValue() + ")";
					map.put(uniqueLabel, e.getKey().getEncodedValue());
				} else if (existingLabels.contains(e.getValue())) {
					// Label already used, make it unique !
					String uniqueLabel = e.getValue() + " (" + e.getKey().getEncodedValue() + ")";
					map.put(uniqueLabel, e.getKey().getEncodedValue());
				} else {
					// Already unique label, just put it into the map
					map.put(e.getValue(), e.getKey().getEncodedValue());
				}
			}
		}
		return map;
	}

	public Map<String, String> getSelectedValue() {
		return selectedValue;
	}

}
