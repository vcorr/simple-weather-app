package fi.vasco.weatherapp.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fi.vasco.weatherapp.model.City;
import fi.vasco.weatherapp.model.CityForecast;
import fi.vasco.weatherapp.repository.CityForecastRepository;
import fi.vasco.weatherapp.repository.CityRepository;

@Repository
public class ForecastServiceImpl implements ForecastService {

	private static final String FMIURL = "http://data.fmi.fi/fmi-apikey/d78b8496-9c5f-4ad1-8ee1-d22fa4d9e786/wfs?";
	private static final String QUERY = "request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::cities::timevaluepair&";
	private DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecond();


	@Resource
	private CityRepository cityRepository;

	@Resource
	private CityForecastRepository cityForecastRepository;

	@Override
	@Cacheable("forecasts")
	public List<ForecastDTO> getForecastsForCities() {
		
		List<City> cities = cityRepository.findByMajorCityTrue();
		List<ForecastDTO> forecastList = new ArrayList<ForecastDTO>();
		
		for (City city : cities) {
			ForecastDTO dto = new ForecastDTO();
			dto.setCityName(city.getName());
			dto.setCityCoords(city.getLat(), city.getLon());

			List<MeasurementDTO> measurements = cityForecastRepository.getMeasurementDTOByCity(city);
			dto.setMeasurementDTO(measurements);
			
			forecastList.add(dto);
		}
		return forecastList;
	}

	/***
	 * fetchDataForCities
	 * 
	 * Fetches new forecasts for cities from the FMI Open Data interface.
	 * Database is updated accordingly and cache is cleared.
	 */
	@Override
	@CacheEvict("forecasts")
	public void fetchDataForCities() throws Exception {
		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTime sixHoursForward = now.plusMinutes(6 * 60);
		String endtime = formatter.print(sixHoursForward);
		String measurementsQuery = QUERY;
		measurementsQuery += "endtime=" + endtime + "&";
		measurementsQuery += "parameters=WeatherSymbol3";

		URL myURL = new URL(FMIURL + measurementsQuery);

		HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();

		if (myURLConnection.getResponseCode() != 200) {
			BufferedReader breader = new BufferedReader(new InputStreamReader(myURLConnection.getErrorStream()));
			String line = "";
			String error = "";
			while ((line = breader.readLine()) != null) {
				error += line;
			}
			throw new Exception("getting data failed" + error);
		}

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document doc = builder.parse(myURLConnection.getInputStream());

		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//*[local-name()='PointTimeSeriesObservation']";

		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < nodeList.getLength(); i++) {
			String name = getLocationName(nodeList.item(i), xPath);
			Map<Date, Measurement> measurementsMap = getValues(nodeList.item(i), xPath);

			// should return exactly 1 city
			List<City> cityList = cityRepository.findByName(name);

			// get existing forecasts for the city
			List<CityForecast> forecastList = cityForecastRepository.findByCity(cityList.get(0));

			Date currentDate = new Date();

			for(CityForecast cityForecast : forecastList) {
				Date forecastDate = cityForecast.getDate();

				if (forecastDate.before(currentDate)) {
					cityForecastRepository.delete(cityForecast.getId());
					continue;
				}

				DateTime dateTime = new DateTime(cityForecast.getDate());
				if (measurementsMap.containsKey(dateTime.toDate())) {
					Measurement m = measurementsMap.get(dateTime.toDate());
					if (!cityForecast.getValue().equals(m.measurementValue)) {
						cityForecast.setValue(m.measurementValue);
						cityForecastRepository.saveAndFlush(cityForecast);
					}
					measurementsMap.remove(dateTime.toDate());
					continue;
				}
			}

			// get remaining measurements and add them to the city as new
			java.util.Set<Date> keys = measurementsMap.keySet();
			for(Date date : keys) {
				Measurement m = measurementsMap.get(date);
				CityForecast cityForecast = CityForecast.getBuilder(cityList.get(0), m.measurementTime, "cond",
						m.measurementValue).build();
				cityForecastRepository.saveAndFlush(cityForecast);
			}
		}
	}

	private String getLocationName(Node rootNode, XPath xPath) throws Exception {
		String childexpression = ".//*[local-name()='Location']/name";
		NodeList nodeChildList = (NodeList) xPath.compile(childexpression).evaluate(rootNode, XPathConstants.NODESET);
		return nodeChildList.item(0).getTextContent();
	}

	private Map<Date, Measurement> getValues(Node rootNode, XPath xPath) throws Exception {

		Map<Date, Measurement> measurementMap = new HashMap<Date, Measurement>();

		NodeList measurementTimeList = (NodeList) xPath.compile(".//*[local-name()='MeasurementTVP']/time").evaluate(
				rootNode, XPathConstants.NODESET);
		
		NodeList measurementValueList = (NodeList) xPath.compile(".//*[local-name()='MeasurementTVP']/value").evaluate(
				rootNode, XPathConstants.NODESET);

		for (int i = 0; i < measurementTimeList.getLength(); i++) {
			Measurement measurement = new Measurement();
			measurement.measurementTime = DateTime.parse(measurementTimeList.item(i).getTextContent()).toDate();
			measurement.measurementValue = measurementValueList.item(i).getTextContent();
			measurementMap.put(measurement.measurementTime, measurement);
		}

		return measurementMap;
	}


	private class Measurement {
		public Date measurementTime;
		public String measurementValue;
	}

}
