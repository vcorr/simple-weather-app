package fi.weatherapp.service;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import fi.weatherapp.TestApplication;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestApplication.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
public class UpdaterServiceTest {

	@Resource
	private UpdaterService updaterService;

	@Value("${fmi.url}")
	private String FMIURL;

	@Test
	@DatabaseSetup(value = "/dbunit/startData.xml", type = DatabaseOperation.CLEAN_INSERT)
	@ExpectedDatabase(value = "/dbunit/endData.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
	@DatabaseTearDown(value = "/dbunit/EmptyData.xml", type = DatabaseOperation.DELETE_ALL)
	public void testFetchingNewForecastsToEmptyDB() throws Exception {
		updaterService.fetchDataForCities();
	}

	@Test
	@DatabaseSetup(value = "/dbunit/updatableForecasts.xml", type = DatabaseOperation.CLEAN_INSERT)
	@ExpectedDatabase(value = "/dbunit/endData.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
	@DatabaseTearDown(value = "/dbunit/EmptyData.xml", type = DatabaseOperation.DELETE_ALL)
	public void testFetchingNewForecastsAndUpdating() throws Exception {
		updaterService.fetchDataForCities();
	}

	
	@Test
	@DatabaseSetup(value = "/dbunit/updatableForecastsWithStale.xml", type = DatabaseOperation.CLEAN_INSERT)
	@ExpectedDatabase(value = "/dbunit/endData.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
	@DatabaseTearDown(value = "/dbunit/EmptyData.xml", type = DatabaseOperation.DELETE_ALL)
	public void testFetchingNewForecastsAndDeleteStaleEntries()
			throws Exception {
		updaterService.fetchDataForCities();
	}

}
