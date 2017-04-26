package com.dukexx.xport.importprocessor;

import com.dukexx.xport.common.CellFormat;
import com.dukexx.xport.common.utils.XlsUtils;
import com.dukexx.xport.headresolver.DefaultNameHeadResolver;
import com.dukexx.xport.importprocessor.datawriter.DefaultMapWriter;
import com.dukexx.xport.posthandler.DefaultMapImportPostHandler;
import com.dukexx.xport.common.CellData;
import com.dukexx.xport.common.ParseInfo;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.headresolver.HeadResolver;
import com.dukexx.xport.importprocessor.datawriter.DataWriter;
import com.dukexx.xport.importprocessor.datawriter.DefaultBeanWriter;
import com.dukexx.xport.posthandler.ImportPostHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
public class DefaultXlsxImEventProcessor<T> extends XlsxImportEventProcessor {

    private static final long serialVersionUID = 1L;
    //tableConfig
    private TableConfig tableConfig;
    //index-field
    private List<String>[] fields;
    //handler
    private HeadResolver headResolver;

    private ImportPostHandler importPostHandler;
    //表头所在行
    private Integer headRow;
    //数据首行
    private Integer dataRow;
    //表头行最大行数
    private Integer maxHeadRow;
    //args
    private Object[] args;

    private String tableKey;
    //is data start
    private boolean sData = false;
    //is head end
    private boolean headEnd = false;
    //保存当前表头行数据
    private Map headRowData = new HashMap();
    //保存当前行数据
    private DataWriter dataWriter;
    //保存当前行format
    private Map<String, CellFormat> rowFormat = new HashMap<>();
    //当前行
    private int curRow;
    //current coordinate
    private String curCoord;
    //当前列内容
    private StringBuilder sbContent = new StringBuilder();
    //当前列类型
    private int cellDataType;
    //当前列format
    private String formatString;

    private int formatIndex;

    private boolean isValue = false;

    public DefaultXlsxImEventProcessor() {
    }

    //sax解析不同事件方法
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //row标签
        //单元格 c
        switch (localName) {
            case "c":
                setCellDataType(attributes);
                curCoord = attributes.getValue("r");
                break;
            case "row":
                curRow = Integer.parseInt(attributes.getValue("r")) - 1;
                if (!sData) {
                    if (headRow != null && headRow == -1) {
                        headEnd = true;
                        if (curRow + 1 >= dataRow) {
                            sData = true;
                        }
                    }
                }
                break;
            case "v":
                if (cellDataType != CellData.INLINESTR)
                    isValue = true;
                break;
            case "t":
                if (cellDataType == CellData.INLINESTR)
                    isValue = true;
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "c":
                if (sData) {
                    //列数据已经获得，进行处理
                    int index = XlsUtils.getIndexFromCoord(curCoord);
                    if (index <= fields.length) {
                        List<String> list = fields[index];
                        if (list != null) {
                            int len = list.size();
                            for (int i = 0; i < len; i++) {
                                setCellData(list.get(i));
                            }
                        }
                    }
                } else {
                    if (headRow != null && curRow < headRow)
                        return;
                    //识别表头
                    headRowData.put(curCoord, getCellData());
                }
                //清除原数据
                sbContent.delete(0, sbContent.length());
                formatString = null;
                formatIndex = 0;
                break;
            case "row":
                if (sData) {
                    //处理结果
                    if (importPostHandler != null) {
                        importPostHandler.importPostHand(dataWriter.popData(), rowFormat, new ParseInfo(tableConfig,
                                curRow, curSheetIndex, tableKey, dataWriter.getType()), args);
                    } else {
                        dataWriter.popData();
                    }
                    //最后处理
//                    System.out.println(rowData);
                    rowFormat = new HashMap<>();
                } else {
                    if (headRow != null && curRow < headRow) {
                        return;
                    }
                    //识别表头
                    if (!headEnd) {
                        headEnd = headResolver.isHeadEnd((Map<String, CellData>) headRowData, tableConfig);
                        if (headEnd)
                            fields = headResolver.getIndexFieldsMap(fields);
                    }
                    if (headEnd) {
                        if (dataRow == null || curRow + 1 >= dataRow) {
                            headRowData = null;
                            sData = true;
                            return;
                        }
                    } else if (maxHeadRow != null && curRow >= maxHeadRow) {
                        throw new RuntimeException("未能识别表头并超出限制，请检查表格或表格配置");
                    }
                    headRowData = new HashMap();
                }
                break;
            case "v":
            case "t":
                isValue = false;
                break;

        }
    }

    /**
     * 接收字符串
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isValue)
            sbContent.append(ch, start, length);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    /**
     * 导入并应用table设置
     *
     * @param tableConfig
     */
    @Override
    public void setTableConfig(String tablekey, TableConfig tableConfig, Class resultType, DataWriter dataWriter) {
        //引入配置
        this.tableKey = tablekey;
        this.tableConfig = tableConfig;
        fields = tableConfig.getIndexFieldMapCopy();
        importPostHandler = tableConfig.getImportPostHandler();
        headResolver = tableConfig.getHeadResolver();
        headRow = tableConfig.getHeadRow();
        dataRow = tableConfig.getDataRow();
        maxHeadRow = tableConfig.getMaxHeadRow();
        if (headResolver == null)
            headResolver = new DefaultNameHeadResolver();
        //补充验证
        if (headRow != null) {
            if (headRow == -1 && dataRow != null && dataRow == 0) {
                sData = true;
            }
        }
        if (importPostHandler == null) {
            importPostHandler = new DefaultMapImportPostHandler();
        }
        //结果类型
        this.dataWriter = dataWriter;
        if (this.dataWriter == null) {
            //如果没有自定义设置，使用默认方案
            try {
                if (resultType == Map.class || resultType.newInstance() instanceof Map) {
                    this.dataWriter = new DefaultMapWriter(resultType);
                } else {
                    this.dataWriter = new DefaultBeanWriter(resultType);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setArguments(Object... args) {
        this.args = args;
    }

    @Override
    public ImportPostHandler getImportPostHandler() {
        return importPostHandler;
    }

    @Override
    protected void continueSet() {
        curRow = 0;
    }

    @Override
    protected void reset() {
        curRow = 0;
        sData = false;
        headEnd = false;
    }

    @Override
    protected void resetTableConfig(boolean dArgs) {
        this.tableKey = null;
        this.tableConfig = null;
        fields = null;
        importPostHandler = null;
        headResolver = null;
        headRow = null;
        dataRow = null;
        maxHeadRow = null;
        curRow = 0;
        sData = false;
        headEnd = false;
        dataWriter.flush();
        dataWriter = null;
        if (dArgs)
            args = null;
    }

    @Override
    protected void resetAll() {
        resetTableConfig(true);
    }

    /**
     * 根据c标签属性获取类型
     *
     * @param attributes
     */
    private void setCellDataType(Attributes attributes) {
        String typeStr = attributes.getValue("t");
        String styleIndex = attributes.getValue("s");
        //switch不能处理null
        if (typeStr == null) {
            typeStr = "";
        }
        switch (typeStr) {
            case "inlineStr":
                cellDataType = CellData.INLINESTR;
                break;
            case "s":
                cellDataType = CellData.SSTINDEX;
                break;
            case "str":
                cellDataType = CellData.FORMULA;
                break;
            case "b":
                cellDataType = CellData.BOOL;
                break;
            case "e":
                cellDataType = CellData.ERROR;
                break;
            default:
                cellDataType = CellData.NUMICA;
                if (styleIndex != null) {
                    int index = Integer.parseInt(styleIndex);
                    CellStyle style = stylesTable.getStyleAt(index);
                    formatString = style.getDataFormatString();
                    formatIndex = style.getDataFormat();
                }
        }
    }

    private void setCellData(String field) {
        String data = sbContent.toString();
        switch (cellDataType) {
            case CellData.SSTINDEX:
                data = sst.getEntryAt(Integer.parseInt(data)).getT();
                break;
            case CellData.NUMICA:
                if (XlsUtils.isDateFormat(formatString)) {
                    cellDataType = CellData.DATE;
                } else {
                    cellDataType = CellData.NUMBER;
                }
                break;
        }
        rowFormat.put(field, new CellFormat(formatString, cellDataType));
        try {
            dataWriter.writeStringData(field, data, cellDataType, formatString);
        } catch (NumberFormatException e) {
            //忽略空数据的影响，例如合并单元格
        }
    }

    private CellData getCellData() {
        Object value = null;
        switch (cellDataType) {
            case CellData.INLINESTR:
                value = sbContent.toString();
                break;
            case CellData.SSTINDEX:
                value = sst.getEntryAt(Integer.parseInt(sbContent.toString())).getT();
                break;
            case CellData.FORMULA:
                value = sbContent.toString();
                break;
            case CellData.BOOL:
                value = Boolean.parseBoolean(sbContent.toString());
                break;
            case CellData.NUMICA:
                //判断是否是日期
                try {
                    if (HSSFDateUtil.isADateFormat(formatIndex, formatString)) {
                        value = HSSFDateUtil.getJavaDate(Double.parseDouble(sbContent.toString()));
                    } else {
                        value = Double.parseDouble(sbContent.toString());
                    }
                } catch (NumberFormatException e) {
                    //合并单元格可能出现空值
                }
                break;
        }
        return new CellData(value, cellDataType, formatString);
    }


}
