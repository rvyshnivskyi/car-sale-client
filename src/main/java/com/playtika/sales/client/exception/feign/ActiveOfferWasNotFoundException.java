package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class ActiveOfferWasNotFoundException extends FeignException {
    public ActiveOfferWasNotFoundException() {
        super("Active offer with this id was not found");
    }
}
