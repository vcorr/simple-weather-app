package fi.weatherapp.service;

import java.util.Date;

public class ForecastDTO {

	private Date forecastDate;
	
	private String weatherSymbol;
	
	private String tempValue;

	public ForecastDTO(Date date, String symbolValue, String tempValue) {
		
		this.forecastDate = date;
		this.weatherSymbol = symbolValue;
		this.tempValue = tempValue;
	}

	public Date getForecastDate() {
		return forecastDate;
	}

	public void setForecastDate(Date forecastDate) {
		this.forecastDate = forecastDate;
	}

	public String getWeatherSymbol() {
		return weatherSymbol;
	}

	public void setWeatherSymbol(String weatherSymbol) {
		this.weatherSymbol = weatherSymbol;
	}

	public String getTempValue() {
		return tempValue;
	}

	public void setTempValue(String tempValue) {
		this.tempValue = tempValue;
	}
	
	

}
