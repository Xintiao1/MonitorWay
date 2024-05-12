package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.model.param.MwModelInterfaceCommonParam;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.util.TCP_UDPSendUtil;
import cn.mw.monitor.util.entity.TCP_UDPFrom;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 广州银行短信发送实现类
 */
public class GuangZhouBankMessageiImpl extends SendMessageBase {

    private static final String METHOD = "广州银行";

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private TCP_UDPFrom from;

    protected MWNetWorkLinkCommonService mWNetWorkLinkCommonService;

    private MwModelCommonService mwModelCommonService;

    private MWAlertAssetsDao assetsDao;

    //接收方手机号
    private HashSet<String> sendPhones;

    public GuangZhouBankMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                     HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get(AlertEnum.ALERTTITLE.toString())==null? map.get(AlertEnum.RECOVERYTITLE.toString())==null? null:false : true;
        MWNetWorkLinkCommonService mWNetWorkLinkCommonService = ApplicationContextProvider.getBean(MWNetWorkLinkCommonService.class);
        this.mWNetWorkLinkCommonService = mWNetWorkLinkCommonService;
        MwModelCommonService mwModelCommonService = ApplicationContextProvider.getBean(MwModelCommonService.class);
        this.mwModelCommonService = mwModelCommonService;
        MWAlertAssetsDao assetsDao= ApplicationContextProvider.getBean(MWAlertAssetsDao.class);
        this.assetsDao = assetsDao;

    }

    @Override
    public void sendMessage(String sendMessage) throws MalformedURLException {
        log.info("广州银行开始发送");
        int isSuccess = -1;
        String error = "";
        try {
            log.info("广州银行接收人sendPhones：" + sendPhones);
            for(String s : sendPhones){
                String msg = s + sendMessage;
                log.info("广州银行接收人：" + msg);
                log.info("广州银行接收人from：" + from);
                isSuccess = TCP_UDPSendUtil.UDPSend(from.getHost(), from.getPort(), msg);
                log.info("广州银行发送结果：" + isSuccess);
            }

        }catch (Exception e){
            error = e.getMessage();
            log.error("error perform send message 广州银行发送错误：" + e);
        }finally {
            //保存记录
            saveHis(METHOD,sendMessage,isSuccess,map.get(AlertEnum.EVENTID.toString()),error,this.title,map.get(AlertEnum.IPAddress.toString()),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }


    @Override
    public String dealMessage() {
        String title = null;
        String suffix = null;
        log.info("广州银行数据格式处理开始");
        String timeType = "";
        String failureTime = "";
        StringBuffer content = new StringBuffer();
        MwModelInterfaceCommonParam interfaceCommonParam = new MwModelInterfaceCommonParam();
        interfaceCommonParam.setHostIp(map.get(AlertEnum.HOSTIP.toString()));
        interfaceCommonParam.setHostId(map.get(AlertEnum.HOSTID.toString()));
        List<MwModelInterfaceCommonParam> interfaceCommonParams = mwModelCommonService.queryInterfaceInfoAlertTag(interfaceCommonParam);
        String interfaceModeDesc = null;
        log.info("广州银行告警信息打印:" + map);
        title = isAlarm ? map.get(AlertEnum.ALERTTITLE.toString()) : map.get(AlertEnum.RECOVERYTITLE.toString());
        if(CollectionUtils.isNotEmpty(interfaceCommonParams)){
            for(MwModelInterfaceCommonParam temp : interfaceCommonParams){
                log.info("接口描述temp111：" + temp+";alert title::"+title);
                if(temp.getInterfaceName() != null && title.contains(temp.getInterfaceName() + "-") && temp.getInterfaceDesc() != null){
                    log.info("接口描述temp222：" + temp);
                    interfaceModeDesc = temp.getInterfaceDesc();
                    break;
                }
            }
        }
        if(isAlarm){
            suffix = AlertEnum.ALERT.toString();
            timeType = AlertEnum.ALERTTIME.toString();
            failureTime = AlertEnum.ALERTTIME.toString();
            if(map.get(AlertEnum.DefaultSelection.toString()).equals(AlertEnum.Default.toString())){
                content.append("【告警】").append(AlertEnum.ALERTTITLE.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTITLE.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Domain.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.HostNameZH.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.HostNameZH.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.SystemInfo.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.SystemInfo.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
                if(interfaceModeDesc != null){
                    content.append(AlertEnum.InterfaceModeDesc.toString() + AlertAssetsEnum.COLON.toString()).append(interfaceModeDesc).append(AlertAssetsEnum.Comma.toString()).append('\n');
                }
                content.append(AlertEnum.ALERTLEVEL.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTLEVEL.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.PROBLEMDETAILS.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.PROBLEMDETAILS.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.NOWSTATE.toString() + AlertAssetsEnum.COLON.toString()).append(AlertEnum.UNUSUAL.toString()).append(AlertAssetsEnum.Comma.toString()).append('\n');
            }

        }else{
            suffix = AlertEnum.RECOVERY.toString();
            timeType = AlertEnum.RECOVERYTIME.toString();
            failureTime = AlertEnum.FAILURETIME.toString();
            if(map.get(AlertEnum.DefaultSelection.toString()).equals(AlertEnum.Default.toString())){
                content.append("【恢复】").append(AlertEnum.RECOVERYTITLE.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTITLE.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Domain.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.HostNameZH.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.HostNameZH.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.SystemInfo.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.SystemInfo.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
                if(interfaceModeDesc != null){
                    content.append(AlertEnum.InterfaceModeDesc.toString() + AlertAssetsEnum.COLON.toString()).append(interfaceModeDesc).append(AlertAssetsEnum.Comma.toString()).append('\n');
                }
                content.append(AlertEnum.RECOVERYLEVEL.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYLEVEL.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.FAILURETIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.FAILURETIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYDETAILS.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYDETAILS.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RENEWSTATUS.toString() + AlertAssetsEnum.COLON.toString()).append(AlertEnum.NORMAL.toString()).append(AlertAssetsEnum.Comma.toString()).append('\n');
            }
        }

        log.info("广州银行-title:" + title);
        String associatedLink = getAssociatedAlarmLink(assets.getAssetsId(),assets.getMonitorServerId(),assets.getInBandIp());
        log.info("广州银行-关联模块:" + associatedLink);
        String linkTargetIp = map.get(AlertEnum.IPAddress.toString());
        if((title.contains(AlertEnum.LINK.toString()) || title.contains(AlertEnum.ICMP.toString())) && associatedLink != null && !associatedLink.equals("") && associatedLink.contains(AlertEnum.LINKNAME.toString())) {
            if(title.contains(AlertEnum.LINK.toString())){
                linkTargetIp = title.substring(title.indexOf(AlertAssetsEnum.RightBracket.toString() + AlertAssetsEnum.LeftBracket.toString())+2,title.lastIndexOf(AlertAssetsEnum.RightBracket.toString()));
            }
            List<String> linkNames = new ArrayList<>();
            String associatedModule = associatedLink.substring(associatedLink.indexOf(AlertEnum.LINKNAME.toString() + AlertAssetsEnum.COLON.toString() + AlertAssetsEnum.LeftBracket.toString())+6, associatedLink.lastIndexOf(AlertAssetsEnum.RightBracket.toString()));
            if(associatedModule.contains(AlertAssetsEnum.ELLIPSIS.toString())){
                associatedModule = associatedModule.substring(0,associatedModule.lastIndexOf(AlertAssetsEnum.Comma.toString() + AlertAssetsEnum.ELLIPSIS.toString()));
            }
            linkNames = Arrays.asList(associatedModule.split(","));
            List<AddAndUpdateParam> links = new ArrayList<>();
            AddAndUpdateParam param = new AddAndUpdateParam();
            param.setLinkNames(linkNames);
            param.setLinkTargetIp(linkTargetIp);
            log.info("广州银行查询条件:" + param);
            links = mWNetWorkLinkCommonService.getLinkByAssetsIdAndIp(param);
            log.info("广州银行线路查询总数:" + links.size());
            if(CollectionUtils.isNotEmpty(links)){
                AddAndUpdateParam link = links.get(0);
                String port = "";
                String ip = "";
                String time =  map.get(timeType);
                if(link.getValuePort().equals(AlertEnum.ROOT.toString().toUpperCase())){
                    port = link.getRootPort();
                    ip = link.getRootIpAddress();
                }else {
                    port = link.getTargetPort();
                    ip = link.getTargetIpAddress();
                }
                //取运营商、线路类型、线路编号
                List<MwAssetsLabelDTO> assetsLabels = link.getAssetsLabel();
                String domain = getLaeblValue(assetsLabels,AlertEnum.Domain.toString());
                StringBuffer sb = new StringBuffer();
                sb.append(AlertAssetsEnum.LeftBracketZH.toString()).append(link.getScanType()).append(suffix).append(AlertAssetsEnum.RightBracketZH.toString()).append(" ")
                        .append(link.getLinkName()).append(AlertAssetsEnum.Dash.toString()).append(domain).append(AlertAssetsEnum.Dash.toString());
                if(isAlarm){
                    sb.append(suffix).append(AlertAssetsEnum.Dash.toString()).append(AlertEnum.LINKDISCONNECTION.toString());
                }else {
                    sb.append(AlertEnum.LINKRECOVERY.toString());
                }
                sb.append('\n').append(AlertAssetsEnum.LEFTPARENTHESES.toString()).append(port).append(AlertAssetsEnum.LEFTPARENTHESES.toString()).append(ip).append(AlertAssetsEnum.RIGETPARENTHESES.toString()).append(AlertAssetsEnum.RIGETPARENTHESES.toString()).append(AlertAssetsEnum.Comma.toString());
                if(CollectionUtils.isNotEmpty(assetsLabels)){
                    for(MwAssetsLabelDTO dto : assetsLabels){
                        String labelValue = "";
                        if(dto.getLabelName().equals(AlertEnum.Domain.toString())) continue;
                        if (dto.getInputFormat().equals(AlertAssetsEnum.One.toString())) {
                            labelValue = dto.getTagboard();
                        }else if (dto.getInputFormat().equals(AlertAssetsEnum.Three.toString())) {
                            labelValue = dto.getDropValue();

                        }
                        if(!labelValue.equals("")){
                            sb.append(dto.getLabelName()).append(AlertAssetsEnum.COLON.toString()).append(labelValue).append(AlertAssetsEnum.Comma.toString());
                        }

                    }
                }

                if(!isAlarm){
                    sb.append(AlertEnum.ALERTHAPPEN.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.FAILURETIME.toString())).append('\n');
                }
                sb.append(timeType).append(AlertAssetsEnum.COLON.toString()).append(time);
                sb.append("【猫维管理平台】");
                return sb.toString();
            }else if(title.contains(AlertEnum.LINK.toString())){
                return null;
            }

        }
        content.append("【猫维管理平台】");
        return content.toString();
    }
    public String getLaeblValue(List<MwAssetsLabelDTO> assetsLabels,String condition){
        log.info("广州银行标签数据：" + assetsLabels);
        log.info("广州银行condition数据：" + condition);
        String labelValue = "";
        if(CollectionUtils.isNotEmpty(assetsLabels)){
            for(MwAssetsLabelDTO label : assetsLabels){
                if(label.getLabelName().equals(condition)){
                    if (label.getInputFormat().equals(AlertAssetsEnum.One.toString())) {
                        labelValue = label.getTagboard();
                        break;
                    }
                    if (label.getInputFormat().equals(AlertAssetsEnum.Three.toString())) {
                        labelValue = label.getDropValue();
                        break;
                    }
                }
            }
        }
        return labelValue;
    }


    public String getAssociatedAlarmLink(String hostId,Integer monitorServerId,String hostIp) {
        StringBuffer sb = new StringBuffer();
        synchronized (sb){
            List<Map> linkmap = assetsDao.getLink(hostId, monitorServerId,hostIp);
            log.info("关联线路名称：" + linkmap);
            log.info("关联线路名称getAssetsId：" + hostId);
            log.info("关联线路名称getMonitorServerId：" + monitorServerId);
            if (null != linkmap && linkmap.size() > 0) {
                sb.append("关联线路名称:[");
                for (int i = 0; i < linkmap.size(); i++) {
                    sb.append(linkmap.get(i).get("linkName").toString()).append(",");
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }

    @Override
    public Object selectFrom(){
        TCP_UDPFrom from = mwWeixinTemplateDao.findTCPFrom(ruleId);
        this.from = from;
        return from;
    }
    /*public  void decrypt(GeneralMessageEntity applyWeiXin) {
        if(applyWeiXin != null){
            try {
                if (applyWeiXin.getSecret() != null) {
                    applyWeiXin.setSecret(EncryptsUtil.decrypt(applyWeiXin.getSecret()));
                    applyWeiXin.setAgentId(EncryptsUtil.decrypt(applyWeiXin.getAgentId()));
                    applyWeiXin.setId(EncryptsUtil.decrypt(applyWeiXin.getId()));
                }
            } catch (Exception e) {

            }
        }
    }*/


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
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
            log.error("perform select accept 广州银行:", e);
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
            //3:查询发送方
            selectFrom();
            log.info("perform select send 广州银行 finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送企业微信消息
            log.info("广州银行 message send star");
            if(sendMessage != null){
                sendMessage(sendMessage);
            }
            log.info("广州银行 message send finish");
            return null;
        }catch (Exception e){
            log.error("广州银行 message send appear unknown error:",e);
            throw new Exception(e);
        }
    }
}
