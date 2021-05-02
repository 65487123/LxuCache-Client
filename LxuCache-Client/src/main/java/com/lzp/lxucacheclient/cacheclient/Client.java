package com.lzp.lxucacheclient.cacheclient;

import com.lzp.lxucacheclient.exception.CacheDataException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:这个缓存和redis类似，是key-value结构， key只能是String类型，value主要有五种类型：
 * 1、String 2、Set<String> 3 Map<String,String> (相当于redis的hash) 4 zset(sorted set) 5 List<String>
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:57
 */
public interface Client extends AutoCloseable{

    /**
      * Description ：put 一个key-value
      * @param key 缓存的key
      * @param value 缓存的value
      * @Return 这个key原先对应的value 或者 null（如果原先没有这个key）
      **/
    String put(String key, String value) throws InterruptedException;

    /**
     * Description ：得到key对应的value值
     * @param key 缓存的key
     * @Return 这个key对应的value 或者 null
     **/
    String get(String key);


    /**
     * Description ：key 对应的value 自增1， 如果value不是纯数值或者加1以后超出范围 会抛CacheDataException
     * @param key key值
     * @Return 自增一以后的value值
     **/
    Long incr(String key);

    /**
     * Description ：key 对应的value 自减1， 如果value不是纯数值或者加1以后超出范围 会抛CacheDataException
     * @param key key值
     * @Return 自减一以后的value值
     **/
    Long decr(String key);

    /**
     * Description ：用新map覆盖key对应的值
     * @param key key值
     * @param map 新value
     * @Return
     **/
    void hput(String key,Map<String,String> map);

    /**
     * Description ：把传入的map和原先的map进行合并.
     * 传入的map的key和value不能包含字符'⚫',不然序列化可能会出现问题
     * @param key key值
     * @param map 要进行合并的map
     * @Return
     **/
    void hmerge(String key, Map<String, String> map);

    /**
     * Description ：在尾上添加新list
     * 传入的list的元素不能包含字符'⚫',不然序列化可能会出现问题
     * @param key key值
     * @param list 新value
     * @Return
     **/
    void lpush(String key, List<String> list);



    /**
     * Description ：key对应的set增加值，如果key不存在会创建
     * 传入的set的元素不能包含字符'⚫',不然序列化可能会出现问题
     * @param key key值
     * @param set 增加的值
     * @Return
     **/
    void sadd(String key, Set<String> set);

    /**
     * Description ：key对应的set增加值，如果key不存在会创建
     * @param key key值
     * @param set 增加的值
     * @Return
     **/
    void zadd(String key, Map<Double, String> set);

    /**
     * Description ：key对应的set增加值，如果key不存在会创建
     * @param key key值
     * @Return
     **/
    void zadd(String key, Double score,String member);
    /**
     * Description ：根据key和map的key得到Map类型里的一条记录
     * @param key key值
     * @param field map的key
     * @Return map里key为field的具体的值
     **/
    String hget(String key, String field) throws CacheDataException;

    /**
     * Description ：根据key得到List类型的整个value
     * @param key key值
     * @Return 整个List
     **/
    List<String> getList(String key) throws CacheDataException;

    /**
     * Description ：根据key得到Set类型的整个value
     * @param key key值
     * @Return 整个Set
     **/
    Set<String> getSet(String key) throws CacheDataException;

    /**
     * Description ：根据key判断Set类型的value是否包含某一个元素
     * @param key key值
     * @param element 要判断是否存在的元素
     * @Return true 如果存在， false 如果不存在
     **/
    boolean scontain(String key,String element) throws CacheDataException;


    /**
     * Description ：给key设超时时间
     * @param key key值
     * @param seconds 超时时间
     * @Return 1 如果设成功， 0 如果设置失败（key不存在)
     **/
    Long expire(final String key, final int seconds);

    /**
     * Description ：移除key对应的key-value
     * @param key key值
     **/
    void remove(String key);

    Set<String> zrange(String key, long start, long end);

    Long zrem(String key, String... member);

    Double zincrby(String key, double score, String member);

    Long zrank(String key, String member);

    Long zrevrank(String key, String member);

    Set<String> zrevrange(String key, long start, long end);

    Long zcard(String key);

    Double zscore(String key, String member);

    Long zcount(String key, double min, double max);

    Set<String> zrangeByScore(String key, double min, double max);

    void hset(String key , String member,String value);
}
