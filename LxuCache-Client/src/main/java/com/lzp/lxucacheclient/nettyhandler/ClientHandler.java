package com.lzp.lxucacheclient.nettyhandler;

import com.lzp.lxucacheclient.cacheclient.ThreadFactoryImpl;
import com.lzp.lxucacheclient.protocol.CommandDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Description:
 *
 * @author: Lu ZePing
 * @date: 2020/7/1 12:59
 */
public class ClientHandler extends SimpleChannelInboundHandler<byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private static ThreadPoolExecutor heartBeatThreadPool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("heartBeat"));

    static {
        heartBeatThreadPool.execute(ClientHandler::hearBeat);
    }

    public static class ThreadResultObj {
        private Thread thread;
        private String result;

        public ThreadResultObj(Thread thread, String result) {
            this.thread = thread;
            this.result = result;
        }

        public ThreadResultObj setThread(Thread thread) {
            this.thread = thread;
            return this;
        }

        public ThreadResultObj setResult(String result) {
            this.result = result;
            return this;
        }

        public Thread getThread() {
            return thread;
        }

        public String getResult() {
            return result;
        }
    }
    /**
     * key是client对应的channel，value是client对应的线程和当次操作的结果(如果有的话)
     **/
    public static Map<Channel, ThreadResultObj> channelResultMap = new ConcurrentHashMap<>(256);




    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channelResultMap.put(ctx.channel(),new ThreadResultObj(null,null));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        ThreadResultObj threadResultObj = channelResultMap.remove(channel);
        threadResultObj.setResult("close");
        LockSupport.unpark(threadResultObj.getThread());
        logger.info(channel.id() + "与服务端断开连接");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        ThreadResultObj threadResultObj = channelResultMap.get(ctx.channel());
        threadResultObj.result = new String(msg);
        LockSupport.unpark(threadResultObj.thread);
    }


    /**
     * Description ：每四秒发送一个心跳包
     **/
    private static void hearBeat() {
        while (true) {
            for (Channel channel : channelResultMap.keySet()) {
                channel.writeAndFlush(CommandDTO.Command.newBuilder().build());
            }
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
