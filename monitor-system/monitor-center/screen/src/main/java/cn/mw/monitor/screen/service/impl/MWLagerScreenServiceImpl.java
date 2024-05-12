package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assetsTemplate.dao.MwAseetstemplateTableDao;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.dao.MWIndexDao;
import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.model.IndexBulk;
import cn.mw.monitor.screen.model.MapAlert;
import cn.mw.monitor.screen.model.Model;
import cn.mw.monitor.screen.param.*;
import cn.mw.monitor.screen.service.MWLagerScreenService;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.OrgMapperDTO;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.api.OrgModuleType;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.zbx.model.AlertDTO;
import cn.mw.monitor.service.zbx.model.HostProblem;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.weixinapi.NotifyAlertMessage;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/4/9 10:41
 */
@Service
@Slf4j
public class MWLagerScreenServiceImpl implements MWLagerScreenService , InitializingBean {

    private static final Logger dbLogger = LoggerFactory.getLogger("MWDBLogger");

    private static final String MODULE = "assets-screen";

    @Resource
    private MWLagerScreenDao dao;

    @Autowired
    private MWLagerScreenManage manage;

    @Autowired
    private MWIndexDao mwIndexDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${screen.image.file.uploadFolder}")
    private String uploadFolder;

    @Value("${screen.timeLag}")
    private int timeLag;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;


    @Resource
    MwAseetstemplateTableDao mwAseetstemplateTableDao;
    
    @Autowired
    private MWOrgService mwOrgService;

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private LargeScreenMessageMange largeScreenMessageMange;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Override
    public Reply addLagerScreen(MwLagerScreenParam mwLagerScreenParam) {
        try {
            String screenId = manage.addLagerScreen(mwLagerScreenParam);
            log.info("SCREEN_LOG[]screen[]大屏[]创建大屏[]{}}", mwLagerScreenParam);
            return Reply.ok(screenId);
        } catch (Exception e) {
            log.error("fail to insert with addLagerScreen={}, cause:{}", mwLagerScreenParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_INSERT_CODE_304003, ErrorConstant.SCREEN_INSERT_MSG_304003);
        }

    }

    @Override
    public Reply updateLagerScreen(MwLagerScreenParam mwLagerScreenParam) {
        try {
            manage.updateLagerScreen(mwLagerScreenParam);
            log.info("SCREEN_LOG[]screen[]大屏[]修改大屏数据[]{}}", mwLagerScreenParam);
        } catch (Exception e) {
            log.error("fail to updateLagerScreen with mwLagerScreenParam={}, cause:{}", mwLagerScreenParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_UPDATE_CODE_304011, ErrorConstant.SCREEN_UPDATE_MSG_304011);
        }
        return Reply.ok();
    }

    @Override
    public Reply deleteLagerScreen(String screenId) {
        try {
            if (null != screenId && StringUtil.isNotEmpty(screenId)) {
                manage.deleteLagerScreen(screenId);

                String screenName = dao.selectScreenName(screenId);
                SystemLogDTO systemLogDTO = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("监控大屏")
                        .objName(screenName).operateDes("删除大屏" + screenName).build();
                dbLogger.info(JSON.toJSONString(systemLogDTO));
            }
            log.info("SCREEN_LOG[]screen[]大屏[]删除大屏数据[]{}}", screenId);
        } catch (Exception e) {
            log.error("fail to deleteLagerScreen with screenId={}, cause:{}", screenId, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_DELETE_CODE_304012, ErrorConstant.SCREEN_DELETE_MSG_304012);
        }
        return Reply.ok();
    }

    @Override
    public Reply updateModelData(UpdateModelDataParam updateModelDataParam) {
        try {
            dao.updateModelData(updateModelDataParam);
            log.info("SCREEN_LOG[]screen[]大屏[]修改大屏组件数据[]{}}", updateModelDataParam);
        } catch (Exception e) {
            log.error("fail to updateModelData with updateModelDataParam={}, cause:{}", updateModelDataParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_UPDATE_MODEL_DATA_CODE_304013, ErrorConstant.SCREEN_UPDATE_MODEL_DATA_MSG_304013);
        }
        return Reply.ok();
    }

    @Override
    public Reply deleteModelData(String modelDataId) {
        try {
            List<String> list = new ArrayList<>();
            list.add(modelDataId);
            dao.updateModelDataId(modelDataId);
            dao.deleteModelData(list);
            dao.deleteAssetsFilter(modelDataId);
            log.info("SCREEN_LOG[]screen[]大屏[]删除大屏组件数据[]{}}", modelDataId);
        } catch (Exception e) {
            log.error("fail to deleteModelData with bulkDataId={}, cause:{}", modelDataId, e);
            return Reply.fail(ErrorConstant.SCREEN_DELETE_MODEL_DATA_CODE_304014, ErrorConstant.SCREEN_DELETE_MODEL_DATA_MSG_304014);
        }
        return Reply.ok();
    }


    @Override
    public Reply updateEnable(EnableParam enableParam) {
        try {
            enableParam.setModifier(iLoginCacheInfo.getLoginName());
            dao.updateEnable(enableParam);
            log.info("SCREEN_LOG[]screen[]大屏[]是否启用大屏[]{}}", enableParam);
        } catch (Exception e) {
            log.error("fail to updateEnable with enableParam={}, cause:{}", enableParam, e.getMessage());
            return Reply.fail("修改大屏enable失败");
        }
        return Reply.ok();
    }


    @Override
    public Reply getLayoutBase() {
        try {
            List<Integer> layoutType = dao.getLayoutBase();
            return Reply.ok(layoutType);
        } catch (Exception e) {
            log.error("fail to select with showLayoutBase={}, cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_LAYOUT_CODE_304001, ErrorConstant.SCREEN_LAYOUT_MSG_304001);
        }
    }

    @Override
    public Reply getModelList() {
        try {
            List<Model> Model = dao.getModelList();
            List<String> layoutType = dao.getModelType();
            List<List<Model>> newModel = new ArrayList<>();
            layoutType.forEach(
                    la -> {
                        List<Model> model = new ArrayList<>();
                        Model.forEach(mo -> {
                                    if (mo.getModelType().equals(la)) {
                                        model.add(mo);
                                    }
                                }
                        );
                        newModel.add(model);
                    });
            return Reply.ok(newModel);
        } catch (Exception e) {
            log.error("fail to select with showLayoutBase={}, cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_MODEL_CODE_304002, ErrorConstant.SCREEN_MODEL_MSG_304002);
        }

    }

    @Override
    public Reply getModelType() {
        try {
            List<String> layoutType = dao.getModelType();
            return Reply.ok(layoutType);
        } catch (Exception e) {
            log.error("fail to select with showLayoutBase={}, cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_MODEL_TYPE_CODE_304004, ErrorConstant.SCREEN_MODEL_TYPE_MSG_304004);
        }
    }

    @Override
    public Reply addLagerScreenData(ModelDataParam modelDataParam) {
        try {
            if(modelDataParam.getModelId() == null){
                return Reply.fail("请选择组件内容!");
            }
            String modelDateId = manage.addLagerScreenData(modelDataParam);
            log.info("modelDataParam{}", modelDataParam);
            return Reply.ok(modelDateId);
        } catch (Exception e) {
            log.error("fail to insert with modelDataParamg{}, cause:{}", modelDataParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_INSERT_MODEL_DATA_CODE_304005, ErrorConstant.SCREEN_INSERT_MODEL_DATA_MSG_304005);
        }
    }


    @Override
    public Reply saveScreenImg(ImgParam imgParam) {
        try {
            String image = RandomStringUtils.randomAlphanumeric(32).toLowerCase() + ".png";
            String imgUrl = uploadFolder + image;
            boolean b = GenerateImage(imgParam.getImg(), imgUrl);
            if (b) {
                dao.saveScreenImg(Constants.UPLOAD_BASE_URL + MODULE + "/" + image, imgParam.getScreenId());
                log.info("modelDataParam{}", imgParam);
                return Reply.ok();
            } else {
                return Reply.fail(ErrorConstant.SCREEN_IMG_ENCODE_CODE_304007, ErrorConstant.SCREEN_IMG_ENCODE_MSG_304007);
            }
        } catch (Exception e) {
            log.error("fail to insert with modelDataParamg{}, cause:{}", imgParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_SAVE_IMG_CODE_304006, ErrorConstant.SCREEN_SAVE_IMG_MSG_304006);
        }
    }

    /**
     * @param imgStr
     * @param imgFilePath
     * @return 对字节数组字符串进行Base64解码并生成图片
     */
    private boolean GenerateImage(String imgStr, String imgFilePath) {
        if (imgStr == null)
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        String[] split = imgStr.split(",");
        imgStr = split[1];
        try {
            // Base64解码
            imgStr = imgStr.replace(" ", "+");
            byte[] bytes = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            // 生成jpeg图片
            log.info("uploadFolder:" + uploadFolder);
            File imagePath = new File(uploadFolder);
            if (!imagePath.exists()) {
                imagePath.mkdirs();
            }
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            log.error("GenerateImage", e);
            return false;
        }
    }


//    @Override
//    public Reply updateBulkDataTime(String bulkDataId, Integer timelag) {
//        try {
//            dao.updateBulkDataTime(bulkDataId, timelag);
//            logger.info("bulkDataId{},timelag{}", bulkDataId, timelag);
//        } catch (Exception e) {
//            log.error("fail to update with bulkDataId={},timelag{}, cause:{}", bulkDataId, timelag, e.getMessage());
//            return Reply.fail("修改时间间隔失败");
//        }
//        return Reply.ok();
//    }


    @Override
    public Reply getLagerScreenList(PermDto permDto) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != groupIds && groupIds.size() > 0) {
                permDto.setGroupIds(groupIds);
            }
            List<LagerScreenDataDto> largerScreenList = new ArrayList<>();
            switch (dataPermission) {
                case PRIVATE:
                    permDto.setUserId(userId);
                    PageHelper.startPage(permDto.getPageNumber(), permDto.getPageSize());
                    Map priCriteria = PropertyUtils.describe(permDto);
                    largerScreenList = dao.getPriLargerScreenList(priCriteria);
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
                        permDto.setOrgIds(orgIds);
                    }
                    permDto.setIsAdmin(isAdmin);
                    PageHelper.startPage(permDto.getPageNumber(), permDto.getPageSize());
                    Map pubCriteria = PropertyUtils.describe(permDto);
                    largerScreenList = dao.getPubLargerScreenList(pubCriteria);
                    break;
            }
            //数据排序
            if(CollectionUtils.isNotEmpty(largerScreenList)){
                Collections.sort(largerScreenList, new Comparator<LagerScreenDataDto>() {
                    @Override
                    public int compare(LagerScreenDataDto o1, LagerScreenDataDto o2) {
                        return o2.getCreateDate().compareTo(o1.getCreateDate());
                    }
                });
            }
            PageInfo pageInfo = new PageInfo<>(largerScreenList);
            pageInfo.setList(largerScreenList);
            log.info("SCREEN_LOG[]screen[]大屏[]查询大屏的列表[]{}}", permDto);
            return Reply.ok(largerScreenList);
        } catch (Exception e) {
            log.error("fail to getLagerScreenList with baseParam={}, cause:{}", permDto, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_LIST_CODE_304009, ErrorConstant.SCREEN_LIST_MSG_304009);
        }
    }

    @Override
    public Reply editLinkRank(String modelDataId,List<String> linkList,int linkType) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String screenType = DataType.SCREEN.getName();
            int count = 0;
            if(linkType == 1){
                count = dao.getfilterLinkCount(userId, screenType, modelDataId);
            }
            if(linkType == 2){
                count = dao.getfilterLinkCount(null, screenType, modelDataId);
            }
            if(count==0){
                if(linkList.size()>0){
                    if(linkType == 2){
                        dao.insertLinkFilter(null,screenType,modelDataId,linkList.get(0),120);
                    }
                    if(linkType == 1){
                        dao.insertLinkFilter(userId,screenType,modelDataId,linkList.toString(),timeLag);
                    }
                }else{
                    dao.insertLinkFilter(userId,screenType,modelDataId,null,timeLag);
                }
            }else {
                if(linkList.size()>0){
                    if(linkType == 2){
                        dao.updateLinkFilter(null,screenType,modelDataId,linkList.get(0));
                    }
                    if(linkType == 1){
                        dao.updateLinkFilter(userId,screenType,modelDataId,linkList.toString());
                    }
                }else{
                    dao.updateLinkFilter(userId,screenType,modelDataId,null);
                }
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("编辑线路组件错误，错误信息"+e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_UPDATE_MODEL_DATA_CODE_304013, ErrorConstant.SCREEN_UPDATE_MODEL_DATA_MSG_304013);
        }

    }

    @Override
    public Reply getLinkOption(String modelDataId) {
        List<String> list=new ArrayList<>();
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        int linkType = 0;
        try {
            String linkEdit = dao.getLinkEdit(modelDataId, userId);
            if(StringUtils.isNotEmpty(linkEdit) && linkEdit.contains("]")){
                String replace = linkEdit.replace(" ", "");
                String substring = replace.substring(1, replace.lastIndexOf("]"));
                String[] split = substring.split(",");
                list=Arrays.asList(split);
                linkType = 1;
            }else if(StringUtils.isNotEmpty(linkEdit) && !linkEdit.contains("]")){
                list.add(linkEdit);
                linkType = 2;
            }
            Map<String,Object> realData = new HashMap<>();
            realData.put("interfaceIds",list);
            realData.put("linkType",linkType);
            return Reply.ok(realData);
        } catch (Exception e) {
            return Reply.fail(e.getMessage());
        }
    }

    @Override
    public Reply getLagerScreenById(String screenId) {
        try {
            LagerScreenDataDto lagerScreenDataDto = dao.getLagerScreenById(screenId);
            // 机构重新赋值使页面可以显示
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (lagerScreenDataDto.getDepartment() != null && lagerScreenDataDto.getDepartment().size() > 0) {
                lagerScreenDataDto.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                lagerScreenDataDto.setOrgIds(orgNodes);
            }
            log.info("SCREEN_LOG[]screen[]大屏[]编辑前查询大屏数据[]{}}", screenId);
            return Reply.ok(lagerScreenDataDto);
        } catch (Exception e) {
            log.error("fail to getLagerScreenById with baseParam={}, cause:{}", screenId, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_LIST_CODE_304009, ErrorConstant.SCREEN_LIST_MSG_304009);
        }
    }

    @Override
    public Reply getFilterAssets(FilterAssetsParam param) {
        MwCommonAssetsDto filterAssets = null;
        try {
            filterAssets = dao.getFilterAssets(param);
            if(filterAssets!=null){
                if(filterAssets.getFilterOrgId()!=null&& StringUtils.isNotEmpty(filterAssets.getFilterOrgId())){
                    List<List<Integer>> filterOrgIds = JSON.parseObject(filterAssets.getFilterOrgId(),List.class);
                    filterAssets.setFilterOrgIds(filterOrgIds);
                }
                if(filterAssets.getFilterLabelId()!=null&&StringUtils.isNotEmpty(filterAssets.getFilterLabelId())){
                    List<Integer> filterLabelIds = JSON.parseObject(filterAssets.getFilterLabelId(),List.class);
                    filterAssets.setFilterLabelIds(filterLabelIds);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Reply.fail(e.getMessage());
        }
        return Reply.ok(filterAssets);
    }

    @Override
    public Reply editIndexLayout(EditorIndexParam param) {
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        IndexPerformType performType=IndexPerformType.getByValue(param.getPerformType());
        try {
            switch (performType){
                case ADD:
//                    mwIndexDao.insertIndexModel(param.getComponentList());
                    Integer bulkId = param.getBulkId();
                    String bulkName = param.getBulkName();
                    String modelDateId = UuidUtil.getUid();
                    List<IndexBulk> componentList=new ArrayList<>();
                    componentList.add(new IndexBulk(modelDateId,bulkId,bulkName,userId));
                    mwIndexDao.insertIndexBulk(componentList);
                    break;
                case MOVE:
                    mwIndexDao.deleteBulkByUser(userId);
                    mwIndexDao.insertIndexBulk(param.getComponentList());
//                    mwIndexDao.deleteIndexModel(null);
//                    mwIndexDao.insertIndexModel(param.getComponentList());
                    break;
                case DELETE:
//                    mwIndexDao.deleteIndexModel(param.getBulkId());
                    mwIndexDao.deleteIndexBulk(userId,param.getModelDataId());
                    break;
            }
        } catch (Exception e) {
            log.error("editIndexLayout{}",e);
            return Reply.fail(e.getMessage());
        }
        return Reply.ok();

    }

    @Override
    public Reply getCoordinate() {
        try {
//          Map<Integer, Map<String, Integer>> assetByOrg = mwModelManage.getAssetByOrg(QueryTangAssetsParam.builder().userId(userId).build());
            List<CoordinateAddress> coordinateAddress = dao.getCoordinateAddress();
            List<AssetOrgMapperDto> orgAssetInfo = dao.getOrgAssetInfo();
            Map<Integer,Map<String,Integer>> mapResult=new HashMap<>();
            if(orgAssetInfo.size()>0){
                Map<Integer, List<AssetOrgMapperDto>> collect = orgAssetInfo.stream().collect(Collectors.groupingBy(AssetOrgMapperDto::getOrgId));
                if(collect.size()>0){
                    AlertParam alertParam = new AlertParam();
                    alertParam.setPageSize(1000000000);
                    alertParam.setPageNumber(0);
                    Reply reply = mwalertService.getCurrAlertPage(alertParam);
                    for (Map.Entry<Integer, List<AssetOrgMapperDto>> en : collect.entrySet()) {
                        if(en.getKey() == null)continue;
                        Integer orgId = en.getKey();
                        List<AssetOrgMapperDto> aom = en.getValue();
                        Map<String, List<AssetOrgMapperDto>> collect1 = aom.stream().collect(Collectors.groupingBy(AssetOrgMapperDto::getTypeName));
                        Map<String,Integer> map1=new LinkedHashMap<>();
                        for (Map.Entry<String, List<AssetOrgMapperDto>> entry : collect1.entrySet()) {
                            String typeName = entry.getKey();
                            int size = entry.getValue().size();
                            map1.put(typeName,size);
                        }
                        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS) {
                            PageInfo pageInfo = (PageInfo) reply.getData();
                            List<ZbxAlertDto> list = pageInfo.getList();
                            if (CollectionUtils.isNotEmpty(list)) {
                                for (ZbxAlertDto zbxAlertDto : list) {
                                    List<OrgDTO> orgDTOList = new ArrayList<>();
                                    MwTangibleassetsDTO assetDto = mwAssetsManager.getAssetsAndOrgs(zbxAlertDto.getAssetsId());
                                    if(assetDto != null && CollectionUtils.isNotEmpty(assetDto.getDepartment())){
                                        orgDTOList = assetDto.getDepartment();
                                        for(OrgDTO od : orgDTOList){
                                            if(od.getOrgId().equals(orgId)){
                                                Integer count = map1.get("告警次数");
                                                map1.put("告警次数",(count == null) ? 1 : count + 1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(orgId != null){
                            mapResult.put(orgId,map1);
                        }
                    }
                }
            }
            for (CoordinateAddress address : coordinateAddress) {
                address.setIsAlert(0);
                for (Map.Entry<Integer, Map<String, Integer>> mapEntry : mapResult.entrySet()) {
                    if(address.getOrgId().equals(mapEntry.getKey())){
                        address.setOrgAsset(mapEntry.getValue());
                        for(Map.Entry<String, Integer> map : mapResult.get(mapEntry.getKey()).entrySet()){
                            if(map.getKey().equals("告警次数")){
                                address.setIsAlert(map.getValue());
                            }
                        }
                        break;
                    }
                }

            }
            return Reply.ok(coordinateAddress);
        } catch (Exception e) {
            log.error("查询地图组件失败",e);
            return Reply.fail(e.getMessage());
        }

    }

    @Override
    public Reply getIcmpLink(boolean first) {
        try {
            List<MapAlert> eventlist = largeScreenMessageMange.getMapEvent();
            if(null != eventlist){
                return Reply.ok(eventlist);
            }

            if(first || null == eventlist) {
                List<MapAlert> list = initMapAlertData();

                Map<String ,List<MapAlert>> map = new HashMap<>();
                for(MapAlert mapAlert : list){
                    List<MapAlert> data = map.get(mapAlert.getLinkHostId());
                    if(null == data){
                        data = new ArrayList<>();
                        map.put(mapAlert.getLinkHostId() ,data);
                    }
                    data.add(mapAlert);
                }

                //获取当前异常主机
                List<HostProblem> hostProblems = mwalertService.getZipCurrHostProblemFromRedis();
                if(null != hostProblems){
                    hostProblems.parallelStream().forEach((problem)->{
                        List<MapAlert> problemMapAlerts = map.get(problem.getHostId());
                        if(null != problemMapAlerts){
                            for(MapAlert mapAlert : problemMapAlerts){
                                mapAlert.setColor(largeScreenMessageMange.getMapAlertConfig().getLinkErrorColor());
                            }
                        }
                    });
                }

                //同步当前状态
                if(list.size() == 0){
                    MapAlert mapAlert = new MapAlert();
                    mapAlert.empty();
                    list.add(mapAlert);
                }
                largeScreenMessageMange.saveCurrentMap(list);

                return Reply.ok(list);
            }
        } catch (Exception e) {
            log.error("mwLagerScreenService {} getIcmpLink",e);
            return Reply.fail(e.getMessage());
        }
        return Reply.ok();
    }

    private List<MapAlert> initMapAlertData(){
        //线路目标设备
        List<TargetAssetsIdDto> targetLinkAssetIds = dao.getTargetLinkAssetIds();
        //线路icmp设备
        List<TargetAssetsIdDto> icmpLinkAssetIds = dao.getIcmpLinkAssetIds();
        List<MapAlert> list = new ArrayList<>();
        List<String> assetsIds = new ArrayList<>();
        assetsIds.add("null");
        for (TargetAssetsIdDto targetLinkAssetId : targetLinkAssetIds) {
            for (TargetAssetsIdDto icmpLinkAssetId : icmpLinkAssetIds) {
                if (targetLinkAssetId.getLinkId().equals(icmpLinkAssetId.getLinkId())) {
                    MapAlert mapAlert = new MapAlert();
                    mapAlert.setTargetId(targetLinkAssetId.getId());
                    mapAlert.setIcmpId(icmpLinkAssetId.getId());

                    if(!assetsIds.contains(targetLinkAssetId.getId())){
                        assetsIds.add(targetLinkAssetId.getId());
                    }

                    if(!assetsIds.contains(icmpLinkAssetId.getId())){
                        assetsIds.add(icmpLinkAssetId.getId());
                    }

                    mapAlert.setLinkHostId(icmpLinkAssetId.getLinkHostId());
                    list.add(mapAlert);
                }
            }
        }

        Map criteria = new HashMap();
        criteria.put("ids", assetsIds);
        criteria.put("moduleType", OrgModuleType.ASSETS.name());
        Reply reply = mwOrgService.selectOrgMapByParamsAndIds(criteria);
        Map<String ,List<OrgMapperDTO>> orgMap = null;
        if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
            orgMap = (Map)reply.getData();
        }
        for (MapAlert mapAlert : list) {
            if(null != orgMap) {
                if (StringUtils.isNotEmpty(mapAlert.getTargetId())) {
                    List<OrgMapperDTO> targetOrgs = orgMap.get(mapAlert.getTargetId());
                    if (targetOrgs != null) {
                        if (targetOrgs.size() > 0) {
                            OrgMapperDTO orgMapperDTO = targetOrgs.get(0);
                            mapAlert.setTarget(orgMapperDTO.getCoordinate());
                        }
                    }
                }

                if (StringUtils.isNotEmpty(mapAlert.getIcmpId())) {
                    List<OrgMapperDTO> icmpOrgs = orgMap.get(mapAlert.getIcmpId());
                    if (icmpOrgs != null) {
                        if (icmpOrgs.size() > 0) {
                            OrgMapperDTO orgMapperDTO = icmpOrgs.get(0);
                            mapAlert.setIcmp(orgMapperDTO.getCoordinate());
                        }
                    }

                }
            }

            mapAlert.setColor(largeScreenMessageMange.getMapAlertConfig().getLinkNormalColor());
        }

        return list;
    }

    private String genRedisKey(String methodName, String objectName, Integer uid) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(":").append(objectName)
                .append("_").append(uid);
        return sb.toString();
    }


    @Autowired
    private WebSocket webSocket;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Transactional
    @Async
    public Reply getDataListByModelDataId1(String modelDataId,Integer userId) {
        try {
            ModelContentDto model = dao.getModelId(modelDataId);
            //大屏线路模块使用
            String interfaces = dao.getLinkInterfaces(null, DataType.SCREEN.getName(), modelDataId);
            if(null!=interfaces&&""!=interfaces){
                model.setLinkInterfaces(interfaces);
            }
            log.info("yuzhi11 "+model);
            Class<?> aClass = Class.forName(model.getClassName());
            log.info("yuzhi12 "+aClass);
            Method process = aClass.getMethod("process", ModelContentDto.class);
            log.info("yuzhi13 "+process);
           // Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            model.setUserId(userId);
            int timeLag = dao.getBulkDataTimeCount(modelDataId, userId) == 0 ? 600 : dao.getBulkDataTime(modelDataId, userId);
            log.info("yuzhi14 "+timeLag);
            model.setTimeLag(timeLag);
            process.invoke(applicationContext.getBean(aClass), model);
            log.info("yuzhi15 ");
            return Reply.ok();
        } catch (Exception e) {
            log.error("SCREEN_LOG[]screen[]大屏[]查询大屏的组件数据[]{}getDataListByModelDataId", e);
            return Reply.fail(ErrorConstant.SCREEN_MODEL_DATA_CODE_304008, ErrorConstant.SCREEN_MODEL_DAT_MSG_304008);
        }
    }

    @Override
    public Reply getRolePermission() {
        try {
            MwRoleDTO roleInfo = iLoginCacheInfo.getRoleInfo();
            String perm = roleInfo.getDataPerm();
            log.info("SCREEN_LOG[]screen[]大屏[]查询创建人的角色权限[]{}}", perm);
            return Reply.ok(perm);
        } catch (Exception e) {
            log.error("fail to getRolePermission with  cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_CREATOR_PERM_CODE_304010, ErrorConstant.SCREEN_CREATOR_PERM_MSG_304010);
        }
    }

    @Override
    public Reply updateScreenName(ScreenNameParam screenNameParam) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            screenNameParam.setModifier(loginName);
            dao.updateScreenName(screenNameParam);
            log.info("SCREEN_LOG[]screen[]大屏[]修改大屏的名称[]{}}", screenNameParam);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to updateScreenName with screenNameParam cause:{}", screenNameParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCREEN_UPDATE_NAME_CODE_304015, ErrorConstant.SCREEN_UPDATE_NAME_DATA_MSG_304015);
        }
    }


    @Override
    @Transactional()
    public Reply editorFilterAssets(FilterAssetsParam param) {
        try {

            if(param.getFilterLabelIds()!=null&&param.getFilterLabelIds().size()>0){
                param.setFilterLabelId(param.getFilterLabelIds().toString());
            }
            if(param.getFilterOrgIds()!=null&&param.getFilterOrgIds().size()>0){
                param.setFilterOrgId(param.getFilterOrgIds().toString());
            }
            param.setUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
            int count = dao.getFilterAssetsCount(param);
            if (count == 0) {
                dao.insertFilterAssets(param);
            } else {
                dao.updateFilterAssets(param);
            }
            if(param.getType().equals(DataType.INDEX.getName())){
                mwIndexDao.updateBulkName(param.getModelDataId(),param.getBulkName());
            }
            if (param.getType().equals(DataType.SCREEN.getName())) {
                String modelType = dao.getModelTypeById(param.getModelId());
                if (modelType.equals(MWUtils.MODEL_ALARM_COUNT_TYPE)) {
                    String assetsTypeName = mwAseetstemplateTableDao.selectTypeName(param.getAssetsTypeId());
                    dao.updateModelBase(assetsTypeName, param.getModelId(), param.getAssetsTypeId()==null?null:param.getAssetsTypeId().toString());
                }
            }
            log.info("SCREEN_INDEX_LOG[]screenORINDEX[]编辑首页或大屏资产过滤条件[]{}}", param);
        } catch (Exception e) {
            return Reply.fail(e.getMessage());
        }
        return Reply.ok();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
