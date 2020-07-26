package com.lzp.util;

import java.util.*;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/9 17:06
 */
public class ValueUtil {
    /**
     * Description ：把map转为字符串
     *
     * @param map 需要序列化的map
     * @Return
     **/
    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append("©")
                    .append(entry.getValue()).append("⚫");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }


    /**
     * Description ：由于把map转为字符串纯粹只为满足自己的私有协议，并不会给外界使用，所以没必要转成标准json。
     * 这样序列化反序列化效率能高一点，网络传输的内容也少点。
     *
     * @param map 需要序列化的map
     * @Return
     **/
    public static String mapWithDouToSimplifiedJson(Map<String, Double> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append("©")
                    .append(entry.getValue()).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    /**
     * Description ：把字符串数组变为字符串
     *
     * @param
     * @Return
     **/
    public static String arrayToString(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string  : strings) {
            stringBuilder.append(string).append("⚫");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
    /**
     * Description ：把collection变为字符串
     *
     * @param
     * @Return
     **/
    public static String collectionToString(Collection<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string).append("⚫");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    /**
     * Description ：把字符串数组变为字符串
     *
     * @param
     * @Return
     **/
    public static List<String> stringToList(String listString) {
        List<String> list = new ArrayList<>();
        for (String string : listString.split("⚫")) {
            list.add(string);
        }
        return list;
    }
    /**
     * Description ：把字符串转为Set
     *
     * @param
     * @Return
     **/
    public static Set<String> stringToSet(String listString) {
        Set<String> set = new HashSet<>();
        for (String string : listString.split("⚫")) {
            set.add(string);
        }
        return set;
    }
}
