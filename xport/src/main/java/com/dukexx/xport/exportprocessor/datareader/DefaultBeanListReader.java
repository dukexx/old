package com.dukexx.xport.exportprocessor.datareader;


import com.dukexx.xport.exception.NotFoundFieldException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class DefaultBeanListReader<T> extends DefaultListReader<T>{
    private static final long serialVersionUID = 1L;
    private Class clazz;

    //缓存read方法
    private Map<String, Method> methodMap = new HashMap<>();

    public DefaultBeanListReader(List<T> sourceData) {
        this.sourceData = sourceData;
        length = sourceData.size();
        if (length != 0) {
            curRowData = sourceData.get(0);
            clazz = curRowData.getClass();
        }
        iterator = sourceData.iterator();
    }

    @Override
    public Object getValue(int row, String field) {
        Method method = getReaderMethod(field);
        T t = sourceData.get(row);
        if (t == null) {
            return null;
        }
        try {
            return method.invoke(t,null);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getCurValue(String field) {
        if (curRow >= length) {
            throw new IndexOutOfBoundsException("cur row index out of record count");
        }
        if (curRowData == null) {
            return null;
        }
        Method method = getReaderMethod(field);
        try {
            return method.invoke(curRowData, null);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取写方法，并放入缓存
     * @param field
     * @return
     */
    private Method getReaderMethod(String field) {
        Method method = methodMap.get(field);
        if (method == null) {
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field, clazz);
                method = propertyDescriptor.getReadMethod();
                methodMap.put(field, method);
                return method;
            } catch (IntrospectionException e) {
                throw new NotFoundFieldException(field, e);
            }
        }
        return method;
    }
}
