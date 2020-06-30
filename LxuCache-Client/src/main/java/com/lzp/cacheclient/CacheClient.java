package com.lzp.cacheclient;

import com.lzp.protocol.CommandDTO;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/**
 * Description:缓存客户端.使用方法:new CacheClient(ip.port);然后就可以通过这个对象对服务端的缓存进行增删改查
 *
 * @author: Lu ZePing
 * @date: 2020/6/10 13:23
 */
public class CacheClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(CacheClient.class);

    private Channel channel;
    private static EventLoopGroup eventExecutors = new NioEventLoopGroup(1);
    private static Bootstrap bootstrap = new Bootstrap();

    static {
        bootstrap.group(eventExecutors).channel(NioSocketChannel.class).handler(new ClientInitializer());
    }

    public CacheClient(String ip, int port) {
        try {
            channel = bootstrap.connect(ip, port).sync().channel();
            //每个channel对应的map操作不会出现线程安全问题，所以用HashMap就行
            Map<String,Object> operResultMap = new HashMap<>();
            operResultMap.put("get",null);
            operResultMap.put("put",null);
            operResultMap.put("remove",null);
            operResultMap.put("getMaxMemorySize",null);
            ClientHandler.channelObjectMap.put(channel,operResultMap);
        } catch (InterruptedException e) {
            logger.error("initialize channel failed",e);
        }
    }


    public synchronized Object get(String key) {
        Map<String,Object> map = ClientHandler.channelObjectMap.get(this.channel);
        map.put("get",Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("get").setKey(key).setValue(null).build());
        LockSupport.park();
        return map.get("get");
    }

    public synchronized Object put(String key, String value) {
        Map<String, Object> map = ClientHandler.channelObjectMap.get(this.channel);
        map.put("put", Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("put").setKey(key).setValue(value).build());
        LockSupport.park();
        return map.get("put");
    }


    public synchronized Object remove(String key) {
        Map<String, Object> map = ClientHandler.channelObjectMap.get(this.channel);
        map.put("remove", Thread.currentThread());
        channel.writeAndFlush(CommandDTO.Command.newBuilder().setType("remove").setKey(key).setValue(null).build());

        LockSupport.park();
        return map.get("remove");
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

