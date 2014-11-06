package fr.logica.jsf.model.link;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

import fr.logica.business.Action.Input;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.data.ComboData;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.DataModel;
import fr.logica.jsf.webflow.View;

public class LinkComboModel extends DataModel {

	/** serialUID */
	private static final long serialVersionUID = -8974959372490165560L;

	/** Main entity */
	private Entity sourceEntity;

	private String entityName;

	/** Link between reference entity and combo entity */
	private String linkName;

	/** Filter used to load combo elements */
	private String filterName;

	/** Combo main entity name */
	private ComboData data;

	/** currently selected value (encoded Key) */
	private String selectedValue;

	private Boolean isCriteriaTemplate = Boolean.FALSE;

	protected String SELECTED_VALUE = "encodedValue";

	public LinkComboModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String linkName,
			String filterName) {
		super(viewCtrl);
		this.sourceEntity = entity;
		this.entityName = entityName;
		this.filterName = filterName;
		this.linkName = linkName;
		if (viewCtrl.getCurrentView().getAction().getInput() == Input.QUERY) {
			isCriteriaTemplate = Boolean.TRUE;
		}
		if (store.get(SELECTED_VALUE) != null) {
			sourceEntity.setForeignKey(sourceEntity.getModel().getLinkModel(linkName).getLinkName(),
					new Key(entityName, store.get(SELECTED_VALUE)));
		}
		loadData(viewCtrl.getContext());
	}

	@Override
	public void loadData(RequestContext context) {
		BusinessController bc = new BusinessController();
		this.data = bc.getLinkComboData(sourceEntity, entityName, linkName, filterName, viewCtrl.getCurrentView()
				.getAction(),
				context);
		Key foreignKey = sourceEntity.getForeignKey(linkName);
		if (foreignKey.isFull()) {
			this.selectedValue = foreignKey.getEncodedValue();
		} else {
			this.selectedValue = null;
		}
		if (linkName.equals(viewCtrl.getCurrentView().getLinkName()) || viewCtrl.getCurrentView().getAction().getUi() == UserInterface.READONLY) {
			readonly = true;
		} else {
			readonly = bc.isLinkProtected(sourceEntity, linkName, viewCtrl.getCurrentView().getAction(), context);
		}
	}

	@Override
	public void validateView(View currentView) {
		if (!readonly) {
			Key oldForeignKey = sourceEntity.getForeignKey(linkName);
			Key newForeignKey = new Key(entityName, selectedValue);
			if (!newForeignKey.isNull() || oldForeignKey.isFull()) {
				// We override when newForeignKey is not null OR when old foreign key was full. 
				// We won't override if combo selects nothing and old foreign key was not full 
				// so we won't remove partial key info
				sourceEntity.setForeignKey(linkName, newForeignKey);
			}
		}
	}

	@Override
	public void storeViewData(Map<String, String> store) {
		if (selectedValue != null) {
			store.put(SELECTED_VALUE, selectedValue);
		}
	}

	/** Values displayed in the combobox */
	public Map<String, String> getComboValues() {
		// Combos are "reversed", labels are the keys and keys are values. This is the JSF2 way to ensure unicity of labels.
		Map<String, String> map = new LinkedHashMap<String, String>();
		// Handle labels that appear more than once
		Set<String> existingLabels = new HashSet<String>();
		if (data != null) {
			String mapKey;
			for (Entry<Key, String> e : data.getComboValues().entrySet()) {
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
					mapKey = uniqueLabel;
				} else if (existingLabels.contains(e.getValue())) {
					// Label already used, make it unique !
					String uniqueLabel = e.getValue() + " (" + e.getKey().getEncodedValue() + ")";
					mapKey = uniqueLabel;
				} else {
					// Already unique label, just put it into the map
					mapKey = e.getValue();
				}
				
				map.put(StringEscapeUtils.escapeHtml4(mapKey), e.getKey().getEncodedValue());
			}
		}
		return map;
	}

	public String getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(String selectedValue) {
		this.selectedValue = selectedValue;
		if (isCriteriaTemplate) {
			sourceEntity.setForeignKey(linkName, new Key(entityName, selectedValue));
		}
	}

}
