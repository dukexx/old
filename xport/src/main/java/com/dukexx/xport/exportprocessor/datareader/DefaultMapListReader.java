package com.dukexx.xport.exportprocessor.datareader;

import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class DefaultMapListReader extends DefaultListReader<Map>{
    private static final long serialVersionUID = 1L;

    public DefaultMapListReader(List<Map> sourceData) {
        this.sourceData = sourceData;
        length = sourceData.size();
        if (length != 0) {
            curRowData = sourceData.get(0);
        }
        iterator = sourceData.iterator();
    }

    @Override
    public Object getValue(int row, String field) {
        Map rowData = sourceData.get(row);
        if (rowData == null) {
            return null;
        }
        return rowData.get(field);
    }

    @Override
    public Object getCurValue(String field) {
        if (curRow >= length) {
            throw new IndexOutOfBoundsException("cur row index out of record count");
        }
        if (curRowData == null) {
            return null;
        }
        return curRowData.get(field);
    }
}
