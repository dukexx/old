package com.dukexx.xport.common;

import com.dukexx.xport.importprocessor.datawriter.DataWriter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Sheet模式配置，tableKey的配置优先于SheetModel配置
 * tableKey不可以为null，SheetModel默认为reset
 *
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class ImportConf implements Serializable{

    private static final long serialVersionUID = 1L;
    //tableKey
    private String tableKey;
    //SheetModel
    private SheetModel sheetModel;
    //导入结果的类型
    private Class resultType;
    //dataWriter
    private DataWriter dataWriter;
    //args
    private Object[] args;

    public ImportConf(String tableKey, SheetModel sheetModel, Class clazz, Object... args) {
        this.tableKey = tableKey;
        this.sheetModel = sheetModel;
        this.resultType = clazz;
        this.args = args;
    }

    public ImportConf(String tableKey, SheetModel sheetModel, Class clazz, DataWriter dataWriter, Object... args) {
        this.tableKey = tableKey;
        this.sheetModel = sheetModel;
        this.resultType = clazz;
        this.args = args;
        this.dataWriter=dataWriter;
    }

}
