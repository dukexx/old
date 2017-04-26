package com.dukexx.xport.common;

import com.dukexx.xport.exportprocessor.datareader.DataReader;
import com.dukexx.xport.exportprocessor.datareader.DefaultListReader;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.Serializable;
import java.util.List;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class ExportConf implements Serializable{

    private static final long serialVersionUID = 1L;

    private List listData;

    private DataReader dataReader;

    private String tableKey;

    private Integer sheetIndex;

    private SheetModel sheetModel;

    private Object[] args;

    public ExportConf(DataReader dataReader, String tableKey, Object... args) {
        setDataReader(dataReader);
        this.tableKey = tableKey;
        this.args = args;
    }

    public ExportConf(List listData, String tableKey, Object... args) {
        setListData(listData);
        this.tableKey = tableKey;
    }

    public ExportConf(List listData, String tableKey, Integer sheetIndex, Workbook workbook, Object[] args) {
        setListData(listData);
        this.tableKey = tableKey;
        this.sheetIndex = sheetIndex;
        this.args = args;
    }

    public ExportConf(DataReader dataReader, String tableKey, Integer sheetIndex, Workbook workbook, Object[] args) {
        setDataReader(dataReader);
        this.tableKey = tableKey;
        this.sheetIndex = sheetIndex;
        this.args = args;
    }

    public void setDataReader(DataReader dataReader) {
        if (dataReader != null) {
            this.dataReader = dataReader;
        }
    }

    public void setListData(List listData) {
        this.listData=listData;
        if (dataReader == null && listData != null) {
            this.dataReader = DefaultListReader.getDataReaderByList(listData);
        }
    }

    public boolean isEmpty() {
        return dataReader==null||dataReader.getSize()==0;
    }

}
