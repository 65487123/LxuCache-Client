package com.lzp.nettyhandler;

import com.lzp.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:58
 */
public class ClusterClientInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline().addLast(new IdleStateHandler(Integer.MAX_VALUE,12,Integer.MAX_VALUE))
                .addLast(new LzpMessageDecoder()).addLast(new LzpProtobufDecoder(ResponseDTO.Response.getDefaultInstance()))
                .addLast(new LzpMessageEncoder()).addLast(new LzpProtobufEncoder()).addLast(new ClusterClientHandler());
    }
}
