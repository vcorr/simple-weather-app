package fi.vasco.weatherapp.service;

import java.util.Iterator;
import java.util.List;

import fi.vasco.weatherapp.model.City;
import fi.vasco.weatherapp.model.CityForecast;

public interface ForecastService {

	void fetchDataForCities() throws Exception;

	public abstract List<ForecastDTO> getForecastsForCities();

}
