package fr.logica.ui;

import java.util.List;
import java.util.Map;

import fr.logica.business.Entity;

public class CustomPage extends Page {

	/** serialUID */
	private static final long serialVersionUID = -3092702853005518340L;

	private String url;

	private List<Message> messages;

	public CustomPage(String url) {
		this.url = url;
	}

	@Override
	public String getUrl() {
		if (url != null) {
			if (url.startsWith("/")) {
				return url;
			}
			return "/" + url;
		}
		return null;
	}

	@Override
	public String getDomainName() {
		return null;
	}

	@Override
	public Page getNextPage() {
		return null;
	}

	@Override
	public void setNextPage(Page page) {
	}

	@Override
	public void setTitle(String title) {

	}

	@Override
	public List<Message> getMessages() {
		return messages;
	}

	@Override
	public void setMessages(List messages) {
		this.messages = messages;
	}

	@Override
	public Entity getBean() {
		return null;
	}

	public void setBean(Entity bean) {
	}

	@Override
	public void updateLinks() {
	}

	@Override
	public void setAttachment(Object attachment) {
	}

	@Override
	public Object getAttachment() {
		return null;
	}

	@Override
	public Map getCustomData() {
		return null;
	}
}
