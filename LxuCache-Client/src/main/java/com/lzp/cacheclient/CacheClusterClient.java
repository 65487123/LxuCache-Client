

package com.lzp.cacheclient;

import com.lzp.exception.CacheDataException;
import com.lzp.nettyhandler.ClusterClientHandler;
import com.lzp.nettyhandler.ClusterClientInitializer;
import com.lzp.protocol.CommandDTO;
import com.lzp.util.HashUtil;
import com.lzp.util.SerialUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;



/**
 * Description:集群版客户端
 *
 * @author: Lu ZePing
 * @date: 2020/8/5 17:46
 */


public class CacheClusterClient implements Client {

    private static final Logger logger = LoggerFactory.getLogger(CacheClusterClient.class);

    private static EventLoopGroup eventExecutors = new NioEventLoopGroup(1);

    private static Bootstrap bootstrap = new Bootstrap();

    /**和主节点断开连接，通过这个线程池里的线程找到新主*/
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("election"));

    /**存这个客户端的线程和每次操作的结果*/
    private ClusterClientHandler.ThreadResultObj threadResultObj;

    /**存所有主节点和每个主节点下存的从节点*/
    private Map<HostAndPort, List<HostAndPort>> hostAndPortListMap = new HashMap<>();

    /**存所有主节点的连接*/
    private Channel[] channels;

    /**主节点个数减一*/
    private final int N;

    /**主节点个数是否为2的次方*/
    private final boolean IS_POWER_OF_TWO;

    /**所有主节点的连接和这些主节点对应的线程结果。handler收到事件会查找这个容器来选择唤醒对应线程*/
    public static Map<Channel, ClusterClientHandler.ThreadResultObj> masterChannelThreadResultMap = new ConcurrentHashMap<>();

    static {
        bootstrap.group(eventExecutors).channel(NioSocketChannel.class).handler(new ClusterClientInitializer());
    }

    public static class HostAndPort {

        private String host;
        private int port;

        public HostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HostAndPort) {
                HostAndPort hp = (HostAndPort) obj;
                return port == hp.port && host.equals(hp.host);
            }
            return false;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port);
        }
    }


    public CacheClusterClient(List<HostAndPort> hostAndPorts) throws InterruptedException {
        threadResultObj = new ClusterClientHandler.ThreadResultObj(null,null);
        Set<HostAndPort> checkedHostAndPorts = new HashSet<>();
        List<Channel> masters = new ArrayList<>();
        for (int i = hostAndPorts.size() - 1; i > -1; i--) {
            HostAndPort hostAndPort = hostAndPorts.get(i);
            if (checkedHostAndPorts.contains(hostAndPort)){
                continue;
            }
            Channel channel;
            channel = bootstrap.connect(hostAndPort.host, hostAndPort.port).sync().channel();
            masterChannelThreadResultMap.put(channel,threadResultObj);
            threadResultObj.setThread(Thread.currentThread());
            channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("getMaster").build());
            LockSupport.park();
            String result = threadResultObj.getResult();
            if ("yes".equals(result)) {
                if (hostAndPortListMap.get(hostAndPort) == null) {
                    hostAndPortListMap.put(hostAndPort, new ArrayList<>());
                    masters.add(channel);
                }
            } else {
                String[] hostPort = result.split(":");
                masterChannelThreadResultMap.remove(channel);
                channel.close().sync();
                hostAndPorts.remove(i);
                for (HostAndPort hostAndPort1 : hostAndPorts) {
                    if (hostPort[0].equals(hostAndPort1.host) && hostPort[1].equals(String.valueOf(hostAndPort1.port))) {
                        List<HostAndPort> slaves;
                        if ((slaves = hostAndPortListMap.get(hostAndPort1)) == null) {
                            slaves = new ArrayList<>();
                            slaves.add(hostAndPort);
                            channel = bootstrap.connect(hostAndPort1.host, hostAndPort1.port).sync().channel();
                            hostAndPortListMap.put(hostAndPort1, slaves);
                            masters.add(channel);
                            masterChannelThreadResultMap.put(channel, threadResultObj);
                            checkedHostAndPorts.add(hostAndPort1);
                        } else {
                            slaves.add(hostAndPort);
                        }
                    }
                }
            }
        }
        channels = new Channel[masters.size()];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = masters.get(i);
            electionOnClose(channels[i], i);
        }
        N = channels.length - 1;
        IS_POWER_OF_TWO = (channels.length & (N)) == 0;
    }

    private void electionOnClose(Channel channel, int index) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        HostAndPort masterHostAndPort = new HostAndPort(inetSocketAddress.getHostString(),inetSocketAddress.getPort());
        List<HostAndPort> slaves = hostAndPortListMap.get(masterHostAndPort);
        if (slaves.size() == 0) {
            return;
        }
        Object lock = this;
        channel.closeFuture().addListener(future -> {
            threadPool.execute(() -> {
                //收到断开连接事件，选举新主；
                //下面的操作是在IO线程中执行的，也就是和channel读写是在一个线程中。
                //唤醒请求的线程（有可能已经被唤醒），告知连接已经断开
                //有三种可能会发生，1、没有成功得到请求结果触发事件，
                // 2、成功得到请求结果，但还没返回出去  3、成功得到请求结果，并且返回出去了
                //第一种情况又可以分为两种情况1、server端没执行2、server端成功执行了，在返回过程连接断了，这种情况在秒杀场景下就表现为少卖
                //第二种情况和第一种情况的第二种小情况一样。
                //第三种情况就是正常情况，出现问题一般会在主从复制上面，如果从还没复制全就选举为主了，秒杀场景就会出现超卖现象。
                //由于选举顺序是从节点加入顺序，并且从和从节点数据不一定完全一样的，所以当主挂了，选了个条数少的从，这个从升级为主服务了一段时间又挂了，再次选举另一个从，这样在秒杀场景还是会出现少卖现象。
                threadResultObj.setResult("close");
                hostAndPortListMap.remove(masterHostAndPort);
                masterChannelThreadResultMap.remove(channels[index]);
                channels[index] = null;
                LockSupport.unpark(threadResultObj.getThread());
                HostAndPort slave = slaves.get(0);
                Channel channel1 = null;
                synchronized (lock) {
                    while (true) {
                        try {
                            channel1 = bootstrap.connect(slave.host, slave.port).sync().channel();
                            threadResultObj.setThread(Thread.currentThread());
                            masterChannelThreadResultMap.put(channel1, threadResultObj);
                            channel1.writeAndFlush(CommandDTO.Command.newBuilder().setType("getMaster").build());
                            LockSupport.park();
                            String result = threadResultObj.getResult();
                            threadResultObj.setResult("close");
                            if (!"yes".equals(result)) {
                                masterChannelThreadResultMap.remove(channel1);
                                channel1.close().sync();
                                String[] hostPort = result.split(":");
                                int port = Integer.parseInt(hostPort[1]);
                                InetSocketAddress inetSocketAddress1 = (InetSocketAddress) ((ChannelFuture) future).channel().remoteAddress();
                                if (hostPort[0].equals(inetSocketAddress1.getHostString()) && port == inetSocketAddress1.getPort()) {
                                    Thread.sleep(1000);
                                    continue;
                                }
                                slave = new HostAndPort(hostPort[0], port);
                                channel1 = bootstrap.connect(hostPort[0], port).sync().channel();
                                masterChannelThreadResultMap.put(channel1, threadResultObj);
                            }
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                        slaves.remove(slave);
                        hostAndPortListMap.put(slave, slaves);
                        channels[index] = channel1;
                        lock.notifyAll();
                        break;
                    }
                }
                electionOnClose(channel1, index);
            });
        });
    }


    private Channel getChannelAndSetThread(String key) {
        Channel channel = IS_POWER_OF_TWO ? channels[HashUtil.sumChar(key) & N] : channels[HashUtil.sumChar(key) % N];
        if (channel == null) {
            try {
                this.wait();
                channel = IS_POWER_OF_TWO ? channels[HashUtil.sumChar(key) & N] : channels[HashUtil.sumChar(key) % N];
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        threadResultObj.setThread(Thread.currentThread());

        return channel;
    }


    @Override
    public String get(String key) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("get").setKey(key).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return get(key);
        }
        return result;
    }


    @Override
    public Long incr(String key) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("incr").setKey(key).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return incr(key);
        }
        try {
            return Long.parseLong(result);
        } catch (ClassCastException e) {
            throw new CacheDataException();
        }
    }

    @Override
    public Long decr(String key) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("decr").setKey(key).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return decr(key);
        }
        try {
            return Long.parseLong(result);
        } catch (ClassCastException e) {
            throw new CacheDataException();
        }
    }

    @Override
    public void hput(String key, Map<String, String> map) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("hput").setKey(key).setValue(SerialUtil.mapToString(map)).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            hput(key, map);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }

    @Override
    public void hmerge(String key, Map<String, String> map) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("hmerge").setKey(key).setValue(SerialUtil.mapToString(map)).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            hmerge(key, map);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }

    @Override
    public void lpush(String key, List<String> list) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("lpush").setKey(key).setValue(SerialUtil.collectionToString(list)).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            lpush(key, list);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }


    @Override
    public void sadd(String key, Set<String> set) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("sadd").setKey(key).setValue(SerialUtil.collectionToString(set)).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            sadd(key, set);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }

    @Override
    public void zadd(String key, Map<Double, String> zset) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("zadd").setKey(key).setValue(SerialUtil.mapWithDouToString(zset)).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            zadd(key, zset);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }

    @Override
    public void zadd(String key, Double score, String member) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("zadd").setKey(key).setValue(score + "©" + member).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            zadd(key, score, member);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }

    @Override
    public String put(String key, String value) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("put").setKey(key).setValue(value).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return put(key, value);
        }
        return threadResultObj.getResult();
    }


    @Override
    public void remove(String key) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("remove").setKey(key).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            remove(key);
        }
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("zrange").setKey(key).setValue(start + "©" + end).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return zrange(key, start, end);
        }
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        } else {
            return SerialUtil.stringToSet(threadResultObj.getResult());
        }
    }

    @Override
    public Long zrem(String key, String... member) {
        return null;
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        return null;
    }

    @Override
    public Long zrank(String key, String member) {
        return null;
    }

    @Override
    public Long zrevrank(String key, String member) {
        return null;
    }

    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        return null;
    }

    @Override
    public Long zcard(String key) {
        return null;
    }

    @Override
    public Double zscore(String key, String member) {
        return null;
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return null;
    }

    @Override
    public void hset(String key, String member, String value) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("hset").setKey(key).setValue(member + "©" + value).build());

            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            hset(key, member, value);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        }
    }

    @Override
    public String hget(String key, String field) throws CacheDataException {
        String result;
        synchronized (this) {

            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("hget").setKey(key).setValue(field).build());

            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return hget(key, field);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "null".equals(result) ? null : result;
        }
    }

    @Override
    public List<String> getList(String key) throws CacheDataException {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("getList").setKey(key).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return getList(key);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "null".equals(result) ? null : SerialUtil.stringToList(result);
        }
    }

    @Override
    public Set<String> getSet(String key) throws CacheDataException {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("getSet").setKey(key).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return getSet(key);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "null".equals(result) ? null : SerialUtil.stringToSet(result);
        }
    }

    @Override
    public boolean scontain(String key, String element) throws CacheDataException {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("scontain").setKey(key).setValue(element).build());
            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return scontain(key, element);
        }
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "true".equals(result);
        }
    }

    @Override
    public Long expire(String key, int seconds) {
        String result;
        synchronized (this) {
            getChannelAndSetThread(key).writeAndFlush(CommandDTO.Command.newBuilder().setType("expire").setKey(key).setValue(String.valueOf(seconds)).build());

            LockSupport.park();
            result = threadResultObj.getResult();
        }
        if ("close".equals(result)) {
            return expire(key, seconds);
        }
        return Long.parseLong(threadResultObj.getResult());
    }

    @Override
    public void close() throws Exception {
        for (int i = 0; i < channels.length; i++) {
            channels[i].close().sync();
            masterChannelThreadResultMap.remove(channels[i]);
        }
    }
}


