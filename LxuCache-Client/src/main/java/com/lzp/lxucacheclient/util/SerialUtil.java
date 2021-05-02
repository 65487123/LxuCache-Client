package com.lzp.lxucacheclient.util;

import java.util.*;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/9 17:06
 */
public class SerialUtil {
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
                    .append(entry.getValue()).append("È");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }


    /**
     * Description ：
     *
     * @param map 需要序列化的map
     * @Return
     **/
    public static String mapWithDouToString(Map<Double, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append("©")
                    .append(entry.getValue()).append("È");
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
            stringBuilder.append(string).append("È");
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
            stringBuilder.append(string).append("È");
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
        for (String string : listString.split("È")) {
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
        Set<String> set = new LinkedHashSet<>();
        for (String string : listString.split("È")) {
            set.add(string);
        }
        return set;
    }
}
