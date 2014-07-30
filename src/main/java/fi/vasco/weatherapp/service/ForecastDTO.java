package fi.vasco.weatherapp.service;

import java.util.List;

public class ForecastDTO {

	private String cityName;

	private Coords cityCoords;

	private List<MeasurementDTO> measurementDTO;

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

	public List<MeasurementDTO> getMeasurementDTO() {
		return measurementDTO;
	}

	public void setMeasurementDTO(List<MeasurementDTO> measurementDTO) {
		this.measurementDTO = measurementDTO;
	}

	private class Coords {
		public float lat;
		public float lon;
	}
}
