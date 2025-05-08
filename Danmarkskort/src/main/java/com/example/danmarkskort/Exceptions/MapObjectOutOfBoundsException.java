package com.example.danmarkskort.Exceptions;

public class MapObjectOutOfBoundsException extends RuntimeException {
    ///Exception for when the node is out of bounds of the render-space (eg. tilegrid)
    public MapObjectOutOfBoundsException(String message) {
        super(message);
    }
}
