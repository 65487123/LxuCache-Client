package com.lzp.cacheclient;

import com.lzp.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;


/**
 * @Author：luzeping
 * @Date: 2020/1/9 14:30
 */
public class ClientInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline().addLast(new LzpMessageDecoder()).addLast(new LzpProtobufDecoder(ResponseDTO.Response.getDefaultInstance()))
                .addLast(new LzpMessageEncoder()).addLast(new LzpProtobufEncoder()).addLast(new ClientHandler());
    }
}
