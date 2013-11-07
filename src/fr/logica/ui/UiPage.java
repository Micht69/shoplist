package fr.logica.ui;

import java.util.ArrayList;
import java.util.List;

public class UiPage {

	public List<UiElement> elements = new ArrayList<UiElement>();

	@Override
	public String toString() {
		return "UiPage [" + elements.size() + " elements]";
	}

}
