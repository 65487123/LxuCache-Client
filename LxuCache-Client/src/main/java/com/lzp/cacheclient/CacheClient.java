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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

/**
 * Description:缓存客户端
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:57
 */
public class CacheClient implements Client {
    private static final Logger logger = LoggerFactory.getLogger(CacheClient.class);
    private static EventLoopGroup eventExecutors = new NioEventLoopGroup(1);
    private static Bootstrap bootstrap = new Bootstrap();
    private Channel channel;
    private ClientHandler.ThreadResultObj threadResultObj;

    static {
        bootstrap.group(eventExecutors).channel(NioSocketChannel.class).handler(new ClientInitializer());
    }

    public CacheClient(String ip, int port) throws InterruptedException {
        channel = bootstrap.connect(ip, port).sync().channel();
        threadResultObj = ClientHandler.channelResultMap.get(channel);
        channel.closeFuture().addListener(future -> {
            threadResultObj.setResult("e");
            LockSupport.unpark(threadResultObj.getThread());
        });
    }


    @Override
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
    public synchronized Set<String> zrange(String key, long start, long end) {
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
    public synchronized void hset(String key, String member, String value) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hset").setKey(key).setValue(member + "©" + value).build());
        LockSupport.park();
        if ("e".equals(threadResultObj.getResult())) {
            throw new CacheDataException();
        }
    }

    @Override
    public synchronized String hget(String key, String field) throws CacheDataException {
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
    public synchronized List<String> getList(String key) throws CacheDataException {
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
    public synchronized Set<String> getSet(String key) throws CacheDataException {
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
    public synchronized boolean scontain(String key, String element) throws CacheDataException {
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
    public synchronized Long expire(String key, int seconds) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("expire").setKey(key).setValue(String.valueOf(seconds)).build());
        LockSupport.park();
        return Long.parseLong(threadResultObj.getResult());
    }


    @Override
    public void close() throws Exception {
        channel.close().sync();
    }
}
