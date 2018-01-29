package com.playtika.sales.client.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Offer {
    private Long id;
    private double price;
    private String buyerFirstName;
    private String buyerPhoneNumber;
    private String buyerLastName;
}
