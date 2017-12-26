package com.playtika.sales.client.web;

import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.domain.dto.CarSaleDto;
import com.playtika.sales.client.service.CarSalesExtractorService;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;

@AllArgsConstructor
@RestController
@Slf4j
public class CarServiceClientController {

    private final CarSalesExtractorService carSalesExtractor;
    private final CarServiceClient carServiceClient;

    @PostMapping(value = "/cars", consumes = "text/plain;charset=UTF-8")
    public long addCarsSales(@RequestBody String carsCsvFileUrl) throws NotPossibleToDownloadFileException {
        List<CarSaleDto> carSales;
        try {
            carSales = carSalesExtractor.extractAllCarSales(new URL(carsCsvFileUrl));
        } catch (IOException ex) {
            throw new NotPossibleToDownloadFileException(carsCsvFileUrl);
        }

        return carSales.stream()
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
        try {
            log.info("Try to add car sale using feign client");
            return carServiceClient.addCarWithSaleDetails(carSale.getPrice(), carSale.getOwnerFirstName(), carSale.getOwnerPhoneNumber(), carSale.getOwnerLastName(), car) > 0;
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.CONFLICT.value()) {
                log.error(format("Car with plateNumber=[%s] can't be added, cause car with the same plateNumber is already exist", carSale.getNumber()));
            } else {
                log.error("Can't add car sales details with next parameters: " + carSale.toString(), ex);
            }
            return false;
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class NotPossibleToDownloadFileException extends IOException {
        public NotPossibleToDownloadFileException(String url) {
            super(format("Troubles with downloading csv-file from url=[%s]. Please check the url and retry", url));
        }
    }
}
