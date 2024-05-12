package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwOutbandAssetsTableDao;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.model.MwOutbandAssetsTable;
import cn.mw.monitor.assets.service.thread.GetDataThread;
import cn.mw.monitor.common.util.*;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.service.MwAssetsMainTainService;
import cn.mw.monitor.service.assets.service.MwAssetsVirtualService;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.*;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.MWUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/6/3 14:07
 */
@Service
@Slf4j
class MwAssetsManagerImpl implements MwAssetsManager {

    @Value("${monitor.assets.debug}")
    private boolean debug;

    @Resource
    MwTangibleAssetsTableDao mwTangibleAssetsTableDao;

    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    MwLabelManageService mwLabelManageService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Resource
    private MwOutbandAssetsTableDao mwOutbandAssetsTableDao;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwTangibleAssetsService tangibleAssetsService;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    @Autowired
    private MWCommonService commonService;

    @Autowired
    private MwAssetsMainTainService mwAssetsMainTainService;


    public static final int ADMIN = 106;

    public Map<String, Object> getAssetsByUserId(Integer userId) {
        try {
            MwCommonAssetsDto mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).assetsTypeId(0).build();
            return getAssetsByUserId(mwCommonAssetsDto);
        } catch (Exception e) {
            log.error("fail to getAssetsByUserId with userId={}, cause:{}", userId, e.getMessage());
            return null;
        }

    }

    @Autowired
    private MwAssetsVirtualService mwVirtualService;

    private static final String HOSTNAME = "hostName";

    private static final String GROUPIDS = "groupIds";


    /**
     * 资产类型
     *
     * @param mwCommonAssetsDto
     * @return
     */
    public List<MwTangibleassetsTable> getAllAssetsByUserId(MwCommonAssetsDto mwCommonAssetsDto) {
        try {
            long time = System.currentTimeMillis();
            Integer userId = mwCommonAssetsDto.getUserId();
            String loginName = mwUserCommonService.getLoginNameByUserId(userId);
            String perm = mwUserCommonService.getRolePermByUserId(userId);
            List<Integer> groups = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            mwCommonAssetsDto.setGroups(groups);
            mwCommonAssetsDto.setLoginName(loginName);
            mwCommonAssetsDto.setPerm(perm);
            if(userId == null || userId == ADMIN){
                mwCommonAssetsDto.setSkipDataPermission(true);
            }
            QueryTangAssetsParam qparam = new QueryTangAssetsParam();
            org.springframework.beans.BeanUtils.copyProperties(mwCommonAssetsDto,qparam);
            log.info("资产查询参数:" + qparam);
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = getAssetsTable(qparam);
            log.info("getAssetsByUserId 花费总时间：" + (System.currentTimeMillis() - time));
            List<MwOutbandAssetsTable> mwOutbandAssetses = getOutbandAssetsByUserId(mwCommonAssetsDto);
            if(CollectionUtils.isNotEmpty(mwOutbandAssetses)){
                for(MwOutbandAssetsTable s : mwOutbandAssetses){
                    MwTangibleassetsTable temp = new MwTangibleassetsTable();
                    BeanUtils.copyProperties(temp,s);

                    temp.setInBandIp(s.getIpAddress());
                    temp.setIsJump(false);
                    mwTangibleassetsDTOS.add(temp);
                }
            }
            List<MwTangibleassetsTable> vrAssetsList = getVRAssetsByUserId(mwCommonAssetsDto);
            if(CollectionUtils.isNotEmpty(vrAssetsList)){
                mwTangibleassetsDTOS.addAll(vrAssetsList);
            }
            mwTangibleassetsDTOS = mwTangibleassetsDTOS.stream().filter(MwTangibleassetsTable -> MwTangibleassetsTable.getAssetsId() != null).collect(Collectors.toList());
            log.info("getAllAssetsByUserId 花费总时间：" + (System.currentTimeMillis() - time));
            return mwTangibleassetsDTOS;
        } catch (Exception e) {
            log.error("fail to getAssetsByUserId with mwCommonAssetsDto={}, cause:{}", mwCommonAssetsDto, e);
           return null;
        }

    }


    public Map<Integer, List<String>> getAssetsByServerId(List<MwTangibleassetsTable> mwTangibleassetsDTOS){
        Map<Integer, List<String>> result = new HashMap<>();
        Map<Integer,List<MwTangibleassetsTable>> collect = new HashMap<>();
        collect = mwTangibleassetsDTOS.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0).collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId));
        Map<Integer,List<MWMainTainHostView>> hostViewsCollect = new HashMap<>();
        List<MWMainTainHostView> hostViews = new ArrayList<>();
        if(modelAssetEnable){
            hostViews = mwAssetsMainTainService.getUnderMaintenanceHost();
        }
        if(CollectionUtils.isNotEmpty(hostViews)){
            log.info("维护中的资产数量：" + hostViews.size());
            log.info("维护中的资产信息：" + hostViews);
            hostViewsCollect = hostViews.stream().filter(item-> item.getServerId() != null &&  item.getServerId() != 0).collect(Collectors.groupingBy(MWMainTainHostView::getServerId));
        }
        if(debug) {
            log.info("mwTangibleassetsDTOS{}", mwTangibleassetsDTOS);
            log.info("collect{}", collect);
        }
        for (Integer key : collect.keySet()) {
            //资产表中monitorServerId=0说明没有关联资产
            if (null != key && key != 0) {
                List<MwTangibleassetsTable> mwTangibleassetsTables = collect.get(key);

                List<MWMainTainHostView> hostViewList = hostViewsCollect.get(key);
                List<String> list = new ArrayList<>();
                List<MwTangibleassetsTable> temp = mwTangibleassetsTables.stream()
                        .filter(mwTangibleassets ->CollectionUtils.isEmpty(hostViewList) || hostViewList.stream().noneMatch(hostView -> hostView.getHostId().equals(mwTangibleassets.getAssetsId())))
                        .collect(Collectors.toList());

                for (MwTangibleassetsTable dto : temp) {
                    list.add(dto.getAssetsId());
                }
                result.put(key, list);
            }
        }
        return result;
    }


    public List<MwOutbandAssetsTable> getOutbandAssetsByUserId(MwCommonAssetsDto mwCommonAssetsDto){
        try {
            log.info("带外资产获取");
            long time = System.currentTimeMillis();
            Integer userId = mwCommonAssetsDto.getUserId();
            String loginName = mwCommonAssetsDto.getLoginName();
            String perm = mwCommonAssetsDto.getPerm();
            List<Integer> groups = mwCommonAssetsDto.getGroups();
            List<MwOutbandAssetsTable> mwOutbandAssetses = new ArrayList<>();
            if (null != groups && groups.size() > 0) {
                mwCommonAssetsDto.setGroupIds(groups);
            }
            if (perm.equals(AlertEnum.PRIVATE.toString())) {
                mwCommonAssetsDto.setUserId(userId);
                Map<String, Object> pubCriteria1 = PropertyUtils.describe(mwCommonAssetsDto);
                mwOutbandAssetses = mwOutbandAssetsTableDao.selectPriList(pubCriteria1);
            } else {
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
                    mwCommonAssetsDto.setOrgIds(orgIds);
                }
                mwCommonAssetsDto.setIsAdmin(isAdmin);
                log.info("过滤条件"+mwCommonAssetsDto);
                Map<String, Object> pubCriteria1 = PropertyUtils.describe(mwCommonAssetsDto);
                mwOutbandAssetses = mwOutbandAssetsTableDao.selectPubList(pubCriteria1);
            }
            Map<String,MwOutbandAssetsTable> assetsMap = new HashMap();
            List<String> id = new ArrayList<>();
            List<String> hostId = new ArrayList<>();
            for (MwOutbandAssetsTable outbandAssetsTable : mwOutbandAssetses){
                id.add(outbandAssetsTable.getId());
                assetsMap.put(outbandAssetsTable.getId(),outbandAssetsTable);
                hostId.add(outbandAssetsTable.getAssetsId());
            }
            List<cn.mw.monitor.bean.DataPermission> dataAuthByIds = commonService.getDataAuthByIds(DataType.OUTBANDASSETS, id);
            for (cn.mw.monitor.bean.DataPermission dataAuthById : dataAuthByIds) {

                MwOutbandAssetsTable outbandAssetsTable = assetsMap.get(dataAuthById.getId());
                //机构转换
                List<cn.mw.monitor.service.user.dto.OrgDTO> department = dataAuthById.getDepartment();
                List<OrgDTO> assetsOrgDTOs = CopyUtils.copyList(OrgDTO.class, department);
                outbandAssetsTable.setDepartment(assetsOrgDTOs);
                outbandAssetsTable.setGroup(dataAuthById.getGroups());
            }
            log.info("getOutbandAssetsByUserId mwOutbandAssetses：" + mwOutbandAssetses);
            log.info("getOutbandAssetsByUserId hostId：" + hostId);
            log.info("getOutbandAssetsByUserId 花费总时间：" + (System.currentTimeMillis() - time));
            return mwOutbandAssetses;
        }catch (Exception e){
            log.error("getOutbandAssetsByUserId error:{}",e.getMessage());
            return null;
        }

    }

    public  List<MwTangibleassetsTable> getVRAssetsByUserId(MwCommonAssetsDto mwCommonAssetsDto) throws Exception {
        long time = System.currentTimeMillis();
        Integer userId = mwCommonAssetsDto.getUserId();
        String loginName = mwCommonAssetsDto.getLoginName();
        List<VHostTreeDTO> assetsInfos;
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        Reply reply;
        if(userId == ADMIN){
            reply = mwVirtualService.getAllTree(AlertEnum.VHost.toString(), AlertAssetsEnum.Zero.toString());
        }else {
            reply = mwVirtualService.getAllInventedAssets(AlertEnum.VHost.toString(),mwUserOrgCommonService.getRoleIdByLoginName(loginName),userId);
        }

        if (null != reply) {
            assetsInfos = (List<VHostTreeDTO>) reply.getData();
            if(CollectionUtils.isEmpty(assetsInfos)) return mwTangibleassetsDTOS;
            //根据zabbixid分组查询虚拟化
            log.info("虚拟化进程1");
            Map<Integer, List<VHostTreeDTO>> assetsInfosMap = assetsInfos.stream().collect(Collectors.groupingBy(VHostTreeDTO::getMonitorServerId));
            for (Map.Entry<Integer, List<VHostTreeDTO>> assetsMap : assetsInfosMap.entrySet()) {
                List<VHostTreeDTO> hostDtos = assetsMap.getValue();
                List<GroupHosts> hostIdList = new ArrayList<>();
                QueryHostParam qParam = new QueryHostParam();
                List<GroupHosts> vmList = new ArrayList<>();
                for (VHostTreeDTO s : hostDtos) {
                    log.info("虚拟化进程2");
                    MwTangibleassetsTable assetsTable = new MwTangibleassetsTable();
                    assetsTable.setAssetsId(s.getAssetHostId());
                    assetsTable.setMonitorServerName(s.getMonitorServerName());
                    assetsTable.setMonitorServerId(s.getMonitorServerId());
                    assetsTable.setInBandIp(s.getIp());
                    mwTangibleassetsDTOS.add(assetsTable);
                    if (CollectionUtils.isNotEmpty(s.getHostList())) {
                        hostIdList.addAll(s.getHostList());
                    }
                    if (CollectionUtils.isNotEmpty(s.getVmList())) {
                        vmList.addAll(s.getVmList());
                    }
                }
                qParam.setMonitorServerId(assetsMap.getKey());
                qParam.setVmList(vmList);
                Reply replyVr = mwVirtualService.getVMsInfoList(qParam);
                List<VirtualTableDto> virtualTable = (List<VirtualTableDto>) replyVr.getData();
                if (CollectionUtils.isNotEmpty(virtualTable)) {
                    log.info("虚拟化进程3");
                    List<String> id = new ArrayList<>();
                    HashMap<String,VirtualTableDto> vrMap = new HashMap<>();
                    for (VirtualTableDto vr : virtualTable) {
                        String key = "vm_" + vr.getHostId() + "_" + vr.getIpAddress() + "_" + assetsMap.getKey();
                        id.add(key);
                        vrMap.put(key,vr);
                    }
                    List<cn.mw.monitor.bean.DataPermission> dataAuthByIds = commonService.getDataAuthByIds(DataType.VIRTUAL, id);
                    log.info("虚拟化进程4");
                    for (cn.mw.monitor.bean.DataPermission dataAuthById : dataAuthByIds) {
                        log.info("虚拟化进程5");
                        VirtualTableDto vr = vrMap.get(dataAuthById.getId());
                        //机构转换
                        List<cn.mw.monitor.service.user.dto.OrgDTO> department = dataAuthById.getDepartment();
                        List<OrgDTO> assetsOrgDTOs = CopyUtils.copyList(OrgDTO.class, department);
                        MwTangibleassetsTable temp = new MwTangibleassetsTable();
                        temp.setAssetsId(vr.getHostId());
                        temp.setInBandIp(vr.getIpAddress());
                        temp.setMonitorServerId(assetsMap.getKey());
                        temp.setMonitorServerName(hostDtos.get(0).getMonitorServerName());
                        temp.setIsJump(false);
                        temp.setAssetsTypeName(AlertEnum.VR.toString());
                        temp.setAssetsName(vr.getHostName());
                        temp.setDepartment(assetsOrgDTOs);
                        temp.setGroup(dataAuthById.getGroups());
                        temp.setId(dataAuthById.getId());
                        mwTangibleassetsDTOS.add(temp);
                    }
                }
                List<String> hostId = new ArrayList<>();
                HashMap<String,GroupHosts> hostMap = new HashMap<>();
                for (GroupHosts gs : hostIdList) {
                    String key = "host_" + gs.getHostid() + "_" + gs.getName() + "_" + assetsMap.getKey();
                    hostId.add(key);
                    hostMap.put(key,gs);
                }
                List<cn.mw.monitor.bean.DataPermission> dataAuthByIds = commonService.getDataAuthByIds(DataType.VIRTUAL, hostId);
                log.info("虚拟化进程6");
                for (cn.mw.monitor.bean.DataPermission dataAuthById : dataAuthByIds) {
                    log.info("虚拟化进程7");
                    GroupHosts gs = hostMap.get(dataAuthById.getId());
                    //机构转换
                    List<cn.mw.monitor.service.user.dto.OrgDTO> department = dataAuthById.getDepartment();
                    List<OrgDTO> assetsOrgDTOs = CopyUtils.copyList(OrgDTO.class, department);
                    MwTangibleassetsTable temp = new MwTangibleassetsTable();
                    temp.setAssetsId(gs.getHostid());
                    temp.setInBandIp(gs.getName());
                    temp.setMonitorServerId(assetsMap.getKey());
                    temp.setMonitorServerName(hostDtos.get(0).getMonitorServerName());
                    temp.setIsJump(false);
                    temp.setAssetsTypeName(AlertEnum.VR.toString());
                    temp.setAssetsName(gs.getName());
                    temp.setDepartment(assetsOrgDTOs);
                    temp.setGroup(dataAuthById.getGroups());
                    temp.setId(dataAuthById.getId());
                    mwTangibleassetsDTOS.add(temp);
                }
            }
        }
        log.info("getVRAssetsByUserId 花费总时间：" + (System.currentTimeMillis() - time));
        return mwTangibleassetsDTOS;
    }



    /**
     * 资产类型
     *
     * @param mwCommonAssetsDto
     * @return
     */
    public Map<String, Object> getAssetsByUserId(MwCommonAssetsDto mwCommonAssetsDto) {
        try {
            long time = System.currentTimeMillis();
            Integer userId = mwCommonAssetsDto.getUserId();
            Integer assetsTypeId = mwCommonAssetsDto.getAssetsTypeId();
            String loginName = mwUserCommonService.getLoginNameByUserId(userId);
            String perm = mwUserCommonService.getRolePermByUserId(userId);
            List<Integer> groups = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != assetsTypeId && assetsTypeId != 0) {
                mwCommonAssetsDto.setAssetsTypeId(assetsTypeId);
            }
            if (null != mwCommonAssetsDto.getAssetsId()) {
                mwCommonAssetsDto.setAssetsId(mwCommonAssetsDto.getAssetsId());
            }
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
            if (null != groups && groups.size() > 0) {
                mwCommonAssetsDto.setGroupIds(groups);
            }
            List<String> assetsIds = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            if (StringUtils.isNotBlank(mwCommonAssetsDto.getFilterLabelId()) ) {
                List<Integer> filterLabelIds  = JSON.parseObject(mwCommonAssetsDto.getFilterLabelId(), List.class);
                //资产过滤时根据标签查资产
                assetsIds = mwLabelManageService.getAssetsIdByLabel(filterLabelIds, DataType.ASSETS.getName());
                if (assetsIds!=null&&assetsIds.size() > 0) {
                    mwCommonAssetsDto.setAssetsIds(assetsIds);
                }else{
                    return map;
                }
            }

            DataPermission dataPermission = DataPermission.valueOf(perm);
            mwTangibleassetsDTOS = tangibleAssetsService.doSelectAssets(mwCommonAssetsDto,dataPermission,loginName,userId);
            mwTangibleassetsDTOS = mwTangibleassetsDTOS.stream().filter(dto -> StringUtils.isNotBlank(dto.getAssetsId())).collect(Collectors.toList());
            ArrayList<String> assetIds = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();
            mwTangibleassetsDTOS.forEach(assets -> {
                assetIds.add(assets.getAssetsId());
            });
            mwTangibleassetsDTOS.forEach(assets -> {
                ids.add(assets.getId());
            });
            //log.info("根据用户获取资产：" + mwTangibleassetsDTOS);
            map.put("assetsList", mwTangibleassetsDTOS);
            map.put("assetIds", assetIds);
            map.put("ids", ids);
            log.info("getAssetsByUserId 花费总时间：" + (System.currentTimeMillis() - time));
            return map;
        } catch (Exception e) {
            log.error("fail to getAssetsByUserId with mwCommonAssetsDto={}, cause:{}", mwCommonAssetsDto, e.getMessage());
            return null;
        }
    }




    @Override
    public Map<Integer, List<String>> getAssetsByServerId(Map<String, Object> map) {
        Object assetsList = null;
        if(null!=map) {
            assetsList= map.get("assetsList");
        }

        if(debug) {
            log.info("assetsList{}", assetsList);
        }
        Map<Integer, List<String>> newMap = new HashMap<>();
        if (null != assetsList) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            newMap = getAssetsByServerId(mwTangibleassetsDTOS);
        }

        if(debug) {
            log.info("newMap----------{}", newMap);
        }
        return newMap;
    }


    public List<MwAssetsIdsDTO> getAssetsIds(Boolean deleteFlag) {
        try {
            return mwTangibleAssetsTableDao.selectAllAssetsIds(deleteFlag);
        } catch (Exception e) {
            log.error("fail to getAssetsHostIds, cause:{}", e);
            return null;
        }

    }


    public List<String> getLogHostList(MwCommonAssetsDto mwCommonAssetsDto) {
        List<String> hostIpList = new ArrayList<>();
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> assets = getAssetsByUserId(mwCommonAssetsDto);
        //根据机构过滤资产
        if(StringUtils.isNotBlank(mwCommonAssetsDto.getFilterOrgId())&&assets!=null){
            assets=getAssetByOrgId(assets,mwCommonAssetsDto.getFilterOrgId());
        }
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        if (null != assets) {
            Object assetsList = assets.get("assetsList");
            if (null != assetsList) {
                mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            }
        }
        for (MwTangibleassetsTable assetsDto : mwTangibleassetsDTOS) {
            hostIpList.add(assetsDto.getInBandIp());
        }
        return hostIpList;
    }

    public List<String> getLogHostList1(MwCommonAssetsDto mwCommonAssetsDto) {
        List<List<String>> listList=new ArrayList<>();
        List<String> hostIpList = new ArrayList<>();
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> assets = getAssetsByUserId(mwCommonAssetsDto);
        //根据机构过滤资产
        if(StringUtils.isNotBlank(mwCommonAssetsDto.getFilterOrgId())&&assets!=null){
            assets=getAssetByOrgId(assets,mwCommonAssetsDto.getFilterOrgId());
        }
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        if (null != assets) {
            Object assetsList = assets.get("assetsList");
            if (null != assetsList) {
                mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            }
        }
        for (MwTangibleassetsTable assetsDto : mwTangibleassetsDTOS) {
            hostIpList.add(assetsDto.getInBandIp()+","+assetsDto.getId());
        }

        return hostIpList;
    }

    public Map<String,Object> getAssetByOrgId(Map<String,Object> assets,String assetOrgId){
        List<MwTangibleassetsTable> mwTangibleassetsDTOS;
        List<String> assetIds =new ArrayList<>();
        List<String> ids =new ArrayList<>();
        List<MwTangibleassetsTable> result=new ArrayList<>();
        List<List<Integer>> filterOrgIds = JSON.parseObject(assetOrgId, List.class);
        if(assets.get("assetsList")!=null){
            mwTangibleassetsDTOS= (List<MwTangibleassetsTable>) assets.get("assetsList");
            if(mwTangibleassetsDTOS.size()>0){
                ExecutorService executorService = Executors.newFixedThreadPool(20);
                List<Future<MwTangibleassetsTable>> futureList = new ArrayList<>();
                for(MwTangibleassetsTable mw:mwTangibleassetsDTOS){
                    GetDataThread<MwTangibleassetsTable> getDataByCallable=new GetDataThread<MwTangibleassetsTable>() {
                        @Override
                        public MwTangibleassetsTable call() throws Exception {
                            //根据资产id获取对应机构，一个资产至少包含一个机构
                            MwTangibleassetsDTO assetsAndOrgs = getAssetsAndOrgs(mw.getId());
                            if(null!=assetsAndOrgs){
                                if(assetsAndOrgs.getDepartment().size()>0){
                                    List<OrgDTO> department = assetsAndOrgs.getDepartment();
                                    //一个资产多个机构
                                    List<Integer> ss=new ArrayList<>();
                                    for (OrgDTO o : department) {
                                        if(null!=o){
                                            ss.add(o.getOrgId());
                                        }
                                    }
                                    if(ss.size()>0){
                                        for (List<Integer> filterOrgId : filterOrgIds) {
                                            if(ss.contains(filterOrgId.get(filterOrgId.size()-1))){
                                                return mw;
                                            }
                                        }
                                        return null;
                                    }
                                }
                            }
                            return null;
                        }
                    };
                    if(null!=getDataByCallable){
                        Future<MwTangibleassetsTable> f = executorService.submit(getDataByCallable);
                        futureList.add(f);
                    }
                }

                for (Future<MwTangibleassetsTable> item : futureList) {
                    try {
                        MwTangibleassetsTable mt = item.get(30, TimeUnit.SECONDS);
                        if(mt!=null){
                            result.add(mt);
                        }
                    } catch (Exception e) {
                        item.cancel(true);
                        executorService.shutdown();
                    }
                }
                executorService.shutdown();
            }
            if(result.size()>0){
                result.forEach(s->assetIds.add(s.getAssetsId()));
                result.forEach(s->ids.add(s.getId()));
            }
        }
        mwTangibleassetsDTOS=result;
        Map<String, Object> map = new HashMap<>();
        map.put("assetsList", mwTangibleassetsDTOS);
        map.put("assetIds", assetIds);
        map.put("ids", ids);
        return map;
    }

    @Override
    public MwAssetsIdsDTO selectAssetsByIp(String linkTargetIp) {
        try {
            if(!modelAssetEnable){
                return mwTangibleAssetsTableDao.selectAssetsByIp(linkTargetIp);
            }
            //根据IP和ICMP类型查询对应资产
            QueryModelAssetsParam queryModelAssetsParam = new QueryModelAssetsParam();
            queryModelAssetsParam.setInBandIp(linkTargetIp);
            queryModelAssetsParam.setMonitorMode(4);
            List<MwTangibleassetsTable> modelAssets = mwModelViewCommonService.findModelAssets(MwTangibleassetsTable.class, queryModelAssetsParam);
            if(CollectionUtils.isEmpty(modelAssets)){return  null;}
            MwAssetsIdsDTO mwAssetsIdsDTO = new MwAssetsIdsDTO();
            mwAssetsIdsDTO.extractFrom(modelAssets.get(0));
            return mwAssetsIdsDTO;
        }catch (Throwable e){
            log.error("MwAssetsManagerImpl {} selectAssetsByIp()",e);
            return  null;
        }
    }

    @Override
    public List<String> getAssetsByAction(MwCommonAssetsDto mwCommonAssetsDto) {
        Map<String, Object> pubCriteria = null;
        List<String> ids = new ArrayList<>();
        try {
            pubCriteria = PropertyUtils.describe(mwCommonAssetsDto);
            List<MwTangibleassetsTable> list = mwTangibleAssetsTableDao.selectPubList(pubCriteria);
            if (null != list && list.size() > 0) {
                for (MwTangibleassetsTable assets : list) {
                    ids.add(assets.getId());
                }
            }
        } catch (Exception e) {
            log.error("getAssetsByAction{}", e);
        }
        return ids;
    }

    @Override
    public List<MwTangibleassetsDTO> getAssetsByAssetsTypeId(Integer assetsTypeId) {
        List<MwTangibleassetsDTO> dtos = mwTangibleAssetsTableDao.selectAssetsByAssetsTypeId(assetsTypeId);
        return dtos;
    }

    @Override
    public List<MwTangibleassetsTable> getAssetsTable(QueryTangAssetsParam qParam) {
        List<MwTangibleassetsTable> mwTangAssetses = new ArrayList<>();
        try {
            qParam.setPageNumber(-1);
            qParam.setPageSize(0);
            Integer userId=qParam.getUserId();
            if(userId.equals(106)){
                qParam.setSkipDataPermission(true);
            }
            String loginName = mwUserCommonService.getLoginNameByUserId(userId);
            String perm = mwUserCommonService.getRolePermByUserId(userId);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            DataPermission dataPermission = DataPermission.valueOf(perm);
            if (null != groupIds && groupIds.size() > 0) {
                qParam.setGroupIds(groupIds);
            }
            mwTangAssetses = tangibleAssetsService.doSelectAssets(qParam,dataPermission,loginName,userId);
            //加资产健康状态
//            if (mwTangAssetses != null && mwTangAssetses.size() > 0) {
//                Map<Integer, List<String>> groupMap = mwTangAssetses.stream()
//                        .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
//                Map<String, String> statusMap = new HashMap<>();
//                for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
//                    if (value.getKey() != null && value.getKey() > 0) {
//                        //有改动-zabbi
//                        MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.ASSETS_STATUS, value.getValue());
//                        if (!statusData.isFail()) {
//                            JsonNode jsonNode = (JsonNode) statusData.getData();
//                            if (jsonNode.size() > 0) {
//                                for (JsonNode node : jsonNode) {
//                                    Integer lastvalue = node.get("lastvalue").asInt();
//                                    String hostId = node.get("hostid").asText();
//                                    String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
//                                    statusMap.put(value.getKey() + ":" + hostId, status);
//                                }
//                            }
//                    }
//                        /*statusMap.put(value.getKey() + ":" + value.getValue(), "ABNORMAL");*/
//                    }
//                }
//                String status = "";
//                for (MwTangibleassetsTable asset : mwTangAssetses) {
//                    String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
//                    if (s != null && StringUtils.isNotEmpty(s)) {
//                        status = s;
//                    } else {
//                        status = "UNKNOWN";
//                    }
//                    asset.setItemAssetsStatus(status);
//                }
//                log.info("ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);
//            }
        } catch (Exception e) {
            log.error("ScreengetAssetsTable{}", e);
        }
        return mwTangAssetses;
    }

    @Override
    public MwTangibleassetsDTO getAssetsAndOrgs(String assetsId) {
        try {
            if(modelAssetEnable){
                QueryModelAssetsParam param = new QueryModelAssetsParam();
                param.setModelInstanceId(Integer.parseInt(assetsId));
                List<MwTangibleassetsDTO> datas = mwModelViewCommonService.findModelAssets(MwTangibleassetsDTO.class, param);
                if(CollectionUtils.isEmpty(datas)){return null;}
                cn.mw.monitor.bean.DataPermission dataPermission = commonService.getDataPermission(DataType.INSTANCE_MANAGE, datas.get(0).getId());
                List<OrgDTO> assetsOrgDTOs = CopyUtils.copyList(OrgDTO.class, dataPermission.getDepartment());
                datas.get(0).setDepartment(assetsOrgDTOs);
                return datas.get(0);
            }
            return mwTangibleAssetsTableDao.selectById(assetsId);
        }catch (Throwable e){
            log.error("根据主机ID查询资产失败");
            return null;
        }

    }

    @Override
    public Boolean checkNowItems(int monitorServerId, String hostId) {
        List<String> itemIds = new ArrayList<>();
        //所有自动发现规则的监控项id
        MWZabbixAPIResult resultDRule = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostId);
        if (!resultDRule.isFail()) {
            JsonNode jsonNode = (JsonNode) resultDRule.getData();
            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String itemId = node.get("itemid").asText();
                    itemIds.add(itemId);
                }
            }
        }
        //所有的监控项id
        MWZabbixAPIResult resultItem = mwtpServerAPI.itemGetbyHostId(monitorServerId, hostId);
        if (!resultItem.isFail()) {
            JsonNode jsonNode = (JsonNode) resultItem.getData();
            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String itemId = node.get("itemid").asText();
                    itemIds.add(itemId);
                }
            }
        }

        //将所有的监控项进行是否立即执行 "6"是立即执行；"1"是诊断信息
        MWZabbixAPIResult result = mwtpServerAPI.taskItems(monitorServerId, "6", itemIds);
        if (!result.isFail()) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据传入字段查询对应数据
     * @param fields
     * @return
     */
    @Override
    public List<Map<String, Object>> getAssetsFieldData(List<String> fields) {
        List<Map<String, Object>> realMap = new ArrayList<>();
        if(CollectionUtils.isEmpty(fields)) return realMap;
        return mwTangibleAssetsTableDao.getAssetsFieldData(fields);
    }


}
