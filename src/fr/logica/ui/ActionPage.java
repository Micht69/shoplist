package fr.logica.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.Entity;
import fr.logica.business.Key;

public class ActionPage<E extends Entity> extends Page<E> {

	/** serialUID */
	private static final long serialVersionUID = -2177422829993546527L;

	private Action action;

	private List<Key> keyList;

	private String domainName;

	private E bean;

	private String linkName;

	private String customUrl;

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public List<Key> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<Key> keyList) {
		this.keyList = keyList;
	}

	@Override
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	@Override
	public E getBean() {
		return bean;
	}

	public void setBean(E bean) {
		this.bean = bean;
	}

	@Override
	public String getUrl() {
		return "/" + domainName + "/" + getPageName();
	}

	public String getPageName() {

		if (null == customUrl) {
			return UiManager.getActionPage(domainName, action.code);
		}
		return customUrl;
	}

	/**
	 * This method clones the current page WITHOUT the bean. Cloned page shall have a null bean.
	 * 
	 * @return the cloned page, with a null bean.
	 */
	@Override
	public ActionPage<E> clone() {
		ActionPage<E> actionPage = new ActionPage<E>();
		actionPage.setAction(new Action(action.code, action.type));
		List<Key> newKeyList = new ArrayList<Key>();
		if (keyList != null) {
			newKeyList.addAll(keyList);
		}
		actionPage.setKeyList(newKeyList);
		actionPage.setDomainName(domainName);
		actionPage.setNextPage(getNextPage());
		actionPage.setLinkName(linkName);
		return actionPage;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setCustomUrl(String customUrl) {
		this.customUrl = customUrl;
	}

	public int getActionCode() {
		return (null != action) ? action.code : null;
	}

}
