package com.dukexx.xport.importprocessor;

import com.dukexx.xport.common.ImportConf;
import com.dukexx.xport.common.SheetModel;
import com.dukexx.xport.confighelper.MapperLoader;
import com.dukexx.xport.confighelper.TableConfig;
import com.dukexx.xport.exception.IllegalFileFormatException;
import com.dukexx.xport.exception.NotFoundTableConfigException;
import com.dukexx.xport.importprocessor.datawriter.DataWriter;
import com.dukexx.xport.posthandler.ImportPostHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
public abstract class XlsxImportEventProcessor extends DefaultHandler implements ImportProcessor {
    //xlsx中所有字符串内容引用
    protected SharedStringsTable sst;
    //xlsx中所有Style的引用
    protected StylesTable stylesTable;
    //配置加载
    private MapperLoader xMapperLoader;
    //存储importPostHandler
    private List<ImportPostHandler> postHandlers = new ArrayList<>();
    ;
    //本地临时TableConfig
    private Map<String, TableConfig> localTableConfigMap = new HashMap<>();
    //当前sheet索引
    protected Integer curSheetIndex;

    /**
     * 处理第一个sheet
     *
     * @param file
     * @return
     */
    @Override
    public ImportPostHandler processOneSheet(File file, ImportConf importModel) {
        try {
            return processOneSheet(OPCPackage.open(file), importModel);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException("file format error", e);
        }
    }

    @Override
    public ImportPostHandler processOneSheet(InputStream inputStream, ImportConf importModel) {
        try {
            return processOneSheet(OPCPackage.open(inputStream), importModel);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException("file format error", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImportPostHandler processOneSheet(OPCPackage opcPackage, ImportConf importModel) {
        log.debug("start method processOneSheet, importModel:" + importModel);
        if (importModel == null) {
            throw new IllegalArgumentException("argument importModel cannot be null");
        }
        String tableKey = importModel.getTableKey();
        if (tableKey == null) {
            throw new IllegalArgumentException("tableKey of argument importModel cannot be null");
        }
        Object[] args = importModel.getArgs();
        Class resultType = importModel.getResultType();
        DataWriter dataWriter = importModel.getDataWriter();
        try {
            TableConfig tableConfig = getTableConfig(tableKey);
            if (tableConfig == null) {
                throw new NotFoundTableConfigException(tableKey);
            }
            setTableConfig(tableKey, tableConfig, resultType, dataWriter);
            setArguments(args);
            XSSFReader xssfReader = getXSSFReader(opcPackage);
            XMLReader xmlReader = getXMLReader();
            InputStream sheet = xssfReader.getSheetsData().next();
            curSheetIndex = 0;
            log.debug("start parse sheet, sheetIndex:0, tableKey:" + tableKey);
            xmlReader.parse(new InputSource(sheet));
            log.debug("finish parse sheet, sheetIndex:0, tableKey:" + tableKey);
            ImportPostHandler returnPostHandler = getImportPostHandler();
            //释放资源
            releaseAll(sheet);
            log.debug("finish method processOneSheet, return:" + returnPostHandler);
            return returnPostHandler;
        } catch (IOException | OpenXML4JException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理多条sheet，指定sheet数量，并指定是否连续(即是否每个sheet都重新解析表头)
     * 如果是RESET，返回值中包含多个ImportPostHandler对应每个handler，如果是CONTINUOUS，只包含一个ImportPostHandler
     *
     * @param file
     * @param start       起始sheet索引，从0开始
     * @param count       数量，如果为-1，表示处理全部
     * @param importModel
     * @return
     */
    @Override
    public ImportPostHandler processSheets(File file, int start, int count, ImportConf importModel) {
        try {
            return processSheets(OPCPackage.open(file), start, count, importModel);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException("file format error", e);
        }
    }

    @Override
    public ImportPostHandler processSheets(InputStream inputStream, int start, int count, ImportConf importModel) {
        try {
            return processSheets(OPCPackage.open(inputStream), start, count, importModel);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException("file format error", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImportPostHandler processSheets(OPCPackage opcPackage, int start, int count, ImportConf importModel) {
        log.debug("start method processSheets, importModel:" + importModel);
        String tableKey = importModel.getTableKey();
        SheetModel sheetModel = importModel.getSheetModel();
        Class clazz = importModel.getResultType();
        DataWriter dataWriter = importModel.getDataWriter();
        Object[] args = importModel.getArgs();
        if (start < 0 || count < -1 || tableKey == null) {
            throw new IllegalArgumentException("args of processSheets is illegal");
        }
        try {
            XSSFReader xssfReader = getXSSFReader(opcPackage);
            XMLReader xmlReader = getXMLReader();
            Iterator<InputStream> iss = xssfReader.getSheetsData();
            TableConfig tableConfig = getTableConfig(tableKey);
            ImportPostHandler importPostHandler = null;
            if (tableConfig == null) {
                throw new NotFoundTableConfigException(tableKey);
            }
            if (sheetModel == null) {
                sheetModel = tableConfig.getSheetModel();
            }
            setArguments(args);
            setTableConfig(tableKey, tableConfig, clazz, dataWriter);
            if (count == 0) {
                importPostHandler = getImportPostHandler();
                resetAll();
                log.debug("finish method processSheets, return:" + importPostHandler);
                return importPostHandler;
            }
            for (int i = 0; iss.hasNext(); i++) {
                InputStream sheet = iss.next();
                if (i >= start) {
                    //处理
                    curSheetIndex = i;
                    log.debug("start parse sheet, sheetIndex:" + i + ", tableKey:" + tableKey);
                    xmlReader.parse(new InputSource(sheet));
                    log.debug("finish parse sheet, sheetIndex:" + i + ", tableKey:" + tableKey);
                    sheet.close();
                    if (sheetModel.equals(SheetModel.RESET)) {
                        reset();
                    } else {
                        continueSet();
                    }
                    if (count != -1 && i >= start + count - 1)
                        break;
                }
            }
            importPostHandler = getImportPostHandler();
            releaseAll(null);
            log.debug("finish method processSheets, return:" + importPostHandler);
            return importPostHandler;
        } catch (IOException | OpenXML4JException | SAXException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据sheet索引-tableKey表处理sheet，没有分配tableKey的索引使用上一次tableKey配置
     *
     * @param file
     * @param sheetModelConfs
     * @return
     */
    @Override
    public List<ImportPostHandler> processDiffSheets(File file, Map<Integer, ImportConf> sheetModelConfs) {
        try {
            return processDiffSheets(OPCPackage.open(file), sheetModelConfs);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException("file format error", e);
        }
    }

    @Override
    public List<ImportPostHandler> processDiffSheets(InputStream inputStream, Map<Integer, ImportConf> sheetModelConfs) {
        try {
            return processDiffSheets(OPCPackage.open(inputStream), sheetModelConfs);
        } catch (InvalidFormatException e) {
            throw new IllegalFileFormatException("file format error", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ImportPostHandler> processDiffSheets(OPCPackage opcPackage, Map<Integer, ImportConf> sheetModelConfs) {
        log.debug("start method processDiffSheets, sheetModelConfs:" + sheetModelConfs);
        if (sheetModelConfs == null || sheetModelConfs.isEmpty()) {
            throw new IllegalArgumentException("sheetModelConfs cannot be null or empty");
        }
        try {
            List<ImportPostHandler> importPostHandlers = new LinkedList<>();
            XSSFReader xssfReader = getXSSFReader(opcPackage);
            XMLReader xmlReader = getXMLReader();
            Iterator<InputStream> iss = xssfReader.getSheetsData();
            //标记前面是否有解析行为
            boolean flag = false;
            String curTableKey = null;
            for (int i = 0; iss.hasNext(); i++) {
                InputStream sheet = iss.next();
                ImportConf sheetModelConf = sheetModelConfs.get(i);
                if (sheetModelConf != null) {
                    String tableKey = sheetModelConf.getTableKey();
                    TableConfig tableConfig = getTableConfig(tableKey);
                    Class clazz = sheetModelConf.getResultType();
                    DataWriter dataWriter = sheetModelConf.getDataWriter();
                    if (tableKey == null || tableConfig == null) {
                        throw new NotFoundTableConfigException(tableKey);
                    }
                    if (tableKey.equals(curTableKey)) {
                        //和上一次同一个tablekey
                        SheetModel sheetModel = sheetModelConf.getSheetModel();
                        if (sheetModel == null) {
                            sheetModel = tableConfig.getSheetModel();
                        }
                        if (sheetModel.equals(SheetModel.RESET)) {
                            reset();
                        } else {
                            continueSet();
                        }
                    } else {
                        //不同tableKey，都视为reset
                        if (curTableKey != null) {
                            importPostHandlers.add(getImportPostHandler());
                            resetTableConfig(true);
                        }
                        setTableConfig(tableKey, tableConfig, clazz, dataWriter);
                    }
                    setArguments(sheetModelConf.getArgs());
                    //设置标记
                    curTableKey = tableKey;
                    curSheetIndex = i;
                    log.debug("start parse sheet, sheetIndex:" + i + ", tableKey:" + tableKey);
                    xmlReader.parse(new InputSource(sheet));
                    log.debug("finish parse sheet, sheetIndex:" + i + ", tableKey:" + tableKey);
                    sheet.close();
                }
            }
            if (curTableKey != null) {
                importPostHandlers.add(getImportPostHandler());
            }
            releaseAll(null);
            log.debug("finish method processDiffSheets, return:" + importPostHandlers);
            return importPostHandlers;
        } catch (IOException | OpenXML4JException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addTableConfig(String tableKey, TableConfig tableConfig) {
        if (tableKey == null || tableConfig == null)
            throw new IllegalArgumentException("argument tableKey or tableConfig cannot be null");
        tableConfig.setTableKey(tableKey);
        localTableConfigMap.put(tableKey, tableConfig);
    }

    @Override
    public void setxMapperLoader(MapperLoader xMapperLoader) {
        this.xMapperLoader = xMapperLoader;
    }

    /**
     * 导入并应用table设置
     *
     * @param tableKey
     * @param tableConfig
     */
    public abstract void setTableConfig(String tableKey, TableConfig tableConfig, Class resultType, DataWriter dataWriter);

    /**
     * 传递其它参数
     *
     * @param args
     */
    protected abstract void setArguments(Object... args);

    /**
     * 获取后置处理器
     *
     * @return
     */
    public abstract ImportPostHandler getImportPostHandler();

    /**
     * 获取XSSFReader
     *
     * @param opcPackage
     * @return
     * @throws IOException
     * @throws OpenXML4JException
     */
    private XSSFReader getXSSFReader(OPCPackage opcPackage) throws IOException, OpenXML4JException {
        XSSFReader reader = new XSSFReader(opcPackage);
        sst = reader.getSharedStringsTable();
        stylesTable = reader.getStylesTable();
        return reader;
    }

    /**
     * 获取XMLReader
     *
     * @return
     * @throws SAXException
     */
    private XMLReader getXMLReader() throws SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(this);
        return reader;
    }

    /**
     * 从localTableConfigMap和ResourceLoader获取tableConfig
     *
     * @return
     */
    protected TableConfig getTableConfig(String tableKey) {
        tableKey = tableKey.replaceAll(" ", "");
        TableConfig tableConfig = localTableConfigMap.get(tableKey);
        if (tableConfig == null)
            tableConfig = xMapperLoader.getTableConfig(tableKey);
        return tableConfig;
    }

    private void releaseAll(InputStream sheet) throws IOException {
        curSheetIndex = null;
        postHandlers = new ArrayList<>();
        sst = null;
        stylesTable = null;
        if (sheet != null)
            sheet.close();
    }

    /**
     * 新的sheet继续上一个sheet数据
     */
    protected abstract void continueSet();

    /**
     * 部分重置，不重置TableConfig
     */
    protected abstract void reset();

    /**
     * 部分重置，在reset基础上重置tableconfig
     */
    protected abstract void resetTableConfig(boolean dArgs);

    /**
     * 完全重置
     */
    protected abstract void resetAll();

}
