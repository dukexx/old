package com.dukexx.xport.common;

import com.dukexx.xport.confighelper.TableConfig;
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
public class ParseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private TableConfig tableConfig;
    private Integer curRow;
    private Integer curSheetIndex;
    private String tableKey;
    private Class resultType;

}
