package com.dukexx.xport.importprocessor;

import com.dukexx.xport.common.ImportConf;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.confighelper.MapperLoader;
import com.dukexx.xport.exception.IllegalFileFormatException;
import com.dukexx.xport.posthandler.ImportPostHandler;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public abstract class XlsImportEventProcessor implements HSSFListener, ImportProcessor {
    //配置加载
    protected MapperLoader xMapperLoader;
    //本地临时TableConfig
    protected Map<String, TableConfig> localTableConfigMap = new HashMap<>();


    @Override
    public ImportPostHandler processOneSheet(File file, ImportConf importModel) {
        try {
            return processOneSheet(OPCPackage.open(file), importModel);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException(e);
        }
    }

    @Override
    public ImportPostHandler processOneSheet(InputStream inputStream, ImportConf importModel) {
        try {
            return processOneSheet(OPCPackage.open(inputStream), importModel);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImportPostHandler processOneSheet(OPCPackage opcPackage, ImportConf importModel) {
        return null;
    }

    @Override
    public ImportPostHandler processSheets(File file, int start, int count, ImportConf sheetModelConf) {
        try {
            return processSheets(OPCPackage.open(file), start, count, sheetModelConf);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException(e);
        }
    }

    @Override
    public ImportPostHandler processSheets(InputStream inputStream, int start, int count, ImportConf sheetModelConf) {
        try {
            return processSheets(OPCPackage.open(inputStream), start, count, sheetModelConf);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImportPostHandler processSheets(OPCPackage opcPackage, int start, int count, ImportConf sheetModelConf) {
        return null;
    }

    @Override
    public List<ImportPostHandler> processDiffSheets(File file, Map<Integer, ImportConf> tableKeys) {
        try {
            return processDiffSheets(OPCPackage.open(file), tableKeys);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException(e);
        }
    }

    @Override
    public List<ImportPostHandler> processDiffSheets(InputStream inputStream, Map<Integer, ImportConf> tableKeys) {
        try {
            return processDiffSheets(OPCPackage.open(inputStream), tableKeys);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ImportPostHandler> processDiffSheets(OPCPackage opcPackage, Map<Integer, ImportConf> tableKeys) {
        return null;
    }

    @Override
    public void setxMapperLoader(MapperLoader xMapperLoader) {
        this.xMapperLoader = xMapperLoader;
    }

    @Override
    public void addTableConfig(String tableKey, TableConfig tableConfig) {
        if (tableKey == null || tableConfig == null)
            throw new IllegalArgumentException("argument tableKey or tableConfig cannot be null");
        tableConfig.setTableKey(tableKey);
        localTableConfigMap.put(tableKey, tableConfig);
    }

    protected abstract void setSheetModelConfs(Map<String, ImportConf> sheetModelConfs);

    protected TableConfig getTableConfig(String tableKey) {
        TableConfig tableConfig = localTableConfigMap.get(tableKey);
        if (tableConfig == null) {
            tableConfig = xMapperLoader.getTableConfig(tableKey);
        }
        return tableConfig;
    }

    protected abstract Map<Integer, ImportPostHandler> getResultPostHandlers();

    protected abstract void resetAll();
}
