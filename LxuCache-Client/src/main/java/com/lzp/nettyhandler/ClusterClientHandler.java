package com.lzp.nettyhandler;

import com.lzp.cacheclient.CacheClusterClient;
import com.lzp.cacheclient.ThreadFactoryImpl;
import com.lzp.protocol.CommandDTO;
import com.lzp.protocol.ResponseDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.lzp.cacheclient.CacheClusterClient.masterChannelThreadResultMap;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:59
 */
public class ClusterClientHandler extends SimpleChannelInboundHandler<ResponseDTO.Response> {
    private static final Logger logger = LoggerFactory.getLogger(ClusterClientHandler.class);

    private static ThreadPoolExecutor heartBeatThreadPool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("heartBeat"));

    static {
        heartBeatThreadPool.execute(ClusterClientHandler::hearBeat);
    }

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
    protected void channelRead0(ChannelHandlerContext ctx, ResponseDTO.Response msg) {
        ThreadResultObj threadResultObj = masterChannelThreadResultMap.get(ctx.channel());
        threadResultObj.result = msg.getResult();
        LockSupport.unpark(threadResultObj.thread);
    }


    /**
     * Description ：每四秒发送一个心跳包
     **/
    private static void hearBeat() {
        while (true) {
            for (Channel channel : masterChannelThreadResultMap.keySet()) {
                channel.writeAndFlush(CommandDTO.Command.newBuilder().build());
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

}
