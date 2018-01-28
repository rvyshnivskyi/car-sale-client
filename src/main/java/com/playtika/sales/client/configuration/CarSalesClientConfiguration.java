package com.playtika.sales.client.configuration;

import com.playtika.sales.client.service.external.http.CarSalesErrorDecoder;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("car")
public class CarSalesClientConfiguration {

    @Bean
    public CarServiceClient carServiceClient(@Value("${car.service.url}") String carServiceUrl) {
        return Feign.builder()
                .errorDecoder(new CarSalesErrorDecoder())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(CarServiceClient.class, carServiceUrl);
    }
}
