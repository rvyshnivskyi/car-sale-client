package com.playtika.sales.client.service.external.http;

import com.playtika.sales.client.domain.Offer;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface CarOffersClient {
    @RequestLine("POST /cars/{id}")
    @Headers("Content-Type: application/json")
    long addOffer(@Param("id") long carId, Offer offer);

    @RequestLine("GET /cars/{id}/offers")
    List<Offer> getOffersForCar(@Param("id") long carId);

    @RequestLine("PUT /offers/{id}")
    long acceptActiveOffer(@Param("id") long offerId);
}
