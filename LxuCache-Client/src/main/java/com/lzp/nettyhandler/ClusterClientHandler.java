package com.lzp.nettyhandler;

import com.lzp.cacheclient.CacheClusterClient;
import com.lzp.protocol.ResponseDTO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:59
 */
public class ClusterClientHandler extends SimpleChannelInboundHandler<ResponseDTO.Response> {
    private static final Logger logger = LoggerFactory.getLogger(ClusterClientHandler.class);
    public static class ThreadResultObj {
        private Thread thread;
        private String result;

        public ThreadResultObj(Thread thread, String result) {
            this.thread = thread;
            this.result = result;
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Thread getThread() {
            return thread;
        }

        public String getResult() {
            return result;
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseDTO.Response msg) throws Exception {
        ThreadResultObj threadResultObj = CacheClusterClient.masterChannelThreadResultMap.get(ctx.channel());
        threadResultObj.result = msg.getResult();
        LockSupport.unpark(threadResultObj.thread);
    }
}
