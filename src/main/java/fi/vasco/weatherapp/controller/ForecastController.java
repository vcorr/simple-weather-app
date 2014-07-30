package fi.vasco.weatherapp.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.vasco.weatherapp.service.ForecastDTO;
import fi.vasco.weatherapp.service.ForecastService;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

	@Resource
	private ForecastService forecastService;

	@RequestMapping(value = "cities", method = RequestMethod.GET, produces = "application/json")
	public List<ForecastDTO> getForecastsForCities() {
		return forecastService.getForecastsForCities();
	}
}
