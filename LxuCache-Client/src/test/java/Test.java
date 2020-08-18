
import com.lzp.cacheclient.CacheClient;
import com.lzp.cacheclient.CacheClusterClient;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

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
        for (int i=0;i<100;i++){
            cacheClientList.add(new CacheClient("10.240.30.78",8888));
        }
        for (int i=0;i<100;i++){
            cacheClientList1.add(new CacheClient("10.240.30.78" ,8887));
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i=0;i<100;i++){
            jedises.add(new Jedis("10.240.30.78",6379));
        }
        CountDownLatch countDownLatch = new CountDownLatch(100);
        long now = Instant.now().toEpochMilli();
        for (int i = 0; i <100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                Jedis jedis = jedises.get(finalI);
                for (long j = 0; j < 1000; j++) {
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
        CountDownLatch countDownLatch0 = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                CacheClient cacheClient = cacheClientList.get(finalI);
                for (long j = 0; j < 1000; j++) {
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
        CountDownLatch countDownLatch1 = new CountDownLatch(100);
        now = Instant.now().toEpochMilli();
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                CacheClient cacheClient = cacheClientList1.get(finalI);
                for (long j = 0; j < 1000; j++) {
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
        /*CacheClient cacheClient = new CacheClient("127.0.0.1",8887);
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
        long now = Instant.now().toEpochMilli();
        for (int i=10000;i<45200;i++){
            cacheClient.put(String.valueOf(i),String.valueOf(i));
        }
        System.out.println(Instant.now().toEpochMilli()-now);*/
       /* cacheClient.zadd("165555",1.5,"3");
        cacheClient.zadd("165555",1.6,"1");
        cacheClient.zadd("165555",2.98,"7");
        cacheClient.zadd("165555",8.98,"11");
        cacheClient.zadd("165555",0.98,"5");
        System.out.println(cacheClient.zrange("165555",0,4));*/
        /*Jedis jedis = new Jedis("10.240.30.78",6379);
        jedis.zadd("104441",1.2,"2");
        jedis.zadd("104441",1.5,"24");
        jedis.zadd("104441",1.82,"22");
        jedis.zadd("104441",1.0,"23");
        System.out.println(jedis.zrange("104441",0,3));*/
        //JedisCluster jedisCluster = new JedisCluster();
        List<CacheClusterClient.HostAndPort> hostAndPorts = new ArrayList<>();
        //hostAndPorts.add(new CacheClusterClient.HostAndPort("10.240.30.78",8887));
        hostAndPorts.add(new CacheClusterClient.HostAndPort("10.240.30.78",8886));
        hostAndPorts.add(new CacheClusterClient.HostAndPort("10.240.30.78",8885));

        CacheClusterClient cacheClient = new CacheClusterClient(hostAndPorts);
        /*cacheClient.put("1","2");
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
        System.out.println(cacheClient.get("1"));*/
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
        Thread.sleep(2010);
        System.out.println(cacheClient.get("3"));
        long now = Instant.now().toEpochMilli();
    }
}
