package fi.weatherapp;

import javax.annotation.Resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import fi.weatherapp.service.ForecastService;

@Configuration
@ComponentScan(excludeFilters = @ComponentScan.Filter(value = Application.class, type = FilterType.ASSIGNABLE_TYPE))
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = { "fi.weatherapp" })
@EnableCaching
@PropertySource("classpath:/application-test.properties")
public class TestApplication {

	@Resource
	private ForecastService forecastService;

	public static void main(String[] args) {

		SpringApplication.run(TestApplication.class, args);
	}

}
