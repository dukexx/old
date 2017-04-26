package com.dukexx.xport.importprocessor.datawriter;

import com.dukexx.xport.common.CellData;
import com.dukexx.xport.exception.SetBeanPropException;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.tools.ant.util.DateUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class DefaultBeanWriter<T> implements DataWriter<T> {
    private static final long serialVersionUID = 1L;
    private static final String CN_STRING = "java.lang.String";
    private static final String CN_DATE = "java.util.Date";
    private static final String CN_CALENDAR = "java.util.Calendar";
    private static final String CN_BOOLEAN = "java.lang.Boolean";
    private static final String CN_BOOLEAN_B = "boolean";
    private static final String CN_INTEGER = "java.lang.Integer";
    private static final String CN_INT_B = "int";
    private static final String CN_BYTE = "java.lang.Byte";
    private static final String CN_BYTE_B = "byte";
    private static final String CN_SHORT = "java.lang.Short";
    private static final String CN_SHORT_B = "short";
    private static final String CN_LONG = "java.lang.Long";
    private static final String CN_LONG_B = "long";
    private static final String CN_FLOAT = "java.lang.Float";
    private static final String CN_FLOAT_B = "float";
    private static final String CN_DOUBLE = "java.lang.Double";
    private static final String CN_DOUBLE_B = "double";
    private static final String CN_CHARACTER = "java.lang.Character";
    private static final String CN_CHAR_B = "char";



    private Class<T> type;
    private T data;
    //缓存write方法
    private Map<String, Method> writeMethods = new HashMap<>();
    //缓存属性class
    private Map<String, String> propTypes = new HashMap<>();

    public DefaultBeanWriter(Class<T> type) {
        this.type = type;
        try {
            this.data = type.newInstance();
        } catch (IllegalAccessException |InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeStringData(String key, String data, int type,String format) {
        String clazz = propTypes.get(key);
        Method method = writeMethods.get(key);
        try {
            if (clazz == null||method==null) {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(key,this.type);
                method = propertyDescriptor.getWriteMethod();
                clazz = propertyDescriptor.getPropertyType().getName();
                writeMethods.put(key, method);
                propTypes.put(key, clazz);
            }
            if (data == null) {
                method.invoke(this.data, null);
            }
            switch (type) {
                case CellData.SSTINDEX:
                case CellData.INLINESTR:
                case CellData.STRING:
                case CellData.NUMBER:
                    switch (clazz) {
                        case CN_STRING:
                            method.invoke(this.data, data);
                            break;
                        case CN_BYTE:
                        case CN_BYTE_B:
                            method.invoke(this.data, new Double(data).byteValue());
                            break;
                        case CN_SHORT:
                        case CN_SHORT_B:
                            method.invoke(this.data, new Double(data).shortValue());
                            break;
                        case CN_INTEGER:
                        case CN_INT_B:
                            method.invoke(this.data, new Double(data).intValue());
                            break;
                        case CN_LONG:
                        case CN_LONG_B:
                            method.invoke(this.data, new Double(data).longValue());
                            break;
                        case CN_FLOAT:
                        case CN_FLOAT_B:
                            method.invoke(this.data, new Double(data).floatValue());
                            break;
                        case CN_DOUBLE:
                        case CN_DOUBLE_B:
                            method.invoke(this.data, new Double(data));
                            break;
                        case CN_CHARACTER:
                        case CN_CHAR_B:
                            if (data.length() == 1) {
                                method.invoke(this.data, data.charAt(0));
                            }else{
                                throw new SetBeanPropException("cannot set value of type:string to property:" + key
                                        + " of type:" + clazz+", cause: to long");
                            }
                            break;
                        default:
                            // boolean date calendar
                            if (type != CellData.NUMBER) {
                                switch (clazz) {
                                    case CN_BOOLEAN:
                                    case CN_BOOLEAN_B:
                                        method.invoke(this.data, Boolean.parseBoolean(data));
                                        break;
                                    case CN_DATE:
                                        method.invoke(this.data, DateUtils.parseDateFromHeader(data));
                                        break;
                                    case CN_CALENDAR:
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(DateUtils.parseDateFromHeader(data));
                                        method.invoke(this.data, calendar);
                                        break;
                                    default:
                                        throw new SetBeanPropException("cannot set value of type:string to property:"
                                        +key+" of type:"+clazz );
                                }
                            }
                    }
                    break;
                case CellData.DATE:
                    if (CN_DATE.equals(clazz)) {
                        method.invoke(this.data, DateUtil.getJavaDate(Double.parseDouble(data)));
                    } else if (CN_CALENDAR.equals(clazz)) {
                        method.invoke(this.data, DateUtil.getJavaCalendar(Double.parseDouble(data)));
                    } else if (CN_STRING.equals(clazz)) {
                        method.invoke(this.data, new SimpleDateFormat(format).format(DateUtil
                                .getJavaDate(Double.parseDouble(data))));
                    } else {
                        throw new SetBeanPropException("cannot set value of type:date to property:" + key
                                + " of type:" + clazz);
                    }
                    break;
                case CellData.FORMULA:
                    if (CN_STRING.equals(clazz)) {
                        method.invoke(this.data, data);
                    } else {
                        throw new SetBeanPropException("cannot set value of type:furmula(String) to property:" + key
                                + " of type:" + clazz);
                    }
                    break;
                case CellData.BOOL:
                    if (CN_BOOLEAN.equals(clazz)||CN_BOOLEAN_B.equals(clazz)) {
                        method.invoke(this.data, Boolean.parseBoolean(data));
                    } else if (CN_STRING.equals(clazz)) {
                        method.invoke(this.data, data);
                    } else {
                        throw new SetBeanPropException("cannot set value of type:boolean to property:" + key
                                + " of type:" + clazz);
                    }
                    break;
                default:
                    if (CN_STRING.equals(clazz)) {
                        method.invoke(this.data, data);
                    } else {
                        throw new SetBeanPropException("cannot set value of type:other to property:" + key
                                + " of type:" + clazz);
                    }
            }
        } catch (IllegalAccessException |InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IntrospectionException e) {
            //内省异常
            throw new SetBeanPropException("cannot set value to property:"+key+", cause:cannot find property:"+key
                    +" in the bean");
        }  catch (ParseException e) {
            throw new SetBeanPropException("cannot set value of type:string to property:" + key + " of type:" + clazz
                    + ", cause:illegal data format cannot be parse");
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T popData() {
        T result = data;
        try {
            data = type.newInstance();
        } catch (InstantiationException |IllegalAccessException e) {
            throw new RuntimeException("illegal bean type:"+type+", the type should be newInstance");
        }
        return result;
    }

    @Override
    public void flush() {
        type = null;
        data=null;
        writeMethods = new HashMap<>();
        propTypes = new HashMap<>();
    }
}
