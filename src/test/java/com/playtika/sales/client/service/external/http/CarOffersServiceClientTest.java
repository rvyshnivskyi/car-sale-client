package com.playtika.sales.client.service.external.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.playtika.sales.client.domain.Offer;
import com.playtika.sales.client.exception.feign.ActiveOfferWasNotFoundException;
import com.playtika.sales.client.exception.feign.CarOffersServiceClientException;
import com.playtika.sales.client.exception.feign.CarOffersServiceServerException;
import com.playtika.sales.client.exception.feign.ThereAreNotOpenSalesProposeForThisCarException;
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
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static wiremock.com.google.common.net.HttpHeaders.CONTENT_TYPE;

@ActiveProfiles("test")
public class CarOffersServiceClientTest {

    CarOffersClient feignClient;

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8091));

    @Before
    public void setUp() throws Exception {
        feignClient = Feign.builder()
                .errorDecoder(new CarOffersServiceClientErrorDecoder())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(CarOffersClient.class, "http://localhost:8091");
    }

    @Test
    public void registrationOfferForCarReturnsOfferId() throws Exception {
        long carId = 1L;
        long offerId = 2L;
        String buyerFirstName = "Roma";
        stubFor(WireMock.post("/cars/" + carId)
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(new Gson().toJson(generateOffer(null, buyerFirstName))))
        .willReturn(ok("" + offerId)));

        long result = feignClient.addOffer(carId, generateOffer(null, buyerFirstName));
        assertThat(result, is(offerId));
    }

    @Test(expected = ThereAreNotOpenSalesProposeForThisCarException.class)
    public void unSuccessfulRegistrationOfferForCarThrowsExceptionIfNotFound() throws Exception {
        long carId = 1L;
        String buyerFirstName = "Roma";
        stubFor(WireMock.post("/cars/" + carId)
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(new Gson().toJson(generateOffer(null, buyerFirstName))))
        .willReturn(notFound().withStatusMessage("There aren't any open sale proposes for this car")));

        feignClient.addOffer(carId, generateOffer(null, buyerFirstName));
    }

    @Test(expected = CarOffersServiceServerException.class)
    public void unSuccessfulRegistrationOfferForCarCauseServerErrorThrowsException() throws Exception {
        long carId = 1L;
        String buyerFirstName = "Roma";
        stubFor(WireMock.post("/cars/" + carId)
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(new Gson().toJson(generateOffer(null, buyerFirstName))))
        .willReturn(serverError()));

        feignClient.addOffer(carId, generateOffer(null, buyerFirstName));
    }

    @Test(expected = CarOffersServiceClientException.class)
    public void unSuccessfulRegistrationOfferForCarCauseClientErrorThrowsException() throws Exception {
        long carId = 1L;
        String buyerFirstName = "Roma";
        stubFor(WireMock.post("/cars/" + carId)
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(new Gson().toJson(generateOffer(null, buyerFirstName))))
        .willReturn(badRequest()));

        feignClient.addOffer(carId, generateOffer(null, buyerFirstName));
    }

    @Test
    public void activeOffersForCarSuccessfullyReturns() throws Exception {
        long carId = 4L;
        stubFor(WireMock.get("/cars/" + carId + "/offers")
                .willReturn(ok(new Gson().toJson(asList(generateOffer(1L, "Roma"), generateOffer(2L, "Vasia"))))));

        List<Offer> result = feignClient.getOffersForCar(carId);
        assertThat(result, hasSize(2));
        assertThat(result, hasItems(is(generateOffer(1L, "Roma")), is(generateOffer(2L, "Vasia"))));
    }

    @Test(expected = CarOffersServiceServerException.class)
    public void unSuccessfulGettingOffersForCarCauseServerErrorThrowsException() throws Exception {
        long carId = 4L;
        stubFor(WireMock.get("/cars/" + carId + "/offers")
                .willReturn(serverError()));

        feignClient.getOffersForCar(carId);
    }

    @Test
    public void offerSuccessfullyAccepted() throws Exception {
        long offerId = 3L;
        long newOwnerId = 2L;
        stubFor(WireMock.put("/offers/" + offerId)
                .willReturn(ok("" + newOwnerId)));

        long result = feignClient.acceptActiveOffer(offerId);
        assertThat(result, is(newOwnerId));
    }

    @Test(expected = ActiveOfferWasNotFoundException.class)
    public void offerUnSuccessfullyAcceptedThrowsExceptionIfNotFound() throws Exception {
        long offerId = 3L;
        stubFor(WireMock.put("/offers/" + offerId)
                .willReturn(notFound().withStatusMessage("Active offer with this id was not found")));

        feignClient.acceptActiveOffer(offerId);
    }

    private Offer generateOffer(Long id, String buyerFirstName) {
        return Offer.builder()
                .id(id)
                .buyerFirstName(buyerFirstName)
                .buyerLastName("lastName")
                .buyerPhoneNumber("0000")
                .price(222.2)
                .build();
    }
}