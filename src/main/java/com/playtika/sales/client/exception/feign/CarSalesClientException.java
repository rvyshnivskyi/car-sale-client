package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class CarSalesClientException extends FeignException {
    public CarSalesClientException(int status, String message) {
        super(status, message);
    }
}
