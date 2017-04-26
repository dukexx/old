package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class ExportFailException extends RuntimeException {

    public ExportFailException() {
    }

    public ExportFailException(String message) {
        super(message);
    }

    public ExportFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportFailException(Throwable cause) {
        super(cause);
    }

    public ExportFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
