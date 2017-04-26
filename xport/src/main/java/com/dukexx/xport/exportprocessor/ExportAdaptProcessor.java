package com.dukexx.xport.exportprocessor;

import com.dukexx.xport.common.ExcelFormat;
import com.dukexx.xport.common.ExportConf;
import com.dukexx.xport.common.ExportResult;
import com.dukexx.xport.confighelper.MapperLoader;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.exception.ExportFailException;
import com.dukexx.xport.exportprocessor.datareader.DataReader;
import com.dukexx.xport.exportprocessor.datareader.DefaultListReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public abstract class ExportAdaptProcessor implements ExportProcessor {

    //MapperLoader
    protected MapperLoader mapperLoader;
    //local TableConfigs map
    protected Map<String, TableConfig> localTableConfigs = new HashMap<>();

    protected static final int MAXROWS_XLS = 65535;
    protected static final int MAXROWS_XLSX = 1048500;


    /**
     * export to excel，适配DataReader
     *
     * @param data
     * @param outputStream
     * @param tableKey
     * @param excelFormat
     * @param args
     * @return
     */
    @Override
    public ExportResult exportToXl(List data, OutputStream outputStream, String tableKey,
                                   ExcelFormat excelFormat, Object... args) {
        DataReader dataReader = DefaultListReader.getDataReaderByList(data);
        return exportToXl(dataReader, outputStream, tableKey, excelFormat, args);
    }

    /**
     * 指定dataReader的exportToX
     * 注意这里不会对参数进行验证，兼容自定义子类实现类，子类自行验证
     *
     * @param dataReader
     * @param outputStream
     * @param tableKey
     * @param excelFormat
     * @param args
     * @return
     */
    @Override
    public ExportResult exportToXl(DataReader dataReader, OutputStream outputStream, String tableKey, ExcelFormat excelFormat, Object... args) {
        return exportToXl(new ExportConf(dataReader, tableKey, args), outputStream, excelFormat);
    }

    /**
     * export to excel by ExportConfig
     *
     * @param exportConf
     * @param outputStream
     * @param excelFormat
     * @return
     */
    @Override
    public ExportResult exportToXl(ExportConf exportConf, OutputStream outputStream, ExcelFormat excelFormat) {
        List<ExportConf> exportConfs = new ArrayList<>();
        if (exportConf != null) {
            exportConfs.add(exportConf);
        }
        return exportToXl(exportConfs, outputStream, excelFormat);
    }

    /**
     * export to excel by ExportConfig list
     *
     * @param exportConfs
     * @param outputStream
     * @param excelFormat
     * @return
     */
    @Override
    public ExportResult exportToXl(List<ExportConf> exportConfs, OutputStream outputStream, ExcelFormat excelFormat) {
        if (exportConfs == null) {
            exportConfs = new ArrayList<>();
        }
        ExportResult result = dataReaderExport(exportConfs, null,excelFormat);
        //resetAll after finish
        resetAll();
        Workbook workbook = result.getWorkbook();
        if (workbook == null) {
            throw new ExportFailException("write to workbook error");
        }
        try {
            writeOutputStream(outputStream, workbook);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public ExportResult exportToWorkbook(List data, Workbook workbook, String tableKey, Object... args) {
        DataReader dataReader = DefaultListReader.getDataReaderByList(data);
        return exportToWorkbook(dataReader, workbook, tableKey,args);
    }

    @Override
    public ExportResult exportToWorkbook(DataReader dataReader, Workbook workbook, String tableKey, Object... args) {
        return exportToWorkbook(new ExportConf(dataReader, tableKey, args), workbook);
    }

    @Override
    public ExportResult exportToWorkbook(ExportConf exportConf, Workbook workbook) {
        List<ExportConf> exportConfs = new ArrayList<>();
        if (exportConf != null) {
            exportConfs.add(exportConf);
        }
        return exportToWorkbook(exportConfs, workbook);
    }

    @Override
    public ExportResult exportToWorkbook(List<ExportConf> exportConfs, Workbook workbook) {
        if (exportConfs == null) {
            exportConfs = new ArrayList<>();
        }
        ExportResult result = dataReaderExport(exportConfs, workbook,resolveXlFmt(workbook));
        resetAll();
        return result;
    }

    @Override
    public void setMapperLoader(MapperLoader mapperLoader) {
        this.mapperLoader = mapperLoader;
    }

    @Override
    public void addTableConfig(String tableKey, TableConfig tableConfig) {
        if (tableKey == null || tableConfig == null) {
            throw new IllegalArgumentException("arguments tableKey or tableConfig cannot be null");
        }
        localTableConfigs.put(tableKey, tableConfig);
    }

    /**
     * 根据tableKey获取tableConfig
     *
     * @param tableKey
     * @return
     */
    protected TableConfig getTableConfig(String tableKey) {
        TableConfig tableConfig = localTableConfigs.get(tableKey);
        if (tableConfig == null) {
            tableConfig = mapperLoader.getTableConfig(tableKey);
        }
        return tableConfig;
    }

    /**
     * achieve by subclass, process export
     *
     * @param sheetExportConfs
     * @param excelFormat
     * @return
     */
    protected abstract ExportResult dataReaderExport(List<ExportConf> sheetExportConfs, Workbook workbook,ExcelFormat excelFormat);

    /**
     * 由子类重写，当一次完整的处理结束后调用，用于初始化processor状态，使该对象可复用
     */
    protected abstract void resetAll();

    private ExcelFormat resolveXlFmt(Workbook workbook) {
        ExcelFormat excelFormat=null;
        if (workbook instanceof HSSFWorkbook) {
            excelFormat=ExcelFormat.XLS;
        } else if (workbook instanceof SXSSFWorkbook || workbook instanceof XSSFWorkbook) {
            excelFormat=ExcelFormat.XLSX;
        }
        return excelFormat;
    }

    private void writeOutputStream(OutputStream outputStream, Workbook workbook) throws IOException {
        workbook.write(outputStream);
        outputStream.close();
    }
}
