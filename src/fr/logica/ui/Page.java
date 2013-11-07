package fr.logica.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.logica.business.Entity;
import fr.logica.business.Link;

public abstract class Page<E extends Entity> implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 4659233893376807239L;

	private Map<String, String> jsFilterMap;
	private Map<String, String> tabsSelectedMap;

	private Page<?> nextPage;

	private String title;

	private boolean lastActionSuccess = true;

	private List<Message> messages = new ArrayList<Message>();

	private Object attachment;

	private Map<String, Object> customData;

	public abstract E getBean();

	private Map<String, UiAccess> access;

	/** Indique si la page est dans un état sale ou non. */
	private boolean dirty;

	/**
	 * Store javascript filter fields data
	 * 
	 * @return
	 */
	public Map<String, String> getJsFilterMap() {
		if (jsFilterMap == null) {
			jsFilterMap = new HashMap<String, String>();
		}
		return jsFilterMap;
	}

	public Map<String, String> getTabsSelectedMap() {
		if (tabsSelectedMap == null) {
			tabsSelectedMap = new HashMap<String, String>();
		}
		return tabsSelectedMap;
	}

	/** Update the current entity with user values */
	public void updateLinks() {
		updateLinks(getBean());
	}

	/** Update links recursively in inner templates */
	private void updateLinks(Entity e) {
		for (Link link : e.getLinks().values()) {
			link.updateFromUi(e);
			if (link.getEntity() != null) {
				if (!link.getEntity().getPrimaryKey().getEncodedValue().equals(e.getPrimaryKey().getEncodedValue())) {
					updateLinks(link.getEntity());
				}
			}
		}
	}

	public abstract String getUrl();

	public abstract String getDomainName();

	public Page<?> getNextPage() {
		return nextPage;
	}

	public void setNextPage(Page<?> page) {
		nextPage = page;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public Object getAttachment() {
		return attachment;
	}

	public Map<String, Object> getCustomData() {
		if (customData == null) {
			customData = new HashMap<String, Object>();
		}
		return customData;
	}

	public Map<String, UiAccess> getAccess() {
		return access;
	}

	public void setAccess(Map<String, UiAccess> access) {
		this.access = access;
	}

	public boolean isLastActionSuccess() {
		return lastActionSuccess;
	}

	public void setLastActionSuccess(boolean lastActionSuccess) {
		this.lastActionSuccess = lastActionSuccess;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}	

}
