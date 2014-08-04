package fi.weatherapp.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class CityForecast {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private long id;

	@ManyToOne
	@JoinColumn(name = "city_id")
	private City city;

	private Date date;

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
			if(type.equals("WeatherSymbol3")) {
				built = new WeatherConditionForecast();
			}else if(type.equals("Temperature")) {
				built = new TemperatureForecast();
			}
			built.city = city;
			built.date = date;
			built.value = value;
		}

		public CityForecast build() {
			return built;
		}

	}

}
