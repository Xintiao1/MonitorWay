package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwAssetsMainTainDao;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.service.assets.service.MwAssetsMainTainService;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.GzipTool;
import cn.mw.monitor.util.MWUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwAssetsMainTainServiceImpl
 * @Description 猫维资产管理维护的service操作
 * @Author gengjb
 * @Date 2021/7/26 15:32
 * @Version 1.0
 **/
@Service
@Transactional
public class MwAssetsMainTainServiceImpl implements MwAssetsMainTainService {

    private static final Logger logger = LoggerFactory.getLogger("MwAssetsMainTainServiceImpl");

    @Autowired
    private MWTPServerAPI zabbixApi;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Resource
    private MwAssetsMainTainDao mainTainDao;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MwTangibleAssetsService mwTangService;

    private final String HOST_ID = "hostid";

    private final String HOST = "hosts";

    @Autowired
    private MwModelViewCommonService viewCommonService;

    @Autowired
    private MWUserService userService;

    @Autowired
    private MWUserCommonService commonService;

    /**
     * 资产管理中维护节点的新增
     * @param param 添加主机维护的参数
     * @return
     */
    @Override
    public Reply addAssetsMainTain(MwAssetsMainTainParamV1 param) {
        try{
            //根据id判断是否编辑,先删除后添加
            if(null != param.getId()){
                List<MwAssetsMainTainDelParam> idList = new ArrayList<>();
                MwAssetsMainTainParam mainTainParam = new MwAssetsMainTainParam();
                mainTainParam.setId(param.getId());
                Reply reply1 = selectMainTainHostInfo(mainTainParam);
                if(null == reply1){
                    return  Reply.warn("数据被编辑过");
                }

                if(PaasConstant.RES_SUCCESS != reply1.getRes()){
                    return  reply1;
                }

                MwAssetsMainTainViewV1 selParam = (MwAssetsMainTainViewV1) reply1.getData();
                for(MWMainTainHostParam host : selParam.getHostids()){
                    MwAssetsMainTainDelParam delParam = new MwAssetsMainTainDelParam();
                    delParam.setId(param.getId());
                    delParam.setMaintenid(host.getMaintenanceid());
                    delParam.setServerId(host.getServerId());
                    idList.add(delParam);
                }
                deleteAssetsMainTain(idList);
            }

            List<MWMainTainHostParam> host = param.getHostids();
            if(CollectionUtils.isEmpty(host)){
                return  Reply.fail("维护资产不可为空");
            }
            String name = param.getName();
            //查询是否名称重复
            Boolean repeatName = nameFilter(name,null);
            if(!repeatName){
               return  Reply.fail("该名称已存在，请重新输入");
            }
            //组合参数信息
            Map<String,Object> mainTenParamMap = new HashMap<String,Object>();

            List<HashMap> newtimes = new ArrayList<>();
            MwAssetsMainTainParam mwAssetsMainTainParam = new MwAssetsMainTainParam();
            mwAssetsMainTainParam.extractFrom(param);
            setMainTainBasicParam(mainTenParamMap, mwAssetsMainTainParam,newtimes);
            Map<Integer,List<String>> hostAndServerIdMap = (Map<Integer, List<String>>) mainTenParamMap.get("hostids");
            for (Integer serverId : hostAndServerIdMap.keySet()) {
                List<String> hosts = hostAndServerIdMap.get(serverId);
                mainTenParamMap.put(HOST,setHostParam(hosts));
                mainTenParamMap.put("hostids",hosts);
                logger.info("MwAssetsMainTainServiceImpl{} addAssetsMainTain() serverId:::"+serverId+ "times::"+newtimes);
                //不能对time进行修改，多循环需要用到
                MWZabbixAPIResult zabbixAPIResult = zabbixApi.maintenanceCreate(serverId,mainTenParamMap,new ArrayList<>(newtimes));
                if(zabbixAPIResult.getData() != null) {
                    JsonNode data = (JsonNode) zabbixAPIResult.getData();
                    if (data.size() > 0) {
                        data.forEach(node -> {
                            int maintenanceid = node.get(0).asInt();
                            List<MWMainTainHostParam> hostids = param.getHostids();
                            if(!CollectionUtils.isEmpty(hostids)){
                                for (MWMainTainHostParam hostid : hostids) {
                                    if(serverId.equals(hostid.getServerId())){
                                        hostid.setMaintenanceid(maintenanceid);
                                    }
                                }
                            }
                        });
                    }
                }
            }

            //压缩保存请求数据
            String formData = GzipTool.gzip(JSON.toJSONString(param));
            mwAssetsMainTainParam.setFormData(formData);

            //登录用户
            GlobalUserInfo globalUser = userService.getGlobalUser();
            mwAssetsMainTainParam.setCreator(globalUser.getUserName());
            //维护主机成功需将数据插入猫维数据库
            mainTainDao.addAssetsMainTain(mwAssetsMainTainParam);
            //添加主机数据
            addMainTianHostData(mwAssetsMainTainParam,globalUser.getUserName());

            if(null != param.getId()){
                return Reply.ok("数据编辑成功");
            }

            return Reply.ok("数据添加成功");

        }catch (Exception e){
            logger.error("数据维护失败，调用zabbixAPI失败：",e);
            return Reply.fail("数据维护失败");
        }
    }


    private List<Map> setHostParam(List<String> hostIds){
        //增加6.0版本host参数
        List<Map> hosts = new ArrayList<>();
        for (String hostId : hostIds) {
            Map map = new HashMap();
            map.put(HOST_ID,hostId);
            hosts.add(map);
        }
        return hosts;
    }

    /**
     * 设置zabbix维护的基本参数
     * @param mainTenParamMap 参数的集合
     * @param param 页面传过来的参数数据
     */
    private void setMainTainBasicParam(Map<String,Object> mainTenParamMap,MwAssetsMainTainParam param,List<HashMap> newtimes) throws ParseException {
        //设置维护名称
        mainTenParamMap.put("name",param.getName());
        //设置启用自从
        //将时间类型改为秒值
        long activeSince = param.getActiveSince().getTime() / 1000;
        mainTenParamMap.put("activeSince",activeSince);
        //将时间类型改为秒值
        long activeTill = param.getActiveTill().getTime() / 1000;
        mainTenParamMap.put("activeTill",activeTill);
        //设置描述字段
        mainTenParamMap.put("description",param.getDescription());
        //设置维护类型
        mainTenParamMap.put("maintenanceType",param.getMaintenanceType());

        //设置主机
        List<MWMainTainHostParam> hostParams = param.getHostids();
        Map<Integer,List<String>> hostAndServerIdMap = new HashMap<>();
        for (MWMainTainHostParam hostParam : hostParams) {
            Integer serverId = hostParam.getServerId();
            List<String> hostids = hostAndServerIdMap.get(serverId);
            if(null == hostids){
                hostids = new ArrayList<>();
                hostAndServerIdMap.put(serverId,hostids);
            }
            hostids.add(hostParam.getHostId());
        }
        mainTenParamMap.put("hostids",hostAndServerIdMap);
        logger.info("MwAssetsMainTainServiceImpl{} setMainTainBasicParam() period::"+param.getMainTainPeriodParams());
        for(Object period : param.getMainTainPeriodParams()){
            MwAssetsMainTainZabbixParam timeperiod = new MwAssetsMainTainZabbixParam();
            timeperiod.extractFrom(period);
            HashMap value = timeperiod.getRequest();
            newtimes.add(value);
        }
    }

    /**
     * 添加主机数据
     * @param param 维护页面的桉树数据
     * @param loginName 登录名称
     */
    private void addMainTianHostData(MwAssetsMainTainParam param,String loginName){
        //资产管理维护的基础数据主键
        Integer id = param.getId();
        List<MWMainTainHostParam> hostids = param.getHostids();
        if(!CollectionUtils.isEmpty(hostids)){
            List<Map<String,Object>> hostList = new ArrayList<>();
            for (MWMainTainHostParam hostid : hostids) {
                Map<String,Object> hostMap = new HashMap<>();
                hostMap.put("maintenid",id);
                hostMap.put("hostid",hostid.getHostId());
                hostMap.put("hostName",hostid.getHostName());
                hostMap.put("serverId",hostid.getServerId());
                hostMap.put("maintenanceid",hostid.getMaintenanceid());
                hostMap.put("typeId",hostid.getTypeId());
                hostMap.put("creator",loginName);
                hostMap.put("modelInstanceId",hostid.getModelInstanceId());
                hostList.add(hostMap);
            }
            mainTainDao.addAssetsMainTainHost(hostList);
        }
    }

    /**
     * 添加主机组数据
     * @param param 维护页面的参数数据
     * @param loginName 登录名称
     */
    private void addMainTianHostGroupData(MwAssetsMainTainParam param,String loginName){
        //资产管理维护的基础数据主键
        Integer id = param.getId();
        List<String> groupids = param.getGroupids();
        if(!CollectionUtils.isEmpty(groupids)){
            List<Map<String,Object>> groupList = new ArrayList<>();
            for (String groupid : groupids) {
                Map<String,Object> groupMap = new HashMap<>();
                groupMap.put("maintenid",id);
                groupMap.put("groupid",groupid);
                groupMap.put("creator",loginName);
                groupList.add(groupMap);
            }
//            mainTainDao.addAssetsMainTainHostGroup(groupList);
        }
    }

    /**
     * 添加标记数据
     * @param param 维护页面的参数数据
     * @param loginName 登录名称
     */
    private void addMainTianTagData(MwAssetsMainTainParam param,String loginName){
        //资产管理维护的基础数据主键
        Integer id = param.getId();
        List<Map<String,Object>> tags = param.getTags();
        if(!CollectionUtils.isEmpty(tags)){
            for (Map<String, Object> tag : tags) {
                tag.put("maintenid",id);
                tag.put("creator",loginName);
            }
//            mainTainDao.addAssetsMainTainTags(tags);
        }
    }

    private void addMainTainTimesData(MwAssetsMainTainParam param,String loginName){
        List<HashMap> times = param.getTimes();
        if(!CollectionUtils.isEmpty(times)){
            for (HashMap time : times) {
                time.put("maintenid",param.getId());
                time.put("creator",loginName);
            }
//            mainTainDao.addAssetsMainTainTimes(times);
        }
    }


    /**
     * 查询数据
     * @param param 添加主机维护的参数
     * @return Reply
     */
    @Override
    public Reply selectAssetsMainTain(MwAssetsMainTainParam param) {
        try{
            List<MwAssetsMainTainView> viewList = new ArrayList<>();
//            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            //根据条件查询基础数据
            List<MwAssetsMainTainParam> mainTainList = mainTainDao.selectMainTain(param);
            //根据名称过滤数据
            if(!CollectionUtils.isEmpty(mainTainList)){
                //查询zabbix中的过期状态
                List<Integer> maintenids = new ArrayList<Integer>();
                MainStatusCallBack mainStatusCallBack = null;
                if(param.getStatus() != null && param.getStatus() != -1){
                    mainStatusCallBack = new MainStatusCallBack() {
                        @Override
                        public void callback(List<Integer> maintenids, Iterator<MwAssetsMainTainParam> iterator, MwAssetsMainTainParam mainTain, MwAssetsMainTainParam param) {
                            if(param.getStatus() != null && param.getStatus() == mainTain.getStatus()){
                                maintenids.add(mainTain.getMaintenanceid());
                            }else if(param.getStatus() != null && param.getStatus() != mainTain.getStatus()){
                                iterator.remove();
                            }else{
                                maintenids.add(mainTain.getMaintenanceid());
                            }
                        }
                    };
                }else {
                    mainStatusCallBack = new MainStatusCallBack() {
                        @Override
                        public void callback(List<Integer> maintenids, Iterator<MwAssetsMainTainParam> iterator, MwAssetsMainTainParam mainTain, MwAssetsMainTainParam param) {
                            maintenids.add(mainTain.getMaintenanceid());
                        }
                    };
                }

                viewList = getMaintainView(mainTainList ,param ,maintenids ,mainStatusCallBack);

                if(CollectionUtils.isEmpty(maintenids)){
                    PageInfo pageInfo = new PageInfo<>(viewList);
                    pageInfo.setList(viewList);
                    return Reply.ok(pageInfo);
                }

                getMainTainHostData(viewList);
            }
            //是否筛选过期数据
            if(param.getIsExpire() != null && param.getIsExpire()){
                List<MwAssetsMainTainView> exprieMainTains = viewList.stream().filter(item -> item.getStatus() == 2).collect(Collectors.toList());
                PageInfo pageInfo = mainTainPageHelper(exprieMainTains, param);
                return Reply.ok(pageInfo);
            }
            List<MwAssetsMainTainView> mainTainViews = viewList.stream().filter(item -> item.getStatus() != 2).collect(Collectors.toList());
            PageInfo pageInfo = mainTainPageHelper(mainTainViews, param);
            return Reply.ok(pageInfo);
        }catch (Exception e){
            logger.error("资产维护查询失败：",e);
            return Reply.fail("资产维护查询失败");
        }
    }


    private PageInfo mainTainPageHelper(List<MwAssetsMainTainView> mainTainViews,MwAssetsMainTainParam param){
        if(CollectionUtils.isNotEmpty(mainTainViews)){
            //根据分页信息分割数据
            Integer pageNumber = param.getPageNumber();
            Integer pageSize = param.getPageSize();
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > mainTainViews.size()){
                toIndex = mainTainViews.size();
            }
            List<MwAssetsMainTainView> mwAssetsMainTainViews = mainTainViews.subList(fromIndex, toIndex);
            PageInfo pageInfo = new PageInfo<>(mainTainViews);
            pageInfo.setPageSize(mainTainViews.size());
            pageInfo.setList(mwAssetsMainTainViews);
            pageInfo.setPageNum(mainTainViews.size());
            return pageInfo;
        }
        return new PageInfo();
    }

    private List<MwAssetsMainTainView> getMaintainView(List<MwAssetsMainTainParam> mainTainList
            ,MwAssetsMainTainParam param ,List<Integer> maintenids ,MainStatusCallBack statusCallBack) throws Exception{

        List<MwAssetsMainTainView> ret = new ArrayList<>();
        Iterator<MwAssetsMainTainParam> iterator = mainTainList.iterator();

        Date curTime = new Date();
        String dataStr = DateUtils.formatDate(curTime);
        CheckMainTainStatus mainTainStatus = new CheckMainTainStatus();
        while(iterator.hasNext()){
            MwAssetsMainTainParam mainTain = iterator.next();
            MwAssetsMainTainView view = new MwAssetsMainTainView();

            String formData = GzipTool.gunzip(mainTain.getFormData());
            MwAssetsMainTainParamV1 mwAssetsMainTainParamV1 = JSON.parseObject(formData ,MwAssetsMainTainParamV1.class);
            if(null == mwAssetsMainTainParamV1){
                continue;
            }

            Date activeTill = mainTain.getActiveTill();
            Date activeSince = mainTain.getActiveSince();
            Date currDate = new Date();
            if(activeTill.compareTo(currDate) > 0 && activeSince.compareTo(currDate) <= 0){
                mainTain.setStatus(MaintainStatus.Using.getCode());
                if(null != mwAssetsMainTainParamV1.getPeriods()){
                    boolean isShielding = mainTainStatus.checkStatus(mwAssetsMainTainParamV1);
                    if(isShielding){
                        mainTain.setStatus(MaintainStatus.Shielding.getCode());
                    }
                }
            }

            if(activeTill.compareTo(currDate) < 0){
                mainTain.setStatus(MaintainStatus.Expire.getCode());
            }
            if(activeSince.compareTo(currDate) > 0){
                mainTain.setStatus(MaintainStatus.Closing.getCode());
            }

            if(null != statusCallBack){
                statusCallBack.callback(maintenids ,iterator ,mainTain ,param);
            }
            view.extractFrom(mainTain ,mwAssetsMainTainParamV1);
            ret.add(view);
        }

        return ret;
    }

    /**
     * 查询维护数据的主机数据
     * @param mainTainList 查询的数据结果集合
     */
    private void getMainTainHostData(List<MwAssetsMainTainView> mainTainList) throws Exception{
        List<Integer> mainTainIds = new ArrayList<Integer>();
        mainTainList.forEach(param->{
            mainTainIds.add(param.getId());
        });
        //查询主机数据
        List<Map<String, Object>> hostDatas = mainTainDao.selectMainTainHostData(mainTainIds);
        if(!CollectionUtils.isEmpty(hostDatas)){
            mainTainList.forEach(param->{
                //基础数据ID
                Integer id = param.getId();
                List<MWMainTainHostView> hostIds = new ArrayList();
                hostDatas.forEach(hostMap->{
                    MWMainTainHostView hostParam = new MWMainTainHostView();
                    Object maintenid = hostMap.get("maintenid");
                    Object hostid = hostMap.get("hostid");
                    Object hostName = hostMap.get("hostName");
                    Object serverId = hostMap.get("serverId");
                    Object maintenanceid = hostMap.get("maintenanceid");
                    Object typeId = hostMap.get("typeId");
                    Object modelInstanceId = hostMap.get("modelInstanceId");
                    if(maintenid != null && hostid != null){
                    //对比ID，如果一样说明是同一条数据的主机
                    if(Integer.parseInt(maintenid.toString()) == id){
                        hostParam.setHostId(hostid.toString());
                        hostParam.setHostName(hostName != null?hostName.toString():"");
                        hostParam.setModelInstanceId(modelInstanceId != null?modelInstanceId.toString():"");
                        hostParam.setServerId(serverId!=null?Integer.parseInt(serverId.toString()):null);
                        hostParam.setMaintenanceid(maintenanceid!=null?Integer.parseInt(maintenanceid.toString()):null);
                        hostParam.setTypeId(typeId!=null?Integer.parseInt(typeId.toString()):null);

                        hostIds.add(hostParam);
                        }
                    }
                });
                //设置主机数据
                param.setHostids(hostIds);
            });

            Set<String> modelInstanceIdSet = new HashSet<>();
            Map<String ,List<MWMainTainHostView>> hostViewMap = new HashMap<>();

            for(MwAssetsMainTainView view : mainTainList) {
                for (MWMainTainHostView hostView : view.getHostids()) {
                    String instanceId = hostView.getModelInstanceId();
                    if (StringUtils.isNotEmpty(instanceId)) {
                        modelInstanceIdSet.add(instanceId);
                    }
                    List<MWMainTainHostView> list = hostViewMap.get(instanceId);
                    if (null == list) {
                        list = new ArrayList<>();
                        hostViewMap.put(instanceId, list);
                    }
                    list.add(hostView);
                }
            }

            QueryModelAssetsParam queryTangAssetsParam = new QueryModelAssetsParam();
            List<Integer> integerList = modelInstanceIdSet.stream().map(data -> Integer.parseInt(data)).collect(Collectors.toList());
            queryTangAssetsParam.setInstanceIds(integerList);
            queryTangAssetsParam.setUserId(commonService.getAdmin());
            List<MwTangibleassetsDTO> mwTangibleassetsTables = viewCommonService.findModelAssets(MwTangibleassetsDTO.class,queryTangAssetsParam);
            for(MwTangibleassetsDTO mwTangibleassetsDTO : mwTangibleassetsTables){
                if(null != mwTangibleassetsDTO.getModelInstanceId()){
                    List<MWMainTainHostView> list = hostViewMap.get(mwTangibleassetsDTO.getModelInstanceId().toString());
                    if(null != list){
                        for(MWMainTainHostView hostView : list){
                            hostView.setInBandIp(mwTangibleassetsDTO.getInBandIp());
                            hostView.setInstanceName(mwTangibleassetsDTO.getInstanceName());
                        }
                    }
                }
            }
        }
    }


    /**
     * 查询维护数据的主机组数据
     * @param mainTainList 查询的数据结果集合
     */
    private void getMainTainHostGroupData(List<MwAssetsMainTainParam> mainTainList,List<String> queryGroupids){
        List<Integer> mainTainIds = new ArrayList<Integer>();
        mainTainList.forEach(param->{
            mainTainIds.add(param.getId());
        });
        //查询主机组数据
//        List<Map<String, Object>> hostGroups = mainTainDao.selectMainTainHostGroupData(mainTainIds);
        List<Map<String, Object>> hostGroups = null;
        if(!CollectionUtils.isEmpty(hostGroups)){
            mainTainList.forEach(param->{
                //基础数据ID
                Integer id = param.getId();
                List<String> groupids = new ArrayList<String>();
                hostGroups.forEach(hostMap->{
                    Object maintenid = hostMap.get("maintenid");
                    Object groupid = hostMap.get("groupid");
                    if(maintenid != null && groupid != null){
                        //对比ID，如果一样说明是同一条数据的主机组
                        if(Integer.parseInt(maintenid.toString()) == id){
                                groupids.add(groupid.toString());
                        }
                    }
                });
                //设置主机组数据
                param.setGroupids(groupids);
            });

            //根据主机组进行过滤
            Iterator<MwAssetsMainTainParam> iterator = mainTainList.iterator();
            while (iterator.hasNext()){
                List<String> groupids = iterator.next().getGroupids();
                Boolean flag = false;
                for (String groupid : groupids) {
                    if(CollectionUtils.isEmpty(queryGroupids) || queryGroupids.contains(groupid)){
                        flag = true;
                    }
                }
                if(!flag){
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 查询维护的时间段数据
     * @param mainTainList 查询的数据结果集合
     */
    private void getMainTainTimeSlotData(List<MwAssetsMainTainParam> mainTainList){
        List<Integer> mainTainIds = new ArrayList<Integer>();
        mainTainList.forEach(param->{
            mainTainIds.add(param.getId());
        });
        //查询时间段数据
//        List<HashMap<String, Object>> timeSlotDatas = mainTainDao.selectMainTainTimeSlotData(mainTainIds);
        List<HashMap<String, Object>> timeSlotDatas = null;
        if(!CollectionUtils.isEmpty(timeSlotDatas)){
            mainTainList.forEach(param->{
                //基础数据ID
                Integer id = param.getId();
                List<HashMap> times = new ArrayList<HashMap>();
                timeSlotDatas.forEach(timeSlotmap->{
                    Object maintenid = timeSlotmap.get("maintenid");
                    //对比ID，如果一样说明是同一条数据的时间段
                    if(maintenid != null && Integer.parseInt(maintenid.toString()) == id){
                        times.add(timeSlotmap);

                    }
                });
                //设置时间段数据
                param.setTimes(times);
            });
        }
    }


    /**
     * 查询维护的标记数据
     * @param mainTainList 查询的数据结果集合
     */
    private void getMainTainTagsData(List<MwAssetsMainTainParam> mainTainList){
        List<Integer> mainTainIds = new ArrayList<Integer>();
        mainTainList.forEach(param->{
            mainTainIds.add(param.getId());
        });
        //查询时间段数据
//        List<Map<String, Object>> tagMaps = mainTainDao.selectMainTainTagData(mainTainIds);
        List<Map<String, Object>> tagMaps = null;
        if(!CollectionUtils.isEmpty(tagMaps)){
            mainTainList.forEach(param->{
                //基础数据ID
                Integer id = param.getId();
                List<Map<String, Object>> tags = new ArrayList<Map<String, Object>>();
                tagMaps.forEach(tagMap->{
                    Object maintenid = tagMap.get("maintenid");
                    //对比ID，如果一样说明是同一条数据的时间段
                    if(maintenid != null && Integer.parseInt(maintenid.toString()) == id){
                        tags.add(tagMap);

                    }
                });
                //设置时间段数据
                param.setTags(tags);
            });
        }
    }

    @Override
    public Reply updateAssetsMainTain(MwAssetsMainTainParamV1 param){
        try {
            MwAssetsMainTainParam mainTainParam = new MwAssetsMainTainParam();
            mainTainParam.setId(param.getId());
            //获取zabbixID信息
            Reply reply1 = selectMainTainHostInfo(mainTainParam);
            if(null == reply1){
                return  Reply.warn("数据被编辑过");
            }

            if(PaasConstant.RES_SUCCESS != reply1.getRes()){
                return  reply1;
            }
            MwAssetsMainTainViewV1 selParam = (MwAssetsMainTainViewV1) reply1.getData();
            List<MWMainTainHostView> hostViews = selParam.getHostids();
            //按照serverId将数据分组
            Map<Integer, List<Integer>> mainTenIdMap = hostViews.stream().filter(item -> item.getServerId() != null && item.getServerId() != 0)
                    .collect(Collectors.groupingBy(MWMainTainHostView::getServerId, Collectors.mapping(MWMainTainHostView::getMaintenanceid, Collectors.toList())));
            //将修改后的参数进行数据处理
            List<HashMap> newtimes = new ArrayList<>();
            Map<String,Object> mainTenParamMap = new HashMap<String,Object>();
            MwAssetsMainTainParam mwAssetsMainTainParam = new MwAssetsMainTainParam();
            mwAssetsMainTainParam.extractFrom(param);
            setMainTainBasicParam(mainTenParamMap, mwAssetsMainTainParam,newtimes);

            Map<Integer,List<String>> hostAndServerIdMap = (Map<Integer, List<String>>) mainTenParamMap.get("hostids");
            for (Integer serverId : hostAndServerIdMap.keySet()) {
                List<String> hosts = hostAndServerIdMap.get(serverId);
                mainTenParamMap.put(HOST,setHostParam(hosts));
                mainTenParamMap.put("hostids",hosts);
                MWZabbixAPIResult zabbixAPIResult;
                if(mainTenIdMap.get(serverId) == null){
                    //调用新增方法
                    zabbixAPIResult = zabbixApi.maintenanceCreate(serverId,mainTenParamMap,new ArrayList<>(newtimes));
                }else{
                    mainTenParamMap.put("maintenanceid",mainTenIdMap.get(serverId).get(0));
                    //调用修改方法
                    zabbixAPIResult = zabbixApi.maintenanceUpdate(serverId,mainTenParamMap,newtimes);
                }


                if(zabbixAPIResult.getData() != null) {
                    JsonNode data = (JsonNode) zabbixAPIResult.getData();
                    if (data.size() > 0) {
                        data.forEach(node -> {
                            int maintenanceid = node.get(0).asInt();
                            List<MWMainTainHostParam> hostids = param.getHostids();
                            if(!CollectionUtils.isEmpty(hostids)){
                                for (MWMainTainHostParam hostid : hostids) {
                                    if(serverId.equals(hostid.getServerId())){
                                        hostid.setMaintenanceid(maintenanceid);
                                    }
                                }
                            }
                        });
                    }
                }
            }
            //删除原来数据
            //删除猫维中的数据
            mainTainDao.deleteMainTain(Arrays.asList(param.getId()));
            //删除主机数据
            mainTainDao.deleteHostIdDate(Arrays.asList(param.getId()));
            //压缩保存请求数据
            String formData = GzipTool.gzip(JSON.toJSONString(param));
            mwAssetsMainTainParam.setFormData(formData);

            //登录用户
            GlobalUserInfo globalUser = userService.getGlobalUser();
            mwAssetsMainTainParam.setCreator(globalUser.getUserName());
            //维护主机成功需将数据插入猫维数据库
            mainTainDao.addAssetsMainTain(mwAssetsMainTainParam);
            //添加主机数据
            addMainTianHostData(mwAssetsMainTainParam,globalUser.getUserName());
            return Reply.ok("修改成功");
        }catch (Throwable e){
            logger.error("资产维护修改失败：",e);
            return Reply.fail("资产维护修改失败");
        }
    }


    /**
     * 修改维护数据
     * @param param 修改主机维护的参数
     * @return
     */
//    @Override
    public Reply updateAssetsMainTain2(MwAssetsMainTainParam param) {
        try{
            List<MWMainTainHostParam> host = param.getHostids();
            if(CollectionUtils.isEmpty(host)){
                return  Reply.fail("维护资产不可为空");
            }
            //组合参数信息
            Map<String,Object> mainTenParamMap = new HashMap<String,Object>();
            List<HashMap> newtimes = new ArrayList<>();
            Boolean repeatName = nameFilter(param.getName(),param.getId());
            if(!repeatName){
                return  Reply.fail("该名称已存在，请重新输入");
            }
            setMainTainBasicParam(mainTenParamMap, param,newtimes);
            Integer mainId = param.getId();
            List<Integer> mainIds = new ArrayList<>();
            mainIds.add(mainId);
            List<Map<String, Object>> maps = mainTainDao.selectMainTainHostData(mainIds);
            Set<String> serverIdAndMainIds = new HashSet<>();
            for (Map<String, Object> map : maps) {
                Object serverId = map.get("serverId");
                Object maintenanceid = map.get("maintenanceid");
                if(serverId != null && maintenanceid != null){
                    if(!serverIdAndMainIds.contains(serverId.toString()+"_"+maintenanceid.toString())){
                        List<String> mantanIds = new ArrayList<>();
                        mantanIds.add(maintenanceid.toString());
                        //删除ZABBIX中的数据
                        MWZabbixAPIResult zabbixAPIResult = zabbixApi.maintenanceDelete(Integer.parseInt(serverId.toString()), mantanIds);
                        serverIdAndMainIds.add(serverId.toString()+"_"+maintenanceid.toString());
                    }
                }
            }
            //进行zabbix数据重新添加
            Map<Integer,List<String>> hostAndServerIdMap = (Map<Integer, List<String>>) mainTenParamMap.get("hostids");
            for (Integer serverId : hostAndServerIdMap.keySet()) {
                List<String> hosts = hostAndServerIdMap.get(serverId);
                mainTenParamMap.put(HOST,setHostParam(hosts));
                mainTenParamMap.put("hostids",hosts);
                MWZabbixAPIResult zabbixAPIResult = zabbixApi.maintenanceCreate(serverId,mainTenParamMap,newtimes);
                if(zabbixAPIResult.getData() != null) {
                    JsonNode data = (JsonNode) zabbixAPIResult.getData();
                    if (data.size() > 0) {
                        data.forEach(node -> {
                            int maintenanceid = node.get(0).asInt();
                            List<MWMainTainHostParam> hostids = param.getHostids();
                            if(!CollectionUtils.isEmpty(hostids)){
                                for (MWMainTainHostParam hostid : hostids) {
                                    if(serverId == hostid.getServerId()){
                                        hostid.setMaintenanceid(maintenanceid);
                                    }
                                }
                            }
                        });
                    }
                }
            }
            //修改猫维数据库数据
            //登录用户
            String loginName = iLoginCacheInfo.getLoginName();
            param.setModifier(loginName);
            mainTainDao.updatemainTain(param);
            Integer id = param.getId();
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            //删除主机数据
            mainTainDao.deleteHostIdDate(ids);
//            //删除主机组数据
//            mainTainDao.deleteHostGroupIdDate(ids);
//            //删除时间段数据
//            mainTainDao.deleteTimeSlotDate(ids);
//            //删除标记数据
//            mainTainDao.deleteTagData(ids);
            //重新添加主机，主机组，时间段数据
            //添加主机数据
            addMainTianHostData(param,loginName);
            //添加主机组数据
//            addMainTianHostGroupData(param,loginName);
//            //添加时间段数据
//            addMainTainTimesData(param,loginName);
//            //添加标记数据
//            addMainTianTagData(param,loginName);
            return Reply.ok("数据修改成功");
        }catch (Exception e){
            logger.error("资产维护修改失败：",e);
            return Reply.fail("资产维护修改失败");
        }

    }

    /**
     * 删除操作
     * @return
     */
    @Override
    public Reply deleteAssetsMainTain(List<MwAssetsMainTainDelParam> delParams) {
        try{
            if(!CollectionUtils.isEmpty(delParams)){
                //通过serverID分组处理
                List<Integer> ids = new ArrayList<>();
                Map<Integer,List<String>> map = new HashMap<>();
                for(MwAssetsMainTainDelParam delParam:delParams){
                    List<String> list = map.get(delParam.getServerId());
                    if(null == list){
                        list = new ArrayList<>();
                        map.put(delParam.getServerId() ,list);
                    }
                    String maintenid = delParam.getMaintenid().toString();
                    if(StringUtils.isNotEmpty(maintenid) && !list.contains(maintenid)){
                        list.add(maintenid);
                    }

                    if(!ids.contains(delParam.getId())){
                        ids.add(delParam.getId());
                    }
                }

                for (Integer key : map.keySet()) {
                    List<String> value = map.get(key);
                    //删除ZABBIX中的数据
                    MWZabbixAPIResult zabbixAPIResult = zabbixApi.maintenanceDelete(key, value);
                }
                //删除猫维中的数据
                mainTainDao.deleteMainTain(ids);
                //删除主机数据
                mainTainDao.deleteHostIdDate(ids);
            }
            return Reply.ok("数据删除成功");
        }catch (Exception e){
            logger.error("资产维护删除失败：",e);
            return Reply.fail("资产维护删除失败");
        }
    }

    /**
     * 查询维护下拉框数据
     * @param mainTainParam 对应的监控服务器
     * @return
     */
    @Override
    public Reply selectAssetsMainTainGroupDropDown(MwAssetsMainTainParam mainTainParam) {
        try{
            List<Map<String, Object>> groupDropDowm = mainTainDao.selectHostGroupDropDown(mainTainParam.getServerId());
            return Reply.ok(groupDropDowm);
        }catch (Exception e){
            logger.error("查询主机组失败：",e);
            return Reply.fail("查询主机组失败");
        }
    }

    /**
     * 主机下拉数据查询
     * @param mainTainParam 资产类型
     * @return
     */
    @Override
    public Reply selectAssetsMainTainHostDropDown(MwAssetsMainTainParam mainTainParam) {
        try{
            List<Map<String, Object>> hostDropDowm = mainTainDao.selectHostDropDown(mainTainParam);
            return Reply.ok(hostDropDowm);
        }catch (Exception e){
            logger.error("查询主机组失败：",e);
            return Reply.fail("查询主机组失败");
        }
    }

    private List<MwAssetsMainTainParam> dataPermissionFilter(List<MwAssetsMainTainParam> mainTainList) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<MwAssetsMainTainParam> filtermainTainList = new ArrayList<>();
        QueryTangAssetsParam qParam = new QueryTangAssetsParam();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限：private public
        DataPermission dataPermission = DataPermission.valueOf(perm);
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);//用户所在的用户组id
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        List<MwTangibleassetsTable> mwTangAssetses = new ArrayList();
        switch (dataPermission) {
            case PRIVATE:
                qParam.setUserId(userId);
                PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                Map priCriteria = PropertyUtils.describe(qParam);
                mwTangAssetses = mwTangibleAssetsDao.selectPriList(priCriteria);
                break;
            case PUBLIC:
                String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                List<Integer> orgIds = new ArrayList<>();
                Boolean isAdmin = false;
                if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                    isAdmin = true;
                }
                if (!isAdmin) {
                    // List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                    //orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);
                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                }
                if (null != orgIds && orgIds.size() > 0) {
                    qParam.setOrgIds(orgIds);
                }
                PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                Map pubCriteria = PropertyUtils.describe(qParam);
                mwTangAssetses = mwTangibleAssetsDao.selectPubList(pubCriteria);
                break;
        }
        if(CollectionUtils.isEmpty(mwTangAssetses)){
            return filtermainTainList;
        }
        mwTangAssetses.forEach(tangAssets->{
            String assetsId = tangAssets.getAssetsId();
            mainTainList.forEach(mainTain->{
                List<MWMainTainHostParam> params = mainTain.getHostids();
                List<String> hostids = new ArrayList<>();
                for (MWMainTainHostParam param : params) {
                    hostids.add(param.getHostId());
                }
                if(hostids.contains(assetsId)){
                    filtermainTainList.add(mainTain);
                }
            });
        });
        return filtermainTainList;
    }

    /**
     * 根据名称过滤数据
     * @param mainTainList 所有维护数据集合
     * @param queryName 需要查询的名称
     */
    private void mainTainNameFilter(List<MwAssetsMainTainParam> mainTainList,String queryName){
        if(CollectionUtils.isEmpty(mainTainList) || StringUtils.isBlank(queryName)){
            return;
        }
        Iterator<MwAssetsMainTainParam> iterator = mainTainList.iterator();
        while (iterator.hasNext()){
            MwAssetsMainTainParam mainTain = iterator.next();
            if(!mainTain.getName().contains(queryName)){
                iterator.remove();
            }
        }
    }


    private Boolean nameFilter(String name,Integer id){
        Integer count = mainTainDao.selectRepeatName(name,id);
        if(count > 0){
            return false;
        }
        return true;
    }

    /**
     * 处理标签为空的问题
     * @param param 添加维护的数据
     */
    private void handleEmptyTags(MwAssetsMainTainParam param){
        if(param.getTags() != null && param.getTags().size() > 0){
            List<Map<String, Object>> tags = param.getTags();
            Iterator<Map<String, Object>> iterator = tags.iterator();
            while(iterator.hasNext()){
                Map<String, Object> next = iterator.next();
                if((next.get("tag") == null || "".equals(next.get("tag").toString().replace(" ","")))
                        && (next.get("value") == null || "".equals(next.get("value").toString().replace(" ","")))){
                    iterator.remove();
                }
            }
        }
        if(param.getTags() == null){
            param.setTags(new ArrayList<>());
        }
    }

    @Override
    public Reply selectMainTainAssetsDifficulty() {
        Integer [] typeIds = {1,2,3,4,5,6};
        QueryAssetsTypeParam param = new QueryAssetsTypeParam();
        List<AssetsTreeDTO> treeDTOS = new ArrayList<>();
        for (Integer typeId : typeIds) {
            param.setAssetsTypeId(typeId);
            param.setTableType(1);
            Reply assetsTypesTree = mwTangService.getAssetsTypesTree(param);
            List<AssetsTreeDTO> data = (List<AssetsTreeDTO>) assetsTypesTree.getData();
            treeDTOS.addAll(data);
        }
        return Reply.ok(treeDTOS);
    }

    /**
     * 资产维护计划模糊搜索数据
     * @return
     */
    @Override
    public Reply getAssetsMainTainPlanFuzzQuery() {
        List<String> mainTainNames = mainTainDao.selectMainTainPlanNames();
        Map<String,List<String>> nameMap = new HashMap<>();
        nameMap.put("name",mainTainNames);
        return Reply.ok(nameMap);
    }

    /**
     * 查询主机数据
     * @param mainTainParam
     * @return
     */
    @Override
    public Reply selectMainTainHostInfo(MwAssetsMainTainParam mainTainParam) throws Exception{
        Integer id = mainTainParam.getId();
        if(id == null){
            return null;
        }
        //根据ID查询主机信息
        MwAssetsMainTainParam searchParam = new MwAssetsMainTainParam();
        searchParam.setId(id);
        List<MwAssetsMainTainParam> retParams = mainTainDao.selectMainTain(searchParam);
        if(null == retParams || retParams.isEmpty()){
            return null;
        }

        MwAssetsMainTainParam mwAssetsMainTainParam = retParams.get(0);
        String formData = GzipTool.gunzip(mwAssetsMainTainParam.getFormData());
        MwAssetsMainTainViewV1 mwAssetsMainTainViewV1 = JSON.parseObject(formData ,MwAssetsMainTainViewV1.class);

        if(null != mwAssetsMainTainViewV1.getHostids()){
            QueryModelAssetsParam queryTangAssetsParam = new QueryModelAssetsParam();
            Map<Integer ,List<MWMainTainHostView>> viewMap = new HashMap<>();
            List<Integer> intanceIdList = new ArrayList<>();
            for(MWMainTainHostView view : mwAssetsMainTainViewV1.getHostids()){
                if(StringUtils.isNotEmpty(view.getModelInstanceId())){
                    Integer instanceId = Integer.parseInt(view.getModelInstanceId());
                    List<MWMainTainHostView> list = viewMap.get(instanceId);
                    if(null == list){
                        list = new ArrayList<>();
                        viewMap.put(instanceId ,list);
                    }
                    list.add(view);
                    intanceIdList.add(instanceId);
                }
            }

            if(!intanceIdList.isEmpty()){
                queryTangAssetsParam.setInstanceIds(intanceIdList);
                List<MwTangibleassetsDTO> mwTangibleassetsTables = viewCommonService.findModelAssets(MwTangibleassetsDTO.class,queryTangAssetsParam);
                for(MwTangibleassetsDTO data : mwTangibleassetsTables){
                    List<MWMainTainHostView> list = viewMap.get(data.getModelInstanceId());
                    if(null != list){
                        for(MWMainTainHostView mwMainTainHostView : list){
                            mwMainTainHostView.setInBandIp(data.getInBandIp());
                            mwMainTainHostView.setInstanceName(data.getInstanceName());
                        }
                    }
                }
            }
        }

        return Reply.ok(mwAssetsMainTainViewV1);
    }

    /**
     * 查询实例
     * @param param
     * @return
     */
    @Override
    public Reply getModelListInfo(QueryInstanceModelParam param) {
        try {
            Reply listInfoByView = viewCommonService.getModelListInfoByView(param);
            //实例数据处理
            if(listInfoByView == null || listInfoByView.getRes() != PaasConstant.RES_SUCCESS){return listInfoByView;}
            PageInfo pageInfo = (PageInfo) listInfoByView.getData();
            List<Map<String, Object>> listMap = pageInfo.getList();
            List<Integer> modelInstanceIds = new ArrayList<>();
            //数据翻译
//            for (Map<String, Object> map : listMap) {
//                modelInstanceIds.add(Integer.parseInt(map.get("modelInstanceId").toString()));
//            }
            return Reply.ok(pageInfo);
        }catch (Throwable e){
            logger.error("MwAssetsMainTainServiceImpl{} getModelListInfo::",e);
            return Reply.fail("getModelListInfo()");
        }
    }



    private interface MainStatusCallBack{
        void callback(List<Integer> maintenids ,Iterator<MwAssetsMainTainParam> iterator
                ,MwAssetsMainTainParam mainTain ,MwAssetsMainTainParam param);
    }

    /**
     * 获取维护中的资产
     * @return
     */
    @Override
    public List<MWMainTainHostView> getUnderMaintenanceHost() {
        try {
            List<MWMainTainHostView> hostViews = new ArrayList<>();
            MwAssetsMainTainParam mainTainParam = new MwAssetsMainTainParam();
            mainTainParam.setPageNumber(1);
            mainTainParam.setPageSize(Integer.MAX_VALUE);
            Reply reply = selectAssetsMainTain(mainTainParam);
            if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS){return hostViews;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(pageInfo == null || pageInfo.getList() == null){return hostViews;}
            List<MwAssetsMainTainView> mainTainViews = pageInfo.getList();
            //只取屏蔽中的数据
            List<MwAssetsMainTainView> viewList = mainTainViews.stream().filter(item -> item.getStatus() == MaintainStatus.Shielding.getCode()).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(viewList)){return hostViews;}
            for (MwAssetsMainTainView mwAssetsMainTainView : viewList) {
                List<MWMainTainHostView> hostids = mwAssetsMainTainView.getHostids();
                if(CollectionUtils.isEmpty(hostids)){continue;}
                hostViews.addAll(hostids);
            }
            return hostViews;
        }catch (Throwable e){
            logger.error("获取维护中的资产失败",e);
            return null;
        }
    }
}
