package cn.mw.monitor.agent.impl;

import cn.mw.monitor.util.RSAUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import feign.gson.DoubleToIntMapTypeAdapter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.UTF8;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class DecryptGsonDecoder implements Decoder {
    private static final String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOCVZnruEhTReG6XS2FL49zAjGvLijTO7Fs+um92D88CQ9Cqh4DTSzTGwY+N+jHlJa4vR7CNsAzxG4A0PgOPpcqkrp9qQdp9AaP86pP2LRU1QNpdpYvSMb+mN3ScTJqSWgHt0vzJy7tbagWx9ajdJfoW4n+iftjRJpevfLXN8A9tAgMBAAECgYEA2sZvGF2GW6VarlDP664kAM3JfNDAh4TzlkdbEm1uJPhN253jdklev8DGmfNywNw6gp4oNgMwdssBQTijGJD8rMHELpuploumYBHbaj7heiib6M1ww16QEF7PUedodr39uPS3Wb8LULl15Fj8O1G1RW4adaqjeBKmHO5M8mXEecECQQD1+02vbifVC/U2vj2sMzScoxNIy0rN1zo7tDcWEoV41YAIO5uZiCPDZ4e8/Y/CUjPw5wQ/5ZwuR7OO+j3tj6oZAkEA6br+IczlcMWStDHi6Cz9ybJ8tVzQJBsze3e+vpKQc0wh5/bF9re8XpXjgAWoh/dgmqu99gvJ78OFb4FRkjAidQJBAO6lzN358n05J8Pf2HfcChw5/vit+zovqRjJpHQurf3orVnPcwwG0CPBqyjJnJL8K9Z6W14ex2MDP4rk7/YuXukCQQDe1538c/I9duHMU9PXMS4246nq8Lax9g07pouB/xMiGnApTSqpc7xxIc9p+/sWx1CfpybSM6MwqeXKzi0LiEERAkAbBAY4oQ0pEnZ8ofCwfG7D2Kh2DTqxNnXyBFWTDVfkJ+MLAXP+KUiFwo5B2lWXRWbiHR4vI97D3fda1RLeSRun";

    private final Gson gson;

    public DecryptGsonDecoder(Iterable<TypeAdapter<?>> adapters) {
        this(create(adapters));
    }

    public DecryptGsonDecoder() {
        this((Iterable) Collections.emptyList());
    }

    public DecryptGsonDecoder(Gson gson) {
        this.gson = gson;
    }

    public Object decode(Response response, Type type) throws IOException {
        if (response.status() == 404) {
            return Util.emptyValueOf(type);
        } else if (response.body() == null) {
            return null;
        } else {
            String data = response.body().toString();
            log.info("before:{}" ,data);
            byte[] decryptData = null;
            try {
                decryptData = RSAUtils.decryptDataBytes(data, privateKey);
            }catch (Exception e){
                log.error("DecryptGsonDecoder" ,e);
            }
            log.info("after:{}" ,new String(decryptData));
            Response.Builder builder = response.toBuilder().body(decryptData);
            Response newResponse = builder.build();
            Reader reader = newResponse.body().asReader();

            Object var4;
            try {
                var4 = this.gson.fromJson(reader, type);
            } catch (JsonIOException var8) {
                if (var8.getCause() != null && var8.getCause() instanceof IOException) {
                    throw (IOException)IOException.class.cast(var8.getCause());
                }

                throw var8;
            } finally {
                Util.ensureClosed(reader);
            }

            return var4;
        }
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
