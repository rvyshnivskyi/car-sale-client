package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class CarWithInsertedIdWasNotFoundException extends FeignException {
    public CarWithInsertedIdWasNotFoundException() {
        super("Car with inserted id was not found");
    }
}
