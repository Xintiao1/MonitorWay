package cn.mw.monitor.visualized.util;

import cn.mw.monitor.visualized.dto.MwPrometheusResult;
import cn.mw.monitor.visualized.dto.MwVisualizedPrometheusDropDto;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @ClassName
 * @Description 可视化工具类
 * @Author gengjb
 * @Date 2023/5/25 19:45
 * @Version 1.0
 **/
@Slf4j
public class MwVisualizedUtil {

    public static final String reg = "^(([0])|([1-9]+[0-9]*.{1}[0-9]+)|([0].{1}[1-9]+[0-9]*)|([1-9][0-9]*)|([0][.][0-9]+[1-9]+))$";

    public static final String scientificNotationRegex = "[-+]?[0-9]+(\\.[0-9]+)?[eE][-+]?[0-9]+";



    /**
     * 获取字符串是否是数字
     * @param value
     * @return
     */
    public static boolean checkStrIsNumber(String value){
        if(StringUtils.isBlank(value)){return false;}
        if(value.matches(reg) || value.matches(scientificNotationRegex)){
            return true;
        }
        return false;
    }

    /**
     * 获取Prometheus请求数据
     * @param mwVisualizedPrometheusDropDto
     * @return
     * @throws IOException
     */
    public static MwPrometheusResult getHttpPrometheusGet(MwVisualizedPrometheusDropDto mwVisualizedPrometheusDropDto) throws IOException {
        String respContent = "";
        log.info("可视化查询Prometheus地址"+mwVisualizedPrometheusDropDto.getUrl()+mwVisualizedPrometheusDropDto.getParam());
        String param = URLEncoder.encode(mwVisualizedPrometheusDropDto.getParam().replace("query=",""), "UTF-8");
        HttpGet httpGet = new HttpGet(mwVisualizedPrometheusDropDto.getUrl()+"?query="+param);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000)
                .setConnectionRequestTimeout(2000).setSocketTimeout(5000).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse resp = null;//执行时机
        resp = client.execute(httpGet);
        if (resp.getStatusLine().getStatusCode() == 200) {
            HttpEntity he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        }
        log.info("可视化查询Prometheus地址响应数据"+resp);
        if(StringUtils.isBlank(respContent)){return new MwPrometheusResult();}
        MwPrometheusResult mwPrometheusResult = JSON.parseObject(respContent, MwPrometheusResult.class);
        return mwPrometheusResult;
    }

    /**
     *json格式校验
     * @param jsonString json字符串
     * @return
     */
    public static boolean isValidJSON(String jsonString){
        try {
            new ObjectMapper().readTree(jsonString);
            return true;
        }catch (Throwable e){
            log.info("MwVisualizedUtil{} isValidJSON() jsonString::"+jsonString);
            return false;
        }
    }
}
