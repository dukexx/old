package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class IllegalTableConfigException extends RuntimeException{

    public IllegalTableConfigException() {
    }

    public IllegalTableConfigException(String message) {
        super(message);
    }

    public IllegalTableConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTableConfigException(Throwable cause) {
        super(cause);
    }

    public IllegalTableConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
