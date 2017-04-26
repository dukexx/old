package com.dukexx.xport.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class XlsUtils {
    private static Map<String, Boolean> dateFormats = new HashMap<>();

    /**
     *  将excel列名转化为index
     */
    public static Integer convertColumnToIndex(String column) {
        column = column.trim().toUpperCase();
        char[] chars = column.toCharArray();
        Integer index=0;
        for(int i=chars.length-1;i>=0;i--) {
            index += (int)(Math.pow(26, chars.length - 1 - i)*(chars[i]-64));
        }
        return index-1;
    }

    /**
     * 将index转化为excel列
     * @param index
     * @return
     */
    public static String convertIndexToColumn(Integer index) {
        if (index == null || index < 0)
            return null;
        String column = "";
        index++;
        while (true) {
            int i=index%26;
            column = (char) (i+64)+column;
            index=index/26;
            if(index==0)
                break;
        }
        return column;
    }

    /**
     * 提取坐标中的列名部分为索引
     * @param coordinate
     * @return
     */
    public static int getIndexFromCoord(String coordinate) {
        return convertColumnToIndex(getColumnFromCoord(coordinate));
    }

    /**
     * 提取坐标中的列名部分
     * @param coordinate
     * @return
     */
    public static String getColumnFromCoord(String coordinate) {
        for(int i=0;i<coordinate.length();i++) {
            char cha = coordinate.charAt(i);
            if (cha > 47 && cha < 58) {
                return coordinate.substring(0, i);
            }
        }
        return coordinate;
    }

    /**
     * 复制数组
     * @param tarArray
     * @param array
     * @return
     */
    public static Object[] growArray(Object[] tarArray, Object[] array) {
        for(int i=0;i<array.length;i++) {
            tarArray[i] = array[i];
        }
        return tarArray;
    }

    public static<T> T[] setArrayValue(T[] ts, int index, T t) {
        if (index > ts.length - 1) {
            ts = (T[]) growArray(new Object[index + 20], ts);
        }
        ts[index]=t;
        return ts;
    }

    public static boolean isDateFormat(String format) {
        if(format==null)
            return false;
        Boolean isDate = dateFormats.get(format);
        if(isDate!=null)
            return isDate;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            simpleDateFormat.format(new Date());
            char[] chars = format.toCharArray();
            boolean isIn=false;
            for(int i=0;i<chars.length;i++) {
                char cha = chars[i];
                if (cha == '\"' || cha == '\'') {
                    isIn=!isIn;
                }else{
                    if (!isIn) {
                        if (cha == 'd' || cha == 'D' || cha == 'H' || cha == 'y' || cha == 'M' || cha == 'm' || cha == 's'
                                || cha == 'w' || cha == 'k' || cha == 'W' || cha == 'G' || cha == 'F' || cha == 'E'
                                || cha == 'a' || cha == 'K' || cha == 'h' || cha == 'S' || cha == 'z' || cha == 'Z') {
                            isDate=true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            isDate=false;
        }
        isDate=isDate==null?false:isDate;
        //缓存结果
        setDateFormatsValue(format,isDate);
        return isDate;
    }

    private static void setDateFormatsValue(String format,Boolean isDate) {
        if(dateFormats.size()>1000)
            dateFormats.clear();
        dateFormats.put(format, isDate);
    }

}
