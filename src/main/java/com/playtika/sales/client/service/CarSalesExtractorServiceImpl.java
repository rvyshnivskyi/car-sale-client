package com.playtika.sales.client.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.playtika.sales.client.domain.dto.CarSaleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class CarSalesExtractorServiceImpl implements CarSalesExtractorService {
    @Override
    public List<CarSaleDto> extractAllCarSales(URL carsCsvFileUrl) throws IOException {
        FileURLConnection httpConn = (FileURLConnection) carsCsvFileUrl.openConnection();
        try (InputStream in = httpConn.getInputStream()) {
            return new CsvToBeanBuilder(new InputStreamReader(in))
                    .withIgnoreLeadingWhiteSpace(true)
                    .withType(CarSaleDto.class)
                    .build().parse();
        }
    }
}
