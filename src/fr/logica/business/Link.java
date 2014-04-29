/**
 * 
 */
package fr.logica.business;

import java.io.Serializable;

/**
 * @author bellangerf
 * 
 */
public class Link implements Serializable {

	private static final long serialVersionUID = 2191902039469622194L;

	private final LinkModel model;

	private Entity entity;

	private Key key;

	private boolean prepared;
	private boolean applyActionOnLink;

	public Link(LinkModel linkModel) {
		model = linkModel;
	}

	public Entity getEntity() {
		return entity;
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

	public Key getKey() {
		return key;
	}

	public LinkModel getModel() {
		return model;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}

	public boolean isApplyActionOnLink() {
		return applyActionOnLink;
	}

	public void setApplyActionOnLink(boolean applyActionOnLink) {
		this.applyActionOnLink = applyActionOnLink;
	}

}
