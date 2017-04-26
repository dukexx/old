package com.dukexx.xport.importprocessor;

import com.dukexx.xport.common.ImportConf;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.confighelper.MapperLoader;
import com.dukexx.xport.posthandler.ImportPostHandler;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface ImportProcessor extends Serializable {

    /**
     * 处理第一个sheet
     *
     * @param file
     * @return
     */
    ImportPostHandler processOneSheet(File file, ImportConf importModel);

    /**
     * 处理第一个sheet
     *
     * @param inputStream
     * @return
     */
    ImportPostHandler processOneSheet(InputStream inputStream, ImportConf importModel);


    /**
     * 处理多条sheet，指定sheet数量，并指定是否连续(即是否每个sheet都重新解析表头)
     *
     * @param file
     * @param start       起始sheet索引，从0开始
     * @param count       数量，如果为-1，表示处理全部
     * @param importModel
     * @return
     */
    ImportPostHandler processSheets(File file, int start, int count, ImportConf importModel);

    /**
     * 处理多条sheet，指定sheet数量，并指定是否连续(即是否每个sheet都重新解析表头)
     *
     * @param inputStream
     * @param start       起始sheet索引，从0开始
     * @param count       数量，如果为-1，表示处理全部
     * @param importModel
     * @return
     */
    ImportPostHandler processSheets(InputStream inputStream, int start, int count, ImportConf importModel);

    /**
     * 根据sheet索引-tableKey表处理sheet，没有分配tableKey的索引使用上一次tableKey配置
     *
     * @param file
     * @param sheetModelConfs
     * @return
     */
    List<ImportPostHandler> processDiffSheets(File file, Map<Integer, ImportConf> sheetModelConfs);

    /**
     * 根据sheet索引-tableKey表处理sheet，没有分配tableKey的索引使用上一次tableKey配置
     *
     * @param inputStream
     * @param sheetModelConfs
     * @return
     */
    List<ImportPostHandler> processDiffSheets(InputStream inputStream, Map<Integer, ImportConf> sheetModelConfs);

    /**
     * 设置mapper资源加载器
     *
     * @param xMapperLoader
     */
    void setxMapperLoader(MapperLoader xMapperLoader);

    /**
     * 添加临时的新tableConfig，临时的tableConfig优先于原有的，但是仅在该对象内有效
     *
     * @param tableConfig
     */
    public void addTableConfig(String tableKey, TableConfig tableConfig);
}
