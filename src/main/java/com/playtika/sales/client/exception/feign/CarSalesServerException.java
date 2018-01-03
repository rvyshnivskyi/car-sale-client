package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class CarSalesServerException extends FeignException {
    public CarSalesServerException(int status, String message) {
        super(status, message);
    }
}