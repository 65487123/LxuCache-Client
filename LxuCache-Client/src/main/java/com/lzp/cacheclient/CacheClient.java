package com.lzp.cacheclient;

import com.lzp.exception.CacheDataException;
import com.lzp.nettyhandler.ClientHandler;
import com.lzp.nettyhandler.ClientInitializer;
import com.lzp.protocol.CommandDTO;
import com.lzp.util.ValueUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.DataFormatException;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:57
 */
public class CacheClient implements Client {
    private static final Logger logger = LoggerFactory.getLogger(CacheClient.class);
    private static EventLoopGroup eventExecutors = new NioEventLoopGroup();
    private static Bootstrap bootstrap = new Bootstrap();
    private Channel channel;
    private ClientHandler.ThreadResultObj threadResultObj;

    static {
        bootstrap.group(eventExecutors).channel(NioSocketChannel.class).handler(new ClientInitializer());
    }

    public CacheClient(String ip, int port) {
        try {
            channel = bootstrap.connect(ip, port).sync().channel();
            threadResultObj = ClientHandler.channelResultMap.get(channel);
        } catch (InterruptedException e) {
            logger.error("initialize channel failed", e);
        }
    }


    @Override
    public synchronized String get(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("get").setKey(key).build());
        LockSupport.park();
        return threadResultObj.getResult();
    }


    @Override
    public Long incr(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("incr").setKey(key).build());
        LockSupport.park();
        return Long.parseLong(threadResultObj.getResult());
    }

    @Override
    public Long decr(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("decr").setKey(key).build());
        LockSupport.park();
        return Long.parseLong(threadResultObj.getResult());
    }

    @Override
    public void hput(String key, Map<String, String> map) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hput").setKey(key).setValue(ValueUtil.mapToString(map)).build());
        LockSupport.park();
    }

    @Override
    public void hmerge(String key, Map<String, String> map) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("hmerge").setKey(key).setValue(ValueUtil.mapToString(map)).build());
        LockSupport.park();
    }

    @Override
    public void lpush(String key, List<String> list) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("lpush").setKey(key).setValue(ValueUtil.collectionToString(list)).build());
        LockSupport.park();
    }


    @Override
    public void sadd(String key, Set<String> set) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("sadd").setKey(key).setValue(ValueUtil.collectionToString(set)).build());
        LockSupport.park();
    }

    @Override
    public void zadd(String key, Map<String, Double> zset) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("zadd").setKey(key).setValue(ValueUtil.mapWithDouToSimplifiedJson(zset)).build());
        LockSupport.park();
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
            return "null".equals(result) ? null : ValueUtil.stringToList(result);
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
            return "null".equals(result) ? null : ValueUtil.stringToSet(result);
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
    }
}
