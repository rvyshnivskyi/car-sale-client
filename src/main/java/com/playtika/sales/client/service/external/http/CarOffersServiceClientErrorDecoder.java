package com.playtika.sales.client.service.external.http;

import com.playtika.sales.client.exception.feign.ActiveOfferWasNotFoundException;
import com.playtika.sales.client.exception.feign.CarOffersServiceClientException;
import com.playtika.sales.client.exception.feign.CarOffersServiceServerException;
import com.playtika.sales.client.exception.feign.ThereAreNotOpenSalesProposeForThisCarException;
import feign.Response;
import feign.codec.ErrorDecoder;

import static feign.FeignException.errorStatus;

public class CarOffersServiceClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            if (response.status() == 404 && response.reason().contains("Active offer")) {
                return new ActiveOfferWasNotFoundException();
            }
            if (response.status() == 404 && response.reason().contains("sale")) {
                return new ThereAreNotOpenSalesProposeForThisCarException();
            }
            return new CarOffersServiceClientException(
                    response.status(),
                    response.reason()
            );
        }
        if (response.status() >= 500 && response.status() <= 599) {
            return new CarOffersServiceServerException(
                    response.status(),
                    response.reason()
            );
        }
        return errorStatus(methodKey, response);
    }
}
