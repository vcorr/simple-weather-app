package fi.vasco.weatherapp;

import javax.annotation.Resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import fi.vasco.weatherapp.service.ForecastService;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
@EnableJpaRepositories(basePackages = { "fi.vasco.weatherapp" })
public class Application {

	@Resource
	private ForecastService forecastService;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
	}

	@Scheduled(fixedRate = 10 * 60 * 1000)
	private void updateStationData() {

		try {
			forecastService.fetchDataForCities();
		} catch (Exception e) {
			
		}
	}

	
}
