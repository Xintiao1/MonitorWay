package cn.mw.monitor.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author lumingming
 * @createTime 20230411 9:39
 * @description
 */
@Slf4j
public class OkHttpUtil {

    public static OkHttpClient okHttpClient = null;

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    public static X509TrustManager getX509TrustManager() {
        X509TrustManager trustManager = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            trustManager = (X509TrustManager) trustManagers[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trustManager;
    }


    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null,  new TrustManager[] { new TrustAllCerts() }, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }


    private OkHttpUtil(){}
    private  static  final  OkHttpUtil single = new OkHttpUtil();

    public static  OkHttpUtil getInstance(){
        return single;
    }

    public Map<String,Object> header = new HashMap<>();
    public String Contype = "application/json";
    public String Content_type     ="Content-Type";
    public void setHeader(Map<String, Object> header) {
        this.header = header;
    }

    public void setContype(String contype) {
        Contype = contype;
    }

    public void setContent_type(String content_type) {
        Content_type = content_type;
    }

    public void resetHttp(){
        this.header = new HashMap<>();
        this.Contype = "application/json";
        this.Content_type = "Content-Type";
    }


    public OkHttpClient getClient(){
        if (okHttpClient==null){
            OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
            //信任所有证书
            mBuilder.sslSocketFactory(createSSLSocketFactory(),getX509TrustManager());
            mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
            OkHttpClient client = mBuilder
                    .callTimeout(2, TimeUnit.SECONDS)
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .writeTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(2, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .addInterceptor(new OkhttpInterceptor(2))
                    .build();
            okHttpClient = client;
            return client;
        }
        else {
            return okHttpClient;
        }
    }


    public Response DoPost(String url,String bodyString ){


        OkHttpClient client = getClient();
        MediaType mediaType = MediaType.parse(Contype);
        RequestBody body = RequestBody.create(mediaType,bodyString);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader(Content_type, Contype)
                .build();
        for (String key : header.keySet()){
            request = request.newBuilder().addHeader("st-auth-token",header.get(key).toString()).build();
        }
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            return null;
        }
        if (response.isSuccessful()){
            return response;
        }
        if (response.code()==400){
            return null;
        }
        if (response.code()==401){
            return null;
        }else {
            return null;
        }
    }

    public Response DoGet(String url){
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        for (String key : header.keySet()){
            request = request.newBuilder().addHeader("st-auth-token",header.get(key).toString()).build();
        }
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            return null;
        }
        if (response.isSuccessful()){
            return response;
        }
        if (response.code()==401){
            return null;
        }else {
            return null;
        }
    }


    public static class OkhttpInterceptor implements Interceptor {
        // 最大重试次数
        private int maxRentry;

        public OkhttpInterceptor(int maxRentry) {
            this.maxRentry = maxRentry;
        }


        @Override
        public Response intercept(Chain chain) throws IOException {
            /* 递归 2次下发请求，如果仍然失败 则返回 null ,但是 intercept must not return null.
             * 返回 null 会报 IllegalStateException 异常
             * */
            return retry(chain, 0);//这个递归真的很舒服
        }

        Response retry(Chain chain, int retryCent) {
            Request request = chain.request();
            Response response = null;
            try {
//                System.out.println("第" + (retryCent + 1) + "次执行发http请求.");
                response = chain.proceed(request);
            } catch (Exception e) {
                if (maxRentry > retryCent) {
                    return retry(chain, retryCent + 1);
                }
            } finally {
                return response;
            }
        }
    }

}
