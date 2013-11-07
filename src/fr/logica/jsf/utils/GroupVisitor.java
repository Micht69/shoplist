package fr.logica.jsf.utils;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;

import fr.logica.jsf.components.tab.HtmlTab;
import fr.logica.jsf.components.tab.HtmlTabPanel;
import fr.logica.jsf.components.wizard.HtmlWizard;

public class GroupVisitor implements VisitCallback {

	private final List<String> groups;
	private final List<String> tabPanels;
	private final List<String> tabs;

	public GroupVisitor() {
		groups = new ArrayList<String>();
		tabPanels = new ArrayList<String>();
		tabs = new ArrayList<String>();
	}

	@Override
	public VisitResult visit(VisitContext context, UIComponent component) {

		if (component instanceof HtmlWizard || component instanceof HtmlTabPanel) {
			tabPanels.add(component.getId());

		} else if (component instanceof HtmlTab) {
			tabs.add(component.getId());

		} else if (component instanceof HtmlPanelGroup && null != component.getId()) {
			groups.add(component.getId());
		}
		return VisitResult.ACCEPT;
	}

	public List<String> getGroups() {
		return groups;
	}

	public List<String> getTabPanels() {
		return tabPanels;
	}

	public List<String> getTabs() {
		return tabs;
	}

}
