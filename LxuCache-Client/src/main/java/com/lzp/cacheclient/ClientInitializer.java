package com.lzp.cacheclient;

import com.lzp.cacheclient.ClientHandler;
import com.lzp.protocol.LzpMessageDecoder;
import com.lzp.protocol.LzpMessageEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * @Author：luzeping
 * @Date: 2020/1/9 14:30
 */
public class ClientInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(new LzpMessageEncoder()).addLast(new LzpMessageDecoder()).addLast(new ClientHandler());

    }
}
