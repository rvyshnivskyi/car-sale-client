package com.playtika.sales.client;

import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.domain.Offer;
import com.playtika.sales.client.domain.Person;
import com.playtika.sales.client.service.external.http.CarOffersClient;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = CarSalesClientApplication.class)
@AutoConfigureMockMvc
@Slf4j
public class CarServiceClientEndToEndTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CarServiceClient carServiceClient;

    @Autowired
    CarOffersClient carOffersClient;

    @Test
    public void successfulRegistrationCarSales() throws Exception {
        addNewCarSales();

        long maxCarId = getMaxCarId();
        log.info("Max car id = [" + maxCarId + "]");

        Person carOwnerBeforeSellingTheCar = carServiceClient.getCarOwner(maxCarId);

        log.info("Car owner of car with id = [{}] before selling: {}", maxCarId, carOwnerBeforeSellingTheCar);

        Offer offer = addOffersForCarReturnOfferWithMaxId(maxCarId, asList(
                generateOffer(null, "firstBuyer"),
                generateOffer(null, "secondBuyer")));

        log.info("Offer with max id for car with id = [{}]: {}", maxCarId, offer);

        acceptOffer(offer);

        checkThatOfferWasAcceptedAndCarOwnerChanged(maxCarId,
                carOwnerBeforeSellingTheCar,
                offer.getBuyerFirstName());
    }

    private void checkThatOfferWasAcceptedAndCarOwnerChanged(long carId, Person carOwnerBeforeClosingDeal, String buyerFirstName) {
        List<Offer> activeOffersAfterClosingDeal = carOffersClient.getOffersForCar(carId);
        assertThat(activeOffersAfterClosingDeal, empty());

        Person newCarOwner = carServiceClient.getCarOwner(carId);
        assertThat(newCarOwner, not(carOwnerBeforeClosingDeal));

        assertThat(newCarOwner.getFirstName(), is(buyerFirstName));

        log.info("Offer was successfully accepted. New car owner: {}", newCarOwner);
    }

    private void acceptOffer(Offer offer) {
        log.info("Try to accept test offer: {}", offer);
        carOffersClient.acceptActiveOffer(offer.getId());
    }

    private Offer addOffersForCarReturnOfferWithMaxId(long carId, List<Offer> offers) {
        log.info("Try to add new offers for car wih id = [{}]", carId);
        offers.forEach(offer -> carOffersClient.addOffer(carId, offer));

        List<Offer> addedOffers = carOffersClient.getOffersForCar(carId);

        assertThat(addedOffers, hasSize(offers.size()));
        offers.forEach(offer -> {
            assertThat(addedOffers, hasItem(hasProperty("buyerFirstName", is(offer.getBuyerFirstName()))));
        });

        log.info("{} offers successfully added to the car with id = [{}]. Offers: {}",
                addedOffers.size(),
                carId,
                addedOffers);

        return addedOffers.stream()
                .sorted(Comparator.comparing(Offer::getId).reversed())
                .findFirst()
                .get();
    }

    private void addNewCarSales() throws Exception {
        int carsCountBeforeAddingNewCars = carServiceClient.getAllCars().size();

        log.info("Try to add new car with sale details from CSV file with using feign client and extractor service");
        long result = Long.valueOf(
                mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                        .contentType("text/plain;charset=UTF-8")
                        .content(new File("src/test/java/resources/cars.csv").toURL().toString()))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse()
                        .getContentAsString());

        int carsCountAfterAddingNewCars = carServiceClient.getAllCars().size();

        assertThat(result, Matchers.is(0L));
        assertThat(carsCountAfterAddingNewCars, greaterThan(carsCountBeforeAddingNewCars));

        log.info("New cars with sale details were successfully added. Cars count before adding = [{}]; after = [{}]",
                carsCountBeforeAddingNewCars,
                carsCountAfterAddingNewCars);
    }

    private Offer generateOffer(Long id, String firstName) {
        return Offer.builder()
                .id(id)
                .buyerFirstName(firstName)
                .buyerLastName("testLastName")
                .price(222.2)
                .buyerPhoneNumber("0000")
                .build();
    }

    private long getMaxCarId() {
        return carServiceClient.getAllCars().stream()
                .mapToLong(Car::getId)
                .max().getAsLong();
    }
}
