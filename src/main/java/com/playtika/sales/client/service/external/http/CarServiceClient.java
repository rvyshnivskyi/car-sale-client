package com.playtika.sales.client.service.external.http;

import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.domain.Person;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface CarServiceClient {
    @RequestLine("POST /cars?price={price}&firstName={firstName}&phone={phone}&lastName={lastName}")
    @Headers("Content-Type: application/json")
    long addCarWithSaleDetails(@Param("price") double price,
                                      @Param("firstName") String ownerFirstName,
                                      @Param("phone") String ownerPhoneNumber,
                                      @Param("lastName") String ownerLastName,
                                      Car car);
    @RequestLine("GET /cars")
    List<Car> getAllCars();

    @RequestLine("GET /cars/{carId}/owner")
    Person getCarOwner(@Param("carId") long carId);
}
