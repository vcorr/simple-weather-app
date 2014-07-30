package fi.vasco.weatherapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.vasco.weatherapp.model.City;

public interface CityRepository extends JpaRepository<City, Long> {

	public List<City> findByName(String name);
	
	public List<City> findByMajorCityTrue();

}
