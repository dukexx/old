package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class IllegalFileFormatException extends RuntimeException {

    public IllegalFileFormatException() {
    }

    public IllegalFileFormatException(String message) {
        super(message);
    }

    public IllegalFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalFileFormatException(Throwable cause) {
        super(cause);
    }

    public IllegalFileFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
