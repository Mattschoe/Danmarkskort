package com.example.danmarkskort.Exceptions;

public class ParserSavingException extends RuntimeException {
    ///Exception for when there has occurred an error in either saving the parser to .obj or its values to .bin files
    public ParserSavingException(String message) {
        super(message);
    }
}
