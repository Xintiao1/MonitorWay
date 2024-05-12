package cn.mw.zbx;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MWRequestBuilder {

    private static final AtomicInteger nextId = new AtomicInteger(1);

    private MWRequestAbstract request;

    private MWRequestBuilder() {

    }

    static public MWRequestBuilder newBuilder() {
        return new MWRequestBuilder();
    }

    public MWRequestAbstract build() {
        if (request.getId() == null) {
            request.setId(nextId.getAndIncrement());
        }
        return request;
    }

    public MWRequestBuilder version(String version) {
        request.setJsonrpc(version);
        return this;
    }

    public MWRequestBuilder initRequest(Object param) {
        if (param instanceof Map) {
            request = new MWMapRequest();
        } else if (param instanceof List) {
            request = new MWListRequest();
        } else {
            request = new MWMapRequest();
        }
        request.setParams(param);
        return this;
    }

    public MWRequestBuilder auth(String auth) {
        request.setAuth(auth);
        return this;
    }

    public MWRequestBuilder method(String method) {
        request.setMethod(method);
        return this;
    }

    public MWRequestBuilder id(Integer id) {
        request.setId(id);
        return this;
    }

}
