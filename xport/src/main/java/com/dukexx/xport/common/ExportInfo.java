package com.dukexx.xport.common;

import com.dukexx.xport.confighelper.TableConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportInfo implements Serializable{

    private static final long serialVersionUID = 1L;

    private int sheetIndex;

    private Workbook workbook;

    private TableConfig tableConfig;

}
