package com.dukexx.xport.exportprocessor.datareader;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface DataReader<T> extends Serializable {

    /**
     * 获取源数据
     * @return
     */
    Object getSourceData();

    /**
     * 根据行索引和列字段获取值
     * @param row
     * @param field
     * @return
     */
    Object getValue(int row,String field);

    /**
     * 将指针移动到下一行，如果下一行不存在，则返回false，否则返回true，初始为第一行
     * @return
     */
    boolean next();

    /**
     * 在当前行获取指定字段值
     * @param field
     * @return
     */
    Object getCurValue(String field);

    /**
     * 获取当前指针所在行索引
     * @return
     */
    int getCurRow();

    /**
     * 获取当前指针所指行数据
     * @return
     */
    T getCurRowData();

    /**
     * 获取长度
     * @return
     */
    int getSize();
}
