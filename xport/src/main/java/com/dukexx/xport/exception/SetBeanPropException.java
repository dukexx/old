package com.dukexx.xport.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class SetBeanPropException extends RuntimeException {
    public SetBeanPropException() {
    }

    public SetBeanPropException(String message) {
        super(message);
    }

    public SetBeanPropException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetBeanPropException(Throwable cause) {
        super(cause);
    }

    public SetBeanPropException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
