package com.dukexx.xport.posthandler;

import com.dukexx.xport.common.ParseInfo;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface ImportPostHandler extends Serializable {

    boolean importPostHand(Object data, Object dataDesc, ParseInfo parseInfo, Object... args);

    Object getData();

    Object getDataDesc();

    String getTableKey();
}
