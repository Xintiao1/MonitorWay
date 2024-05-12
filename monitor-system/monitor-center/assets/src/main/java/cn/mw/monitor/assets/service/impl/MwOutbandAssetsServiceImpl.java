package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.api.exception.AssetsException;
import cn.mw.monitor.service.assets.param.AddUpdateOutbandAssetsParam;
import cn.mw.monitor.service.assets.param.QueryOutbandAssetsParam;
import cn.mw.monitor.assets.api.param.assets.UpdateMonStateParam;
import cn.mw.monitor.assets.api.param.assets.UpdateSetStateParam;
import cn.mw.monitor.assets.dao.MwOutbandAssetsTableDao;
import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.MwOutbandAssetsByIdDTO;
import cn.mw.monitor.assets.dto.MwOutbandAssetsDTO;
import cn.mw.monitor.assets.dto.OutbandWithAssetsDTO;
import cn.mw.monitor.assets.model.*;
import cn.mw.monitor.service.assets.api.MwOutbandAssetsService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.engineManage.api.exception.DeleteEngineException;
import cn.mw.monitor.engineManage.dao.MwEngineManageTableDao;
import cn.mw.monitor.engineManage.service.MwEngineManageService;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.dto.UpdateDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.MWUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/6/22 14:53
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class MwOutbandAssetsServiceImpl implements MwOutbandAssetsService {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/outbandAssets");

    private static final String LOCAL = "本机";
    private static final String CUSTOM = "自定义";

    @Resource
    private MwOutbandAssetsTableDao mwOutbandAssetsTableDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    MwEngineManageService mwEngineMangeService;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    MwEngineCommonsService mwEngineCommonsService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Resource
    MwEngineManageTableDao mwEngineManageTableDao;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Override
    public Reply selectById(String id) {
        try {
            MwOutbandAssetsDTO mwOutbandAssetsDTO = mwOutbandAssetsTableDao.selectById(id);
            Boolean flag = (mwOutbandAssetsDTO.getPollingEngine() != null && StringUtils.isNotEmpty(mwOutbandAssetsDTO.getPollingEngine()));
            mwOutbandAssetsDTO.setPollingMode(flag ? CUSTOM : LOCAL);
//            mwOutbandAssetsDTO.setPollingMode((mwOutbandAssetsDTO.getPollingEngine() != null) ? CUSTOM : LOCAL);
            MwOutbandAssetsByIdDTO mtDtos = CopyUtils.copy(MwOutbandAssetsByIdDTO.class, mwOutbandAssetsDTO);
            // usergroup重新赋值使页面可以显示
            List<Integer> groupIds = new ArrayList<>();
            mwOutbandAssetsDTO.getGroup().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            mtDtos.setGroupIds(groupIds);
            // department重新赋值使页面可以显示
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (mwOutbandAssetsDTO.getDepartment() != null) {
                mwOutbandAssetsDTO.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                mtDtos.setOrgIds(orgNodes);
            }
            // user重新赋值
            List<Integer> userIds = new ArrayList<>();
            mwOutbandAssetsDTO.getPrincipal().forEach(
                    userDTO -> userIds.add(userDTO.getUserId())
            );
            mtDtos.setPrincipal(userIds);
            //查询标签重新赋值给页面可以显示
            List<MwAssetsLabelDTO> labelBoard = mwLabelCommonServcie.getLabelBoard(id, DataType.OUTBANDASSETS.getName());
            mtDtos.setAssetsLabel(labelBoard);
            String pollingEngine = mtDtos.getPollingEngine();
            Map<String,String> pollingEngines = new HashMap<>();
            if(StringUtils.isNotBlank(pollingEngine)){
                pollingEngines.put(mtDtos.getMonitorServerId()+"",pollingEngine);
            }
            mtDtos.setPollingEngines(pollingEngines);
            return Reply.ok(mtDtos);
        } catch (Exception e) {
            logger.error("fail to selectById with d={}, cause:{}", id, e);
            return Reply.fail(ErrorConstant.OUTBAND_ASSETSCODE_210112, ErrorConstant.OUTBAND_ASSETS_MSG_210112);
        }
    }

    @Override
    public Reply selectList(QueryOutbandAssetsParam qParam) {
        try {
            if(qParam.getIsHomePageType() == 1){//首页需要查询全部
                qParam.setPageSize(Integer.MAX_VALUE);
            }
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List<MwOutbandAssetsTable> mwOutbandAssetses = new ArrayList();
            List<String> ids = qParam.getIds();
            if (ids != null && ids.size() == 0 ) {
                PageInfo pageInfo = new PageInfo<>(mwOutbandAssetses);
                pageInfo.setList(mwOutbandAssetses);

                logger.info("ASSETS_LOG[]assets[]带外资产管理[]查询带外资产信息[]{}[]", qParam);

                return Reply.ok(pageInfo);
            }

            //是否是高级查询
            if (null != qParam.getLogicalQueryLabelParamList() && qParam.getLogicalQueryLabelParamList().size() > 0) {
                List<String> assetsIds = mwLabelCommonServcie.getTypeIdsByLabel(qParam.getLogicalQueryLabelParamList());
                if (null != assetsIds && assetsIds.size() > 0) {

                    if (ids != null && ids.size() > 0) {
                        assetsIds.retainAll(ids);
                    }
                    if (null != assetsIds && assetsIds.size() > 0) {
                        qParam.setIds(assetsIds);
                    } else {
                        PageInfo pageInfo = new PageInfo<>(mwOutbandAssetses);
                        pageInfo.setList(mwOutbandAssetses);

                        logger.info("ASSETS_LOG[]assets[]带外资产管理[]查询带外资产信息[]{}[]", qParam);

                        return Reply.ok(pageInfo);
                    }
                } else {
                    PageInfo pageInfo = new PageInfo<>(mwOutbandAssetses);
                    pageInfo.setList(mwOutbandAssetses);

                    logger.info("ASSETS_LOG[]assets[]带外资产管理[]查询带外资产信息[]{}[]", qParam);

                    return Reply.ok(pageInfo);
                }
            }
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != groupIds && groupIds.size() > 0) {
                qParam.setGroupIds(groupIds);
            }
//            if (null != qParam.getLogicalQueryLabelParamList() && qParam.getLogicalQueryLabelParamList().size() > 0) {
//                List<String> ids = mwLabelCommonServcie.getTypeIdsByLabel(qParam.getLogicalQueryLabelParamList());
//                if (null != ids && ids.size() > 0) {
//                    qParam.setIds(ids);
//                }
//            }
            switch (dataPermission) {
                case PRIVATE:
                    qParam.setUserId(userId);
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                    Map priCriteria = PropertyUtils.describe(qParam);
                    mwOutbandAssetses = mwOutbandAssetsTableDao.selectPriList(priCriteria);
                    break;
                case PUBLIC:
                    String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                    List<Integer> orgIds = new ArrayList<>();
                    Boolean isAdmin = false;
                    if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                        isAdmin = true;
                    }
                    if (!isAdmin) {
                        orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                    }
                    if (null != orgIds && orgIds.size() > 0) {
                        qParam.setOrgIds(orgIds);
                    }
                    qParam.setIsAdmin(isAdmin);
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                    Map pubCriteria = PropertyUtils.describe(qParam);
                    mwOutbandAssetses = mwOutbandAssetsTableDao.selectPubList(pubCriteria);
                    break;
            }
//            }

            if(CollectionUtils.isNotEmpty(mwOutbandAssetses)){
                List<String> outAssetsIps = mwOutbandAssetses.stream().map(MwOutbandAssetsTable::getIpAddress).collect(Collectors.toList());
                //根据IP查询有形资产
                List<String> assetsIds = mwOutbandAssetsTableDao.selectTangibleAssetsByIps(outAssetsIps);

                //设置带外资产关联的有形资产信息
                if(CollectionUtils.isNotEmpty(assetsIds)){
                    //根据ID查询资产
                    QueryTangAssetsParam param = new QueryTangAssetsParam();
                    param.setPageNumber(1);
                    param.setPageSize(10000);
                    param.setAssetsIds(assetsIds);
                    Reply reply = assetsService.selectList(param);
                    if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                        PageInfo pageInfo = (PageInfo) reply.getData();
                        List<MwTangibleassetsTable> mwTangibleassetsTables = pageInfo.getList();
                        for (MwOutbandAssetsTable mwOutbandAssets : mwOutbandAssetses) {
                            String ipAddress = mwOutbandAssets.getIpAddress();
                            for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
                                String outBandIp = mwTangibleassetsTable.getOutBandIp();
                                if(ipAddress.equals(outBandIp)){
                                    mwOutbandAssets.setTangibleAssetsName(mwTangibleassetsTable.getAssetsName());
                                    mwOutbandAssets.setTangibleassetsTable(mwTangibleassetsTable);
                                    continue;
                                }
                            }
                        }
                    }
                }
                //获取资产状态
                getOutBandAssetsStatus(mwOutbandAssetses);
            }
            PageInfo pageInfo = new PageInfo<>(mwOutbandAssetses);
            pageInfo.setList(mwOutbandAssetses);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            logger.error("fail to selectList with mtaDTO={}, cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.OUTBAND_ASSETSCODE_210114, ErrorConstant.OUTBAND_ASSETS_MSG_210114);
        } finally {
            logger.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    @Autowired
    private MwTangibleAssetsService assetsService;

    @Override
    public Reply updateAssets(AddUpdateOutbandAssetsParam uParam) throws Throwable {

        uParam.setModifier(iLoginCacheInfo.getLoginName());
        List<String> ids = uParam.getIds();
        if (ids != null && ids.size() > 1) {
            for (String id : ids) {
                if(uParam.isOutBandAssetsLabelcheckbox()){
                    //删除标签参数
                    mwLabelCommonServcie.deleteLabelBoard(id, DataType.OUTBANDASSETS.getName());
                    //插入标签参数
                    if (null != uParam.getAssetsLabel() && uParam.getAssetsLabel().size() > 0) {
                        mwLabelCommonServcie.insertLabelboardMapper(uParam.getAssetsLabel(), id, DataType.OUTBANDASSETS.getName());
                    }
                }
            }
            updateMapperAndPerm(uParam);
            mwOutbandAssetsTableDao.updateBatch(uParam);
            if (uParam.getPollingEngineList() == null || uParam.getPollingEngineList().size() == 0) {
                for (Map.Entry<Integer, List<String>> entry : uParam.getProxyIdList().entrySet()) {
                    Map<String, Object> updateParam = new HashMap<>();
                    updateParam.put("proxy_hostid", null);
                    MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(entry.getKey(), entry.getValue(), updateParam);
                    if (result.isFail()) {
                        log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                        throw new AssetsException("删除主机代理失败:" + result.getData());
                    }
                }
            }
            if(uParam.isPollingEnginecheckbox() && uParam.getPollingEngineList() != null){
                for (Map.Entry<Integer, String> entry : uParam.getPollingEngineList().entrySet()) {
                    if (entry.getKey() != null && entry.getKey() != 0) {
                        //获取代理ip
                        String pollId = entry.getValue();
                        if (pollId != null && !"".equals(pollId)) {
                            MwEngineManageDTO proxyEntity = mwEngineManageTableDao.selectById(pollId);
                            Map<String, Object> updateParam = new HashMap<>();
                            updateParam.put("proxy_hostid", proxyEntity.getProxyId());
                            MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(entry.getKey(), uParam.getProxyIdList().get(entry.getKey()), updateParam);
                            if (result.isFail()) {
                                log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                                throw new AssetsException("修改主机代理失败:" + result.getData());
                            }
                        } else {
                            Map<String, Object> updateParam = new HashMap<>();
                            updateParam.put("proxy_hostid", null);
                            MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(entry.getKey(), uParam.getProxyIdList().get(entry.getKey()), updateParam);
                            if (result.isFail()) {
                                log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                                throw new AssetsException("删除主机代理失败:" + result.getData());
                            }
                        }
                    }
                }
            }
        } else {
            //删除负责人
            deleteMapperAndPerm(uParam.getId());
            //添加权限
            addMapperAndPerm(uParam);
            MwOutbandAssetsDTO outbandAssetsDTO = mwOutbandAssetsTableDao.selectById(uParam.getId());
            String beforePolling_engine = outbandAssetsDTO.getPollingEngine();
            //更新之前关联引擎信息
//            if (beforePolling_engine != null && !"".equals(beforePolling_engine)) {
//                mwEngineCommonsService.updateMonitorNums(false, beforePolling_engine, uParam.getAssetsId());
//            }
//            if (uParam.getPollingEngine() != null && !"".equals(uParam.getPollingEngine())) {
//                mwEngineCommonsService.updateMonitorNums(true, uParam.getPollingEngine(), uParam.getAssetsId());
//            }
            //判断修改的是本机还是自定义，如果是本机，将PollingEngine置为Null
            String pollingMode = uParam.getPollingMode();
            if (StringUtils.isNotBlank(pollingMode) && "本机".equals(pollingMode)) {
                uParam.setPollingEngine("");
            }
            //判断是否需要更新IP
            if (!uParam.getEditBeforeIp().equals(uParam.getIpAddress())) {
                MWZabbixAPIResult result = mwtpServerAPI.hostInterfaceGet(uParam.getMonitorServerId(), uParam.getAssetsId());
                if (!result.isFail()) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0) {
                        String interfaceid = jsonNode.get(0).get("interfaceid").asText();
                        Map<String, Object> map = new HashMap<>();
                        map.put("ip", uParam.getIpAddress());
                        MWZabbixAPIResult updateResult = mwtpServerAPI.hostInterfaceUpdate(uParam.getMonitorServerId(), interfaceid, map);
                        if (updateResult.isFail()) {
                            log.error("[]ERROR_LOG[][]修改主机interface信息失败[][]msg:[]{}", updateResult.getData());
                            throw new AssetsException("修改主机interface信息失败:" + updateResult.getData());
                        }
                    }
                }
            }else{
                uParam.setIpAddress(null);
            }
            //更新这条资产信息
            mwOutbandAssetsTableDao.update(uParam);

            //获取代理ip
            String pollId = uParam.getPollingEngine();
            if (pollId != null && !"".equals(pollId)) {
                MwEngineManageDTO proxyEntity = mwEngineManageTableDao.selectById(pollId);
                MWZabbixAPIResult result = mwtpServerAPI.hostProxyUpdate(uParam.getMonitorServerId(), uParam.getAssetsId(), proxyEntity.getProxyId());
                if (result.isFail()) {
                    log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                    throw new AssetsException("修改主机代理失败:" + result.getData());
                }
            } else {
                if (!Strings.isNullOrEmpty(beforePolling_engine) && uParam.getMonitorServerId() != null && uParam.getMonitorServerId() != 0) {
                    MWZabbixAPIResult result = mwtpServerAPI.hostProxyUpdate(uParam.getMonitorServerId(), uParam.getAssetsId(), null);
                    if (result.isFail()) {
                        log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                        throw new AssetsException("删除主机代理失败:" + result.getData());
                    }
                }
            }

            //修改zabbix中主机可见名称
            boolean isUpdateNmae = updateZabbixSoName(uParam.getMonitorServerId(),uParam.getAssetsId(), uParam.getAssetsName());
            if(!isUpdateNmae){
                log.info("修改资产可见名称失败,主机ID："+uParam.getAssetsId()+",修改名称:"+uParam.getAssetsName());
            }
            //删除标签参数
            mwLabelCommonServcie.deleteLabelBoard(uParam.getId(), DataType.OUTBANDASSETS.getName());
            //插入标签参数
            if (null != uParam.getAssetsLabel() && uParam.getAssetsLabel().size() > 0) {
                mwLabelCommonServcie.insertLabelboardMapper(uParam.getAssetsLabel(), uParam.getId(), DataType.OUTBANDASSETS.getName());
            }
        }
        return Reply.ok("更新成功");
    }

    @Override
    public Reply deleteAssets(List<DeleteTangAssetsID> ids) {
        if (null == ids || ids.size() == 0) {
            return Reply.fail("无删除数据");
        }
        StringBuffer failMsg = new StringBuffer("以下资产被关联:");
        //用来存主键id
        List<String> tids = new ArrayList<String>();
        //key值是监控服务器id, value值是监控服务器对应的hostid数组
        Map<Integer, List<String>> hostIdList = new HashMap<>();
        for (DeleteTangAssetsID id : ids) {
            OutbandWithAssetsDTO outbandWithAssetsDTO = mwOutbandAssetsTableDao.selectRelevanceByOBIds(id.getId());
            if (outbandWithAssetsDTO != null && outbandWithAssetsDTO.getAssetsList() != null) {
                List<AssetsDTO> assetsList = outbandWithAssetsDTO.getAssetsList();
                //关联上的资产名称
                List<String> assetsNameList = assetsList.stream().map(AssetsDTO::getAssetsName).collect(Collectors.toList());
                if (assetsNameList.size() > 0) {
                    failMsg.append("\n").append(StringUtils.join(assetsNameList, ",")).append("已关联上").append(outbandWithAssetsDTO.getOutbandAssetsName());
                    continue;
                }
            }
            tids.add(id.getId());
            List<String> value = new ArrayList<>();
            if (hostIdList.get(id.getMonitorServerId()) != null && hostIdList.get(id.getMonitorServerId()).size() > 0) {
                value = hostIdList.get(id.getMonitorServerId());
            }
            value.add(id.getAssetsId());
            hostIdList.put(id.getMonitorServerId(), value);
//            //更新关联引擎信息
//            if (id.getPollingEngine() != null && !"".equals(id.getPollingEngine())) {
//                mwEngineCommonsService.updateMonitorNums(false, id.getPollingEngine(), id.getAssetsId());
//            }
            //删除标签
            mwLabelCommonServcie.deleteLabelBoard(id.getId(), DataType.OUTBANDASSETS.getName());
        }
        if (tids.size() > 0) {
            //删除主表信息
            mwOutbandAssetsTableDao.delete(tids);
            //删除关联信息
            deleteMapperAndPerm(tids);
            //删除zabbix 配置
            for (Map.Entry<Integer, List<String>> entry : hostIdList.entrySet()) {
                if (entry.getKey() != null && entry.getKey() != 0) {
                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostDelete(entry.getKey(), entry.getValue());
                    if (mwZabbixAPIResult.isFail()) {
                        throw new DeleteEngineException(ErrorConstant.OUTBAND_ASSETS_MSG_210117, mwZabbixAPIResult.data.toString());
                    }
                }
            }
        }
        if (failMsg.indexOf("\n") != -1) {
            return Reply.warn("删除失败!" + "\n" + failMsg.append("\n请解除关联后再做删除"));
        } else {
            return Reply.ok("删除成功");
        }
    }

    @Override
    public Reply insertAssets(AddUpdateOutbandAssetsParam aParam) throws Throwable {
        List<String> items = new ArrayList<>();
        //重复性校验
        List<MwOutbandAssetsDTO> check = mwOutbandAssetsTableDao.check(QueryOutbandAssetsParam.builder().ipAddress(aParam.getIpAddress()).build());
        if (check.size() == 1) {
            items.add("IP重复");
        }
        if (items.size() > 0) {
            throw new Throwable(StringUtils.join(new String[]{ErrorConstant.OUTBAND_ASSETS_MSG_210116, StringUtils.join(items, "、 ")}));
        }
        if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
            //设置第三方监控服务器中主机名称
            aParam.setTPServerHostName(UuidUtil.getUid());
        }
        try {
            return doInsertAssets(aParam);
        } catch (Throwable t) {
            //报错删除已添加的zabbixHost
            if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() != 0) {
                ArrayList<String> hostNames = new ArrayList<>();
                hostNames.add(aParam.getTPServerHostName());
                MWZabbixAPIResult result = mwtpServerAPI.hostListGetByHostName(aParam.getMonitorServerId(), hostNames);
                if (!result.isFail()) {
                    List<String> hostIds = new ArrayList<>();
                    JsonNode data = (JsonNode) result.getData();
                    data.forEach(hostId -> {
                        hostIds.add(hostId.get("hostid").asText());
                    });
                    mwtpServerAPI.hostDelete(aParam.getMonitorServerId(), hostIds);
                }
            }
            return Reply.fail(ErrorConstant.OUTBAND_ASSETS_MSG_210116);
        }
    }

    @Override
    public Reply doInsertAssets(AddUpdateOutbandAssetsParam aParam) throws Throwable {
        aParam.setCreator(iLoginCacheInfo.getLoginName());
        aParam.setModifier(iLoginCacheInfo.getLoginName());
        aParam.setId(UuidUtil.getUid());

        //说明创建不关联zabbix的资产,启用状态设置成未启用
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0 ) {
            aParam.setEnable(TangibleAssetState.DISACTIVE.name());
        } else {//说明是关联zabbix资产,启用状态设置成启用
            aParam.setEnable(TangibleAssetState.ACTIVE.name());
        }

        String zabbixHostId = aParam.getAssetsId();
        if (zabbixHostId == null || StringUtils.isEmpty(zabbixHostId)) {
            zabbixHostId = createAndGetZabbixHostId(aParam);
        }
        aParam.setAssetsId(zabbixHostId);
        aParam.setDeleteFlag(false);
        Date date = new Date();
        aParam.setCreateDate(date);
        aParam.setModificationDate(date);

        String checkNowMsg = "";
        //是否启动立即执行
        if (aParam.isCheckNowFlag() && !Strings.isNullOrEmpty(aParam.getAssetsId())) {
            Boolean aBoolean = mwAssetsManager.checkNowItems(aParam.getMonitorServerId(), aParam.getAssetsId());
            if (!aBoolean) {
                checkNowMsg = "立即执行的操作未成功";
            }
        }

        mwOutbandAssetsTableDao.insert(aParam);
//        //修改zabbix中主机可见名称
//        boolean isUpdateNmae = updateZabbixSoName(aParam.getMonitorServerId(),aParam.getAssetsId(), aParam.getAssetsName());
//        if(!isUpdateNmae){
//            log.info("修改资产可见名称失败,主机ID："+aParam.getAssetsId()+",修改名称:"+aParam.getAssetsName());
//        }
        //添加权限
        addMapperAndPerm(aParam);
//        if (aParam.getPollingEngine() != null && !"".equals(aParam.getPollingEngine())) {
//            mwEngineCommonsService.updateMonitorNums(true, aParam.getPollingEngine(), aParam.getAssetsId());
//        }
        //插入标签参数
        if (null != aParam.getAssetsLabel() && aParam.getAssetsLabel().size() > 0) {
            mwLabelCommonServcie.insertLabelboardMapper(aParam.getAssetsLabel(), aParam.getId(), DataType.OUTBANDASSETS.getName());
        }
        return Reply.ok("新增成功");
    }

    @Override
    public Reply updateState(UpdateAssetsStateParam updateAssetsStateParam) {
        TangibleAssetStateType tast = TangibleAssetStateType.valueOf(updateAssetsStateParam.getStateType());
        switch (tast) {
            case AssetState:
                TangibleAssetState tas = TangibleAssetState.valueOf(updateAssetsStateParam.getEnable());
                mwtpServerAPI.hostUpdate(updateAssetsStateParam.getMonitorServerId(), updateAssetsStateParam.getHostIds(), tas.getTangibleAssetMonitorState().getZabbixStatus());
                mwOutbandAssetsTableDao.updateAssetsState(updateAssetsStateParam);
                break;
            case MonitorState:
                TangibleAssetMonitorState tams = TangibleAssetMonitorState.valueOf(updateAssetsStateParam.getEnable().toUpperCase());
                mwtpServerAPI.hostUpdate(updateAssetsStateParam.getMonitorServerId(), updateAssetsStateParam.getHostIds(), tams.getZabbixStatus());
                UpdateMonStateParam umsp = new UpdateMonStateParam();
                umsp.setIdList(updateAssetsStateParam.getIdList());
                umsp.setHostIds(updateAssetsStateParam.getHostIds());
                umsp.setMonitorFlag(tams.isActive());
                mwOutbandAssetsTableDao.updateAssetsMonState(umsp);
                break;
            case SettingState:
                TangibleAssetSetState tass = TangibleAssetSetState.valueOf(updateAssetsStateParam.getEnable().toUpperCase());
                UpdateSetStateParam ussp = new UpdateSetStateParam();
                ussp.setIdList(updateAssetsStateParam.getIdList());
                ussp.setHostIds(updateAssetsStateParam.getHostIds());
                ussp.setSettingFlag(tass.isEnable());
                mwOutbandAssetsTableDao.updateAssetsSetState(ussp);
                break;
            default:
                break;
        }
        return Reply.ok("更新成功");
    }

    @Override
    public Reply updateAssetsTemplateIds() {
        List<MwOutbandAssetsTable> MwOutbandAssetsTables = mwOutbandAssetsTableDao.selectTopoAssetsList();
        Map<Integer, List<String>> mapHost = new HashMap();
        List<MwAssetsIdsDTO> updataTemplateList = new ArrayList<>();
        if (MwOutbandAssetsTables != null && MwOutbandAssetsTables.size() > 0) {
            //将模型名称当成key值，转为map数据
            for (MwOutbandAssetsTable assets : MwOutbandAssetsTables) {
                Integer serverId = assets.getMonitorServerId();
                String hostId = assets.getAssetsId();
                if (mapHost.containsKey(serverId)) {
                    List<String> hostIdList1 = mapHost.get(serverId);
                    hostIdList1.add(hostId);
                    mapHost.put(serverId, hostIdList1);
                } else {
                    List<String> hostIdList = new ArrayList<>();
                    hostIdList.add(hostId);
                    mapHost.put(assets.getMonitorServerId(), hostIdList);
                }
            }
            mapHost.forEach((k, v) -> {
                if (v != null && v.size() > 0) {
                    MWZabbixAPIResult result = mwtpServerAPI.getHostInfosById(k, v);
                    if (!result.isFail()) {
                        JsonNode node = (JsonNode) result.getData();
                        if (node.size() > 0) {
                            node.forEach(data -> {
                                MwAssetsIdsDTO mwDto = new MwAssetsIdsDTO();
                                String templateId = "";
                                if (data.get("parentTemplates").size() > 0) {
                                    //根据接口Api获取templateId
                                    templateId = data.get("parentTemplates").get(0).get("templateid").asText();
                                }
                                String hostId = data.get("hostid").asText();
                                if (!Strings.isNullOrEmpty(templateId) && !Strings.isNullOrEmpty(hostId)) {
                                    mwDto.setTemplateId(templateId);
                                    mwDto.setHostId(hostId);
                                    updataTemplateList.add(mwDto);
                                }
                            });
                        }
                    }
                }

            });
            if (updataTemplateList.size() > 0) {
                mwOutbandAssetsTableDao.updateTemplateIdBatch(updataTemplateList);
            }
        }
        return Reply.ok();
    }

    @Override
    public String createAndGetZabbixHostId(AddUpdateOutbandAssetsParam aParam) {
        if (aParam.getAssetsId() != null && StringUtils.isNotEmpty(aParam.getAssetsId())) {
            return aParam.getAssetsId();
        }
        //说明创建不关联zabbix的资产
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0 ) {
            return null;
        }
        //获取分组Id
        ArrayList<String> groupIdList = new ArrayList<>();
        groupIdList.add(aParam.getHostGroupId());
        ArrayList<Map<String, Object>> interList = new ArrayList<>();
        Map interMap = new HashMap();
        interMap.put("ip", aParam.getIpAddress());
        interMap.put("main", 1);
        String port = "623";
        int interfaceType = 1;

        Map detailsMap = new HashMap();
        List<Map> macroDTOS = new ArrayList<>();
        Map<String, Object> otherParam = new HashMap<>();
        if (aParam.getMonitorMode() == RuleType.IPMI.getMonitorMode()) {
            if (null != aParam.getMwIPMIAssetsDTO() && !"".equals(aParam.getMwIPMIAssetsDTO().getPort())) {
                port = aParam.getMwIPMIAssetsDTO().getPort().toString();
            }
            interfaceType = RuleType.IPMI.geInterfaceType();
            //用户名称
            otherParam.put("ipmi_username", aParam.getMwIPMIAssetsDTO().getAccount());
            //密码
            otherParam.put("ipmi_password", aParam.getMwIPMIAssetsDTO().getPassword());
        } else {
            logger.warn("no match MonitorMode" + aParam.getMonitorMode());
        }

        interMap.put("dns", "");
        interMap.put("port", port);
        interMap.put("type", interfaceType);//type 接口类型  1 - agent 2 - SNMP;3 - IPMI;4 - JMX.
        interMap.put("useip", 1);//使用的链接方式 0 DNS名称连接 1 IP地址进行连接
        if (detailsMap.size() > 0) {
            interMap.put("details", detailsMap);
        }
        interList.add(interMap);

        ArrayList<String> templList = new ArrayList<>();
        templList.add(aParam.getTemplateId());

        Integer status = aParam.getMonitorFlag()
                ? TangibleAssetMonitorState.TRUE.getZabbixStatus() : TangibleAssetMonitorState.FALSE.getZabbixStatus();
        //设置状态
        otherParam.put("status", status);
        //关联引擎
        if (aParam.getPollingEngine() != null && !"".equals(aParam.getPollingEngine())) {
            //获取代理ip
            MwEngineManageDTO proxyEntity = mwEngineManageTableDao.selectById(aParam.getPollingEngine());
            otherParam.put("proxy_hostid", proxyEntity.getProxyId());
        }
        String visibleName = aParam.getAssetsName();
        MWZabbixAPIResult result = mwtpServerAPI.hostCreate(aParam.getMonitorServerId(), aParam.getTPServerHostName(),visibleName
                , groupIdList, interList, templList, macroDTOS, otherParam);
        if (result.isFail()) {
            logger.error("[]ERROR_LOG[][]添加主机失败[][]msg:[]{}", result.getData());
            throw new AssetsException("添加主机失败:" + result.getData());
        }
        String hostids = String.valueOf(result.getData());
        JSONObject js = JSONObject.parseObject(hostids);
        JSONArray strs = (JSONArray) js.get("hostids");
        String hostid = strs.getString(0);
        return hostid;
    }

    /**
     * 获取带外资产的ip列表下拉框
     *
     * @return
     */
    @Override
    public Reply selectDropdownList() {
        List<String> ipDropdownList = mwOutbandAssetsTableDao.selectIPDropdownList();
        return Reply.ok(ipDropdownList);
    }

    /**
     * 删除负责人
     *
     * @param id
     */
    private void deleteMapperAndPerm(String id) {
        DeleteDto deleteDto = DeleteDto.builder().typeId(id).type(DataType.OUTBANDASSETS.getName()).build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 批量删除负责人
     *
     * @param ids
     */
    private void deleteMapperAndPerm(List<String> ids) {
        DeleteDto deleteDto = DeleteDto.builder().typeIds(ids).type(DataType.OUTBANDASSETS.getName()).build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加负责人
     *
     * @param uParam
     */
    private void addMapperAndPerm(AddUpdateOutbandAssetsParam uParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(uParam.getGroupIds())
                .userIds(uParam.getPrincipal())
                .orgIds(uParam.getOrgIds())
                .typeId(uParam.getId())
                .type(DataType.OUTBANDASSETS.getName())
                .desc(DataType.OUTBANDASSETS.getDesc()).build();
        //添加负责人
        mwCommonService.addMapperAndPerm(insertDto);
    }

    /**
     * 修改负责人
     *
     * @param uParam
     */
    private void updateMapperAndPerm(AddUpdateOutbandAssetsParam uParam) {
        UpdateDTO updateDTO = UpdateDTO.builder()
                .isGroup(uParam.isGroupIdscheckbox())
                .isUser(uParam.isPrincipalcheckbox())
                .isOrg(uParam.isOrgIdscheckbox())
                .groupIds(uParam.getGroupIds())  //用户组
                .userIds(uParam.getPrincipal()) //责任人
                .orgIds(uParam.getOrgIds()) //机构
                .typeIds(uParam.getIds())    //批量资产数据主键
                .type(DataType.OUTBANDASSETS.getName())
                .desc(DataType.OUTBANDASSETS.getDesc()).build();
        //添加负责人
        mwCommonService.editorMapperAndPerms(updateDTO);
    }

    /**
     * 修改zabbix可见名称
     * @param hostId 主机ID
     * @param assetsName 资产名称
     */
    private boolean updateZabbixSoName(Integer serverId,String hostId,String assetsName){
        if(StringUtils.isNotBlank(hostId) && StringUtils.isNotBlank(assetsName)){
            //调用zabbix接口根据主机ID修改可见名称
            MWZabbixAPIResult result = mwtpServerAPI.hostUpdateSoName(serverId,hostId, assetsName);
            if (result != null && !result.isFail()){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取带外资产资产状态
     * @param mwOutbandAssetses
     */
    private void getOutBandAssetsStatus(List<MwOutbandAssetsTable> mwOutbandAssetses){
        //查询带外资产状态
        if (mwOutbandAssetses != null && mwOutbandAssetses.size() > 0) {
            Map<Integer, List<String>> groupMap = mwOutbandAssetses.stream()
                    .collect(Collectors.groupingBy(MwOutbandAssetsTable::getMonitorServerId, Collectors.mapping(MwOutbandAssetsTable::getAssetsId, Collectors.toList())));
            Map<String, String> statusMap = new HashMap<>();
            Set<String> hostSets = new HashSet<>();
            for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                if (value.getKey() != null && value.getKey() > 0) {
                    MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.NEW_ASSETS_STATUS, value.getValue());
                    if (!statusData.isFail()) {
                        JsonNode jsonNode = (JsonNode) statusData.getData();
                        if (jsonNode.size() > 0) {
                            for (JsonNode node : jsonNode) {
                                Integer lastvalue = node.get("lastvalue").asInt();
                                String hostId = node.get("hostid").asText();
                                String name = node.get("name").asText();
                                if((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)){
                                    String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                    statusMap.put(value.getKey() + ":" + hostId, status);
                                    hostSets.add(hostId);
                                }
                                if(hostSets.contains(hostId)){
                                    continue;
                                }
                                String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                statusMap.put(value.getKey() + ":" + hostId, status);
                            }
                        }
                    }
                }
            }
            String status = "";
            for (MwOutbandAssetsTable outbandAssetsTable : mwOutbandAssetses) {
                if (outbandAssetsTable.getMonitorFlag()) {
                    String s = statusMap.get(outbandAssetsTable.getMonitorServerId() + ":" + outbandAssetsTable.getAssetsId());
                    if (s != null && StringUtils.isNotEmpty(s)) {
                        status = s;
                    } else {
                        status = "UNKNOWN";
                    }
                } else {
                    status = "SHUTDOWN";
                }
                outbandAssetsTable.setItemAssetsStatus(status);
            }
        }
    }

    /**
     * 模糊查询带外资产
     * @return
     */
    @Override
    public Reply outBandAssetsFuzzyQuery() {
        //查询所有字段信息
        Map pubCriteria = new HashMap();
        List<MwOutbandAssetsTable> mwOutbandAssetsTables = mwOutbandAssetsTableDao.selectPubList(pubCriteria);
        Map<String,List<String>> filedMap = new HashMap<>();
        if(CollectionUtils.isEmpty(mwOutbandAssetsTables)) return Reply.ok(filedMap);
        Set<String> all = new HashSet<>();
        Set<String> assetsNames = new HashSet<>();
        Set<String> ipAddresss = new HashSet<>();
        Set<String> specificationss = new HashSet<>();
        Set<String> descriptions = new HashSet<>();
        Set<String> creators = new HashSet<>();
        Set<String> modifiers = new HashSet<>();
        for (MwOutbandAssetsTable mwOutbandAssetsTable : mwOutbandAssetsTables) {
            String ipAddress = mwOutbandAssetsTable.getIpAddress();
            String assetsName = mwOutbandAssetsTable.getAssetsName();
            String assetsTypeName = mwOutbandAssetsTable.getAssetsTypeName();
            String assetsTypeSubName = mwOutbandAssetsTable.getAssetsTypeSubName();
            String specifications = mwOutbandAssetsTable.getSpecifications();
            String creator = mwOutbandAssetsTable.getCreator();
            String modifier = mwOutbandAssetsTable.getModifier();
            String description = mwOutbandAssetsTable.getDescription();
            all.add(ipAddress);
            all.add(assetsName);
            all.add(assetsTypeName);
            all.add(assetsTypeSubName);
            all.add(specifications);
            all.add(creator);
            all.add(modifier);
            assetsNames.add(assetsName);
            ipAddresss.add(ipAddress);
            specificationss.add(specifications);
            creators.add(creator);
            modifiers.add(modifier);
            descriptions.add(description);
        }
        ArrayList<String> listAll = new ArrayList<>(all);
        Collections.sort(listAll);
        ArrayList<String> listAssetsNames = new ArrayList<>(assetsNames);
        Collections.sort(listAssetsNames);
        ArrayList<String> listIpAddresss = new ArrayList<>(ipAddresss);
        Collections.sort(listIpAddresss);
        ArrayList<String> listSpecificationss = new ArrayList<>(specificationss);
        Collections.sort(listSpecificationss);
        ArrayList<String> listCreators = new ArrayList<>(creators);
        Collections.sort(listCreators);
        ArrayList<String> listModifiers = new ArrayList<>(modifiers);
        Collections.sort(listModifiers);
        ArrayList<String> listDescriptions = new ArrayList<>(descriptions);
        Collections.sort(listDescriptions);
        filedMap.put("fuzzyQuery",listAll);
        filedMap.put("assetsName",listAssetsNames);
        filedMap.put("ipAddress",listIpAddresss);
        filedMap.put("specifications",listSpecificationss);
        filedMap.put("creator",listCreators);
        filedMap.put("modifier",listModifiers);
        filedMap.put("description",listDescriptions);
        return Reply.ok(filedMap);
    }
}
