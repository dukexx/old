package com.dukexx.xport.importprocessor.datawriter;

import com.dukexx.xport.common.CellData;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class DefaultMapWriter implements DataWriter<Map> {
    private static final long serialVersionUID = 1L;
    private Class type;
    private Map data;

    public DefaultMapWriter(Class type) {
        if (type == Map.class) {
            this.type = HashMap.class;
        }else{
            this.type=type;
        }
        try {
            data = (Map) this.type.newInstance();
        } catch (InstantiationException |IllegalAccessException e) {
            throw new RuntimeException("illegal Map type:"+type+", DefaultMapWriter need type extends java.util.Map",e);
        }
    }

    @Override
    public void writeStringData(String key, String data, int type,String format) {
        switch (type) {
            case CellData.SSTINDEX:
                this.data.put(key, data);
                break;
            case CellData.NUMBER:
                this.data.put(key, Double.parseDouble(data));
                break;
            case CellData.DATE:
                this.data.put(key, DateUtil.getJavaDate(Double.parseDouble(data)));
                break;
            case CellData.INLINESTR:
                this.data.put(key, data);
                break;
            case CellData.STRING:
                this.data.put(key, data);
                break;
            case CellData.FORMULA:
                this.data.put(key, data);
                break;
            case CellData.BOOL:
                this.data.put(key, Boolean.parseBoolean(data));
                break;
            default:
        }
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public Map popData() {
        Map result=data;
        try {
            data = (Map) type.newInstance();
        } catch (InstantiationException |IllegalAccessException e) {
            throw new RuntimeException("illegal Map type:"+type+", the type should be newInstance");
        }
        return result;
    }

    @Override
    public void flush() {
        type = null;
        data = null;
    }
}
