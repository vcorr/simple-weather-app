package fi.weatherapp.service;

import java.util.ArrayList;
import java.util.List;

public class CityForecastDTO {

	private String cityName;

	private Coords cityCoords;

	private List<Forecast> forecasts = new ArrayList();

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public Coords getCityCoords() {
		return cityCoords;
	}

	public void setCityCoords(float lat, float lon) {
		this.cityCoords = new Coords();
		this.cityCoords.lat = lat;
		this.cityCoords.lon = lon;
	}

	private class Coords {
		public float lat;
		public float lon;
	}

	public void addForecast(Forecast forecast) {
		this.forecasts.add(forecast);
	}

	public List<Forecast> getForecasts() {
		return this.forecasts;
	}
}
