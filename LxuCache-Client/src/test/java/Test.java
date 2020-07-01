import com.lzp.cacheclient.CacheClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Description:有连接池的情况下，多客户端并发写，性能比redis要高一点
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 11:24
 */
public class Test {
    public static void main(String[] args) {

        ExecutorService threadPool = new ThreadPoolExecutor(30,30,0, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000000));
        //三个连接池
        List<CacheClient> cacheClientList = new ArrayList<>();
        /*List<Jedis> jedises = new ArrayList<>();*/
        List<CacheClient> cacheClientList1 = new ArrayList<>();
        //往三个连接池里加连接
        for (int i=0;i<100;i++){
            cacheClientList.add(new CacheClient("10.240.30.78",8888));
        }
        for (int i=0;i<100;i++){
            cacheClientList1.add(new CacheClient("10.240.30.78",8887));
        }
        /*for (int i=0;i<100;i++){
            jedises.add(new Jedis("10.240.30.78",6379));
        }*/
        /*
        long now = Instant.now().toEpochMilli();
        CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                Jedis jedis = jedises.get(finalI);
                for (long j = 0; j < 500; j++) {
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
        System.out.println(Instant.now().toEpochMilli()-now);*/
        long now = Instant.now().toEpochMilli();
        CountDownLatch countDownLatch0 = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                CacheClient cacheClient = cacheClientList.get(finalI);
                for (long j = 0; j < 500; j++) {
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

        now = Instant.now().toEpochMilli();
        CountDownLatch countDownLatch1 = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                CacheClient cacheClient = cacheClientList1.get(finalI);
                for (long j = 0; j < 500; j++) {
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
        threadPool.shutdown();
        System.out.println(Instant.now().toEpochMilli()-now);
    }
}
