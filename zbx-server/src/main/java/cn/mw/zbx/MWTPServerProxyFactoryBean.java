package cn.mw.zbx;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class MWTPServerProxyFactoryBean<T> implements FactoryBean<T> {

    private boolean debug = false;


    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public T getObject() throws Exception {
        Class[] interfaces = { MWTPServerAPI.class };
        MWTPServerProxy mwtpServerProxy = new MWTPServerProxy();
        mwtpServerProxy.setDebug(debug);
        MWTPServerAPI proxy = (MWTPServerAPI) Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, mwtpServerProxy);
        return (T)proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return MWTPServerAPI.class;
    }

}
