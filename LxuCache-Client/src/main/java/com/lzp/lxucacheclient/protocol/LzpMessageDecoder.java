package com.lzp.lxucacheclient.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LzpMessageDecoder extends ReplayingDecoder<Void> {
    private final boolean isServer;

    public LzpMessageDecoder(boolean isServer) {
        this.isServer = isServer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int length = byteBuf.readInt();
        if (length == 0) {
            if (isServer) {
                channelHandlerContext.channel().writeAndFlush(new byte[0]);
            }
            return;
        }
        byte[] content = new byte[length];
        byteBuf.readBytes(content);
        list.add(content);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        }
    }
}
