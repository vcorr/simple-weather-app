package fi.weatherapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.weatherapp.model.City;
import fi.weatherapp.model.CityForecast;
import fi.weatherapp.service.ForecastDTO;

public interface CityForecastRepository extends
		JpaRepository<CityForecast, Long> {

	@Query("Select cf from CityForecast cf where cf.city = :city and type(cf) = :type  ORDER BY cf.date")
	public List<CityForecast> findByCityAndForecastType(
			@Param("city") City city, @Param("type") Class<?> type);

	@Query("Select new fi.weatherapp.service.ForecastDTO(wf.date, wf.value, tf.value)"
			+ " from WeatherConditionForecast wf, TemperatureForecast tf where wf.city = :city and tf.city = :city and wf.date = tf.date ORDER BY wf.date")
	public List<ForecastDTO> getForecastsForCity(@Param("city") City city);
}
