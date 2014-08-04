package fi.weatherapp.service;

import java.util.ArrayList;
import java.util.List;

public class CityForecastDTO {

	private String cityName;

	private Coords cityCoords;

	private List<ForecastDTO> forecasts = new ArrayList<ForecastDTO>();

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

	public List<ForecastDTO> getForecasts() {
		return this.forecasts;
	}

	private class Coords {
		public float lat;
		public float lon;
	}

	public void addForecasts(List<ForecastDTO> forecasts) {
		this.forecasts = forecasts;

	}
}
