package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.activiti.param.DutyCommonsParam;
import cn.mw.monitor.service.activiti.param.service.DutyManageCommonService;
import cn.mw.monitor.service.ipmanage.exception.IpScanInterruptException;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.api.MWScanCommonService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.user.model.ScanIpAddressManageQueueVO;
import cn.mw.monitor.user.dao.MwMessageManageDao;
import cn.mw.monitor.user.service.MWMessageManageService;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.websocket.Message;
import cn.mw.monitor.websocket.RealTimeUpdateDataDTO;
import cn.mw.monitor.websocket.WebSocketGetCount;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static cn.mw.monitor.user.model.SendMessageConstant.*;

/**
 * @author lumingming
 * @createTime 202110/2929 16:20
 * @description 实现消息传递
 */
@Service
@Slf4j
public class MWMessageManageServiceImpl implements MWMessageService, MWMessageManageService {


    @Resource
    private MwMessageManageDao mwMessageManageDao;

    @Autowired
    private WebSocketGetCount webSocketGetCount;
    @Autowired
    ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    MWScanCommonService mwScanCommonService;

    @Autowired
    private MWUserCommonService mwUserCommonService;
    @Autowired
    private DutyManageCommonService dutyManageCommonService;

    @Override
    public void sendActivitiMessage(String text, List<MWUser> users) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        DutyCommonsParam commonsParam = new DutyCommonsParam();
        Date date = new Date();
        commonsParam.setStartDate(format.format(date));
        commonsParam.setEndDate(format.format(date));
        HashSet<Integer> userIds = dutyManageCommonService.getDutyUserIds(commonsParam);
        if(CollectionUtils.isEmpty(userIds)) return;
        List<MWUser> sendUser = new ArrayList<>();
        for (MWUser user : users){
            if(userIds.contains(user.getUserId())){
                sendUser.add(user);
            }
        }
        createMessage(text, 1, 0,"流程审批",sendUser);
    }

    @Override
    public void sendFailAlertMessage(String text, List<MWUser> users,String title, Boolean isRedirect,Object obj) {
        createMessageByusers(text, 1, 0,title,users,isRedirect,obj);
    }

    @Override
    public void sendTimeOutMessage(String text, List<MWUser> users,Boolean isRedirect,Object obj) {
        createMessageByusers(text, 1, 0,TIME_OUT,users,isRedirect,obj);
    }

    @Override
    public void sendAssetsNameSyncSuccessMessage(String text, List<MWUser> users) {
        createMessage(text, 1, 0,ASSETS_NAME_SYNC,users);
    }

    @Override
    public void sendVirtualDeviceSuccessMessage(String text, List<MWUser> users) {
        createMessage(text, 1, 0,VRITUA_LIZATION,users);
    }

    @Override
    public void sendAssetsScanCompleteMessage(String text, List<MWUser> users,String title) {
        createMessage(text, 1, 0,title,users);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void createMessage(String text,Integer type,Integer percentScan,MWUser user) {
        if (user!=null){
            List<MWUser> m= new ArrayList<>();
            m.add(user);
            createMessage(text, type, percentScan,IP_MANAGE,m);
        }else {
            try {
                List<MWUser> m= new ArrayList<>();
                MWUser userInfo = (MWUser) mwUserCommonService.selectByUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId()).getData();
                m.add(userInfo);
                createMessage(text, type, percentScan,IP_MANAGE,m);
            }catch (Exception e){
                log.error("queryList" ,e);
            }
        }

    }

    public void createMessageByusers(String text, Integer type, Integer percentScan, String tittle,List<MWUser> users,Boolean isRedirect,Object obj) {
        String loginName ="";
        Integer userId =0;
        if (users.size()==0){
            try {
                loginName = iLoginCacheInfo.getLoginName();
                userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
                sendMessage(text,type,percentScan,tittle,loginName,userId,isRedirect,obj);
            }catch (Exception e){
                log.info("自动扫描定时任务开启");
            }
        }
        else {
            for (MWUser user:users) {
                loginName = user.getLoginName();
                userId = user.getUserId();
                sendMessage(text,type,percentScan,tittle,loginName,userId,isRedirect,obj);
            }
        }
    }

    public void createMessage(String text, Integer type, Integer percentScan, String tittle,List<MWUser> users) {
        String loginName ="";
        Integer userId =0;
        if (users.size()==0){
            try {
                loginName = iLoginCacheInfo.getLoginName();
                userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
                sendMessage(text,type,percentScan,tittle,loginName,userId);
            }catch (Exception e){
                log.info("自动扫描定时任务开启");
            }
        }
        else {
            for (MWUser user:users) {
                loginName = user.getLoginName();
                userId = user.getUserId();
                sendMessage(text,type,percentScan,tittle,loginName,userId);
            }
        }
    }

    private  void sendMessage(String text, Integer type, Integer percentScan, String tittle,String loginName,Integer userId){
        //组装全局参数
        RealTimeUpdateDataDTO realTimeUpdateDataDTO = new RealTimeUpdateDataDTO();
        List<ScanIpAddressManageQueueVO> scanIpAddressManageQueue = mwScanCommonService.selectqueueList(-1);
        List<Integer> ids = new ArrayList<>();
        for (ScanIpAddressManageQueueVO s:scanIpAddressManageQueue) {
            ids.add(s.getLinkId());
        }
        realTimeUpdateDataDTO.setLinkIds(ids);

        //已读消息
        List<Message> readMessage = new ArrayList<>();
        //未读消息
        List<Message> unReadMessage = new ArrayList<>();
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        String key = loginName+text;
        groupByRead(readMessage,unReadMessage,loginName);
        boolean kill = true;
        if (type==0){
            redisUtils.set("crruntUserScan"+loginName, text,86400);
            if (redisUtils.get("crruntUserScan"+loginName).toString().equals(text)){
                if (percentScan==0){
                    redisUtils.del("brokenPonint"+key);
                }
                redisUtils.set("PercentScan"+key, percentScan,86400);
                redisUtils.set("BelongeId"+key, text,86400);
                realTimeUpdateDataDTO.setPercentScan(percentScan);
                realTimeUpdateDataDTO.setScan(true);
                realTimeUpdateDataDTO.setBelongId(Integer.valueOf(text));
            }else {
                kill = false;
            }
            if (percentScan==100){
                realTimeUpdateDataDTO.setScan(false);
                redisUtils.del("PercentScan"+key);
                redisUtils.del("BelongeId"+key);
            }
        }else if (type==1){
            if (redisUtils.get("PercentScan"+key)!=null&&redisUtils.get("crruntUserScan"+loginName).toString().equals(text)){
                String  percentS= redisUtils.get("PercentScan"+key).toString();
                String  BelongeId= redisUtils.get("BelongeId"+key).toString();
                if (percentS!=null&&!percentS.equals("0")){
                    realTimeUpdateDataDTO.setPercentScan(percentScan);
                    realTimeUpdateDataDTO.setScan(true);
                    realTimeUpdateDataDTO.setBelongId(Integer.valueOf(BelongeId));
                }
            }
            Message message = createNewMessage(text,loginName,tittle,userId);
            realTimeUpdateDataDTO.setNewMessage(message);
            unReadMessage.add(message);
        }else if (type==4){
            redisUtils.set("crruntUserScan"+loginName, text,86400);
            if (redisUtils.get("PercentScan"+key)!=null){
                String  percentS= redisUtils.get("PercentScan"+key).toString();
                String  BelongeId= redisUtils.get("BelongeId"+key).toString();
                if (percentS!=null&&!percentS.equals("0")){
                    realTimeUpdateDataDTO.setPercentScan(Integer.parseInt(percentS));
                    realTimeUpdateDataDTO.setScan(true);
                    realTimeUpdateDataDTO.setBelongId(Integer.valueOf(BelongeId));
                }
            }
        }
        realTimeUpdateDataDTO.setUnReadMessage(unReadMessage);
        realTimeUpdateDataDTO.setUnReadMessageCount(unReadMessage.size());
        realTimeUpdateDataDTO.setReadMessage(readMessage);
        realTimeUpdateDataDTO.setReadMessageCount(readMessage.size());
        realTimeUpdateDataDTO.setTotalCount(unReadMessage.size()+readMessage.size());
        realTimeUpdateDataDTO.setUnfinishActiviti(mwMessageManageDao.selectUnfinishActivitiByreadUserLoginName(loginName));
        if (kill){
            synchronized (webSocketGetCount.sessionPool){
                if (webSocketGetCount.sessionPool.get(userId) != null) {
                    webSocketGetCount.sendObjMessage(userId, realTimeUpdateDataDTO);
                }
            }
        }
    }

    private  void sendMessage(String text, Integer type, Integer percentScan, String tittle,String loginName,Integer userId,Boolean isRedirect,Object obj){
        RealTimeUpdateDataDTO realTimeUpdateDataDTO = new RealTimeUpdateDataDTO();
        //已读消息
        List<Message> readMessage = new ArrayList<>();
        //未读消息
        List<Message> unReadMessage = new ArrayList<>();
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        String key = loginName+text;
        groupByRead(readMessage,unReadMessage,loginName);
        boolean kill = true;
        if (type==0){
            if (redisUtils.get("crruntUserScan"+loginName).toString().equals(text)){
                if (percentScan==0){
                    redisUtils.del("brokenPonint"+key);
                }
                redisUtils.set("PercentScan"+key, percentScan,86400);
                redisUtils.set("BelongeId"+key, text,86400);
                realTimeUpdateDataDTO.setPercentScan(percentScan);
                realTimeUpdateDataDTO.setScan(true);
                realTimeUpdateDataDTO.setBelongId(Integer.valueOf(text));
            }else {
                kill = false;
            }
            if (percentScan==100){
                realTimeUpdateDataDTO.setScan(false);
                redisUtils.del("PercentScan"+key);
                redisUtils.del("BelongeId"+key);
            }
        }else if (type==1){
            if (redisUtils.get("PercentScan"+key)!=null&&redisUtils.get("crruntUserScan"+loginName).toString().equals(text)){
                String  percentS= redisUtils.get("PercentScan"+key).toString();
                String  BelongeId= redisUtils.get("BelongeId"+key).toString();
                if (percentS!=null&&!percentS.equals("0")){
                    realTimeUpdateDataDTO.setPercentScan(percentScan);
                    realTimeUpdateDataDTO.setScan(true);
                    realTimeUpdateDataDTO.setBelongId(Integer.valueOf(BelongeId));
                }
            }
            Message message = createNewMessage(text,loginName,tittle,userId,isRedirect,obj);
            realTimeUpdateDataDTO.setNewMessage(message);
            unReadMessage.add(message);
        }else if (type==4){
            redisUtils.set("crruntUserScan"+loginName, text,86400);
            if (redisUtils.get("PercentScan"+key)!=null){
                String  percentS= redisUtils.get("PercentScan"+key).toString();
                String  BelongeId= redisUtils.get("BelongeId"+key).toString();
                if (percentS!=null&&!percentS.equals("0")){
                    realTimeUpdateDataDTO.setPercentScan(Integer.parseInt(percentS));
                    realTimeUpdateDataDTO.setScan(true);
                    realTimeUpdateDataDTO.setBelongId(Integer.valueOf(BelongeId));
                }
            }
        }
        realTimeUpdateDataDTO.setUnReadMessage(unReadMessage);
        realTimeUpdateDataDTO.setUnReadMessageCount(unReadMessage.size());
        realTimeUpdateDataDTO.setReadMessage(readMessage);
        realTimeUpdateDataDTO.setReadMessageCount(readMessage.size());
        realTimeUpdateDataDTO.setTotalCount(unReadMessage.size());
        realTimeUpdateDataDTO.setUnfinishActiviti(mwMessageManageDao.selectUnfinishActivitiByreadUserLoginName(loginName));
        realTimeUpdateDataDTO.setDataInfo(obj);
        log.info("webSocket发送信息::kill:"+type+";kill:"+kill);
        if(unReadMessage!=null && unReadMessage.size()>0){
            log.info("webSocket发送信息::unReadMessage"+unReadMessage.get(0).getMessageText());
        }
        if (kill){
            synchronized (webSocketGetCount.sessionPool) {
                if (webSocketGetCount.sessionPool.get(userId) != null) {
                    log.info("发送信息:11");
                    webSocketGetCount.sendObjMessage(userId, realTimeUpdateDataDTO);
                    log.info("发送信息:userId::"+userId+";realTimeUpdateDataDTO::"+realTimeUpdateDataDTO);
                }
            }
        }
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reply chageEditor(Integer param) {
        try{
            String loginName = iLoginCacheInfo.getLoginName();
            if (param>=0){
                mwMessageManageDao.chageEditor(loginName,param);
            }else {
                mwMessageManageDao.delete(loginName,param);
            }
            System.out.println(1111);
            MWUser userInfo = (MWUser) mwUserCommonService.selectByUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId()).getData();
            System.out.println(1111);
            createMessage(null,2,0,userInfo);
            System.out.println(1111);
        }catch (Exception e){

        }

        return null;
    }

    @Override
    public void brokenPonint(String s) {
        String loginName = "";
        try {
             loginName = iLoginCacheInfo.getLoginName();
        }catch (Exception e){
            log.info("自动扫描定时任务开启");
        }
       if (!loginName.trim().equals("")){
           String key = loginName+s;
           RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
           if (redisUtils.get("brokenPonint"+key)!=null){
               redisUtils.del("PercentScan"+key);
               redisUtils.del("brokenPonint"+key);
               redisUtils.del("BelongeId"+key);
               throw new IpScanInterruptException();
           }
       }
    }

    @Override
    public void createBrokenPonint(String toString) {
        String loginName = iLoginCacheInfo.getLoginName();
        String key = loginName+toString;
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        redisUtils.del("PercentScan"+key);
        redisUtils.del("brokenPonint"+key);
        redisUtils.del("BelongeId"+key);
        redisUtils.set("brokenPonint"+key,true,86400);
    }

    private void groupByRead(List<Message> readMessage, List<Message> unReadMessage, String loginName) {
        readMessage.addAll(mwMessageManageDao.selectByreadUserLoginName(loginName,1));
        unReadMessage.addAll(mwMessageManageDao.selectByreadUserLoginName(loginName,0));
    }

    private Message createNewMessage(String text, String loginName,String tittle,Integer userId) {
        Message message = new Message();
        message.setCreateDate(new Date());
        message.setOwnUser(loginName);
        message.setModule(tittle==null?IP_MANAGE:tittle);
        message.setReadStatus(0);
        message.setMessageText(text);
        message.setUserId(userId);
        mwMessageManageDao.insert(message);
        return message;
    }

    private Message createNewMessage(String text, String loginName,String tittle,Integer userId,Boolean isRedirect,Object obj) {
        Message message = new Message();
        message.setCreateDate(new Date());
        message.setOwnUser(loginName);
        message.setModule(tittle==null?IP_MANAGE:tittle);
        message.setReadStatus(0);
        message.setMessageText(text);
        message.setUserId(userId);
        message.setIsRedirect(isRedirect);
        if(obj!=null){
            message.setNode(JSONObject.toJSON(obj).toString());
        }
        mwMessageManageDao.insert(message);
        return message;
    }

}
