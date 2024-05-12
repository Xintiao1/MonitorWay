package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import cn.mw.monitor.weixin.entity.MwCaiZhengTingSMSFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业微信发送实现类
 */
public class CaiZhengTingSMSMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方手机号
    private HashSet<String> sendPhones;

    //发送方（财政厅短信）
    private MwCaiZhengTingSMSFromEntity qyEntity;


    public CaiZhengTingSMSMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
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
        log.info("财政厅发送信息");
        int isSuccess = -1;
        String error = "";
        try {
            sendMessage = sendMessage.replaceAll("\\[","【")
                    .replaceAll("]","】");
            String url = qyEntity.getUrl().trim();
            if(url.endsWith("/")){
                url = url.substring(0,url.length()-1);
            }

            String mobile = getSendTouer();
            log.info("财政厅手机号:" + mobile);
            if(qyEntity.getType().equals(1)){
                sendMessage = qyEntity.getSign() + sendMessage;
                String msg = URLEncoder.encode(sendMessage,"utf-8");
                url = url + "/yktsms/send?appid=APPID&mobile=MOBILE&msg=MSG&sign=SIGN";
                String sign = qyEntity.getAppID() + mobile + sendMessage + qyEntity.getAppKey();
                log.info("财政厅sign:" + sign);
                String signMd5 = md5(sign);
                url = url.replaceAll("APPID", qyEntity.getAppID())
                        .replaceAll("MOBILE",mobile).replaceAll("MSG",msg).replaceAll("SIGN",signMd5);
                log.info("财政厅调用URL:" + url);
                error = DingdingQunSendUtil.doPost(url,null,null);
                if(error.contains("0")){
                    isSuccess = 0;
                }
                log.info("财政厅翼讯通发送结果：" + error);
            }
            if(qyEntity.getType().equals(2)){
                sendMessage = "【四川财政】" + sendMessage;
                String msg = URLEncoder.encode(sendMessage ,"utf-8");
                url = url + "/smshttp?act=sendmsg&unitid=UNITID&username=USERNAME&passwd=PASSWD&msg=MSG&phone=PHONE&port=&sendtime=";
                log.info("财政厅内网加密前密码：" + qyEntity.getPassword());
                String passwdMd5 = md5(qyEntity.getPassword());
                log.info("财政厅内网加密后密码：" + passwdMd5);
                url = url.replaceAll("UNITID",Integer.toString(qyEntity.getId())).replaceAll("USERNAME",qyEntity.getAccount())
                        .replaceAll("PASSWD", passwdMd5).replaceAll("MSG",msg).replaceAll("PHONE",mobile);
                Map<String, String> headParam = new HashMap();
                headParam.put("Content-type", "application/json;charset=UTF-8");
                error = DingdingQunSendMessageiImpl.sendPost(url,null,headParam);
                if(error.contains("0")){
                    isSuccess = 0;
                }
                log.info("财政厅内网url：" + url);
                log.info("财政厅内网发送结果：" + error);
            }

        }catch (Exception e){
            error = e.getMessage();
            log.error("error perform send message 财政厅:{}",e);
        }finally {
            //保存记录
            saveHis("财政厅",sendMessage,isSuccess,map.get("事件ID"),error,this.title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }

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
        return super.dealMessage();
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
            //根据系统用户id,查询用户手机号
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
            log.error("perform select accept 财政厅:{}", e);
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
            log.info("perform select send 财政厅 finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送企业微信消息
            log.info("财政厅 message send star");
            sendMessage(sendMessage);
            log.info("财政厅 message send finish");
            return null;
        }catch (Exception e){
            log.error("财政厅 message send appear unknown error:{}",e);
            throw new Exception(e);
        }
    }
}
