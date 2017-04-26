package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class ImportFailException extends RuntimeException {

    public ImportFailException() {
    }

    public ImportFailException(String message) {
        super(message);
    }

    public ImportFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportFailException(Throwable cause) {
        super(cause);
    }

    public ImportFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
