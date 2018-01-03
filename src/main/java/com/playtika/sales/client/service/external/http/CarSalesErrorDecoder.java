package com.playtika.sales.client.service.external.http;

import com.playtika.sales.client.exception.feign.CarIsAlreadyExistException;
import com.playtika.sales.client.exception.feign.CarSalesClientException;
import com.playtika.sales.client.exception.feign.CarSalesServerException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import static feign.FeignException.errorStatus;

public class CarSalesErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            if (response.status() == HttpStatus.CONFLICT.value()) {
                return new CarIsAlreadyExistException();
            }
            return new CarSalesClientException(
                    response.status(),
                    response.reason()
            );
        }
        if (response.status() >= 500 && response.status() <= 599) {
            return new CarSalesServerException(
                    response.status(),
                    response.reason()
            );
        }
        return errorStatus(methodKey, response);
    }
}
