package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.engineManage.service.MwEngineManageService;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.model.dao.*;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.param.citrix.ModelCitrixType;
import cn.mw.monitor.model.param.citrix.MwModelCitrixInfoParam;
import cn.mw.monitor.model.param.virtual.QueryVirtualInstanceParam;
import cn.mw.monitor.model.service.*;
import cn.mw.monitor.model.service.query.ModelQuery;
import cn.mw.monitor.model.service.query.ModelQueryFactory;
import cn.mw.monitor.model.util.ConnectCheckTest;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.AssetsDto;
import cn.mw.monitor.service.alert.dto.MWAlertAssetsParam;
import cn.mw.monitor.service.assets.api.MwInspectModeService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.service.assets.service.MwAssetsInterfaceService;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import cn.mw.monitor.service.dropdown.param.SelectCharDropDto;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.license.param.LicenseAssetsModuleStatusParam;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.link.param.AssetsParam;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.ModelInstanceBaseInfoDTO;
import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelAssetsByESService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.MwLoginUserDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.virtual.dto.VirtualizationMonitorInfo;
import cn.mw.monitor.service.virtual.dto.VirtualizationType;
import cn.mw.monitor.service.zbx.param.QueryAlertStateParam;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.TransferUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWTPServerProxy;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.ConnectCheckModelEnum.CITRIXADC;
import static cn.mw.monitor.model.param.MatchModelTypeEnum.PROJECTS;
import static cn.mw.monitor.model.param.citrix.ModelCitrixType.*;
import static cn.mw.monitor.service.assets.utils.RuleType.getInfoByName;
import static cn.mw.monitor.service.model.service.ModelCabinetField.*;
import static cn.mw.monitor.service.model.service.ModelPropertiesType.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.*;
import static cn.mw.monitor.service.virtual.dto.VirtualizationType.*;

/**
 * @author qzg
 * @date 2022/10/19
 */
@Service
@Slf4j
public class MwModelViewServiceImpl extends ListenerService implements MwModelViewService, MwModelViewCommonService
        , InitializingBean {

    public static List typeList = Arrays.asList(
            ModelPropertiesType.STRUCE.getCode(),
            ModelPropertiesType.MULTIPLE_ENUM.getCode(),
            MULTIPLE_RELATION.getCode(),
            ModelPropertiesType.ORG.getCode(),
            ModelPropertiesType.USER.getCode(),
            ModelPropertiesType.GROUP.getCode(),
            ModelPropertiesType.LAYOUTDATA.getCode());

    //字符串类型
    public static List arrList = Arrays.asList(
            ModelPropertiesType.STRING.getCode(),
            SINGLE_RELATION.getCode(),
            DATE.getCode(),
            ModelPropertiesType.IP.getCode(),
            ModelPropertiesType.SINGLE_ENUM.getCode());

    static {
        MwModelViewCommonService.DATA_PERMINSSION_KEY.add("orgIds");
        MwModelViewCommonService.DATA_PERMINSSION_KEY.add("groupIds");
        MwModelViewCommonService.DATA_PERMINSSION_KEY.add("userIds");
    }

    private int pageSize = 10000;
    @Autowired
    private MwModelWebMonitorService mwModelWebMonitorService;
    @Autowired
    private MwInspectModeService mwInspectModeService;
    @Resource
    private MWModelTemplateDao mwModelTemplateDao;
    @Autowired
    private LicenseManagementService licenseManagementService;
    @Autowired
    private ConnectCheckTest connectCheckTest;
    @Autowired
    private MwModelInstanceServiceImplV1 mwModelInstanceServiceImplV1;
    @Autowired
    private MwModelAssestDiscoveryService mwModelAssetsDiscoveryService;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private MwModelViewDao mwModelViewDao;
    @Resource
    private MwModelAssetsDiscoveryDao mwModelAssetsDiscoveryDao;
    @Autowired
    private ModelServer modelSever;
    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;
    @Autowired
    private ILoginCacheInfo loginCacheInfo;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;
    @Autowired
    private MWOrgCommonService mwOrgCommonService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;

    @Autowired
    private MwModelCommonServiceImpl mwModelCommonServiceImpl;

    @Resource
    private MwModelManageDao mwModelManageDao;

    @Resource
    private MwModelInstanceDao mwModelInstanceDao;

    @Autowired
    private MWUserService userService;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Resource
    private MwModelExportDao mwModelExportDao;

    @Autowired
    private MwModelManageService mwModelManageService;

    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MwModelAssetsByESService mwModelAssetsByESService;

    @Autowired
    private MwEngineManageService mwEngineManageService;

    @Autowired
    private MwAssetsInterfaceService mwAssetsInterfaceService;

    @Autowired
    private MwModelVirtualizationService mwModelVirtualizationService;

    @Autowired
    private MWAlertService mwAlertService;

    //资产id和key的告警key的映射
    private Map<String, String> assetIdKeyMap = new HashMap<>();

    //es中资产类型字段
    private static final String ASSETSTYPENAME = "assetsTypeName";
    //es中资产类型字段
    private static final String ASSETSTYPEID = "assetsTypeId";
    //es中资产子类型字段
    private static final String ASSETSTYPESUBNAME = "assetsTypeSubName";
    //es中资产子类型字段
    private static final String ASSETSTYPESUBID = "assetsTypeSubId";

    public static final String  IS_MANAGE = "isManage";
    //es中监控方式字段
    private static final String MONITORMODE = "monitorMode";
    //es中监控方式字段
    public static final String MONITORMODENAME = "monitorModeName";
    //es中监控服务器字段
    private static final String MONITORSERVERID = "monitorServerId";

    //es中监控服务器字段
    public static final String MONITORSERVERNAME = "monitorServerName";
    //es中监控服务器字段
    public static final String POLLINGENGINE = "pollingEngine";
    //es中监控服务器字段
    public static final String POLLINGENGINENAME = "pollingEngineName";

    public static final String LOCALENGINE = "localhost";
    private static final String LOCAL = "本机";
    private static final String CUSTOM = "自定义";
    private final String UNKNOWN = "未知";

    private final String NORMAL = "正常";

    private final String ABNORMAL = "异常";

    private final String ALERT = "告警";

    private final String unknown = "未知";

    @Value("${model.debug}")
    private boolean debug;

    @Value("${es.duration.timeNum}")
    private int timeNum;

    //    @Value("${model.instance.batchFetchNum}")
    private static final int insBatchFetchNum = 1000;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    @Value("${tangible.jump.url}")
    private String tangibleJumpUrl;

    @Value("${model.jump.url}")
    private String modelJumpUrl;

    @Value("${modelView.commonField.parentModelId}")
    private String parentModelIds;

    private final String defaultInstanceModel = "16";

    @Value("${modelSystem.groupId}")
    private String modelSystemGroupId;
    @Value("${modelSystemParent.modelId}")
    private String modelSystemParentModelId;
    @Value("${alert.level}")
    private String alertLevel;
    private static String alertLevelStr = "default";
    //监控服务器集合
    private Set<Integer> monitorServerSet = new CopyOnWriteArraySet<>();
    //web监测模型Id
    private static Integer webMonitorModeId = 72;
    //web监测分组Id
    private static String webMonitorGroupId = ",11,";
    @Autowired
    private MwModelSysClassIfyHandle mwModelSysClassIfyHandle;

    @Override
    public Reply getScanSuccessInfoById(QueryModelViewInstanceParam param) {
        try {
            MwModelScanResultSuccessParam successParam = mwModelViewDao.getScanSuccessInfoById(param.getScanSuccessId());
            List groupNodeList = new ArrayList();
            //根据资产子类型获取模型的分组信息
            if (successParam.getAssetsTypeSubId() != null && successParam.getAssetsTypeSubId() != -1) {
                String modelGroupNodes = mwModelViewDao.getModelIdGroups(successParam.getAssetsTypeSubId());
                if (!Strings.isNullOrEmpty(modelGroupNodes) && modelGroupNodes.split(",").length > 0) {
                    String[] str = modelGroupNodes.split(",");
                    for (String groupNode : str) {
                        //将分组信息组装成数字数组，方便前端显示
                        if (!Strings.isNullOrEmpty(groupNode)) {
                            groupNodeList.add(intValueConvert(groupNode));
                        }
                    }
                }
            }
            successParam.setModelGroup(groupNodeList);
            return Reply.ok(successParam);
        } catch (Throwable e) {
            log.error("fail to getScanSuccessInfoById cause:{}", e);
            return Reply.fail(500, "根据规则id获取扫描成功结果信息失败");
        }
    }

    @Override
    public Reply getMacInfoByTemplate(MwModelTemplateInfo param) {
        List<MacrosDTO> macros = new ArrayList<>();
        Map<Integer, String> m = new HashMap();
        List<MwModelTemplateInfo> templateInfos = mwModelViewDao.getServerTemplateIdByName(param);
        for (MwModelTemplateInfo info : templateInfos) {
            m.put(info.getServerId(), info.getServerTemplateId());
        }
        List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
        for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
            String templateId = m.get(mwtpServerAPI.getServerId());
            if (!Strings.isNullOrEmpty(templateId)) {
                MWZabbixAPIResult result = mwtpServerAPI.getMacrosByTemplateId(mwtpServerAPI.getServerId(), templateId);
                if (!result.isFail() && ((ArrayNode) result.getData()).size() > 0) {
                    List<MacrosDTO> macrosList = mwModelAssetsDiscoveryDao.selectMacros();
                    Map<String, List<MacrosDTO>> collect = macrosList.stream().collect(Collectors.groupingBy(Macros::getMacro));
                    JsonNode node = (JsonNode) result.getData();
                    if (node.size() > 0) {
                        node.forEach(macro -> {
                            List<MacrosDTO> macroDTO = collect.get(macro.get("macro").asText());
                            if (macroDTO != null && macroDTO.size() > 0) {
                                MacrosDTO macrosDTO = macroDTO.get(0);
                                macrosDTO.setValue(macro.get("value").asText());
                                macros.add(macrosDTO);
                            }
                        });
                    }
                    //多个zabbix服务器，只要有一个有值，就直接获取，剩余的直接跳出。
                    break;
                }
            }
        }
        return Reply.ok(macros);
    }

    @Override
    public Reply getTemplateInfoByMode(MwModelTemplateInfo param) {
        List<MwModelTemplateInfo> templateList = mwModelViewDao.getTemplateInfoByMode(param);
        return Reply.ok(templateList);
    }

    /**
     * 凭证信息check
     *
     * @param params
     * @return
     */
    @Override
    public boolean checkAuthenticationInfo(List<MwModelMacrosValInfoParam> params) {
        boolean isCheck = false;
        //去除引擎信息
        Iterator<MwModelMacrosValInfoParam> iterator = params.iterator();
        while (iterator.hasNext()) {
            MwModelMacrosValInfoParam param = iterator.next();
            if ("2".equals(param.getMacroType())) {
                iterator.remove();
            }
        }
        if (CollectionUtils.isNotEmpty(params)) {
            MwModelMacrosValInfoParam param = params.get(0);
            List<MwModelMacrosValInfoParam> macroList = mwModelViewDao.getMacrosInfoByName(param);
            params.sort(Comparator.comparing(t -> t.getMacro()));
            macroList.sort(Comparator.comparing(t -> t.getMacro()));
            //新增的数据和数据库中查询的数据一致，则不需要新增更新
            if (params.toString().equals(macroList.toString())) {
                isCheck = false;
            } else {
                isCheck = true;
            }
        }
        return isCheck;
    }

    /**
     * 获取所有模板名称
     */
    @Override
    public Reply getAllTemplateName() {
        try {
            List<MwModelTemplateInfo> list = mwModelViewDao.getAllTemplateName();
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getAllTemplateName with cause:{}", e);
            return Reply.fail("获取所有模板名称失败！");
        }
    }

    @Override
    public Reply getTemplateNameAndMonitorMode(MwModelTemplateInfo param) {
        try {
            log.info("进入方法getTemplateNameAndMonitorMode中");
            List<MwModelTemplateInfo> list = mwModelViewDao.getTemplateNameAndMonitorMode(param);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getTemplateNameByServerId with cause:{}", e);
            return Reply.fail("获取模板名称监控方式失败！");
        }
    }

    @Override
    public Reply getMarcoInfoByModelId(MwModelTemplateInfo param) {
        List<MwModelMacrosValInfoParam> macrosInfo = new ArrayList<>();
        List<Integer> modelIds = Arrays.asList(param.getModelId());
        if (CollectionUtils.isNotEmpty(modelIds)) {
            macrosInfo = mwModelViewDao.getMacrosInfoByModel(modelIds);
        }
        List<MwModelAuthNameParam> marcoList = new ArrayList<>();
        //数据组装，将凭证名称作为下拉选项的key显示，宏值作为value
        Map<String, List<MwModelMacrosValInfoParam>> map = macrosInfo.stream().filter(item -> !Strings.isNullOrEmpty(item.getAuthName())).collect(Collectors.groupingBy(MwModelMacrosValInfoParam::getAuthName));
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, List<MwModelMacrosValInfoParam>> entry : map.entrySet()) {
                MwModelAuthNameParam authParam = new MwModelAuthNameParam();
                authParam.setAuthName(entry.getKey());
                authParam.setMacrosParam(entry.getValue());
                marcoList.add(authParam);
            }
        }
        return Reply.ok(marcoList);
    }


    @Override
    public Reply getMarcoInfoByTemplateNameAndMode(MwModelTemplateInfo param) {
        List<MwModelMacrosValInfoParam> macrosInfo = new ArrayList<>();
        param.setTemplateName(param.getTemplateName().split("@")[0]);
        List<Integer> modelIds = mwModelViewDao.getModelIdByTemplateNameAndMode(param);
        if (CollectionUtils.isNotEmpty(modelIds)) {
            macrosInfo = mwModelViewDao.getMacrosInfoByModel(modelIds);
        }
        List<MwModelAuthNameParam> marcoList = new ArrayList<>();
        //数据组装，将凭证名称作为下拉选项的key显示，宏值作为value
        Map<String, List<MwModelMacrosValInfoParam>> map = macrosInfo.stream().filter(item -> !Strings.isNullOrEmpty(item.getAuthName())).collect(Collectors.groupingBy(MwModelMacrosValInfoParam::getAuthName));
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, List<MwModelMacrosValInfoParam>> entry : map.entrySet()) {
                MwModelAuthNameParam authParam = new MwModelAuthNameParam();
                authParam.setAuthName(entry.getKey());
                authParam.setMacrosParam(entry.getValue());
                marcoList.add(authParam);
            }
        }
        return Reply.ok(marcoList);
    }

    @Override
    public Reply deleteMarcoInfoByModel(DeleteModelMacrosParam param) {
        mwModelViewDao.deleteMarcoInfoByModel(param);
        return Reply.ok("删除成功");
    }

    /**
     * 根据模板名称获取厂商信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getBrandByTemplateName(MwModelTemplateInfo param) {
        try {
            List<AddAndUpdateModelFirmParam> list = mwModelViewDao.getBrandByTemplateName(param.getTemplateName());
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getBrandByTemplateName with cause:{}", e);
            return Reply.fail("根据模板名称获取厂商信息失败！");
        }
    }

    /**
     * 根据模板名称规格信号获取描述信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getSpecificationByTemplateBrand(MwModelTemplateInfo param) {
        try {
            List<AddAndUpdateModelFirmParam> list = mwModelViewDao.getSpecificationByTemplateNameAndBrand(param);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getSpecificationByTemplateNameAndBrand with cause:{}", e);
            return Reply.fail("根据模板名称规格信号获取描述信息失败！");
        }
    }

    @Override
    public Reply getAllVendorInfo() {
        List<AddAndUpdateModelFirmParam> list = mwModelViewDao.getAllVendorInfo();
        return Reply.ok(list);
    }

    @Override
    public Reply getAllSpecificationInfo() {
        List<AddAndUpdateModelFirmParam> list = mwModelViewDao.getAllSpecificationInfo();
        return Reply.ok(list);
    }

    /**
     * 根据模板名称规格信号获取描述信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getDescriptionByTemplateSpecification(MwModelTemplateInfo param) {
        try {
            List<AddAndUpdateModelFirmParam> list = mwModelViewDao.getDescriptionByTemplateSpecification(param);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getDescriptionByTemplateSpecification with cause:{}", e);
            return Reply.fail("根据模板名称规格信号获取描述信息失败！");
        }
    }

    /**
     * 获取资产视图列表字段
     *
     * @return
     */
    @Override
    public Reply getModelCommonFields(String type) {
        //获取当前登录用户ID
        Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
        //获取model属性
        List<ModelInfo> modelInfoList = mwModelManageDao.selectModelInfoByPids(parentModelIds);
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        Map<String, PropertyInfo> maps = new HashMap<>();
        for (ModelInfo modelInfo : modelInfoList) {
            for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                maps.put(modelInfo.getModelId() + "_" + propertyInfo.getIndexId(), propertyInfo);
            }
        }
        //获取es实例数据中共用的模型属性
        List<MwCustomColByModelDTO> propertiesList = mwModelViewDao.getPropertiesIndexByCommon(userId, parentModelIds);
        for (MwCustomColByModelDTO dto : propertiesList) {
            PropertyInfo propertyInfo = new PropertyInfo();
            if (maps != null && maps.size() > 0 && maps.get(dto.getModelId() + "_" + dto.getProp()) != null) {
                propertyInfo = maps.get(dto.getModelId() + "_" + dto.getProp());
                dto.setRelationModelIndex(propertyInfo.getRelationModelIndex());
                dto.setRelationPropertiesIndex(propertyInfo.getRelationPropertiesIndex());
                dto.setIsInsertShow(propertyInfo.getIsInsertShow());
                dto.setIsEditorShow(propertyInfo.getIsEditorShow());
                dto.setIsLookShow(propertyInfo.getIsLookShow());
                dto.setIsListShow(propertyInfo.getIsListShow());
            }
            //实例名称默认新增查询列表修改全部显示
            if (INSTANCE_NAME_KEY.equals(dto.getProp())) {
                dto.setIsInsertShow(true);
                dto.setIsEditorShow(true);
                dto.setIsLookShow(true);
                dto.setIsListShow(true);
            }
            List<ModelPropertiesStructDto> structList = new ArrayList<>();
            if (!Strings.isNullOrEmpty(dto.getDropOpStr())) {
                dto.setDropOp(Arrays.asList(dto.getDropOpStr().split(",")));
            }
            if (!Strings.isNullOrEmpty(dto.getDefaultValueListStr())) {
                dto.setDefaultValueList(Arrays.asList(dto.getDefaultValueListStr().split(",")));
            }
            if (!Strings.isNullOrEmpty(dto.getGangedValueListStr())) {
                dto.setGangedValueList(JSONArray.parseArray(dto.getGangedValueListStr()));
            }
            if (!Strings.isNullOrEmpty(dto.getDropArrObjStr())) {
                dto.setDropArrObj(JSONArray.parseArray(dto.getDropArrObjStr()));
            }
            // inputFormat 将模型属性类型转为页面下拉查询字段类型
            //1:文本 2:时间 6:下拉框
            if (strValueConvert(ModelPropertiesType.DATE.getCode()).equals(dto.getType())) {//8为时间
                dto.setInputFormat("2");
            } else if (strValueConvert(SINGLE_RELATION.getCode()).equals(dto.getType()) ||
                    strValueConvert(MULTIPLE_RELATION.getCode()).equals(dto.getType()) ||
                    strValueConvert(ModelPropertiesType.SINGLE_ENUM.getCode()).equals(dto.getType()) ||
                    strValueConvert(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(dto.getType()) ||
                    strValueConvert(ModelPropertiesType.ORG.getCode()).equals(dto.getType()) ||
                    strValueConvert(ModelPropertiesType.USER.getCode()).equals(dto.getType()) ||
                    strValueConvert(ModelPropertiesType.GROUP.getCode()).equals(dto.getType())) {
                dto.setInputFormat("6");
            } else {
                dto.setInputFormat("1");
            }
            //属性类型为结构体
            if (strValueConvert(ModelPropertiesType.STRUCE.getCode()).equals(dto.getType())) {
                //根据modelId和属性IndexId，查询属性结构体信息
                structList = mwModelManageDao.getProperticesStructInfo(dto.getModelId(), dto.getProp());
                if (structList != null) {
                    for (ModelPropertiesStructDto mps : structList) {
                        //结构体数据类型为9和10时，代表的数据类型为数组格式，需要转换
                        if (mps.getStructType() != null && (mps.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || mps.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode())) {
                            if (!Strings.isNullOrEmpty(mps.getStructStrValue())) {
                                mps.setStructListValue(Arrays.asList(mps.getStructStrValue().split(",")));
                            }
                        }
                    }
                    dto.setPropertiesStruct(structList);
                } else {
                    dto.setPropertiesStruct(structList);
                }
            }
        }
        List<MwCustomColByModelDTO> propertiesListNew = new ArrayList<>();
        if (Strings.isNullOrEmpty(type)) {
            propertiesListNew = propertiesList;
        } else {
            InstanceFieldQueryType tast = InstanceFieldQueryType.valueOf(strValueConvert(type));
            switch (tast) {
                case insert:
                    propertiesListNew = propertiesList.stream().filter(s -> s.getIsInsertShow() != null && s.getIsInsertShow()).collect(Collectors.toList());
                    break;
                case list:
                    propertiesListNew = propertiesList.stream().filter(s -> s.getIsListShow() != null && s.getIsListShow()).collect(Collectors.toList());
                    break;
                case editor:
                    propertiesListNew = propertiesList.stream().filter(s -> s.getIsEditorShow() != null && s.getIsEditorShow()).collect(Collectors.toList());
                    break;
                case look:
                    propertiesListNew = propertiesList.stream().filter(s -> s.getIsLookShow() != null && s.getIsLookShow()).collect(Collectors.toList());
                    break;
            }
        }
        return Reply.ok(propertiesListNew);
    }
//    public Reply getModelCommonFields() {
//
//
//        //获取当前登录用户ID
//        Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
////        //获取es实例数据中共用的模型属性
//        List<MwCustomColByModelDTO> propertiesList = mwModelViewDao.getPropertiesIndexByCommon(userId);
////        List<MwCustomColByModelDTO> propertiesList = new ArrayList<>();
//
//        //根据父模型获取字段属性
//        String pids = parentModelIds;
//        List<ModelInfo> modelInfoList = mwModelManageDao.selectModelInfoByPids(pids);
//        List<PropertyInfo> allProperty = new ArrayList<>();
//        for (ModelInfo modelInfo : modelInfoList) {
//            for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
//                allProperty.add(propertyInfo);
//            }
//        }
//        //获取所有父模型的CustCol信息
//        List<MwCustomColByModelDTO> customColList = mwModelManageDao.selectCustomColList(allProperty,userId);
//
//        //获取当前模型的instanceName属性的CustCol信息
////        List<MwCustomColByModelDTO> customColToInstanceName = mwModelManageDao.selectCustomColList(Arrays.asList(INSTANCE_NAME_KEY),userId,null);
//
//        List<MwCustomColByModelDTO> ret = null;
//
//        MwCustomColByModelDTO customColByModelDTO = new MwCustomColByModelDTO();
//
//        if (null != modelInfoList) {
//            ret = new ArrayList<>();
//            Set<String> propertyNameSet = new HashSet<>();
//            for (ModelInfo modelInfo : modelInfoList) {
//                if (null != modelInfo.getPropertyInfos() && modelInfo.getPropertyInfos().size() > 0) {
//                    for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
//
//                        PropertyFilterType propertyFilterType = PropertyFilterType.valueOf("list");
//                        if (!propertyFilterType.filter(propertyInfo)) {
//                            continue;
//                        }
//
//                        if (!propertyInfo.getIsShow()
//                                || propertyNameSet.contains(propertyInfo.getPropertiesName())) {
//                            continue;
//                        }
//
//                        MwCustomColByModelDTO mwCustomColByModelDTO = new MwCustomColByModelDTO();
//                        mwCustomColByModelDTO.extractFrom(propertyInfo);
//                        mwCustomColByModelDTO.setModelId(modelInfo.getModelId());
//                        ret.add(mwCustomColByModelDTO);
//                        propertyNameSet.add(propertyInfo.getPropertiesName());
//                    }
//                }
//            }
//        }
//        for(MwCustomColByModelDTO customDtos : ret){
//            for(MwCustomColByModelDTO customDto :  customColList){
//                    if(customDtos.getProp().equals(customDto.getProp()) && customDtos.getModelId().equals(customDto.getModelId())){
//                        customDtos.setColId(customDto.getColId());
//                        customDtos.setOrderNumber(customDto.getOrderNumber());
//                        customDtos.setCustomId(customDto.getCustomId());
//                        customDtos.setVisible(customDto.getVisible());
//                        customDtos.setWidth(customDto.getWidth());
//                        customDtos.setUserId(customDto.getUserId());
//                        customDtos.setSortable(customDto.getSortable());
//                    }
//            }
//        }
//
//        customColByModelDTO.setProp(INSTANCE_NAME_KEY);
//        customColByModelDTO.setLabel(INSTANCE_NAME_FIELD);
//        customColByModelDTO.setPropertiesTypeId(1);
//        customColByModelDTO.setInputFormat("1");
//        customColByModelDTO.setOrderNumber(1);
//        ret.add(0,customColByModelDTO);
//        List<MwCustomColByModelDTO> rets =  ret.stream().filter(s ->s.getVisible()!=null && s.getVisible()).collect(Collectors.toList());
//        rets = rets.stream().sorted(Comparator.comparing(MwCustomColByModelDTO::getOrderNumber)).collect(Collectors.toList());
////        Collections.sort(ret);
//
//
////        //获取所有属性
////        List<PropertyInfo> propertyInfos = new ArrayList<>();
////        for (ModelInfo mInfo : modelInfoList) {
////            propertyInfos.addAll(mInfo.getPropertyInfos());
////        }
////        List<PropertyInfo> propertyInfoByShow = new ArrayList<>();
////        propertyInfoByShow =  propertyInfos.stream().filter(s ->s.getIsShow()!=null ? s.getIsShow() :true).collect(Collectors.toList());
////        List<PropertyInfo> propertyInfoByLook = new ArrayList<>();
////        propertyInfoByLook =  propertyInfoByShow.stream().filter(s ->s.getIsLookShow()!=null ? s.getIsLookShow() :true).collect(Collectors.toList());
//
////        for (MwCustomColByModelDTO dto : propertiesList) {
////            List<ModelPropertiesStructDto> structList = new ArrayList<>();
////            if (!Strings.isNullOrEmpty(dto.getDropOpStr())) {
////                dto.setDropOp(Arrays.asList(dto.getDropOpStr().split(",")));
////            }
////            if (!Strings.isNullOrEmpty(dto.getDefaultValueListStr())) {
////                dto.setDefaultValueList(Arrays.asList(dto.getDefaultValueListStr().split(",")));
////            }
////            if (!Strings.isNullOrEmpty(dto.getGangedValueListStr())) {
////                dto.setGangedValueList(JSONArray.parseArray(dto.getGangedValueListStr()));
////            }
////            if (!Strings.isNullOrEmpty(dto.getDropArrObjStr())) {
////                dto.setDropArrObj(JSONArray.parseArray(dto.getDropArrObjStr()));
////            }
////            // inputFormat 将模型属性类型转为页面下拉查询字段类型
////            //1:文本 2:时间 6:下拉框
////            if (String.valueOf(ModelPropertiesType.DATE.getCode()).equals(dto.getType())) {//8为时间
////                dto.setInputFormat("2");
////            } else if (String.valueOf(ModelPropertiesType.RELATION.getCode()).equals(dto.getType()) ||
////                    String.valueOf(ModelPropertiesType.SINGLE_ENUM.getCode()).equals(dto.getType()) ||
////                    String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(dto.getType()) ||
////                    String.valueOf(ModelPropertiesType.ORG.getCode()).equals(dto.getType()) ||
////                    String.valueOf(ModelPropertiesType.USER.getCode()).equals(dto.getType()) ||
////                    String.valueOf(ModelPropertiesType.GROUP.getCode()).equals(dto.getType())) {
////                dto.setInputFormat("6");
////            } else {
////                dto.setInputFormat("1");
////            }
////            //属性类型为结构体
////            if (String.valueOf(ModelPropertiesType.STRUCE.getCode()).equals(dto.getType())) {
////                //根据modelId和属性IndexId，查询属性结构体信息
////                structList = mwModelManageDao.getProperticesStructInfo(dto.getModelId(), dto.getProp());
////                if (structList != null) {
////                    for (ModelPropertiesStructDto mps : structList) {
////                        //结构体数据类型为9和10时，代表的数据类型为数组格式，需要转换
////                        if (mps.getStructType() != null && (mps.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || mps.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode())) {
////                            if (!Strings.isNullOrEmpty(mps.getStructStrValue())) {
////                                mps.setStructListValue(Arrays.asList(mps.getStructStrValue().split(",")));
////                            }
////                        }
////                    }
////                    dto.setPropertiesStruct(structList);
////                } else {
////                    dto.setPropertiesStruct(structList);
////                }
////            }
////        }
//        return Reply.ok(rets);
//    }

    protected Map<String, Object> getModelListInfoByBase(QueryInstanceModelParam param) {
        Map<String, Object> dataInfo = new HashMap();
        List<Map<String, Object>> listMap = new ArrayList<>();

        PageInfo pageInfo = new PageInfo<List>();
        PageList pageList = new PageList();
        try {

            if (CollectionUtils.isNotEmpty(param.getModelIndexs()) && CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                //如果实例id数量太多,需要分组
                List<List<Integer>> instanceIdGroups = null;
                if (null != param.getInstanceIds()) {
                    instanceIdGroups = Lists.partition(param.getInstanceIds(), insBatchFetchNum);
                }
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                //全字段模糊查询
                if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                    BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                    BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                    for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                        //资产视图 左侧树结构查询
                        if ((m.getIsTreeQuery() != null && m.getIsTreeQuery())) {
                            if (StringUtils.isBlank(m.getPropertiesIndexId())) {
                                continue;
                            }
                            //树结构中，节点Id为-2 或者name为"未知"，表示该节点字段数据不存在或为空
                            if ((!Strings.isNullOrEmpty(m.getPropertiesValue()) && (UNKNOWN.equals(m.getPropertiesValue()) || "-2".equals(m.getPropertiesValue())))) {
                                //es中 ‘字段不存在’ 的查询语句
                                queryBuilder = queryBuilder.mustNot(QueryBuilders.existsQuery(m.getPropertiesIndexId())).should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", ""));
                            } else {
                                //字符串
                                if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                    String value = m.getPropertiesValue().replace("*", "\\*").replace("?", "\\?");
                                    if ("groupNodes".equals(m.getPropertiesIndexId())) {
                                        //树结构groupNodes查询，使用模糊查询
                                        queryBuilder1 = queryBuilder1.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + value + "*"));
                                    } else {
                                        queryBuilder1 = queryBuilder1.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", value));
                                    }
                                }
                                //数组
                                if ((typeList.contains(m.getPropertiesType())) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                    queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                                }
                                //布尔类型
                                if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                    queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                                }
                                queryBuilder.must(queryBuilder1);
                            }
                        } else {
                            //资产视图列表输入框查询
                            QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
                            if (null != qb) {
                                queryBuilder2.should(qb);
                            }
                        }
                    }
                    queryBuilder.must(queryBuilder2);
                }
                BoolQueryBuilder pQuery = null;
                long count = 0;
                if (null != instanceIdGroups) {
                    int startRow = 0;
                    for (List<Integer> intancedIds : instanceIdGroups) {
                        pQuery = createCopy(queryBuilder);
                        //web监测数据不进行查询过滤，后面特殊处理
                        if (param.getModelId() == webMonitorModeId) {
                            pQuery = new BoolQueryBuilder();
                        }
                        QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
                        pQuery.must(queryBuilder1);
                        if (debug) {
                            log.info("es query index {}", param.getModelIndexs().toString());
                            log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
                        }
                        SearchResponse search = doEsSearchResponse(pQuery
                                , startRow
                                , insBatchFetchNum
                                , param
                                , param.getModelIndexs());

                        for (SearchHit searchHit : search.getHits().getHits()) {
                            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                            sourceAsMap.put(ESID, searchHit.getId());
                            if (sourceAsMap.containsKey(MONITOR_FLAG) && sourceAsMap.get(MONITOR_FLAG) != null) {
                                Object monitorFlag = sourceAsMap.get(MONITOR_FLAG);
                                if ("true".equals(monitorFlag.toString())) {
                                    sourceAsMap.put(MONITOR_FLAG, true);
                                }
                                if ("false".equals(monitorFlag.toString())) {
                                    sourceAsMap.put(MONITOR_FLAG, false);
                                }
                            }
                            listMap.add(sourceAsMap);
                        }
//                        count += search.getHits().getTotalHits().value;
//                        startRow += insBatchFetchNum;
                    }
                }

                List<Map<String, Object>> mapList = new ArrayList<>(listMap);

                //过滤出只有web监测的数据
                List<Map<String, Object>> disListMap = listMap.stream().filter(s -> webMonitorModeId.equals(intValueConvert(s.get(ASSETTYPE_SUB_ID)))).collect(Collectors.toList());
                boolean bool2 = CollectionUtils.isNotEmpty(disListMap);
                List<Map<String, Object>> webMonitorList = new ArrayList<>();
                if (bool2) {
                    webMonitorList = disListMap;
                } else {
                    webMonitorList = listMap;
                }

                boolean bool1 = false;

                //是否选择了web监测模型
                boolean bool3 = webMonitorModeId.equals(param.getModelId());

                //根据树结构groupNodes:11或assetsTypeSubId:72，表示web监测模型，单独处理
                if (CollectionUtils.isNotEmpty(param.getPropertiesList())) {
                    bool1 = param.getPropertiesList().stream().anyMatch(a -> (ASSETTYPE_SUB_ID.equals(a.getPropertiesIndexId()) && strValueConvert(webMonitorModeId).equals(a.getPropertiesValue()))
                            || (GROUP_NODES.equals(a.getPropertiesIndexId()) && webMonitorGroupId.equals(a.getPropertiesValue())));
                }
                boolean bool = bool1 || bool3;
                if (bool && bool2) {
                    mapList.removeAll(disListMap);
                    //进入web监测查询接口
                    List<MwModelWebMonitorTable> webMonitorParams = MwModelUtils.convertEsData(MwModelWebMonitorTable.class, webMonitorList);
                    List<MwModelWebMonitorTable> webMonitorTables = mwModelWebMonitorService.queryWebSeverList(webMonitorParams);
                    List<Map<String, Object>> newListMap = ListMapObjUtils.convertList(webMonitorTables);

                    listMap = newListMap;
                    //web监测查询处理
                    listMap = webMonitorConvert(listMap, param);
                    for (Map<String, Object> ms : listMap) {
                        String status = "";
                        if (NORMAL.equals(strValueConvert(ms.get(WEB_STATE)))) {
                            status = "NORMAL";
                        }
                        if (ABNORMAL.equals(strValueConvert(ms.get(WEB_STATE)))) {
                            status = "ABNORMAL";
                        }
                        ms.put(ITEM_ASSETS_STATUS, status);
                    }
                }

                count = listMap.size();
                int startIndex = param.getPageNumber();
                int endIndex = param.getPageSize();
//                //根据树结构ModelId:72，表示web监测模型，单独处理
//                if (param.getModelId() == webMonitorModeId) {
//                    startIndex = 0;
//                    endIndex = pageSize;
//                }
                if (mwInspectModeService.getInspectModeInfo()) {
                    List<Map<String, Object>> newList = getAssetsStateByZabbix(listMap);
                    dataInfo.put("sum", (long) newList.size());
                    newList = pageList.getList(newList, startIndex, endIndex);
                    dataInfo.put("data", newList);

                } else {
                    listMap = pageList.getList(listMap, startIndex, endIndex);
                    dataInfo.put("data", listMap);
                    dataInfo.put("sum", count);
                }
            }
        } catch (
                Exception e) {
            log.error("fail to getModelListInfoByBase param{}, case by {}", param, e);
        }
        return dataInfo;
    }


    @Override
    public List<Integer> getModelGroupIdByName(String name) {
        List<Integer> groupIds = mwModelManageDao.getModelGroupIdByName(name);
        return groupIds;
    }

    @Override
    public List<Map<String, Object>> getModelListInfoByPerm(QueryModelAssetsParam qParam) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        List<Map<String, Object>> newList = new ArrayList<>();
        try {
            long time1 = System.currentTimeMillis();
            Map<String, Object> paramMap = ListMapObjUtils.beanToMap(qParam);
            QueryInstanceModelParam esParam = new QueryInstanceModelParam();
            esParam.setModelIndexs(qParam.getModelIndexs());
            esParam.setInstanceIds(qParam.getInstanceIds());
            esParam.setFieldList(qParam.getFieldList());
            esParam.setNoFieldList(qParam.getNoFieldList());
            esParam.setSkipDataPermission(qParam.getSkipDataPermission());
            //获取所有模型分组
            List<AssetTypeIconDTO> assetTypeIconDTOS = mwModelViewDao.selectAllAssetsTypeIcon();
            Map<String, AssetTypeIconDTO> assetTypeMap = new HashMap<>();
            for (AssetTypeIconDTO assetTypeIconDTO : assetTypeIconDTOS) {
                assetTypeMap.put(assetTypeIconDTO.getId().toString(), assetTypeIconDTO);
            }
            //获取所有基础模型的公共属性
            Map<Integer, ModelInfo> modelInfoMap = new HashMap<>();
            List<String> modelIndexs = new ArrayList<>();
            List<ModelInfo> modelInfoList = mwModelManageDao.getBaseModelInfos();
            Map<String, PropertyInfo> propertyMap = new HashMap<>();
            if (null != modelInfoList) {
                for (ModelInfo modelInfo : modelInfoList) {
                    modelInfoMap.put(modelInfo.getModelId(), modelInfo);
                    if (modelInfo.getModelTypeId() != null && modelInfo.getModelTypeId().intValue() == 1) {
                        modelIndexs.add(modelInfo.getModelIndex());
                    }

                    if (null != modelInfo.getPropertyInfos()) {
                        for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                            propertyMap.put(propertyInfo.getIndexId(), propertyInfo);
                        }
                    }
                }
            }

            //获取所有基础设施下的模型Index
            if (CollectionUtils.isEmpty(esParam.getModelIndexs())) {
                esParam.setModelIndexs(modelIndexs);
            } else {//只查询给定的medelIndexs;
                modelIndexs = new ArrayList<>();
                modelIndexs.addAll(esParam.getModelIndexs());
            }
            GlobalUserInfo globalUser = null;
            List<Integer> instanceIdList = null;
            //获取所有基础设施下的实例Id
            if ((esParam.getSkipDataPermission() != null && esParam.getSkipDataPermission())) {
                if (CollectionUtils.isNotEmpty(modelIndexs) && CollectionUtils.isEmpty(esParam.getInstanceIds())) {
                    instanceIdList = mwModelViewDao.getInstanceIdByBase(modelIndexs);
                    esParam.setInstanceIds(instanceIdList);
                }
            } else {
                if (qParam.getUserId() != null) {
                    globalUser = userService.getGlobalUser(qParam.getUserId());
                } else {
                    globalUser = userService.getGlobalUser();
                }
                if (globalUser.isSystemUser()) {
                    //如果是超级管理员,则查询所有数据
                    if (CollectionUtils.isNotEmpty(modelIndexs)) {
                        instanceIdList = mwModelViewDao.getInstanceIdByBase(modelIndexs);
                    }
                    if (CollectionUtils.isEmpty(esParam.getInstanceIds())) {
                        esParam.setInstanceIds(instanceIdList);
                    }
                } else {
                    List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
                    List<Integer> intIds = allTypeIdList.stream().filter(str -> str.matches("\\d+"))//过滤非数字的数据
                            .map(Integer::parseInt).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(esParam.getInstanceIds())) {
                        List<Integer> disInstanceList = esParam.getInstanceIds().stream().filter(item -> intIds.contains(item)).collect(Collectors.toList());
                        esParam.setInstanceIds(disInstanceList);
                    } else {
                        esParam.setInstanceIds(intIds);
                    }
                }
            }

            List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
            AddModelInstancePropertiesParam propertiesParam1 = new AddModelInstancePropertiesParam();
            propertiesParam1.setPropertiesIndexId(INSTANCE_ID_KEY);
            propertiesParam1.setPropertiesType(ModelPropertiesType.STRING.getCode());
            propertiesParamList.add(propertiesParam1);
            AddModelInstancePropertiesParam propertiesParam2 = new AddModelInstancePropertiesParam();
            propertiesParam2.setPropertiesIndexId(MwModelViewCommonService.INSTANCE_NAME_KEY);
            propertiesParam2.setPropertiesType(ModelPropertiesType.STRING.getCode());
            propertiesParamList.add(propertiesParam2);

            //将输入的查询实体类QueryModelAssetsParam转为
            //es对应查询的List<AddModelInstancePropertiesParam>
            long time2 = System.currentTimeMillis();
            paramMap.forEach((key, value) -> {
                if (value != null && value != "") {
                    String valueStr = value.toString();
                    PropertyInfo propertyInfo = propertyMap.get(key);
                    if (null != propertyInfo) {
                        //数据权限由intanceId条件控制
                        if (MwModelViewCommonService.DATA_PERMINSSION_KEY.contains(propertyInfo.getIndexId())) {
                            return;
                        }
                        AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
                        addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
                        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
                        if (type == ModelPropertiesType.DATE) {
                            if (value instanceof Map) {
                                Map valueMap = (Map) value;
                                Date valueObj = (Date) valueMap.get("startTime");
                                if (null != valueObj) {
                                    addModelInstancePropertiesParam.setStartTime(valueObj);
                                }

                                valueObj = (Date) valueMap.get("endTime");
                                if (null != valueObj) {
                                    addModelInstancePropertiesParam.setEndTime(valueObj);
                                }
                            }
                        }
                        addModelInstancePropertiesParam.setPropertiesValue(valueStr);
                        propertiesParamList.add(addModelInstancePropertiesParam);
                    }
                }
            });

            esParam.setPropertiesList(propertiesParamList);

            long time3 = 0l;
            if (CollectionUtils.isNotEmpty(esParam.getModelIndexs())) {
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

                //如果实例id数量太多,需要分组
                List<List<Integer>> instanceIdGroups = null;
                if (null != esParam.getInstanceIds()) {
                    instanceIdGroups = Lists.partition(esParam.getInstanceIds(), insBatchFetchNum);
                }

                //构造es查询条件
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                for (AddModelInstancePropertiesParam m : esParam.getPropertiesList()) {
                    //设置是否精准查询
                    m.setFilterQuery(qParam.isFilterQuery());
                    QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
//                    if (null != qb) {
//                        queryBuilder2.must(qb);
//                    }
                    //判断是否或查询
                    if (null != qb) {
                        if (qParam.getIsfuzzyQuery() != null && qParam.getIsfuzzyQuery()) {
                            queryBuilder2.should(qb);
                        } else {
                            queryBuilder2.must(qb);
                        }
                    }
                }
                queryBuilder.must(queryBuilder2);

                BoolQueryBuilder pQuery = null;
                time3 = System.currentTimeMillis();

                if (null != instanceIdGroups) {
                    int startRow = 0;
                    for (List<Integer> intancedIds : instanceIdGroups) {
                        pQuery = createCopy(queryBuilder);
                        QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
                        pQuery.must(queryBuilder1);
                        if (debug) {
                            log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
                        }
                        List<Map<String, Object>> ret = doEsSearch(pQuery
                                , startRow
                                , insBatchFetchNum
                                , esParam
                                , modelIndexs);

                        for (Map<String, Object> data : ret) {
                            setTypeName(data, modelInfoMap, assetTypeMap);
                        }

                        if (null != ret) {
                            listMap.addAll(ret);
                        }

                        startRow += insBatchFetchNum;
                    }
                }
            }
            long time4 = System.currentTimeMillis();


            newList = listMap;
            //是否查询资产状态
            if (mwInspectModeService.getInspectModeInfo() || (qParam.getIsQueryAssetsState() != null && qParam.getIsQueryAssetsState())) {
                newList = new ArrayList<>();
                newList = getAssetsStateByZabbix(listMap);
            }
            //过滤出只有web监测的数据
            List<Map<String, Object>> disListMap = newList.stream().filter(s -> webMonitorModeId.equals(intValueConvert(s.get(ASSETTYPE_SUB_ID)))).collect(Collectors.toList());
            List<Map<String, Object>> webMonitorList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(disListMap)) {
                newList.removeAll(disListMap);
                webMonitorList = disListMap;
                //进入web监测查询接口
                List<MwModelWebMonitorTable> webMonitorParams = MwModelUtils.convertEsData(MwModelWebMonitorTable.class, webMonitorList);
                List<MwModelWebMonitorTable> webMonitorTables = mwModelWebMonitorService.queryWebSeverList(webMonitorParams);
                List<Map<String, Object>> newListMap = ListMapObjUtils.convertList(webMonitorTables);
                for (Map<String, Object> ms : newListMap) {
                    String status = "";
                    if (NORMAL.equals(strValueConvert(ms.get(WEB_STATE)))) {
                        status = "NORMAL";
                    }
                    if (ABNORMAL.equals(strValueConvert(ms.get(WEB_STATE)))) {
                        status = "ABNORMAL";
                    }
                    ms.put(ITEM_ASSETS_STATUS, status);
                    //web监测默认监控状态为true
                    ms.put(MONITOR_FLAG, true);
                }
                newList.addAll(newListMap);
            }
            long time5 = System.currentTimeMillis();
            log.info("公共接口查询资产耗时getModelListInfoByPerm::" + qParam.getIsQueryAssetsState() + "time2:" + (time2 - time1) + "ms;time3:" + (time3 - time2) + "ms;time4:" + (time4 - time3) + "ms;time5:" + (time5 - time4) + "ms;总耗时:" + (time5 - time1) + "ms;");
        } catch (Exception e) {
            log.error("fail to getModelListInfoByPerm param{}, case by {}", qParam, e);
        }
        return newList;
    }

    private void setTypeName(Map<String, Object> data
            , Map<Integer, ModelInfo> modelInfoMap, Map<String, AssetTypeIconDTO> assetTypeMap) {
        //2023-04-04 modelId会存在String和Integer两个类型，导致cast异常
        if (null == data.get(MODEL_ID_KEY)) {
            return;
        }

        Integer modelId = intValueConvert(data.get(MODEL_ID_KEY));
        ModelInfo modelInfo = modelInfoMap.get(modelId);
        if (null != modelInfo && StringUtils.isNotEmpty(modelInfo.getModelName())) {
            data.put(ASSETSUBTYPE_NAME, modelInfo.getModelName());
        }

        String typeId = strValueConvert(data.get(ASSETTYPE_ID_KEY));
        AssetTypeIconDTO assetTypeIconDTO = assetTypeMap.get(typeId);
        if (null != assetTypeIconDTO) {
            data.put(ASSETTYPE_NAME, assetTypeIconDTO.getName());
        }
    }

    @Override
    public List<Map<String, Object>> getModelListInfoByCommonQuery(QueryModelParam queryModelParam
            , QueryInstanceModelParam esParam) {

        List<Map<String, Object>> listMap = new ArrayList<>();

        try {
            //获取所有基础模型的索引
            List<String> modelIndexs = null;

            if (null != esParam) {
                modelIndexs = esParam.getModelIndexs();
            }

            //获取所有模型分组
            List<AssetTypeIconDTO> assetTypeIconDTOS = mwModelViewDao.selectAllAssetsTypeIcon();
            Map<String, AssetTypeIconDTO> assetTypeMap = new HashMap<>();
            for (AssetTypeIconDTO assetTypeIconDTO : assetTypeIconDTOS) {
                assetTypeMap.put(assetTypeIconDTO.getId().toString(), assetTypeIconDTO);
            }

            //获取所有基础模型的公共属性
            Map<Integer, ModelInfo> modelInfoMap = new HashMap<>();

            if (null == modelIndexs) {
                modelIndexs = new ArrayList<>();
                List<ModelInfo> modelInfoList = mwModelManageDao.getBaseModelInfos();
                if (null != modelInfoList) {
                    for (ModelInfo modelInfo : modelInfoList) {
                        modelInfoMap.put(modelInfo.getModelId(), modelInfo);
                        modelIndexs.add(modelInfo.getModelIndex());
                    }
                }
            }

            //如果实例id数量太多,需要分组
            List<List<Integer>> instanceIdGroups = null;
            if (null != esParam && null != esParam.getInstanceIds()) {
                instanceIdGroups = Lists.partition(esParam.getInstanceIds(), insBatchFetchNum);
            }

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

            //生成查询条件
            if (null != queryModelParam) {
                ModelQuery modelQuery = ModelQueryFactory.genModelQuery(queryModelParam);
                if (null != modelQuery) {
                    QueryBuilder condition = modelQuery.genQuery();
                    queryBuilder.must(condition);
                }
            }

            BoolQueryBuilder pQuery = null;

            //如果传入了实例查询的范围,则进行分组查询
            if (null != instanceIdGroups) {
                int startRow = 0;
                for (List<Integer> intancedIds : instanceIdGroups) {
                    pQuery = createCopy(queryBuilder);
                    QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
                    pQuery.must(queryBuilder1);
                    if (debug) {
                        log.info("es query:{}", pQuery.toString());
                    }

                    pQuery.must(queryBuilder1);
                    List<Map<String, Object>> ret = doEsSearch(pQuery
                            , startRow
                            , insBatchFetchNum
                            , esParam
                            , modelIndexs);

                    if (null != ret) {
                        listMap.addAll(ret);
                    }

                    startRow += insBatchFetchNum;
                }
            } else {
                if (debug) {
                    log.info("es query:{}", queryBuilder.toString());
                }
                List<Map<String, Object>> ret = doEsSearch(queryBuilder
                        , 0
                        , -1
                        , esParam
                        , modelIndexs);

                for (Map<String, Object> data : ret) {
                    setTypeName(data, modelInfoMap, assetTypeMap);
                }

                if (null != ret) {
                    listMap.addAll(ret);
                }
            }
        } catch (Exception e) {
            log.error("getModelListInfoByCommonQuery all {}", e);
        }

        return listMap;
    }

    @Override
    public <T> List<T> getModelListInfoByCommonQuery(Class<T> type, QueryModelParam queryModelParam, QueryInstanceModelParam param) {
        List<T> ret = new ArrayList<>();
        try {
            List<Map<String, Object>> listMap = getModelListInfoByCommonQuery(queryModelParam, param);
            ret = MwModelUtils.convertEsData(type, listMap);
        } catch (Exception e) {
            log.error("getModelListInfoByCommonQuery type {}", e);
        }
        return ret;
    }

    protected BoolQueryBuilder createCopy(BoolQueryBuilder oldQuery) {
        BoolQueryBuilder builder = new BoolQueryBuilder();
        oldQuery.must().forEach(builder::must);
        oldQuery.mustNot().forEach(builder::mustNot);
        oldQuery.filter().forEach(builder::filter);
        oldQuery.should().forEach(builder::should);
        builder.minimumShouldMatch(oldQuery.minimumShouldMatch());
        builder.adjustPureNegative(oldQuery.adjustPureNegative());
        return builder;
    }

    protected List<Map<String, Object>> doEsSearch(BoolQueryBuilder queryBuilder, int startFrom, int size
            , QueryInstanceModelParam param, List<String> modelIndexs) throws Exception {

        List<Map<String, Object>> ret = new ArrayList<>();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (startFrom >= 0 && size > 0) {
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(size);
        } else {
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(pageSize);
        }

        //设置超时时间
        searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
        searchSourceBuilder.query(queryBuilder);
//                        searchSourceBuilder.query(queryBuilder).query(QueryBuilders.idsQuery().addIds("mw_162243886299458af294ff91f4ef98a57274"));;
        //排序
        //字符串的排序需要使用不分词类型
        if (param.getSortFieldType() != null && param.getSortField() != null) {
            if (arrList.contains(param.getSortFieldType())) {
                searchSourceBuilder.sort(param.getSortField() + (".keyword"), (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
            } else {
                searchSourceBuilder.sort(param.getSortField(), (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
            }
        }
        //返回指定字段数据
        if (CollectionUtils.isNotEmpty(param.getFieldList())) {
            String[] includes = param.getFieldList().toArray(new String[param.getFieldList().size()]);
            FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
            searchSourceBuilder.fetchSource(sourceContext);
        }
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(String.join(",", modelIndexs));
//                searchRequest.indices(param.getModelIndex());
        SearchResponse search = null;

        search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit searchHit : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put("esId", searchHit.getId());
            ret.add(sourceAsMap);
        }
        return ret;
    }

    protected SearchResponse doEsSearchResponse(BoolQueryBuilder queryBuilder, int startFrom, int endFrom, QueryInstanceModelParam param, List<String> modelIndexs) throws Exception {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(startFrom);
        searchSourceBuilder.size(endFrom);
        //设置超时时间
        searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
        searchSourceBuilder.query(queryBuilder);
        //排序
        //字符串的排序需要使用不分词类型
        if (param.getSortFieldType() != null && param.getSortField() != null) {
            if (arrList.contains(param.getSortFieldType())) {
                searchSourceBuilder.sort(param.getSortField() + (".keyword"), (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
            } else {
                searchSourceBuilder.sort(param.getSortField(), (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
            }
        }else{
            Script script = new Script( ScriptType.INLINE, "painless", "if (doc.containsKey('createDate.keyword')) { return doc['createDate.keyword'].value } else { return '' }", Collections.emptyMap());
            searchSourceBuilder.sort(new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.STRING).order(SortOrder.ASC));
        }

        //返回指定字段数据
        if (CollectionUtils.isNotEmpty(param.getFieldList())) {
            String[] includes = param.getFieldList().toArray(new String[param.getFieldList().size()]);
            FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
            searchSourceBuilder.fetchSource(sourceContext);
        }
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(String.join(",", modelIndexs));
        SearchResponse search = null;
        search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return search;
    }

    /**
     * 获取资产视图列表数据
     *
     * @return
     */
    @Override
    public Reply getModelListInfoByView(QueryInstanceModelParam param) {
        long time3 = 0l;
        long time4 = 0l;
        long time5 = 0l;
        try {
            long time1 = System.currentTimeMillis();
            getInstanceListData(param);
            Map<String, Object> map = getModelListInfoByBase(param);
            long sum = 0l;
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }
            if (map != null && map.get("sum") != null) {
                sum = (long) map.get("sum");
            }

            long time12 = System.currentTimeMillis();
            //机构，用户组，负责人字段转换
            mwModelCommonServiceImpl.powerFieldConvert(param.getSkipDataPermission(), listMap);
            long time13 = System.currentTimeMillis();
            //外部关联类型转换（id转为名称）
            if (param.isConvertVal()) {
                mwModelCommonServiceImpl.relationFieldConvert(listMap);
            }
            //监控服务关联类型字段值转换
            mwModelCommonServiceImpl.monitorServerRelationConvert(listMap);

            long time2 = System.currentTimeMillis();
            //过滤，没有资产id和服务器id的实例 ，不进行zabbix查询获取健康状态值
            List<Map<String, Object>> newList = new ArrayList<>();
            newList = listMap;
            if (!mwInspectModeService.getInspectModeInfo()) {
                newList = new ArrayList<>();
                newList = getAssetsStateByZabbix(listMap);
            }
            time3 = System.currentTimeMillis();

            //如果需要查看接口数据，则进行处理
            if (param.getNetFlowInterface() == 1) {
                updateAssetsInterface(newList);
            }

            time4 = System.currentTimeMillis();
            log.info("资产视图getModelListInfoByView查询es数据耗时2:" + (time2 - time1) + "ms;数量:" + newList.size() + ";查询资产转态耗时:" + (time3 - time2) + "ms;查看接口数据:" + (time4 - time3) + "ms;总耗时:" + (time4 - time1) + "ms;");
            PageInfo pageInfo = new PageInfo<>(newList);
            pageInfo.setList(newList);
            pageInfo.setTotal(sum);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectModelInstance param{}, case by {}", param, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_SELECT_CODE_313005, ErrorConstant.MODEL_INSTANCE_SELECT_MSG_313005);
        }
    }

    @Override
    public List<MwInstanceCommonParam> getNameListByIds(List<Integer> ids) {
        List<MwInstanceCommonParam> instanceNameListByIds = mwModelInstanceDao.getInstanceNameListByIds(ids);
        return instanceNameListByIds;
    }

    /**
     * 更新资源中心资产接口
     *
     * @param listMap 资产数据
     */
    private void updateAssetsInterface(List<Map<String, Object>> listMap) {
        List<QueryAssetsInterfaceParam> list;
        for (Map<String, Object> objectMap : listMap) {
            try {
                String id = strValueConvert(objectMap.get("modelInstanceId"));
                QueryAssetsInterfaceParam queryParam = new QueryAssetsInterfaceParam();
                queryParam.setAssetsId(id);
                Reply reply = mwAssetsInterfaceService.getAllInterfaces(queryParam);
                if (null != reply && PaasConstant.RES_SUCCESS.equals(reply.getRes())) {
                    list = (List<QueryAssetsInterfaceParam>) reply.getData();
                    objectMap.put("interfaceList", list);
                }
            } catch (Exception e) {
                log.error("更新资源中心资产接口失败", e);
            }
        }
    }

    public List<Map<String, Object>> getAssetsStateByZabbix(List<Map<String, Object>> listMap) {
        long time3 = 0l;
        long time4 = 0l;
        long time5 = 0l;
        //过滤，没有资产id和服务器id的实例 且是web监测的，不进行zabbix查询获取健康状态值
        List<Map<String, Object>> disMap = listMap.stream().filter(s -> s.get(ASSETS_ID) != null && !s.get(ASSETS_ID).toString().equals("")
                && s.get(MONITOR_SERVER_ID) != null && !"".equals(s.get(MONITOR_SERVER_ID).toString()) && !"0".equals(s.get(MONITOR_SERVER_ID).toString())
                && !webMonitorModeId.equals(intValueConvert(s.get(ASSETTYPE_SUB_ID)))).collect(Collectors.toList());
        long time2 = System.currentTimeMillis();
        if (disMap != null && disMap.size() > 0) {
            Map<String, String> statusMap = new HashMap<>();
            //根据资产查询告警信息
           List<QueryAlertStateParam> paramList = new ArrayList<>();
            for(Map<String, Object> m : disMap){
                QueryAlertStateParam alertStateParam = new QueryAlertStateParam();
                alertStateParam.setHostid(strValueConvert(m.get(ASSETS_ID)));
                alertStateParam.setInstanceId(strValueConvert(m.get(INSTANCE_ID_KEY)));
                alertStateParam.setMonitorServerId(intValueConvert(m.get(MONITOR_SERVER_ID)));
                paramList.add(alertStateParam);
            }
            Reply reply = mwAlertService.getIsAlert(paramList);
            Map<String, Boolean> alertMap = new HashMap<>();
            if(reply!=null && reply.getRes() == PaasConstant.RES_SUCCESS){
                List<QueryAlertStateParam> alertList =  (List<QueryAlertStateParam>)reply.getData();
                alertMap = alertList.stream().collect(Collectors.toMap(s -> s.getInstanceId(), s -> booleanValueConvert(s.getIsAlert())));
            }

            Map<Integer, List<String>> groupMap = disMap.stream()
                    .collect(Collectors.groupingBy(s -> intValueConvert(s.get(MONITOR_SERVER_ID).toString()), Collectors.mapping(s -> s.get(ASSETS_ID).toString(), Collectors.toList())));

            //使用多线程处理多个zabbix服务的情况
            int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
            coreSizePool = (coreSizePool < groupMap.size()) ? coreSizePool : groupMap.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, groupMap.size(), 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            List<Map<String, String>> listInfo = new ArrayList();
            List<Future<Map<String, String>>> futureList = new ArrayList<>();
            //keys为serverId，value为assetsId
            groupMap.forEach((keys, value) -> {
                Callable<Map<String, String>> callable = new Callable<Map<String, String>>() {
                    @Override
                    public Map<String, String> call() throws Exception {
                        Set<String> hostSets = new HashSet<>();
                        List<String> disList = value.stream().distinct().collect(Collectors.toList());
                        long time22 = System.currentTimeMillis();
                        log.info("时间1：" + time22);
                        MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(keys, ZabbixItemConstant.NEW_ASSETS_STATUS, disList);
                        long time23 = System.currentTimeMillis();
                        log.info("时间2：" + time23);
                        log.info("资产视图查询zabbix状态耗时:" + (time23 - time22) + "ms");
                        if (statusData != null && !statusData.isFail()) {
                            JsonNode jsonNode = (JsonNode) statusData.getData();
                            if (jsonNode.size() > 0) {
                                for (JsonNode node : jsonNode) {
                                    Integer lastvalue = node.get("lastvalue").asInt();
                                    String hostId = node.get("hostid").asText();
                                    String name = node.get("name").asText();
                                    if ((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)) {
                                        String status = (lastvalue == 0) ? AssetsStatusEnum.ABNORMAL.name() : AssetsStatusEnum.NORMAL.name();
                                        statusMap.put(keys + ":" + hostId, status);
                                        hostSets.add(hostId);
                                    }
                                    if (hostSets.contains(hostId)) {
                                        continue;
                                    }
                                    String status = (lastvalue == 0) ? AssetsStatusEnum.ABNORMAL.name() : AssetsStatusEnum.NORMAL.name();
                                    statusMap.put(keys + ":" + hostId, status);
                                }
                            }
                        }
                        return statusMap;
                    }
                };
                Future<Map<String, String>> submit = executorService.submit(callable);
                futureList.add(submit);
            });
            if (futureList.size() > 0) {
                futureList.forEach(f -> {
                    try {
                        Map<String, String> result = f.get(20, TimeUnit.SECONDS);
                        listInfo.add(result);
                    } catch (Exception e) {
                        log.error("fail to getDataInfoBydeviceName:多线程等待数据返回失败cause:{}", e);
                    }
                });
            }
            executorService.shutdown();
            log.info("关闭线程池");

            time3 = System.currentTimeMillis();
            Map<String, String> mapAll = new HashMap();
            for (Map<String, String> m : listInfo) {
                mapAll.putAll(m);
            }
            time4 = System.currentTimeMillis();
            String status = "";
            for (Map<String, Object> m : listMap) {
                Integer monitorServerId = intValueConvert(m.get(MONITOR_SERVER_ID));
                String assetsId = m.get(ASSETS_ID) != null ? m.get(ASSETS_ID).toString() : "";
                Boolean monitorFlag = m.get(MONITOR_FLAG) != null ? Boolean.valueOf(m.get(MONITOR_FLAG).toString()) : null;
                if (monitorFlag != null && monitorFlag) {
                    String s = mapAll.get(monitorServerId + ":" + assetsId);
                    if (s != null && StringUtils.isNotEmpty(s)) {
                        status = s;
                    } else {
                        status = AssetsStatusEnum.UNKNOWN.name();
                    }
                } else if (monitorFlag != null && !monitorFlag) {
                    status = AssetsStatusEnum.UNKNOWN.name();
                } else {
                    status = AssetsStatusEnum.UNKNOWN.name();
                }
                if(alertMap!=null && alertMap.containsKey(strValueConvert(m.get(INSTANCE_ID_KEY)))){
                    Boolean aBoolean = alertMap.get(strValueConvert(m.get(INSTANCE_ID_KEY)));
                    //有告警，则资产状态为告警
                    if(aBoolean!=null && aBoolean){
                        status = AssetsStatusEnum.ALERT.name();
                    }
                }
                m.put(ITEM_ASSETS_STATUS, status);
            }
            List<Map<String, Object>> newList = new ArrayList<>();
            newList = listMap;
            //领导检查模型，过滤掉所有异常状态
            if (mwInspectModeService.getInspectModeInfo()) {
                listMap = newList.stream().filter(s -> (s.get(MONITOR_FLAG) != null && Boolean.valueOf(s.get(MONITOR_FLAG).toString())) && s.get(ITEM_ASSETS_STATUS) != null && "NORMAL".equals(s.get(ITEM_ASSETS_STATUS).toString())).collect(Collectors.toList());
            } else {
                listMap = newList;
            }
            time5 = System.currentTimeMillis();
            log.error("查询资产状态耗时：" + (time5 - time2) + "ms");
        }
        return listMap;
    }


    @Override
    public Reply getViewByFuzzyQuery() {
        try {
            QueryInstanceModelParam param = new QueryInstanceModelParam();
            param.setPageSize(pageSize);
            getInstanceListData(param);
            long time1 = System.currentTimeMillis();
            Map<String, Object> infoMap = getModelListInfoByBase(param);
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (infoMap != null && infoMap.get("data") != null) {
                listMap = (List<Map<String, Object>>) infoMap.get("data");
            }
            long time2 = System.currentTimeMillis();
            //获取当前登录用户ID
            Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
            List<ModelInfo> modelInfoList = mwModelManageDao.getBaseModelInfos();
            Map<String, PropertyInfo> propertyMap = new HashMap<>();
            if (null != modelInfoList) {
                for (ModelInfo modelInfo : modelInfoList) {
                    if (null != modelInfo.getPropertyInfos()) {
                        for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                            propertyMap.put(propertyInfo.getIndexId(), propertyInfo);
                        }
                    }
                }
            }
            //模糊提示显示的字段
            List<String> fuzzyField = Arrays.asList("instanceName", "specifications", "manufacturer");
            //获取es实例数据中共用的模型属性
            List<MwCustomColByModelDTO> customColList = mwModelViewDao.getPropertiesIndexByCommon(userId, parentModelIds);
            //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
            List<String> datas = new ArrayList<>();
            for (Map<String, Object> data : listMap) {
                Map map = new HashMap();
                for (Map.Entry<String, Object> e : data.entrySet()) {
                    if (fuzzyField.contains(e.getKey()) && e.getValue() != null) {
                        datas.add(e.getValue().toString());
                    }
                }
            }
            datas = datas.stream().distinct().collect(Collectors.toList());
            long time3 = System.currentTimeMillis();
            log.info("资产视图模糊提示接口耗时:查询:time2:" + (time2 - time1) + "ms;处理:time3" + (time3 - time2) + "ms");
            return Reply.ok(datas);
        } catch (Exception e) {
            log.error("fail to getViewByFuzzyQuery case by {}", e);
            return Reply.fail(500, "资产视图模糊查询提示失败");
        }
    }

    @Override
    public Reply getAuthInfoByModel(MwModelMacrosValInfoParam param) {
        try {
            List<MwModelMacrosValInfoParam> macrosInfo = mwModelViewDao.getMacrosFieldByModel(param.getModelId());

            //查询同步的agent
            Reply reply = mwEngineManageService.selectDropdownList(0, true, false);
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                MwModelMacrosValInfoParam macrosValInfoParam = macrosInfo.get(0);

                List<SelectCharDropDto> selectCharDropDtos = (List) reply.getData();
                MwModelMacrosValInfoParam mwModelMacrosValInfoParam = new MwModelMacrosValInfoParam();
                mwModelMacrosValInfoParam.extractFrom(selectCharDropDtos);
                mwModelMacrosValInfoParam.setModelId(macrosValInfoParam.getModelId());
                macrosInfo.add(mwModelMacrosValInfoParam);
            }

            return Reply.ok(macrosInfo);
        } catch (Exception e) {
            log.error("fail to getAuthInfoByModel case by {}", e);
            return Reply.fail(500, "获取模型宏值失败");
        }
    }

    @Override
    public Reply saveAuthInfoByModel(List<MwModelMacrosValInfoParam> paramList) {
        try {
            if (CollectionUtils.isNotEmpty(paramList)) {
                mwModelViewDao.saveMacroValAuthName(paramList);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to saveAuthInfoByModel case by {}", e);
            return Reply.fail(500, "保存同步凭证成功");
        }
    }

    /**
     * 查询指定字段的模型实例
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> getWhetherExistsFieldModelInfo(QueryESWhetherExistField queryParam) {
        QueryInstanceModelParam param = new QueryInstanceModelParam();
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            param.setInstanceIds(queryParam.getInstanceIds());
            if (queryParam.getIsBaseData() == null) {
                queryParam.setIsBaseData(true);
            }
            //获取指定的模型Index和实例Id
            Set<String> modelIndexSet = new HashSet<>();
            List<Integer> instanceIdList = new ArrayList<>();
            if (CollectionUtils.isEmpty(param.getModelIndexs())) {
                List<ModelInstanceBaseInfoDTO> lists = mwModelViewDao.getModelIndexANDInstanceInfo(queryParam.getIsBaseData());
                for (ModelInstanceBaseInfoDTO aParam : lists) {
                    modelIndexSet.add(aParam.getModelIndex());
                    instanceIdList.add(aParam.getInstanceId());
                }
                param.setModelIndexs(new ArrayList<>(modelIndexSet));
            }
            if (CollectionUtils.isEmpty(param.getInstanceIds())) {
                param.setInstanceIds(instanceIdList);
            }
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                //对modelInstanceId进行多值查询
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                //如果实例id数量太多,需要分组
                List<List<Integer>> instanceIdGroups = null;
                if (null != param.getInstanceIds()) {
                    instanceIdGroups = Lists.partition(param.getInstanceIds(), insBatchFetchNum);
                }
                if (queryParam.getNotExistFields() != null && queryParam.getNotExistFields().size() > 0) {
                    //指定字段不存在的
                    for (String noExistsField : queryParam.getNotExistFields()) {
                        queryBuilder.mustNot(QueryBuilders.existsQuery(noExistsField));
                    }
                }
                //存在的指定字段
                if (queryParam.getExistFields() != null && queryParam.getExistFields().size() > 0) {
                    for (String existsField : queryParam.getExistFields()) {
                        queryBuilder.must(QueryBuilders.existsQuery(existsField));
                    }
                }
                BoolQueryBuilder pQuery = null;
                if (null != instanceIdGroups) {
                    int startRow = 0;
                    for (List<Integer> intancedIds : instanceIdGroups) {
                        pQuery = createCopy(queryBuilder);
                        QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
                        pQuery.must(queryBuilder1);
                        if (debug) {
                            log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
                        }
                        List<Map<String, Object>> ret = doEsSearch(pQuery
                                , startRow
                                , insBatchFetchNum
                                , param
                                , param.getModelIndexs());

                        if (null != ret) {
                            listMap.addAll(ret);
                        }
                    }
                }
            }
            return listMap;
        } catch (Exception e) {
            log.error("fail to getNoFieldModelInfo param{}, case by {}", param, e);
            return listMap;
        }
    }


    /**
     * 资产设施模型新增资产类型字段
     *
     * @return
     */
    @Override
    public Reply addModelFieldGroupNodesByBase() {
        QueryESWhetherExistField queryParam = new QueryESWhetherExistField();
        queryParam.setNotExistFields(Arrays.asList("groupNodes"));
        List<Map<String, Object>> listMap = getWhetherExistsFieldModelInfo(queryParam);
        List<MwModelManageDTO> groupNodeList = mwModelManageDao.getModelGroupNodes();
        try {
            Map<String, String> groupNodeMap = new HashMap();
            for (MwModelManageDTO manageDTO : groupNodeList) {
                groupNodeMap.put(manageDTO.getModelIndex(), manageDTO.getGroupNodes());
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (Map<String, Object> m : listMap) {
                String groupNodes = groupNodeMap.get(m.get("modelIndex").toString());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("groupNodes", groupNodes);
                bulkRequest.add(new UpdateRequest(m.get("modelIndex").toString(), m.get("esId").toString()).doc(hashMap).upsert());
            }
            if (listMap.size() > 0) {
                bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            }
            return Reply.ok("模型新增groupNodes字段成功");
        } catch (Exception e) {
            log.error("fail to addModelFieldGroupNodesByBase, case by {}", e);
            return Reply.fail(500, "模型新增groupNodes字段失败");
        }
    }

    /**
     * 在mysql中改动数据的modelId和分组id时，需要同步到es中
     *
     * @return
     */
    @Override
    public Reply editorModelFieldToEs() {
        List<MwModelInstanceCommonParam> modelInfoList = mwModelManageDao.getAllModelInstanceInfo();
        BulkRequest request = new BulkRequest();
        try {
            for (MwModelInstanceCommonParam instanceCommonParam : modelInfoList) {
                if (!Strings.isNullOrEmpty(instanceCommonParam.getModelIndex()) && !Strings.isNullOrEmpty(strValueConvert(instanceCommonParam.getModelInstanceId()))) {
                    String esId = instanceCommonParam.getModelIndex() + instanceCommonParam.getModelInstanceId();
                    UpdateRequest updateRequest = new UpdateRequest(instanceCommonParam.getModelIndex(), esId);
                    updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("modelId", instanceCommonParam.getModelId());
                    jsonMap.put("assetsTypeSubId", strValueConvert(instanceCommonParam.getModelId()));
                    jsonMap.put("assetsTypeId", instanceCommonParam.getGroupId());
                    jsonMap.put("groupNodes", instanceCommonParam.getGroupNodes());
                    updateRequest.doc(jsonMap);
                    request.add(updateRequest);
                }
            }
            restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        log.error("异步执行批量添加模型insertModelInstanceProperties属性到es中失败");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("异步执行批量添加模型属性到es中失败", e);
                }
            });
            return Reply.ok();
        } catch (Throwable e) {
            log.error("异步执行批量添加模型属性到es中失败{}", e);
            return Reply.fail(500, "修改数据失败");
        }
    }

    @Override
    public Reply addModelFieldAssetsTypeIdByBase() {
        QueryESWhetherExistField queryParam = new QueryESWhetherExistField();
        queryParam.setNotExistFields(Arrays.asList("assetsTypeSubId"));
        List<Map<String, Object>> listMap = getWhetherExistsFieldModelInfo(queryParam);
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (Map<String, Object> m : listMap) {
                String groupNodes = m.get("groupNodes").toString();
                String groupNodeId = "";
                if (groupNodes.split(",").length > 1) {
                    groupNodeId = groupNodes.split(",")[groupNodes.split(",").length - 1];
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("assetsTypeSubId", m.get("modelId").toString());
                hashMap.put("assetsTypeId", groupNodeId);
                bulkRequest.add(new UpdateRequest(m.get("modelIndex").toString(), m.get("esId").toString()).doc(hashMap).upsert());
            }
            if (listMap.size() > 0) {
                bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            }
            return Reply.ok("资产设施模型新增资产类型字段成功");
        } catch (Exception e) {
            log.error("fail to addModelFieldAssetsTypeIdByBase, case by {}", e);
            return Reply.fail(500, "资产设施模型新增资产类型字段失败");
        }
    }

    @Override
    public Reply exportForExcel(QueryInstanceModelParam param, HttpServletRequest request, HttpServletResponse
            response) {
        try {
            List<String> lable = param.getHeader();
            List<String> lableName = param.getHeaderName();
            Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
            //获取模型下的属性类型
            List<AddModelInstancePropertiesParam> propertiesNameList;
            Integer modelId = param.getModelId();
            param.setModelId(modelId);
            param.setPageSize(pageSize);
            boolean bool = false;
            //根据树结构groupNodes:11或assetsTypeSubId:72，表示web监测模型，单独处理
            if (CollectionUtils.isNotEmpty(param.getPropertiesList())) {
                bool = param.getPropertiesList().stream().anyMatch(a -> (ASSETTYPE_SUB_ID.equals(a.getPropertiesIndexId()) && strValueConvert(webMonitorModeId).equals(a.getPropertiesValue()))
                        || (GROUP_NODES.equals(a.getPropertiesIndexId()) && webMonitorGroupId.equals(a.getPropertiesValue())));
            }

            //前端页面勾选中的资产实例Id
            if (CollectionUtils.isNotEmpty(param.getInstanceIdSelectedList())) {
                List<Integer> instanceIds = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                    instanceIds = param.getInstanceIds();
                }
                instanceIds.addAll(param.getInstanceIdSelectedList());
                param.setInstanceIds(instanceIds);
            }
            getInstanceListData(param);
            if (!bool) {//web监测查询时，不设置指定返回字段，否则按照导出字段返回
                List<String> field = new ArrayList<>(lable);
                field.add(INSTANCE_ID_KEY);
                param.setFieldList(field);
            }

            Map<String, Object> maps = getModelListInfoByBase(param);
            List<String> relationIndexIds = new ArrayList<>();
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (maps != null && maps.get("data") != null) {
                listMap = (List<Map<String, Object>>) maps.get("data");
            }
            //根据树结构groupNodes:11或assetsTypeSubId:72，表示web监测模型，单独处理

            if (bool) {
                //进入web监测查询接口
                List<MwModelWebMonitorTable> webMonitorParams = null;

                webMonitorParams = MwModelUtils.convertEsData(MwModelWebMonitorTable.class, listMap);

                List<MwModelWebMonitorTable> webMonitorTables = mwModelWebMonitorService.queryWebSeverList(webMonitorParams);
                List<Map<String, Object>> newListMap = ListMapObjUtils.convertList(webMonitorTables);
                Map<String, Map<String, Object>> groupMap = newListMap.stream().collect(Collectors.toMap(s -> s.get(INSTANCE_ID_KEY).toString(), s -> s));
                for (Map<String, Object> mapParam : listMap) {
                    String instanceId = mapParam.get(INSTANCE_ID_KEY).toString();
                    if (groupMap.containsKey(instanceId)) {
                        mapParam.putAll(groupMap.get(instanceId));
                    }
                }
            }
            //获取所有模型属性
            List<PropertyInfo> allPropertyList = getAllPropertyInfo();
            //将属性index和type存入map中，方便取值
            Map map = new HashMap();
            for (PropertyInfo aParam : allPropertyList) {
                map.put(aParam.getIndexId(), aParam.getPropertiesTypeId());
            }

            //资产视图下的导出
            relationIndexIds = allPropertyList.stream().filter(s -> lable.contains(s.getIndexId()) && intValueConvert(s.getPropertiesTypeId()) == 5).map(PropertyInfo::getIndexId).collect(Collectors.toList());

            //模型分组下的导出
            if (param.getIsAssetsView() != null && !param.getIsAssetsView()) {
                map = new HashMap();
                List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListWithParent(param.getModelId());
                List<PropertyInfo> propertyInfoList = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfoList) {
                    if (null != modelInfo.getPropertyInfos()) {
                        propertyInfoList.addAll(modelInfo.getPropertyInfos());
                    }
                }
                for (PropertyInfo m : propertyInfoList) {
                    map.put(m.getIndexId(), m.getPropertiesTypeId());
                }
                relationIndexIds = propertyInfoList.stream().filter(s -> lable.contains(s.getIndexId()) && s.getPropertiesTypeId().intValue() == 5 || s.getPropertiesTypeId().intValue() == 4).map(PropertyInfo::getIndexId).collect(Collectors.toList());
            }
            if (lable.contains(RELATIONSITEROOM.getField()) || lable.contains(POSITIONBYROOM.getField())
                    || lable.contains(RELATIONSITECABINET.getField()) || lable.contains(POSITIONBYCABINET.getField())) {
                //设置转换机房机柜数据
                mwModelInstanceServiceImplV1.addRoomAndCabinetInfo(listMap);
            }
            Set<Integer> relatioinInstanceIds = new HashSet<>();
            //循环获取所有外部关联的实例Id
            for (String relationIndexId : relationIndexIds) {
                for (Map<String, Object> m : listMap) {
                    if (m.get(relationIndexId) != null && !Strings.isNullOrEmpty(m.get(relationIndexId).toString())) {
                        boolean isNum = pattern.matcher(m.get(relationIndexId).toString()).matches();
                        if (isNum) {
                            relatioinInstanceIds.add(intValueConvert(m.get(relationIndexId).toString()));
                        }
                    }
                }
            }
            List<QueryInstanceParam> instanceParams = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(relatioinInstanceIds)) {
                List<List<Integer>> instanceIdGroups = new ArrayList<>();
                instanceIdGroups = Lists.partition(new ArrayList<>(relatioinInstanceIds), insBatchFetchNum);
                if (null != instanceIdGroups) {
                    for (List<Integer> instanceIdList : instanceIdGroups) {
                        instanceParams.addAll(mwModelInstanceDao.getInstanceNameByIds(instanceIdList));
                    }
                }
            }
            //将外部关联的实例Id作为key，name为value
            Map<String, String> relationMap = instanceParams.stream().collect(Collectors.toMap(s -> s.getModelInstanceId() != null ? s.getModelInstanceId().toString() : "0", s -> s.getInstanceName(), (
                    value1, value2) -> {
                return value2;
            }));

            //监控服务关联类型字段值转换
            mwModelCommonServiceImpl.monitorServerRelationConvert(listMap);
            //获取所有用户信息
            List<MwModelViewTreeDTO> orgNameAllList = mwModelExportDao.getOrgNameAllByExport();
            Map orgMap = new HashMap();
            for (MwModelViewTreeDTO dto : orgNameAllList) {
                orgMap.put(dto.getId(), dto.getName());
            }
            //获取所有用户信息
            List<MwModelViewTreeDTO> userNameAllList = mwModelExportDao.getUserNameAllByExport();
            Map userMap = new HashMap();
            for (MwModelViewTreeDTO dto : userNameAllList) {
                userMap.put(dto.getId(), dto.getName());
            }
            //获取所有用户组信息
            List<MwModelViewTreeDTO> groupNameAllList = mwModelExportDao.getGroupNameAllByExport();
            Map groupMap = new HashMap();
            for (MwModelViewTreeDTO dto : groupNameAllList) {
                groupMap.put(dto.getId(), dto.getName());
            }
            //格式转换 List<Map<String, Object>> -> List<Map>
            List<Map> mapList = new ArrayList<>();
            for (Map<String, Object> m : listMap) {
                //对es的数据循环匹配获取属性type，查询用户组，机构，用户的名称
                //es中存储的是该项的id值
                if (!m.containsKey(MONITOR_FLAG)) {//监控状态没有字段的，设置一个默认值
                    m.put(MONITOR_FLAG, false);
                }
                Map finalMap = map;
                m.forEach((k, v) -> {
                    //机构/部门
                    if (finalMap.get(k) != null) {
                        //外部关联(单选)类型
                        if (strValueConvert(SINGLE_RELATION.getCode()).equals(finalMap.get(k).toString())) {
                            String label = "";
                            if (v instanceof String) {
                                String code = v.toString();
                                label = relationMap.get(code);
                            }
                            m.put(k, label);
                        }
                        //外部关联(多选)类型
                        if (strValueConvert(MULTIPLE_RELATION.getCode()).equals(finalMap.get(k).toString())) {
                            List<String> list = (List) v;
                            String label = "";
                            for (String str : list) {
                                label += relationMap.get(str) + "/";
                            }
                            if (label.length() > 1) {
                                label = label.substring(0, label.length() - 1);
                            }
                            m.put(k, label);
                        }
                        //多选枚举型
                        if (strValueConvert(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(finalMap.get(k).toString())) {
                            List list = (List) v;
                            String enumNames = Joiner.on(",").join(list);
                            m.put(k, enumNames);
                        }
                        //机构
                        if (strValueConvert(ModelPropertiesType.ORG.getCode()).equals(finalMap.get(k).toString())) {
                            List<List> list = (List) v;
                            String orgNames = "";
                            for (int i = 0, len = list.size(); i < len; i++) {
                                List<Integer> list1 = list.get(i);
                                if (CollectionUtils.isNotEmpty(list1) && orgMap != null) {
                                    Integer orgId = list1.get(list1.size() - 1);
                                    if (orgMap.get(orgId + "") != null) {
                                        orgNames += orgMap.get(orgId + "") + "/";
                                    }
                                }
                            }
                            if (orgNames.length() > 0) {
                                orgNames = orgNames.substring(0, orgNames.length() - 1);
                            }
                            m.put(k, orgNames);
                        }
                        //负责人
                        if (strValueConvert(ModelPropertiesType.USER.getCode()).equals(finalMap.get(k).toString())) {
                            String userNames = "";
                            List<Integer> list = (List) v;
                            for (Integer userId : list) {
                                if (userMap != null && userMap.get(userId + "") != null) {
                                    userNames += userMap.get(userId + "") + "/";
                                }
                            }
                            if (userNames.length() > 0) {
                                userNames = userNames.substring(0, userNames.length() - 1);
                            }
                            m.put(k, userNames);
                        }
                        //用户组
                        if (strValueConvert(ModelPropertiesType.GROUP.getCode()).equals(finalMap.get(k).toString())) {
                            List<Integer> list = (List) v;
                            String groupNames = "";
                            for (Integer groupId : list) {
                                if (groupMap != null && groupMap.get(groupId + "") != null) {
                                    groupNames += groupMap.get(groupId + "") + "/";
                                }
                            }
                            if (groupNames.length() > 0) {
                                groupNames = groupNames.substring(0, groupNames.length() - 1);
                            }
                            m.put(k, groupNames);
                        }
                        //机房位置 机房数据格式为List
                        if (strValueConvert(ModelPropertiesType.LAYOUTDATA.getCode()).equals(finalMap.get(k).toString()) && POSITIONBYROOM.getField().equals(k)) {
                            String index = "";
                            if (v instanceof List && ((List) v).size() > 0) {
                                List list = (List) v;
                                if (list.size() > 1) {
                                    //行
                                    Integer row = intValueConvert(list.get(0));
                                    //列
                                    Integer col = intValueConvert(list.get(1));
                                    index = "第" + (row + 1) + "行第" + (col + 1) + "列";
                                }
                            }
                            m.put(k, index);
                        }
                        //机柜位置
                        if (strValueConvert(ModelPropertiesType.LAYOUTDATA.getCode()).equals(finalMap.get(k).toString()) && POSITIONBYCABINET.getField().equals(k)) {
                            String index = "";
                            boolean isBlade = true;
                            //机柜数据格式为Map
                            if (v instanceof Map && ((Map) v).size() > 0) {//处理华新脏数据
                                Object daoData = ((Map<String, Object>) v).get("daoData");
                                if (daoData instanceof Map || !(daoData instanceof List)) {
                                    isBlade = false;
                                    ((Map<String, Object>) v).put("daoData", new ArrayList<>());
                                }
                                CabinetLayoutDataParam layoutDataParam = JSONObject.parseObject(JSONObject.toJSONString(v), CabinetLayoutDataParam.class);
                                if (layoutDataParam != null) {
                                    int x = 0;
                                    List<Integer> indexList = new ArrayList<>();
                                    //刀片视图
                                    if (BLADE_VIEW.equals(strValueConvert(layoutDataParam.getType())) || isBlade) {
                                        List<List<QueryBladeInstanceParam>> assetsList = layoutDataParam.getDaoData();
                                        Integer startIndex = intValueConvert(layoutDataParam.getStart());
                                        Integer endIndex = intValueConvert(layoutDataParam.getEnd());
                                        if (endIndex > startIndex) {
                                            index = "第" + (startIndex + 1) + "-" + (endIndex + 1) + "层";
                                        } else {
                                            index = "第" + (startIndex + 1) + "层";
                                        }
                                        String instanceId = strValueConvert(m.get(INSTANCE_ID_KEY));
                                        for (List<QueryBladeInstanceParam> list1 : assetsList) {
                                            for (QueryBladeInstanceParam listParam : list1) {
                                                x++;
                                                if (listParam.getInstanceId().equals(instanceId)) {
                                                    indexList.add(x);
                                                }
                                            }
                                        }
                                        if (indexList.size() > 1) {
                                            index += "/Bay" + indexList.get(0) + "-" + indexList.get(indexList.size() - 1);
                                        } else {
                                            index += "/Bay" + indexList.get(0);
                                        }
                                    } else {//默认视图,机柜下属设备视图
                                        Integer startIndex = intValueConvert(layoutDataParam.getStart());
                                        Integer endIndex = intValueConvert(layoutDataParam.getEnd());
                                        if (endIndex > startIndex) {
                                            index = "第" + (startIndex + 1) + "-" + (endIndex + 1) + "层";
                                        } else {
                                            index = "第" + (startIndex + 1) + "层";
                                        }
                                    }
                                }

                            }
                            m.put(k, index);
                        }
                    }
                });
                mapList.add(m);
            }
            ExportExcel.exportExcel("模型实例列表导出", "模型实例列表导出", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
        } catch (Exception e) {
            log.error("exportForExcel{}", e);
        }
        return Reply.ok("导出成功");
    }

    /**
     * 原资产数据同步到es模型中
     *
     * @return
     */
    @SneakyThrows
    @Override
    @Transactional
    public Reply syncAssetsInfoToES() {
        //指定必须存在的字段
        List<String> existsField = Arrays.asList("hostName", "inBandIp", "assetsTypeSubId", "assetsId", "monitorServerId", "templateId");
        QueryESWhetherExistField queryParam = new QueryESWhetherExistField();
        queryParam.setExistFields(existsField);
        List<Map<String, Object>> instanceInfoList = getWhetherExistsFieldModelInfo(queryParam);
        //利用这些字段值，用来区分资产数据同步到es中是否重复。这些字段的值全部对应一致，表示数据已经存在了
        Map<String, List<Map<String, Object>>> groupByInstance = instanceInfoList.stream().collect(Collectors.groupingBy(item -> {
            return item.get(MwModelViewCommonService.INSTANCE_NAME_KEY) + "_" + item.get("hostName") + "_" + item.get("inBandIp")
                    + "_" + item.get("assetsTypeSubId") + "_" + item.get("assetsId")
                    + "_" + item.get("monitorServerId") + "_" + item.get("templateId");

        }));
        //获取所有资产数据
        List<MwModelTangibleAssetsDTO> modelTangibleAssets = mwModelViewDao.getAllTangibleAssetsInfo();
        //获取所有模型信息
        List<MwModelInfoDTO> allModelInfo = mwModelViewDao.getAllModelInfo();
        Map<String, MwModelInfoDTO> modelInfoMap = new HashMap();
        for (MwModelInfoDTO dto : allModelInfo) {
            modelInfoMap.put(dto.getModelId(), dto);
        }
        //获取资产对应的用户权限
        List<MwModelTangiblePermDTO> userList = mwModelViewDao.getAllUserPerInfoByAssets();
        Map<String, String> userMap = new HashMap();
        for (MwModelTangiblePermDTO dto : userList) {
            userMap.put(dto.getTypeId(), dto.getPerId());
        }
        //获取资产对应的机构权限
        List<MwModelTangiblePermDTO> orgList = mwModelViewDao.getAllOrgPerInfoByAssets();

        Map<String, List<MwModelTangiblePermDTO>> orgMap = orgList.stream().collect(Collectors.groupingBy(MwModelTangiblePermDTO::getTypeId));

        //获取资产对应的用户组权限
        List<MwModelTangiblePermDTO> groupList = mwModelViewDao.getAllGroupPerInfoByAssets();
        Map<String, String> groupMap = new HashMap();
        for (MwModelTangiblePermDTO dto : groupList) {
            groupMap.put(dto.getTypeId(), dto.getPerId());
        }

        List<AddModelInstancePropertiesParam> propertiesParamList = mwModelViewDao.getPropertiesNameByMoreModel(parentModelIds);
        List<AddAndUpdateModelInstanceParam> instanceParamList = new ArrayList<>();


        for (MwModelTangibleAssetsDTO dto : modelTangibleAssets) {
            List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
            if (groupByInstance != null && groupByInstance.size() > 0) {
                //利用指定的字段作为key，获取map中对应的值，如果该值存在，则表明这条数据es中已经存在，忽略
                String key = dto.getInstanceName() + "_" + dto.getHostName() + "_" + dto.getInBandIp() + "_" + dto.getAssetsTypeSubId()
                        + "_" + dto.getAssetsId() + "_" + dto.getMonitorServerId() + "_" + dto.getTemplateId();
                List<Map<String, Object>> distinctList = groupByInstance.get(key);
                if (distinctList != null && distinctList.size() > 0) {
                    continue;
                }
            }
            AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
            if (modelInfoMap != null && modelInfoMap.size() > 0) {
                if (modelInfoMap.get(dto.getAssetsTypeSubId() + "") != null) {
                    param.setModelIndex(modelInfoMap.get(dto.getAssetsTypeSubId() + "").getModelIndex());
                    param.setModelId(dto.getAssetsTypeSubId());
                    param.setModelName(modelInfoMap.get(dto.getAssetsTypeSubId() + "").getModelName());
                } else {//资产子类型未获取对应的模型id
                    //资产类型IdList
                    List<Integer> idList = new ArrayList<>();
                    idList.add(dto.getAssetsTypeSubId());
                    //资产父类型id
                    idList.add(dto.getAssetsTypeId());
                    //先判断资产类型在模型分组中可存在，不存在则新建
                    Integer num = mwModelViewDao.checkModelGroupExist(dto.getAssetsTypeId());
                    //获取原资产类型信息
                    List<MwModelSubTypeTable> assetsTypeList = mwModelViewDao.selectAssetsTypeInfoById(idList);
                    Map<Integer, MwModelSubTypeTable> assetsTypeMap = new HashMap();
                    for (MwModelSubTypeTable modelType : assetsTypeList) {
                        assetsTypeMap.put(modelType.getId(), modelType);
                    }
                    if (num != null && num == 0) {
                        //新建资产父类型
                        AddAndUpdateModelGroupParam groupParam = new AddAndUpdateModelGroupParam();
                        if (assetsTypeMap != null && assetsTypeMap.get(dto.getAssetsTypeId()) != null) {
                            MwModelSubTypeTable typeInfo = assetsTypeMap.get(dto.getAssetsTypeId());
                            groupParam.setModelGroupName(typeInfo.getTypeName());
                            groupParam.setPid(0);
                            groupParam.setIsShow(true);
                            groupParam.setGroupLevel(0);
                            groupParam.setSyncZabbix(1);
                            groupParam.setNetwork("Model_" + typeInfo.getTypeName());
                            mwModelManageService.creatModelGroup(groupParam);
                            mwModelViewDao.updateModelGroupId(dto.getAssetsTypeId(), groupParam.getModelGroupName());
                        }
                    }
                    if (assetsTypeMap != null && assetsTypeMap.get(dto.getAssetsTypeSubId()) != null) {
                        MwModelSubTypeTable subTypeInfo = assetsTypeMap.get(dto.getAssetsTypeSubId());
                        //新建子类型
                        AddAndUpdateModelParam addAndUpdateModelParam = new AddAndUpdateModelParam();
                        String modelIndex = "mw_" + UUIDUtils.getUUID();
                        addAndUpdateModelParam.setModelIndex(modelIndex);
                        addAndUpdateModelParam.setPids("1,1001,1003,1031,");
                        String nodes = subTypeInfo.getNodes().replace(dto.getAssetsTypeSubId() + ",", "");
                        addAndUpdateModelParam.setGroupNodes(nodes);
                        addAndUpdateModelParam.setModelGroupId(dto.getAssetsTypeId());
                        addAndUpdateModelParam.setModelName(subTypeInfo.getTypeName());
                        addAndUpdateModelParam.setModelDesc(subTypeInfo.getTypeName());
                        addAndUpdateModelParam.setModelLevel(0);
                        addAndUpdateModelParam.setModelTypeId(1);
                        addAndUpdateModelParam.setModelView(0);
                        mwModelManageService.creatModel(addAndUpdateModelParam);
                        updateModelIdByData(addAndUpdateModelParam.getModelId(), dto.getAssetsTypeSubId());
                        //再次设置模型信息
                        param.setModelIndex(modelIndex);
                        param.setModelId(dto.getAssetsTypeSubId());
                        param.setModelName(subTypeInfo.getTypeName());
                    }

                }
            }
            //权限负责人
            if (userMap.get(dto.getId()) != null) {
                List<String> userStrList = Arrays.asList(userMap.get(dto.getId()).split(","));
                List<Integer> userIds = userStrList.stream().map(Integer::parseInt).collect(Collectors.toList());
                dto.setUserIds(userIds);
            }

            //权限机构部门
            if (orgMap.get(dto.getId()) != null) {
                List<MwModelTangiblePermDTO> orgLists = orgMap.get(dto.getId());
                List<List<Integer>> orgIdList = new ArrayList<>();
                for (MwModelTangiblePermDTO orgDTO : orgLists) {
                    orgDTO.getPerId();
                    List<String> orgStrList = Arrays.asList(orgDTO.getPerId().substring(1).split(","));
                    List<Integer> orgIds = orgStrList.stream().map(Integer::parseInt).collect(Collectors.toList());
                    orgIdList.add(orgIds);
                }
                dto.setOrgIds(orgIdList);
            }

            //权限用户组
            if (groupMap.get(dto.getId()) != null) {
                List<String> groupStrList = Arrays.asList(groupMap.get(dto.getId()).split(","));
                List<Integer> groupIds = groupStrList.stream().map(Integer::parseInt).collect(Collectors.toList());
                dto.setGroupIds(groupIds);
            }
            param.setInstanceName(dto.getInstanceName() == null ? dto.getHostName() : dto.getInstanceName());
            Map m = ListMapObjUtils.beanToMap(dto);
            for (AddModelInstancePropertiesParam p : propertiesParamList) {
                AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                TransferUtils.transferBean(p, instanceParam);
                //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? JSON.toJSONString(m.get(instanceParam.getPropertiesIndexId())) : null);
                if (m.get(ASSETS_ID) != null && instanceParam.getPropertiesIndexId().equals("isManage")) {
                    instanceParam.setPropertiesValue(JSON.toJSONString(true));
                }
                propertiesParamLists.add(instanceParam);
            }
            param.setPropertiesList(propertiesParamLists);
            instanceParamList.add(param);
        }
        if (instanceParamList != null && instanceParamList.size() > 0) {
            mwModelInstanceService.saveData(instanceParamList, true, true);
        }
        return Reply.ok("原资产数据同步到es模型中成功");
    }

    /**
     * 同步凭证校验
     *
     * @param connectParam
     * @return
     */
    @Override
    public Reply checkConnectAuto(List<MwModelMacrosValInfoParam> connectParam) {
        Reply reply = null;
        try {
            reply = connectCheckTest.connectCheck(connectParam);
        } catch (Exception e) {
            log.error("fail to connectParam{}", e);
            return Reply.fail("同步凭证校验失败");
        }
        return reply;
    }

    /**
     * @param modelId      原模型id
     * @param finalModelId 修改后的模型id
     */
    private void updateModelIdByData(Integer modelId, Integer finalModelId) {
        mwModelViewDao.updateModelId(modelId, finalModelId);
        mwModelViewDao.updateModelProperties(modelId, finalModelId);
        mwModelViewDao.updateModelPagefield(modelId, finalModelId);
    }

    protected void getInstanceListData(QueryInstanceModelParam param) {
        //资产视图树的所有数据为基础设施下的模型实例
        if (param.getIsBaseData() == null) {
            param.setIsBaseData(true);
        }
        Set<String> modelIndexSet = new HashSet<>();
        List<Integer> instanceIdList = new ArrayList<>();
        List<ModelInstanceBaseInfoDTO> lists = new ArrayList<>();
        if (!Strings.isNullOrEmpty(param.getModelIndex())) {
            param.setModelIndexs(Arrays.asList(param.getModelIndex()));
        }
        if (CollectionUtils.isEmpty(param.getModelIndexs())) {
            //获取基础设施下的模型Index和实例Id
            lists = mwModelViewDao.getModelIndexANDInstanceInfo(param.getIsBaseData());
            for (ModelInstanceBaseInfoDTO aParam : lists) {
                modelIndexSet.add(aParam.getModelIndex());
            }
            param.setModelIndexs(new ArrayList<>(modelIndexSet));
        }
        if (CollectionUtils.isEmpty(param.getInstanceIds())) {
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                instanceIdList = mwModelViewDao.getInstanceIdByBase(param.getModelIndexs());
            }
            param.setInstanceIds(instanceIdList);
        }
        if (param.getSkipDataPermission() != null && param.getSkipDataPermission()) {
            param.setInstanceIds(instanceIdList);
        } else {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
            if (globalUser.isSystemUser()) {//系统管理员，查询所有实例id
                //前端传入的InstanceIds为空，使用所有的实例ids
                if (CollectionUtils.isEmpty(param.getInstanceIds())) {
                    param.setInstanceIds(instanceIdList);
                }
            } else {//普通用户，获取对应的实例id
                List<Integer> intIds = allTypeIdList.stream().filter(str -> str.matches("\\d+"))//过滤非数字的数据
                        .map(Integer::parseInt).collect(Collectors.toList());
                //前端传入的InstanceIds和权限控制的InstanceIds 做交集，相同的才能使用
                if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                    List<Integer> disInstanceList = param.getInstanceIds().stream().filter(item -> intIds.contains(item)).collect(Collectors.toList());
                    param.setInstanceIds(disInstanceList);
                } else {
                    param.setInstanceIds(intIds);
                }
            }
        }
    }

    /**
     * 根据资产类型获取树结构
     */
    @Override
    public Reply getModelAssetsTreeInfo(QueryInstanceModelParam param) {
        try {
            long time1 = System.currentTimeMillis();
            //获取所有的资产实例ids、modelIndexs数据
            param.setIsBaseData(true);
            getInstanceListData(param);
            //获取对应的es实例数据
            param.setPageSize(pageSize);
            Map<String, Object> map = getModelListInfoByBase(param);
            List<MwModelTangibleAssetsDTO> modelAssetsDto = new ArrayList<>();
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
                modelAssetsDto = JSON.parseArray(JSONObject.toJSONString(listMap), MwModelTangibleAssetsDTO.class);
            }
            List<Integer> modelIds = new ArrayList<>();
            for (MwModelTangibleAssetsDTO m : modelAssetsDto) {
                if (m != null && m.getModelId() != null) {
                    modelIds.add(m.getModelId());
                }
            }

            GlobalUserInfo globalUserInfo = userService.getGlobalUser();
            boolean isSystemUser = globalUserInfo.isSystemUser();
            List<Integer> orgIds = globalUserInfo.getOrgIdList();
            List<Integer> groupIds = globalUserInfo.getUserGroupIdList();
            long time2 = System.currentTimeMillis();
            log.info("资产视图树结构查询时间1:" + (time2 - time1) + "ms");
            List<MwModelViewTreeDTO> treeDTO = new ArrayList<>();
            switch (param.getAssetsTypeId()) {
                case 0:
                    break;
                case 1://品牌（加品牌图标）
                    Map<String, Long> collect = modelAssetsDto.stream().collect(Collectors.groupingBy(m -> !Strings.isNullOrEmpty(m.getManufacturer()) ? m.getManufacturer().toLowerCase() : UNKNOWN
                            , Collectors.counting()));

                    Map<String, List<Integer>> collect1 = modelAssetsDto.stream().collect(Collectors.groupingBy(m -> !Strings.isNullOrEmpty(m.getManufacturer()) ? m.getManufacturer().toLowerCase() : UNKNOWN
                            , Collectors.mapping(MwModelTangibleAssetsDTO::getModelInstanceId, Collectors.toList())));

                    List<AddAndUpdateModelFirmParam> firmParams = mwModelViewDao.getVendorIcon();
                    Map<String, AddAndUpdateModelFirmParam> iconMap = new HashMap();
                    for (AddAndUpdateModelFirmParam firmParam : firmParams) {
                        iconMap.put(firmParam.getBrand().toLowerCase(), firmParam);
                    }
                    long time3 = System.currentTimeMillis();
                    int x = 1;
                    for (Map.Entry<String, List<Integer>> m : collect1.entrySet()) {
                        MwModelViewTreeDTO dto = new MwModelViewTreeDTO();
                        dto.setId(x + "");
                        dto.setInstanceNum(m.getValue().size());
                        dto.setPId("-1");
                        dto.setType(MANUFACTURER);
                        dto.setPropertiesType(1);
                        String url = "";
                        Integer customFlag = -1;
                        if (iconMap != null && iconMap.get(m.getKey().toLowerCase()) != null) {
                            AddAndUpdateModelFirmParam firmParam = iconMap.get(m.getKey().toLowerCase());
                            dto.setName(firmParam.getBrand());
                            url = firmParam.getVendorSmallIcon();
                            customFlag = firmParam.getCustomFlag();
                        } else {
                            dto.setName(UNKNOWN);
                            if (UNKNOWN.equals(m.getKey())) {
                                dto.setId("-2");
                            }
                        }
                        dto.setInstanceIds(m.getValue());
                        dto.setUrl(url);
                        dto.setCustomFlag(customFlag);
                        x++;
                        treeDTO.add(dto);
                    }
                    long time4 = System.currentTimeMillis();
                    log.info("资产视图::品牌树::结构查询时间1:" + (time3 - time2) + "ms;时间2:" + (time4 - time3) + "ms");
                    break;
                case 2://资产类型（资产类型图标待加）
                    //获取所有模型实例的分组信息。根据资产类型分类
                    Map<String, Long> assetsTypeCollect = modelAssetsDto.stream().collect(Collectors.groupingBy(m -> m.getGroupNodes() != null ? m.getGroupNodes().toLowerCase() : "-1"
                            , Collectors.counting()));
                    Map<String, List<Integer>> assetsTypeCollect2 = modelAssetsDto.stream().collect(Collectors.groupingBy(m -> m.getGroupNodes() != null ? m.getGroupNodes().toLowerCase() : "-1"
                            , Collectors.mapping(MwModelTangibleAssetsDTO::getModelInstanceId, Collectors.toList())));


                    //根据资产子类型分类
                    Map<String, Long> assetsSubTypeCollect = modelAssetsDto.stream().collect(Collectors.groupingBy(m -> m.getAssetsTypeSubId() != null ? m.getAssetsTypeSubId().toString() : "0"
                            , Collectors.counting()));
                    Map<String, List<Integer>> assetsSubTypeCollect2 = modelAssetsDto.stream().collect(Collectors.groupingBy(m -> m.getAssetsTypeSubId() != null ? m.getAssetsTypeSubId().toString() : "0"
                            , Collectors.mapping(MwModelTangibleAssetsDTO::getModelInstanceId, Collectors.toList())));

                    if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                        List<List<Integer>> instanceIdGroups = Lists.partition(param.getInstanceIds(), insBatchFetchNum);
                        for (List<Integer> instanceIdSubList : instanceIdGroups) {
                            treeDTO.addAll(mwModelViewDao.getModelGroupTreeByView(instanceIdSubList));
                        }
                    }
                    long time5 = System.currentTimeMillis();
                    treeDTO = treeDTO.stream().distinct().collect(Collectors.toList());
                    if (treeDTO != null && treeDTO.size() > 0) {
                        for (MwModelViewTreeDTO dto : treeDTO) {
                            if (GROUP_NODES.equals(dto.getType()) && assetsTypeCollect != null) {
                                if (dto.getRealId().equals("0")) {
                                    dto.setInstanceNum(modelAssetsDto.size());
                                } else {
                                    //根据分组groupNodes匹配
                                    int instaceNUm = 0;
                                    for (Map.Entry<String, List<Integer>> entry : assetsTypeCollect2.entrySet()) {
                                        String k = entry.getKey();
                                        List<Integer> v = entry.getValue();
                                        if (k.indexOf("," + dto.getRealId() + ",") != -1) {
                                            instaceNUm += v.size();
                                            dto.setInstanceIds(v);
                                        }
                                    }
                                    dto.setInstanceNum(instaceNUm);
                                }
                            }
                            if (ASSETTYPE_SUB_ID.equals(dto.getType()) && assetsTypeCollect2 != null) {
                                dto.setInstanceNum(intValueConvert(assetsSubTypeCollect2.get(dto.getRealId()) != null ? assetsSubTypeCollect2.get(dto.getRealId()).size() : 0));
                                dto.setInstanceIds(assetsSubTypeCollect2.get(dto.getRealId()));
                            }
                            if (!Strings.isNullOrEmpty(dto.getNodes())) {
                                List<String> list = Arrays.asList(dto.getNodes().substring(1).split(","));
                                List<Integer> listInts = list.stream().map(Integer::parseInt).collect(Collectors.toList());
                                dto.setModelGroupIdList(listInts);
                            } else {
                                dto.setModelGroupIdList(new ArrayList<>());
                            }
                        }
                    }
                    long time6 = System.currentTimeMillis();
                    log.info("资产视图::资产类型::树结构查询时间1:" + (time5 - time2) + "ms;时间2:" + (time6 - time5) + "ms");
                    break;
                case 3://标签
                    break;
                case 4://用户
                    break;
                case 5://用户组
                    break;
                case 6://机构
                    List<Integer> orgList = new ArrayList<>();
                    //获取所有机构数据，
                    //利用机构数据重复的次数，等价于机构出现的次数，也就是各个机构下实例的数量
                    List<List<Integer>> list = new ArrayList<>();
                    for (MwModelTangibleAssetsDTO m : modelAssetsDto) {
                        if (m != null && m.getOrgIds() != null) {
                            List orgLists = JSONArray.parseArray(JSONArray.toJSONString(m.getOrgIds()), List.class);
                            list = (List<List<Integer>>) orgLists;
                            for (List<Integer> list1 : list) {//只获取机构的最后一层数据
                                if (isSystemUser) {
                                    orgList.add(list1.get(list1.size() - 1));
                                } else {//非管理员用户，需要过滤机构部门，只显示当前用户所拥有的部门信息
                                    if (orgIds.contains(list1.get(list1.size() - 1))) {
                                        orgList.add(list1.get(list1.size() - 1));
                                    }
                                }

                            }
                        }
                    }
                    long time7 = System.currentTimeMillis();
                    Map<Integer, Long> orgMap = orgList.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
                    //去除，查询部门机构信息。
                    List<Integer> orgDisList = orgList.stream().distinct().collect(Collectors.toList());
                    if (orgDisList.size() > 0) {
                        treeDTO = mwModelViewDao.getOrgInfoById(orgDisList);
                    }
                    for (MwModelViewTreeDTO viewTreeDTO : treeDTO) {
                        if (orgMap != null && orgMap.size() > 0) {
                            viewTreeDTO.setInstanceNum(intValueConvert(orgMap.get(intValueConvert(viewTreeDTO.getId()))));
                        }
                    }
                    long time8 = System.currentTimeMillis();
                    log.info("资产视图::部门机构::树结构查询时间1:" + (time7 - time2) + "ms;时间2:" + (time8 - time7) + "ms");
                    break;
                case 8://用户组
                    List<Integer> groupList = new ArrayList<>();
                    for (MwModelTangibleAssetsDTO m : modelAssetsDto) {
                        if (m != null) {
                            if (m.getGroupIds() != null) {
                                List groupLists = (List<? extends Integer>) JSONArray.parse(JSONArray.toJSONString(m.getGroupIds()));
                                if (groupLists.size() > 0) {
                                    List<Integer> listGroup = (List<Integer>) groupLists;
                                    if (isSystemUser) {
                                        groupList.addAll(listGroup);
                                    } else {//非管理员用户，需要过滤用户组，只显示当前用户所拥有的用户组信息
                                        groupList.addAll(groupIds.stream().filter(listGroup::contains).collect(Collectors.toList()));
                                    }
                                } else {
                                    //-1表示未知
                                    groupList.add(-2);
                                }
                            } else {
                                //-1表示未知
                                groupList.add(-2);
                            }

                        }
                    }
                    long time9 = System.currentTimeMillis();
                    Map<Integer, Long> groupMap = groupList.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
                    //去重，查询部门机构信息。
                    List<Integer> groupDisList = groupList.stream().distinct().collect(Collectors.toList());
                    if (groupDisList.size() > 0) {
                        treeDTO = mwModelViewDao.getGroupInfoById(groupDisList);
                    }
                    for (MwModelViewTreeDTO viewTreeDTO : treeDTO) {
                        if (groupMap != null && groupMap.size() > 0) {
                            viewTreeDTO.setInstanceNum(intValueConvert(groupMap.get(intValueConvert(viewTreeDTO.getId()))));
                        }
                    }
                    //将未知的状态加上。
                    if (groupDisList.contains(-2)) {
                        MwModelViewTreeDTO dto = new MwModelViewTreeDTO();
                        dto.setId("-2");
                        dto.setName(UNKNOWN);
                        dto.setPId("-1");
                        dto.setType(GROUP_IDS);
                        dto.setInstanceNum(groupMap.get(-2).intValue());
                        treeDTO.add(dto);
                    }
                    long time10 = System.currentTimeMillis();
                    log.info("资产视图::用户组::树结构查询时间1:" + (time9 - time2) + "ms;时间2:" + (time10 - time9) + "ms");
                    break;
                case 10://业务系统

                    //外部关联类型转换（id转为名称）
                    mwModelCommonServiceImpl.relationFieldConvert(listMap);
                    modelAssetsDto = JSON.parseArray(JSONObject.toJSONString(listMap), MwModelTangibleAssetsDTO.class);
                    treeDTO = mwModelSysClassIfyHandle.handleSysClassIfy(modelAssetsDto);
                    long time11 = System.currentTimeMillis();
                    log.info("资产视图::业务系统::树结构查询时间1:" + (time11 - time2) + "ms");
                    break;
                default:
                    break;
            }
            Collections.sort(treeDTO, new Comparator<MwModelViewTreeDTO>() {
                @Override
                public int compare(MwModelViewTreeDTO o1, MwModelViewTreeDTO o2) {
                    if (null == o1.getName() || UNKNOWN.equals(o1.getName())) {
                        return 1;
                    }
                    if (null == o2.getName() || UNKNOWN.equals(o2.getName())) {
                        return -1;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return Reply.ok(treeDTO);
        } catch (Throwable e) {
            log.error("fail to getModelAssetsTreeInfo with cause:{}", e);
            return Reply.fail("根据资产类型获取树结构失败！");
        }
    }
//    public Reply getModelAssetsTreeInfo(QueryInstanceModelParam param) {
//        try {
//            //获取所有的资产实例ids、modelIndexs数据
//            param.setIsBaseData(true);
//            getInstanceListData(param);
//            //获取对应的es实例数据
//            param.setPageSize(pageSize);
//            Map<String, Object> map = getModelListInfoByBase(param);
//            List<Map<String, Object>> instanceInfoList = new ArrayList<>();
//            if (map != null && map.get("data") != null) {
//                instanceInfoList = (List<Map<String, Object>>) map.get("data");
//            }
//            List<Integer> modelIds = new ArrayList<>();
//            for (Map<String, Object> m : instanceInfoList) {
//                m.get("modelId").toString();
//                if (m != null && m.get("modelId") != null) {
//                    modelIds.add(Integer.valueOf(m.get("modelId").toString()));
//                }
//            }
//            List<MwModelViewTreeDTO> treeDTO = new ArrayList<>();
//            switch (param.getAssetsTypeId()) {
//                case 0:
//                    break;
//                case 1://品牌（加品牌图标）
//                    Map<String, Long> collect = instanceInfoList.stream().collect(Collectors.groupingBy(m -> m.get("manufacturer") != null ? m.get("manufacturer").toString().toLowerCase() : "未知"
//                            , Collectors.counting()));
//                    List<AddAndUpdateModelFirmParam> firmParams = mwModelViewDao.getVendorIcon();
//                    Map<String, AddAndUpdateModelFirmParam> iconMap = new HashMap();
//                    for (AddAndUpdateModelFirmParam firmParam : firmParams) {
//                        iconMap.put(firmParam.getBrand().toLowerCase(), firmParam);
//                    }
//                    int x = 1;
//                    for (Map.Entry<String, Long> m : collect.entrySet()) {
//                        MwModelViewTreeDTO dto = new MwModelViewTreeDTO();
//                        dto.setId(x + "");
//                        dto.setInstanceNum(m.getValue().intValue());
//                        dto.setPId("-1");
//                        dto.setType("manufacturer");
//                        dto.setPropertiesType(1);
//                        String url = "";
//                        Integer customFlag = -1;
//                        if (iconMap != null && iconMap.get(m.getKey().toLowerCase()) != null) {
//                            AddAndUpdateModelFirmParam firmParam = iconMap.get(m.getKey().toLowerCase());
//                            dto.setName(firmParam.getBrand());
//                            url = firmParam.getVendorSmallIcon();
//                            customFlag = firmParam.getCustomFlag();
//                        } else {
//                            dto.setName("未知");
//                            if ("未知".equals(m.getKey())) {
//                                dto.setId("-2");
//                            }
//                        }
//                        dto.setUrl(url);
//                        dto.setCustomFlag(customFlag);
//                        x++;
//                        treeDTO.add(dto);
//                    }
//                    break;
//                case 2://资产类型（资产类型图标待加）
//                    //获取所有模型实例的分组信息。根据资产类型分类
//                    Map<String, Long> assetsTypeCollect = instanceInfoList.stream().collect(Collectors.groupingBy(m -> m.get(GROUP_NODES) != null ? m.get(GROUP_NODES).toString().toLowerCase() : "-1"
//                            , Collectors.counting()));
//                    //根据资产子类型分类
//                    Map<String, Long> assetsSubTypeCollect = instanceInfoList.stream().collect(Collectors.groupingBy(m -> m.get(ASSETTYPE_SUB_ID) != null ? m.get(ASSETTYPE_SUB_ID).toString().toLowerCase() : "0"
//                            , Collectors.counting()));
//                    if (modelIds != null && modelIds.size() > 0) {
//                        treeDTO = mwModelViewDao.getModelGroupTreeByView(param.getInstanceIds());
//                    }
//                    if (treeDTO != null && treeDTO.size() > 0) {
//                        for (MwModelViewTreeDTO dto : treeDTO) {
//                            if (GROUP_NODES.equals(dto.getType()) && assetsTypeCollect != null) {
//                                if (dto.getRealId().equals("0")) {
//                                    dto.setInstanceNum(instanceInfoList.size());
//                                } else {
//                                    dto.setInstanceNum(0);
//                                    //根据分组groupNodes匹配
//                                    assetsTypeCollect.forEach((k, v) -> {
//                                        if (k.indexOf("," + dto.getRealId() + ",") != -1) {
//                                            dto.setInstanceNum(v.intValue());
//                                        }
//                                    });
//                                }
//                            }
//                            if (ASSETTYPE_SUB_ID.equals(dto.getType()) && assetsTypeCollect != null) {
//                                dto.setInstanceNum(assetsSubTypeCollect.get(dto.getRealId()) != null ? assetsSubTypeCollect.get(dto.getRealId()).intValue() : 0);
//                            }
//                            if (!Strings.isNullOrEmpty(dto.getNodes())) {
//                                List<String> list = Arrays.asList(dto.getNodes().substring(1).split(","));
//                                List<Integer> listInts = list.stream().map(Integer::parseInt).collect(Collectors.toList());
//                                dto.setModelGroupIdList(listInts);
//                            } else {
//                                dto.setModelGroupIdList(new ArrayList<>());
//                            }
//                        }
//                    }
//                    break;
//                case 3://标签
//                    break;
//                case 4://用户
//                    break;
//                case 5://用户组
//                    break;
//                case 6://机构
//                    List<Integer> orgList = new ArrayList<>();
//                    //获取所有机构数据，
//                    //利用机构数据重复的次数，等价于机构出现的次数，也就是各个机构下实例的数量
//                    List<List<Integer>> list = new ArrayList<>();
//                    for (Map<String, Object> m : instanceInfoList) {
//                        if (m != null && m.get("orgIds") != null) {
//                            List orgLists = JSONArray.parseArray(m.get("orgIds").toString(), List.class);
//                            list = (List<List<Integer>>) orgLists;
//                            for (List<Integer> list1 : list) {
//                                for (Integer orgId : list1) {
//                                    orgList.add(orgId);
//                                }
//                            }
//                        }
//                    }
//                    Map<Integer, Long> orgMap = orgList.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
//                    //去除，查询部门机构信息。
//                    List<Integer> orgDisList = orgList.stream().distinct().collect(Collectors.toList());
//                    if (orgDisList.size() > 0) {
//                        treeDTO = mwModelViewDao.getOrgInfoById(orgDisList);
//                    }
//                    for (MwModelViewTreeDTO viewTreeDTO : treeDTO) {
//                        if (orgMap != null && orgMap.size() > 0) {
//                            viewTreeDTO.setInstanceNum(orgMap.get(Integer.valueOf(viewTreeDTO.getId())).intValue());
//                        }
//                    }
//                    break;
//                case 8://用户组
//                    List<Integer> groupList = new ArrayList<>();
//                    for (Map<String, Object> m : instanceInfoList) {
//                        if (m != null) {
//                            if (m.get("groupIds") != null) {
//                                List groupLists = (List<? extends Integer>) JSONArray.parse(m.get("groupIds").toString());
//                                if (groupLists.size() > 0) {
//                                    List<Integer> listGroup = (List<Integer>) groupLists;
//                                    for (Integer groupId : listGroup) {
//                                        groupList.add(groupId);
//                                    }
//                                } else {
//                                    //-1表示未知
//                                    groupList.add(-2);
//                                }
//                            } else {
//                                //-1表示未知
//                                groupList.add(-2);
//                            }
//
//                        }
//                    }
//                    Map<Integer, Long> groupMap = groupList.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
//                    //去重，查询部门机构信息。
//                    List<Integer> groupDisList = groupList.stream().distinct().collect(Collectors.toList());
//                    if (groupDisList.size() > 0) {
//                        treeDTO = mwModelViewDao.getGroupInfoById(groupDisList);
//                    }
//                    for (MwModelViewTreeDTO viewTreeDTO : treeDTO) {
//                        if (groupMap != null && groupMap.size() > 0) {
//                            viewTreeDTO.setInstanceNum(groupMap.get(Integer.valueOf(viewTreeDTO.getId())).intValue());
//                        }
//                    }
//                    //将未知的状态加上。
//                    if (groupDisList.contains(-2)) {
//                        MwModelViewTreeDTO dto = new MwModelViewTreeDTO();
//                        dto.setId("-2");
//                        dto.setName("未知");
//                        dto.setPId("-1");
//                        dto.setType("groupIds");
//                        dto.setInstanceNum(groupMap.get(-2).intValue());
//                        treeDTO.add(dto);
//                    }
//                    break;
//                default:
//                    break;
//            }
//            treeDTO = treeDTO.stream().sorted(Comparator.comparing(MwModelViewTreeDTO::getId).reversed()).collect(Collectors.toList());
//            return Reply.ok(treeDTO);
//        } catch (Throwable e) {
//            log.error("fail to getModelAssetsTreeInfo with cause:{}", e);
//            return Reply.fail("根据资产类型获取树结构失败！");
//        }
//    }

    @Override
    public Reply getScanInfoByICMP(QueryModelViewInstanceParam param) {
        MwModelScanResultSuccessParam successParam = mwModelViewDao.getScanTemplateInfoByICMP();
        if (successParam == null || successParam.getAssetsTypeId() == null) {
            return Reply.fail("扫描失败，ICMP通用模板不存在！");
        }
        List groupNodeList = new ArrayList();
        //根据资产子类型获取模型的分组信息
        if (successParam.getAssetsTypeSubId() != null && successParam.getAssetsTypeSubId() != -1) {
            String modelGroupNodes = mwModelViewDao.getModelIdGroups(successParam.getAssetsTypeSubId());
            if (modelGroupNodes.split(",").length > 0) {
                String[] str = modelGroupNodes.split(",");
                for (String groupNode : str) {
                    //将分组信息组装成数字数组，方便前端显示
                    if (!Strings.isNullOrEmpty(groupNode)) {
                        groupNodeList.add(intValueConvert(groupNode));
                    }
                }
            }
        }
        successParam.setModelGroup(groupNodeList);
        if (param != null && !Strings.isNullOrEmpty(param.getInBandIp())) {
            successParam.setHostName("ICMP_" + param.getInBandIp());
            successParam.setInstanceName("ICMP_" + param.getInBandIp());
            successParam.setInBandIp(param.getInBandIp());
        }
        return Reply.ok(successParam);
    }

    @Override
    public Reply getSettingModuleInfo() {
        LicenseAssetsModuleStatusParam licenseAssetsModuleStatusParam = new LicenseAssetsModuleStatusParam();
        licenseAssetsModuleStatusParam.setOperationCount(0);
        licenseAssetsModuleStatusParam.setLogCount(0);
        licenseAssetsModuleStatusParam.setAutoCount(0);
        licenseAssetsModuleStatusParam.setPropCount(0);
        LicenseAssetsModuleStatusParam moduleStatus = licenseManagementService.getModuleStatus(licenseAssetsModuleStatusParam);
        return Reply.ok(moduleStatus);
    }


    /**
     * 获取用户查询资产的限制条件
     *
     * @param queryParam 查询参数
     */
    private void getUserPerm(Map<String, Object> queryParam) {
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        //数据权限：private public
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
        //用户角色是否为系统管理员
        Boolean isAdmin = false;
        //用户所在的用户组id
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            queryParam.put("groupIds", groupIds);
        }
        switch (dataPermission) {
            case PRIVATE:
                queryParam.put("userId", userId);
                break;
            case PUBLIC:
                String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                List<Integer> orgIds = new ArrayList<>();
                if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                    isAdmin = true;
                }
                if (!isAdmin) {
                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                }
                if (null != orgIds && orgIds.size() > 0) {
                    queryParam.put("orgIds", orgIds);
                }
                break;
        }
        queryParam.put("isAdmin", isAdmin);
        queryParam.put("perm", dataPermission.getName());
    }

    @Override
    public Reply findTopoModelAssetsBySNMP() {
        Reply reply = null;

        try {
            if (modelAssetEnable) {
                QueryModelAssetsParam param = new QueryModelAssetsParam();

                //判断是否是定时任务用户
                MwLoginUserDto taskUser = iLoginCacheInfo.getTimeTaskUser();
                if (null != taskUser) {
                    param.setUserId(taskUser.getUserId());
                }
                param.setMonitorMode(RuleType.SNMPv1v2.getMonitorMode());
                List<Map<String, Object>> modelAssets = getModelListInfoByPerm(param);
                List<MwTangibleassetsDTO> mwTangibleassetsDTOS = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
                reply = Reply.ok(mwTangibleassetsDTOS);
            } else {
                reply = mwTangibleAssetsService.selectListWithExtend();
            }
        } catch (Exception e) {
            log.error("findTopoModelAssetsBySNMP", e);
        }
        return reply;
    }

    @Override
    public <T> Reply findTopoModelAssets(Class<T> clazz, Map map) {
        Reply reply = null;
        try {
            if (modelAssetEnable) {
                List<Map<String, Object>> modelAssets = null;
                QueryModelAssetsParam param = new QueryModelAssetsParam();
                Object idsObj = map.get(ID_KEY);
                if (null != idsObj) {
                    param.setInstanceIds((List) idsObj);
                    modelAssets = getModelListInfoByPerm(param);
                }

                List ips = (List) map.get(IPS_KEY);
                if (null != ips && ips.size() > 0) {
                    QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                    QueryModelOr queryModelOr = new QueryModelOr("inBandIp", ips);
                    modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
                }

                List scanIds = (List) map.get(SCANSUCESS_ID_KEY);
                if (null != scanIds && scanIds.size() > 0) {
                    QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                    QueryModelOr queryModelOr = new QueryModelOr("scanSuccessId", scanIds);
                    modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
                }

                if (null != modelAssets && modelAssets.size() > 0) {
                    List<T> mwTangibleassetsTables = MwModelUtils.convertEsData(clazz, modelAssets);
                    reply = Reply.ok(mwTangibleassetsTables);
                }
            } else {

                map.put("keyName", clazz.getName());
                reply = mwTangibleAssetsService.selectTopoAssetsList(map);
                List<MwTangibleassetsTable> list = (List<MwTangibleassetsTable>) reply.getData();
                List<T> resultList = CopyUtils.copyList(clazz, list);
                reply = Reply.ok(resultList);
            }
        } catch (Exception e) {
            log.error("findTopoModelAssets", e);
        }
        return reply;
    }

    @Override
    public <T> Reply findTopoModelAssets(Class<T> clazz) {
        Reply reply = null;
        try {
            if (modelAssetEnable) {
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(null, esParam);
                List<T> mwTangibleassetsTables = MwModelUtils.convertEsData(clazz, modelAssets);
                reply = Reply.ok(mwTangibleassetsTables);
            } else {
                reply = mwTangibleAssetsService.selectListWithExtend();
            }
        } catch (Exception e) {
            log.error("findTopoModelAssets topo {}", e);
        }
        return reply;
    }

    @Override
    public <T> List<T> findModelAssets(Class<T> clazz, QueryModelAssetsParam param) throws Exception {
        long time1 = System.currentTimeMillis();
        List<Map<String, Object>> modelAssets = getModelListInfoByPerm(param);
        mwModelCommonServiceImpl.relationFieldConvert(modelAssets);
        //是否告警查询，true，将告警所需要的字段带入
        long time2 = System.currentTimeMillis();
        long time3 = 0l;
        if (param.isAlertQuery()) {
            modelAssets = getAlertFieldInfo2(modelAssets);
            time3 = System.currentTimeMillis();
        }
        List<T> mwTangibleassetsTables = MwModelUtils.convertEsData(clazz, modelAssets);
        long time4 = System.currentTimeMillis();
        log.info("findModelAssets::公共接口查询数据耗时:es获取:" + (time2 - time1) + "ms;告警字段转换:" + (time3 - time2) + "ms;总耗时:" + (time4 - time1) + "ms");
        return mwTangibleassetsTables;
    }

    /**
     * 获取外部关联Id下的实例数据
     *
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public List<MwTangibleassetsDTO> findModelAssetsByRelationIds(QueryModelInstanceByPropertyIndexParamList params) throws
            Exception {
        //如果查询参数ParamLists没有值为null，则查询所有的数据
        if (params == null || CollectionUtils.isEmpty(params.getParamLists())) {
            QueryModelAssetsParam param = new QueryModelAssetsParam();
            param.setIsQueryAssetsState(params.getIsQueryAssetsState());
            param.setSkipDataPermission(params.getSkipDataPermission());
            List<MwTangibleassetsDTO> list = findModelAssets(MwTangibleassetsDTO.class, param);
            log.info("参数为null时,查询所有资产::" + list.size());
            return list;
        }
        //获取所有模型的公共属性
        List<ModelInfo> modelInfoList = mwModelManageDao.selectAllModelInfo();
        Map<String, PropertyInfo> propertyMap = new HashMap<>();
        Set<String> relationIndexIdSets = new HashSet<>();
        if (null != modelInfoList) {
            for (ModelInfo modelInfo : modelInfoList) {
                if (null != modelInfo.getPropertyInfos()) {
                    for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                        propertyMap.put(propertyInfo.getIndexId(), propertyInfo);
                    }
                }
            }
        }
        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
        for (QueryModelInstanceByPropertyIndexParam queryParam : params.getParamLists()) {
            if (propertyMap != null && propertyMap.containsKey(queryParam.getPropertiesIndexId())) {
                PropertyInfo propertyInfo = propertyMap.get(queryParam.getPropertiesIndexId());
                AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
                addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
                addModelInstancePropertiesParam.setPropertiesValue(queryParam.getPropertiesValue());
                addModelInstancePropertiesParam.setQueryType(0);//默认或查询
                if (MODEL_SYSTEM.equals(queryParam.getPropertiesIndexId())) {
                    addModelInstancePropertiesParam.setQueryType(1);//设置业务系统为 且查询
                }
                propertiesParamList.add(addModelInstancePropertiesParam);
            }

        }
        List<Map<String, Object>> listMap = new ArrayList<>();
        getEsQuery(propertiesParamList, listMap);
        //是否查询资产状态
        List<Map<String, Object>> newList = new ArrayList<>();
        newList = listMap;
        if (mwInspectModeService.getInspectModeInfo() || params.getIsQueryAssetsState() != null && params.getIsQueryAssetsState()) {
            newList = new ArrayList<>();
            newList = getAssetsStateByZabbix(listMap);
        }
        //外部关联类型转换（id转为名称）
        mwModelCommonServiceImpl.relationFieldConvert(newList);
        List<MwTangibleassetsDTO> list = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, newList);
        return list;
    }


    private void getEsQuery(List<AddModelInstancePropertiesParam> propertiesParamList, List<Map<String, Object>> listMap) throws
            IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
        for (AddModelInstancePropertiesParam m : propertiesParamList) {
            if (m.getPropertiesType() != null) {
                m.setFilterQuery(true);//设置为精准查询
                QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
                if (null != qb) {
                    if (m.getQueryType() != null && m.getQueryType().intValue() == 0) {
                        queryBuilder2.should(qb);
                    } else {
                        queryBuilder.must(qb);
                    }
                }
            }
        }
        queryBuilder.must(queryBuilder2);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(pageSize);
        //设置超时时间
        searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices("mw_*");
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit searchHit : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put("esId", searchHit.getId());
            listMap.add(sourceAsMap);
        }
    }

    private List<MwModelInstanceParam> getRelationInstnaceInfo(List<PropertyInfo> propertyInfoList) {
        List<PropertyInfo> list = propertyInfoList.stream().filter(s -> s.getPropertiesTypeId() == MULTIPLE_RELATION.getCode() || s.getPropertiesTypeId() == SINGLE_RELATION.getCode()).collect(Collectors.toList());
        List<String> relationIndexs = list.stream().map(PropertyInfo::getRelationModelIndex).collect(Collectors.toList());
        List<String> disRelationIndexs = relationIndexs.stream().distinct().collect(Collectors.toList());
        List<MwModelInstanceParam> lists = new ArrayList<>();
        List<List<String>> instanceIdGroups = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(disRelationIndexs)) {
            instanceIdGroups = Lists.partition(disRelationIndexs, insBatchFetchNum);
            if (null != instanceIdGroups) {
                for (List<String> relationIndexList : instanceIdGroups) {
                    lists.addAll(mwModelViewDao.getSystemAndClassifyInstanceInfo(relationIndexList));
                }
            }
        }
        List<MwModelInstanceParam> instanceParamList = new ArrayList<>();
        for (PropertyInfo propertyInfo : list) {
            for (MwModelInstanceParam instanceParam : lists) {
                MwModelInstanceParam params = new MwModelInstanceParam();
                BeanUtils.copyProperties(instanceParam, params);
                if (propertyInfo.getRelationModelIndex() != null && propertyInfo.getRelationModelIndex().equals(instanceParam.getModelIndex())) {
                    params.setPropertiesIndex(propertyInfo.getIndexId());
                    instanceParamList.add(params);
                }
            }
        }
        return instanceParamList;
    }


    /**
     * 获取告警字段信息
     */
    private List<Map<String, Object>> getAlertFieldInfo2(List<Map<String, Object>> modelAssets) throws
            Exception {
        mwModelCommonServiceImpl.relationFieldConvert(modelAssets);
        for (Map<String, Object> m : modelAssets) {
            m.forEach((k, v) -> {
                //机柜位置
                if (POSITIONBYCABINET.getField().equals(k)) {
                    String index = "";
                    //机柜数据格式为Map
                    if (v instanceof Map && ((Map) v).size() > 0) {
                        Map mapInfo = (Map) v;
                        Integer startIndex = intValueConvert(mapInfo.get("start"));
                        Integer endIndex = intValueConvert(mapInfo.get("end"));
                        if (endIndex > startIndex) {
                            index = "第" + (startIndex + 1) + "-" + (endIndex + 1) + "层";
                        } else {
                            index = "第" + (startIndex + 1) + "层";
                        }
                    }
                    m.put(k, index);
                }
            });
        }
        return modelAssets;
    }


    /**
     * 获取告警字段信息
     */
    private List<Map<String, Object>> getAlertFieldInfo(List<Map<String, Object>> modelAssets) throws
            Exception {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        List<QueryAlertFieldParam> alertFieldParam = new ArrayList<>();
        for (Map<String, Object> objectMap : modelAssets) {
            QueryAlertFieldParam queryAlertFieldParam = new QueryAlertFieldParam();
            if (objectMap.get(POSITIONBYCABINET.getField()) != null && objectMap.get(POSITIONBYCABINET.getField()) instanceof List) {//对异常数据处理
                List<CabinetLayoutDataParam> cabinetCoordinateList = JSON.parseArray(JSONObject.toJSONString(objectMap.get(POSITIONBYCABINET.getField())), CabinetLayoutDataParam.class);
                if (CollectionUtils.isNotEmpty(cabinetCoordinateList)) {
                    objectMap.put(POSITIONBYCABINET.getField(), cabinetCoordinateList.get(0));
                }
            }
            queryAlertFieldParam = JSONObject.parseObject(JSONObject.toJSONString(objectMap), QueryAlertFieldParam.class);
            alertFieldParam.add(queryAlertFieldParam);
        }
        //所属机柜Id
        List<Integer> cabinetInstanceIds = new ArrayList<>();
        //关联的业务系统Id
        List<Integer> modelSystemInstanceIds = new ArrayList<>();
        //所属机房Id
        List<Integer> roomInstanceIds = new ArrayList<>();
        //全部实例id
        List<Integer> instanceAllIds = new ArrayList<>();
        for (QueryAlertFieldParam param : alertFieldParam) {
            //所属机柜字段值不为空，获取关联机柜设备id
            if (!Strings.isNullOrEmpty(param.getRelationSiteCabinet())) {
                cabinetInstanceIds.add(intValueConvert(param.getRelationSiteCabinet()));
            }
            //业务系统
            if (!Strings.isNullOrEmpty(param.getModelSystem())) {
                //判断是否是数字
                boolean isNum = pattern.matcher(param.getModelSystem()).matches();
                if (isNum) {
                    modelSystemInstanceIds.add(intValueConvert(param.getModelSystem()));
                }
            }
            //业务系统
            if (!Strings.isNullOrEmpty(param.getModelClassify())) {
                //判断是否是数字
                boolean isNum = pattern.matcher(param.getModelClassify()).matches();
                if (isNum) {
                    modelSystemInstanceIds.add(intValueConvert(param.getModelClassify()));
                }
            }
        }
        List<QueryInstanceParam> cabinetDataList = mwModelInstanceService.getCabinetInfoByRelationCabinedId(cabinetInstanceIds);
        for (QueryAlertFieldParam assetsData : alertFieldParam) {
            for (QueryInstanceParam cabinetData : cabinetDataList) {
                if (!Strings.isNullOrEmpty(assetsData.getRelationSiteCabinet()) &&
                        cabinetData.getModelInstanceId() != null) {
                    //所属机柜id 和 机柜实例id相同，获取机柜实例中的关联机房id
                    if (assetsData.getRelationSiteCabinet().equals(strValueConvert(cabinetData.getModelInstanceId()))) {
                        assetsData.setRelationSiteRoom(cabinetData.getRelationSiteRoom());
                        if (!Strings.isNullOrEmpty(cabinetData.getRelationSiteRoom())) {
                            roomInstanceIds.add(intValueConvert(cabinetData.getRelationSiteRoom()));
                        }
                    }
                }
            }
        }
        instanceAllIds.addAll(cabinetInstanceIds);
        instanceAllIds.addAll(modelSystemInstanceIds);
        instanceAllIds.addAll(roomInstanceIds);
        List<QueryInstanceParam> instanceList = mwModelInstanceService.getAllInstanceNameById(instanceAllIds);
        if (CollectionUtils.isNotEmpty(instanceList)) {
            Map<Integer, String> map = instanceList.stream().collect(Collectors.toMap(QueryInstanceParam::getModelInstanceId, QueryInstanceParam::getInstanceName, (
                    value1, value2) -> {
                return value2;
            }));
            alertFieldSetting(alertFieldParam, modelAssets, map);
        }
        return modelAssets;
    }


    private void alertFieldSetting
            (List<QueryAlertFieldParam> alertFieldParam, List<Map<String, Object>> modelAssets, Map<Integer, String> map) {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        for (QueryAlertFieldParam assetsData : alertFieldParam) {
            //判断是否是数字
            String relationSiteRoomName = "";
            String relationSiteCabinetName = "";
            String modelSystemName = "";
            String modelClassifyName = "";
            if (!Strings.isNullOrEmpty(assetsData.getRelationSiteRoom())) {
                boolean isRelationSiteRoomNum = pattern.matcher(assetsData.getRelationSiteRoom()).matches();
                if (isRelationSiteRoomNum) {
                    relationSiteRoomName = map.get(intValueConvert(assetsData.getRelationSiteRoom()));
                }
            }
            if (!Strings.isNullOrEmpty(assetsData.getRelationSiteCabinet())) {
                boolean isRelationSiteCabinetNum = pattern.matcher(assetsData.getRelationSiteCabinet()).matches();
                if (isRelationSiteCabinetNum) {
                    relationSiteCabinetName = map.get(intValueConvert(assetsData.getRelationSiteCabinet()));
                }
            }
            if (!Strings.isNullOrEmpty(assetsData.getModelSystem())) {
                boolean isModelSystemNum = pattern.matcher(assetsData.getModelSystem()).matches();
                if (isModelSystemNum) {
                    modelSystemName = map.get(intValueConvert(assetsData.getModelSystem()));
                }
            }
            if (!Strings.isNullOrEmpty(assetsData.getModelClassify())) {
                boolean isModelClassifyNum = pattern.matcher(assetsData.getModelClassify()).matches();
                if (isModelClassifyNum) {
                    modelClassifyName = map.get(intValueConvert(assetsData.getModelClassify()));
                }
            }
            CabinetLayoutDataParam cabinetLayout = assetsData.getPositionByCabinet();
            Integer stratNum = 0;
            Integer endNum = 0;
            if (cabinetLayout != null) {
                stratNum = cabinetLayout.getStart() + 1;///保存的数据是从0开始的，展示时需要加1；
                endNum = cabinetLayout.getEnd() + 1;
                assetsData.setPositionByCabinetName(stratNum + "-" + endNum);
            } else {
                assetsData.setPositionByCabinetName("");
            }
            assetsData.setModelSystemName(modelSystemName != null ? modelSystemName : "");
            assetsData.setModelClassifyName(modelClassifyName != null ? modelClassifyName : "");
            if (!Strings.isNullOrEmpty(relationSiteRoomName) && !Strings.isNullOrEmpty(relationSiteCabinetName)) {
                assetsData.setModelArea(relationSiteRoomName + "_" + relationSiteCabinetName + "_" + stratNum + "-" + endNum);
            } else {
                assetsData.setModelArea("");
            }
            assetsData.setRelationSiteRoomName(relationSiteRoomName != null ? relationSiteRoomName : "");
            assetsData.setRelationSiteCabinetName(relationSiteCabinetName != null ? relationSiteCabinetName : "");
            for (Map<String, Object> m : modelAssets) {
                JSON.parseObject(JSON.toJSONString(assetsData), new TypeReference<Map<String, Object>>() {
                });
                if (assetsData.getModelInstanceId().equals(strValueConvert(m.get(INSTANCE_ID_KEY)))) {
                    m.putAll(JSON.parseObject(JSON.toJSONString(assetsData), new TypeReference<Map<String, Object>>() {
                    }));
                }
            }
        }
    }

    @Override
    public MwTangibleassetsDTO findModelAssetsByInstanceId(Integer instanceId) throws Exception {
        MwTangibleassetsDTO modelAssetDto = new MwTangibleassetsDTO();
        if (null != instanceId) {
            QueryModelAssetsParam param = new QueryModelAssetsParam();
            param.setInstanceIds(Arrays.asList(instanceId));
            List<Map<String, Object>> modelAssets = getModelListInfoByPerm(param);
            List<MwTangibleassetsDTO> assetsList = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
            if (assetsList != null && assetsList.size() > 0) {
                modelAssetDto = assetsList.get(0);
            }
        }
        return modelAssetDto;
    }

    @Override
    public List<MwRancherProjectUserListDTO> getAllRancherProjectUserInfo() throws Exception {
        QueryInstanceModelParam param = new QueryInstanceModelParam();
        List<String> modelIndexs = mwModelManageDao.selectModelIndexsByModelIds(Arrays.asList(PROJECTS.getModelId(), MatchModelTypeEnum.CLUSTER.getModelId()));
        param.setModelIndexs(modelIndexs);
        //是否查询基础数据，否-》查询所有
        param.setIsBaseData(false);
        //外部调用接口，不做权限控制
        param.setSkipDataPermission(true);
        param.setPageSize(pageSize);
        getInstanceListData(param);
        //根据Rancher实例获取es数据信息
        Map<String, Object> infoMap = getModelListInfoByBase(param);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (infoMap != null && infoMap.get("data") != null) {
            listMap = (List<Map<String, Object>>) infoMap.get("data");
        }
        List<MwRancherProjectUserListDTO> projectListDto = MwModelUtils.convertEsData(MwRancherProjectUserListDTO.class, listMap);
        return projectListDto;
    }

    /**
     * 获取所有虚拟机，所有集群的监控信息
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<VirtualizationMonitorInfo> getAllVirtualInfoByMonitorData() throws Exception {

        QueryVirtualInstanceParam param = new QueryVirtualInstanceParam();
        List<VirtualizationMonitorInfo> virtualizationList = new ArrayList<>();
        List<MwModelInstanceCommonParam> list = mwModelInstanceDao.selectModelInstanceInfoByIds(Arrays.asList(VCENTER.getModelId(), CLUSTER.getModelId(), DATACNETER.getModelId()));
        Map<Integer, List<MwModelInstanceCommonParam>> map = list.stream().collect(Collectors.groupingBy(MwModelInstanceCommonParam::getModelId));
        List<MwModelInstanceCommonParam> vcenterInstanceInfoList = map.get(VCENTER.getModelId());
        //默认获取集群信息
        List<MwModelInstanceCommonParam> instanceInfoList = map.get(CLUSTER.getModelId());
        if (CollectionUtils.isEmpty(instanceInfoList)) {
            //没有集群层级时，获取dataCenter层数据
            instanceInfoList = map.get(DATACNETER.getModelId());
        }
        if (CollectionUtils.isNotEmpty(instanceInfoList)) {
            Map<Integer, MwModelInstanceCommonParam> instanceMap = instanceInfoList.stream().collect(Collectors.toMap(s -> s.getRelationInstanceId(), s -> s, (
                    value1, value2) -> {
                return value2;
            }));
            if (map != null && map.size() > 0 && CollectionUtils.isNotEmpty(vcenterInstanceInfoList)) {
                for (MwModelInstanceCommonParam instanceCommonParam : vcenterInstanceInfoList) {
                    if (instanceMap.containsKey(instanceCommonParam.getModelInstanceId())) {
                        MwModelInstanceCommonParam virInstance = instanceMap.get(instanceCommonParam.getModelInstanceId());
                        param.setModelIndex(instanceCommonParam.getModelIndex());
                        param.setVirtualName(virInstance.getModelInstanceName());
                        param.setVirtualType(VirtualizationType.getType(virInstance.getModelId()));
                        param.setModelInstanceId(instanceCommonParam.getModelInstanceId());
                        Reply reply = mwModelVirtualizationService.getVirDeviceByPieSimple(param);
                        VirtualizationMonitorInfo virtualizationMonitorInfo = (VirtualizationMonitorInfo) reply.getData();
                        virtualizationMonitorInfo.setInstanceName(instanceCommonParam.getModelInstanceName());
                        virtualizationMonitorInfo.setInstanceId(instanceCommonParam.getModelInstanceId());
                        virtualizationList.add(virtualizationMonitorInfo);
                    }
                }
            }
        }
        return virtualizationList;
    }

    @Override
    public Reply getAllCitrixListRelationInfo() throws Exception {

        List<MwModelInstanceCommonParam> citrixInstanceList = mwModelInstanceDao.selectModelInstanceInfoById(CITRIXADC.getModelId());
        Set<String> modelIndexSet = new HashSet<>();
        Set<Integer> instanceIdSet = new HashSet<>();
        for (MwModelInstanceCommonParam m : citrixInstanceList) {
            if (m != null && !Strings.isNullOrEmpty(m.getModelIndex())) {
                modelIndexSet.add(m.getModelIndex());
            }
            if (m != null && m.getModelInstanceId() != null) {
                instanceIdSet.add(m.getModelInstanceId());
            }
        }
        QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
        params.setModelIndexs(new ArrayList<>(modelIndexSet));
        params.setInstanceIds(new ArrayList<>(instanceIdSet));
        List<Map<String, Object>> ownInstanceMaps = selectInstanceInfoByIdsAndModelIndexs(params);
        //获取citrix实例数据
        List<MwModelRelationAssetsParam> relationAssetsList = JSON.parseArray(JSONObject.toJSONString(ownInstanceMaps), MwModelRelationAssetsParam.class);

        //获取citrix关联的模型index
        List<Integer> modelIdList = Arrays.asList(LB_VIRTUAL_SERVERS.getModelId(), LB_SERVICES.getModelId(), LOAD_BALANCING.getModelId(), GSLB_VIRTUAL_SERVERS.getModelId(), GSLB_SERVICES.getModelId());
        List<String> modelIndexs = mwModelManageDao.selectModelIndexsByModelIds(modelIdList);
        QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
        param1.setRelationInstanceIds(new ArrayList<>(instanceIdSet));
        param1.setModelIndexs(modelIndexs);
        //根据relationInstanceIds获取所有的citrix关联数据
        List<Map<String, Object>> listMap = selectInstanceInfoByRelationInstanceIdList(param1);

        List<MwModelCitrixInfoParam> citrixDataList = new ArrayList<>();
        citrixDataList = JSON.parseArray(JSONObject.toJSONString(listMap), MwModelCitrixInfoParam.class);
        for (MwModelCitrixInfoParam infoParam : citrixDataList) {
            infoParam.setType(ModelCitrixType.getDesc(intValueConvert(infoParam.getModelId())));
        }

        Map<String, List<MwModelCitrixInfoParam>> disListMap = citrixDataList.stream().collect(Collectors.groupingBy(s -> s.getRelationInstanceId()));
        for (MwModelRelationAssetsParam m : relationAssetsList) {
            if (disListMap != null && disListMap.size() > 0) {
                List<MwModelCitrixInfoParam> relationCitrixList = disListMap.get(m.getModelInstanceId() + "");
                m.setRelationListData(relationCitrixList);
            }
        }
        return Reply.ok(relationAssetsList);
    }


    @Override
    public Object selectByAssetsIdAndServerId(String assetsId, int monitorServerId) throws Exception {
        Object ret = null;
        if (modelAssetEnable) {
            QueryModelAssetsParam param = new QueryModelAssetsParam();
            param.setAssetsId(assetsId);
            param.setMonitorServerId(monitorServerId);
            List<ModelInstanceBaseInfoDTO> instanceBaseInfoDTOS = findModelAssets(ModelInstanceBaseInfoDTO.class, param);
            if (null != instanceBaseInfoDTOS && instanceBaseInfoDTOS.size() > 0) {
                ret = instanceBaseInfoDTOS.get(0);
            }
        } else {
            ret = mwTangibleAssetsService.selectByAssetsIdAndServerId(assetsId, monitorServerId);
        }
        return ret;
    }

    @Override
    public MwTangibleassetsDTO selectByIp(String ip) {
        MwTangibleassetsDTO mwTangibleassetsDTO = null;
        try {
            if (modelAssetEnable) {
                QueryModelOr queryModelOr = genIpCondition(ip);
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
                List<MwTangibleassetsDTO> mwTangibleassetsTables = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
                if (null != mwTangibleassetsTables && mwTangibleassetsTables.size() > 0) {
                    mwTangibleassetsDTO = mwTangibleassetsTables.get(0);
                }
            } else {
                mwTangibleassetsDTO = mwTangibleAssetsService.selectByIp(ip);
            }
        } catch (Exception e) {
            log.error("selectByIp model {}", e);
        }
        return mwTangibleassetsDTO;
    }

    @Override
    public MwTangibleassetsDTO selectByHostIdAndIp(String hostId, String ip) {
        MwTangibleassetsDTO mwTangibleassetsDTO = null;
        try {
            if (modelAssetEnable) {
                List<QueryModelParam> conditions = new ArrayList<>();
                QueryModelOr queryModelOr = genIpCondition(ip);
                conditions.add(queryModelOr);

                QueryModelEq queryModelEq = new QueryModelEq("assetsId", hostId);
                conditions.add(queryModelEq);

                QueryModelAnd queryModelAnd = new QueryModelAnd(conditions);

                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelAnd, esParam);
                List<MwTangibleassetsDTO> mwTangibleassetsTables = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
                if (null != mwTangibleassetsTables && mwTangibleassetsTables.size() > 0) {
                    mwTangibleassetsDTO = mwTangibleassetsTables.get(0);
                }
            } else {
                Map criteria = new HashMap();
                List ipList = new ArrayList();
                ipList.add(ip);
                List assetsId = new ArrayList();
                assetsId.add(hostId);

                criteria.put("ipList", ipList);
                criteria.put("assetsId", assetsId);
                Reply reply = mwTangibleAssetsService.selectListWithExtend(criteria);
                if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                    List<MwTangibleassetsDTO> list = (List) reply.getData();
                    if (list.size() > 0) {
                        mwTangibleassetsDTO = list.get(0);
                    }
                }
            }
        } catch (Exception e) {
            log.error("selectByIp model {}", e);
        }
        return mwTangibleassetsDTO;
    }

    @Override
    public List<String> getAssetsNameByIp(String ip) {
        List<String> ret = new ArrayList<>();
        try {
            if (modelAssetEnable) {
                QueryModelOr queryModelOr = genIpCondition(ip);
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
                List<MwTangibleassetsDTO> mwTangibleassetsTables = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
                if (null != mwTangibleassetsTables) {
                    ret = mwTangibleassetsTables.stream().map(MwTangibleassetsDTO::getAssetsName).collect(Collectors.toList());
                }
            } else {
                ret = mwTangibleAssetsService.getAssetsNameByIp(ip);
            }
        } catch (Exception e) {
            log.error("getAssetsNameByIp {}", e);
        }

        return ret;
    }

    @Override
    public List<IpAssetsNameDTO> getAssetsNameByIps(List<String> ips) {
        List<IpAssetsNameDTO> ret = new ArrayList<>();
        try {
            if (modelAssetEnable) {
                List<QueryModelEq> conditions = new ArrayList<>();
                for (String ip : ips) {
                    QueryModelEq queryModelEq = new QueryModelEq("inBandIp", ip);
                    conditions.add(queryModelEq);
                    queryModelEq = new QueryModelEq("outBandIp", ip);
                    conditions.add(queryModelEq);
                }
                QueryModelOr queryModelOr = new QueryModelOr(conditions);
                queryModelOr.setDataList(conditions);
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
                for (Map<String, Object> map : modelAssets) {
                    IpAssetsNameDTO ipAssetsNameDTO = new IpAssetsNameDTO();
                    try {
                        Object assetsId = map.get("assetsId");
                        Object inBandIp = map.get("inBandIp");
                        Object instanceName = map.get("instanceName");

                        if (null != assetsId) {
                            ipAssetsNameDTO.setAssetId(assetsId.toString());
                        }

                        if (null != inBandIp) {
                            ipAssetsNameDTO.setInBandIp(inBandIp.toString());
                        }

                        if (null != instanceName) {
                            ipAssetsNameDTO.setAssetsName(instanceName.toString());
                        }
                        ret.add(ipAssetsNameDTO);
                    } catch (Exception e) {
                        log.error("noAssetsName {}", e);
                    }

                }
                /*ret = MwModelUtils.convertEsData(IpAssetsNameDTO.class, modelAssets);*/
            } else {
                ret = mwTangibleAssetsService.getAssetsNameByIps(ips);
            }
        } catch (Exception e) {
            log.info("getAssetsNameByIps {}", e);
        }
        return ret;
    }

    private QueryModelOr genIpCondition(String ip) {
        List<QueryModelEq> conditions = new ArrayList<>();
        QueryModelEq queryModelEq = new QueryModelEq("inBandIp", ip);
        conditions.add(queryModelEq);

        queryModelEq = new QueryModelEq("outBandIp", ip);
        conditions.add(queryModelEq);

        QueryModelOr queryModelOr = new QueryModelOr(conditions);
        queryModelOr.setDataList(conditions);
        return queryModelOr;
    }

    @Override
    public Reply selectById(String id) {
        Reply reply = null;
        MwTangibleassetsDTO mwTangibleassetsDTO = null;
        try {
            if (modelAssetEnable) {
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                QueryModelEq queryModelEq = new QueryModelEq(INSTANCE_ID_KEY, Integer.parseInt(id));
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelEq, esParam);
                List<MwTangibleassetsDTO> mwTangibleassetsTables = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
                if (null != mwTangibleassetsTables && mwTangibleassetsTables.size() > 0) {
                    mwTangibleassetsDTO = mwTangibleassetsTables.get(0);
                }
            } else {
                Map map = new HashMap();
                List list = new ArrayList();
                list.add(id);
                map.put("ids", list);
                reply = mwTangibleAssetsService.selectListWithExtend(map);
                if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                    List<MwTangibleassetsDTO> datas = (List) reply.getData();
                    if (CollectionUtils.isNotEmpty(datas)) {
                        mwTangibleassetsDTO = datas.get(0);
                    }

                }
            }
            MwTangibleassetsByIdDTO mdto = null;
            if (mwTangibleassetsDTO != null) {
                mdto = doSelectById(mwTangibleassetsDTO);
            }

            reply = Reply.ok(mdto);
        } catch (Exception e) {
            log.error("selectById", e);
        }

        return reply;
    }

    @Override
    public Reply selectById(String id, Boolean isQueryAssetsState) {
        Reply reply = null;
        MwTangibleassetsDTO mwTangibleassetsDTO = null;
        try {
            if (modelAssetEnable) {
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                QueryModelEq queryModelEq = new QueryModelEq(INSTANCE_ID_KEY, Integer.parseInt(id));
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelEq, esParam);
                List<MwTangibleassetsDTO> mwTangibleassetsTables = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
                if (null != mwTangibleassetsTables && mwTangibleassetsTables.size() > 0) {
                    mwTangibleassetsDTO = mwTangibleassetsTables.get(0);
                }
            } else {
                Map map = new HashMap();
                List list = new ArrayList();
                list.add(id);
                map.put("ids", list);
                reply = mwTangibleAssetsService.selectListWithExtend(map);
                if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                    List<MwTangibleassetsDTO> datas = (List) reply.getData();
                    if (CollectionUtils.isNotEmpty(datas)) {
                        mwTangibleassetsDTO = datas.get(0);
                    }

                }
            }
            MwTangibleassetsByIdDTO mdto = null;
            if (mwTangibleassetsDTO != null) {
                mwTangibleassetsDTO.setIsQueryAssetsState(isQueryAssetsState);
                mdto = doSelectById(mwTangibleassetsDTO);
            }

            reply = Reply.ok(mdto);
        } catch (Exception e) {
            log.error("selectById", e);
        }

        return reply;
    }


    @Override
    public MwTangibleassetsByIdDTO doSelectById(MwTangibleassetsDTO mwTangAsset) throws Exception {
        //查询资产的标签
        List<MwAssetsLabelDTO> labelBoard = mwLabelCommonServcie.getLabelBoard(mwTangAsset.getId(), DataType.ASSETS.getName());
        List<MwAssetsLabelDTO> labelDTOS = new ArrayList<>();
        //组合标签多选
        getAssetsLabelAndComposeValue(labelBoard, labelDTOS);
        if (null != labelDTOS && labelDTOS.size() > 0) {
            mwTangAsset.setAssetsLabel(labelDTOS);
        } else {
            mwTangAsset.setAssetsLabel(labelBoard);
        }
        Boolean flag = (mwTangAsset.getPollingEngine() != null && StringUtils.isNotEmpty(mwTangAsset.getPollingEngine()));
        mwTangAsset.setPollingMode(flag ? CUSTOM : LOCAL);
        MwTangibleassetsByIdDTO mtDtos = CopyUtils.copy(MwTangibleassetsByIdDTO.class, mwTangAsset);
        // usergroup重新赋值使页面可以显示
        List<Integer> groupIds = new ArrayList<>();
        if (null != mwTangAsset.getGroup()) {
            mwTangAsset.getGroup().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            mtDtos.setGroupIdsMap(mwTangAsset.getGroup());
        }
        mtDtos.setGroupIds(groupIds.size() == 0 ? mwTangAsset.getModelViewGroupIds() : groupIds);
        // department重新赋值使页面可以显示
        List<List<Integer>> orgNodes = new ArrayList<>();
        if (mwTangAsset.getDepartment() != null) {
            mwTangAsset.getDepartment().forEach(department -> {
                        List<Integer> orgIds = new ArrayList<>();
                        List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                        nodes.forEach(node -> {
                            if (!"".equals(node))
                                orgIds.add(intValueConvert(node));
                        });
                        orgNodes.add(orgIds);
                    }
            );
        }
        mtDtos.setOrgIds(orgNodes.size() == 0 ? mwTangAsset.getModelViewOrgIds() : orgNodes);
        // user重新赋值
        List<Integer> userIds = new ArrayList<>();
        if (mwTangAsset.getPrincipal() != null) {
            mwTangAsset.getPrincipal().forEach(
                    userDTO -> {
                        if (userDTO != null) {
                            userIds.add(userDTO.getUserId());
                        }
                    });
            mtDtos.setPrincipalMap(mwTangAsset.getPrincipal());
        }
        mtDtos.setPrincipal(userIds.size() == 0 ? mwTangAsset.getModelViewUserIds() : userIds);
        //gengjb 查询资产状态
        Integer monitorServerId = mtDtos.getMonitorServerId();
        if (null == monitorServerId && 0 == monitorServerId.intValue()) {
            mtDtos.setItemAssetsStatus("NORMAL");
        } else {
            if (mwTangAsset.getIsQueryAssetsState() == null || mwTangAsset.getIsQueryAssetsState()) {
                String dtosAssetsId = mtDtos.getAssetsId();
                //加资产健康状态
                MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(monitorServerId, ZabbixItemConstant.NEW_ASSETS_STATUS, dtosAssetsId);
                if (statusData != null && !statusData.isFail()) {
                    JsonNode jsonNode = (JsonNode) statusData.getData();
                    if (jsonNode.size() > 0) {
                        for (JsonNode node : jsonNode) {
                            Integer lastvalue = node.get("lastvalue").asInt();
                            String name = node.get("name").asText();
                            if ((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)) {
                                String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                mtDtos.setItemAssetsStatus(status);
                            }
                        }
                        if (StringUtils.isBlank(mtDtos.getItemAssetsStatus())) {
                            for (JsonNode node : jsonNode) {
                                Integer lastvalue = node.get("lastvalue").asInt();
                                String name = node.get("name").asText();
                                if ((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)) continue;
                                String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                mtDtos.setItemAssetsStatus(status);
                            }
                        }
                    }
                }
            } else {
                mtDtos.setItemAssetsStatus(UNKNOWN);
            }
        }
        return mtDtos;
    }

    @Override
    public List<MwTangibleassetsTable> fuzzySearch(String search, Boolean disableWildcard) {
        List<MwTangibleassetsTable> ret = new ArrayList<>();
        try {
            if (modelAssetEnable) {
                List<QueryModelParam> params = new ArrayList<>();

                if (disableWildcard) {
                    QueryModelEq queryModelEq = new QueryModelEq("inBandIp", search);
                    params.add(queryModelEq);
                    queryModelEq = new QueryModelEq(MwModelViewCommonService.INSTANCE_NAME_KEY, search);
                    params.add(queryModelEq);
                } else {
                    QueryModelWildcard queryModelWildcard = new QueryModelWildcard("inBandIp", search);
                    params.add(queryModelWildcard);
                    queryModelWildcard = new QueryModelWildcard(MwModelViewCommonService.INSTANCE_NAME_KEY, search);
                    params.add(queryModelWildcard);
                }

                QueryModelOr queryModelOr = new QueryModelOr(params);
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
                ret = MwModelUtils.convertEsData(MwTangibleassetsTable.class, modelAssets);
            } else {
                ret = mwTangibleAssetsService.selectBySrecah(search, disableWildcard);
            }
        } catch (Exception e) {
            log.error("fuzzySearch {}", e);
        }
        return ret;
    }

    @Override
    public Reply selectVXLanAssetsList() {
        Reply reply = null;
        try {
            if (modelAssetEnable) {
                QueryInstanceModelParam esParam = new QueryInstanceModelParam();
                QueryModelExist queryModelExist = new QueryModelExist("vxlanUserName");
                List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelExist, esParam);
                List<MwTangibleassetsTable> ret = MwModelUtils.convertEsData(MwTangibleassetsTable.class, modelAssets);
                reply = Reply.ok(ret);
            } else {
                reply = mwTangibleAssetsService.selectVXLanAssetsList();
            }
        } catch (Exception e) {
            log.error("selectVXLanAssetsList", e);
        }
        return reply;
    }

    /**
     * 查询标签并组合参数
     *
     * @param labelBoard
     * @param labelDTOS
     */
    private void getAssetsLabelAndComposeValue
    (List<MwAssetsLabelDTO> labelBoard, List<MwAssetsLabelDTO> labelDTOS) {
        Map<String, MwAssetsLabelDTO> map = new HashMap<>();
        for (MwAssetsLabelDTO mwAssetsLabelDTO : labelBoard) {
            Integer id = mwAssetsLabelDTO.getId();
            String inputFormat = mwAssetsLabelDTO.getInputFormat();
            if (!"3".equals(inputFormat)) {
                labelDTOS.add(mwAssetsLabelDTO);
                continue;
            }
            if (!map.containsKey(id + inputFormat) || map.get(id + inputFormat) == null) {
                DropdownDTO dropdownDTO = new DropdownDTO();
                dropdownDTO.setDropId(mwAssetsLabelDTO.getDropTagboard());
                dropdownDTO.setDropKey(mwAssetsLabelDTO.getDropKey());
                dropdownDTO.setDropValue(mwAssetsLabelDTO.getDropValue());
                List<DropdownDTO> dropDownS = mwAssetsLabelDTO.getDropDowns();
                if (dropDownS == null) {
                    dropDownS = new ArrayList<>();
                }
                dropDownS.add(dropdownDTO);
                mwAssetsLabelDTO.setDropDowns(dropDownS);
                map.put(id + inputFormat, mwAssetsLabelDTO);
                continue;

            }
            if (map.containsKey(id + inputFormat) && map.get(id + inputFormat) != null) {
                MwAssetsLabelDTO labelDTO = map.get(id + inputFormat);
                DropdownDTO dropdownDTO = new DropdownDTO();
                dropdownDTO.setDropId(mwAssetsLabelDTO.getDropTagboard());
                dropdownDTO.setDropKey(mwAssetsLabelDTO.getDropKey());
                dropdownDTO.setDropValue(mwAssetsLabelDTO.getDropValue());
                List<DropdownDTO> dropDownS = labelDTO.getDropDowns();
                if (dropDownS == null) {
                    dropDownS = new ArrayList<>();
                }
                dropDownS.add(dropdownDTO);
                mwAssetsLabelDTO.setDropDowns(dropDownS);
                map.put(id + inputFormat, mwAssetsLabelDTO);
                continue;
            }
        }
        if (!map.isEmpty()) {
            for (Map.Entry<String, MwAssetsLabelDTO> entry : map.entrySet()) {
                MwAssetsLabelDTO value = entry.getValue();
                labelDTOS.add(value);
            }
        }
    }

    /**
     * 实例资产取消纳管
     */
    public Reply cancelManageAssetsToZabbix(List<CancelZabbixAssetsParam> params) {
        if (CollectionUtils.isNotEmpty(params)) {
            //删除对应zabbix资产
            mwModelAssetsDiscoveryService.deleteAssetsToZabbix(params);
            List<MwModelInstanceParam> paramList = new ArrayList<>();
            List<String> cleanFields = Arrays.asList(ASSETS_ID, TPSERVERHOSTNAME, IS_MANAGE, MONITOR_FLAG, MONITOR_SERVER_ID);
            for (CancelZabbixAssetsParam param : params) {
                for (String field : cleanFields) {
                    MwModelInstanceParam instanceParam = new MwModelInstanceParam();
                    //取消纳管时，需要清除的ES字段数据 资产Id：assetsId
                    instanceParam.setPropertiesIndex(field);
                    instanceParam.setPropertiesType(1);
                    if (IS_MANAGE.equals(field) || MONITOR_FLAG.equals(field)) {
                        instanceParam.setPropertiesType(17);
                    }
                    instanceParam.setModelIndex(param.getModelIndex());
                    instanceParam.setEsId(param.getEsId());
                    paramList.add(instanceParam);
                }
            }
            //清空对应资产字段
            mwModelInstanceService.cleanFieldValueToEs(paramList);
        }
        return Reply.ok();
    }

    /**
     * 查询业务系统实例和外部关联字段id
     *
     * @return
     */
    @Override
    public List<MwModelInstanceCommonParam> getModelSystemIndexIdAndInstanceInfo(Integer modelId) {
        //获取业务系统的indexId
        List<MwModelInstanceCommonParam> list = mwModelInstanceDao.selectModelInstanceInfoById(modelId);
        ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(intValueConvert(modelSystemParentModelId));
        if (modelInfo != null) {
            Map<String, String> m = modelInfo.getPropertyInfos().stream().filter(s -> s.getPropertiesTypeId() == MULTIPLE_RELATION.getCode() || s.getPropertiesTypeId() == SINGLE_RELATION.getCode()).collect(Collectors.toMap(s -> s.getRelationModelIndex(), s -> s.getIndexId(), (
                    value1, value2) -> {
                return value2;
            }));
            for (MwModelInstanceCommonParam instanceParam : list) {
                String propertiesIndexId = m.get(instanceParam.getModelIndex());
                instanceParam.setPropertiesIndexId(propertiesIndexId);
            }
        }
        return list;
    }


    /**
     * 查询业务系统业务分类字段信息
     *
     * @return
     */
    @Override
    public Reply getModelSystemAndClassify() {
        ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(intValueConvert(modelSystemParentModelId));
        List<PropertyInfo> list = new ArrayList<>();
        if (modelInfo != null && CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())) {
            list = modelInfo.getPropertyInfos().stream().filter(s -> (s.getIsShow() != null && s.getIsShow()) && (s.getPropertiesTypeId() == MONITORSERVER_RELATION.getCode() || s.getPropertiesTypeId() == MULTIPLE_RELATION.getCode() || s.getPropertiesTypeId() == SINGLE_RELATION.getCode())).collect(Collectors.toList());
        }
        return Reply.ok(list);
    }

    /**
     * 查询业务系统分组下的所有实例信息
     *
     * @return
     */
    @Override
    public Reply getSystemAndClassifyInstanceInfo() {
        List<PropertyInfo> list = new ArrayList<>();
        ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(intValueConvert(modelSystemParentModelId));
        if (modelInfo != null) {
            list = modelInfo.getPropertyInfos().stream().filter(s -> s.getPropertiesTypeId() == MULTIPLE_RELATION.getCode() || s.getPropertiesTypeId() == SINGLE_RELATION.getCode()).collect(Collectors.toList());
        }
        List<String> relationIndexs = list.stream().map(PropertyInfo::getRelationModelIndex).collect(Collectors.toList());
        List<MwModelInstanceParam> lists = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(relationIndexs)) {
            lists = mwModelViewDao.getSystemAndClassifyInstanceInfo(relationIndexs);
        }
        List<MwModelInstanceParam> instanceParamList = new ArrayList<>();
        for (PropertyInfo propertyInfo : list) {
            for (MwModelInstanceParam instanceParam : lists) {
                MwModelInstanceParam params = new MwModelInstanceParam();
                BeanUtils.copyProperties(instanceParam, params);
                if (propertyInfo.getRelationModelIndex().equals(instanceParam.getModelIndex())) {
                    params.setPropertiesIndex(propertyInfo.getIndexId());
                    instanceParamList.add(params);
                }
            }
        }
        Map<String, List<MwModelInstanceParam>> map = instanceParamList.stream().collect(Collectors.groupingBy(MwModelInstanceParam::getPropertiesIndex));
        return Reply.ok(map);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            if (modelAssetEnable && MWAlertAssetsParam.tangibleassetsDTOMap.size() == 0) {
                synchronized (MWAlertAssetsParam.tangibleassetsDTOMap) {
                    if (MWAlertAssetsParam.tangibleassetsDTOMap.size() == 0) {
                        Reply reply = findTopoModelAssets(MwTangibleassetsDTO.class);
                        if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                            List<MwTangibleassetsDTO> tangibleassetsDTOS = (List) reply.getData();
                            for (MwTangibleassetsDTO dto : tangibleassetsDTOS) {
                                addCacheAssetInfo(dto);
                            }
                        }
                    }
                }
                ;
            }
        } catch (Exception e) {
            log.error("查询资产错误:{}", e);
        }
    }

    public void addCacheAssetInfo(MwTangibleassetsDTO dto) {
        String key = dto.getAssetsId() + SEP + dto.getInBandIp();
        assetIdKeyMap.put(dto.getId(), key);
        MWAlertAssetsParam.tangibleassetsDTOMap.put(key, dto);
        monitorServerSet.add(dto.getMonitorServerId());
    }

    public void removeCacheAssetInfo(String id) {
        String key = assetIdKeyMap.get(id);
        if (null != key) {
            MWAlertAssetsParam.tangibleassetsDTOMap.remove(key);
        }
    }

    @Override
    public void updateMonitorServerSet() {
        try {
            QueryInstanceModelParam esParam = new QueryInstanceModelParam();
            QueryModelExist queryModelExist = new QueryModelExist("monitorServerId");
            List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelExist, esParam);
            List<MwAssetsIdsDTO> mwAssetsIdsDTOS = MwModelUtils.convertEsData(MwAssetsIdsDTO.class, modelAssets);
            ;
            Set newSet = new CopyOnWriteArraySet<>();
            for (MwAssetsIdsDTO mwAssetsIdsDTO : mwAssetsIdsDTOS) {
                newSet.add(mwAssetsIdsDTO.getMonitorServerId());
            }
            monitorServerSet = newSet;
        } catch (Exception e) {
            log.error("updateMonitorServerSet {}", e);
        }
    }

    @Override
    public Reply findAllMonitorServerId() {
        if (modelAssetEnable) {
            return Reply.ok(monitorServerSet);
        }
        return mwTangibleAssetsService.findAllMonitorServerId();
    }

    @Override
    public Reply getTemplateListByMode(AddUpdateTangAssetsParam aParam) {
        Reply reply = null;
        if (modelAssetEnable) {
            Map criteria = new HashMap<>();

            criteria.put("serverId", aParam.getMonitorServerId());
            //根据添加模式判断是否需要查询所有模板
            if ((aParam.getAddPattern() == null || aParam.getAddPattern() != 1)
                    && null != aParam.getMonitorModeName() && StringUtils.isNotEmpty(aParam.getMonitorModeName())) {
                List<Integer> monitorModes = new ArrayList<>();
                criteria.put("monitorModes", monitorModes);
                RuleType ruleType = getInfoByName(aParam.getMonitorModeName());
                monitorModes.add(ruleType.getMonitorMode());
            }

            List<MwModelTemplateDTO> data = mwModelTemplateDao.selectListByModel(criteria);
            List<MwAssetsTemplateDTO> mwAssetsTemplateDTOS = new ArrayList<>();
            if (null != data) {
                for (MwModelTemplateDTO mwModelTemplateDTO : data) {
                    MwAssetsTemplateDTO mwAssetsTemplateDTO = new MwAssetsTemplateDTO();
                    BeanUtils.copyProperties(mwModelTemplateDTO, mwAssetsTemplateDTO);
                    mwAssetsTemplateDTOS.add(mwAssetsTemplateDTO);
                }
            }
            reply = Reply.ok(mwAssetsTemplateDTOS);
        } else {
            reply = mwTangibleAssetsService.getTemplateListByMode(aParam);
        }
        return reply;
    }

    @Override
    public Reply insertAssets(AddUpdateTangAssetsParam param, boolean isbatch) throws Throwable {
        if (modelAssetEnable) {
            return mwModelAssetsDiscoveryService.insertAssetsByCommon(param);
        }
        return mwTangibleAssetsService.insertAssets(param, isbatch);
    }

    @Override
    public Reply deleteNetworkLinkAsset(Object id, AssetsParam targetAssetsParam) {
        Reply reply = null;
        if (id instanceof ModelInstanceBaseInfoDTO) {
            ModelInstanceBaseInfoDTO modelInstanceBaseInfoDTO = (ModelInstanceBaseInfoDTO) id;
            DeleteModelInstanceParam deleteModelInstanceParam = new DeleteModelInstanceParam();
            deleteModelInstanceParam.setModelIndex(modelInstanceBaseInfoDTO.getModelIndex());

            List<Integer> list = new ArrayList();
            list.add(modelInstanceBaseInfoDTO.getInstanceId());
            deleteModelInstanceParam.setInstanceIds(list);
            reply = modelSever.deleteModelInstance(deleteModelInstanceParam, 0);
        }

        if (id instanceof MwTangibleassetsDTO) {
            MwTangibleassetsDTO mwTangibleassetsDTO = (MwTangibleassetsDTO) id;
            DeleteTangAssetsID deleteTangAssetsID = new DeleteTangAssetsID();
            deleteTangAssetsID.setId(mwTangibleassetsDTO.getId());
            deleteTangAssetsID.setPollingEngine(mwTangibleassetsDTO.getPollingEngine());
            deleteTangAssetsID.setMonitorServerId(targetAssetsParam.getMonitorServerId());
            deleteTangAssetsID.setAssetsId(targetAssetsParam.getAssetsId());
            reply = mwTangibleAssetsService.deleteAssets(Arrays.asList(deleteTangAssetsID));
        }
        return reply;
    }

    @Override
    public AssetsDto getAssetsById(String assetsId, Integer monitorServerId) throws Exception {
        AssetsDto assets = null;
        if (modelAssetEnable) {
            QueryModelAssetsParam param = new QueryModelAssetsParam();
            param.setMonitorServerId(monitorServerId);
            param.setAssetsId(assetsId);
            param.setSkipDataPermission(true);
            List<MwTangibleassetsDTO> datas = findModelAssets(MwTangibleassetsDTO.class, param);
            if (null != datas && datas.size() > 0) {
                MwTangibleassetsDTO mwTangibleassetsDTO = datas.get(0);
                mwTangibleassetsDTO.setAssetsName(mwTangibleassetsDTO.getInstanceName());
                assets = new AssetsDto();
                assets.extractFrom(mwTangibleassetsDTO);
                ModelDetailParam modelDetailParam = new ModelDetailParam();
                modelDetailParam.extractFrom(mwTangibleassetsDTO);
                assets.setUrl(modelJumpUrl);
                assets.setParam(ListMapObjUtils.beanToMap(modelDetailParam));
            }
        } else {
            Map map = new HashMap();
            List list = new ArrayList();
            list.add(assetsId);
            map.put("assetsId", list);
            map.put("monitorServerId", monitorServerId);
            Reply reply = mwTangibleAssetsService.selectListWithExtend(map);
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                List<MwTangibleassetsDTO> datas = (List) reply.getData();
                if (null != datas && datas.size() > 0) {
                    MwTangibleassetsDTO mwTangibleassetsDTO = datas.get(0);
                    assets = new AssetsDto();
                    assets.extractFrom(mwTangibleassetsDTO);
                    TangibleDetailParam tangibleDetailParam = new TangibleDetailParam();
                    tangibleDetailParam.extractFrom(mwTangibleassetsDTO);
                    assets.setUrl(tangibleJumpUrl);
                    assets.setParam(ListMapObjUtils.beanToMap(tangibleDetailParam));
                }
            }
        }
        return assets;
    }

    @Override
    public List<AssetsDto> getAssetsByIds(List<String> assetsIds) throws Exception {
        List<AssetsDto> ret = new ArrayList<>();
        List<MwTangibleassetsDTO> datas = null;
        if (modelAssetEnable) {
            List<QueryModelParam> params = new ArrayList<>();

            for (String assetId : assetsIds) {
                QueryModelEq queryModelEq = new QueryModelEq("assetsId", assetId);
                params.add(queryModelEq);
            }

            QueryModelOr queryModelOr = new QueryModelOr(params);
            QueryInstanceModelParam esParam = new QueryInstanceModelParam();
            List<Map<String, Object>> modelAssets = getModelListInfoByCommonQuery(queryModelOr, esParam);
            datas = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
        } else {
            Map map = new HashMap();
            map.put("assetsId", assetsIds);
            Reply reply = mwTangibleAssetsService.selectListWithExtend(map);
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                datas = (List) reply.getData();

            }
        }
        if (null != datas) {
            for (MwTangibleassetsDTO data : datas) {
                if (data.getMonitorFlag() == null || !data.getMonitorFlag()) {
                    continue;
                }
                AssetsDto assets = new AssetsDto();
                assets.extractFrom(data);
                if (modelAssetEnable) {
                    ModelDetailParam modelDetailParam = new ModelDetailParam();
                    modelDetailParam.extractFrom(data);
                    assets.setUrl(modelJumpUrl);
                    assets.setParam(ListMapObjUtils.beanToMap(modelDetailParam));
                } else {
                    TangibleDetailParam tangibleDetailParam = new TangibleDetailParam();
                    tangibleDetailParam.extractFrom(data);
                    assets.setUrl(tangibleJumpUrl);
                    assets.setParam(ListMapObjUtils.beanToMap(tangibleDetailParam));
                }
                ret.add(assets);
            }

        }
        return ret;
    }

    @Override
    public Map<Integer, AssetTypeIconDTO> selectAllAssetsTypeIcon() {
        Map<Integer, AssetTypeIconDTO> map = null;
        if (modelAssetEnable) {
            List<AssetTypeIconDTO> assetTypeIconDTOS = mwModelViewDao.selectAllAssetsTypeIcon();
            if (null != assetTypeIconDTOS) {
                map = assetTypeIconDTOS.stream().collect(Collectors.toMap(AssetTypeIconDTO::getId, Function.identity()));
            }
        } else {
            map = mwTangibleAssetsService.selectAllAssetsTypeIcon();
        }
        return map;
    }

    @Override
    public Reply batchShiftPowerByUser(InstanceShiftPowerParam shiftParam) {
        try {
            QueryInstanceModelParam instanceParams = new QueryInstanceModelParam();
            instanceParams.setSkipDataPermission(shiftParam.getSkipDataPermission());
            getInstanceListData(instanceParams);
            List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
            AddModelInstancePropertiesParam instancePropertiesParam = new AddModelInstancePropertiesParam();
            instancePropertiesParam.setPropertiesIndexId(USER_IDS);
            instancePropertiesParam.setPropertiesValue(shiftParam.getBeforeUserId() + "");
            instancePropertiesParam.setPropertiesType(ModelPropertiesType.USER.getCode());
            propertiesList.add(instancePropertiesParam);
            instanceParams.setPropertiesList(propertiesList);
            instanceParams.setFieldList(Arrays.asList(USER_IDS, MODEL_INDEX, ESID, INSTANCE_ID_KEY));
            instanceParams.setPageSize(pageSize);
            //获取es中所有关于指定用户负责人的数据
            List<Map<String, Object>> modelListMap = mwModelInstanceService.getInstanceInfoByPropertiesValue(instanceParams);
            List<InstanceShiftPowerDto> instanceShiftPowerDto = JSON.parseArray(JSONObject.toJSONString(modelListMap), InstanceShiftPowerDto.class);
            List<AddAndUpdateModelInstanceParam> addInstanceParams = new ArrayList<>();
            for (InstanceShiftPowerDto dto : instanceShiftPowerDto) {
                List<AddModelInstancePropertiesParam> propertiesInsertList = new ArrayList<>();
                AddAndUpdateModelInstanceParam addParam = new AddAndUpdateModelInstanceParam();
                AddModelInstancePropertiesParam propertiesInsertParam = new AddModelInstancePropertiesParam();
                List<Integer> userList = new ArrayList(dto.getUserIds());
                //负责人数据替换
                Collections.replaceAll(userList, shiftParam.getBeforeUserId(), shiftParam.getAfterUserId());
                userList = userList.stream().distinct().collect(Collectors.toList());
                propertiesInsertParam.setPropertiesIndexId(USER_IDS);
                propertiesInsertParam.setPropertiesValue(JSONArray.toJSONString(userList));
                propertiesInsertParam.setPropertiesType(ModelPropertiesType.USER.getCode());
                propertiesInsertList.add(propertiesInsertParam);
                addParam.setPropertiesList(propertiesInsertList);
                addParam.setModelIndex(dto.getModelIndex());
                addParam.setEsId(dto.getEsId());
                addInstanceParams.add(addParam);
            }
            //更新es数据
            mwModelInstanceService.batchUpdateModelInstance(addInstanceParams);
        } catch (Exception e) {
            return Reply.fail(500, "Es更新负责人权限数据失败");
        }
        return Reply.ok("Es更新负责人权限数据成功");
    }

    /**
     * 获取所有虚拟化数据
     *
     * @return
     */
    @Override
    public List<MwModelVirtualDataParam> getAllVirtualDeviceData() {
        List<MwModelInstanceParam> virInstanceList = mwModelViewDao.getGroupInstanceInfoByModelId(VCENTER.getModelId());
        Set<String> modelIndexSet = new HashSet<>();
        Set<Integer> instanceIdSet = new HashSet<>();
        for (MwModelInstanceParam m : virInstanceList) {
            if (m != null && !Strings.isNullOrEmpty(m.getModelIndex())) {
                modelIndexSet.add(m.getModelIndex());
            }
            if (m != null && m.getInstanceId() != null) {
                instanceIdSet.add(m.getInstanceId());
            }
        }

        QueryInstanceModelParam instanceModelParam = new QueryInstanceModelParam();
        instanceModelParam.setModelIndexs(new ArrayList<>(modelIndexSet));
        instanceModelParam.setInstanceIds(new ArrayList<>(instanceIdSet));
        instanceModelParam.setPageSize(pageSize);
        //获取es中所有VCenter的关联设备（集群主机虚拟机等）
        Map<String, Object> mapInfo = getModelListInfoByBase(instanceModelParam);
        List<MwModelVirtualDataParam> virtualDataList = new ArrayList<>();
        if (mapInfo != null && mapInfo.get("data") != null) {
            List<Map<String, Object>> modelListMap = (List<Map<String, Object>>) mapInfo.get("data");
            virtualDataList = JSON.parseArray(JSONObject.toJSONString(modelListMap), MwModelVirtualDataParam.class);
        }

        //获取所有监控服务器信息
        List<MwModelViewTreeDTO> serverNameList = mwModelExportDao.getServerNameByExport();
        Map<String, String> serverMap = new HashMap();
        for (MwModelViewTreeDTO dto : serverNameList) {
            serverMap.put(dto.getId(), dto.getName());
        }
        if (serverMap != null && serverMap.size() > 0) {
            for (MwModelVirtualDataParam virtualParam : virtualDataList) {
                virtualParam.setMonitorServerName(serverMap.get(virtualParam.getMonitorServerId().toString()) != null ? serverMap.get(virtualParam.getMonitorServerId().toString()) : "");
            }
        }
        virtualDataList = virtualDataList.stream().filter(s -> !Strings.isNullOrEmpty(s.getHostId())).collect(Collectors.toList());

        return virtualDataList;
    }


    /**
     * 获取模型资产主机数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getModelAssetsHostData(QueryInstanceModelParam param) {
        try {
            List<MwModelAssetsDTO> mwModelAssetsDTOS = new ArrayList<>();
            param.setSkipDataPermission(true);
            Reply reply = getModelListInfoByView(param);
            if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS) {
                return Reply.ok(mwModelAssetsDTOS);
            }
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<Map<String, Object>> modelAssets = pageInfo.getList();
            if (CollectionUtils.isEmpty(modelAssets)) {
                return Reply.ok(mwModelAssetsDTOS);
            }
            List<MwTangibleassetsDTO> mwTangibleassetsDTOS = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, modelAssets);
            for (MwTangibleassetsDTO tangibleassetsDTO : mwTangibleassetsDTOS) {
                if (StringUtils.isBlank(tangibleassetsDTO.getAssetsId())) {
                    continue;
                }
                MwModelAssetsDTO mwModelAssetsDTO = new MwModelAssetsDTO();
                mwModelAssetsDTO.extractFrom(tangibleassetsDTO);
                mwModelAssetsDTOS.add(mwModelAssetsDTO);
            }
            return Reply.ok(mwModelAssetsDTOS);
        } catch (Throwable e) {
            log.error("查询模型资产主机数据失败", e);
            return Reply.fail("查询数据失败", e);
        }
    }

    /**
     * 实例列表数据查询
     *
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> selectInstanceInfoByRelationInstanceId(QueryRelationInstanceModelParam param) throws
            Exception {
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (param.getRelationInstanceId() != null) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
            queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(RELATION_INSTANCE_ID, param.getRelationInstanceId()));
            if (param.getQueryOwnInstancInfo() != null && param.getQueryOwnInstancInfo()) {
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, param.getInstanceId()));
            }
            queryBuilder.must(queryBuilder1);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(pageSize);
            //设置超时时间
            searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                searchRequest.indices(String.join(",", param.getModelIndexs()));
            } else {
                searchRequest.indices("mw_*");
            }
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put(ESID, searchHit.getId());
                listMap.add(sourceAsMap);
            }
        }
        return listMap;
    }

    @Override
    public List<Map<String, Object>> selectInstanceInfoByRelationInstanceIdList(QueryRelationInstanceModelParam
                                                                                        param) throws Exception {
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(param.getRelationInstanceIds())) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            //外部关联id查询
            QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(RELATION_INSTANCE_ID, param.getRelationInstanceIds());
            queryBuilder.must(queryBuilder1);
            //实例id查询
            List<Integer> instanceIds = mwModelInstanceDao.getInstanceIdsByRelationIdAndModelId(param.getRelationInstanceIds());
            if(CollectionUtils.isNotEmpty(instanceIds)){
                QueryBuilder queryBuilder2 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, instanceIds);
                queryBuilder.must(queryBuilder2);
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(pageSize);
            //设置超时时间
            searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                searchRequest.indices(String.join(",", param.getModelIndexs()));
            } else {
                searchRequest.indices("mw_*");
            }
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put(ESID, searchHit.getId());
                listMap.add(sourceAsMap);
            }
        }
        return listMap;
    }

    //    @Override
    @Override
    public  List<Map<String, Object>> selectInstanceInfoByIdsAndModelIndexs(QueryRelationInstanceModelParam param) throws Exception {
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (param.getInstanceIds() != null) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, param.getInstanceIds());
            queryBuilder.must(queryBuilder1);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(pageSize);
            //设置超时时间
            searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                searchRequest.indices(String.join(",", param.getModelIndexs()));
            } else {
                searchRequest.indices("mw_*");
            }
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put(ESID, searchHit.getId());
                listMap.add(sourceAsMap);
            }
        }
        return listMap;
    }


    /**
     * 获取设备的触发器阈值信息
     */
    @Override
    public Reply getTriggerGetHostId(QueryModelAssetsTriggerParam param) {
        PageInfo pageInfo = new PageInfo<List>();
        PageList pageList = new PageList();
        List<String> hostIdList = new ArrayList<>();
        hostIdList.add(param.getAssetsId());
        List<QueryModelTriggerInfoParam> list = new ArrayList<>();
        //判断是否是虚拟化设备
        if(VCENTER.getModelId().equals(param.getModelId()) && !Strings.isNullOrEmpty(param.getIp()) ){
            MWZabbixAPIResult hostInfoResult = mwtpServerAPI.getHostInfoByName(param.getMonitorServerId(),"<"+param.getIp()+">");
            String hostId = "";
            if (hostInfoResult.getCode() == 0) {
                JsonNode jsonNode = (JsonNode) hostInfoResult.getData();
                for (JsonNode hostInfo : jsonNode) {
                    hostId = hostInfo.get("hostid").asText();
                    hostIdList.add(hostId);
                }
            }
        }
        try {
            for(String hostId : hostIdList){
                List<String> triggerIds = new ArrayList<>();
                MWZabbixAPIResult result = mwtpServerAPI.triggerGetHostId(param.getMonitorServerId(), hostId);
                if (result.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    for (JsonNode itemData : jsonNode) {
                        triggerIds.add(itemData.get("triggerid").asText());
                    }
                }

                MWZabbixAPIResult templateIdInfo = mwtpServerAPI.getTemplateIdByHostId(param.getMonitorServerId(), hostId);
                List<String> templateList = new ArrayList<>();
                if (!templateIdInfo.isFail()) {
                    JsonNode node = (JsonNode) templateIdInfo.getData();
                    if (node.size() > 0) {
                        node.forEach(data -> {
                            JsonNode parentTemplates = (JsonNode) data.get("parentTemplates");
                            if (parentTemplates.size() > 0) {
                                for (JsonNode parentTemplate : parentTemplates) {
                                    templateList.add(parentTemplate.get("templateid").asText());
                                }
                            }
                            templateList.add(data.get("templateid").asText());
                        });
                    }
                }
                //根据模板获取宏值
                MWZabbixAPIResult templateMacros = mwtpServerAPI.getMacrosByTemplateIdList(param.getMonitorServerId(), templateList);
                Map<String, String> macroMap = new HashMap<>();
                if (!templateMacros.isFail() && ((ArrayNode) templateMacros.getData()).size() > 0) {
                    JsonNode node = (JsonNode) templateMacros.getData();
                    if (node.size() > 0) {
                        node.forEach(macro -> {
                            if (macro != null && macro.get("macro") != null && macro.get("value") != null) {
                                String macroName = macro.get("macro").asText();
                                String macroValue = macro.get("value").asText();
                                macroMap.put(macroName, macroValue);
                            }
                        });
                    }
                }
                //根据触发器Id获取Item的key_指标
                MWZabbixAPIResult itemKeyResult = mwtpServerAPI.itemKeyGetByTriggerids(param.getMonitorServerId(), triggerIds);
                Map<String, String> mapKey = new HashMap();
                if (itemKeyResult.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) itemKeyResult.getData();
                    for (JsonNode itemData : jsonNode) {
                        String key = itemData.get("key_").asText();
                        String itemid = itemData.get("itemid").asText();
                        mapKey.put(itemid, key);
                    }
                }
                if (result.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    for (JsonNode itemData : jsonNode) {
                        QueryModelTriggerInfoParam triggerInfoParam = new QueryModelTriggerInfoParam();
                        //告警触发函数ids
                        List<String> expressionIds = new ArrayList<>();
                        String expressionParameter = itemData.get("expression").asText();
                        if (!Strings.isNullOrEmpty(expressionParameter)) {
                            String[] strArr = expressionParameter.split("}");
                            if (strArr.length > 1) {
                                for (String str : strArr) {
                                    int index1 = str.indexOf("{");
                                    if (index1 != -1) {
                                        expressionIds.add(str.substring(index1 + 1));
                                    }
                                }

                            }
                        }
                        //恢复触发函数Ids
                        List<String> recoveryIds = new ArrayList<>();
                        String recoveryParameter = itemData.get("recovery_expression").asText();
                        if (!Strings.isNullOrEmpty(recoveryParameter)) {
                            String[] strArr = recoveryParameter.split("}");
                            if (strArr.length > 1) {
                                for (String str : strArr) {
                                    int index1 = str.indexOf("{");
                                    if (index1 != -1) {
                                        recoveryIds.add(str.substring(index1 + 1));
                                    }
                                }

                            }
                        }
                        //标题
                        String descriptionStr = itemData.get("description").asText();
                        String description = replaceStr(descriptionStr, macroMap);
                        triggerInfoParam.setTriggerName(description);
                        JsonNode functions = itemData.get("functions");
                        String expression = expressionParameter;
                        String recovery = recoveryParameter;
                        for (JsonNode function : functions) {
                            String functionid = function.get("functionid").asText();
                            String functionName = function.get("function").asText();
                            String itemId = function.get("itemid").asText();
                            String keyTag = "";
                            if (mapKey != null && mapKey.containsKey(itemId)) {
                                keyTag = mapKey.get(itemId);
                            }
                            //阈值表达式
                            String parameterStr = function.get("parameter").asText();
                            //阈值使用宏值替代
                            String parameter = replaceStr(parameterStr, macroMap);
                            String expreStr = param.getInstanceName() + ":" + keyTag + "." + functionName + "(" + parameter + ")";
                            if (functionid != null && expressionIds.contains(functionid)) {
                                expression = expression.replace(functionid, expreStr);
                            }
                            if (functionid != null && recoveryIds.contains(functionid)) {
                                recovery = recovery.replace(functionid, expreStr);
                            }
                        }
                        triggerInfoParam.setExpressionParameter(expression);
                        triggerInfoParam.setRecoveryParameter(recovery);

                        //告警等级
                        String prority = itemData.get("priority").asText();
                        //默认告警等级
                        if (alertLevelStr.equals(alertLevel)) {
                            if (ModelTriggerDefaultLevelType.LEVEL_ONE.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerDefaultLevelType.LEVEL_ONE.getName());
                            } else if (ModelTriggerDefaultLevelType.LEVEL_TWO.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerDefaultLevelType.LEVEL_TWO.getName());
                            } else if (ModelTriggerDefaultLevelType.LEVEL_THREE.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerDefaultLevelType.LEVEL_THREE.getName());
                            } else if (ModelTriggerDefaultLevelType.LEVEL_FOUR.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerDefaultLevelType.LEVEL_FOUR.getName());
                            } else if (ModelTriggerDefaultLevelType.LEVEL_FIVE.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerDefaultLevelType.LEVEL_FIVE.getName());
                            } else {
                                triggerInfoParam.setTriggerLevel(ModelTriggerDefaultLevelType.LEVEL_OTHER.getName());
                            }
                        } else {
                            if (ModelTriggerLevelType.LEVEL_ONE.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerLevelType.LEVEL_ONE.getName());
                            } else if (ModelTriggerLevelType.LEVEL_TWO.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerLevelType.LEVEL_TWO.getName());
                            } else if (ModelTriggerLevelType.LEVEL_THREE.getLevel().equals(prority)) {
                                triggerInfoParam.setTriggerLevel(ModelTriggerLevelType.LEVEL_THREE.getName());
                            } else {
                                triggerInfoParam.setTriggerLevel(ModelTriggerLevelType.LEVEL_OTHER.getName());
                            }
                        }


                        //状态
                        String status = itemData.get("status").asText();
                        if ("0".equals(status)) {
                            triggerInfoParam.setTriggerStatus("已启用");
                        } else {
                            triggerInfoParam.setTriggerStatus("已停用");
                        }
                        list.add(triggerInfoParam);
                    }
                }
            }
        } catch (Exception e) {
            log.error("根据主机Id获取触发器阈值失败", e);
            return Reply.fail(500, "根据主机Id获取触发器阈值失败");
        }
        pageInfo.setTotal(list.size());
        list = pageList.getList(list, param.getPageNumber(), param.getPageSize());
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    public String replaceStr(String str, Map<String, String> macroMap) {
        String repStr = str;
        if (str.indexOf("{$") != -1) {//阈值使用宏值替代
            if (macroMap != null && macroMap.size() > 0) {
                for (Map.Entry<String, String> entry : macroMap.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (str.indexOf(k) != -1) {
                        repStr = str.replace(k, v);
                    }
                }
            }
        }
        return repStr;
    }


    /**
     * 获取所有模型属性
     *
     * @return
     */
    public List<PropertyInfo> getAllPropertyInfo() {
        List<ModelInfo> modelInfoList = mwModelManageDao.selectAllModelInfo();
        List<PropertyInfo> allAllPropertyList = new ArrayList<>();
        if (null != modelInfoList) {
            for (ModelInfo modelInfo : modelInfoList) {
                if (CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())) {
                    allAllPropertyList.addAll(modelInfo.getPropertyInfos());
                }
            }
        }
        allAllPropertyList = allAllPropertyList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getIndexId()
                + ";" + s.getPropertiesTypeId()))), ArrayList::new));
        return allAllPropertyList;
    }

    @Override
    public void setModelInstanceValName() {
        //获取所有资产类型（模型分组）信息
        List<MwModelViewTreeDTO> assetsTypeList = mwModelExportDao.getAssetsTypeByExport();
        Map<String, String> assetsTypeMap = new HashMap();
        for (MwModelViewTreeDTO dto : assetsTypeList) {
            assetsTypeMap.put(dto.getId(), dto.getName());
        }
        //获取所有资产子类型（模型）信息
        List<MwModelViewTreeDTO> assetsSubTypeList = mwModelExportDao.getAssetsSubTypeByExport();
        Map<String, String> assetsSubTypeMap = new HashMap();
        for (MwModelViewTreeDTO dto : assetsSubTypeList) {
            assetsSubTypeMap.put(dto.getId(), dto.getName());
        }
        //获取所有监控服务器信息
        List<MwModelViewTreeDTO> serverNameList = mwModelExportDao.getServerNameByExport();
        Map<String, String> serverMap = new HashMap();
        for (MwModelViewTreeDTO dto : serverNameList) {
            serverMap.put(dto.getId(), dto.getName());
        }
        //获取所有监控方式信息
        List<MwModelViewTreeDTO> monitorModeList = mwModelExportDao.getMonitorModeByExport();
        Map<String, String> monitorModeMap = new HashMap();
        Map<String, String> monitorModeNameMap = new HashMap();
        for (MwModelViewTreeDTO dto : monitorModeList) {
            monitorModeMap.put(dto.getId(), dto.getName());
            monitorModeNameMap.put(dto.getName(), dto.getId());
        }
        //获取所有轮询引擎信息
        List<MwModelViewTreeDTO> proxyInfoList = mwModelExportDao.getAllProxyInfoByExport();
        Map<String, String> proxyInfoListMap = new HashMap();
        for (MwModelViewTreeDTO dto : proxyInfoList) {
            proxyInfoListMap.put(dto.getId(), dto.getName());
        }
        //获取所有的es资产
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有数据
            sourceBuilder.from(0);
            sourceBuilder.size(pageSize);
            searchRequest.source(sourceBuilder);
            searchRequest.indices("mw_*");
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
            BulkRequest request = new BulkRequest();
            for (Map<String, Object> maps : listMap) {
                Map<String, Object> insertMap = new HashMap<>();
                if (maps.get(MODEL_INDEX) != null && maps.get(ESID) != null) {
                    String modelIndex = maps.get(MODEL_INDEX).toString();
                    String esId = maps.get(ESID).toString();
                    if (maps.get(ASSETSTYPEID) != null && assetsTypeMap.containsKey(maps.get(ASSETSTYPEID).toString())) {
                        insertMap.put(ASSETSTYPENAME, assetsTypeMap.get(maps.get(ASSETSTYPEID).toString()));
                    }
                    if (maps.get(ASSETSTYPESUBID) != null && assetsSubTypeMap.containsKey(maps.get(ASSETSTYPESUBID).toString())) {
                        insertMap.put(ASSETSTYPESUBNAME, assetsSubTypeMap.get(maps.get(ASSETSTYPESUBID).toString()));
                    }
                    if (maps.get(MONITORSERVERID) != null && serverMap.containsKey(maps.get(MONITORSERVERID).toString())) {
                        insertMap.put(MONITORSERVERNAME, serverMap.get(maps.get(MONITORSERVERID).toString()));

                    }
                    if (maps.get(POLLINGENGINE) != null) {
                        if (LOCALENGINE.equals(maps.get(POLLINGENGINE).toString())) {
                            insertMap.put(POLLINGENGINENAME, LOCAL);
                        }
                        if (proxyInfoListMap.containsKey(maps.get(POLLINGENGINE).toString())) {
                            insertMap.put(POLLINGENGINENAME, proxyInfoListMap.get(maps.get(POLLINGENGINE).toString()));
                        }
                    }
                    if (maps.get(MONITORMODE) != null && monitorModeMap.containsKey(strValueConvert(maps.get(MONITORMODE)))) {
                        insertMap.put(MONITORMODENAME, monitorModeMap.get(maps.get(MONITORMODE).toString()));
                    }
                    if (maps.get(MONITORMODENAME) != null && intValueConvert(maps.get(MONITORMODE)) == 0 && monitorModeNameMap.containsKey(maps.get(MONITORMODENAME).toString())) {
                        insertMap.put(MONITORMODE, monitorModeNameMap.get(maps.get(MONITORMODENAME).toString()));
                    }
                    UpdateRequest updateRequest = new UpdateRequest(modelIndex, esId);
                    updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                    updateRequest.doc(insertMap);
                    request.add(updateRequest.upsert());
                }
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                log.error("es数据设置名称字段失败", bulkResponse);
                return;
            } else {
                log.error("es数据设置名称字段成功");
            }
        } catch (Exception e) {
            log.error("es数据设置名称字段失败", e);
        }

    }

    @Override
    public List<Map<String, Object>> getAllInstanceInfoByModelIndexs(QueryEsParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest(String.join(",", param.getModelIndexs()));
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            if (CollectionUtils.isNotEmpty(param.getParamLists())) {
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                for (QueryModelInstanceByPropertyIndexParam propertyIndexParam : param.getParamLists()) {
                    QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(propertyIndexParam.getPropertiesIndexId(), propertyIndexParam.getPropertiesValueList());
                    queryBuilder.must(queryBuilder1);
                }
                sourceBuilder.query(queryBuilder);//条件查询
            } else {
                sourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有数据
            }
            sourceBuilder.from(0);
            sourceBuilder.size(pageSize);
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            List<Integer> instanceIds = new ArrayList<>();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                if (sourceAsMap != null && sourceAsMap.size() > 0)
                    sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
        } catch (Exception e) {
            log.error("查询索引数据失败", e);
        }
        return listMap;

    }

    @Override
    public void setCabinetRelationId() {
        List<String> roomModelIndexs = mwModelManageDao.getAllRoomModelIndex();
        QueryEsParam param = new QueryEsParam();
        param.setModelIndexs(roomModelIndexs);
        //获取所有的机柜es数据
        List<Map<String, Object>> roomMapList = getAllInstanceInfoByModelIndexs(param);
        List<UpdateRelationIdParam> updateParams = new ArrayList<>();
        for (Map<String, Object> map : roomMapList) {
            if (map != null && map.size() > 0 && map.get(RELATIONSITEFLOOR.getField()) != null) {
                UpdateRelationIdParam instanceParam = new UpdateRelationIdParam();
                //获取所属楼宇，所属机房的实例id
                Integer buildInstanceId = intValueConvert(map.get(RELATIONSITEFLOOR.getField()));
                Integer instanceId = intValueConvert(map.get(INSTANCE_ID_KEY));
                String instanceName = map.get(INSTANCE_NAME_KEY) != null ? map.get(INSTANCE_NAME_KEY).toString() : "";
                instanceParam.setInstanceName(instanceName);
                instanceParam.setInstanceId(instanceId);
                instanceParam.setRelationInstanceId(buildInstanceId);
                updateParams.add(instanceParam);
            }
        }
        List<String> cabinetModelIndexs = mwModelManageDao.getAllCabinetModelIndex();
        param = new QueryEsParam();
        param.setModelIndexs(cabinetModelIndexs);
        //获取所有的机柜es数据
        List<Map<String, Object>> mapList = getAllInstanceInfoByModelIndexs(param);
        for (Map<String, Object> map : mapList) {
            if (map != null && map.size() > 0 && map.get(RELATIONSITEROOM.getField()) != null) {
                UpdateRelationIdParam instanceParam = new UpdateRelationIdParam();
                //获取所属机房，所属机柜的实例id
                Integer roomInstanceId = intValueConvert(map.get(RELATIONSITEROOM.getField()));
                Integer instanceId = intValueConvert(map.get(INSTANCE_ID_KEY));
                String instanceName = map.get(INSTANCE_NAME_KEY) != null ? map.get(INSTANCE_NAME_KEY).toString() : "";
                instanceParam.setInstanceName(instanceName);
                instanceParam.setInstanceId(instanceId);
                instanceParam.setRelationInstanceId(roomInstanceId);
                updateParams.add(instanceParam);
            }
        }
        //更新mysql机柜数据，设置机柜关联机房Id
        mwModelInstanceDao.updateCabinetRelationId(updateParams);
    }


    public List<Map<String, Object>> webMonitorConvert(List<Map<String, Object>> listMap, QueryInstanceModelParam param) {
        //web监测查询处理
        List<Map<String, Object>> collectAll = new ArrayList<>();
        List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>(param.getPropertiesList());
        List<AddModelInstancePropertiesParam> collects = propertiesList.stream().filter(s -> s.getIsTreeQuery() == null || (s.getIsTreeQuery() != null && !s.getIsTreeQuery())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collects)) {
            for (AddModelInstancePropertiesParam m : collects) {
                List<Map<String, Object>> collect = listMap.stream().filter(s -> strValueConvert(s.get(m.getPropertiesIndexId())).contains(strValueConvert(m.getPropertiesValue()))).collect(Collectors.toList());
                collectAll.addAll(collect);
            }
        } else {
            collectAll = listMap;
        }
        listMap = collectAll.stream().distinct().collect(Collectors.toList());
        if (!com.google.common.base.Strings.isNullOrEmpty(param.getSortField())) {
            //1降序
            if (intValueConvert(param.getSortType()) == 1) {
                if ("downloadSpeed".equals(param.getSortField())) {
                    listMap = listMap.stream().sorted(Comparator.comparing(s -> strValueConvert(s.get("sortDownloadSpeed")), Comparator.reverseOrder())).collect(Collectors.toList());
                } else {
                    listMap = listMap.stream().sorted(Comparator.comparing(s -> strValueConvert(s.get(param.getSortField())), Comparator.reverseOrder())).collect(Collectors.toList());
                }
            } else {//0升序
                if ("downloadSpeed".equals(param.getSortField())) {
                    listMap = listMap.stream().sorted(Comparator.comparing(s -> strValueConvert(s.get("sortDownloadSpeed")))).collect(Collectors.toList());
                } else {
                    listMap = listMap.stream().sorted(Comparator.comparing(s -> strValueConvert(s.get(param.getSortField())))).collect(Collectors.toList());
                }
            }
        }
        return listMap;
    }
}

