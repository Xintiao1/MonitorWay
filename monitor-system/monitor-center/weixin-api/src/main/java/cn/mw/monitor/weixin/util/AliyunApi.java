package cn.mw.monitor.weixin.util;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;


@Component
@Data
public class AliyunApi implements InitializingBean {


    @Value("${aliyunsms.signName}")
    private String signName;
    @Value("${aliyunsms.templateCode}")
    private String templateCode;
    @Value("${aliyunsms.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyunsms.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyunsms.dateName}")
    private String dateName;

    public String send( String phone, String content) throws UnsupportedEncodingException {
        DefaultProfile profile = DefaultProfile.getProfile("cn-qingdao", accessKeyId,accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", content);
        try {
            ////System.out.println(signName+"===="+templateCode);
            CommonResponse response = client.getCommonResponse(request);
            ////System.out.println(response.toString());
            return response.toString();
        } catch (Exception e) {
            ////System.out.println(e.toString());
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
