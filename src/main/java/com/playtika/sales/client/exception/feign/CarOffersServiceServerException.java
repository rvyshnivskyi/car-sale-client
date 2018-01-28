package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class CarOffersServiceServerException extends FeignException {
    public CarOffersServiceServerException(int status, String reason) {
        super(status, reason);
    }
}
