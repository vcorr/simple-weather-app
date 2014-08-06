package fi.weatherapp.controller;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import fi.weatherapp.TestApplication;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = { TestApplication.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
public class ForecastControllerTest {

	private MockMvc mockMvc;
	
	@Autowired 
	WebApplicationContext wac; 

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	@Test
	@DatabaseSetup(value = "/dbunit/startDataForController.xml", type = DatabaseOperation.CLEAN_INSERT)
	@ExpectedDatabase(value = "/dbunit/startDataForController.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
	@DatabaseTearDown(value = "/dbunit/EmptyData.xml", type = DatabaseOperation.DELETE_ALL)
	public void testFindAll() throws Exception{
		mockMvc.perform(get("/forecast/cities"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].cityName", is("Ylivieska")))
                .andExpect(jsonPath("$[0].cityCoords[lat]", is(1.0)))
                .andExpect(jsonPath("$[0].cityCoords[lon]", is(1.0)))
                .andExpect(jsonPath("$[0].forecasts", hasSize(0)))
                .andExpect(jsonPath("$[1].cityName", is("Mariehamn")))
                .andExpect(jsonPath("$[1].cityCoords[lat]", is(1.0)))
                .andExpect(jsonPath("$[1].cityCoords[lon]", is(1.0)))
                .andExpect(jsonPath("$[1].forecasts", hasSize(3)))
                .andExpect(jsonPath("$[1].forecasts[0].forecastDate", is(1406876400000L)))
                .andExpect(jsonPath("$[1].forecasts[0].weatherSymbol", is("2.0")))
                .andExpect(jsonPath("$[1].forecasts[0].tempValue", is("18")))
                .andExpect(jsonPath("$[1].forecasts[1].forecastDate", is(1406880000000L)))
                .andExpect(jsonPath("$[1].forecasts[1].weatherSymbol", is("1.0")))
                .andExpect(jsonPath("$[1].forecasts[1].tempValue", is("22")))
                .andExpect(jsonPath("$[1].forecasts[2].forecastDate", is(1406883600000L)))
                .andExpect(jsonPath("$[1].forecasts[2].weatherSymbol", is("1.0")))
                .andExpect(jsonPath("$[1].forecasts[2].tempValue", is("24")));
	}
}
