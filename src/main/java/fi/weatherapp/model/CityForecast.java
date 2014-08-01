package fi.weatherapp.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class CityForecast {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;

	@ManyToOne
	@JoinColumn(name = "city_id")
	private City city;

	private Date date;

	private String type;

	private String value;

	public long getId() {
		return id;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
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

	public static Builder getBuilder(City city, Date date, String type, String value) {
		return new Builder(city, date, type, value);
	}

	public static class Builder {
		CityForecast built;

		Builder(City city, Date date, String type, String value) {
			built = new CityForecast();
			built.city = city;
			built.date = date;
			built.type = type;
			built.value = value;
		}

		public CityForecast build() {
			return built;
		}

	}

}
