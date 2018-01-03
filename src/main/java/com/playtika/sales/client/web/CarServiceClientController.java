package com.playtika.sales.client.web;

import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.domain.dto.CarSaleDto;
import com.playtika.sales.client.exception.feign.CarIsAlreadyExistException;
import com.playtika.sales.client.exception.feign.CarSalesClientException;
import com.playtika.sales.client.exception.feign.CarSalesServerException;
import com.playtika.sales.client.service.CarSalesExtractorService;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static java.lang.String.format;

@AllArgsConstructor
@RestController
@Slf4j
public class CarServiceClientController {

    private final CarSalesExtractorService carSalesExtractor;
    private final CarServiceClient carServiceClient;

    @PostMapping(value = "/cars", consumes = "text/plain;charset=UTF-8")
    public long addCarsSales(@RequestBody String carsCsvFileUrl) throws IOException {
        return carSalesExtractor.extractAllCarSales(carsCsvFileUrl).stream()
                .map(this::addCar)
                .filter(r -> !r)
                .count();
    }

    private boolean addCar(CarSaleDto carSale) {
        Car car = Car.builder()
                .brand(carSale.getBrand())
                .color(carSale.getColor())
                .number(carSale.getNumber())
                .year(carSale.getYear())
                .build();
        log.info("Try to add car sale using feign client");
        try {
            log.info("Try to add car sale using feign client");
            return carServiceClient.addCarWithSaleDetails(carSale.getPrice(), carSale.getOwnerFirstName(), carSale.getOwnerPhoneNumber(), carSale.getOwnerLastName(), car) > 0;
        } catch (CarIsAlreadyExistException ex) {
            log.error(format("Car with plateNumber=[%s] can't be added, cause car with the same plateNumber is already exist", carSale.getNumber()));
        } catch (CarSalesClientException ex) {
            log.error("Can't add car sales details with next parameters: " + carSale.toString(), ex);
        } catch (CarSalesServerException ex) {
            log.error("Can't add car sales details with next parameters: " + carSale.toString(), ex);
        }
        return false;
    }
}
