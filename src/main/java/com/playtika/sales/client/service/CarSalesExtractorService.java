package com.playtika.sales.client.service;

import com.playtika.sales.client.domain.dto.CarSaleDto;

import java.io.IOException;
import java.util.List;

public interface CarSalesExtractorService {
    List <CarSaleDto> extractAllCarSales(String carsFileUrl) throws IOException;
}
