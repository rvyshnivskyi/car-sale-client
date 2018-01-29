package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class CarOffersServiceClientException extends FeignException {
    public CarOffersServiceClientException(int status, String reason) {
        super(status, reason);
    }
}
