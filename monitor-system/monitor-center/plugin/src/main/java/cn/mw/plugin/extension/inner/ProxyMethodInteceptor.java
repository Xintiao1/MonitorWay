package cn.mw.plugin.extension.inner;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiyao(1210)
 * @date 2022-04-06
 */
public class ProxyMethodInteceptor implements MethodInterceptor {
    private Object origin;
    private Object enhanceClass;
    private Method method;
    private String proxyId;

    private static final Logger logger = LoggerFactory.getLogger(ProxyMethodInteceptor.class);

    Map<Method, Object> agents = new HashMap<>();

    public ProxyMethodInteceptor(Object o, Method method, String proxyId) {
        origin = o;
        this.method = method;
        this.proxyId = proxyId;
    }

    public boolean addMethodProxy(Object o, Method m) {
        if (method.getParameterCount() != m.getParameterCount()) {
            return false;
        }
        if (!method.getReturnType().isAssignableFrom(m.getReturnType())) {
            return false;
        }
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (!method.getParameterTypes()[i].equals(m.getParameterTypes()[i])) {
                return false;
            }
        }
        agents.put(m, o);
        return true;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        if (!agents.isEmpty()) {
            if (this.method.equals(method)) {
                Map.Entry<Method, Object> methodObjectEntry = agents.entrySet().stream().findFirst().get();
                logger.debug("[plugin-inner-extension]proxy-id[{}]-method[{}] call agentMethod by class[{}]-method[{}]", proxyId,method.getName(),
                    methodObjectEntry.getValue().getClass().getName(),methodObjectEntry.getKey().getName());
                return methodObjectEntry.getKey().invoke(methodObjectEntry.getValue(), params);
            } else {
                logger.debug("[plugin-inner-extension] use enhanceClass method");
                return methodProxy.invokeSuper(enhanceClass, params);
            }
        }
        logger.debug("[plugin-inner-extension] use origin method");
        return methodProxy.invoke(origin, params);
    }

    public void removeMethodProxy(Object instance, Method method) {
        agents.remove(method);
    }

    public Object invoke(Object[] params) throws Throwable {
        return method.invoke(origin, params);
    }

    public void setEnhance(Object enhanceClazz) {
        this.enhanceClass = enhanceClazz;
    }
}
