package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class IllegalFieldConfigException extends RuntimeException {

    public IllegalFieldConfigException() {
    }

    public IllegalFieldConfigException(String message) {
        super(message);
    }

    public IllegalFieldConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalFieldConfigException(Throwable cause) {
        super(cause);
    }

    public IllegalFieldConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
