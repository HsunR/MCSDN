package com.blog.service.impl;

public class SpamException extends RuntimeException {
    public SpamException() {
        super();
    }
    public SpamException(String message) {
        super(message);
    }
}
