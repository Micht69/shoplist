package fr.logica.geocoding;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.logica.business.MessageUtils;

public class MapGeocoding {

	private static final Logger LOGGER = Logger.getLogger(MapGeocoding.class);
	private static final String GEOCODING_URL = "geocoding.url";
	private static final String GEOCODING_PROXY_HOST = "geocoding.proxy.host";
	private static final String GEOCODING_PROXY_PORT = "geocoding.proxy.port";

	/** Type de variable : préfixe du champ contenant l'adresse (associé au champs GEOMETRY) */
	public final static String GEOMETRY_ADDRESS = "GEOADR_";
	/** Type de variable : SRID du champ GEOMETRY (projection WGS84_SRID = 4326) */
	public final static int GEOMETRY_SRID = 4326;

	/**
	 * Input string must be formatted as "well known" text representation<br>
	 * (http://portal.opengeospatial.org/files/?artifact_id=25355 p52)
	 * 
	 * @param s
	 * @return null if string cannot be converted
	 */
	public static Geometry toGeometry(String s){
		try{
			return new WKTReader().read(s);
		}catch(ParseException e){
			return null;
		}
	}
	
	public MapCoords getCoordinates(Object obj) throws ClientProtocolException, IOException, URISyntaxException {
		MapCoords mc = null;

		if (obj != null) {

			if (obj instanceof String) {
				mc = getCoordinates((String) obj);

			} else if (obj instanceof Geometry) {
				mc = getCoordinates((Geometry) obj);

			} else {
				throw new IllegalArgumentException();
			}
		}
		return mc;
	}

	private MapCoords getCoordinates(Geometry geometry) {
		MapCoords mapCoords = new MapCoords();
		Coordinate c = geometry.getCoordinate();
		mapCoords.setLatitude(String.valueOf(c.x));
		mapCoords.setLongitude(String.valueOf(c.y));
		return mapCoords;
	}

	/**
	 * Récupère les coordonnées d'une adresse à partir d'un web service
	 * 
	 * @param adresse
	 *            : l'adresse complète
	 * @return MapCoords : objet contenant les informations sur l'adresse (pour l'instant les coordonnees en latitude/longitude, la qualité, on
	 *         pourrait également récupérer l'adresse formatée par le service web, etc...)
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws Exception
	 */
	private MapCoords getCoordinates(String adresse) throws ClientProtocolException, IOException, URISyntaxException {
		MapCoords mapCoords = new MapCoords();
		Reader reader = null;

		try {
			URL url = new URL(MessageUtils.getServerProperty(GEOCODING_URL) + adresse.replace(' ', '+'));
			DefaultHttpClient client = new DefaultHttpClient();

			String proxyHost = MessageUtils.getServerProperty(GEOCODING_PROXY_HOST);
			String proxyPort = MessageUtils.getServerProperty(GEOCODING_PROXY_PORT);

			if (null != proxyHost && !"".equals(proxyHost)) {
				HttpHost proxy;

				if (null != proxyPort && !"".equals(proxyPort)) {
					proxy = new HttpHost(proxyHost, Integer.valueOf(proxyPort));
				} else {
					proxy = new HttpHost(proxyHost);
				}
				client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}

			HttpGet request = new HttpGet(url.toURI());
			HttpResponse response = client.execute(request);

			reader = new InputStreamReader(response.getEntity().getContent());
			StringBuffer sb = new StringBuffer();
			int read;
			char[] cbuf = new char[1024];
			while ((read = reader.read(cbuf)) != -1) {
				sb.append(cbuf, 0, read);
			}

			JSONArray array = (JSONArray) JSONValue.parse(sb.toString());

			if (array.size() > 0) {
				JSONObject obj = (JSONObject) array.get(0);
				mapCoords.setLatitude(obj.get("lat").toString());
				mapCoords.setLongitude(obj.get("lon").toString());
				mapCoords.setQuality(obj.get("importance").toString());
			}

		} finally {
			IOUtils.closeQuietly(reader);
		}
		return mapCoords;
	}

	/**
	 * Crée un objet Géométrie à partir des coordonnées (x, y)
	 * 
	 * @param x
	 * @param y
	 * @return Geometry : les coordonnées du point au format Geometry
	 */
	public Geometry getGeometry(double x, double y) {
		Geometry geom = null;
		// Create GeometryFactory (Precision and SRID)
		PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
		GeometryFactory geometryFactory = new GeometryFactory(precisionModel, MapGeocoding.GEOMETRY_SRID);
		// Create Point
		Coordinate coordinate = new Coordinate();
		coordinate.x = x;
		coordinate.y = y;
		geom = geometryFactory.createPoint(coordinate);
		return geom;
	}

	/**
	 * Récupère un point de type Geometry à partir d'une adresse à partir d'un web service
	 * 
	 * @param adresse
	 *            : l'adresse complète
	 * @return Geometry : les coordonnées du point au format Geometry
	 */
	public Geometry getGeometryPoint(String adresse) {
		Geometry geom = null;

		try {
			MapCoords coords = this.getCoordinates(adresse);
	
			if (coords != null) {
				// Create GeometryFactory (Precision and SRID)
				PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
				GeometryFactory geometryFactory = new GeometryFactory(precisionModel, MapGeocoding.GEOMETRY_SRID);
				// Create Point
				Coordinate coordinate = new Coordinate();
				coordinate.x = Double.parseDouble(coords.getLatitude());
				coordinate.y = Double.parseDouble(coords.getLongitude());
				geom = geometryFactory.createPoint(coordinate);
			}

		} catch (Exception exception) {
			LOGGER.error("Unable to retrieve geometry location for the address " + adresse, exception);
		}
		return geom;
	}

	/**
	 * Retourne la chaîne de caractères
	 * 
	 * @param geom
	 * @return String
	 * @throws Exception
	 */
	public String toString(Geometry geom) throws Exception {
		String geomStr = "";
		if (geom != null) {
			geomStr = geom.toText();
		}
		return geomStr;
	}
}
