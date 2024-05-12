package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.ipaddressmanage.param.AddUpdateIpAddressManageListParam;
import cn.mw.monitor.service.alert.api.MWMessageNotifyService;
import cn.mw.monitor.service.alert.callback.BusinessIds;
import cn.mw.monitor.service.alert.callback.BusinessIdsFetch;
import cn.mw.monitor.service.assets.model.IpAssetsNameDTO;
import cn.mw.monitor.service.common.NetworkConstant;
import cn.mw.monitor.service.ipmanage.model.IPInfoChangeView;
import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.snmp.utils.SNMPUtils;
import cn.mw.monitor.util.EmailSendUtil;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class NotificationManage {

    @Autowired
    private MWMessageNotifyService mwMessageNotifyService;

    public void checkIPInfoAndNofity(List<NotifyParam> uParam, final Map<String, List<IPInfoDTO>> scanRes){

        //对比是否有差异
        List<IPInfoChangeView> changeViews = new ArrayList<>();
        Set<Integer> linkIdSet = new HashSet<>();
        for(NotifyParam ipChangeInfo :uParam){
            linkIdSet.add(ipChangeInfo.getLinkId());
            List<IPInfoDTO> ipChangeInfos = scanRes.get(ipChangeInfo.getIpAddress());
            if(null != ipChangeInfos && ipChangeInfos.size() > 0){
                IPInfoDTO newIPInfoDTO = ipChangeInfos.get(0);
                String newMac = StringUtils.isNotEmpty(newIPInfoDTO.getMac())?SNMPUtils.convertMacFromHexString(newIPInfoDTO.getMac()):"";
                if(NetworkConstant.DEFAULT_MAC.equals(newMac) || StringUtils.isEmpty(newMac)){
                    log.warn("checkIPInfoAndNofity newMac:{}" ,newMac);
                    continue;
                }

                boolean chg = checkChange(ipChangeInfo ,newIPInfoDTO);
                if(chg){
                    IPInfoChangeView ipInfoChangeView = new IPInfoChangeView();
                    ipInfoChangeView.setIp(ipChangeInfo.getIpAddress());
                    ipInfoChangeView.setOldMac(ipChangeInfo.getMac());
                    ipInfoChangeView.setNewMac(newMac);
                    changeViews.add(ipInfoChangeView);
                }
            }
        }

        log.info("changeViews size:{}" ,changeViews.size());
        //如果有差异发送邮件
        if(changeViews.size() > 0){
            //发送邮件
            MessageContext messageContext = new MessageContext();
            messageContext.setCommon(true);
            messageContext.addKey(EmailSendUtil.TEMPLATE_PARAM ,changeViews);
            messageContext.addKey(EmailSendUtil.TEMPLATE_NAME ,"ipInfoChange");
            messageContext.addKey(EmailSendUtil.TEMPLATE_TITLE ,"IP扫描信息变化通知");

            IPManageBusinessFetchImpl ipManageBusinessFetch = new IPManageBusinessFetchImpl(new ArrayList<>(linkIdSet));
            messageContext.addKey(BusinessIdsFetch.MESSAGECONTEXT_KEY ,ipManageBusinessFetch);
            messageContext.addKey("数据来源" ,MWMessageNotifyService.IPMANAGE_MESSAGE_RULE_KEY);
            mwMessageNotifyService.sendMessage(messageContext);
        }

    }

    private boolean checkChange(NotifyParam oldInfo ,IPInfoDTO newInfo){
        boolean oldMacEmpty = StringUtils.isNotEmpty(oldInfo.getMac());
        boolean newMacEmpty = StringUtils.isNotEmpty(newInfo.getMac());
        if(!oldMacEmpty && !newMacEmpty){
            return false;
        }

        if(oldMacEmpty && newMacEmpty){
            String newMac = SNMPUtils.convertMacFromHexString(newInfo.getMac());
            return !oldInfo.getMac().equals(newMac);
        }

        return true;
    }
}
