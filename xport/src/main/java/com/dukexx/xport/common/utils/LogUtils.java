package com.dukexx.xport.common.utils;


import org.slf4j.Logger;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class LogUtils {
    public static final String EXCEPTIONFROM="exception from method:";
    public static final String EXCEPTIONDESC = ", desc:";
    public static void buildMethodErrorLog(Logger log, String method, String desc, Throwable e) {
        log.error(EXCEPTIONFROM+method+EXCEPTIONDESC+desc,e);
    }
}
