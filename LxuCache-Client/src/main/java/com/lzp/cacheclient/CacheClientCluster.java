package com.lzp.cacheclient;

import com.lzp.exception.CacheDataException;
import com.lzp.nettyhandler.ClientHandler;
import com.lzp.nettyhandler.ClientInitializer;
import com.lzp.protocol.CommandDTO;
import com.lzp.util.SerialUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;

import java.util.*;
import java.util.concurrent.locks.LockSupport;

/**
 * Description:集群版客户端
 *
 * @author: Lu ZePing
 * @date: 2020/8/5 17:46
 */
public class CacheClientCluster implements Client {

    private static final Logger logger = LoggerFactory.getLogger(CacheClientCluster.class);
    private static EventLoopGroup eventExecutors = new NioEventLoopGroup(1);
    private static Bootstrap bootstrap = new Bootstrap();
    private ClientHandler.ThreadResultObj threadResultObj;
    private Map<HostAndPort, List<HostAndPort>> hostAndPortListMap = new HashMap<>();
    private ChannelAndHostPort[] channelHostAndPortMap ;

    static {
        bootstrap.group(eventExecutors).channel(NioSocketChannel.class).handler(new ClientInitializer());
    }

    @Override
    public String put(String key, String value) {
        return null;
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public Long incr(String key) {
        return null;
    }

    @Override
    public Long decr(String key) {
        return null;
    }

    @Override
    public void hput(String key, Map<String, String> map) {

    }

    @Override
    public void hmerge(String key, Map<String, String> map) {

    }

    @Override
    public void lpush(String key, List<String> list) {

    }

    @Override
    public void sadd(String key, Set<String> set) {

    }

    @Override
    public void zadd(String key, Map<Double, String> set) {

    }

    @Override
    public void zadd(String key, Double score, String member) {

    }

    @Override
    public String hget(String key, String field) throws CacheDataException {
        return null;
    }

    @Override
    public List<String> getList(String key) throws CacheDataException {
        return null;
    }

    @Override
    public Set<String> getSet(String key) throws CacheDataException {
        return null;
    }

    @Override
    public boolean scontain(String key, String element) throws CacheDataException {
        return false;
    }

    @Override
    public Long expire(String key, int seconds) {
        return null;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        return null;
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

    }

    @Override
    public void close() throws Exception {

    }

    static class HostAndPort {
        public static final String LOCALHOST_STR = "localhost";

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

                String thisHost = convertHost(host);
                String hpHost = convertHost(hp.host);
                return port == hp.port && thisHost.equals(hpHost);

            }

            return false;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }

        private String convertHost(String host) {
            if (host.equals("127.0.0.1")) {
                return LOCALHOST_STR;
            } else if (host.equals("::1")) {
                return LOCALHOST_STR;
            }

            return host;
        }
    }

    private static class ChannelAndHostPort {
        Channel channel;
        HostAndPort hostAndPort;

        ChannelAndHostPort(Channel channel, HostAndPort hostAndPort) {
            this.channel = channel;
            this.hostAndPort = hostAndPort;
        }
    }

    public CacheClientCluster(List<HostAndPort> hostAndPorts) throws InterruptedException {
        List<ChannelAndHostPort> masters = new ArrayList<>();
        for (int i = hostAndPorts.size() - 1; i > -1; i--) {
            Channel channel;
            HostAndPort hostAndPort = hostAndPorts.get(i);
            channel = bootstrap.connect(hostAndPort.host, hostAndPort.port).sync().channel();
            threadResultObj.setThread(Thread.currentThread());
            channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("getMaster").build());
            LockSupport.park();
            String result = threadResultObj.getResult();
            if ("yes".equals(result)) {
                if (hostAndPortListMap.get(hostAndPort) == null) {
                    hostAndPortListMap.put(hostAndPort, new ArrayList<>());
                    masters.add(new ChannelAndHostPort(channel, hostAndPort));
                }
            } else {
                String[] hostPort = result.split(":");
                for (HostAndPort hostAndPort1 : hostAndPorts) {
                    if (hostPort[0].equals(hostAndPort1.host) && hostPort[1].equals(hostAndPort1.port)) {
                        List<HostAndPort> slaves;
                        if ((slaves = hostAndPortListMap.get(hostAndPort1)) == null) {
                            slaves = new ArrayList<>();
                            slaves.add(hostAndPort);
                            hostAndPortListMap.put(hostAndPort1, slaves);
                        } else {
                            slaves.add(hostAndPort);
                        }
                    }
                }
                channel.close().sync();
                hostAndPorts.remove(i);
            }
        }
        channelHostAndPortMap = (ChannelAndHostPort[]) masters.toArray();

        for (ChannelAndHostPort channelAndHostPort : channelHostAndPortMap) {
            channelAndHostPort.channel.closeFuture().addListener((GenericFutureListener) future -> {

            });
        }
    }




    /*@Override
    public synchronized String get(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("get").setKey(key).build());
        LockSupport.park();
        return threadResultObj.getResult();
    }


    @Override
    public synchronized Long incr(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("incr").setKey(key).build());
        LockSupport.park();
        try {
            return Long.parseLong(threadResultObj.getResult());
        } catch (ClassCastException e){
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized Long decr(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("decr").setKey(key).build());
        LockSupport.park();
        try {
            return Long.parseLong(threadResultObj.getResult());
        } catch (ClassCastException e) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized void hput(String key, Map<String, String> map) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hput").setKey(key).setValue(SerialUtil.mapToString(map)).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized void hmerge(String key, Map<String, String> map) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hmerge").setKey(key).setValue(SerialUtil.mapToString(map)).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized void lpush(String key, List<String> list) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("lpush").setKey(key).setValue(SerialUtil.collectionToString(list)).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }


    @Override
    public synchronized void sadd(String key, Set<String> set) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("sadd").setKey(key).setValue(SerialUtil.collectionToString(set)).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized void zadd(String key, Map<Double, String> zset) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("zadd").setKey(key).setValue(SerialUtil.mapWithDouToString(zset)).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized void zadd(String key, Double score, String member) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("zadd").setKey(key).setValue(score+"©"+member).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized String put(String key, String value) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("put").setKey(key).setValue(value).build());
        LockSupport.park();
        return threadResultObj.getResult();
    }


    @Override
    public synchronized void remove(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("remove").setKey(key).build());
        LockSupport.park();
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("zrange").setKey(key).setValue(start+"©"+end).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }else {
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
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hset").setKey(key).setValue(member + "©" + value).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public String hget(String key, String field) throws CacheDataException {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hget").setKey(key).setValue(field).build());
        LockSupport.park();
        String result = threadResultObj.getResult();
        if ("e".equals(result)){
            throw new CacheDataException();
        } else {
            return "null".equals(result)? null :result;
        }
    }

    @Override
    public List<String> getList(String key) throws CacheDataException {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("getList").setKey(key).build());
        LockSupport.park();
        String result = threadResultObj.getResult();
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "null".equals(result) ? null : SerialUtil.stringToList(result);
        }
    }

    @Override
    public Set<String> getSet(String key) throws CacheDataException {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("getSet").setKey(key).build());
        LockSupport.park();
        String result = threadResultObj.getResult();
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "null".equals(result) ? null : SerialUtil.stringToSet(result);
        }
    }

    @Override
    public boolean scontain(String key, String element) throws CacheDataException {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("scontain").setKey(key).setValue(element).build());
        LockSupport.park();
        String result = threadResultObj.getResult();
        if ("e".equals(result)) {
            throw new CacheDataException();
        } else {
            return "true".equals(result);
        }
    }

    @Override
    public Long expire(String key, int seconds) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("expire").setKey(key).setValue(String.valueOf(seconds)).build());
        LockSupport.park();
        return Long.parseLong(threadResultObj.getResult());
    }


    @Override
    public void close() throws Exception {
        channel.close().sync();
    }*/
}
