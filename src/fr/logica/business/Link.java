/**
 * 
 */
package fr.logica.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.logica.reflect.DomainUtils;
import fr.logica.ui.UiElement.Type;
import fr.logica.ui.UiLink;
import fr.logica.ui.UiScheduleLink;

/**
 * @author bellangerf
 * 
 */
public class Link implements Serializable {

	private static final long serialVersionUID = 2191902039469622194L;

	private final LinkModel model;

	private Entity entity;

	private Key key;

	/** Ui representation of this Link */
	private List<UiLink> templates = new ArrayList<UiLink>();

	public Link(LinkModel linkModel, Type t) {
		model = linkModel;
	}

	public Link(LinkModel linkModel) {
		model = linkModel;
	}

	/**
	 * Do not try to modify this.
	 * 
	 * @param parentLink
	 * @return
	 */
	public Map<String, String> getMultiComboValues(Link parentLink) {
		if (parentLink == null) {
			// Root link of the multi combos
			return getComboValues();
		}

		Key parentPrimaryKey = parentLink.getKey();
		if (parentPrimaryKey == null) {
			// No parent link selected, display nothing
			return new LinkedHashMap<String, String>();
		}

		// Do not touch this unless you've got many test cases...
		Key targetFkToParent = EntityManager.buildForeignKey(parentLink.getModel().getEntityName(), parentPrimaryKey, parentLink.getModel());
		Entity target = DomainUtils.newDomain(model.getEntityName());
		target.setForeignKey(parentLink.getModel().getKeyName(), targetFkToParent);
		Key childPartialPrimaryKey = new Key(model.getRefEntityName());
		childPartialPrimaryKey.setValue(target.getForeignKey(model.getKeyName()));

		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.COMBO) {
				return ui.getFilteredComboValues(childPartialPrimaryKey);
			}
		}
		return new LinkedHashMap<String, String>();

	}

	public Map<String, String> getComboValues() {
		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.COMBO) {
				return ui.getComboValues();
			}
		}
		return new LinkedHashMap<String, String>();
	}

	/**
	 * Renvoi la description de la valeur sélectionnée.
	 * 
	 * @return La description
	 */
	public String getReadValue() {
		String value = "";
		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.LINK || ui.getType() == fr.logica.ui.UiLink.Type.BACK_REF
					|| ui.getDescription() != null) {
				value = ui.getDescription();
			}
		}
		return value;
	}

	public void setReadValue(String readValue) {
		// Empty setter for refQuickSearch component (in JSF template).
	}

	public Entity getLinkedEntity() {
		return entity;
	}

	public Entity getEntity() {
		/*
		 * if (entity == null) { Entity e = DomainUtils.newDomain(model.getRefEntityName()); e.removeDefaultValues(); return e; }
		 */
		return entity;
	}

	public void setLinkedEntity(Entity linkedEntity) {
		this.entity = linkedEntity;
	}

	public void setEntity(Entity linkedEntity) {
		this.entity = linkedEntity;
		if (linkedEntity != null) {
			key = entity.getPrimaryKey();
		} else {
			key = null;
		}
	}

	public String getEncodedValue() {
		if (key != null) {
			return key.getEncodedValue();
		}
		return null;
	}

	public void setEncodedValue(String encodedValue) {
		if (encodedValue == null) {
			key = null;
			return;
		}
		if (key == null) {
			key = new Key(model.getRefEntityName());
		}
		key.setEncodedValue(encodedValue);
	}

	public void updateFromUi(Entity beanEntity) {
		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.COMBO) {
				beanEntity.setForeignKey(getModel().getKeyName(), key);
			}
			if (key == null) {
				ui.setDescription(null);
			}
		}
	}

	public Key getKey() {
		return key;
	}

	@Deprecated
	public void setLinkedKey(Key linkedKey) {
		this.key = linkedKey;
	}

	/**
	 * @return the results
	 */
	public Results results(String queryName) {
		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.LINK_LIST) {
				if (queryName != null && queryName.equals(ui.getQueryName())) {
					return ui.getResults();
				}
			}
		}
		return null;
	}

	public UiScheduleLink scheduleLink(String queryName) {
		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.LINK_LIST && ui instanceof UiScheduleLink) {
				if (queryName != null && queryName.equals(ui.getQueryName())) {
					return ((UiScheduleLink) ui);
				}
			}
		}
		return null;
	}

	public String getQueryName() {
		for (UiLink ui : templates) {
			if (ui.getType() == fr.logica.ui.UiLink.Type.LINK || ui.getType() == fr.logica.ui.UiLink.Type.BACK_REF) {
				return ui.getQueryName();
			}
		}
		return null;
	}

	public String getSearchQueryName() {
		for (UiLink ui : templates) {
			if (ui.getSearchQueryName() != null) {
				return ui.getSearchQueryName();
			}
		}
		return null;
	}

	public LinkModel getModel() {
		return model;
	}

	public List<UiLink> getTemplates() {
		return templates;
	}
}
