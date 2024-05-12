package cn.mw.monitor.util;

import cn.mw.monitor.util.entity.AliYunYuYinlParam;
import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AliyunYuYinSendUtil {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");
    /**
     * @describe 发送阿里云txet消息
     */
    public static String sendQyWeixinMessage(AliYunYuYinlParam param, String jsonData) throws Exception {
        String result = null;
        log.info("阿里语音参数：" + param);
        Client client = createClient(param.getAccessKeyId(), param.getAccessKeySecret());
        SingleCallByTtsRequest singleCallByTtsRequest = new SingleCallByTtsRequest()
                .setCalledNumber(param.getCalledNumber())
                .setTtsCode(param.getTtsCode())
                .setTtsParam(jsonData);
        if(param.getType() != null && param.getType()==1){
            singleCallByTtsRequest.setCalledShowNumber(param.getCalledShowNumber());
        }
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            SingleCallByTtsResponse response = client.singleCallByTtsWithOptions(singleCallByTtsRequest, runtime);
            result = response.getBody().getMessage() + "-" + response.getBody().getRequestId();
        } catch (TeaException error) {
            result = error.message;
        } catch (Exception _error) {
            result = _error.getMessage();
        }
        return result;
    }
    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dyvmsapi.aliyuncs.com";
        return new com.aliyun.dyvmsapi20170525.Client(config);
    }


}
