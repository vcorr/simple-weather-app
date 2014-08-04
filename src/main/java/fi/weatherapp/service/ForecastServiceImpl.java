package fi.weatherapp.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import fi.weatherapp.model.City;
import fi.weatherapp.repository.CityForecastRepository;
import fi.weatherapp.repository.CityRepository;

@Repository
public class ForecastServiceImpl implements ForecastService {

	private Logger logger = Logger.getLogger(ForecastServiceImpl.class);

	@Resource
	private CityRepository cityRepository;

	@Resource
	private CityForecastRepository cityForecastRepository;

	@Override
	@Cacheable("forecasts")
	public List<CityForecastDTO> getForecastsForCities() {

		logger.debug("No cached entries found - Getting current forecasts from DB");

		List<City> cities = cityRepository.findByMajorCityTrue();
		List<CityForecastDTO> forecastList = new ArrayList<CityForecastDTO>();

		for (City city : cities) {
			CityForecastDTO cityForecastDTO = new CityForecastDTO();
			cityForecastDTO.setCityName(city.getName());
			cityForecastDTO.setCityCoords(city.getLat(), city.getLon());

			List<ForecastDTO> forecasts = cityForecastRepository
					.getForecastsForCity(city);

			cityForecastDTO.addForecasts(forecasts);

			forecastList.add(cityForecastDTO);
		}
		return forecastList;
	}

}
