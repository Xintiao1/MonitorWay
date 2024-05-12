package cn.mw.monitor.weixin.message;

import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.alert.dao.MwAlertRuleDao;
import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.alert.service.impl.ChooseUserNotify;
import cn.mw.monitor.alert.service.impl.DefautNotify;
import cn.mw.monitor.alert.service.impl.NotifyMethod;
import cn.mw.monitor.assetsTemplate.dao.MwAseetstemplateTableDao;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.common.util.GroupHosts;
import cn.mw.monitor.common.util.VHostTreeDTO;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.action.param.UserIdsType;
import cn.mw.monitor.service.action.service.CommonActionService;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.MWAlertAssetsParam;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.service.MwAssetsVirtualService;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.entity.ActionRule;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mw.monitor.weixin.service.impl.*;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageExecuter {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    @Autowired
    MWAlertService mwAlertService;

    @Autowired
    MwServerManager mwServerManager;

    @Autowired
    private CommonActionService commonActionService;

    @Resource
    private MwAlertActionDao mwAlertActionDao;

    @Resource
    private MwWeixinTemplateDao mwWeixinTemplateDao;

    @Autowired
    MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    MWGroupCommonService mwGroupCommonService;

    @Resource
    private MwAlertRuleDao mwAlertRuleDao;

    @Autowired
    private MWOrgCommonService mwOrgService;

    private MWCommonService mwCommonService;

    @Autowired
    private MwAssetsVirtualService mwVirtualService;

    @Autowired
    private MwAseetstemplateTableDao mwAseetstemplateTableDao;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;

    public Environment env;

    public static final int ADMIN = 106;

    private String modelEnable = "model.assets.enable";

    public MessageExecuter(){

        MWAlertService mwAlertService = ApplicationContextProvider.getBean(MWAlertService.class);
        this.mwAlertService = mwAlertService;

        MwServerManager mwServerManager = ApplicationContextProvider.getBean(MwServerManager.class);
        this.mwServerManager = mwServerManager;
        CommonActionService commonActionService = ApplicationContextProvider.getBean(CommonActionService.class);
        this.commonActionService = commonActionService;

        MwAlertActionDao mwAlertActionDao = ApplicationContextProvider.getBean(MwAlertActionDao.class);
        this.mwAlertActionDao = mwAlertActionDao;
        MwWeixinTemplateDao mwWeixinTemplateDao = ApplicationContextProvider.getBean(MwWeixinTemplateDao.class);
        this.mwWeixinTemplateDao = mwWeixinTemplateDao;

        MwLabelCommonServcie mwLabelCommonServcie = ApplicationContextProvider.getBean(MwLabelCommonServcie.class);
        this.mwLabelCommonServcie = mwLabelCommonServcie;
        MWGroupCommonService mwGroupCommonService = ApplicationContextProvider.getBean(MWGroupCommonService.class);
        this.mwGroupCommonService = mwGroupCommonService;

        MwAlertRuleDao mwAlertRuleDao = ApplicationContextProvider.getBean(MwAlertRuleDao.class);
        this.mwAlertRuleDao = mwAlertRuleDao;
        MWOrgCommonService mwOrgService = ApplicationContextProvider.getBean(MWOrgCommonService.class);
        this.mwOrgService = mwOrgService;

        this.mwCommonService = ApplicationContextProvider.getBean(MWCommonService.class);

        MwAssetsVirtualService mwVirtualService = ApplicationContextProvider.getBean(MwAssetsVirtualService.class);
        this.mwVirtualService = mwVirtualService;

        MwAseetstemplateTableDao mwAseetstemplateTableDao = ApplicationContextProvider.getBean(MwAseetstemplateTableDao.class);
        this.mwAseetstemplateTableDao = mwAseetstemplateTableDao;

        MwAssetsManager mwAssetsManager = ApplicationContextProvider.getBean(MwAssetsManager.class);
        this.mwAssetsManager = mwAssetsManager;

        MwModelViewCommonService mwModelViewCommonService = ApplicationContextProvider.getBean(MwModelViewCommonService.class);
        this.mwModelViewCommonService = mwModelViewCommonService;

        Environment env = ApplicationContextProvider.getBean(Environment.class);
        this.env = env;

        MwTangibleAssetsService mwTangibleAssetsService = ApplicationContextProvider.getBean(MwTangibleAssetsService.class);
        this.mwTangibleAssetsService = mwTangibleAssetsService;

    }

    //处理发送消息
    //List<SendMessageBase> ruleBases = new ArrayList<>();
    public void execute(String msg, ActionLevelRuleParam alr, HashSet<Integer> userIds, Integer size) throws Exception {
        log.info("发送告警msg:" + msg);
        boolean modelAssetEnable = env.getProperty(modelEnable,boolean.class);
        msg = toJsonString(msg);
        HashMap<String, String> map = new HashMap<>();
        msg = converUnicodeToChar(msg);
        String[] strs = msg.split(",");
        String regex = ":";
        for (String s : strs) {
            String s1 = s.substring(0, s.indexOf(regex) + 1).replaceAll(":", "");
            String s2 = s.substring(s.indexOf(regex) + 1);
            map.put(s1, s2);
        }
        if(size != null){
            map.put(AlertEnum.CompressNumber.toString(),size.toString());
        }
        log.info("发送告警map:" + map);
        //根据资产id和ip去查询发送信息的规则
        //1 判断资产是否存在 资产不存在不发送信息
        Boolean isAlarm = map.get(AlertEnum.ALERTTITLE.toString()) == null ? map.get(AlertEnum.RECOVERYTITLE.toString()) == null ? null : false : true;
        String title = "";
        if (isAlarm) {
            title = AlertEnum.ALERTTITLE.toString();
        } else {
            title = AlertEnum.RECOVERYTITLE.toString();
        }
        String hostid = map.get(AlertEnum.HOSTID.toString());
        String hostip = map.get(AlertEnum.HOSTIP.toString());
        List<MwTangibleassetsTable> mwTangibleassetsTables = new ArrayList<>();
        if(map.get(title).contains(AlertEnum.VR.toString())){
            String hostName = map.get(AlertEnum.HOSTNAME.toString());
            hostName = hostName.substring(hostName.indexOf("<")+1,hostName.lastIndexOf(">"));
            String regexIp = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
            Pattern pattern = Pattern.compile(regexIp);
            Matcher matcher = pattern.matcher(hostName);
            log.info("虚拟化资产ip处理前：" + hostName);
            QueryTangAssetsParam qParam = new QueryTangAssetsParam();
            qParam.setInBandIp(hostip);
            qParam.setUserId(ADMIN);
            mwTangibleassetsTables = mwAssetsManager.getAssetsTable(qParam);
            if(matcher.find()){
                hostip = matcher.group();
                map.put(AlertEnum.HOSTIP.toString(),hostip);
                log.info("虚拟化资产ip处理后：" + hostip);
            }

        }
        log.info("dealMessage,hostid:{},hostip:{}", hostid, hostip);
        QueryTangAssetsParam param = new QueryTangAssetsParam();
        param.setAlertQuery(true);
        param.setAssetsId(hostid);
        param.setInBandIp(hostip);
        param.setSkipDataPermission(true);
        param.setUserId(ADMIN);

        MwTangibleassetsDTO assets = new MwTangibleassetsDTO();
        String key = hostid + AlertAssetsEnum.Dash.toString() + hostip;
        assets = MWAlertAssetsParam.tangibleassetsDTOMap.get(key);
        if(assets == null){
            List<MwTangibleassetsTable> assetsList = mwAssetsManager.getAssetsTable(param);
            if(CollectionUtils.isNotEmpty(assetsList)){
                BeanUtils.copyProperties(assetsList.get(0),assets);
            }
        }

        log.info("assets:" + assets);
        if(CollectionUtils.isNotEmpty(mwTangibleassetsTables)){
            assets.setId(mwTangibleassetsTables.get(0).getId());
        }
        String assetsIp = hostip;
        String assetsName = map.get(AlertEnum.HOSTNAME.toString());
        String specifications = null;
        String id = null;

        if (map.get(AlertEnum.ALERTTITLE.toString()) != null) {
            String wtxq = getWtxq(map.get(AlertEnum.PROBLEMDETAILS.toString()));
            map.put(AlertEnum.PROBLEMDETAILS.toString(), wtxq);
        } else {
            String hfxq = getWtxq(map.get(AlertEnum.RECOVERYDETAILS.toString()));
            map.put(AlertEnum.RECOVERYDETAILS.toString(), hfxq);
        }
        if (assets == null || assets.getId() == null) {
            MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
            mwCommonAssetsDto.setUserId(ADMIN);
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = mwAssetsManager.getAllAssetsByUserId(mwCommonAssetsDto);
            for (MwTangibleassetsTable mt : mwTangibleassetsDTOS) {
                if(hostid.equals(mt.getAssetsId())){
                    assets = new MwTangibleassetsDTO();
                    assets.setAssetsId(mt.getAssetsId());
                    assets.setInBandIp(mt.getInBandIp());
                    assets.setMonitorModeName(mt.getMonitorModeName());
                    assets.setAssetsName(mt.getAssetsName());
                    assets.setAssetsTypeName(mt.getAssetsTypeName());
                    assets.setMonitorServerId(mt.getMonitorServerId());
                    assets.setId(mt.getId());
                    assetsIp = mt.getInBandIp();
                    hostip = mt.getInBandIp();
                    break;
                }
            }
            if(assets == null || assets.getInBandIp() == null){
                GroupHosts vrHosts = isDealVr(hostid);
                if(vrHosts == null){
                    log.info("dealMessage, can not find assets");
                    return;
                }else{
                    assetsIp = vrHosts.getName();
                    hostip = vrHosts.getName();
                }
            }
        }
        //2 判断是否关联我的监控下面的web监测和线路
        if(assets != null){
            log.info("exmessage assets:" + assets);
            map.put(AlertEnum.MODELSYSTEM.toString(),assets.getModelSystem() + "-" + assets.getModelClassify());
            map.put(AlertEnum.Specifications.toString(),assets.getSpecifications());

            //资产表主键id
            id = assets.getId()==null ? assets.getModelId()==null?null:assets.getModelId().toString(): assets.getId();
            assetsIp = assets.getInBandIp();
            if(assets.getAssetsTypeName() != null && assets.getAssetsTypeName().equals(AlertEnum.CONTROLLER.toString())){
                assetsName = map.get(title).substring(map.get(title).indexOf(AlertAssetsEnum.LeftBracket.toString())+1,map.get(title).indexOf(AlertAssetsEnum.RightBracket.toString()));
            }else if(assets.getAssetsName() != null){
                assetsName = assets.getAssetsName();
            }
            specifications = assets.getSpecifications();
            String assetsMonitor = mwAlertService.getAssetsMonitor(assets);
            map.put(AlertEnum.AssociatedModule.toString(), assetsMonitor);
        }
        //添加字段
        map.put(AlertEnum.HostNameZH.toString(), assetsName);
        map.put(AlertEnum.IPAddress.toString(), assetsIp);
        map.put(AlertEnum.SystemInfo.toString(), specifications);
        //5分钟重复告警信息不发送
        Date date = new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.add(GregorianCalendar.MINUTE, -5);
        /*AlertRecordTable arTable = new AlertRecordTable();
        arTable.setDate(gc.getTime());
        arTable.setTitle(title);
        arTable.setIsAlarm(isAlarm);
        arTable.setIp(map.get("IP地址"));
        log.info("arTable:" + arTable);
        if(isAlarm){
            Integer count = mwWeixinTemplateDao.selectRecord(arTable);
            log.info("count:" + count);
            if (count != 0) {
                return;
            }
        }*/
        //3查询规则表id
        List<String> actionIds = new ArrayList<>();
        /*if(id != null){
            actionIds = commonActionService.getActionByHostId(id);
        }else{
            actionIds = mwAlertActionDao.selectActionIds();
        }*/

        List<ActionRule> rules = new ArrayList<>();
        //判断是否为分级告警
        if(alr != null && alr.getActionId() != null && !alr.getActionId().equals("")){
            actionIds.clear();
            actionIds.add(alr.getActionId());
            rules = mwWeixinTemplateDao.selectLevelRuleMapper(alr.getActionId(),alr.getLevel());
        }
        log.info("告警发送规则 actionIds" + actionIds);
        for (String actionId : actionIds) {
            //判断告警动作是否开启
            log.info("dealMessage, 规则告警actionId:" + actionId);
            List<AssetsFielidParam> assetsFielidParams = mwAlertActionDao.selectActionAssetsclumn(actionId);
            if(assetsFielidParams != null && assetsFielidParams.size()>0){
                for(int i=0;i<assetsFielidParams.size();i++){
                    if(assetsFielidParams.get(i).getClumnName().equals(AlertEnum.Default.toString())){
                        map.put(AlertEnum.DefaultSelection.toString(), AlertEnum.Default.toString());
                        assetsFielidParams.remove(i);
                    }
                }
                if(assetsFielidParams.size() > 0){
                    MwTangibleassetsParam result = mwAlertActionDao.slecltTangibleassetsByfielids(assetsFielidParams, id);
                    if (result != null) {
                        if (result.getId() != null) {
                            map.put(AlertEnum.AutoSequence.toString(), result.getId());
                        }
                        if (result.getAssetsId() != null) {
                            map.put(AlertEnum.AssetsID.toString(), result.getAssetsId());
                        }
                        if (result.getAssetsName() != null) {
                            map.put(AlertEnum.AssetsName.toString(), result.getAssetsName());
                        }
                        if (result.getHostName() != null) {
                            map.put(AlertEnum.HostNameZH.toString(), result.getHostName());
                        }
                        if (result.getInBandIp() != null) {
                            map.put(AlertEnum.InBandIp.toString(), result.getInBandIp());
                        }
                        if (result.getAssetsTypeId() != null) {
                            map.put(AlertEnum.ASSETSTYPE.toString(),  mwAseetstemplateTableDao.selectTypeName(result.getAssetsTypeId()));
                        }
                        if (result.getAssetsTypeSubId() != null) {
                            String assetsTypeName = mwAseetstemplateTableDao.selectTypeName(result.getAssetsTypeSubId());
                            map.put(AlertEnum.AssetsTypeSubId.toString(), assetsTypeName);
                        }
                        if (result.getPollingEngine() != null) {
                            map.put(AlertEnum.PollingEngine.toString(), result.getPollingEngine());
                        }
                        if (result.getMonitorMode() != null) {
                            map.put(AlertEnum.MonitorMode.toString(), result.getMonitorMode().toString());
                        }
                        if (result.getManufacturer() != null) {
                            map.put(AlertEnum.Manufacturer.toString(), result.getManufacturer());
                        }
                        if (result.getSpecifications() != null) {
                            map.put(AlertEnum.Specifications.toString(), result.getSpecifications());
                        }
                        if (result.getDescription() != null) {
                            map.put(AlertEnum.Description.toString(), result.getDescription());
                        }
                        if (result.getEnable() != null) {
                            map.put(AlertEnum.AssetsStatus.toString(), result.getEnable());
                        }
                        if (result.getDeleteFlag() != null) {
                            map.put(AlertEnum.DeleteFlag.toString(), result.getDeleteFlag().toString());
                        }
                        if (result.getMonitorFlag() != null) {
                            map.put(AlertEnum.MonitorFlag.toString(), result.getMonitorFlag().toString());
                        }
                        if (result.getSettingFlag() != null) {
                            map.put(AlertEnum.SettingFlag.toString(), result.getSettingFlag().toString());
                        }
                        if (result.getCreator() != null) {
                            map.put(AlertEnum.Creator.toString(), result.getCreator());
                        }
                        if (result.getCreateDate() != null) {
                            map.put(AlertEnum.CreateDate.toString(), result.getCreateDate().toString());
                        }
                        if (result.getModifier() != null) {
                            map.put(AlertEnum.Modifier.toString(), result.getModifier());
                        }
                        if (result.getModificationDate() != null) {
                            map.put(AlertEnum.ModificationDate.toString(), result.getModificationDate().toString());
                        }
                        if (result.getScanSuccessId() != null) {
                            map.put(AlertEnum.ScanSuccessId.toString(), result.getScanSuccessId().toString());
                        }
                        if (result.getMonitorServerId() != null) {
                            map.put(AlertEnum.MonitorServerId.toString(), result.getMonitorServerId().toString());
                        }
                        if (result.getTiming() != null) {
                            map.put(AlertEnum.Timing.toString(), result.getTiming());
                        }
                        if (result.getTpServerHostName() != null) {
                            map.put(AlertEnum.TpServerHostName.toString(), result.getTpServerHostName());
                        }
                        if (result.getTemplateId() != null) {
                            map.put(AlertEnum.TemplateId.toString(), result.getTemplateId());
                        }
                        if (result.getAssetsUuid() != null) {
                            map.put(AlertEnum.AssetsUuid.toString(), result.getAssetsUuid());
                        }
                        if (result.getAssetsSerialnum() != null) {
                            map.put(AlertEnum.AssetsSerialnum.toString(), result.getAssetsSerialnum());
                        }
                    }
                }
            }

            AddAndUpdateAlertActionParam alertActionParam = commonActionService.selectPopupAction(actionId);
               /* //4 通过actionId查询对应要发送的资产
                List<String> ids = commonActionService.getActionAssetsIds(actionId);
                log.info("dealMessage,ids:" + ids.size());
                if (ids.size() == 0) {
                    continue;
                }
                //5 判断当前资产是否 在需要发送的资产中
                if (ids.contains(id) || id == null) {

                }*/
            //6 如果不存在分级告警 查询发送方式和发送者 mw_alert_action_rule_mapper
            if(rules == null || rules.size() == 0){
                rules = mwWeixinTemplateDao.selectRuleMapper(actionId);
            }
            //7 查询接收者
            UserIdsType userIdsType = new UserIdsType();
            if(CollectionUtils.isEmpty(userIds) || (alr != null && alr.getIsActionLevel() != null && alr.getIsActionLevel())){
                userIdsType = commonActionService.getActionUserIds(actionId, id, assets);
                log.info("查询接收者 getActionUserIds:" + userIdsType);
            }
            if(CollectionUtils.isEmpty(userIdsType.getPersonUserIds())){
                userIdsType = commonActionService.getOutbandUserIds(actionId, id);
            }
            if(CollectionUtils.isEmpty(userIds)){
                userIds = new HashSet<>();
            }
            if(CollectionUtils.isNotEmpty(userIdsType.getPersonUserIds())){
                userIds.addAll(userIdsType.getPersonUserIds());
            }
            log.info("查询接收者 userIds:" + userIds);
            if(CollectionUtils.isEmpty(userIds)){
                userIds = commonActionService.getVrUserIds(actionId,hostid);
                log.info("查询接收者 getVrUserIds:" + userIds);
            }
            if(CollectionUtils.isEmpty(userIds)){
                if (map.get(title).contains(AlertEnum.WebMonitor.toString())) {
                    userIds = getWebMonitorUsers(map.get(title), hostid);
                    log.info("[网站监测] userIds:" + userIds);
                }
                if (map.get(title).contains(AlertEnum.XianLu.toString())) {
                    userIds = getXianLuUserIds(map.get(title), hostip, hostid);
                    log.info("[线路] userIds:" + userIds);
                }
            }
            if (map.get(title).contains(AlertEnum.WebMonitor.toString())) {
                map.put(AlertEnum.IPAddress.toString(),assets.getWebUrl());
                map.put(AlertEnum.HostNameZH.toString(),assets.getAssetsName());
            }
            if (alr==null) alr = new ActionLevelRuleParam();
            alr.setEmailCC(commonActionService.getActionEamilCCUserIds(actionId));
            alr.setGroupUserIds(userIdsType.getGroupUserIds());
            alr.setOrgUserIds(userIdsType.getOrgUserIds());
            alr.setUserIdsType(userIdsType);
            alr.setEmailUserIds(userIdsType.getEmailUserIds());
            alr.setEmailGroupUserIds(userIdsType.getEmailGroupUserIds());
            if(!modelAssetEnable){
                if(CollectionUtils.isNotEmpty(userIdsType.getGroupUserIds())) userIds.addAll(userIdsType.getGroupUserIds());
                if(CollectionUtils.isNotEmpty(userIdsType.getOrgUserIds())) userIds.addAll(userIdsType.getOrgUserIds());
            }
            log.info("dealMessage userids:" + userIds);
            log.info("dealMessage emailCC:" + alr.getEmailCC());
            //获取区域字段
            String doamin = assets.getModelArea() == null ? AlertEnum.AddLabel.toString() : assets.getModelArea();
            log.info("doamin:" + assets.getModelArea());
            map.put(AlertEnum.Domain.toString(),doamin);
            if(assets.getId() != null && !modelAssetEnable){
                MwTangibleassetsDTO labelDtos = mwAssetsManager.getAssetsAndOrgs(assets.getId());
                if(labelDtos != null && CollectionUtils.isNotEmpty(labelDtos.getAssetsLabel())){
                    for(MwAssetsLabelDTO labelDTO : labelDtos.getAssetsLabel()){
                        if(labelDTO.getLabelName().equals(AlertEnum.Domain.toString())){
                            if (labelDTO.getInputFormat().equals(AlertAssetsEnum.One.toString())) {
                                map.put(AlertEnum.Domain.toString(), labelDTO.getTagboard());
                                break;
                            }
                            if (labelDTO.getInputFormat().equals(AlertAssetsEnum.Three.toString())) {
                                map.put(AlertEnum.Domain.toString(), labelDTO.getDropValue());
                                break;
                            }
                        }
                    }
                }


            }

            List<OrgDTO> orgDTOList = assets.getDepartment();
            if(CollectionUtils.isEmpty(orgDTOList)){
                Map tempMap = new HashMap();
                List list = new ArrayList();
                list.add(id);
                tempMap.put("ids", list);
                Reply reply = mwTangibleAssetsService.selectListWithExtend(tempMap);
                if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                    List<MwTangibleassetsDTO> datas = (List) reply.getData();
                    MwTangibleassetsDTO temp = new MwTangibleassetsDTO();
                    log.info("获取机构datas：" + datas);
                    if(CollectionUtils.isNotEmpty(datas)){
                        temp = datas.get(0);
                        orgDTOList = temp.getDepartment();
                    }
                }
            }
            String orgName = "";
            if (CollectionUtils.isNotEmpty(orgDTOList)) {
                log.info("labelDTOID:" + orgDTOList.size());
                for (OrgDTO s : orgDTOList) {
                    orgName = orgName + s.getOrgName() + "/";
                }
                orgName = orgName.substring(0, orgName.length() - 1);
                if(alertActionParam.getArea() != null && !alertActionParam.getArea().equals("")){
                    if(alertActionParam.getArea().equals(AlertEnum.ORG.toString()) && id != null){
                        map.put(AlertEnum.Domain.toString(), orgName);
                    }
                }

            }
            map.put(AlertEnum.ORG.toString(), orgName);
            map.put(AlertEnum.MODELCLASSIFY.toString(), assets.getModelClassify());
            map.put(AlertEnum.MODELSYSTEMZH.toString(), assets.getModelSystem());
            log.info("dealMessage userids:" + userIds);
            log.info("dealMessage alr:" + alr);
            log.info("dealMessage maps:" + map);
            doExecute(actionId ,rules ,map ,userIds ,assets ,alr ,null);
        }
    }

    public void executeMatchedActionId(MessageContext messageContext ,AddAndUpdateAlertActionParam alertActionParam){
        String actionId = alertActionParam.getActionId();
        List<ActionRule> rules = mwWeixinTemplateDao.selectRuleMapper(actionId);
        NotifyType notifyType = NotifyType.getNotifyType(alertActionParam.getIsAllUser());
        NotifyMethod notifyMethod = null;
        if(null != notifyType){
            switch (notifyType){
                case ChooseUser:
                    notifyMethod = new ChooseUserNotify(actionId ,mwAlertActionDao ,mwGroupCommonService);
                    break;
                case Default:
                    notifyMethod = new DefautNotify(mwCommonService ,mwGroupCommonService ,mwOrgService);
                    break;
                case Custom:
            }
        }

        HashSet<Integer> userIds = null;
        if(null != notifyMethod) {
            userIds =(HashSet) notifyMethod.getUserIds(messageContext);
        }
        try {
            doExecute(actionId, rules, null, userIds, null, null ,messageContext);
        }catch (Exception e){
            log.error("executeMatchedActionId" ,e);
        }
    }

    private void doExecute(String actionId ,List<ActionRule> rules ,HashMap<String, String> map ,HashSet<Integer> userIds
        ,MwTangibleassetsDTO assets ,ActionLevelRuleParam alr ,MessageContext messageContext) throws Exception{
        //查询规则级别，只有在这个级别的信息才可以发送
        HashSet<String> severity = mwWeixinTemplateDao.selectLevel(actionId);

        //判断发送方式（发送者是否存在）不存在不发送信息
        log.info("rules.size:{}", rules.size());
        ExecutorService pool = Executors.newFixedThreadPool(10);
        if (rules != null && rules.size() > 0) {
            for (ActionRule rule : rules) {
                AddAndUpdateAlertRuleParam alertRuleParam = mwAlertRuleDao.selectRuleById(rule.getRuleId());
                if (!alertRuleParam.getEnable()) {
                    log.info("该规则告警按钮已关闭ruleId：" + rule.getRuleId());
                    continue;
                }
                if(rule.getActionType() != 3  && rule.getActionType() != 5 && rule.getActionType() != 17 && rule.getActionType() != 18 && alr != null){
                    if(CollectionUtils.isNotEmpty(alr.getGroupUserIds())){
                        userIds.addAll(alr.getGroupUserIds());
                    }
                    if(CollectionUtils.isNotEmpty(alr.getOrgUserIds())){
                        userIds.addAll(alr.getOrgUserIds());
                    }
                }
                String alertLevel = env.getProperty("alert.level");
                if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                    String modelSystem = map.get(AlertEnum.MODELSYSTEM.toString());
                    modelSystem = modelSystem.substring(0,modelSystem.indexOf("-"));
                    HashSet<Integer> subUserIds = mwAlertRuleDao.selectSubUserId(modelSystem,rule.getRuleId());
                    if(CollectionUtils.isNotEmpty(subUserIds)){
                        userIds.addAll(subUserIds);
                    }
                }
                /*if(rule.getActionType() != 3  && CollectionUtils.isNotEmpty(alr.getEmailCC())){
                    userIds.addAll(alr.getEmailCC());
                }*/
                log.info("dealMessage 发送方式：" + rule.getActionType());
                log.info("dealMessage 接收人:" + userIds);
                SendMessageBase sendMessageBase = null;
                if (rule.getActionType() == 1) {//发送方式有微信服务号
                    //dealWxMessage(map, userIds, severity, assets);
                    sendMessageBase = new WxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if (rule.getActionType() == 3) {//发送方式有邮件
                    //dealEmailMessage(map, actionId, severity, userIds, rule.getRuleId(), assets);
                    sendMessageBase = new EmailSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId(),actionId,alr);
                } else if (rule.getActionType() == 5) {//发送方式有企业微信
                    //dealQyWeixinMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new QyWxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId(),alr);
                } else if (rule.getActionType() == 4) {//发送方式有短信
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new DxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if (rule.getActionType() == 7) {//发送方式有钉钉群消息
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new DingdingQunSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if (rule.getActionType() == 8) {//发送方式有阿里短息消息
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new AliyunDxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if (rule.getActionType() == 9) {//深圳广电局
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    log.info("深圳");
                    sendMessageBase = new ShenZhenSMSMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if(rule.getActionType() == 10){
                    sendMessageBase = new SysLogMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if (rule.getActionType() == 11) {//财政厅
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    log.info("财政厅");
                    sendMessageBase = new CaiZhengTingSMSMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                } else if (rule.getActionType() == 12) {//首钢
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new ShouGangSMSMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if (rule.getActionType() == 13) {//广州银行
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new GuangZhouBankMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if (rule.getActionType() == 14) {//阿里云语音
                    //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                    sendMessageBase = new AliyunYuYinSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 15){//腾讯华为短信混合
                    sendMessageBase = new TengxunAndHuaWeiDxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 16){//WeLink
                    sendMessageBase = new WelinkSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 17){
                    sendMessageBase = new HuaXingMobileSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId(),alr);
                }else if(rule.getActionType() == 18){
                    sendMessageBase = new TXinSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId(),alr);
                }else if(rule.getActionType() == 19){
                    sendMessageBase = new WorkSystemSendMessageimpl(map,alr.getUserIdsType(),rule.getRuleId());
                }else if(rule.getActionType() == 21){
                    sendMessageBase = new GaoXinQuSMSMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 22){
                    sendMessageBase = new HuaXingWorkSystemSendMessageimpl(map, userIds, severity);
                }else if(rule.getActionType() == 23){
                    sendMessageBase = new YuYinBoBaoSendMessageimpl(map, userIds, severity);
                }else if(rule.getActionType() == 24){
                    sendMessageBase = new HuaXingYuYinSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId(),alr);
                }else if(rule.getActionType() == 25){
                    sendMessageBase = new LeShanBankMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 26){
                    sendMessageBase = new YunZhiXunDxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 27){
                    sendMessageBase = new TianChangYunDxSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }else if(rule.getActionType() == 28){
                    sendMessageBase = new FeiShuQunSendMessageiImpl(map, userIds, severity, assets, rule.getRuleId());
                }

                if(null != sendMessageBase) {
                    if(null != messageContext){
                        sendMessageBase.setMessageContext(messageContext);
                        sendMessageBase.setCommon(messageContext.isCommon());
                    }

                    pool.submit(sendMessageBase);
                }
            }
        } else {
            log.info("发送方式不存在");
            return;
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程错误：" , e);
        }
    }

    /**
     * @describe 将unicode字符串转为正常字符串, 去除一些不规范符号
     */
    public String converUnicodeToChar(String str) {
        str = str.replaceAll("\\\\r", "")
                .replaceAll("\\\\n", "")
                .replaceAll("\\\\f", "")
                .replaceAll("\\\\b", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll(" ", "")
                .replaceAll("\\\\\"", "\"");

        Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
        Matcher matcher = pattern.matcher(str);

        //迭代，将str的unicode都转为字符
        while (matcher.find()) {
            String unicodeFull = matcher.group(1);
            String unicodeNum = matcher.group(2);

            char singleChar = (char) Integer.parseInt(unicodeNum, 16);
            str = str.replace(unicodeFull, singleChar + "");
        }
        return str;
    }

    private static String toJsonString(String s) {
        char[] tempArr = s.toCharArray();
        int tempLength = tempArr.length;
        for (int i = 0; i < tempLength; i++) {
            if (tempArr[i] == ':' && tempArr[i + 1] == '"') {
                for (int j = i + 2; j < tempLength; j++) {
                    if (tempArr[j] == '"') {
                        if (tempArr[j + 1] != ',' && tempArr[j + 1] != '}') {
                            tempArr[j] = '”'; // 将value中的 双引号替换为中文双引号
                        } else if (tempArr[j + 1] == ',' || tempArr[j + 1] == '}') {
                            break;
                        }
                    }
                }
            }
        }
        return new String(tempArr);
    }

    //获取问题详细转换后的数据
    public String getWtxq(String str) {
        if(str != null){
            String[] details = str.split(":");
            if (details.length == 2) {
                String srr = mwServerManager.getChName(details[0]);
                return srr + ":" + details[1];
            } else if (details.length < 2) {
                return mwServerManager.getChName(str);
            } else if (details.length > 2) {
                return str;
            }
            return str;
        }else {
            return null;
        }
    }

    public HashSet<Integer> getWebMonitorUsers(String webName,String hostid){
        webName = webName.substring(7);
        String[] s = webName.split("]");
        WebMonitorParam webMonitorParam = new WebMonitorParam();
        webMonitorParam.setWebName(s[0]);
        webMonitorParam.setHostid(hostid);
        Integer webId = mwAlertActionDao.selectWebmonitorId(webMonitorParam);
        HashSet<Integer> userIds = getUserIds(Integer.toString(webId));
        return userIds;
    }

    public HashSet<Integer> getUserIds(String typeId){
        HashSet<Integer> userIds = mwAlertActionDao.selectUserMapper(typeId);
        List<Integer> groupIds = mwAlertActionDao.selectGroupMapper(typeId);
        for (Integer groupid : groupIds) {
            Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
            if (selectGroupUser.getRes() == 0) {
                List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                if (null != groupUserData && groupUserData.size() > 0) {
                    for (GroupUserDTO pri : groupUserData) {
                        userIds.add(pri.getUserId());
                    }
                }
            }
        }
        List<Integer> orgIds = mwAlertActionDao.selectOrgMapper(typeId);
        List<Integer> orgUserIDs = mwOrgService.selectPubUserIdByOrgId(orgIds);
        if (null != orgUserIDs && orgUserIDs.size() > 0) {
            for (Integer pri : orgUserIDs) {
                userIds.add(pri);
            }
        }
        return userIds;
    }

    public HashSet<Integer> getXianLuUserIds(String portName, String hostIp, String assertId){
        portName = portName.substring(5);
        HashSet<Integer> userIds = new HashSet<>();
        String[] s = portName.split("]");
        if(s[0] == ""){
            return null;
        }
        NetworkLinkParam networkLinkParam = new NetworkLinkParam();
        networkLinkParam.setPortName(s[0]);
        networkLinkParam.setHostIp(hostIp);
        networkLinkParam.setAssertId(assertId);
        List<String> linkId = mwAlertActionDao.selectNetworkLinkId(networkLinkParam);
        for(String str : linkId){
            HashSet<Integer> userId = getUserIds(str);
            userIds.addAll(userId);
        }
        return userIds;
    }


    //判断是否为虚拟化资产
    public GroupHosts isDealVr(String hostid){
        List<VHostTreeDTO> assetsInfos = new ArrayList<>();
        Reply reply = mwVirtualService.getAllTree(AlertEnum.VHost.toString(),AlertAssetsEnum.Zero.toString());
        if (null != reply) {
            assetsInfos = (List<VHostTreeDTO>) reply.getData();
            for(VHostTreeDTO s: assetsInfos){
                List<GroupHosts> hostIdList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(s.getHostList())){
                    hostIdList.addAll(s.getHostList());
                }
                if(CollectionUtils.isNotEmpty(s.getVmList())){
                    hostIdList.addAll(s.getVmList());
                }
                /*if(CollectionUtils.isNotEmpty(s.getStoreList())){
                    hostIdList.addAll(s.getStoreList());
                }*/
                for(GroupHosts gs : hostIdList){
                    if(gs.getHostid().equals(hostid)){
                        return gs;
                        /*List<String> typeIds = mwAlertActionDao.selectTypeIdMapper(hostid);
                        for(String ti : typeIds){
                            if(ti.contains(hostip)){
                                return true;
                            }
                        }*/
                    }
                }
            }

        }
        return null;
    }


}
