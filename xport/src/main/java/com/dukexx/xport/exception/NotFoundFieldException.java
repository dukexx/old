package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class NotFoundFieldException extends RuntimeException {

    public NotFoundFieldException() {
    }

    public NotFoundFieldException(String message) {
        super("cannot find property:"+message+"from data");
    }

    public NotFoundFieldException(String message, Throwable cause) {
        super("cannot find property:"+message+"from data", cause);
    }

    public NotFoundFieldException(Throwable cause) {
        super(cause);
    }

    public NotFoundFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("cannot find property:"+message+"from data", cause, enableSuppression, writableStackTrace);
    }

}
