package cn.mw.monitor.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


/**
 * @author xhy
 * @date 2020/9/27 11:25
 */
@Slf4j
public class MessageEncoder  implements Encoder.Text<Object>{

    @Override
    public String encode(Object t) throws EncodeException {
        return JSON.toJSON(t).toString();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

        log.info("encode初始化init");
    }

    @Override
    public void destroy() {

        log.info("encode销毁destroy");
    }
}
