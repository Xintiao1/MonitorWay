package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.alert.dto.*;
import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.alert.service.MWAlertActionService;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.action.param.UserIdsType;
import cn.mw.monitor.service.action.service.CommonActionService;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsByIdDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.dto.UserGroupDTO;
import cn.mw.monitor.user.service.MWGroupService;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/8/26 9:17
 */
@Service
@Slf4j
public class MWAlertActionServiceImpl implements MWAlertActionService, CommonActionService {
    private static final Logger logger = LoggerFactory.getLogger(MWAlertActionServiceImpl.class);


    @Autowired
    private MWCommonService mwCommonService;
    @Resource
    private MwAlertActionDao mwAlertActionDao;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    MWGroupCommonService mwGroupCommonService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWOrgCommonService mwOrgService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWUserService mwUserService;

    @Autowired
    private MWGroupService mWGroupService;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    public void insertAndEditorAction(AddAndUpdateAlertActionParam param) {
        String loginName = iLoginCacheInfo.getLoginName();
        String actionId = param.getActionId();
        List<String> assetsIds = new ArrayList<>();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        MwCommonAssetsDto mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).isAlert(true).build();
        List<String> ruleIds = param.getRuleIds();
        List<ActionRuleMapper> actionRuleMappers = new ArrayList<>();
        ruleIds.forEach(ruleId -> {
                    ActionRuleMapper actionRuleMapper = ActionRuleMapper.builder().ActionId(actionId).ruleId(ruleId).build();
                    actionRuleMappers.add(actionRuleMapper);
                }
        );
        mwAlertActionDao.insertActionRuleMapper(actionRuleMappers);

        //插入数据到 mw_alert_action_label_mapper
        if (null != param.getLabel()) {
            param.getLabel().setActionId(param.getActionId());
            if (null != param.getLabel().getLabelTimeValue() && param.getLabel().getLabelTimeValue().size() > 0) {
                param.getLabel().setLabelDateStart(param.getLabel().getLabelTimeValue().get(0));
                param.getLabel().setLabelDateEnd(param.getLabel().getLabelTimeValue().get(1));
            }
            mwAlertActionDao.insertActionLabelMapper(param.getLabel());
        }

        //插入数据到 mw_alert_action_type_mapper
        List<Integer> actionTypeIds = param.getActionTypeIds();
        List<ActionTypeMapper> actionTypeMappers = new ArrayList<>();
        if (null != actionTypeIds && actionTypeIds.size() > 0) {
            for (Integer actionTypeId : actionTypeIds) {
                ActionTypeMapper actionTypeMapper = ActionTypeMapper.builder().actionId(actionId).actionTypeId(actionTypeId).build();
                actionTypeMappers.add(actionTypeMapper);
            }
            mwAlertActionDao.insertActionTypeMapper(actionTypeMappers);
        }
        //插入数据到mw_alert_action_level_rule
        List<Integer> levelTwoUserIds = param.getLevelTwoUserIds();
        List<Integer> levelThreeUserIds = param.getLevelThreeUserIds();
        Integer state = param.getState();
        float date = param.getDate();
        ActionLevelRule actionLevelRules = ActionLevelRule.builder().actionId(actionId).state(state).date(date).build();
        mwAlertActionDao.insertActionLeveRule(actionLevelRules);

        //插入数据到 mw_alert_action_level_user_mapper
        List<ActionLevelRule> actionLevelRuleList = new ArrayList<>();
        if (null != levelTwoUserIds && levelTwoUserIds.size() > 0) {
            for (Integer levelTwoUserId : levelTwoUserIds) {
                ActionLevelRule actionLevelRule = ActionLevelRule.builder().actionId(actionId).userId(levelTwoUserId).level(2).build();
                actionLevelRuleList.add(actionLevelRule);
            }
            for (Integer levelThreeUserId : levelThreeUserIds) {
                ActionLevelRule actionLevelRule = ActionLevelRule.builder().actionId(actionId).userId(levelThreeUserId).level(3).build();
                actionLevelRuleList.add(actionLevelRule);
            }
            mwAlertActionDao.insertActionLeveUserMapper(actionLevelRuleList);
        }

        //判断是否是当前用户所有默认可以选择的资产
//        if (param.getIsAllAssets()) {
//            mwCommonAssetsDto.setAssetsTypeId(0);
//        } else {
//            /**查询资产过滤查询到的所有资产
//             *
//             */
//            //添加标签查询
//            if (null != param.getLabel() && null != param.getLabel().getLabelId() && null != param.getLabel().getLabelValue() && StringUtils.isNotEmpty(param.getLabel().getLabelValue())) {
//                mwCommonAssetsDto.setLabelId(param.getLabel().getLabelId());
//                mwCommonAssetsDto.setInputFormat(param.getLabel().getInputFormat());
//                if (param.getLabel().getInputFormat() == 1) {
//                    mwCommonAssetsDto.setLabelValue(param.getLabel().getLabelValue());
//                } else if (param.getLabel().getInputFormat() == 2) {
//                    mwCommonAssetsDto.setLabelDateStart(param.getLabel().getLabelTimeValue().get(0));
//                    mwCommonAssetsDto.setLabelDateEnd(param.getLabel().getLabelTimeValue().get(1));
//                } else if (param.getLabel().getInputFormat() == 3) {
//                    mwCommonAssetsDto.setDropKey(param.getLabel().getDropKey());
//                }
//            }
//            //资产过滤查询
//            if (null != param.getAssetsName() && StringUtils.isNotEmpty(param.getAssetsName())) {
//                mwCommonAssetsDto.setAssetsName(param.getAssetsName());
//            }
//            if (null != param.getInBandIp() && StringUtils.isNotEmpty(param.getInBandIp())) {
//                mwCommonAssetsDto.setInBandIp(param.getInBandIp());
//            }
//            if (null != param.getSpecifications() && StringUtils.isNotEmpty(param.getSpecifications())) {
//                mwCommonAssetsDto.setSpecifications(param.getSpecifications());
//            }
//            if (null != param.getManufacturer() && StringUtils.isNotEmpty(param.getManufacturer())) {
//                mwCommonAssetsDto.setManufacturer(param.getManufacturer());
//            }
//            if (null != param.getAssetsTypeId()) {
//                mwCommonAssetsDto.setAssetsTypeId(param.getAssetsTypeId());
//            }
//            if (null != param.getAssetsTypeSubId()) {
//                mwCommonAssetsDto.setAssetsTypeId(param.getAssetsTypeSubId());
//            }
//            if (null != param.getPollingEngine()) {
//                mwCommonAssetsDto.setPollingEngine(param.getPollingEngine());
//            }
//            if (null != param.getMonitorMode()) {
//                mwCommonAssetsDto.setMonitorMode(param.getMonitorMode());
//            }
//
//        }
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        if (null != map) {
            assetsIds = (List<String>) map.get("assetIds");
        }
        //插入数据到 mw_alert_serverity_mapper
        List<String> severitys = param.getSeverity();
        if (null != severitys && severitys.size() > 0) {
            List<ActionSeverityMapper> actionSeverityMappers = new ArrayList<>();
            for (String severity : severitys) {
                ActionSeverityMapper actionSeverityMapper = ActionSeverityMapper.builder().actionId(actionId).severity(severity).build();
                actionSeverityMappers.add(actionSeverityMapper);
            }
            mwAlertActionDao.insertSeverityMapper(actionSeverityMappers);
        }

//        List<ActionSeverityMapper> actionSeverityMappers = new ArrayList<>();
//        ActionSeverityMapper actionSeverityMapper = ActionSeverityMapper.builder().actionId(actionId).severity(param.getSeverity()).build();
//        actionSeverityMappers.add(actionSeverityMapper);
//
//        mwAlertActionDao.insertSeverityMapper(actionSeverityMappers);

        //插入数据到 mw_alert_action_assets_mapper
        if (assetsIds.size() > 0) {
            List<ActionAssetsMapper> actionAssetsMappers = new ArrayList<>();
            for (String assetsId : assetsIds) {
                ActionAssetsMapper actionAssetsMapper = ActionAssetsMapper.builder().actionId(actionId).assetsId(assetsId).build();
                actionAssetsMappers.add(actionAssetsMapper);
            }

            mwAlertActionDao.insertAssetsMapper(actionAssetsMappers);
        }

        List<Integer> userIds = new ArrayList<>();
        //默认用户 如果是私有权限就只发送给自己和用户组中的人，如果是公有权限就发送给该机构和所在用户组的所有人
        List<ActionUserMapper> actionUserMappers = new ArrayList<>();

        List<Integer> groupIds = new ArrayList<>();
        //如果是非默认用户
        if (param.getIsAllUser() == 0) {
//
//            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
//            String dataPermission = DataPermission.valueOf(perm).name();
//            //根据用户id查询用户组的所有用户
//            List<Integer> groupUserIds = mwUserGroupMapperDao.selectGroupUserIdByUserId(userId);
//            Set<Integer> setUserIds = new HashSet<>();
//
////                    String roleId = mwUserOrgMapperDao.getRoleIdByLoginName(loginName);
//            //  if (!roleId.equals(MWUtils.ROLE_TOP_ID)) {
//            //公有权限
//            if (dataPermission.equals("PUBLIC")) {
//                List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
//                List<Integer> orgUserIds = mwUserOrgMapperDao.getUserIdByOrgId(nodes, userId);
//
//                if (orgUserIds.size() > 0) {
//                    orgUserIds.forEach(orgUserId -> {
//                        setUserIds.add(orgUserId);
//                    });
//                }
//                //私有权限
//            } else {
//                setUserIds.add(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
//            }
//            if (setUserIds.size() > 0) {
//                groupUserIds.forEach(groupUserId -> {
//                    setUserIds.add(groupUserId);
//                });
//            }
//            if (setUserIds.size() > 0) {
//                setUserIds.forEach(userid -> {
//                    ActionUserMapper actionUserMapper = ActionUserMapper.builder().actionId(actionId).userId(userid).build();
//                    actionUserMappers.add(actionUserMapper);
//                });
//            }
//
//        } else
//            {
            userIds = param.getActionUserIds();
            if (userIds.size() > 0) {
                userIds.forEach(userid -> {
                    ActionUserMapper actionUserMapper = ActionUserMapper.builder().actionId(actionId).userId(userid).build();
                    actionUserMappers.add(actionUserMapper);
                });
            }
            List<ActionGroupMapper> actionGroupMappers = new ArrayList<>();
            if(param.getActionGroupIds().size() > 0){
                groupIds = param.getActionGroupIds();
                groupIds.forEach(groupId ->{
                    ActionGroupMapper actionGroupMapper =  ActionGroupMapper.builder().actionId(actionId).groupId(groupId).build();
                    actionGroupMappers.add(actionGroupMapper);
                });
                mwAlertActionDao.insertActionGroupsMapper(actionGroupMappers);
            }
        }


        if (actionUserMappers.size() > 0) {
            mwAlertActionDao.insertActionUsersMapper(actionUserMappers);
        }

    }

    @Override
    @Transactional
    public Reply deleteAction(List<AddAndUpdateAlertActionParam> lists) {
        try {
            List<String> actionIds = new ArrayList<>();
            for (AddAndUpdateAlertActionParam list : lists) {
                actionIds.add(list.getActionId());
            }
            mwAlertActionDao.deleteAction(actionIds);
            //  List<String> assetsList = mwAlertActionDao.getAssetsByActionIds(actionIds);

            mwAlertActionDao.deleteActionAssetsMappers(actionIds);
            mwAlertActionDao.deleteActionUsersMappers(actionIds);
            mwAlertActionDao.deleteActionRulesMappers(actionIds);
            mwAlertActionDao.deleteActionTypesMappers(actionIds);
            mwAlertActionDao.deleteActionServerityMappers(actionIds);
            mwAlertActionDao.deleteActionLabelMappers(actionIds);
            mwAlertActionDao.deleteActionLevelRules(actionIds);
            mwAlertActionDao.deleteActionLevelEventMappers(actionIds);
            mwAlertActionDao.deleteActionLevelUserMappers(actionIds);
            mwAlertActionDao.deleteActionAssetsclumnMappers(actionIds);
            mwAlertActionDao.deleteActionLeveRuleMappers(actionIds);
            mwAlertActionDao.deleteMwAlertRuleSelects(actionIds);
            mwAlertActionDao.deleteActionLevelEmailMappers(actionIds);
            mwAlertActionDao.deleteActionUserTypeMappers(actionIds);
            DeleteDto deleteDto = DeleteDto.builder().typeIds(actionIds).type(DataType.ACTION.getName()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);

            return Reply.ok();

        } catch (Exception e) {
//            log.error("fail to deleteAction with param={}, cause:{}", lists, e.getMessage());
//            return Reply.fail(ErrorConstant.ALERT_ACTION_DELETE_CODE_300013, ErrorConstant.ALERT_ACTION_DELETE_MAG_300013);

            throw new RuntimeException("fail to deleteAction with param={}, cause:{}");

        }
    }

    @Override
    public Reply selectAction(AlertActionParam param) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> idList = mwUserService.getAllTypeIdList(userInfo,DataType.ACTION);
            Map pubCriteria = PropertyUtils.describe(param);
            pubCriteria.put("isSystem",userInfo.isSystemUser());
            if(CollectionUtils.isNotEmpty(idList)){
                if(idList.get(0) != null){
                    pubCriteria.put("listSet", Joiner.on(",").join(idList));

                }
            }
            List<AlertActionTable> list = new ArrayList<>();
            list = mwAlertActionDao.selectActionList(pubCriteria);
            if(CollectionUtils.isNotEmpty(list)){
                for(AlertActionTable alr : list){
                    DataPermission dataPermission = mwCommonService.getDataPermissionDetail(DataType.ACTION,alr.getActionId());
                    alr.setPrincipal(dataPermission.getPrincipal());
                }
            }

            /*String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != groupIds && groupIds.size() > 0) {
                param.setGroupIds(groupIds);
            }
            switch (dataPermission) {
                case PRIVATE:
                    param.setUserId(userId);
                    PageHelper.startPage(param.getPageNumber(), param.getPageSize());
                    Map priCriteria = PropertyUtils.describe(param);
                    list = mwAlertActionDao.selectPriActionList(priCriteria);
                    break;
                case PUBLIC:
                    String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                    List<Integer> orgIds = new ArrayList<>();
                    Boolean isAdmin = false;
                    if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                        isAdmin = true;
                    }
                    *//*if (!isAdmin) {
                        //List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                        //orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);
                        orgIds = mwOrgService.getOrgIdsByNodes(loginName);

                    }
                    if (null != orgIds && orgIds.size() > 0) {
                        param.setOrgIds(orgIds);
                    }*//*
                    param.setIsAdmin(isAdmin);
                    PageHelper.startPage(param.getPageNumber(), param.getPageSize());
                    Map pubCriteria = PropertyUtils.describe(param);
                    list = mwAlertActionDao.selectPubActionList(pubCriteria);
                    break;
            }*/
            PageList pageList = new PageList();
            List newList = pageList.getList(list, param.getPageNumber(), param.getPageSize());
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(param.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setList(newList);
            logger.info("ACCESS_LOG[]zbx_action[]告警管理[]分页查询告警通知[]{}[]", param);
            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to selectAction with param={}, cause:{}", param, e.getMessage());
            return Reply.fail(ErrorConstant.ALERT_ACTION_BROWSE_CODE_300011, ErrorConstant.ALERT_ACTION_BROWSE_MAG_300011);

        }
    }

    @Override
    public UserIdsType getActionUserIds(String actionId, String id){
        return getActionUserIds(actionId,id,null);
    }

    @Override
    public UserIdsType getActionUserIds(String actionId, String id,MwTangibleassetsDTO assets) {
        UserIdsType userIdsType = new UserIdsType();
        AddAndUpdateAlertActionParam param = selectPopupAction(actionId);
        log.info("getActionUserIds param：" + param);
        log.info("getActionUserIds param：" + param.getActionUserIds());


        if (param.getIsAllUser() == 1 && StringUtils.isNotBlank(id)) {
            if(modelAssetEnable && assets != null){
                userIdsType.setEmailUserIds(assets.getModelViewUserIds());
                if(CollectionUtils.isNotEmpty(assets.getModelViewGroupIds())){
                    Reply reply = mWGroupService.getUserListByGroupIds(assets.getModelViewGroupIds());
                    if (reply.getRes() == 0) {
                        List<Integer> groupUserIds = new ArrayList<>();
                        Map<Integer, List<UserGroupDTO>> result = (Map<Integer, List<UserGroupDTO>>) reply.getData();
                        for (Integer groupId : assets.getModelViewGroupIds()){
                            List<UserGroupDTO> temp = result.get(groupId);
                            List<Integer> userTemp = temp.stream()
                                    .filter(m -> !groupUserIds.contains(m.getUserId()))
                                    .map(m -> m.getUserId()).
                                    collect(Collectors.toList());
                            groupUserIds.addAll(userTemp);
                        }
                        userIdsType.setEmailGroupUserIds(groupUserIds);
                    }
                }

            }
            //默认用户，查询资产的用户
            Reply reply = mwModelViewCommonService.selectById(id,false);
            if (reply.getRes() == 0) {
                MwTangibleassetsByIdDTO mtDtos = (MwTangibleassetsByIdDTO) reply.getData();
                log.info("资产mtDtos：" + mtDtos);
                if(mtDtos != null){
                    //资产负责人
                    if(CollectionUtils.isEmpty(param.getUserTypes()) || param.getUserTypes().contains(UserTypeEnum.USER.getName())){
                        HashSet<Integer> userIds = new HashSet<>();
                        List<Integer> principal = mtDtos.getPrincipal();
                        if (null != principal && principal.size() > 0) {
                            for (Integer pri : principal) {
                                userIds.add(pri);
                            }
                        }
                        log.info("资产负责人" + userIds);
                        userIdsType.setPersonUserIds(userIds);
                    }
                    //查询用户组中所有用户
                    if(CollectionUtils.isEmpty(param.getUserTypes()) || param.getUserTypes().contains(UserTypeEnum.GROUP.getName())){
                        List<Integer> groupIds = mtDtos.getGroupIds();
                        if (null != groupIds && groupIds.size() > 0) {
                            HashSet<Integer> groupUserIds = new HashSet<>();
                            for (Integer groupid : groupIds) {
                                Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
                                if (selectGroupUser.getRes() == 0) {
                                    List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                                    if (null != groupUserData && groupUserData.size() > 0) {
                                        for (GroupUserDTO pri : groupUserData) {
                                            groupUserIds.add(pri.getUserId());
                                        }
                                    }
                                }
                            }
                            log.info("查询用户组中所有用户" + groupUserIds);
                            userIdsType.setGroupUserIds(groupUserIds);
                        }

                    }
                    //查询机构中所有的公有权限用户
                    if(CollectionUtils.isEmpty(param.getUserTypes()) || param.getUserTypes().contains(UserTypeEnum.ORG.getName())){
                        List<List<Integer>> orgIdsList = mtDtos.getOrgIds();
                        if(CollectionUtils.isNotEmpty(orgIdsList)){
                            HashSet<Integer> orgUserIds = new HashSet<>();
                            List<Integer> orgIds = new ArrayList<>();
                            for(List<Integer> ids : orgIdsList){
                                orgIds.add(ids.get(ids.size()-1));
                            }
                            if (orgIds.size() > 0) {
                                List<Integer> orgUserIDs = mwOrgService.selectPubUserIdByOrgId(orgIds);
                                if (null != orgUserIDs && orgUserIDs.size() > 0) {
                                    for (Integer pri : orgUserIDs) {
                                        orgUserIds.add(pri);
                                    }
                                }
                            }
                            log.info("查询机构中所有的公有权限用户" + orgUserIds);
                            userIdsType.setOrgUserIds(orgUserIds);
                        }
                    }
                }
            }
        }else if(param.getIsAllUser() == 0){
            HashSet<Integer> userIds = mwAlertActionDao.selectActionUsersMapper(actionId);
            if(CollectionUtils.isNotEmpty(userIds)){
                userIdsType.setPersonUserIds(userIds);
            }
            //暂时性修改
            userIdsType.setEmailUserIds(param.getActionUserIds());
            log.info("getIsAllUser 用户" + userIds);
            List<Integer> groupIds = mwAlertActionDao.selectActionGroupsMapper(actionId);
            //查询用户组中所有用户
            if (null != groupIds && groupIds.size() > 0) {
                HashSet<Integer> groupUserIds = new HashSet<>();
                HashSet<Integer> groupIdset = new HashSet<>();
                for (Integer groupid : groupIds) {
                    Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
                    if (selectGroupUser.getRes() == 0) {
                        List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                        if (null != groupUserData && groupUserData.size() > 0) {
                            for (GroupUserDTO pri : groupUserData) {
                                groupUserIds.add(pri.getUserId());
                            }
                        }
                    }
                }
                groupIdset.addAll(groupIds);
                userIdsType.setGroupIds(groupIdset);
                userIdsType.setGroupUserIds(groupUserIds);
                log.info("getIsAllUser查询用户组中所有用户" + groupUserIds);
            }
        }
        return userIdsType;
    }

    @Override
    public UserIdsType getOutbandUserIds(String actionId, String id) {
        UserIdsType userIdsType = new UserIdsType();
        AddAndUpdateAlertActionParam param = selectPopupAction(actionId);
        log.info("getActionUserIds param：" + param);

        if (param.getIsAllUser() == 1 && StringUtils.isNotBlank(id)) {
            //默认用户，查询资产的用户
            List<String> outIds = new ArrayList<>();
            outIds.add(id);
            List<cn.mw.monitor.bean.DataPermission> dataAuthByIds = mwCommonService.getDataAuthByIds(DataType.OUTBANDASSETS, outIds);
            if(CollectionUtils.isNotEmpty(dataAuthByIds)){
                //资产负责人
                if(CollectionUtils.isEmpty(param.getUserTypes()) || param.getUserTypes().contains(UserTypeEnum.USER.getName())){
                    HashSet<Integer> userIds = new HashSet<>();
                    List<Integer> principal = dataAuthByIds.get(0).getUserIds();
                    if (null != principal && principal.size() > 0) {
                        for (Integer pri : principal) {
                            userIds.add(pri);
                        }
                    }
                    log.info("带外资产负责人" + userIds);
                    userIdsType.setPersonUserIds(userIds);
                }
                //查询用户组中所有用户
                if(CollectionUtils.isEmpty(param.getUserTypes()) || param.getUserTypes().contains(UserTypeEnum.GROUP.getName())){
                    List<Integer> groupIds = dataAuthByIds.get(0).getGroupIds();
                    if (null != groupIds && groupIds.size() > 0) {
                        HashSet<Integer> groupUserIds = new HashSet<>();
                        for (Integer groupid : groupIds) {
                            Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
                            if (selectGroupUser.getRes() == 0) {
                                List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                                if (null != groupUserData && groupUserData.size() > 0) {
                                    for (GroupUserDTO pri : groupUserData) {
                                        groupUserIds.add(pri.getUserId());
                                    }
                                }
                            }
                        }
                        log.info("带外资产查询用户组中所有用户" + groupUserIds);
                        userIdsType.setGroupUserIds(groupUserIds);
                    }

                }
                //查询机构中所有的公有权限用户
                if(CollectionUtils.isEmpty(param.getUserTypes()) || param.getUserTypes().contains(UserTypeEnum.ORG.getName())){
                    List<List<Integer>> orgIdsList = dataAuthByIds.get(0).getOrgNodes();
                    if(CollectionUtils.isNotEmpty(orgIdsList)){
                        HashSet<Integer> orgUserIds = new HashSet<>();
                        List<Integer> orgIds = new ArrayList<>();
                        for(List<Integer> ids : orgIdsList){
                            orgIds.add(ids.get(ids.size()-1));
                        }
                        if (orgIds.size() > 0) {
                            List<Integer> orgUserIDs = mwOrgService.selectPubUserIdByOrgId(orgIds);
                            if (null != orgUserIDs && orgUserIDs.size() > 0) {
                                for (Integer pri : orgUserIDs) {
                                    orgUserIds.add(pri);
                                }
                            }
                        }
                        log.info("带外资产查询机构中所有的公有权限用户" + orgUserIds);
                        userIdsType.setOrgUserIds(orgUserIds);
                    }
                }
            }
        }else if(param.getIsAllUser() == 0){
            HashSet<Integer> userIds = mwAlertActionDao.selectActionUsersMapper(actionId);
            if(CollectionUtils.isNotEmpty(userIds)){
                userIdsType.setPersonUserIds(userIds);
            }

            log.info("getIsAllUser 用户" + userIds);
            List<Integer> groupIds = mwAlertActionDao.selectActionGroupsMapper(actionId);
            //查询用户组中所有用户
            if (null != groupIds && groupIds.size() > 0) {
                HashSet<Integer> groupUserIds = new HashSet<>();
                HashSet<Integer> groupIdset = new HashSet<>();
                for (Integer groupid : groupIds) {
                    Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
                    if (selectGroupUser.getRes() == 0) {
                        List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                        if (null != groupUserData && groupUserData.size() > 0) {
                            for (GroupUserDTO pri : groupUserData) {
                                groupUserIds.add(pri.getUserId());
                            }
                        }
                    }
                }
                groupIdset.addAll(groupIds);
                userIdsType.setGroupIds(groupIdset);
                userIdsType.setGroupUserIds(groupUserIds);
                log.info("getIsAllUser查询用户组中所有用户" + groupUserIds);
            }
        }
        return userIdsType;
    }

    @Override
    public HashSet<Integer> getActionEamilCCUserIds(String actionId) {
        HashSet<Integer> userIds = new HashSet<>();
        userIds = mwAlertActionDao.selectActionUsersMapper(actionId);
        List<Integer> groupIds = mwAlertActionDao.selectActionGroupsMapper(actionId);
        //查询用户组中所有用户
        if (null != groupIds && groupIds.size() > 0) {
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
        }
        return userIds;
    }

    @Override
    public HashSet<Integer> getVrUserIds(String actionId, String hostid) {
        HashSet<Integer> userIds = new HashSet<>();
        userIds = mwAlertActionDao.selectFuzzUserMapper(hostid + "_");
        List<Integer>  groupIds = mwAlertActionDao.selectFuzzGroupMapper(hostid + "_");
        if(groupIds != null && groupIds.size() > 0){
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
        }
        List<Integer>  orgIds = mwAlertActionDao.selectFuzzOrgMapper(hostid + "_");
        if (orgIds != null && orgIds.size() > 0) {
            List<Integer> orgUserIDs = mwOrgService.selectPubUserIdByOrgId(orgIds);
            if (null != orgUserIDs && orgUserIDs.size() > 0) {
                for (Integer pri : orgUserIDs) {
                    userIds.add(pri);
                }
            }
        }
        return userIds;
    }

    /**
     * 查询规则要发送的资产
     *
     * @param actionId
     * @return
     */
    @Override
    public List<String> getActionAssetsIds(String actionId) {
        //   Integer userId = iLoginCacheInfo.getCacheInfo("admin").getUserId();
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setIsAdmin(true);
        //AddAndUpdateAlertActionParam param = mwAlertActionDao.selectPopupAction(actionId);
        //判断是否是当前用户所有默认可以选择的资产
        mwCommonAssetsDto.setAssetsTypeId(0);
        /*if (param.getIsAllAssets()) {
            mwCommonAssetsDto.setAssetsTypeId(0);
        } else {
            *//**查询资产过滤查询到的所有资产
             *
             *//*
            //添加标签查询
            if (null != param.getLabel() && null != param.getLabel().getLabelId() && null != param.getLabel().getLabelValue() && StringUtils.isNotEmpty(param.getLabel().getLabelValue())) {
                mwCommonAssetsDto.setLabelId(param.getLabel().getLabelId());
                mwCommonAssetsDto.setInputFormat(param.getLabel().getInputFormat());
                if (param.getLabel().getInputFormat() == 1) {
                    mwCommonAssetsDto.setLabelValue(param.getLabel().getLabelValue());
                } else if (param.getLabel().getInputFormat() == 2) {
                    mwCommonAssetsDto.setLabelDateStart(param.getLabel().getLabelTimeValue().get(0));
                    mwCommonAssetsDto.setLabelDateEnd(param.getLabel().getLabelTimeValue().get(1));
                } else if (param.getLabel().getInputFormat() == 3) {
                    mwCommonAssetsDto.setDropKey(param.getLabel().getDropKey());
                }
            }
            //资产过滤查询
            if (null != param.getAssetsName() && StringUtils.isNotEmpty(param.getAssetsName())) {
                mwCommonAssetsDto.setAssetsName(param.getAssetsName());
            }
            if (null != param.getInBandIp() && StringUtils.isNotEmpty(param.getInBandIp())) {
                mwCommonAssetsDto.setInBandIp(param.getInBandIp());
            }
            if (null != param.getSpecifications() && StringUtils.isNotEmpty(param.getSpecifications())) {
                mwCommonAssetsDto.setSpecifications(param.getSpecifications());
            }
            if (null != param.getManufacturer() && StringUtils.isNotEmpty(param.getManufacturer())) {
                mwCommonAssetsDto.setManufacturer(param.getManufacturer());
            }
            if (null != param.getAssetsTypeId()) {
                mwCommonAssetsDto.setAssetsTypeId(param.getAssetsTypeId());
            }
            if (null != param.getAssetsTypeSubId()) {
                mwCommonAssetsDto.setAssetsTypeId(param.getAssetsTypeSubId());
            }
            if (null != param.getPollingEngine()) {
                mwCommonAssetsDto.setPollingEngine(param.getPollingEngine());
            }
            if (null != param.getMonitorMode()) {
                mwCommonAssetsDto.setMonitorMode(param.getMonitorMode());
            }

        }*/
        //  Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        List<String> ids = mwAssetsManager.getAssetsByAction(mwCommonAssetsDto);
        return ids;
    }

    /**
     * 通过资产表id查询对应资产能够匹配的规则
     *
     * @param id
     * @return
     */
    @Override
    public List<String> getActionByHostId(String id) {
        //查询所有的告警动作规则
        AlertActionParam param = new AlertActionParam();
        param.setIsAdmin(true);
        Map pubCriteria = null;
        List<String> actionIds = new ArrayList<>();
        try {
            pubCriteria = PropertyUtils.describe(param);
            List<AlertActionTable> alertActionTables = mwAlertActionDao.selectPubActionList(pubCriteria);
            if (null != alertActionTables && alertActionTables.size() > 0) {
                for (AlertActionTable alertActionTable : alertActionTables) {
                    List<String> ids = getActionAssetsIds(alertActionTable.getActionId());
                    if (null!=ids&&ids.size()>0&&ids.contains(id)) {
                        actionIds.add(alertActionTable.getActionId());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("getActionByHostId{}", e);
        }

        return actionIds;
    }

    @Override
    public Reply getFielid(){
        List<AssetsFielidParam> fielids = mwAlertActionDao.getAssetsFielid();
        List<AssetsFielidParam> result = new ArrayList<>();
        AssetsFielidParam param = new AssetsFielidParam();
        param.setClumnName("default");
        param.setClumnComent("默认选择");
        result.add(param);
        for (AssetsFielidParam fielid : fielids) {
            if(fielid.getClumnComent() != null && !fielid.getClumnComent().equals("")){
                result.add(fielid);
            }
        }
        return Reply.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reply addAction(MwRuleSelectListParam param){
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            param.setCreator(loginName);
            param.setModifier(loginName);
            String actionId = UuidUtil.getUid();
            param.setActionId(actionId);
            mwAlertActionDao.addAction(param);
            addAndEditorAction(param);
            mwCommonService.addMapperAndPerm(param);
            log.info("ACTION_LOG[]告警规则[]新增告警规则[]{}insertRule[]param{}", param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to insertAction with param={}, cause:{}", e.getMessage());
//            return Reply.fail(ErrorConstant.ALERT_ACTION_INSERT_CODE_300010, ErrorConstant.ALERT_ACTION_INSERT_MAG_300010);
            throw new RuntimeException("fail to insertAction with param={}, cause:{}" + e.getMessage());

        }
    }

    public void addAndEditorAction(MwRuleSelectListParam param) {
        String actionId = param.getActionId();
        List<String> ruleIds = param.getRuleIds();
        List<ActionRuleMapper> actionRuleMappers = new ArrayList<>();
        ruleIds.forEach(ruleId -> {
                    ActionRuleMapper actionRuleMapper = ActionRuleMapper.builder().ActionId(actionId).ruleId(ruleId).build();
                    actionRuleMappers.add(actionRuleMapper);
                }
        );
        mwAlertActionDao.insertActionRuleMapper(actionRuleMappers);

        //插入数据到 mw_alert_action_level_rule
        List<Integer> levelOneUserIds = param.getLevelOneUserIds();
        List<Integer> levelTwoUserIds = param.getLevelTwoUserIds();
        List<Integer> levelThreeUserIds = param.getLevelThreeUserIds();
        Integer state = param.getState();
        Integer level = param.getLevel();
        float levelOnedate = param.getLevelOneDate();
        float levelTwodate = param.getLevelTwoDate();
        float levelThreedate = param.getLevelThreeDate();
        ActionLevelRule actionLevelRule = ActionLevelRule.builder().actionId(actionId).state(state).date(levelOnedate).dateTwo(levelTwodate).dateThree(levelThreedate).level(level).build();
        mwAlertActionDao.insertActionLeveRule(actionLevelRule);

        //插入数据到 mw_alert_action_level_rule_mapper
        List<String> levelOneRuleIds = param.getLevelOneRuleIds();
        List<String> levelTwoRuleIds = param.getLevelTwoRuleIds();
        List<String> levelThreeRuleIds = param.getLevelThreeRuleIds();
        Integer oneTime = param.getOneTime();
        Integer twoTime = param.getTwoTime();
        Integer threeTime = param.getThreeTime();
        if(levelOneRuleIds != null && levelOneRuleIds.size() > 0){
            List<ActionLevelRule> actionLevelRules = new ArrayList<>();
            for(String levelOneRuleId : levelOneRuleIds){
                ActionLevelRule temp = ActionLevelRule.builder().actionId(param.getActionId()).level(1).ruleId(levelOneRuleId).timeUnit(oneTime).isSendPerson(param.getIsSendPersonOne()).build();
                actionLevelRules.add(temp);
            }
            mwAlertActionDao.insertActionLeveRuleMapper(actionLevelRules);
        }
        if(levelTwoRuleIds != null && levelTwoRuleIds.size() > 0){
            List<ActionLevelRule> actionLevelRules = new ArrayList<>();
            for(String levelTwoRuleId : levelTwoRuleIds){
                ActionLevelRule temp = ActionLevelRule.builder().actionId(param.getActionId()).level(2).ruleId(levelTwoRuleId).timeUnit(twoTime).isSendPerson(param.getIsSendPersonTwo()).build();
                actionLevelRules.add(temp);
            }
            mwAlertActionDao.insertActionLeveRuleMapper(actionLevelRules);
        }
        if(levelThreeRuleIds != null && levelThreeRuleIds.size() > 0){
            List<ActionLevelRule> actionLevelRules = new ArrayList<>();
            for(String levelThreeRuleId : levelThreeRuleIds){
                ActionLevelRule temp = ActionLevelRule.builder().actionId(param.getActionId()).level(3).ruleId(levelThreeRuleId).timeUnit(threeTime).isSendPerson(param.getIsSendPersonThree()).build();
                actionLevelRules.add(temp);
            }
            mwAlertActionDao.insertActionLeveRuleMapper(actionLevelRules);
        }

        //插入数据到 mw_alert_action_level_user_mapper
        if (null != levelTwoUserIds && levelTwoUserIds.size() > 0) {
            List<ActionLevelRule> actionLevelRuleList = new ArrayList<>();
            for (Integer levelTwoUserId : levelTwoUserIds) {
                ActionLevelRule temp = ActionLevelRule.builder().actionId(actionId).userId(levelTwoUserId).level(2).isAllUser(param.getLevelTwoIsAllUser()).email(param.getLevelTwoEmail()).build();
                actionLevelRuleList.add(temp);
            }
            mwAlertActionDao.insertActionLeveUserMapper(actionLevelRuleList);
        }
        if (null != levelThreeUserIds && levelThreeUserIds.size() > 0) {
            List<ActionLevelRule> actionLevelRuleList = new ArrayList<>();
            for (Integer levelThreeUserId : levelThreeUserIds) {
                ActionLevelRule temp = ActionLevelRule.builder().actionId(actionId).userId(levelThreeUserId).level(3).isAllUser(param.getLevelThreeIsAllUser()).email(param.getLevelThreeEmail()).build();
                actionLevelRuleList.add(temp);
            }
            mwAlertActionDao.insertActionLeveUserMapper(actionLevelRuleList);
        }
        if (null != levelOneUserIds && levelOneUserIds.size() > 0) {
            List<ActionLevelRule> actionLevelRuleList = new ArrayList<>();
            for (Integer levelOneUserId: levelOneUserIds) {
                ActionLevelRule temp = ActionLevelRule.builder().actionId(actionId).userId(levelOneUserId).level(1).isAllUser(param.getLevelOneIsAllUser()).email(param.getLevelOneEmail()).build();
                actionLevelRuleList.add(temp);
            }
            mwAlertActionDao.insertActionLeveUserMapper(actionLevelRuleList);
        }
        //插入mw_alert_action_level_rule_email_mapper
        List<ActionLevelRule> levelRuleList = new ArrayList<>();
        if(param.getLevelOneIsAllUser() != null){
            ActionLevelRule temp = ActionLevelRule.builder().actionId(actionId).isAllUser(param.getLevelOneIsAllUser()).email(param.getLevelOneEmail()).level(1).build();
            levelRuleList.add(temp);
        }
        if(param.getLevelTwoIsAllUser() != null){
            ActionLevelRule temp = ActionLevelRule.builder().actionId(actionId).isAllUser(param.getLevelTwoIsAllUser()).email(param.getLevelTwoEmail()).level(2).build();
            levelRuleList.add(temp);
        }
        if(param.getLevelThreeIsAllUser() != null){
            ActionLevelRule temp = ActionLevelRule.builder().actionId(actionId).isAllUser(param.getLevelThreeIsAllUser()).email(param.getLevelThreeEmail()).level(3).build();
            levelRuleList.add(temp);
        }
        if(CollectionUtils.isNotEmpty(levelRuleList)){
            mwAlertActionDao.insertActionLeveEmailMapper(levelRuleList);
        }
        //插入数据到 mw_alert_serverity_mapper
        List<String> severitys = param.getSeverity();
        if (null != severitys && severitys.size() > 0) {
            List<ActionSeverityMapper> actionSeverityMappers = new ArrayList<>();
            for (String severity : severitys) {
                ActionSeverityMapper actionSeverityMapper = ActionSeverityMapper.builder().actionId(actionId).severity(severity).build();
                actionSeverityMappers.add(actionSeverityMapper);
            }
            mwAlertActionDao.insertSeverityMapper(actionSeverityMappers);
        }
        //插入数据到 mw_alert_action_assetsclumn_mapper
        List<AssetsFielidParam> assetsFielids = param.getAssetsFielid();
        if(assetsFielids != null && assetsFielids.size() > 0){
            List<AssetsFielidParam> result = new ArrayList<>();
            for(int i=0; i<assetsFielids.size(); i++){
                assetsFielids.get(i).setActionId(param.getActionId());
            }
        }else{
            AssetsFielidParam fielidParam = new AssetsFielidParam();
            fielidParam.setClumnName("default");
            fielidParam.setClumnComent("默认选择");
            fielidParam.setActionId(param.getActionId());
            assetsFielids.add(fielidParam);
        }
        mwAlertActionDao.insertActionAssetsclumn(assetsFielids);
        //插入数据到 mw_alert_rule_select
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        if(param.getMwRuleSelectListParam() != null && param.getMwRuleSelectListParam().size() > 0){
            for (MwRuleSelectParam s : param.getMwRuleSelectListParam()){
                MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
                ruleSelectDto.setCondition(s.getCondition());
                ruleSelectDto.setDeep(s.getDeep());
                ruleSelectDto.setKey(s.getKey());
                ruleSelectDto.setName(s.getName());
                ruleSelectDto.setParentKey(s.getParentKey());
                ruleSelectDto.setRelation(s.getRelation());
                ruleSelectDto.setValue(s.getValue());
                ruleSelectDto.setUuid(param.getActionId());
                paramList.add(ruleSelectDto);
                s.setUuid(param.getActionId());
                if(s.getConstituentElements() != null && s.getConstituentElements().size() > 0){
                    paramList.addAll(delMwRuleSelectList(s));
                }
            }
            mwAlertActionDao.insertMwAlertRuleSelect(paramList);
        }
        //插入 mw_alert_action_user_type_mapper
        if(param.getUserTypes() != null && param.getUserTypes().size() > 0){
            List<ActionUserTypeMapper> actionUserTypeMappers = new ArrayList<>();
            for(String userType : param.getUserTypes()){
                ActionUserTypeMapper temp = new ActionUserTypeMapper();
                temp.setActionId(param.getActionId());
                temp.setUserType(userType);
                actionUserTypeMappers.add(temp);
            }
            mwAlertActionDao.insertUserTypeMapper(actionUserTypeMappers);
        }
        List<Integer> userIds = new ArrayList<>();
        //默认用户 如果是私有权限就只发送给自己和用户组中的人，如果是公有权限就发送给该机构和所在用户组的所有人
        List<Integer> groupIds = new ArrayList<>();
        //如果是非默认用户
        if (CollectionUtils.isNotEmpty(param.getActionUserIds())) {
            List<ActionUserMapper> actionUserMappers = new ArrayList<>();
            userIds = param.getActionUserIds();
            if (userIds.size() > 0) {
                userIds.forEach(userid -> {
                    ActionUserMapper actionUserMapper = ActionUserMapper.builder().actionId(actionId).userId(userid).build();
                    actionUserMappers.add(actionUserMapper);
                });
            }
            mwAlertActionDao.insertActionUsersMapper(actionUserMappers);
        }
        if (CollectionUtils.isNotEmpty(param.getActionGroupIds())) {
            List<ActionGroupMapper> actionGroupMappers = new ArrayList<>();
            groupIds = param.getActionGroupIds();
            groupIds.forEach(groupId ->{
                ActionGroupMapper actionGroupMapper =  ActionGroupMapper.builder().actionId(actionId).groupId(groupId).build();
                actionGroupMappers.add(actionGroupMapper);
            });
            mwAlertActionDao.insertActionGroupsMapper(actionGroupMappers);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reply updateAction(MwRuleSelectListParam param) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            param.setModifier(loginName);
            mwAlertActionDao.upAction(param);
            String actionId = param.getActionId();
            //List<String> assetsList = mwAlertActionDao.getAssetsByActionId(actionId);
            //刪除后重新添加
            /*mwAlertActionDao.deleteActionAssetsMapper(actionId);
            mwAlertActionDao.deleteActionLabelMapper(actionId);*/
            mwAlertActionDao.deleteActionUsersMapper(actionId);
            mwAlertActionDao.deleteActionGroupsMapper(actionId);
            mwAlertActionDao.deleteActionRulesMapper(actionId);
            mwAlertActionDao.deleteActionTypesMapper(actionId);
            mwAlertActionDao.deleteActionServerityMapper(actionId);
            mwAlertActionDao.deleteActionLevelRule(actionId);
            mwAlertActionDao.deleteActionLevelEventMapper(actionId);
            mwAlertActionDao.deleteActionLevelUserMapper(actionId);
            mwAlertActionDao.deleteActionAssetsclumnMapper(actionId);
            mwAlertActionDao.deleteActionLeveRuleMapper(actionId);
            mwAlertActionDao.deleteMwAlertRuleSelect(actionId);
            mwAlertActionDao.deleteActionLevelEmailMapper(actionId);
            mwAlertActionDao.deleteActionUserTypeMapper(actionId);
            addAndEditorAction(param);
            mwCommonService.updateMapperAndPerm(param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to editorAction with param={}, cause:{}", param, e);
//            return Reply.fail(ErrorConstant.ALERT_ACTION_EDITOR_CODE_300012, ErrorConstant.ALERT_ACTION_EDITOR_MAG_300012);
            throw new RuntimeException("fail to editorAction with param={}, cause:{}");
        }
    }

    @Override
    public Reply selectAction(String actionId) {
        try {
            MwRuleSelectListParam actionDto = selectActionList(actionId);
            ActionLevelRuleParam actionLevelRuleParam = mwAlertActionDao.selectActionLevelUserIdByActionId(actionId);
            if(actionLevelRuleParam != null){
                Integer level = actionLevelRuleParam.getLevel();
                actionDto.setState(actionLevelRuleParam.getState());
                actionDto.setLevelOneDate(actionLevelRuleParam.getDate());
                actionDto.setLevelTwoDate(actionLevelRuleParam.getDateTwo());
                actionDto.setLevelThreeDate(actionLevelRuleParam.getDateThree());
                actionLevelRuleParam.setLevel(2);
                List<ActionLevelParam> twoLevel = mwAlertActionDao.getLevelInfo(actionLevelRuleParam);
                if(CollectionUtils.isNotEmpty(twoLevel)){
                    List<String> rule = new ArrayList<>();
                    for(ActionLevelParam levelParam : twoLevel){
                        rule.add(levelParam.getRuleId());
                    }
                    actionDto.setLevelTwoUserIds(mwAlertActionDao.selectActionLevelUserId(actionLevelRuleParam));
                    actionDto.setLevelTwoRuleIds(rule);
                    actionDto.setTwoTime(twoLevel.get(0).getTimeUnit());
                    actionDto.setIsSendPersonTwo(twoLevel.get(0).getIsSendPerson());
                }
                actionLevelRuleParam.setLevel(3);
                List<ActionLevelParam> threeLevel = mwAlertActionDao.getLevelInfo(actionLevelRuleParam);
                if(CollectionUtils.isNotEmpty(threeLevel)){
                    List<String> rule = new ArrayList<>();
                    for(ActionLevelParam levelParam : threeLevel){
                        rule.add(levelParam.getRuleId());
                    }
                    actionDto.setLevelThreeUserIds(mwAlertActionDao.selectActionLevelUserId(actionLevelRuleParam));
                    actionDto.setLevelThreeRuleIds(rule);
                    actionDto.setThreeTime(threeLevel.get(0).getTimeUnit());
                    actionDto.setIsSendPersonThree(threeLevel.get(0).getIsSendPerson());
                }

                actionLevelRuleParam.setLevel(1);
                List<ActionLevelParam> oneLevel = mwAlertActionDao.getLevelInfo(actionLevelRuleParam);
                if(CollectionUtils.isNotEmpty(oneLevel)){
                    List<String> rule = new ArrayList<>();
                    for(ActionLevelParam levelParam : oneLevel){
                        rule.add(levelParam.getRuleId());
                    }
                    actionDto.setLevelOneUserIds(mwAlertActionDao.selectActionLevelUserId(actionLevelRuleParam));
                    actionDto.setLevelOneRuleIds(rule);
                    actionDto.setOneTime(oneLevel.get(0).getTimeUnit());
                    actionDto.setIsSendPersonOne(oneLevel.get(0).getIsSendPerson());
                }
                //actionDto.setAssetsFielid(mwAlertActionDao.selectActionAssetsclumn(actionId));
                actionDto.setLevel(level);
                List<ActionLevelRuleParam> actionLevelRuleParamList = new ArrayList<>();
                actionLevelRuleParamList = mwAlertActionDao.selecActionLeveEmail(actionId);
                if(CollectionUtils.isNotEmpty(actionLevelRuleParamList)){
                    for (ActionLevelRuleParam rule : actionLevelRuleParamList){
                        if(rule.getLevel() == 1){
                            actionDto.setLevelOneIsAllUser(rule.getIsAllUser());
                            actionDto.setLevelOneEmail(rule.getEmail());
                        }else if(rule.getLevel() == 2){
                            actionDto.setLevelTwoIsAllUser(rule.getIsAllUser());
                            actionDto.setLevelTwoEmail(rule.getEmail());
                        }else if(rule.getLevel() == 3){
                            actionDto.setLevelThreeDate(rule.getIsAllUser());
                            actionDto.setLevelThreeEmail(rule.getEmail());
                        }
                    }
                }

            }
            List<MwRuleSelectParam> ruleSelectList = mwAlertActionDao.selectMwAlertRuleSelect(actionId);
            List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
            if(ruleSelectList != null && ruleSelectList.size() > 0){
                for (MwRuleSelectParam s : ruleSelectList){
                    if(s.getKey().equals("root")){
                        ruleSelectParams.add(s);
                    }
                }
                for(MwRuleSelectParam s : ruleSelectParams){
                    s.setConstituentElements(getChild(s.getKey(),ruleSelectList));
                }
            }
            actionDto.setMwRuleSelectListParam(ruleSelectParams);
            DataPermission dataPermission = mwCommonService.getDataPermission(DataType.ACTION,actionId);
            actionDto.setOrgIds(dataPermission.getOrgNodes());
            actionDto.setPrincipal(dataPermission.getUserIds());
            actionDto.setGroupIds(dataPermission.getGroupIds());
            return Reply.ok(actionDto);
        } catch (Exception e) {
            log.error("fail to selectPopupAction with actionId={}, cause:{}", actionId, e.getMessage());
            return Reply.fail(ErrorConstant.ALERT_ACTION_POPUP_BROWSE_CODE_300014, ErrorConstant.ALERT_ACTION_POPUP_BROWSE_MAG_300014);

        }
    }


    public List<MwRuleSelectParam> delMwRuleSelectList(MwRuleSelectParam param){
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        for (MwRuleSelectParam s : param.getConstituentElements()){
            MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
            ruleSelectDto.setCondition(s.getCondition());
            ruleSelectDto.setDeep(s.getDeep());
            ruleSelectDto.setKey(s.getKey());
            ruleSelectDto.setName(s.getName());
            ruleSelectDto.setParentKey(s.getParentKey());
            ruleSelectDto.setRelation(s.getRelation());
            ruleSelectDto.setValue(s.getValue());
            ruleSelectDto.setUuid(param.getUuid());
            paramList.add(ruleSelectDto);
            s.setUuid(param.getUuid());
            if(s.getConstituentElements() != null && s.getConstituentElements().size() > 0){
                List<MwRuleSelectParam> temps = delMwRuleSelectList(s);
                paramList.addAll(temps);
            }
        }
        return paramList;
    }

    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList){
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for(MwRuleSelectParam s : rootList){
            if(s.getParentKey() != null && s.getParentKey().equals(key)){
                childList.add(s);
            }
        }
        for(MwRuleSelectParam s : childList){
            s.setConstituentElements(getChild(s.getKey(),rootList));
        }
        if(childList.size() == 0){
            return null;
        }
        return childList;

    }

    @Override
    public Reply getTag(){
        try {
            List<MwAssetsLabelDTO> getLabelBoard = mwLabelCommonServcie.getLabelBoard(null, "ASSETS");
            List<String> labelValue = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(getLabelBoard)) {
                for (MwAssetsLabelDTO s : getLabelBoard) {
                    if (s.getTagboard() != null) {
                        labelValue.add(s.getTagboard());
                    }
                    if (s.getDropValue() != null) {
                        labelValue.add(s.getDropValue());
                    }
                    if (s.getDateTagboard() != null) {
                        labelValue.add(s.getDateTagboard().toString());
                    }
                }
            }
            return Reply.ok(labelValue);
        }catch (Exception e){
            log.error("查询标签失败，" + e);
            return Reply.fail("查询标签失败！");
        }
    }

    @Override
    public AddAndUpdateAlertActionParam selectPopupAction(String actionId){
        log.info("selectPopupAction actionId:" + actionId);
        AddAndUpdateAlertActionParam param = mwAlertActionDao.selectPopupAction(actionId);
        param.setLabel(mwAlertActionDao.selectLabel(actionId));
        param.setSeverity(mwAlertActionDao.selectSeverity(actionId));
        param.setRuleIds(mwAlertActionDao.selectRules(actionId));
        param.setActionTypeIds(mwAlertActionDao.selectActionTypes(actionId));
        param.setActionUserIds(mwAlertActionDao.selectActionUsers(actionId));
        param.setActionGroupIds(mwAlertActionDao.selectActionGroupsMapper(actionId));
        param.setUserTypes(mwAlertActionDao.selectUserType(actionId));
        return  param;
    }

    public MwRuleSelectListParam selectActionList(String actionId){
        MwRuleSelectListParam actionDto = mwAlertActionDao.selectAction(actionId);
        actionDto.setSeverity(mwAlertActionDao.selectSeverity(actionId));
        actionDto.setRuleIds(mwAlertActionDao.selectRules(actionId));
        actionDto.setActionUserIds(mwAlertActionDao.selectActionUsers(actionId));
        actionDto.setActionGroupIds(mwAlertActionDao.selectActionGroupsMapper(actionId));
        actionDto.setAssetsFielid(mwAlertActionDao.selectActionAssetsclumn(actionId));
        actionDto.setUserTypes(mwAlertActionDao.selectUserType(actionId));
        return actionDto;
    }

    @Override
    public Reply getAlertLevel(){
        return Reply.ok(MWAlertLevelParam.actionAlertLevelMap);
    }

}
