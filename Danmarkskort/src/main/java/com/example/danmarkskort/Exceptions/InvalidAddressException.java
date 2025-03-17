package com.example.danmarkskort.Exceptions;

public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException(String[] address) {
        super(address[0] + " " + address[1] + " " + address[2] + " " + address[3] + " | Exception: Address doesn't follow format!");
    }
}
