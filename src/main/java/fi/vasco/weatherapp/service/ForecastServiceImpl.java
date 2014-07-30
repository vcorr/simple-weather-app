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
	public List<ForecastDTO> getForecastsForCities() {
		List<City> cities = cityRepository.findByMajorCityTrue();

		List<ForecastDTO> forecastList = new ArrayList<ForecastDTO>();

		for (Iterator<City> iterator = cities.iterator(); iterator.hasNext();) {
			City city = (City) iterator.next();

			ForecastDTO dto = new ForecastDTO();
			dto.setCityName(city.getName());
			dto.setCityCoords(city.getLat(), city.getLon());

			List<MeasurementDTO> measurements = cityForecastRepository.getMeasurementDTOByCity(city);

			if (city.getName().equals("Tampere")) {
				System.out.println("Tampere");
				System.out.println(measurements.size());
				System.out.println(new DateTime(measurements.get(0).getDate()));
				System.out.println(new DateTime(measurements.get(1).getDate()));
				System.out.println(new DateTime(measurements.get(2).getDate()));
				System.out.println(new DateTime(measurements.get(3).getDate()));
				System.out.println(new DateTime(measurements.get(4).getDate()));
				System.out.println(new DateTime(measurements.get(5).getDate()));
				System.out.println(measurements.get(0).getValue());
			}

			dto.setMeasurementDTO(measurements);
			forecastList.add(dto);
		}
		return forecastList;
	}

	@Override
	public void fetchDataForCities() throws Exception {

		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTime hourForward = now.plusMinutes(6 * 60);
		String endtime = formatter.print(hourForward);
		String measurementsQuery = QUERY;
		measurementsQuery += "endtime=" + endtime + "&";
		measurementsQuery += "parameters=WeatherSymbol3";

		URL myURL = new URL(FMIURL + measurementsQuery);

		System.out.println("QUERY:" + measurementsQuery);
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

			// should return 1 city
			List<City> cityList = cityRepository.findByName(name);

			// get existing forecasts for the city
			List<CityForecast> forecastList = cityForecastRepository.findByCity(cityList.get(0));

			Date currentDate = new Date();

			for (Iterator<CityForecast> iterator = forecastList.iterator(); iterator.hasNext();) {
				CityForecast cityForecast = (CityForecast) iterator.next();
				Date forecastDate = cityForecast.getDate();

				if (forecastDate.before(currentDate)) {
					System.out.println("Forecast id: " + cityForecast.getId() + " is out of date, deleting");
					cityForecastRepository.delete(cityForecast.getId());
					continue;
				}

				// does this date exists in current measurements? if yes, then
				// update
				DateTime dateTime = new DateTime(cityForecast.getDate());
				if (measurementsMap.containsKey(dateTime.toDate())) {
					Measurement m = measurementsMap.get(dateTime.toDate());
					if (!cityForecast.getValue().equals(m.measurementValue)) {
						System.out.println("Updating Forecast id: " + cityForecast.getId());
						cityForecast.setValue(m.measurementValue);
						cityForecastRepository.saveAndFlush(cityForecast);
					} else {
						System.out.println("Forecast value had not changed, do nothing");
					}

					measurementsMap.remove(dateTime.toDate());
					continue;
				}
			}

			// get remaining measurements and add them to the city
			java.util.Set<Date> keys = measurementsMap.keySet();
			for (Iterator<Date> iterator = keys.iterator(); iterator.hasNext();) {
				Date date = (Date) iterator.next();
				Measurement m = measurementsMap.get(date);
				System.out.println("Adding new Forecast for " + cityList.get(0).getName());
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

	private String getLocationPosition(Node rootNode, XPath xPath) throws Exception {
		String childexpression = ".//*[local-name()='Point']/pos";
		NodeList nodeChildList = (NodeList) xPath.compile(childexpression).evaluate(rootNode, XPathConstants.NODESET);
		return nodeChildList.item(0).getTextContent();
	}

	private Map<Date, Measurement> getValues(Node rootNode, XPath xPath) throws Exception {

		Map<Date, Measurement> measurementMap = new HashMap<Date, Measurement>();

		NodeList measurementTimeList = (NodeList) xPath.compile(".//*[local-name()='MeasurementTVP']/time").evaluate(
				rootNode, XPathConstants.NODESET);
		System.out.println("LIST" + measurementTimeList.getLength());

		NodeList measurementValueList = (NodeList) xPath.compile(".//*[local-name()='MeasurementTVP']/value").evaluate(
				rootNode, XPathConstants.NODESET);

		for (int i = 0; i < measurementTimeList.getLength(); i++) {
			Measurement measurement = new Measurement();
			measurement.measurementTime = DateTime.parse(measurementTimeList.item(i).getTextContent()).toDate();
			measurement.measurementValue = measurementValueList.item(i).getTextContent();
			measurementMap.put(measurement.measurementTime, measurement);
		}

		System.out.println("map:" + measurementMap.size());
		return measurementMap;
	}


	private class Measurement {
		public Date measurementTime;
		public String measurementValue;
	}

}
