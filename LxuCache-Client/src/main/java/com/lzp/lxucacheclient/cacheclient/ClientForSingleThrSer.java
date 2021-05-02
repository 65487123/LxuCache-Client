package com.lzp.lxucacheclient.cacheclient;

import java.util.List;
import java.util.Map;

public interface ClientForSingleThrSer extends Client {

    /**
     * Description ：put多个key-value
     * @param keysAndValues 多组key-value
     * @Return ok，因为mput不会失败
     **/
    void mput(Map<String,String> keysAndValues);

    /**
     * Description ：get多个key-value,如果key对应的value不是String类型会抛CacheDataExcepiton
     * @param keys 多个key
     * @Return key对应的value集合
     **/
    List<String> mget(String... keys);

}
