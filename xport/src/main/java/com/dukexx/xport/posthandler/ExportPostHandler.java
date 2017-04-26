package com.dukexx.xport.posthandler;

import com.dukexx.xport.common.ExportInfo;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface ExportPostHandler extends Serializable{

    Integer getDataRow();

    void postHand(ExportInfo exportInfo, Object... args);
}
