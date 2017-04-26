package com.dukexx.xport.factory;

import com.dukexx.xport.common.ExcelFormat;
import com.dukexx.xport.exception.NotFoundMapperLoaderException;
import com.dukexx.xport.exportprocessor.ExportProcessor;
import com.dukexx.xport.importprocessor.DefaultXlsxImEventProcessor;
import com.dukexx.xport.importprocessor.ImportProcessor;
import com.dukexx.xport.confighelper.MapperLoader;
import com.dukexx.xport.exportprocessor.DefaultExportProcessor;
import com.dukexx.xport.importprocessor.DefaultXlsImEventMapProcessor;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class ProcessorFactory {

    private MapperLoader mapperLoader;

    public ProcessorFactory() {

    }

    public MapperLoader getMapperLoader() {
        return mapperLoader;
    }

    /**
     * 设置XlsMapperResourceLoader
     * @param mapperLoader
     */
    public void setMapperLoader(MapperLoader mapperLoader) {
        this.mapperLoader = mapperLoader;
    }

    /**
     * 获取默认map导出处理器
     * @param excelFormat
     * @return
     */
    public ImportProcessor getDefaultImportProcessor(ExcelFormat excelFormat) {
        if(excelFormat==null)
            throw new IllegalArgumentException();
        ImportProcessor importProcessor = null;
        if (excelFormat.equals(ExcelFormat.XLSX))
            importProcessor = new DefaultXlsxImEventProcessor();
        else
            importProcessor = new DefaultXlsImEventMapProcessor();
        importProcessor.setxMapperLoader(getRequireMapperLoader());
        return importProcessor;
    }

    /**
     * 获取处理xlsx默认map导出处理器
     * @return
     */
    public ImportProcessor getDefaultXlsxImportProcessor() {
        return getDefaultImportProcessor(ExcelFormat.XLSX);
    }

    /**
     * 获取处理xls默认map导出处理器
     * @return
     */
    public ImportProcessor getDefaultXlsImportProcessor() {
        return getDefaultImportProcessor(ExcelFormat.XLS);
    }

    /**
     * 根据指定的处理器的class对象创建处理器，用于设置xlsMapperResourceLoader
     * @return
     */
    public ImportProcessor getCustomImportProcessor(Class<? extends ImportProcessor> clazz) {
        try {
            ImportProcessor importProcessor = clazz.newInstance();
            importProcessor.setxMapperLoader(getRequireMapperLoader());
            return importProcessor;
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //导出

    /**
     * 获取默认的导出处理器
     * @return
     */
    public ExportProcessor getDefaultExportProcessor() {
        ExportProcessor exportProcessor = new DefaultExportProcessor();
        exportProcessor.setMapperLoader(getRequireMapperLoader());
        return exportProcessor;
    }

    /**
     * 获取自定义导出处理器，必须含有无参构造，只进行XMapperLoader的注入
     * @param clazz
     * @return
     */
    public ExportProcessor getCustomExportProcessor(Class<? extends ExportProcessor> clazz) {
        try {
            ExportProcessor exportProcessor = clazz.newInstance();
            exportProcessor.setMapperLoader(getRequireMapperLoader());
            return exportProcessor;
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取本地XMapperLoader，如果xMapperLoader为null，抛出异常
     * @return
     */
    private MapperLoader getRequireMapperLoader() {
        if (mapperLoader == null) {
                throw new NotFoundMapperLoaderException("ProcessorFactory");
        }
        return mapperLoader;
    }
}
