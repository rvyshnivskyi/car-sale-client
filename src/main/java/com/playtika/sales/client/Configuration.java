package com.playtika.sales.client;

import com.playtika.sales.client.service.external.http.CarServiceClient;
import com.playtika.sales.client.service.external.http.CarServiceClientImpl;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Value("${car.service.url}")
    private String carServiceUrl;

    @Bean
    CarServiceClient carServiceClient() {
        CarServiceClient feignClient = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(CarServiceClient.class, carServiceUrl);
        return new CarServiceClientImpl(feignClient);
    }
}
