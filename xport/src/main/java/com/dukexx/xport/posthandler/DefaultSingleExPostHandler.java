package com.dukexx.xport.posthandler;

import com.dukexx.xport.common.ExportInfo;

/**
 * not achieve
 *
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Deprecated
public class DefaultSingleExPostHandler implements ExportPostHandler {
    private static final long serialVersionUID = 1L;

    @Override
    public Integer getDataRow() {
        return null;
    }

    @Override
    public void postHand(ExportInfo exportInfo, Object... args) {

    }
}
