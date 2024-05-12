package cn.mw.monitor.weixin.util;


import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author bkc
 * @create 2020-06-30 23:55
 */
@Slf4j
public class LoadUtil {

    /**
     * 向指定的地址发送post请求
     */
    public static String post(String url, String data) {
        try {
            System.err.println("发送post请求的url:"+url);
            System.err.println("发送post请求的请求体:"+data);
            URL urlObj = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection)urlObj.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");

            // 要发送数据出去，必须要设置为可发送数据状态
            connection.setDoOutput(true);
            connection.setDoInput(true);
            // 获取输出流
            OutputStream os = connection.getOutputStream();
            // 写出数据
            os.write(data.getBytes("UTF-8"));
            os.flush();
            // 获取输入流
            InputStream is = connection.getInputStream();
            byte[] b = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = is.read(b)) != -1) {
                sb.append(new String(b, 0, len));
            }
            System.err.println("接受post请求的回复信息:"+sb.toString());

            os.close();
            is.close();
            return sb.toString();
        } catch (Exception e) {
            log.error(e.toString());
        }finally {

        }
        return null;
    }

    /**
     * 向指定的地址发送get请求
     *
     * @param urlstr
     */
    public static String get(String urlstr) {
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(urlstr);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

            httpUrlConn.setDoOutput(false);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            httpUrlConn.disconnect();
        } catch (Exception e) {
            log.error(e.toString());
        }
        return buffer.toString();
    }
}
