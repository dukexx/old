package com.dukexx.xport.exportprocessor;

import com.dukexx.xport.common.*;
import com.dukexx.xport.common.utils.XlsUtils;
import com.dukexx.xport.confighelper.FieldConfig;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.exception.NotFoundTableConfigException;
import com.dukexx.xport.exportprocessor.datareader.DataReader;
import com.dukexx.xport.posthandler.ExportPostHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
public class DefaultExportProcessor extends ExportAdaptProcessor {
    private static final long serialVersionUID = 1L;

    private static final Class CLASS_STRING = String.class;
    private static final Class CLASS_BOOLEAN = Boolean.class;

    protected Workbook workbook = null;

    protected CreationHelper creationHelper = null;

    protected DataFormat df = null;

    protected ExcelFormat excelFormat;

    protected String curTableKey;

    protected TableConfig tableConfig;
    //rows in memory when SXSSF
    protected int memRows = 3000;
    //is SXSSF
    protected boolean lowMem = true;

    protected int curSheet = -1;
    //failDatas
    protected Map<String, List<FailData>> failDatas = new HashMap();
    //cache sheet name mode
    private Map<String, Integer> sheetNameMap = new HashMap<>();


    /**
     * export from dataReader
     *
     * @param exportConfs
     * @param excelFormat
     * @return
     */
    @Override
    protected ExportResult dataReaderExport(List<ExportConf> exportConfs, Workbook workbook, ExcelFormat excelFormat) {
        log.debug("enter method:dataReaderExport, arguments:exportConfs:" + exportConfs + ",excelFormat:" + excelFormat);
        if (excelFormat == null) {
            throw new IllegalArgumentException("argument excelFormat cannot be null");
        }
        this.workbook = workbook;
        this.excelFormat = excelFormat;
        for (ExportConf exportConf : exportConfs) {
            if (exportConf == null) {
                continue;
            }
            curTableKey = exportConf.getTableKey();
            String sheetName = null;
            if (curTableKey != null) {
                setTableConfig(curTableKey);
                sheetName = tableConfig.getSheetName();
            } else {
                tableConfig = null;
            }
            buildWorkbook(excelFormat);
            //create blank sheet
            if (exportConf.isEmpty()) {
                createBlankSheet(sheetName);
                continue;
            }
            if (curTableKey == null || tableConfig == null) {
                throw new NotFoundTableConfigException(curTableKey);
            }
            //write to workbook
            exportToWorkbook(exportConf, sheetName);
        }
        log.debug("finish method dataReaderExport, return:" + workbook + ", failDataSize:" + failDatas.size());
        return new ExportResult(workbook, failDatas);
    }

    /**
     * write to workbook achieve
     *
     * @param exportConf
     * @param sheetName
     */
    protected void exportToWorkbook(ExportConf exportConf, String sheetName) {
        DataReader dataReader = exportConf.getDataReader();
        //cache cellStyle
        CellStyle[] cellStyleMap = new CellStyle[20];
        int cellStylyLength = 20;
        int dataRow = tableConfig.getDataRow();
        List<Map.Entry<String, FieldConfig>> fieldConfigs = new ArrayList<>(tableConfig.getFieldConfigMap().entrySet());
        int length = fieldConfigs.size();
        int curRow = dataRow;
        Integer sheetRows = tableConfig.getSheetRows();
        SheetModel sheetModel = tableConfig.getSheetModel();
        ExportPostHandler exportPostHandler = tableConfig.getExportPostHandler();
        if (sheetRows == null) {
            sheetRows = excelFormat.equals(ExcelFormat.XLS) ? MAXROWS_XLS : MAXROWS_XLSX;
        }
        //start export
        s:
        while (true) {
            Sheet sheet = createSheet(workbook, sheetName);
            setSheetStyles(sheet, tableConfig, fieldConfigs);
            curSheet++;
            log.debug("export to sheet sheetIndex:" + curSheet + ", sheetName:" + sheet.getSheetName());
            Map.Entry<String, FieldConfig> entry = null;
            Row row = null;
            Cell cell = null;
            while (dataReader.next()) {
                //process row
                row = sheet.createRow(curRow);
                try {
                    for (int i = 0; i < length; i++) {
                        entry = fieldConfigs.get(i);
                        String field = entry.getKey();
                        Object value = dataReader.getCurValue(field);
                        FieldConfig fieldConfig = entry.getValue();
                        Integer index = fieldConfig.getIndex();
                        if (index == null) {
                            continue;
                        }
                        cell = row.createCell(index);
                        //set CellStyle
                        if (fieldConfig.getFormat() != null) {
                            CellStyle cellStyle = null;
                            if (index < cellStylyLength) {
                                cellStyle = cellStyleMap[index];
                            }
                            if (cellStyle == null) {
                                cellStyle = workbook.createCellStyle();
                                cellStyle.setDataFormat(df.getFormat(fieldConfig.getFormat()));
                                cellStyleMap = XlsUtils.setArrayValue(cellStyleMap, index, cellStyle);
                            }
                            cell.setCellStyle(cellStyle);
                        }
                        //set value
                        if (value != null) {
                            setCellData(cell, creationHelper, value, fieldConfig.getType());
                        }
                    }
                } catch (Exception e) {
                    log.debug("exception on processing row:" + curRow, e);
                    setFailDatas(dataReader, curSheet, curRow);
                }
                if (++curRow > sheetRows) {
                    //next sheet
                    if (exportPostHandler != null) {
                        exportPostHandler.postHand(new ExportInfo(curSheet, workbook, tableConfig), exportConf.getArgs());
                    }
                    if (dataReader.getCurRow() + 1 >= dataReader.getSize()) {
                        break s;
                    }
                    if (sheetModel.equals(SheetModel.RESET)) {
                        curRow = dataRow;
                    } else {
                        //from row 0
                        curRow = 0;
                    }
                    //complete sheet
                    continue s;
                }
            }
            //data end
            if (exportPostHandler != null) {
                exportPostHandler.postHand(new ExportInfo(curSheet, workbook, tableConfig), exportConf.getArgs());
            }
            break s;
        }
    }

    @Override
    protected void resetAll() {
        curTableKey = null;
        tableConfig = null;
        memRows = 3000;
        lowMem = true;
        failDatas = new HashMap<>();
    }

    /**
     * if cannot find TableConfig of tableKey, throw exception
     *
     * @param tableKey
     * @return
     */
    protected TableConfig setTableConfig(String tableKey) {
        tableConfig = getTableConfig(tableKey);
        if (tableConfig == null || tableKey == null) {
            throw new NotFoundTableConfigException(tableKey);
        }
        setTableConfig(tableConfig);
        return tableConfig;
    }

    protected void setCellData(Cell cell, CreationHelper creationHelper, Object value, int type) {
        //judgment type
        if (type == CellData.FORMULA) {
            cell.setCellFormula(value.toString());
        } else {
            Class clazz = value.getClass();
            if (clazz == CLASS_STRING) {
                cell.setCellValue(creationHelper.createRichTextString((String) value));
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (clazz == CLASS_BOOLEAN) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof Calendar) {
                cell.setCellValue((Calendar) value);
            } else {
                cell.setCellValue(creationHelper.createRichTextString(value.toString()));
            }
        }
    }

    protected void setFailDatas(DataReader dataReader, int curSheet, int curRow) {
        List<FailData> failDatas1 = failDatas.get(curTableKey);
        if (failDatas1 == null) {
            failDatas1 = new LinkedList<>();
            failDatas.put(curTableKey, failDatas1);
        }
        failDatas1.add(new FailData(dataReader.getCurRowData(), curTableKey, curSheet, curRow));
    }

//    protected Sheet createSheet(Workbook workbook, String sheetName, int index) {
//        Sheet sheet=null;
//        sheet.setDefaultRowHeight();
//        workbook.ge
//    }

    /**
     * 创建sheet，解析sheetName模式，管理sheetName
     *
     * @param workbook
     * @param sheetName
     * @return
     */
    protected Sheet createSheet(Workbook workbook, String sheetName) {
        Sheet sheet = null;
        if (sheetName == null) {
            sheet = workbook.createSheet();
        } else {
            String realSheetName;
            Integer i = sheetNameMap.get(sheetName);
            if (i == null) {
                i = 0;
            }
            //处理占位符
            i++;
            if (sheetName.indexOf('?') == -1) {
                if (i == 1) {
                    realSheetName = sheetName;
                } else {
                    realSheetName = sheetName + i;
                }
            } else {
                realSheetName = sheetName.replaceAll("\\?", i.toString());
            }
            sheetNameMap.put(sheetName, i);
            sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(realSheetName));
        }
        return sheet;
    }

    /**
     * create blank sheet
     *
     * @param sheetName
     */
    protected void createBlankSheet(String sheetName) {
        curSheet++;
        Sheet sheet = createSheet(workbook, sheetName);
        if (tableConfig != null) {
            setSheetStyles(sheet, tableConfig, null);
            ExportPostHandler exportPostHandler = tableConfig.getExportPostHandler();
            if (exportPostHandler != null) {
                exportPostHandler.postHand(new ExportInfo(curSheet, workbook, tableConfig));
            }
        }
    }

    /**
     * set sheet style
     *
     * @param sheet
     * @param tableConfig
     */
    protected void setSheetStyles(Sheet sheet, TableConfig tableConfig, List<Map.Entry<String, FieldConfig>> fieldConfs) {
        Collection<Map.Entry<String, FieldConfig>> collection = fieldConfs;
        if (fieldConfs == null) {
            collection = tableConfig.getFieldConfigMap().entrySet();
        }
        if (tableConfig != null) {
            Integer rowHeight = tableConfig.getRowHeight();
            if (rowHeight != null) {
                sheet.setDefaultRowHeight(rowHeight.shortValue());
            }
        }
        for (Map.Entry<String, FieldConfig> entry : collection) {
            FieldConfig fieldConfig = entry.getValue();
            Integer width = fieldConfig.getWidth();
            if (width != null) {
                sheet.setColumnWidth(fieldConfig.getIndex(), width);
            }else{
                sheet.autoSizeColumn(fieldConfig.getIndex());
            }
        }
    }

    /**
     * 根据格式选择Workbook实例
     *
     * @param excelFormat
     * @return
     */
    protected Workbook buildWorkbook(ExcelFormat excelFormat) {
        if (workbook != null) {
            return workbook;
        }
        if (excelFormat.equals(ExcelFormat.XLS)) {
            workbook = new HSSFWorkbook();
        } else {
            if (lowMem) {
                workbook = new SXSSFWorkbook(memRows);
            } else {
                workbook = new XSSFWorkbook();
            }
        }
        creationHelper = workbook.getCreationHelper();
        df = workbook.createDataFormat();
        return workbook;
    }

    private void setTableConfig(TableConfig tableConfig) {
        memRows = tableConfig.getMemCount();
        lowMem = tableConfig.isLowMem();
    }

}
