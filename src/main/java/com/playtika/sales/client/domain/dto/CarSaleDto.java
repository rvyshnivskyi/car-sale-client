package com.playtika.sales.client.domain.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CarSaleDto {
    @CsvBindByName(required = true)
    private String brand;
    @CsvBindByName(required = true)
    private String color;
    @CsvBindByName(required = true)
    private int year;
    @CsvBindByName(required = true)
    private String number;
    @CsvBindByName(required = true)
    private double price;
    @CsvBindByName(column = "firstName", required = true)
    private String ownerFirstName;
    @CsvBindByName(column = "phoneNumber", required = true)
    private String ownerPhoneNumber;
    @CsvBindByName(column = "lastName")
    private String ownerLastName;
}
