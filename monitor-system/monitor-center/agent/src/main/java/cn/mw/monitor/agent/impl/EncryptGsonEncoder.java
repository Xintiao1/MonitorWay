package cn.mw.monitor.agent.impl;

import cn.mw.monitor.util.RSAUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import feign.RequestTemplate;
import feign.Util;
import feign.codec.Encoder;
import feign.gson.DoubleToIntMapTypeAdapter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class EncryptGsonEncoder implements Encoder {
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAUT1gLv4cfXzOTQg9r039i5g50sIS8nyp/Ij33VhSP3eLIP7Ki6SIpqyzjEcSBNFlK8dZwaAbkoYquCRA6MynbZjg3OhY+FNNrlduQWvN5kAuHHbTBQ/Tf0OdUpZXwZL6R6FIaKGI+ylUWBLcE+MAJ1uBgTNApDPfMVGUNYVvzwIDAQAB";

    private final Gson gson;

    public EncryptGsonEncoder(Iterable<TypeAdapter<?>> adapters) {
        this(create(adapters));
    }

    public EncryptGsonEncoder() {
        this((Iterable) Collections.emptyList());
    }

    public EncryptGsonEncoder(Gson gson) {
        this.gson = gson;
    }

    public void encode(Object object, Type bodyType, RequestTemplate template) {
        String data = this.gson.toJson(object, bodyType);
        template.body(RSAUtils.encryptData(data ,publicKey));
    }

    static Gson create(Iterable<TypeAdapter<?>> adapters) {
        GsonBuilder builder = (new GsonBuilder()).setPrettyPrinting();
        builder.registerTypeAdapter((new TypeToken<Map<String, Object>>() {
        }).getType(), new DoubleToIntMapTypeAdapter());
        Iterator var2 = adapters.iterator();

        while(var2.hasNext()) {
            TypeAdapter<?> adapter = (TypeAdapter)var2.next();
            Type type = Util.resolveLastTypeParameter(adapter.getClass(), TypeAdapter.class);
            builder.registerTypeAdapter(type, adapter);
        }

        return builder.create();
    }
}
