package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.AliyunYuYinSendUtil;
import cn.mw.monitor.util.entity.AliYunYuYinlParam;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**y
 * 阿里语音短信发送实现类
 */
public class AliyunYuYinSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //发送方（阿里云配置）
    private AliYunYuYinlParam qyEntity;

    //发送的信息
    protected HashMap<String, String> map_a;

    //接收方手机号
    private HashSet<String> sendPhones;

    public AliyunYuYinSendMessageiImpl(HashMap<String, String> Hashmap, HashSet<Integer> userIds,
                                       HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) throws Exception {
        map_a = Hashmap;
        this.map = Hashmap;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = Hashmap.get("告警标题")==null? Hashmap.get("恢复标题")==null? null:false : true;
    }


    @Override
    public String dealMessage() {
        log.info("AliyunYuYin dealMessage star");
        log.info("AliyunYuYin map" + map_a);
        JSONObject jsonObject = new JSONObject();
        if(!isAlarm){
            return null;
        }
        String hostname = ipConvertCh(map_a.get(AlertEnum.HostNameZH.toString()));
        hostname = hostname.replaceAll("\\.",AlertEnum.DROPZH.toString());
        String info = ipConvertCh(map_a.get(AlertEnum.PROBLEMDETAILS.toString()));
        jsonObject.put(AlertEnum.INFO.toString(),info);
        jsonObject.put(AlertEnum.HOSTNAME.toString().toLowerCase(), hostname);
        /*if(qyEntity.getType()!=null && qyEntity.getType()==1){
            String faultTime = map_a.get(AlertEnum.ALERTTIME.toString());
            String address = ipConvertCh(map_a.get(AlertEnum.IPAddress.toString()));
            String renewStatus = AlertEnum.UNUSUAL.toString();
            jsonObject.put(AlertEnum.FAULTTIME.toString(),faultTime);
            jsonObject.put(AlertEnum.ADDRESS.toString(),address);
            jsonObject.put(AlertEnum.RENEWSTATUSEN.toString(),renewStatus);
        }*/
        log.info("AliyunYuYin dealMessage jsonObject.toJSONString()" + jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    public static String ipConvertCh(String msg){
        String ip = patternTool(msg);
        String[] ch = {AlertAssetsEnum.ZEROZH.toString(),AlertAssetsEnum.ONEZH.toString(),AlertAssetsEnum.TWOZH.toString(),AlertAssetsEnum.THREEZH.toString(),AlertAssetsEnum.FOURZH.toString(),AlertAssetsEnum.FIVEZH.toString(),AlertAssetsEnum.SIXZH.toString(),AlertAssetsEnum.SEVENZH.toString(),AlertAssetsEnum.EIGHTZH.toString(),AlertAssetsEnum.NINEZH.toString()};
        StringBuffer sb = new StringBuffer();
        if(ip != null){
            String[] ipSplit = ip.split("\\.");
            for(String s : ipSplit){
                for(char sChar : s.toCharArray()){
                    sb.append(ch[Integer.parseInt(String.valueOf(sChar))]);
                }
                sb.append(AlertEnum.DROPZH.toString());
            }
            msg = msg.replaceAll(ip,sb.toString());
        }
        return msg;
    }

    public static String patternTool(String str){
        String regular = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        Pattern pattern = Pattern.compile(regular);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            return matcher.group();
        }
        return null;
    }

    @Override
    public Object selectFrom(){
        AliYunYuYinlParam qyEntity = mwWeixinTemplateDao.findAliyunYuYinMessage(ruleId);
        this.qyEntity = qyEntity;
        return qyEntity;
    }

    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
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
    }

    @Override
    public void sendMessage(String sendMessage) {
        //发送信息
        String error = "";
        int isSuccess = -1;
        try{
            if(sendPhones != null && sendPhones.size()>0 ){
                for (String sendPhone : sendPhones) {
                    qyEntity.setCalledNumber(sendPhone);
                    error = AliyunYuYinSendUtil.sendQyWeixinMessage(qyEntity,sendMessage);
                    log.info("阿里语音短信发送结果：" + error);
                }
            }
            isSuccess = 0;
        }catch(Exception e){
            error = e.getMessage();
            log.error("error perform send message 阿里语音短信:{}",e);
        }finally {
            //保存记录
            saveHis("阿里语音",sendMessage,isSuccess,map_a.get("事件ID"),error,title,map_a.get("IP地址"),isAlarm, userIds, map_a.get(AlertEnum.HOSTID.toString()));
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

            //2根据系统用户id,查询接收人手机号
            selectAccepts(userIds);
            log.info("阿里语音短信 sendPhones" + sendPhones);

            if(sendPhones == null || sendPhones.size()==0){
                log.info("perform select phones:{}", sendPhones.size());
                return null;
            }
            log.info("perform select phones:{}", sendPhones.size());
            //3获取发送发
            selectFrom();
            log.info("perform deal selectFrom:{}", qyEntity);
            if(qyEntity == null || qyEntity.getAccessKeyId().equals("")){
                log.info("perform select send 阿里语音短信 is null");
                return null;
            }
            //4拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", sendMessage);
            //4发送短信
            if(isAlarm){
                sendMessage(sendMessage);
            }

            log.info("阿里语音短信 message send finish");
            return null;
        }catch (Exception e){
            log.error("阿里语音短信 message send appear unknown error:{}",e);
            throw new Exception(e);
        }
    }
}
