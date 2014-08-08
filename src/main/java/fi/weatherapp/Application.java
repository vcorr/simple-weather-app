package fi.weatherapp;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import fi.weatherapp.service.UpdaterService;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
@EnableJpaRepositories(basePackages = { "fi.weatherapp" })
@EnableCaching
public class Application {

	@Resource
	private UpdaterService updaterService;

	private static Logger logger = Logger.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// weather model is updated  at 00, 06, 12 and 18 UTC. Let's wait 15 mins after update and then refresh the forecasts	
	@Scheduled(cron = "0 15 0,6,12,18 * * *")
	private void updateStationData() {
		try {
			logger.debug("Fetching fresh data from FMI, current time is:"+new Date());
			updaterService.fetchDataForCities();
		} catch (Exception e) {
			logger.error("Failed to fetch new forecasts:");
			logger.error(e.getMessage());
		}
	}
}
