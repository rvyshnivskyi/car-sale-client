package com.playtika.sales.client.service;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface CarSalesExtractorService {
    List extractAllCarSales(URL carsFileUrl) throws IOException;
}
