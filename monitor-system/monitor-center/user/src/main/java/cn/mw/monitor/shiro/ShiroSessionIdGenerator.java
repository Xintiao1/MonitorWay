package cn.mw.monitor.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;

import java.io.Serializable;
import java.util.UUID;

/**
 * @Author: bcb
 * @Date: 2020/5/18
 * @Version 1.0
 * @Discription 自定义SessionId生成器
 */
public class ShiroSessionIdGenerator implements SessionIdGenerator {
    @Override
    public Serializable generateId(Session session) {
        //可以使用更加复杂的,例如加解密算法等等算法
        return "mw" + UUID.randomUUID().toString().replace("-", "");
    }
}
