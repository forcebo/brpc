package com.lwb.exceptions;

public class NetWorkException extends RuntimeException{
    public NetWorkException() {
    }

    public NetWorkException(Throwable cause) {
        super(cause);
    }

    public NetWorkException(String message) {
        super(message);
    }
}
