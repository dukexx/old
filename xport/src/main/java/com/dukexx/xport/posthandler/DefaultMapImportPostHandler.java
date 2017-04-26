package com.dukexx.xport.posthandler;

import com.dukexx.xport.common.CellFormat;
import com.dukexx.xport.common.ParseInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class DefaultMapImportPostHandler implements ImportPostHandler{
    private List data = new LinkedList<>();
    private List<Map<String, CellFormat>> dataDesc = new LinkedList<>();
    private String tableKey;


    @Override
    public boolean importPostHand(Object data, Object dataDesc, ParseInfo parseInfo, Object... args) {
        this.data.add( data);
        this.dataDesc.add((Map<String, CellFormat>) dataDesc);
        if (tableKey == null) {
            this.tableKey = parseInfo.getTableKey();
        }
        return false;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public Object getDataDesc() {
        return dataDesc;
    }

    @Override
    public String getTableKey() {
        return tableKey;
    }
}
