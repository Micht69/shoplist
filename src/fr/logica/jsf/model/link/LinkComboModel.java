package fr.logica.jsf.model.link;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	private String keyName;

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
		this.keyName = entity.getModel().getLinkModel(linkName).getKeyName();
		if (viewCtrl.getCurrentView().getAction().getInput() == Input.QUERY) {
			isCriteriaTemplate = Boolean.TRUE;
		}
		if (store.get(SELECTED_VALUE) != null) {
			sourceEntity.setForeignKey(sourceEntity.getModel().getLinkModel(linkName).getKeyName(),
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
		Key foreignKey = sourceEntity.getForeignKey(keyName);
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
			sourceEntity.setForeignKey(keyName, new Key(entityName, selectedValue));
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
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (data != null) {
			for (Entry<Key, String> e : data.getComboValues().entrySet()) {
				map.put(e.getValue(), e.getKey().getEncodedValue());
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
			sourceEntity.setForeignKey(keyName, new Key(entityName, selectedValue));
		}
	}

}
