package fi.vasco.weatherapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.vasco.weatherapp.model.City;
import fi.vasco.weatherapp.model.CityForecast;
import fi.vasco.weatherapp.service.MeasurementDTO;

public interface CityForecastRepository extends JpaRepository<CityForecast, Long> {

	@Query("Select new fi.vasco.weatherbot.service.MeasurementDTO(cf.date, cf.type, cf.value) from CityForecast cf where cf.city = :city ORDER BY cf.date")
	public List<MeasurementDTO> getMeasurementDTOByCity(@Param("city") City city);

	public List<CityForecast> findByCity(City city);
}
