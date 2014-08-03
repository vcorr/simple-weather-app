package fi.weatherapp.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.weatherapp.service.CityForecastDTO;
import fi.weatherapp.service.ForecastService;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

	@Resource
	private ForecastService forecastService;

	private Logger logger = Logger.getLogger(ForecastController.class);

	/**
	 * getForecastsForCities
	 * @return 6 forecasts per city in JSON
	 */
	@RequestMapping(value = "cities", method = RequestMethod.GET, produces = "application/json")
	public List<CityForecastDTO> getForecastsForCities() {
		logger.debug("Getting forecasts for cities");
		return forecastService.getForecastsForCities();
	}
	
	
}
