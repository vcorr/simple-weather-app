package fi.weatherapp.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.weatherapp.model.City;
import fi.weatherapp.model.CityForecast;
import fi.weatherapp.service.MeasurementDTO;

public interface CityForecastRepository extends
		JpaRepository<CityForecast, Long> {

	@Query("Select new fi.weatherapp.service.MeasurementDTO(cf.date, cf.type, cf.value) from CityForecast cf where cf.city = :city and cf.type =:type  ORDER BY cf.date")
	public List<MeasurementDTO> getMeasurementDTOByCityAndType(@Param("city") City city, @Param("type") String type);

	
	
	
	public List<CityForecast> findByCityAndType(City city, String type);

	@Query("Select cf.type from CityForecast cf where cf.city = :city")
	public List<String> getForecastTypesByCity(@Param("city") City city);

}
