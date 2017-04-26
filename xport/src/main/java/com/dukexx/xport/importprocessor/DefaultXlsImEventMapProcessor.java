package com.dukexx.xport.importprocessor;

import com.dukexx.xport.common.CellFormat;
import com.dukexx.xport.common.ImportConf;
import com.dukexx.xport.common.SheetModel;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.exception.NotFoundTableConfigException;
import com.dukexx.xport.headresolver.DefaultNameHeadResolver;
import com.dukexx.xport.headresolver.HeadResolver;
import com.dukexx.xport.posthandler.ImportPostHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.record.*;

import java.util.*;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
public class DefaultXlsImEventMapProcessor extends XlsImportEventProcessor {
    private static final long serialVersionUID = 1L;
    //保存对sheet的处理模式
    private Map<String, ImportConf> sheetModelConfMap;
    //保存所有sheet名
    private List<String> sheetNames = new ArrayList<>();

    //保存本次解析的结果ImportPostHandler
    private Map<Integer, ImportPostHandler> resultImportPostHandlers = new TreeMap<>();
    //sst
    private SSTRecord sst;
    //tableConfig
    private TableConfig tableConfig;
    //index-字段
    private List<String>[] fields;
    //列名-字段表
    private Map<String, List<String>> colmFieldMap = new HashMap<String, List<String>>();
    //handler
    private HeadResolver headResolver;
    private ImportPostHandler importPostHandler;
    //表头所在行
    private Integer headRow;
    //数据首行
    private Integer dataRow;
    //表头行最大行数
    private Integer maxHeadRow;
    //
    private Integer guaFields;
    //args
    private Object[] args;
    //标记，是否已经开始数据
    private boolean sData = false;
    private boolean headEnd = false;
    //标记，当前sheet是否解析
    private boolean shouldParsh = false;
    //保存当前行数据
    private Map rowData = new HashMap();
    //保存当前行format
    private Map<String, CellFormat> rowFormat = new HashMap<>();

    //当前sheet索引
    private int curSheetIndex = -1;
    //当前行
    private int curRow = 0;
    //当前列名
    private Integer curColumn;
    //当前tableKey
    private String curTableKey;
    //当前行第一条和最后一条
    private int first = 0;
    private int last = 0;

    @Override
    public void processRecord(Record record) {
        int sid = record.getSid();
        switch (sid) {
            case BOFRecord.sid:
                BOFRecord bofRecord = (BOFRecord) record;
                if (bofRecord.getType() == bofRecord.TYPE_WORKBOOK) {
                    log.debug("start process workbook");
                } else if (bofRecord.getType() == bofRecord.TYPE_WORKSHEET) {
                    //开始sheet
                    curSheetIndex++;
                    ImportConf smc = sheetModelConfMap.get(curSheetIndex);
                    if (smc != null) {
                        //判断之前是否有解析
                        String tableKey = smc.getTableKey();
                        if (!tableKey.equals(curTableKey)) {
                            TableConfig tableConfig = getTableConfig(tableKey);
                            if (tableConfig == null) {
                                throw new NotFoundTableConfigException(smc.getTableKey());
                            }
                            if (curTableKey != null) {
                                resultImportPostHandlers.put(curSheetIndex - 1, importPostHandler);
                            }
                            resetTableConfig(true);
                        } else {
                            if (smc.getSheetModel().equals(SheetModel.CONTINUOUS)) {
                                continueSet();
                            } else {
                                resultImportPostHandlers.put(curSheetIndex - 1, importPostHandler);
                                reset();
                            }
                        }
                        //解析
                        log.debug("start parse sheet, sheetIndex:" + curSheetIndex + ", tableKey:" + tableKey);
                        shouldParsh = true;
                    } else {
                        shouldParsh = false;
                    }
                }
                break;
            case BoundSheetRecord.sid:
                BoundSheetRecord boundSheetRecord = (BoundSheetRecord) record;
                //会获取所有sheet，顺序记录到list
                sheetNames.add(boundSheetRecord.getSheetname());
                break;
            case SSTRecord.sid:
                sst = (SSTRecord) record;
                break;
            case TableStylesRecord.sid:
                TableStylesRecord tableStylesRecord = (TableStylesRecord) record;
                FormatRecord formatRecord = null;
            default:
                if (!shouldParsh)
                    return;
                switch (sid) {
                    case RowRecord.sid:
                        RowRecord rowRecord = (RowRecord) record;
                        curRow = rowRecord.getRowNumber();
                        first = rowRecord.getFirstCol();
                        last = rowRecord.getLastCol();
                        break;
                    case NumberRecord.sid:
                        //判断日期
                        break;
                    case LabelSSTRecord.sid:

                        break;
                    case FormulaRecord.sid:

                        break;
                    case BoolErrRecord.sid:

                        break;
                    case BlankRecord.sid:
                        BlankRecord blankRecord = (BlankRecord) record;
                }
        }
    }

    @Override
    protected void setSheetModelConfs(Map<String, ImportConf> sheetModelConfs) {
        this.sheetModelConfMap = sheetModelConfs;
    }

    /**
     * 导入并应用table设置
     *
     * @param tableConfig
     */
    public void setTableConfig(TableConfig tableConfig) {
        //引入配置
        this.tableConfig = tableConfig;
        fields = tableConfig.getIndexFieldMapCopy();
        colmFieldMap = tableConfig.getNameFieldMap();
        headRow = tableConfig.getHeadRow();
        dataRow = tableConfig.getDataRow();
        maxHeadRow = tableConfig.getMaxHeadRow();
        importPostHandler = tableConfig.getImportPostHandler();
        headResolver = tableConfig.getHeadResolver();
        guaFields = tableConfig.getGuaFields();
        if (headResolver == null)
            headResolver = new DefaultNameHeadResolver();
        //补充验证
        if (headRow != null) {
            if (headRow == -1 && dataRow != null && dataRow == 0) {
                sData = true;
            }
        }
    }

    protected void continueSet() {
        curRow = 0;
    }

    protected void reset() {
        setTableConfig(this.tableConfig);
        curRow = 0;
        sData = false;
        headEnd = false;
    }

    protected void resetTableConfig(boolean dArgs) {
        this.tableConfig = null;
        fields = null;
        colmFieldMap = null;
        importPostHandler = null;
        headResolver = null;
        headRow = null;
        dataRow = null;
        maxHeadRow = null;
        guaFields = null;
        curRow = 0;
        sData = false;
        headEnd = false;
        resultImportPostHandlers = new TreeMap<>();
        sheetModelConfMap = null;
        sheetNames = new ArrayList<>();
        if (dArgs)
            args = null;
    }

    @Override
    protected void resetAll() {
        resetTableConfig(true);
    }

    /**
     * 重写父类，添加最后一次结果，并返回map
     *
     * @return
     */
    @Override
    protected Map<Integer, ImportPostHandler> getResultPostHandlers() {
        //添加最后一次结果，并返回map
        if (curTableKey != null)
            resultImportPostHandlers.put(curSheetIndex, importPostHandler);
        return resultImportPostHandlers;
    }
}
