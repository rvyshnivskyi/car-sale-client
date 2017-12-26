package com.playtika.sales.client.service.external.http;

import com.playtika.sales.client.domain.Car;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CarServiceClient {
    @RequestLine("POST /cars?price={price}&firstName={firstName}&phone={phone}&lastName={lastName}")
    @Headers("Content-Type: application/json")
    public long addCarWithSaleDetails(@Param("price") double price,
                                      @Param("firstName") String ownerFirstName,
                                      @Param("phone") String ownerPhoneNumber,
                                      @Param("lastName") String ownerLastName,
                                      Car car);
}
