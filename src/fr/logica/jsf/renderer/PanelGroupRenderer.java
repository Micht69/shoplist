package fr.logica.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.sun.faces.renderkit.html_basic.GroupRenderer;

public class PanelGroupRenderer extends GroupRenderer {

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

		if (!"none".equals(component.getAttributes().get("layout"))) {
			super.encodeBegin(context, component);
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

		if (!"none".equals(component.getAttributes().get("layout"))) {
			super.encodeEnd(context, component);
		}
	}

}
