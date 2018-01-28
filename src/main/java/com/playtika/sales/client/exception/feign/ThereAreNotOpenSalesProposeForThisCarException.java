package com.playtika.sales.client.exception.feign;

import feign.FeignException;

public class ThereAreNotOpenSalesProposeForThisCarException extends FeignException {
    public ThereAreNotOpenSalesProposeForThisCarException() {
        super("There aren't any open sales for this car");
    }
}
