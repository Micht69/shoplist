package fr.logica.jsf.model.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import fr.logica.business.Entity;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.controller.ViewController;

public class TabPanelModel extends GroupModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6226109712259329656L;

	private static final String SELECTED_TAB = "selected_tab";
	private String selectedTab;

	private List<String> visibleTabs;

	public TabPanelModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String panelName) {
		super(viewCtrl, store, entity, entityName, panelName);
	}

	@Override
	protected void loadVisibility(Map<String, String> store, Entity entity, String entityName, String panelName) {
		this.visibleTabs = new BusinessController().getVisibleTabs(entity, entityName, panelName, getTabs(panelName), viewCtrl.getCurrentView()
				.getAction(), viewCtrl.getContext());
		this.visible = visibleTabs.size() > 0;
		if (this.visible) {
			if (store.containsKey(SELECTED_TAB)) {
				this.selectedTab = store.get(SELECTED_TAB);
			} else {
				this.selectedTab = visibleTabs.get(0);
			}
		}
	}

	@Override
	public void storeViewData(Map<String, String> store) {
		store.put(SELECTED_TAB, selectedTab);
	}

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	public boolean isTabVisible(String tabName) {
		return visibleTabs.contains(tabName);
	}

	private List<String> getTabs(String tabPanelName) {
		List<String> tabs = new ArrayList<String>();
		UIComponent c = FacesContext.getCurrentInstance().getViewRoot().findComponent("mainForm:tabs-selected_" + tabPanelName);
		if (null != c) {
			for (UIComponent tab : c.getChildren()) {
				tabs.add(tab.getId());
			}
		}
		return tabs;
	}
}
