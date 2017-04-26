package com.dukexx.xport.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class ExportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Workbook workbook;
    private Map<String, List<FailData>> failData = new HashMap<>();

    public ExportResult(Workbook workbook, Map<String, List<FailData>> failData) {
        this.workbook = workbook;
        this.failData = failData;
    }

    public List<FailData> getFailDatas(String tableKey) {
        return failData.get(tableKey);
    }

    /**
     * shuild be call finally
     */
    public void destroy() {
        if (workbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) workbook).dispose();
        }
    }

    /**
     * call destroy before this object be release
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}
