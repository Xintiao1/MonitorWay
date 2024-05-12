package cn.mw.monitor.websocket;

import io.swagger.models.auth.In;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author syt
 * @Date 2020/10/15 10:29
 * @Version 1.0
 */
@ServerEndpoint(value = "/mwapi/ws/myWaitingToDo/count/browse/{userId}/{count}", encoders = MessageEncoder.class)
@Component
@Slf4j
@Data
public class WebSocketGetCount {
    public Session session;
    public Integer userId;
    public Long count;
    public static CopyOnWriteArraySet<WebSocketGetCount> webSockets = new CopyOnWriteArraySet<>();
    public static Map<Integer, Session> sessionPool = new HashMap<Integer, Session>();
    private static final String WEBSOCKET_MESSAGE_COUNT = "websocket_messageCount";

    private static StringRedisTemplate redisTemplate;
    //了解保持活性的websocket是否在云心
    private static Integer websocket=0;

    //消息缓存
    public static Map<Integer, Object> messageSession = new HashMap<Integer, Object>();

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        WebSocketGetCount.redisTemplate = redisTemplate;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Integer userId, @PathParam(value = "count") Long count) {
        this.session = session;
        this.userId = userId;
        this.count = count;
        String key = WEBSOCKET_MESSAGE_COUNT + userId;
        redisTemplate.opsForValue().set(key, count.toString());
        webSockets.add(this);
        sessionPool.put(userId, session);
        connectionLive();
        log.info("【websocket消息】有新的连接，总数为:" + webSockets.size());
    }

    @OnClose
    public void onClose(CloseReason closeReason) {
        try {
            boolean remove = webSockets.remove(this);
            messageSession.remove(this.userId);
            Session remove1 = sessionPool.remove(this.userId);
        }catch (Exception e){
            log.info("【websocket消息】WebSocketGetCount连接断开，总数为:" + webSockets.size());
            log.info("sessionPool" + sessionPool.size());
        }
    }

    @OnMessage  //收到客户端之后调用的方法
    public void onMessage(String message) {
        log.info("【websocket消息】收到客户端消息:" + message);
    }

    @OnError
    public void OnError(Session session, Throwable throwable) {
        /*log.error("发生错误", throwable);*/
        log.info("websocket准备短连接");
    }

    // 此为广播消息
    public void sendAllMessage(String message) {
        for (WebSocketGetCount webSocket : webSockets) {
            log.info("【websocket消息】广播消息:" + message);
            try {
                webSocket.session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("fail to webSocket.session.getAsyncRemote().sendText() cause:{}", e.getMessage());
            }

        }
    }

    // 此为单点消息 (发送文本)
    public void sendTextMessage(Integer userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null) {
            try {
                log.info("发送之前");
                session.getBasicRemote().sendText(message);
                messageSession.put(userId,message);
                log.info("发送之后");

            } catch (Exception e) {
                log.error("错误返回 :{}",e);
            }
        }
    }

    //
    private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, Integer.MAX_VALUE, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    // 保持长连接 (发送对象)
    public void connectionLive() {
        try {
            if (websocket==0){
                websocket=1;
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(() -> {
                    for (Integer s : sessionPool.keySet()) {
                        log.info("正在连接用户【websocket】"+s);
                        if (messageSession.get(s) != null) {
                            sendObjMessage(s, messageSession.get(s));
                        } else {
                            sendObjMessage(s, "");
                        }
                    }
                }, 0, 30, TimeUnit.SECONDS);  // 每30秒发送一次心跳

            }
        }catch (Exception e){
            websocket=0;
        }

    }


    // 此为单点消息 (发送对象)
    public void sendObjMessage(Integer userId, Object message) {
        try {
            Runnable runnable = new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    Session session = sessionPool.get(userId);
                    if (session != null) {
                        messageSession.put(userId,message);
                        session.getBasicRemote().sendObject(message);
                    }
                }
            };
            executorService.execute(runnable);
        } catch (Exception e) {
            executorService.shutdownNow();
        }
    }
}
