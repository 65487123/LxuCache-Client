package com.lzp.cacheclient;

import com.lzp.exception.MutiThreadOperCliException;
import com.lzp.nettyHandler.ClientHandler;
import com.lzp.nettyHandler.ClientInitializer;
import com.lzp.protocol.CommandDTO;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:57
 */
public class CacheClient implements AutoCloseable{
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


    public synchronized Object get(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("get").setKey(key).setValue(null).build());
        LockSupport.park();
        return threadResultObj.getResult();
    }

    public synchronized Object put(String key, String value) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("put").setKey(key).setValue(value).build());
        LockSupport.park();
        return threadResultObj.getResult();
    }


    public synchronized Object remove(String key) {
        threadResultObj.setThread(Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("remove").setKey(key).setValue(null).build());
        LockSupport.park();
        return threadResultObj.getResult();
    }

    ///暂时不要这个方法
    /*public synchronized int getMaxMemorySize() {
        Map<String, Object> map = ClientHandler.channelObjectMap.get(this.channel);
        map.put("getMaxMemorySize", Thread.currentThread());
        channel.writeAndFlush(new CommandDTO("getMaxMemorySize", null, null).toBytes());

        LockSupport.park();
        return (int) map.get("getMaxMemorySize");
    }*/


    @Override
    public void close() throws Exception {
        channel.close().sync();
    }
}
