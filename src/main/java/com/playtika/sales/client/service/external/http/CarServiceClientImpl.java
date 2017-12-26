package com.playtika.sales.client.service.external.http;

import com.playtika.sales.client.domain.Car;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
public class CarServiceClientImpl implements CarServiceClient {
    final CarServiceClient feignClient;

    @Override
    public long addCarWithSaleDetails(double price, String ownerFirstName, String ownerPhoneNumber, String ownerLastName, Car car) {
        return feignClient.addCarWithSaleDetails(price, ownerFirstName, ownerPhoneNumber, ownerLastName, car);
    }
}
