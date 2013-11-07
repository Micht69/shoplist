package fr.logica.business;

import java.util.HashMap;

import fr.logica.ui.UiElement.Type;

public class Links extends HashMap<String, Link> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7400010665191645750L;

	private final EntityModel entityModel;

	public Links(EntityModel mdl) {
		entityModel = mdl;
	}

	@Override
	public Link get(Object key) {
		Link link = super.get(key);
		if (link == null) {
			if (entityModel.getLinkNames().contains(key)) {
				link = new Link(entityModel.getLinkModel((String) key), Type.LINK);
				put((String) key, link);
			} else if (entityModel.getBackRefNames().contains(key)) {
				link = new Link(entityModel.getBackRefModel((String) key), Type.BACK_REF_LIST);
				put((String) key, link);
			}
		}
		return link;
	}
}
