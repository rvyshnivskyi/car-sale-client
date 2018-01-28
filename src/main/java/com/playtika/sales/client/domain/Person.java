package com.playtika.sales.client.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Person {
    private Long id;
    private String firstName;
    private String phoneNumber;
    private String lastName;
}
