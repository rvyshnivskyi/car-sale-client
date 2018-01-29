package com.playtika.sales.client.service.external.http;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.domain.Person;
import com.playtika.sales.client.exception.feign.CarIsAlreadyExistException;
import com.playtika.sales.client.exception.feign.CarSalesClientException;
import com.playtika.sales.client.exception.feign.CarSalesServerException;
import com.playtika.sales.client.exception.feign.CarWithInsertedIdWasNotFoundException;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
        assertThat(firstCarId, is(1L));
        assertThat(secondCarId, is(2L));
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

    @Test(expected = CarSalesServerException.class)
    public void unSuccessfulReturningAllCarsCauseServerErrorThrowsException() throws Exception {
        stubFor(WireMock.get("/cars")
                .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
        feignClient.getAllCars();
    }

    @Test
    public void feignClientSuccessfullyReturnsAllCars() throws Exception {
        stubFor(WireMock.get("/cars")
                .willReturn(ok("[" + getCarJSONWithId("AA3295", 1L) + ","
                        + getCarJSONWithId("AA3296", 2L) + "]")));
        List<Car> result = feignClient.getAllCars();
        assertThat(result, hasSize(2));
        assertThat(result, hasItems(is(generateCar("AA3295", 1L)), is(generateCar("AA3296", 2L))));
    }

    @Test
    public void feignClientReturnsEmptyListIfThereAreNotAnyCar() throws Exception {
        stubFor(WireMock.get("/cars")
                .willReturn(ok("[]")));
        List<Car> result = feignClient.getAllCars();
        assertThat(result, empty());
    }

    @Test
    public void carOwnerSuccessfullyReturns() throws Exception {
        long carId = 1L;
        long ownerId = 2L;
        stubFor(WireMock.get("/cars/" + carId + "/owner")
                .willReturn(ok(getPersonJSON(ownerId))));
        Person result = feignClient.getCarOwner(carId);
        assertThat(result, is(generatePerson(ownerId)));
    }

    @Test(expected = CarSalesServerException.class)
    public void unSuccessfulReturningCarOwnerCauseServerErrorThrowsException() throws Exception {
        long carId = 1L;
        stubFor(WireMock.get("/cars/" + carId + "/owner")
                .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
        feignClient.getCarOwner(carId);
    }

    @Test(expected = CarWithInsertedIdWasNotFoundException.class)
    public void unSuccessfulReturningCarOwnerCauseCarNotFoundThrowsException() throws Exception {
        long carId = 1L;
        stubFor(WireMock.get("/cars/" + carId + "/owner")
                .willReturn(aResponse().withStatus(NOT_FOUND.value())));
        feignClient.getCarOwner(carId);
    }

    private String getPersonJSON(long id) {
        return "{\"id\":" + id + ",\"firstName\":\"Roma\",\"phoneNumber\":\"380960000000\",\"lastName\":\"Vyshnivskyi\"}";
    }

    private Person generatePerson(long id) {
        return Person.builder()
                .firstName("Roma")
                .lastName("Vyshnivskyi")
                .phoneNumber("380960000000")
                .id(id)
                .build();
    }

    String getCarJSONWithId(final String number, long id) {
        return "{\"id\":" + id + ",\"brand\":\"BMW\",\"color\":\"red\",\"year\":2001,\"number\":\"" + number + "\"}";
    }

    void generateStubCarSaleService(String number, ResponseDefinitionBuilder response) {
        stubFor(WireMock.post("/cars?price=2003.1&firstName=Roma&phone=380960000000&lastName=Vyshnivskyi")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(getCarJSON(number)))
                .willReturn(response));
    }

    long addCarUsingFeignClient(String number) {
        return feignClient.addCarWithSaleDetails(2003.1, "Roma", "380960000000", "Vyshnivskyi", generateCar(number, null));
    }

    String getCarJSON(final String number) {
        return "{\"brand\":\"BMW\",\"color\":\"red\",\"year\":2001,\"number\":\"" + number + "\"}";
    }

    Car generateCar(String number, Long id) {
        return Car.builder()
                .id(id)
                .number(number)
                .year(2001)
                .color("red")
                .brand("BMW")
                .build();
    }
}