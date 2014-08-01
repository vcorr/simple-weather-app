package fi.weatherapp.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class City {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;

	private float lat;

	private float lon;

	private boolean majorCity;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLon() {
		return lon;
	}

	public void setLon(float lon) {
		this.lon = lon;
	}

	public boolean isMajorCity() {
		return majorCity;
	}

	public void setMajorCity(boolean majorCity) {
		this.majorCity = majorCity;
	}

	public static Builder getBuilder(String name, float lat, float lon) {
		return new Builder(name, lat, lon);
	}

	public static class Builder {
		City built;

		Builder(String name, float lat, float lon) {
			built = new City();
			built.name = name;
			built.lat = lat;
			built.lon = lon;
		}

		public City build() {
			return built;
		}
	}
}
