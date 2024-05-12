package cn.mw.time;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.WeiXinSendUtil;
import cn.mw.monitor.weixin.entity.WeixinFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信发送实现类
 */
public class WxSendSysLogImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收人（多）
    private HashSet<String> touser;

    private static HashMap<String,String> contentmap = new HashMap<>();

    //发送方（微信）
    private WeixinFromEntity qyEntity;
    private Map<String, Object> map;
    public WxSendSysLogImpl(Map<String, Object> map, HashSet<Integer> userIds, String ruleId) {
        log.info("WxSendmessage start!");
        log.info("微信 map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.ruleId = ruleId;

    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("weixin satr sendMessage：" + sendMessage);
        Integer errcode = -1;
        String erroMessage = "";
        try {
            for (String touser : this.touser) {
                //获取token
                String token = WeiXinSendUtil.getAccessToken(qyEntity.getAgentId(),qyEntity.getSecret());
                WeiXinSendUtil.send(touser, contentmap,token);
            }
            errcode = 0;
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.info("error perform send message weixin:{}",e.getMessage());
        }

    }

    @Override
    public String dealMessage() {
        log.info("微信参数开始");
        contentmap.put("templateid",qyEntity.getAlertTempleate());
        contentmap.put("keyword1",map.get("hostName").toString());//资产名称
        contentmap.put("keyword2",map.get("@timestamp").toString());//日志时间
        contentmap.put("keyword3",map.get("severity_label").toString());//日志等级
        contentmap.put("keyword4",map.get("message").toString());//日志信息
        contentmap.put("keyword5",map.get("dataSourceName").toString());//数据源
        contentmap.put("remark","请运维人员相互告知!");
        contentmap.put("first","日志信息推送");
        /*contentmap.put("keyword1",map.get("主机名称"));
        contentmap.put("keyword2",map.get("告警时间"));
        contentmap.put("keyword3",map.get("告警等级"));
        contentmap.put("keyword4",map.get("告警信息"));
        contentmap.put("keyword5",map.get("当前状态"));
        contentmap.put("remark","请运维人员相互告知!");
        contentmap.put("first",map.get("告警标题"));*/
        log.info("微信告警标题结束");
            /*template_id = qyEntity.getAlertTempleate();
            keyword1 = map.get("主机名称");
            keyword2 = map.get("告警时间");
            keyword3 = map.get("告警等级");
            keyword4 = map.get("告警信息");
            keyword5 = map.get("当前状态");
            remark = "请运维人员相互告知！";
            first = map.get("告警标题");*/


        return super.dealMessage();
    }

    @Override
    public Object selectFrom(){
        WeixinFromEntity qyEntity = mwWeixinTemplateDao.findWeiXinFrom(ruleId);
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        if(StringUtils.isNotEmpty(qyEntity.getAgentId())){
            contentmap.put("appid",qyEntity.getAgentId());
            contentmap.put("secret",qyEntity.getSecret());
        }
        return qyEntity;
    }

    public  void decrypt(WeixinFromEntity weiXin) {
        log.info("WeixinFromEntity:" + weiXin.getSecret());
        if(weiXin != null){
            try {
                if (weiXin.getSecret() != null) {
                    weiXin.setSecret(EncryptsUtil.decrypt(weiXin.getSecret()));
                    weiXin.setAgentId(EncryptsUtil.decrypt(weiXin.getAgentId()));
                }
                log.info("解密完成");
            } catch (Exception e) {
                log.info("微信解密错误：" + e.getMessage());
            }
        }
    }

    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        try {
            //根据系统用户id,查询微信userId
            List<String> list = mwWeixinTemplateDao.selectWeixinUserId(userIds);
            log.info("wexin list" + list);
            HashSet<String> touser = (HashSet<String>) list.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
            //处理需要userIds,转换格式
            log.info("wexin touser" + touser);
            this.touser = touser;
            return touser;
        }catch (Exception e){
            log.info("perform select accept weixin:{}", e.getMessage());
            return null;
        }

    }

    @Override
    public Object call() throws Exception {
        synchronized (contentmap){
            try{

                //2:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("weixin userIds" + userIds);
                if(touser == null || touser.equals("")){
                    log.info("perform select accept weixin is null");
                    return null;
                }
                log.info("perform select accept weixin finish");

                //3:查询发送方
                selectFrom();
                log.info("perform select send weixin finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("微信 perform deal message:{}", "*****");

                //4发送微信消息
                sendMessage(sendMessage);
                log.info("weixin message send finish");
                return null;
            }catch (Exception e){
                log.info("weixin message send appear unknown error:{}",e.getMessage());
                throw new Exception(e.getMessage());
            }
        }
    }
}
