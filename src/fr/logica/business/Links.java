package fr.logica.business;

import java.util.HashMap;

public class Links extends HashMap<String, Link> {

	/** serialUID */
	private static final long serialVersionUID = 7400010665191645750L;

	private final EntityModel entityModel;

	private final boolean backRef;

	public Links(EntityModel mdl, boolean backRef) {
		this.entityModel = mdl;
		this.backRef = backRef;
	}

	@Override
	public Link get(Object key) {
		Link link = super.get(key);
		if (link == null) {
			if (!backRef && entityModel.getLinkNames().contains(key)) {
				link = new Link(entityModel.getLinkModel((String) key));
				put((String) key, link);
			} else if (backRef && entityModel.getBackRefNames().contains(key)) {
				link = new Link(entityModel.getBackRefModel((String) key));
				put((String) key, link);
			}
		}
		return link;
	}
}
