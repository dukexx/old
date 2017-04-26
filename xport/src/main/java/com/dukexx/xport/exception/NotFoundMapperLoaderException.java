package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class NotFoundMapperLoaderException extends RuntimeException{

    public NotFoundMapperLoaderException() {
    }

    public NotFoundMapperLoaderException(String message) {
        super("the mapperLoader of "+message+"cannot be null");
    }

    public NotFoundMapperLoaderException(String message, Throwable cause) {
        super("the mapperLoader of "+message+"cannot be null", cause);
    }

    public NotFoundMapperLoaderException(Throwable cause) {
        super(cause);
    }

    public NotFoundMapperLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
