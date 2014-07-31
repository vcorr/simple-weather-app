package fi.weatherapp.service;

import java.util.List;

public interface ForecastService {

	void fetchDataForCities() throws Exception;

	public abstract List<ForecastDTO> getForecastsForCities();

}
