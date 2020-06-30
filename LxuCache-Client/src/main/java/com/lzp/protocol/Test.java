package com.lzp.protocol;

import com.lzp.cacheclient.CacheClient;

import java.time.Instant;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/6/30 16:07
 */
public class Test {
    public static void main(String[] args) {

        CacheClient cacheClient = new CacheClient("127.0.0.1",8888);
        for (int j = 0; j < 10000; j++) {
            System.out.println(cacheClient.put(String.valueOf(j),String.valueOf(j)));
        }
    }
}
