package com.dukexx.xport.importprocessor.datawriter;

import java.io.Serializable;

/**
 * 处理导入过程中的数据
 *
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public interface DataWriter<T> extends Serializable {

    void writeStringData(String key, String data, int type,String format);

    Class<T> getType();

    T popData();

    void flush();
}
