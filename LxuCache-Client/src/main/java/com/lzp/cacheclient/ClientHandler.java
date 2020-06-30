package com.lzp.cacheclient;

import com.lzp.protocol.ResponseDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author：luzeping
 * @Date: 2020/1/9 14:33
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    public static Map<Channel,Map<String,Object>> channelObjectMap = new ConcurrentHashMap<>(16);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ResponseDTO.Response response = (ResponseDTO.Response) msg;
        switch (response.getType()) {
            case "get":
                Map<String, Object> map = channelObjectMap.get(ctx.channel());
                Thread thread = (Thread) map.get("get");
                map.put("get", response.getResult());
                LockSupport.unpark(thread);
                break;
            case "put":
                Map<String, Object> map1 = channelObjectMap.get(ctx.channel());
                Thread thread1 = (Thread) map1.get("put");
                map1.put("put", response.getResult());
                LockSupport.unpark(thread1);
                break;
            case "remove":
                Map<String, Object> map2 = channelObjectMap.get(ctx.channel());
                Thread thread2 = (Thread) map2.get("remove");
                map2.put("remove", response.getResult());
                LockSupport.unpark(thread2);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + response.getType());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelObjectMap.remove(channel);
        logger.info(channel.id() + "与服务端断开连接");
    }
}
