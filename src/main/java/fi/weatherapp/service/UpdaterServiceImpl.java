package fi.weatherapp.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fi.weatherapp.model.City;
import fi.weatherapp.model.CityForecast;
import fi.weatherapp.model.TemperatureForecast;
import fi.weatherapp.model.WeatherConditionForecast;
import fi.weatherapp.repository.CityForecastRepository;
import fi.weatherapp.repository.CityRepository;

@Repository
public class UpdaterServiceImpl implements UpdaterService {

	@Value("${fmi.url}")
	private String FMIURL;

	@Value("${fmi.key}")
	private String FMIKEY;

	@Value("${fmi.query}")
	private String FMIQUERY;

	@Value("${fmi.params}")
	private String FMIPARAMS;

	@Value("${fmi.unittest}")
	private String FMIUNITTEST;

	@Resource
	private CityRepository cityRepository;

	@Resource
	private CityForecastRepository cityForecastRepository;

	private DateTimeFormatter formatter = ISODateTimeFormat
			.dateHourMinuteSecond();
	private Logger logger = Logger.getLogger(UpdaterService.class);
	
	private DateTimeZone timezone = DateTimeZone.forID("Europe/Helsinki");

	/***
	 * fetchDataForCities
	 * 
	 * Fetches new forecasts for cities from the FMI Open Data interface.
	 * Database is updated accordingly and cache is cleared.
	 */
	@Override
	@CacheEvict("forecasts")
	public void fetchDataForCities() throws Exception {
		
		Set<String> ids = DateTimeZone.getAvailableIDs();
		for(String id : ids) {
			System.out.println("ID="+id);
		}
		
		logger.debug("Getting new forecasts from FMI");

		String measurementsQuery = "";
		InputStream inputStream = null;

		// for unit tests we use local file
		if (FMIUNITTEST.equals("true")) {
			inputStream = this.getClass().getClassLoader()
					.getResourceAsStream(FMIURL);

		} else {
			measurementsQuery = FMIURL + FMIKEY + FMIQUERY;
			DateTime sixHoursForward = new DateTime(DateTimeZone.UTC)
					.plusMinutes(6 * 60);
			String endtime = formatter.print(sixHoursForward);
			measurementsQuery += "&endtime=" + endtime + "&";
			measurementsQuery += "parameters=" + FMIPARAMS;

			URL myURL = new URL(measurementsQuery);
			logger.debug("REQUEST:");
			logger.debug(myURL.toString());

			HttpURLConnection myURLConnection = (HttpURLConnection) myURL
					.openConnection();

			if (myURLConnection.getResponseCode() != 200) {
				BufferedReader breader = new BufferedReader(
						new InputStreamReader(myURLConnection.getErrorStream()));
				String line = "";
				String error = "";
				while ((line = breader.readLine()) != null) {
					error += line;
				}
				throw new Exception("getting data failed" + error);
			}
			inputStream = myURLConnection.getInputStream();
		}

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document doc = builder.parse(inputStream);

		XPath xPath = XPathFactory.newInstance().newXPath();

		String timestamp = doc.getChildNodes().item(0).getAttributes()
				.getNamedItem("timeStamp").getNodeValue();
		DateTime timestampInDoc = DateTime.parse(timestamp);

		String expression = "//*[local-name()='PointTimeSeriesObservation']";

		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc,
				XPathConstants.NODESET);

		
		for (int i = 0; i < nodeList.getLength(); i++) {
			String name = getLocationName(nodeList.item(i), xPath);
			String type = getParamType(nodeList.item(i), xPath);

			Map<Date, Measurement> measurementsMap = getValues(
					nodeList.item(i), xPath);

			// should return exactly 1 city
			List<City> cityList = cityRepository.findByName(name);
			City city = cityList.get(0);

			Class<?> forecastClass = getTypeForTypeName(type);
			List<CityForecast> forecasts = cityForecastRepository
					.findByCityAndForecastType(city, forecastClass);

			for (CityForecast cityForecast : forecasts) {
				Date forecastDate = cityForecast.getDate();

				if (forecastDate.before(timestampInDoc.toDate())) {
					logger.debug("Deleting forecast that occures before current time");
					cityForecastRepository.delete(cityForecast.getId());
					continue;
				}

				DateTime dateTime = new DateTime(cityForecast.getDate());
				if (measurementsMap.containsKey(dateTime.toDate())) {
					Measurement m = measurementsMap.get(dateTime.toDate());
					if (!cityForecast.getValue().equals(m.measurementValue)) {
						logger.debug("Fetched forecast is different than the saved one, updating");
						cityForecast.setValue(m.measurementValue);
						cityForecastRepository.saveAndFlush(cityForecast);
					}

					measurementsMap.remove(dateTime.toDate());
					continue;
				}
			}

			// get remaining measurements and add them to the city as new
			java.util.Set<Date> keys = measurementsMap.keySet();
			for (Date date : keys) {
				logger.debug("Adding new entry for " + city.getName());
				Measurement m = measurementsMap.get(date);
				CityForecast cityForecast = CityForecast.getBuilder(city,
						m.measurementTime, type, m.measurementValue).build();
				cityForecastRepository.saveAndFlush(cityForecast);
			}
		}

		logger.debug("Done.");
	}
	
	private Class<?> getTypeForTypeName(String typeName) {
		switch (typeName) {
		case "WeatherSymbol3":
			return WeatherConditionForecast.class;
		case "Temperature":
			return TemperatureForecast.class;
		default:
			return Object.class;
		}
	}

	private String getParamType(Node rootNode, XPath xPath) throws Exception {
		String childexpressionB = ".//*[local-name()='Location']/@id";
		String nodeChildListB = (String) xPath.compile(childexpressionB)
				.evaluate(rootNode, XPathConstants.STRING);
		String[] stringArray = nodeChildListB.split("-");
		return stringArray[stringArray.length - 1];

	}

	private String getLocationName(Node rootNode, XPath xPath) throws Exception {
		String childexpression = ".//*[local-name()='Location']/name";
		NodeList nodeChildList = (NodeList) xPath.compile(childexpression)
				.evaluate(rootNode, XPathConstants.NODESET);
		return nodeChildList.item(0).getTextContent();
	}

	private Map<Date, Measurement> getValues(Node rootNode, XPath xPath)
			throws Exception {

		Map<Date, Measurement> measurementMap = new HashMap<Date, Measurement>();

		NodeList measurementTimeList = (NodeList) xPath.compile(
				".//*[local-name()='MeasurementTVP']/time").evaluate(rootNode,
				XPathConstants.NODESET);

		NodeList measurementValueList = (NodeList) xPath.compile(
				".//*[local-name()='MeasurementTVP']/value").evaluate(rootNode,
				XPathConstants.NODESET);

		for (int i = 0; i < measurementTimeList.getLength(); i++) {
			Measurement measurement = new Measurement();
			measurement.measurementTime = DateTime.parse(
					measurementTimeList.item(i).getTextContent()).toDateTime(timezone).toDate();
			System.out.println("MEASUREMENT TIME"+measurement.measurementTime);
			measurement.measurementValue = measurementValueList.item(i)
					.getTextContent();
			measurementMap.put(measurement.measurementTime, measurement);
		}

		return measurementMap;
	}

	private class Measurement {
		public Date measurementTime;
		public String measurementValue;
	}
}
