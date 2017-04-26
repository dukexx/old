package com.dukexx.xport.headresolver;

import com.dukexx.xport.common.CellData;
import com.dukexx.xport.confighelper.TableConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface HeadResolver extends Serializable {

    /**
     * 判断表头是否结束
     * @return
     */
    boolean isHeadEnd(Map<String,CellData> cellDataMap, TableConfig tableConfig);

    /**
     * 根据excel表头字段-field结果字段映射，和已存在的column-field映射，返回column-field映射
     *
     * @return
     */
    Map<String, List<String>> getColumnFieldsMap(Map<String,List<String>> oldColumnFieldsMap);

    /**
     * 根据excel表头字段-field结果字段映射，和已存在的index-field数组，返回index-field数组，数组索引即表中索引
     *
     * @return
     */
    List<String>[] getIndexFieldsMap(List<String>[] oldIndexFieldArray);
}
