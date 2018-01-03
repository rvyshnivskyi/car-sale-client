package com.playtika.sales.client.exception.feign;

public class CarIsAlreadyExistException extends RuntimeException {
    public CarIsAlreadyExistException() {
        super("Car with the same params is already exist");
    }
}
