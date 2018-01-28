package com.playtika.sales.client.configuration;

import com.playtika.sales.client.service.external.http.CarOffersClient;
import com.playtika.sales.client.service.external.http.CarOffersServiceClientErrorDecoder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarOffersServiceClientConfiguration {

    @Bean
    public CarOffersClient carOffersClient(@Value("${car.service.url}") String carServiceUrl) {
        return Feign.builder()
                .errorDecoder(new CarOffersServiceClientErrorDecoder())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(CarOffersClient.class, carServiceUrl);
    }
}
