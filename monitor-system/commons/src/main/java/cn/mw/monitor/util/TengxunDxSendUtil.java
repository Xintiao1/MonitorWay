package cn.mw.monitor.util;

import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.util.entity.TengXunSmsFromEntity;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TengxunDxSendUtil {

    public static String send(TengXunSmsFromEntity param, String[] msg, String[] phoneNumber, AlertRuleTableCommons alertRuleTable) {
        try{
            if(param == null){
                return "错误";
            }
            Credential cred = new Credential(param.getSecretId(), param.getSecretKey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            if(alertRuleTable!=null && alertRuleTable.getProxyState() != null && alertRuleTable.getProxyState()){
                httpProfile.setProxyHost(alertRuleTable.getProxyIp());
                httpProfile.setProxyPort(Integer.parseInt(alertRuleTable.getProxyPort()));
                httpProfile.setProxyPassword(alertRuleTable.getProxyPassword());
                httpProfile.setProxyUsername(alertRuleTable.getProxyAccount());
            }
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);
            SendSmsRequest req = new SendSmsRequest();
            req.setPhoneNumberSet(phoneNumber);
            req.setSmsSdkAppId(param.getAppId());
            req.setSignName(param.getSignName());
            req.setTemplateId(param.getTemplateId());
            req.setTemplateParamSet(msg);
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            SendStatus[] statuses = resp.getSendStatusSet();
            log.info("腾讯短信发送结果result :" + statuses);
            for(SendStatus s : statuses){
                String code = s.getCode();
                return code;
            }
            return null;
        }catch (Exception e){
            log.error("腾讯短信发送结果result :", e);
            return e.getMessage();
        }

    }

}
