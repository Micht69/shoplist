package fr.logica.jsf.controller.fileUpload;

import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;

/**
 * Faces component for <code>input type="file"</code> field.
 * 
 * @author BalusC
 */
@FacesComponent(value = "HtmlInputFile")
public class HtmlInputFile extends HtmlInputText {

	// Getters
	// ------------------------------------------------------------------------------------

	@Override
	public String getRendererType() {
		return "javax.faces.File";
	}

}
