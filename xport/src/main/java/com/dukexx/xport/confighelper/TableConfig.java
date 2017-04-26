package com.dukexx.xport.confighelper;

import com.dukexx.xport.common.ProcessKind;
import com.dukexx.xport.common.SheetModel;
import com.dukexx.xport.common.utils.XlsUtils;
import com.dukexx.xport.exception.IllegalTableConfigException;
import com.dukexx.xport.headresolver.HeadResolver;
import com.dukexx.xport.posthandler.ExportPostHandler;
import com.dukexx.xport.posthandler.ImportPostHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
public class TableConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    //only import, 索引-字段表，解析过程中主要通过该map映射excel与字段的关系
    private List<String>[] indexFeildMap = new List[20];
    //only import, 列名(name of FieldConfig)-字段表
    private Map<String, List<String>> nameFieldMap = new HashMap<String, List<String>>();
    //字段-设置(index,format,name)表
    private Map<String, FieldConfig> fieldConfigMap = new HashMap<String, FieldConfig>();

    //processKind cannot be null
    private ProcessKind processKind;

    private String tableKey;
    //数据库表名
    private String table;
    //only export, sheet名称
    private String sheetName;
    //sheetmodel
    private SheetModel sheetModel;
    //only export, 导出xlsx时是否使用sxssf模式，默认为true
    private boolean lowMem = true;
    //表头所在行
    private Integer headRow;
    //数据首行
    private Integer dataRow;
    //表头行最大行数
    private Integer maxHeadRow = 15;
    //min match name of field
    private Integer guaFields;
    //only export, 作用于poi SXSSFWorkbook的设置，默认3000
    private Integer memCount = 3000;
    //only export, 指定一个sheet最大行数
    private Integer sheetRows;
    //only export, height of row
    private Integer rowHeight;
    //起始列数，默认为0
    private Integer firstColm = 0;
    //only export, 默认excel文件名，如果前端不指定，那应用该配置
    private String filename;
    //only import, 用于对获取的单行数据进行后置处理
    private Class importPostHandler;
    //only export, 对数据写入完成后的Workbook进行后置处理
    private Class exportPostHandler;
    //表头行判断
    private Class headResolver;

    public TableConfig(ProcessKind processKind) {
        setProcessKind(processKind);
    }

    public void setProcessKind(ProcessKind processKind) {
        if (processKind == null) {
            throw new IllegalArgumentException("processKind cannot be null as new TableConfig");
        }
        this.processKind = processKind;
    }

    public void setLowMem(Boolean lowMem) {
        this.lowMem = lowMem == null ? true : lowMem;
    }

    /**
     * sheetModel, default reset
     *
     * @param sheetModel
     */
    public void setSheetModel(SheetModel sheetModel) {
        this.sheetModel = sheetModel == null ? SheetModel.RESET : sheetModel;
    }

    public void setMaxHeadRow(Integer maxHeadRow) {
        this.maxHeadRow = maxHeadRow == null ? 15 : maxHeadRow;
    }

    public Integer getGuaFields() {
        if (guaFields == null)
            return nameFieldMap.size();
        else
            return guaFields;
    }

    public void setMemCount(Integer memCount) {
        this.memCount = memCount == null ? 3000 : memCount;
    }

    public void setFirstColm(Integer firstColm) {
        this.firstColm = firstColm == null ? 0 : firstColm;
    }

    public void setImportPostHandler(String importPostHandler) {
        if (importPostHandler != null) {
            try {
                this.importPostHandler = Class.forName(importPostHandler);
            } catch (ClassNotFoundException e) {
                throw new IllegalTableConfigException("illegal tableConfig of headResolver:" + headResolver, e);
            }
        }
    }

    public void setHeadRow(Integer headRow) {
        switch (this.processKind) {
            case EXPORT:
                this.headRow=headRow == null ? 0 : headRow;
                break;
            case IMPORT:
                this.headRow=headRow;
                break;
            default:
                throw new IllegalTableConfigException("illegal tableConfig, processKind cannot be null");
        }
    }

    public void setDataRow(Integer dataRow) {
        switch (this.processKind) {
            case EXPORT:
                this.dataRow=dataRow == null ? 0 : dataRow;
                break;
            case IMPORT:
                this.dataRow=dataRow;
                break;
            default:
                throw new IllegalTableConfigException("illegal tableConfig, processKind cannot be null");
        }
    }

    public ImportPostHandler getImportPostHandler() {
        if (importPostHandler == null) {
            return null;
        }
        try {
            return (ImportPostHandler) importPostHandler.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ExportPostHandler getExportPostHandler() {
        if (exportPostHandler == null) {
            return null;
        }
        try {
            return (ExportPostHandler) exportPostHandler.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setExportPostHandler(String exportPostHandler) {
        if (exportPostHandler != null) {
            try {
                this.exportPostHandler = Class.forName(exportPostHandler);
            } catch (ClassNotFoundException e) {
                throw new IllegalTableConfigException("illegal tableConfig of headResolver:" + headResolver, e);
            }
        }
    }

    public HeadResolver getHeadResolver() {
        if (headResolver == null) {
            return null;
        }
        try {
            return (HeadResolver) headResolver.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHeadResolver(String headResolver) {
        if (headResolver != null) {
            try {
                this.headResolver = Class.forName(headResolver);
            } catch (ClassNotFoundException e) {
                throw new IllegalTableConfigException("illegal tableConfig of headResolver:" + headResolver, e);
            }
        }
    }

    /**
     * get indexFieldMap copy
     * @return
     */
    public List<String>[] getIndexFieldMapCopy() {
        int len = indexFeildMap.length;
        List[] newArr = new List[len];
        for (int i = 0; i < len - 1; i++) {
            List fields = indexFeildMap[i];
            if (fields != null) {
                List list = new ArrayList(fields.size());
                newArr[i] = copyList(list, fields);
            }
        }
        return newArr;
    }

    /**
     * get columnFieldMap copy by indexFieldMap
     * @return
     */
    public Map<String, List<String>> getColumnFieldMapCopy() {
        Map<String, List<String>> columnFieldMap = new HashMap<>();
        int len = indexFeildMap.length;
        for(int i=0;i<len-1;i++) {
            List fields = indexFeildMap[i];
            if (fields != null) {
                List list = new ArrayList(fields.size());
                columnFieldMap.put(XlsUtils.convertIndexToColumn(i), copyList(list, fields));
            }
        }
        return columnFieldMap;
    }

    public void setIndexFeildMapValue(int index, String field) {
        if (index > indexFeildMap.length)
            indexFeildMap = (List[]) growArray(new List[index + 20], indexFeildMap);
        indexFeildMap[index] = new ArrayList<>();
        indexFeildMap[index].add(field);
    }

    public void setNameFieldMapValue(String name, String field) {
        if (name == null)
            return;
        List<String> fields = nameFieldMap.get(name);
        if (fields == null) {
            List<String> list = new ArrayList<String>();
            list.add(field);
            nameFieldMap.put(name, list);
        } else
            fields.add(field);
    }


    /**
     * 对TableConfig设置值的主要入口, 建议使用该方法设置FieldConfig
     *
     * @param fieldConfig
     */
    public void addFieldConfig(FieldConfig fieldConfig) {
        String field = fieldConfig.getField();
        this.fieldConfigMap.put(field, fieldConfig);
        switch (this.processKind) {
            case IMPORT:
                Integer index = fieldConfig.getIndex();
                if (index != null) {
                    setIndexFeildMapValue(index, field);
                }
                String name = fieldConfig.getName();
                if (name != null) {
                    setNameFieldMapValue(name, field);
                }
                break;
            case EXPORT:
                break;
            default:
                throw new IllegalTableConfigException("illegal tableConfig, processKind cannot be null");
        }
    }

    /**
     * build TableConfig
     */
    public void build() {
        //export config build
        if (!ProcessKind.IMPORT.equals(this.processKind)) {
            return;
        }
        if (dataRow == null) {
            if (exportPostHandler != null) {
                dataRow = getExportPostHandler().getDataRow();
            }
        }

        FieldConfig[] fieldConfigs = new FieldConfig[20];
        for (Map.Entry<String, FieldConfig> entry : fieldConfigMap.entrySet()) {
            FieldConfig fieldConfig = entry.getValue();
            Integer index = fieldConfig.getIndex();
            if (index != null) {
                if (index > fieldConfigs.length)
                    fieldConfigs = (FieldConfig[]) growArray(new FieldConfig[index + 20], fieldConfigs);
                if (fieldConfigs[index] == null) {
                    fieldConfigs[index] = fieldConfig;
                } else {
                    //fieldConfig which column is not null have priority
                    if (fieldConfigs[index].getColumn() != null) {
                        fieldConfig.setColumn(null);
                        fieldConfig.setIndex(null);
                    } else if (fieldConfig.getColumn() != null) {
                        fieldConfigs[index].setIndex(null);
                        fieldConfigs[index] = fieldConfig;
                    } else {
                        fieldConfig.setIndex(null);
                    }
                }
            }
        }
        //auto set column and index for fieldConfig those column and index is null
        int i = firstColm;
        for (FieldConfig fieldConfig : fieldConfigMap.values()) {
            //set nameFieldMap
            String name = fieldConfig.getName();
            if (name != null) {
                setNameFieldMapValue(name,fieldConfig.getField());
            }
            if (fieldConfig.getIndex() == null) {
                for (; fieldConfigs[i] != null; i++) {
                    setIndexFeildMapValue(i,fieldConfigs[i].getField());
                }
                if (i > fieldConfigs.length) {
                    fieldConfigs = (FieldConfig[]) growArray(new FieldConfig[i + 20], fieldConfigs);
                }
                fieldConfig.setIndex(i);
                fieldConfig.setColumn(XlsUtils.convertIndexToColumn(i));
                fieldConfigs[i] = fieldConfig;
                setIndexFeildMapValue(i,fieldConfig.getField());
                i++;
            }
        }
    }

    private Object[] growArray(Object[] tarArray, Object[] array) {
        for (int i = 0; i < array.length; i++) {
            tarArray[i] = array[i];
        }
        return tarArray;
    }

    private List copyList(List target, List src) {
        int len = src.size();
        for (int i = 0; i < len; i++) {
            target.add(src.get(i));
        }
        return target;
    }

}
