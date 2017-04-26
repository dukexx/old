package com.dukexx.xport.confighelper;

import com.dukexx.xport.common.utils.XlsUtils;
import com.dukexx.xport.exception.IllegalTableConfigException;
import com.dukexx.xport.common.CellData;
import lombok.Data;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
public class FieldConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;

    private Integer index;

    private String name;

    private String format;

    private int type;

    private String column;

    private Integer width;

    private Integer oldIndex;

    public FieldConfig(String field, Integer index, String name, String format, String type, String column, Integer width) {
        this.field = field;
        setIndex(index);
        this.name = name;
        this.format = format;
        this.width = width;
        setType(type);
        //将column转化为index
        coverIndex(column);

    }

    public void setColumn(String column) {
        coverIndex(column);
    }

    public void setIndex(Integer index) {
        this.index = index;
        oldIndex = index;
    }

    public void setType(String type) {
        if (type == null) {
            return;
        }
        String ttype = type;
        type = type.toLowerCase();
        switch (type) {
            case "formula":
                this.type = CellData.FORMULA;
                break;
            case "date":
                this.type = CellData.DATE;
                break;
            case "int":
                this.type = CellData.INT;
                break;
            case "double":
                this.type = CellData.DOUBEL;
                break;
            case "string":
                this.type = CellData.STRING;
                break;
            default:
                throw new IllegalTableConfigException("illegal table config of type:" + ttype);
        }
    }

    private void coverIndex(String column) {
        this.column = column;
        if (column != null) {
            index = XlsUtils.convertColumnToIndex(column);
        } else {
            index = oldIndex;
        }
    }
}
