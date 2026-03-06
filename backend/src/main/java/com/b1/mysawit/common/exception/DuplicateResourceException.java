package com.b1.mysawit.common.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s sudah ada dengan %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
