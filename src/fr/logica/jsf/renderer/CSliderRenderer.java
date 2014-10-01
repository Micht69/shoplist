package fr.logica.jsf.renderer;

import java.io.IOException;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.primefaces.component.slider.Slider;
import org.primefaces.component.slider.SliderRenderer;

/**
 * Custom slider renderer for the Primefaces slider component.
 * <p>
 * It fixes a bug while the linked component value is {@code null}.
 * </p>
 */
public class CSliderRenderer extends SliderRenderer {

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		Slider slider = (Slider) component;

		if (slider.isRange()) {
			// A slider with a range is not managed by this renderer.
			super.encodeEnd(context, component);
			return;
		}

		UIComponent forComponent = getTarget(context, slider, slider.getFor());

		if (forComponent instanceof EditableValueHolder) {
			EditableValueHolder input = (EditableValueHolder) forComponent;
			if (input.getValue() == null && input.getSubmittedValue() == null) {
				// The component does not have any value, so let's assign it the minimum value of the slider.
				input.setValue(slider.getMinValue());
			}
		}
		super.encodeEnd(context, component);
	}

}
