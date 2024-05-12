package cn.mw.plugin.extension.inner;

import cn.mw.plugin.annotation.ProxyMethod;
import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qiyao(1210)
 * @date 2022-04-06
 */
public class ProxyMethodInteceptorFactory {
    private static Map<String, ProxyMethodInteceptor> proxyMethodInteceptorMap = new HashMap<>();

    public static boolean addProxyMethod(String proxyId, Object instance, Method method) {
        if (proxyMethodInteceptorMap.containsKey(proxyId)) {
            ProxyMethodInteceptor proxyMethodInteceptor = proxyMethodInteceptorMap.get(proxyId);
            return proxyMethodInteceptor.addMethodProxy(instance, method);
        }
        return false;
    }

    public static void removeProxyMethod(String proxyId, Object instance, Method method) {
        if (proxyMethodInteceptorMap.containsKey(proxyId)) {
            proxyMethodInteceptorMap.get(proxyId).removeMethodProxy(instance, method);
        }
    }

    /**
     *
     * @param bean
     * @param proxyMethod
     * @param proxyId
     * @return
     */
    public static ProxyMethodInteceptor getCallBack(Object bean, Method proxyMethod, String proxyId) throws RuntimeException {
        if (proxyMethodInteceptorMap.containsKey(proxyId)) {
            throw new RuntimeException("proxyMethod id:[" + proxyId + "], dumplicated.");
        }
        ProxyMethodInteceptor proxyMethodInteceptor =  new ProxyMethodInteceptor(bean, proxyMethod, proxyId);
        proxyMethodInteceptorMap.put(proxyId, proxyMethodInteceptor);
        return proxyMethodInteceptor;
    }

    public static CallbackFilter createCallBackFilter(List<ProxyMethodInteceptor> inteceptors) {
        return new CallbackFilter() {
            @Override
            public int accept(Method method) {
                if (null != method.getAnnotation(ProxyMethod.class)) {
                    ProxyMethodInteceptor proxyMethodInteceptor =
                        proxyMethodInteceptorMap.get(method.getAnnotation(ProxyMethod.class).id());
                    if (null != proxyMethodInteceptor) {
                        for (int i = 0; i < inteceptors.size(); i++) {
                            if (inteceptors.get(i).equals(proxyMethodInteceptor)) {
                                return i;
                            }
                        }
                    }
                }
                return 0;
            }
        };
    }

    /**
     * 调用原始方法.
     * @param proxyId
     * @param params
     * @return
     * @throws RuntimeException
     */
    public static Object invoke(String proxyId, Object[] params) throws Throwable {
        ProxyMethodInteceptor proxyMethodInteceptor = proxyMethodInteceptorMap.get(proxyId);
        if (proxyMethodInteceptor == null) {
            throw new RuntimeException();
        }
        return proxyMethodInteceptor.invoke(params);
    }
}
