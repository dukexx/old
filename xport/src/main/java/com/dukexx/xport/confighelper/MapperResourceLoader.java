package com.dukexx.xport.confighelper;

import com.dukexx.xport.common.ProcessKind;
import com.dukexx.xport.common.SheetModel;
import com.dukexx.xport.exception.IllegalFieldConfigException;
import com.dukexx.xport.exception.IllegalTableConfigException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class MapperResourceLoader extends DefaultResourceLoader implements MapperLoader, Serializable {

    private static final long serialVersionUID = 1L;

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final Yaml yaml = new Yaml();
    //所有表的配置
    private final Map<String, TableConfig> tableConfigMap = new HashMap<String, TableConfig>();

    private String xlsMapperLocation;

    @Required
    public void setXlsMapperLocation(String xlsMapperLocation) {
        this.xlsMapperLocation = xlsMapperLocation;
    }

    /**
     * 初始化方法，属性注入完成后调用，加载配置等
     *
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public void init() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        //加载配置文件
        List<Resource> resources = getResources();
        //加载合并所有配置树
        List<Map<String, Object>> confList = new LinkedList();
        for (Resource resource : resources) {
            Map<String, Object> xlsmappers = (Map<String, Object>) yaml.load(resource.getInputStream());
            if (xlsmappers != null)
                confList.add(xlsmappers);
        }
        for (Map<String, Object> confMap : confList) {
            Set<String> keySet = confMap.keySet();
            for (String key : keySet) {
                key = key.replaceAll(" ", "");
                Object value = confMap.get(key);
                //判断转化key
                int i = key.indexOf(":");
                if (i != -1) {
                    String prefix = key.substring(0, i).toLowerCase();
                    if ("import".equals(prefix) || "export".equals(prefix)) {
                        tableConfigMap.put(key, buildTableConfig(key, value,
                                "import".equals(prefix) ? ProcessKind.IMPORT : ProcessKind.EXPORT));
                        continue;
                    }
                }
                String realKeyBody = key.substring(i + 1);
                tableConfigMap.put("import:" + realKeyBody, buildTableConfig("import:" + realKeyBody, value, ProcessKind.IMPORT));
                tableConfigMap.put("export:" + realKeyBody, buildTableConfig("export:" + realKeyBody, value, ProcessKind.EXPORT));
            }
        }
    }


    /**
     * build tableConfig
     *
     * @param oTableConfMap
     * @param tableKey
     * @param processKind
     * @return
     */
    protected TableConfig buildTableConfig(String tableKey, Object oTableConfMap, ProcessKind processKind) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        Map<String, Object> tableConfMap = (Map<String, Object>) oTableConfMap;
        Map<String, Object> fieldConfsMap = (Map<String, Object>) tableConfMap.get("fields");
        if (tableConfMap == null || tableConfMap.size() == 0)
            return null;
        TableConfig tableConfig = new TableConfig(processKind);
        //设置一级属性
        tableConfig.setTableKey(tableKey);
        tableConfig.setTable((String) tableConfMap.get("table"));
        tableConfig.setHeadRow(parseInt(tableConfMap.get("headRow"), "headRow"));
        tableConfig.setDataRow(parseInt(tableConfMap.get("dataRow"), "dataRow"));
        tableConfig.setFilename((String) tableConfMap.get("filename"));
        tableConfig.setSheetName((String) tableConfMap.get("sheetName"));
        String sheetModel = (String) tableConfMap.get("sheetModel");
        if (sheetModel != null) {
            if ("reset".equals(sheetModel.toLowerCase())) {
                tableConfig.setSheetModel(SheetModel.RESET);
            } else if ("continuous".equals(sheetModel.toLowerCase())) {
                tableConfig.setSheetModel(SheetModel.CONTINUOUS);
            } else {
                throw new IllegalTableConfigException("illegal config of sheetModel:" + sheetModel +
                        ", sheetModel must be reset or continuous");
            }
        }
        tableConfig.setLowMem(parseBoolean(tableConfMap.get("lowMem"), "lowMem"));
        tableConfig.setMaxHeadRow(parseInt(tableConfMap.get("maxHeadRow"), "maxHeadRow"));
        tableConfig.setRowHeight(parseInt(tableConfMap.get("rowHeight"),"rowHeight"));
        tableConfig.setGuaFields(parseInt(tableConfMap.get("guaFields"), "guaFields"));
        tableConfig.setFirstColm(parseInt(tableConfMap.get("firstColm"), "firstColm"));
        tableConfig.setMemCount(parseInt(tableConfMap.get("memCount"), "memCount"));
        tableConfig.setSheetRows(parseInt(tableConfMap.get("sheetRows"), "sheetRows"));
        tableConfig.setImportPostHandler((String) tableConfMap.get("importPostHandler"));
        tableConfig.setExportPostHandler((String) tableConfMap.get("exportPostHandler"));
        tableConfig.setHeadResolver((String) tableConfMap.get("headResolver"));
        //set fieldConfigs
        buildTableFieldConfig(tableConfig, fieldConfsMap);
        tableConfig.build();
        return tableConfig;
    }

    protected void buildTableFieldConfig(TableConfig tableConfig, Map<String, Object> fieldConfsMap) {
        for (Map.Entry<String, Object> entry : fieldConfsMap.entrySet()) {
            Object fieldObj = entry.getValue();
            //process 3 kinds fieldconfig
            FieldConfig fieldConfig = null;
            if (fieldObj instanceof Map) {
                Map<String, Object> fieldConfMap = (Map<String, Object>) fieldObj;
                fieldConfig = new FieldConfig(entry.getKey(), parseInt(fieldConfMap.get("index"), "index"),
                        (String) fieldConfMap.get("name"), (String) fieldConfMap.get("format"), (String) fieldConfMap.get("type"),
                        (String) fieldConfMap.get("column"), parseInt(fieldConfMap.get("width"), "width"));
            } else if (fieldObj instanceof Integer) {
                fieldConfig = new FieldConfig(entry.getKey(), (Integer) fieldObj, null, null, null,
                        null, null);
            } else if (fieldObj instanceof String) {
                fieldConfig = new FieldConfig(entry.getKey(), null, (String) fieldObj, null, null,
                        null, null);
            } else {
                throw new IllegalFieldConfigException("cannot resolve fieldconfig: " + entry.getKey() + ": "
                        + (fieldObj == null ? "\" \"" : fieldObj.getClass())
                        + " ,need" + Integer.class + " or " + String.class);
            }
            //set fieldConfig
            tableConfig.addFieldConfig(fieldConfig);
        }
    }

    /**
     * get tableConfig by tableKey
     *
     * @param tableKey
     * @return
     */
    @Override
    public TableConfig getTableConfig(String tableKey) {
        return tableConfigMap.get(tableKey);
    }

    /**
     * resolve config argument to Integer
     *
     * @param o
     * @param param
     * @return
     */
    private Integer parseInt(Object o, String param) {
        if (o == null)
            return null;
        if (o instanceof Integer)
            return (Integer) o;
        else if (o instanceof String)
            return Integer.parseInt(((String) o).trim());
        else {
            throw new IllegalTableConfigException("illegal argument " + param + ":" + o + ", " + param + "must be int");
        }
    }

    /**
     * resolve config argument to Boolean
     *
     * @param o
     * @param param
     * @return
     */
    private Boolean parseBoolean(Object o, String param) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return Boolean.parseBoolean((String) o);
        } else {
            throw new IllegalTableConfigException("illegal argument " + param + ":" + o + ", " + param + "must be boolean");
        }
    }

    /**
     * get mapper resources
     *
     * @return
     * @throws IOException
     */
    private List<Resource> getResources() throws IOException {
        //获取所有的配置文件
        List<Resource> resources = new LinkedList<Resource>();
        String[] mapperLocations = xlsMapperLocation.split(",");
        for (String mapperLocation : mapperLocations) {
            String trimLocation = mapperLocation.trim();
            if (StringUtils.isEmpty(trimLocation))
                continue;
            Resource[] resources1 = resolver.getResources(trimLocation);
            for (Resource resource : resources1) {
                if (!isYaml(resource.getFilename()))
                    continue;
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * judgment is yaml
     *
     * @param name
     * @return
     */
    private boolean isYaml(String name) {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            String suffex = name.substring(i);
            if (suffex.equals(".yaml") || suffex.equals(".yml"))
                return true;
        }
        return false;
    }

    /**
     * 添加新的tableconfig到loader中
     * 未确定回收或者落地机制，防止越来越多的占用内存，暂不实现
     * @param tableConfig
     */
//    public void addTableConfig(String tableKey,TableConfig tableConfig) {
//        tableConfigMap.put(tableKey, tableConfig);
//    }

}
