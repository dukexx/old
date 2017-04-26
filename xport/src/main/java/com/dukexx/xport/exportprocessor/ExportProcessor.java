package com.dukexx.xport.exportprocessor;

import com.dukexx.xport.common.ExcelFormat;
import com.dukexx.xport.common.ExportConf;
import com.dukexx.xport.common.ExportResult;
import com.dukexx.xport.confighelper.MapperLoader;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.exportprocessor.datareader.DataReader;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface ExportProcessor extends Serializable {

    /**
     * 基本list导入，传list数据，以及要写入的outputStream。
     *
     * @param data
     * @param outputStream
     * @param excelFormat
     * @param tableKey
     * @return
     */
    ExportResult exportToXl(List data, OutputStream outputStream, String tableKey, ExcelFormat excelFormat, Object... args);

    /**
     * 基本通用DataReader，导入过程通过DataReader获取数据。
     *
     * @param dataReader
     * @param outputStream
     * @param tableKey
     * @param excelFormat
     * @param args
     * @return
     */
    ExportResult exportToXl(DataReader dataReader, OutputStream outputStream, String tableKey, ExcelFormat excelFormat, Object... args);

    /**
     * 基本导入重载，可以设置list或dataReader。
     * @param exportConf
     * @param outputStream
     * @param excelFormat
     * @return
     */
    ExportResult exportToXl(ExportConf exportConf, OutputStream outputStream, ExcelFormat excelFormat);

    /**
     * 配置多个sheet顺序导出
     *
     * @param exportConfs
     * @param outputStream
     * @param excelFormat
     * @return
     */
    ExportResult exportToXl(List<ExportConf> exportConfs, OutputStream outputStream, ExcelFormat excelFormat);

    /**
     * 基本list导入，传list数据，以及要写入的outputStream。
     *
     * @param data
     * @param workbook
     * @param tableKey
     * @return
     */
    ExportResult exportToWorkbook(List data, Workbook workbook, String tableKey, Object... args);

    /**
     * 基本通用DataReader，导入过程通过DataReader获取数据。
     *
     * @param dataReader
     * @param workbook
     * @param tableKey
     * @param args
     * @return
     */
    ExportResult exportToWorkbook(DataReader dataReader, Workbook workbook, String tableKey, Object... args);

    /**
     * 基本导入重载，可以设置list或dataReader。
     * @param exportConf
     * @param workbook
     * @return
     */
    ExportResult exportToWorkbook(ExportConf exportConf, Workbook workbook);

    /**
     * 配置多个sheet顺序导出
     *
     * @param exportConfs
     * @param workbook
     * @return
     */
    ExportResult exportToWorkbook(List<ExportConf> exportConfs, Workbook workbook);


    /**
     * 设置XlsMapperResourceLoader。
     *
     * @param mapperLoader
     */
    void setMapperLoader(MapperLoader mapperLoader);

    /**
     * 添加临时tableConfig。
     *
     * @param tableKey
     * @param tableConfig
     */
    void addTableConfig(String tableKey, TableConfig tableConfig);

}
