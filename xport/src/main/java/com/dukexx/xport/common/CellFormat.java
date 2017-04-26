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
public class CellFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    private String format;
    private int type;

}
