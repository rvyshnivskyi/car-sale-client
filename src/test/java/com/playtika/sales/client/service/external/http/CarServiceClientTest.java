package com.playtika.sales.client.service.external.http;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.exception.feign.CarIsAlreadyExistException;
import com.playtika.sales.client.exception.feign.CarSalesClientException;
import com.playtika.sales.client.exception.feign.CarSalesServerException;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ActiveProfiles("test")
public class CarServiceClientTest {

    CarServiceClient feignClient;

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8091));

    @Before
    public void setUp() throws Exception {
        feignClient = Feign.builder()
                .errorDecoder(new CarSalesErrorDecoder())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(CarServiceClient.class, "http://localhost:8091");
    }

    @Test
    public void successfulRegistrationCarSales() throws Exception {
        generateStubCarSaleService("AA3295", ok("1"));
        generateStubCarSaleService("AA3296", ok("2"));
        long firstCarId = addCarUsingFeignClient("AA3295");
        long secondCarId = addCarUsingFeignClient("AA3296");
        assertThat(firstCarId, Matchers.is(1L));
        assertThat(secondCarId, Matchers.is(2L));
    }

    @Test(expected = CarIsAlreadyExistException.class)
    public void unSuccessfulRegistrationCauseConflictThrowsDuplicateException() throws Exception {
        generateStubCarSaleService("AA3296", aResponse().withStatus(CONFLICT.value()));
        addCarUsingFeignClient("AA3296");
    }

    @Test(expected = CarSalesClientException.class)
    public void unSuccessfulRegistrationCauseBadRequestThrowsException() throws Exception {
        generateStubCarSaleService("AA3296", aResponse().withStatus(BAD_REQUEST.value()));
        addCarUsingFeignClient("AA3296");
    }

    @Test(expected = CarSalesServerException.class)
    public void unSuccessfulRegistrationCauseServerErrorThrowsException() throws Exception {
        generateStubCarSaleService("AA3296", aResponse().withStatus(INTERNAL_SERVER_ERROR.value()));
        addCarUsingFeignClient("AA3296");
    }

    void generateStubCarSaleService(String number, ResponseDefinitionBuilder response) {
        stubFor(WireMock.post("/cars?price=2003.1&firstName=Roma&phone=380960000000&lastName=Vyshnivskyi")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(getCarJSON(number)))
                .willReturn(response));
    }

    long addCarUsingFeignClient(String number) {
        return feignClient.addCarWithSaleDetails(2003.1, "Roma", "380960000000", "Vyshnivskyi", generateCar(number));
    }

    String getCarJSON(final String number) {
        return "{\"brand\":\"BMW\",\"color\":\"red\",\"year\":2001,\"number\":\"" + number + "\"}";
    }

    Car generateCar(String number) {
        return Car.builder()
                .number(number)
                .year(2001)
                .color("red")
                .brand("BMW")
                .build();
    }
}