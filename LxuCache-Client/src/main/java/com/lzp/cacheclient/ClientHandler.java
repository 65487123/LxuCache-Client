package com.lzp.cacheclient;

import com.lzp.protocol.ResponseDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author：luzeping
 * @Date: 2020/1/9 14:33
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    public static Cache<Channel,Map<String,Object>> channelObjectMap = new LruCache<>(Integer.MAX_VALUE);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.info(channel.id() + "与服务端建立连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ResponseDTO responseDTO = (ResponseDTO) msg;
        switch (responseDTO.getType()) {
            case "get":
                Map<String, Object> map = channelObjectMap.get(ctx.channel());
                Thread thread = (Thread) map.get("get");
                map.put("get", responseDTO.getResult());
                LockSupport.unpark(thread);
                break;
            case "put":
                Map<String, Object> map1 = channelObjectMap.get(ctx.channel());
                Thread thread1 = (Thread) map1.get("put");
                map1.put("put", responseDTO.getResult());
                LockSupport.unpark(thread1);
                break;
            case "remove":
                Map<String, Object> map2 = channelObjectMap.get(ctx.channel());
                Thread thread2 = (Thread) map2.get("remove");
                map2.put("remove", responseDTO.getResult());
                LockSupport.unpark(thread2);
                break;
            case "getMaxMemorySize":
                Map<String, Object> map3 = channelObjectMap.get(ctx.channel());
                Thread thread3 = (Thread) map3.get("getMaxMemorySize");
                map3.put("getMaxMemorySize", responseDTO.getResult());
                LockSupport.unpark(thread3);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + responseDTO.getType());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.info(channel.id() + "与服务端断开连接");
    }
}
