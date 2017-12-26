package com.playtika.sales.client.web;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.playtika.sales.client.service.CarSalesExtractorService;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarServiceClientControllerSystemTest {
    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @Autowired
    CarServiceClient client;

    @Autowired
    CarSalesExtractorService extractor;

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8091));

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void successfulRegistrationCarSales() throws Exception {
        stubFor(WireMock.post("/cars?price=2003.1&firstName=Roma&phone=380960000000&lastName=Vyshnivskyi")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(getCarJSON("AA3295")))
                .willReturn(ok("1")));
        stubFor(WireMock.post("/cars?price=2003.1&firstName=Roma&phone=380960000000&lastName=Vyshnivskyi")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(getCarJSON("AA3296")))
                .willReturn(ok("2")));

        long result = Long.valueOf(
                mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                        .contentType("text/plain;charset=UTF-8")
                        .content(new File("src/test/java/resources/cars.csv").toURL().toString()))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
        assertThat(result, Matchers.is(0L));
    }

    String getCarJSON(final String number) {
        return "{\"brand\":\"BMW\",\"color\":\"red\",\"year\":2001,\"number\":\"" + number + "\"}";
    }
}
