package fr.logica.geocoding;

import java.io.Serializable;

public class MapCoords implements Serializable {

	private static final long serialVersionUID = 1L;

	private String key;

	private String latitude;

	private String longitude;

	private String quality;

	private String latitudeMin;

	private String longitudeMin;

	private String latitudeMax;

	private String longitudeMax;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getLatitudeMin() {
		return latitudeMin;
	}

	public void setLatitudeMin(String latitudeMin) {
		this.latitudeMin = latitudeMin;
	}

	public String getLongitudeMin() {
		return longitudeMin;
	}

	public void setLongitudeMin(String longitudeMin) {
		this.longitudeMin = longitudeMin;
	}

	public String getLatitudeMax() {
		return latitudeMax;
	}

	public void setLatitudeMax(String latitudeMax) {
		this.latitudeMax = latitudeMax;
	}

	public String getLongitudeMax() {
		return longitudeMax;
	}

	public void setLongitudeMax(String longitudeMax) {
		this.longitudeMax = longitudeMax;
	}

}
