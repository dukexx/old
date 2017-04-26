package com.dukexx.xport.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CellData implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int INLINESTR = 1;
    public static final int SSTINDEX = 2;
    public static final int NUMBER = 3;
    public static final int DATE = 4;
    public static final int FORMULA = 5;
    public static final int BOOL = 6;
    public static final int ERROR = 7;
    public static final int NULL = 8;
    public static final int NUMICA = 9;
    public static final int INT=10;
    public static final int DOUBEL=11;
    public static final int STRING=12;

    private Object value;
    private int type;
    private String format;
}
