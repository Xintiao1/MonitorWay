package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.weixin.huaxingwebservice.ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator;
import cn.mw.monitor.weixin.huaxingwebservice.RequestInfoService;
import cn.mw.monitor.weixin.service.SendMessageBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 企业微信发送实现类
 */
public class HuaXingWorkSystemSendMessageimpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private HashSet<Integer> userIds;
    private List<String> userName;
    private String URL = env.getProperty("huaxingitsm.url");

    public HuaXingWorkSystemSendMessageimpl(HashMap<String, String> map, HashSet<Integer> userIds,HashSet<String> severity) {
        this.map = map;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        this.userIds = userIds;
        this.severity = severity;
    }

    @Override
    public void sendMessage(String sendMessage){
        String result = null;
        String erroMessage = "";
        Integer errcode = -1;
        try {
            HashMap<String,String> params = new HashMap<>();
            String alerTime = map.get(AlertEnum.ALERTTIME.toString());
            params.put("title", sendMessage);//描述
            params.put("effectArea", "269");//267:公司268:生产厂269:产线270:个人
            params.put("emergencyDegree", "274");//274:一般275:低
            params.put("priority", "INCIDENT_PRIORITY_IV");//INCIDENT_PRIORITY_IV:四级
            params.put("occurTime", alerTime);//发生时间
            params.put("alertOwner", getUserName());//发给谁处理 请选告警收件人的第一个人，当前测试使用"chengbifan"//requestEng
            params.put("contactName", "统一监控平台");//联系人
            params.put("subWorkScop", "66");//事件来源ID
            params.put("contactNo", "P318087");//联系人工号
            params.put("contactPhone", "02765501750");//联系人电话 固定为“02765501750”
            params.put("contactEmail", "csot.t5cimalarm1@tcl.com");//联系人邮箱 固定为“csot.t5cimalarm1@tcl.com”
            params.put("sys_name", ""); //
            params.put("contactDept", "中小尺寸客户服务科");//申请人部门
            params.put("contact_name", "统一监控平台");//联系人
            params.put("applyReason", "测试"); //申请理由
            params.put("apply_type", "");//自定义
            params.put("serviceName","366");//自定义
            params.put("contact_code", "P318087");//联系人工号
            params.put("case_code", "819");//自定义
            params.put("alertRegister", "csot.t5cimalarm1");//工单发起人 固定为“csot.t5cimalarm1”
            params.put("ngeccCode", "1");//告警编号
            log.info("华星工单开始接口调用");
            ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator requestInfoServiceServiceLocator = new ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator();
            requestInfoServiceServiceLocator.setRequestInfoServiceEndpointAddress(URL);
            RequestInfoService requestInfoService = requestInfoServiceServiceLocator.getRequestInfoService();
            HashMap resultMap = requestInfoService.addIncidentInfo(params);
            log.info("华星工单结果记录：" + resultMap);
            if(resultMap.containsKey("RETURN") && resultMap.get("RETURN").equals("SUCCESS")){
                errcode = 0;
            }
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("工单系统错误:{}",e);
        }finally {
            saveHis("华星光电工单系统",sendMessage,errcode,map.get("事件ID"),erroMessage,map.get(AlertEnum.ALERTTITLE.toString()),map.get("IP地址"),isAlarm,userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {
        return super.dealMessage();
    }

    @Override
    public Object selectFrom(){
        return null;
    }


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        userName = mwWeixinTemplateDao.selectLoginName(userIds);
        return null;
    }

    @Override
    public Object call() throws Exception {
        try{
            if(!isAlarm){
                return null;
            }
            //1判断级别是否符合
            if(!outPut()){
                return null;
            }

            selectAccepts(userIds);
            log.info("perform deal selectAccepts");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            log.info("华星工单平台 message send star");
            sendMessage(sendMessage);
            log.info("华星工单平台 message send finish");
            return null;
        }catch (Exception e){
            log.error("工单平台 message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }

    public String getUserName(){
        String user = userName.get(0);
        if(user.indexOf("@") >= 0){
            user = user.substring(0,user.indexOf("@"));
        }
        return user;
    }

}
