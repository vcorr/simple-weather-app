package fi.weatherapp.service;

import java.util.Date;

public class MeasurementDTO {

	private Date date;
	private String type;
	private String value;
	
	public MeasurementDTO(Date date, String type, String value) {
		this.date = date;
		this.type = type;
		this.value = value;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
