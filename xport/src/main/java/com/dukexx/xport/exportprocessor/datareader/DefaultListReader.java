package com.dukexx.xport.exportprocessor.datareader;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public abstract class DefaultListReader<T> implements DataReader {

    protected List<T> sourceData;
    //当前所在行
    protected int curRow = -1;
    protected int length;
    protected T curRowData;
    protected Iterator<T> iterator;

    public static <T>DataReader<T> getDataReaderByList(List<T> data) {
        DataReader<T> dataReader = null;
        if (!CollectionUtils.isEmpty(data)) {
            T t = data.get(0);
            if (t instanceof Map) {
                dataReader = new DefaultMapListReader((List<Map>)data);
            } else {
                dataReader = new DefaultBeanListReader(data);
            }
        } else {
            dataReader = new DefaultMapListReader((List<Map>) data);
        }
        return dataReader;
    }

    @Override
    public Object getSourceData() {
        return sourceData;
    }

    @Override
    public boolean next() {
        curRow++;
        boolean hasNext=iterator.hasNext();
        if (hasNext) {
            curRowData=iterator.next();
        }
        return hasNext;
    }

    @Override
    public int getCurRow() {
        return curRow;
    }

    @Override
    public T getCurRowData() {
        return curRowData;
    }

    @Override
    public int getSize() {
        return length;
    }
}
