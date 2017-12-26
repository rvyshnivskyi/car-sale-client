package com.playtika.sales.client.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Car {
    private String brand;
    private String color;
    private int year;
    private String number;
}