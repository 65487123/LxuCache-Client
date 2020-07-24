import com.alibaba.fastjson.JSON;
import com.google.protobuf.MessageLite;
import com.lzp.cacheclient.CacheClient;
import com.lzp.protocol.CommandDTO;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Description:有连接池的情况下，多客户端并发写，性能比redis要高一点
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 11:24
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, IOException {

        /*ExecutorService threadPool = new ThreadPoolExecutor(2,2,0, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000000));
        //三个连接池
        List<CacheClient> cacheClientList = new ArrayList<>();
        List<Jedis> jedises = new ArrayList<>();
        List<CacheClient> cacheClientList1 = new ArrayList<>();
        //往三个连接池里加连接
        for (int i=0;i<256;i++){
            cacheClientList.add(new CacheClient("10.240.30.78",8888));
        }
        for (int i=0;i<256;i++){
            cacheClientList1.add(new CacheClient("10.240.30.78" ,8887));
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i=0;i<256;i++){
            jedises.add(new Jedis("10.240.30.78",6379));
        }
        CountDownLatch countDownLatch = new CountDownLatch(2);
        long now = Instant.now().toEpochMilli();
        for (int i = 0; i <2; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                Jedis jedis = jedises.get(finalI);
                for (long j = 0; j < 20000; j++) {
                    jedis.set(String.valueOf(j), String.valueOf(j));
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Instant.now().toEpochMilli()-now);
        now = Instant.now().toEpochMilli();
        CountDownLatch countDownLatch0 = new CountDownLatch(2);
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                CacheClient cacheClient = cacheClientList.get(finalI);
                for (long j = 0; j < 20000; j++) {
                    cacheClient.put(String.valueOf(j), String.valueOf(j));
                }
                countDownLatch0.countDown();
            });
        }
        try {
            countDownLatch0.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Instant.now().toEpochMilli()-now);
        CountDownLatch countDownLatch1 = new CountDownLatch(2);
        now = Instant.now().toEpochMilli();
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                CacheClient cacheClient = cacheClientList1.get(finalI);
                for (long j = 0; j < 20000; j++) {
                    cacheClient.put(String.valueOf(j), String.valueOf(j));
                }
                countDownLatch1.countDown();
            });
        }
        try {
            countDownLatch1.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Instant.now().toEpochMilli()-now);
        threadPool.shutdown();*/
        /*Jedis jedis1= jedises.get(3);
        now = Instant.now().toEpochMilli();
        for (int i=0 ; i<10000;i++){
            jedis1.set(String.valueOf(i),String.valueOf(i));
        }
        CacheClient cacheClient = cacheClientList1.get(32);
        System.out.println(Instant.now().toEpochMilli()-now);
        now = Instant.now().toEpochMilli();
        for (int i=0 ; i<10000;i++){
            cacheClient.put(String.valueOf(i),String.valueOf(i));
        }
        System.out.println(Instant.now().toEpochMilli()-now);*/
        CacheClient cacheClient = new CacheClient("127.0.0.1",8887);
        cacheClient.put("1","2");
        List<String> list = new ArrayList<>();
        list.add("3,");
        list.add("5");
        cacheClient.lpush("2",list);
        Map<String,String> map = new HashMap<>();
        map.put("3","2");
        map.put("5","1");
        cacheClient.hput("3",map);
        Set<String>  set = new HashSet<>();
        set.add("3");
        set.add("5");
        set.add("1");
        cacheClient.sadd("4",set);
        System.out.println(cacheClient.get("1"));
        System.out.println(cacheClient.getList("2"));
        System.out.println(cacheClient.hget("3","3"));
        System.out.println(cacheClient.hget("3","5"));
        System.out.println(cacheClient.getSet("4"));
        System.out.println(cacheClient.incr("1"));
        System.out.println(cacheClient.decr("1"));
        System.out.println(cacheClient.scontain("4","5"));
        cacheClient.expire("1",3);
        cacheClient.expire("3",5);
        Thread.sleep(3002);
        System.out.println(cacheClient.get("1"));
        System.out.println(cacheClient.get("3"));
        Thread.sleep(2001);
        System.out.println(cacheClient.get("3"));


    }
}
