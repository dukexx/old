package com.dukexx.xport.headresolver;

import com.dukexx.xport.common.utils.XlsUtils;
import com.dukexx.xport.common.CellData;
import com.dukexx.xport.confighelper.TableConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class DefaultNameHeadResolver implements HeadResolver {
    private Integer guaFields = 2;
    private Map<String, CellData> cellDataMap;
    private Map<String,List<String>> nameFieldsMap;
    private Map<String, List<String>> columnFieldsMap = new HashMap<>();
    private List<String>[] indexFieldsMap=new List[20];

    private boolean isHeadEnd;

    public DefaultNameHeadResolver() {

    }

    @Override
    public boolean isHeadEnd(Map<String, CellData> cellDataMap, TableConfig tableConfig) {
        if (nameFieldsMap == null) {
            nameFieldsMap = tableConfig.getNameFieldMap();
        }
        Integer getGuaFields = tableConfig.getGuaFields();
        if (getGuaFields != null) {
            guaFields = getGuaFields;
        }
        this.cellDataMap=cellDataMap;
        int count=0;
        for (Map.Entry<String, CellData> entry : cellDataMap.entrySet()) {
            Object dataValue=entry.getValue().getValue();
            if (dataValue instanceof String) {
                List list = nameFieldsMap.get(dataValue);
                if (list != null) {
                    count++;
                    columnFieldsMap.put(entry.getKey(),list);
                    setIndexFieldsMapValue(XlsUtils.getIndexFromCoord(entry.getKey()),list);
                }
            }
        }
        if(count>=guaFields)
            isHeadEnd=true;
        return isHeadEnd;
    }

    /**
     * merge columnFieldMap
     * @param oldColumnFieldsMap
     * @return
     */
    @Override
    public Map<String, List<String>> getColumnFieldsMap(Map<String, List<String>> oldColumnFieldsMap) {
        if (!isHeadEnd) {
            return oldColumnFieldsMap;
        }
        for (Map.Entry<String, List<String>> entry1 : columnFieldsMap.entrySet()) {
            List<String> list1=entry1.getValue();
            String key1=entry1.getKey();
            int size1=list1.size();
            for(int i1=0;i1<size1;i1++) {
                String field1 = list1.get(i1);
                for (Map.Entry<String, List<String>> entry2 : oldColumnFieldsMap.entrySet()) {
                    List<String> list2 = entry2.getValue();
                    Iterator iterator2 = list2.iterator();
                    while (iterator2.hasNext()) {
                        if (field1.equals(iterator2.next())) {
                            iterator2.remove();
                        }
                    }
                }
            }
            if (oldColumnFieldsMap.get(key1) != null) {
                oldColumnFieldsMap.get(key1).addAll(list1);
            }else{
                oldColumnFieldsMap.put(key1, list1);
            }
        }
        return oldColumnFieldsMap;
    }

    /**
     * merge indexFieldsArray
     * @param oldIndexFieldsMap
     * @return
     */
    @Override
    public List<String>[] getIndexFieldsMap(List<String>[] oldIndexFieldsMap) {
        if (!isHeadEnd) {
            return oldIndexFieldsMap;
        }
        int length1=indexFieldsMap.length;
        for(int i1=0;i1<length1;i1++) {
            List<String> list1 = indexFieldsMap[i1];
            if (list1 == null) {
                continue;
            }
            int size1=list1.size();
            for(int in1=0;in1<size1;in1++) {
                String field = list1.get(in1);
                int length2=oldIndexFieldsMap.length;
                for(int i2=0;i2<length2;i2++) {
                    List<String> list2 = oldIndexFieldsMap[i2];
                    if (list2 == null) {
                        continue;
                    }
                    Iterator iterator2 = list2.iterator();
                    while (iterator2.hasNext()) {
                        if (field.equals(iterator2.next())) {
                            iterator2.remove();
                        }
                    }
                }
            }
            if(i1>oldIndexFieldsMap.length)
                oldIndexFieldsMap = (List<String>[])XlsUtils.growArray(new List[i1 + 20], oldIndexFieldsMap);
            if(oldIndexFieldsMap[i1]==null)
                oldIndexFieldsMap[i1]=list1;
            else
                oldIndexFieldsMap[i1].addAll(list1);
        }
        return oldIndexFieldsMap;
    }

    private void setIndexFieldsMapValue(int index,List list) {
        if (index > indexFieldsMap.length) {
            indexFieldsMap=(List[])XlsUtils.growArray(new List[index+20], indexFieldsMap);
        }
        indexFieldsMap[index]=list;
    }



}
