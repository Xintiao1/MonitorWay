package cn.mw.monitor.screen.service;

import cn.mw.monitor.websocket.MessageEncoder;
import cn.mw.monitor.screen.service.modelImpl.BaseModel;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.IUserControllerLogin;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
@ServerEndpoint(value = "/mwapi/ws/screen/popup/browse/{modelDataId}/{userId}", encoders = MessageEncoder.class)
//此注解相当于设置访问URL
@Data
public class WebSocket  {

    public Session session;
    public String modelDataId;

    private String userId;

    public static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();
    public static Map<String, Session> sessionPool = new ConcurrentHashMap<String, Session>();
    public static Map<String, Session> yuyinBobaoSessionPool = new ConcurrentHashMap<String, Session>();


    @OnOpen
    public void onOpen(Session session, @PathParam(value = "modelDataId") String modelDataId, @PathParam(value = "userId") String userId) {
        this.session = session;
        this.modelDataId = modelDataId;
        this.userId = userId;
        webSockets.add(this);
        sessionPool.put(userId + modelDataId, session);
        if(modelDataId.equals("alertwebscoket")){
            session.setMaxIdleTimeout(2 * 60 * 60 * 1000);
            yuyinBobaoSessionPool.put(userId + modelDataId, session);
        }
        log.info("yuzhi open "+WebSocket.sessionPool.get(userId + modelDataId));
        log.info("【websocket消息】: userId:" + userId);
        log.info("【websocket消息】: modelDataId:" + modelDataId);
        log.info("【websocket消息】有新的连接，总数为:" + webSockets.size());
        log.info("【websocket消息】线程总数为:" + BaseModel.threadHashMap.size());
    }

    @OnClose
    public void onClose(CloseReason closeReason) {
        log.info("yuzhi close "+WebSocket.sessionPool.get(userId + modelDataId));
        log.info("【websocket消息】线程总数为:" + BaseModel.threadHashMap.size());
        log.info("【websocket消息】连接断开，总数为:" + webSockets.size());
        boolean remove = webSockets.remove(this);
        Session remove1 = sessionPool.remove(this.userId + this.modelDataId);
        log.info("【websocket消息】连接断开，总数为:" + webSockets.size());
        log.info("sessionPool" + sessionPool.size());
        Thread thread = BaseModel.threadHashMap.get(this.userId + this.modelDataId);
        if (null != thread) {
            thread.interrupt();
            BaseModel.threadHashMap.remove(this.userId + this.modelDataId);
        }


    }

    @OnMessage  //收到客户端之后调用的方法
    public void onMessage(String message) {
        log.info("【websocket消息】收到客户端消息:" + message);
        if(message.equals("close")){
            yuyinBobaoSessionPool.remove(this.userId + this.modelDataId);
        }
    }

    @OnError
    public void OnError(Session session, Throwable throwable) {
        log.error("发生错误{}", throwable);
        log.info("yuzhi socket关闭发生错误");
    }

    // 此为广播消息
    public void sendAllMessage(String message) {
        for (WebSocket webSocket : webSockets) {
            log.info("【websocket消息】广播消息:" + message);
            try {
                webSocket.session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("fail to sendAllMessage with param={}, cause:{}",message, e);
            }
        }
    }

    // 此为单点消息 (发送文本)
    public void sendTextMessage(String modelDataId, String message) {
        Session session = sessionPool.get(modelDataId);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("fail to sendTextMessage with param={}, cause:{}",message, e);
            }
        }
    }

    //
/*    private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(30, 50, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    static {
        executorService.allowCoreThreadTimeOut(true);
    }


    // 此为单点消息 (发送对象)
    public void sendObjMessage(String modelDataId, Object message) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Session session = sessionPool.get(modelDataId);
                    if (session != null) {
                        log.info("发送之前");
                        session.getAsyncRemote().sendObject(message);
                        log.info("发送之后");
                    }
                } catch (Exception e) {
                    log.error("sendObjMessage" , e);
                }

            }
        };
        executorService.execute(runnable);

    }*/

    // 此为单点消息 (发送对象)
    public void sendObjMessage(Integer userId, String modelDataId, Object message) {
        try {
            log.info("yuzhi sendObjMessage sessionPool.get(userId + modelDataId)");
            Session session = sessionPool.get(userId + modelDataId);
            log.info("session:" + session);
            if (session != null) {
                log.info("发送之前");
                session.getAsyncRemote().sendObject(message);
                log.info("发送之后");
            }
        } catch (Exception e) {
            log.error("sendObjMessage", e);
        }


    }

    public void yuyinBobao(Integer userId, String modelDataId, Object message) {
        try {
            log.info("yuzhi sendObjMessage sessionPool.get(userId + modelDataId)");
            Session session = yuyinBobaoSessionPool.get(userId + modelDataId);
            log.info("session超时时间:" + session.getMaxIdleTimeout());
            log.info("session:" + session);
            if (session != null) {
                log.info("发送之前");
                session.getAsyncRemote().sendObject(message);
                log.info("发送之后");
            }
        } catch (Exception e) {
            log.error("sendObjMessage", e);
        }


    }



//    @Override
//    public List<Reply> handleEvent(Event event) throws Throwable {
//        if (event instanceof LogoutEvent) {
//            LogoutEvent logoutEvent = (LogoutEvent) event;
//            //中断线程池中的线程
//        }
//        return null;
//    }

    //登录事件处理器
    @Autowired
    private List<IUserControllerLogin> loginProcessers;

    //登录上下文信息获取接口
    @Autowired
    private ILoginCacheInfo loginCacheInfoInfo;



}



