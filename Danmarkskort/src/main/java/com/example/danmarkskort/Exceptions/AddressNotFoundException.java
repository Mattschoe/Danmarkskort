package com.example.danmarkskort.Exceptions;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String message) {
        super("this caused exception " + message); //to be changed
    }
}
