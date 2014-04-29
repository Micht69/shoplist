package fr.logica.jsf.components.map;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.html_basic.HiddenRenderer;
import com.vividsolutions.jts.geom.Geometry;

import fr.logica.geocoding.MapGeocoding;

@FacesRenderer(componentFamily = HtmlMap.COMPONENT_FAMILY, rendererType = MapRenderer.RENDERER_TYPE)
public class MapRenderer extends HiddenRenderer {

	public static final String RENDERER_TYPE = "cgi.faces.Map";

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		super.encodeBegin(context, component);
		HtmlMap map = (HtmlMap) component;
		ResponseWriter writer = context.getResponseWriter();
		String clientId = map.getClientId(context);

		writer.writeText("\n", null);
		writer.startElement("div", null);
		writer.writeAttribute("id", clientId + "-map", null);
		if (null != map.getStyle()) {
			writer.writeAttribute("style", map.getStyle(), null);
		}

		if (null != map.getStyleClass()) {
			writer.writeAttribute("class", map.getStyleClass(), null);
		}
		writer.writeText("\n", null);
		encodeScript(context, map);
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("div");
		super.encodeEnd(context, component);
	}

	private void encodeScript(FacesContext context, HtmlMap map) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = map.getClientId(context);
		writer.startElement("script", null);
		writer.writeAttribute("type", "text/javascript", null);
		String script = "";
		script += "\n\t" + "OpenLayers.ImgPath = \"../static/img/map/\";";
		script += "\n\t" + "var mapUtils = new MapUtils(\"" + clientId + "\", " + map.isDisabled() + ");";

		Geometry point = (Geometry) map.getValue();

		if (null == point) {
			String location = map.getStringValue();

			if (null != location && !location.isEmpty()) {
				MapGeocoding geocodingService = new MapGeocoding();
				point = geocodingService.getGeometryPoint(location);
				map.setValue(point);
			}
		}
		if (null != point) {
			script += "\n\t" + "mapUtils.addPoint(" + point.getCoordinate().y + ", " + point.getCoordinate().x + ", null);";
		}

		script += "\n\t" + "registerMap(mapUtils);";
		writer.writeText(script, null);
		writer.endElement("script");
		writer.writeText("\n", null);
	}

}
