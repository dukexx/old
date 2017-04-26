package com.dukexx.xport.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/17
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class FailData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object data;
    private String tableKey;
    private Integer sheet;
    private Integer row;
}
