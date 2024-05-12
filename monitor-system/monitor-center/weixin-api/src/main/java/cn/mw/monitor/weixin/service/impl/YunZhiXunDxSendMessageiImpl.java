package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.AliyunDxSendUtil;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.weixin.entity.AliyunSmsFromEntity;
import cn.mw.monitor.weixin.entity.MwCaiZhengTingSMSFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**y
 * 阿里短信发送实现类
 */
public class YunZhiXunDxSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方手机号
    private HashSet<String> sendPhones;

    //发送方（云之讯短信）
    private MwCaiZhengTingSMSFromEntity qyEntity;

    private static Map<String, Object> contentmap = new HashMap<>();


    public YunZhiXunDxSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                   HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;

    }

    @Override
    public void sendMessage(String sendMessage){
        log.info("云之讯发送信息");
        int isSuccess = -1;
        String error = "";
        try {
            String url = qyEntity.getUrl().trim();
            if(url.endsWith("/")){
                url = url.substring(0,url.length()-1);
            }
            url = url + "/sms-partner/access/USERNAME/sendsms";
            url = url.replaceAll("USERNAME",qyEntity.getAccount());
            log.info("云之讯调用URL:" + url);
            Map<String,String> header = new HashMap<>();
            header.put("Accept", "application/json");
            String result = DingdingQunSendUtil.doPost(url,contentmap,header);
            if(result.contains("0")){
                isSuccess = 0;
            }
            log.info("云之讯发送结果：" + result);
        }catch (Exception e){
            error = e.getMessage();
            log.error("error perform send message 云之讯:",e);
        }finally {
            //保存记录
            saveHis("云之讯",sendMessage,isSuccess,map.get("事件ID"),error,this.title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    public String getSendTouer() {
        StringBuffer touser = new StringBuffer();
        if (sendPhones != null && sendPhones.size() > 0 && sendPhones.size() <= 1000) {
            Iterator iterator = sendPhones.iterator();
            while (iterator.hasNext()) {
                touser.append(",").append(iterator.next());
            }
        }else {
            return "";
        }
        touser.replace(0, 1, "");
        return touser.toString();
    }

    @Override
    public String dealMessage() {
        String msg = qyEntity.getSign() + super.dealMessage();
        contentmap.put("clientid", qyEntity.getAccount());
        contentmap.put("password",md5(qyEntity.getPassword()));
        String mobile = getSendTouer();
        log.info("云之讯手机号:" + mobile);
        contentmap.put("mobile",mobile);
        contentmap.put("smstype","0");
        contentmap.put("content",msg);
        return msg;
    }

    public String md5(String sign){
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] b = sign.getBytes("utf-8");
            md5.update(b);
            String result = "";
            byte[] temp = md5.digest();
            String s = "";
            for (byte bb : temp) {
                s += (bb + " ");
            }
            for (int i = 0; i < temp.length; i++) {
                result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
            }
            return result;
        } catch (Exception e) {
            log.error("财政厅MD5加密失败：" + e);
        }
        return "";
    }

    @Override
    public Object selectFrom(){
        MwCaiZhengTingSMSFromEntity qyEntity = mwWeixinTemplateDao.findCaiZhengTingSmsFrom(ruleId);
        this.qyEntity = qyEntity;
        return qyEntity;
    }


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            //根据系统用户id,查询手机号
            List<String> phones = mwWeixinTemplateDao.selectPhones(userIds);
            List<String> morePhones = mwWeixinTemplateDao.selectMorePhones(userIds);
            if(CollectionUtils.isNotEmpty(morePhones)){
                for(String s : morePhones){
                    String[] strs = s.split(",");
                    phones.addAll(Arrays.asList(strs));
                }
            }
            HashSet<String> sendPhones = (HashSet<String>) phones.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
            this.sendPhones = sendPhones;
            return sendPhones;
        }catch (Exception e){
            log.error("perform select accept 云之讯:", e);
            return null;
        }

    }

    @Override
    public Object call() throws Exception {
        try{
            //1判断级别是否符合
            if(!outPut()){
                return null;
            }
            log.info("the alert information level is satisfied");

            //2:根据系统用户id,查询接收人
            selectAccepts(userIds);
            if(sendPhones == null || sendPhones.size()==0){
                log.info("perform select phones:{}", sendPhones.size());
                return null;
            }
            log.info("perform select phones:{}", sendPhones.size());

            //3:查询发送方
            selectFrom();
            log.info("perform select send 云之讯 finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送企业微信消息
            log.info("云之讯 message send star");
            sendMessage(sendMessage);
            log.info("云之讯 message send finish");
            return null;
        }catch (Exception e){
            log.error("云之讯 message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }
}
