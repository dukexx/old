package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class NotFoundTableConfigException extends RuntimeException {
    public NotFoundTableConfigException() {
    }

    public NotFoundTableConfigException(String message) {
        super("cannot find tableConfig of tableKey:" + message);
    }

    public NotFoundTableConfigException(String message, Throwable cause) {
        super("cannot find tableConfig of tableKey" + message, cause);
    }

    public NotFoundTableConfigException(Throwable cause) {
        super(cause);
    }

    public NotFoundTableConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
