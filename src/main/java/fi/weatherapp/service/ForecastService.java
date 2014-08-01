package fi.weatherapp.service;

import java.util.List;

public interface ForecastService {

	public abstract List<ForecastDTO> getForecastsForCities();

}
