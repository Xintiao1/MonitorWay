package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.model.dao.MwModelExportDao;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelExportService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.service.ModelCabinetField.*;
import static cn.mw.monitor.service.model.service.ModelRoomField.COLNUM;
import static cn.mw.monitor.service.model.service.ModelRoomField.ROWNUM;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.*;

/**
 * @author qzg
 * @date 2021/12/06
 */
@Service
@Slf4j
public class MwModelExportServiceImpl implements MwModelExportService {
    private final int ERRORNUM = 20;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Resource
    private MwModelExportDao mwModelExportDao;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Resource
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelCommonServiceImpl mwModelCommonServiceImpl;
    @Value("${System.isFlag}")
    private Boolean isFlag;
    @Value("${model.instance.batchFetchNum}")
    private int insBatchFetchNum;
    @Autowired
    private MwModelInstanceServiceImplV1 mwModelInstanceServiceImplV1;
    /**
     * 每隔200条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 200;
    // 总行数
    private static int totalRows = 0;
    // 总条数
    private static int totalCells = 0;

    //es中资产类型字段
    private static final String ASSETSTYPEID = "assetsTypeId";
    //es中资产子类型字段
    private static final String ASSETSTYPESUBID = "assetsTypeSubId";
    //es中监控方式字段
    private static final String MONITORMODE = "monitorMode";
    //es中监控服务器字段
    private static final String MONITORSERVERID = "monitorServerId";
    //es中监控服务器字段
    private static final String POLLINGENGINE = "pollingEngine";

    // 错误信息接收器
    private static String errorMsg = "";

    @Override
    public Reply getFieldByFile(MultipartFile file, ModelExportDataInfoParam param) {
        ModelExportDataInfoListParam params = new ModelExportDataInfoListParam();
        try {
            String fileName = file.getOriginalFilename();
            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                //获取表头数据list
                Map map = getExcelInfo(file, true, params);
                //根据modelId获取模型属性名称
                List<ModelPropertiesExportDto> propertiesExportDtos = new ArrayList<>();
                List<ModelInfo> modelInfoList = null;
                if (isFlag && param.getType() != null && "group".equals(param.getType())) {
                    modelInfoList = mwModelManageDao.selectListWithParentAndGroup(param.getModelId());
                } else {
                    modelInfoList = mwModelManageDao.selectModelListWithParent(param.getModelId());
                }
                List<PropertyInfo> propertyInfoList = new ArrayList<>();
                String modelIndex = "";
                Integer modelView = 0;
                for (ModelInfo modelInfo : modelInfoList) {
                    if (modelInfo.getModelId().intValue() == param.getModelId().intValue()) {
                        modelIndex = modelInfo.getModelIndex();
                        modelView = modelInfo.getModelView();
                    }
                    if (null != modelInfo.getPropertyInfos()) {
                        propertyInfoList.addAll(modelInfo.getPropertyInfos());
                    }
                }
                for (PropertyInfo propertyInfo : propertyInfoList) {
                    //机房机柜下属模型判断，
                    ModelPropertiesExportDto mwCustomColByModelDTO = new ModelPropertiesExportDto();
                    mwCustomColByModelDTO.extractFrom(propertyInfo);
                    mwCustomColByModelDTO.setModelId(param.getModelId());
                    mwCustomColByModelDTO.setModelIndexId(modelIndex);
                    mwCustomColByModelDTO.setModelView(modelView);
                    propertiesExportDtos.add(mwCustomColByModelDTO);
                }
                map.put("propertiesInfo", propertiesExportDtos);
                return Reply.ok(map);
            } else {
                log.error("没有传入正确的excel文件", file);
                return Reply.fail("500", "请传入正确的excel文件");
            }
        } catch (ModelManagerException e) {
            throw new ModelManagerException("该模块新增数量已达许可数量上限！");
        } catch (Exception e) {
            log.error("fail to getFieldByFile with MultipartFile={}, cause:{}", file, e);
            return Reply.fail(500, "模型导入获取数据失败。");
        }
    }

    @Override
    public Reply exportDataInfo(MultipartFile file, ModelExportDataInfoListParam param) {
        try {
            //获取导入数据list，并插入数据
            Map map = getExcelInfo(file, false, param);
            return Reply.ok(map);
        } catch (ModelManagerException e) {
            return Reply.fail(500,"该模块新增数量已达许可数量上限！");
        } catch (Exception e) {
            log.error("fail to exportDataInfo with MultipartFile={}, cause:{}", file, e);
            return Reply.fail(500, "模型导入获取数据失败。");
        }
    }

    @Override
    public Reply getAllModelList() {
        List<MwModelManageTypeDto> list = mwModelManageDao.getAllModelList();
        List<MwModelManageTypeDto> orgTopList = new ArrayList<>();
        List<MwModelManageTypeDto> childList = new ArrayList<>();
        list.forEach(mwModelManageTypeDto -> {
            if (mwModelManageTypeDto.getDeep() == 1) {
                orgTopList.add(mwModelManageTypeDto);
            } else {
                childList.add(mwModelManageTypeDto);
            }
        });
        Set<String> modelGroupIdSet = new HashSet<>(childList.size());
        orgTopList.forEach(
                orgTop ->
                        getModelTypeChild(orgTop, childList, modelGroupIdSet)
        );
        PageInfo pageInfo = new PageInfo<>(orgTopList);
        return Reply.ok(pageInfo);
    }

    private void getModelTypeChild(MwModelManageTypeDto mwModelManageTypeDto,
                                   List<MwModelManageTypeDto> mwModelManageTypeDtoList, Set<String> modelGroupIdSet) {
        List<MwModelManageTypeDto> childList = new ArrayList<>();
        mwModelManageTypeDtoList.stream()
                // 判断是否已循环过当前对象
                .filter(child -> child.getModelGroupIdStr() != null && child.getPidStr() != null)
                .filter(child -> !modelGroupIdSet.contains(child.getModelGroupIdStr()))
                // 判断是否为父子关系
                .filter(child -> child.getPidStr().equals(mwModelManageTypeDto.getModelGroupIdStr()))
                // orgIdSet集合大小不超过mwModelManageDtoList的大小
                .filter(child -> modelGroupIdSet.size() <= mwModelManageTypeDtoList.size())
                .forEach(
                        // 放入modelIdSet,递归循环时可以跳过这个项目,提交循环效率
                        child -> {
                            modelGroupIdSet.add(child.getModelGroupIdStr());
                            //获取当前类目的子类目
                            getModelTypeChild(child, mwModelManageTypeDtoList, modelGroupIdSet);
                            childList.add(child);
                        }
                );
        mwModelManageTypeDto.addChild(childList);
    }

    /**
     * m模型实例数据导出
     *
     * @param param
     * @param request
     * @param response
     * @return
     */
    @Override
    public Reply exportForExcel(QueryModelInstanceParam param, HttpServletRequest request, HttpServletResponse response) {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        List<String> lable = param.getHeader();
        List<String> lableName = param.getHeaderName();
        List<Map<String, Object>> listMap;
        //获取模型下的属性类型
        Integer modelId = param.getModelId();
        List<ModelInfo> modelInfoList = null;

        if (isFlag && param.getModelGroupId() != null) {
            param.setModelId(null);
            listMap = mwModelInstanceServiceImplV1.getInstanceInfoByExportGroup(param);
            modelInfoList = mwModelManageDao.selectListWithParentAndGroup(param.getModelId());
        } else {
            param.setModelId(modelId);
            listMap = mwModelInstanceServiceImplV1.getInstanceInfoByExport(param);
            mwModelInstanceServiceImplV1.addRoomAndCabinetInfo(listMap);
            modelInfoList = mwModelManageDao.selectModelListWithParent(param.getModelId());
        }
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        //获取所有外部关联字段。
        for (ModelInfo modelInfo : modelInfoList) {
            if (null != modelInfo.getPropertyInfos()) {
                propertyInfoList.addAll(modelInfo.getPropertyInfos());
            }
        }
        List<String> relationIndexIds = propertyInfoList.stream().filter(s -> s.getPropertiesTypeId().intValue() == 5 || s.getPropertiesTypeId().intValue() == 4).map(PropertyInfo::getIndexId).collect(Collectors.toList());
        Set<Integer> relatioinInstanceIds = new HashSet<>();
        //循环获取所有外部关联的实例Id
        for (String relationIndexId : relationIndexIds) {
            for (Map<String, Object> m : listMap) {
                if (m.get(relationIndexId) != null) {
                    if (m.get(relationIndexId) instanceof List) {
                        relatioinInstanceIds.addAll((List) m.get(relationIndexId));
                    } else {
                        relatioinInstanceIds.add(Integer.valueOf(m.get(relationIndexId).toString()));
                    }
                }
            }
        }
        List<QueryInstanceParam> instanceParams = new ArrayList<>();
        List<List<Integer>> instanceIdGroups = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(relatioinInstanceIds)) {
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
        //将属性index和type存入map中，方便取值
        Map map = new HashMap();
        for (PropertyInfo m : propertyInfoList) {
            map.put(m.getIndexId(), m.getPropertiesTypeId());
        }

        //获取所有机构组织信息
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
        for (MwModelViewTreeDTO dto : monitorModeList) {
            monitorModeMap.put(dto.getId(), dto.getName());
        }
        //获取所有轮询引擎信息
        List<MwModelViewTreeDTO> proxyInfoList = mwModelExportDao.getAllProxyInfoByExport();
        Map<String, String> proxyInfoListMap = new HashMap();
        for (MwModelViewTreeDTO dto : proxyInfoList) {
            proxyInfoListMap.put(dto.getId(), dto.getName());
        }
        //格式转换 List<Map<String, Object>> -> List<Map>
        List<Map> mapList = new ArrayList<>();
        for (Map<String, Object> m : listMap) {
            //对es的数据循环匹配获取属性type，查询用户组，机构，用户的名称
            //es中存储的是该项的id值
            if (!m.containsKey(MONITOR_FLAG)) {//监控状态没有字段的，设置一个默认值
                m.put(MONITOR_FLAG, false);
            }
            m.forEach((k, v) -> {
                //机构/部门
                if (map.get(k) != null) {
                    //外部关联(单选)类型
                    if (String.valueOf(ModelPropertiesType.SINGLE_RELATION.getCode()).equals(map.get(k).toString())) {
                        String label = "";
                        if (v instanceof String) {
                            String code = v.toString();
                            label = relationMap.get(code);
                        }
                        m.put(k, label);
                    }
                    //外部关联(多选)类型
                    if (String.valueOf(ModelPropertiesType.MULTIPLE_RELATION.getCode()).equals(map.get(k).toString())) {
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
                    //资产类型
                    if (ASSETSTYPEID.equals(k)) {
                        String label = assetsTypeMap.get(v + "");
                        m.put(k, label);
                    }
                    //资产子类型
                    if (ASSETSTYPESUBID.equals(k)) {
                        String label = assetsSubTypeMap.get(v + "");
                        m.put(k, label);
                    }
                    //监控方式
                    if (MONITORMODE.equals(k)) {
                        String label = monitorModeMap.get(v + "");
                        m.put(k, label);
                    }
                    //监控服务器
                    if (MONITORSERVERID.equals(k)) {
                        String label = serverMap.get(v + "");
                        m.put(k, label);
                    }
                    //轮询引擎
                    if (POLLINGENGINE.equals(k)) {
                        String label = proxyInfoListMap.get(v + "");
                        if (!org.elasticsearch.common.Strings.isNullOrEmpty(label)) {
                            m.put(k, label);
                        } else {
                            m.put(k, v + "");
                        }
                    }
                    //多选枚举型
                    if (String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(map.get(k).toString())) {
                        List list = (List) v;
                        String enumNames = Joiner.on(",").join(list);
                        m.put(k, enumNames);
                    }
                    //机构
                    if (String.valueOf(ModelPropertiesType.ORG.getCode()).equals(map.get(k).toString())) {
                        List<List> list = (List) v;
                        String orgNames = "";
                        for (int i = 0, len = list.size(); i < len; i++) {
                            List<Integer> list1 = list.get(i);
                            if (list1 != null && list1.size() > 0) {
                                Integer orgId = list1.get(list1.size() - 1);
                                orgNames += orgMap.get(orgId + "") + "/";
                            }
                        }
                        if (orgNames.length() > 1) {
                            orgNames = orgNames.substring(0, orgNames.length() - 1);
                        }
                        m.put(k, orgNames);
                    }
                    //负责人
                    if (String.valueOf(ModelPropertiesType.USER.getCode()).equals(map.get(k).toString())) {
                        String userNames = "";
                        List<Integer> list = (List) v;
                        for (Integer userId : list) {
                            userNames += userMap.get(userId + "") + "/";
                        }
                        if (userNames.length() > 1) {
                            userNames = userNames.substring(0, userNames.length() - 1);
                        }
                        m.put(k, userNames);
                    }
                    //用户组
                    if (String.valueOf(ModelPropertiesType.GROUP.getCode()).equals(map.get(k).toString())) {
                        List<Integer> list = (List) v;
                        String groupNames = "";
                        for (Integer groupId : list) {
                            groupNames += groupMap.get(groupId + "") + "/";
                        }
                        if (groupNames.length() > 1) {
                            groupNames = groupNames.substring(0, groupNames.length() - 1);
                        }
                        m.put(k, groupNames);
                    }
                    //机房位置 机房数据格式为List
                    if (String.valueOf(ModelPropertiesType.LAYOUTDATA.getCode()).equals(map.get(k).toString()) && POSITIONBYROOM.getField().equals(k)) {
                        String index = "";
                        if (v instanceof List && ((List) v).size() > 0) {
                            List list = (List) v;
                            if (list.size() > 1) {
                                //行
                                Integer row = Integer.valueOf(list.get(0).toString());
                                //列
                                Integer col = Integer.valueOf(list.get(1).toString());
                                index = "第" + (row + 1) + "行第" + (col + 1) + "列";
                            }
                        }
                        m.put(k, index);
                    }
                    //机柜位置
                    if (String.valueOf(ModelPropertiesType.LAYOUTDATA.getCode()).equals(map.get(k).toString()) && POSITIONBYCABINET.getField().equals(k)) {
                        String index = "";
                        //机柜数据格式为Map
                        if (v instanceof Map && ((Map) v).size() > 0) {
                            Map mapInfo = (Map) v;
                            Integer startIndex = Integer.valueOf(mapInfo.get("start").toString());
                            Integer endIndex = Integer.valueOf(mapInfo.get("end").toString());
                            if (endIndex > startIndex) {
                                index = "第" + (startIndex + 1) + "-" + (endIndex + 1) + "层";
                            } else {
                                index = "第" + (startIndex + 1) + "层";
                            }
                        }
                        m.put(k, index);
                    }
                }
            });
            mapList.add(m);
        }

        try {
            ExportExcel.exportExcel("模型实例列表导出", "模型实例列表导出", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
        } catch (IOException e) {
            log.error("exportForExcel{}", e);
        }
        return Reply.ok("导出成功");
    }

    /**
     * 模板导出
     *
     * @param param
     * @param request
     * @param response
     * @return
     */
    @Override
    public Reply exportTemplatel(QueryModelInstanceParam param, HttpServletRequest request, HttpServletResponse response) {
        List<ModelPropertiesExportDto> propertiesExportDtos;
        List<PropertyInfo> propertyInfos = new ArrayList<>();
        Map<Integer, List<PropertyInfo>> allModelPropertyInfo = mwModelCommonServiceImpl.getAllModelPropertyInfo();
        if (allModelPropertyInfo != null && allModelPropertyInfo.containsKey(intValueConvert(param.getModelId()))) {
            propertyInfos = allModelPropertyInfo.get(intValueConvert(param.getModelId()));

        }
        List<String> lable = new ArrayList<>();
        List<String> lableName = new ArrayList<>();
        List<Map> mapList = new ArrayList<>();
        List typeList = Arrays.asList(
                String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()),
                String.valueOf(ModelPropertiesType.ORG.getCode()),
                String.valueOf(ModelPropertiesType.USER.getCode()),
                String.valueOf(ModelPropertiesType.GROUP.getCode()));
        for (PropertyInfo dto : propertyInfos) {
            if (dto.getIsInsertShow()) {
                String info = "(";
                if (dto.getIsMust() != null && dto.getIsMust()) {
                    info += "必填,";
                }
                //时间格式
                if (String.valueOf(ModelPropertiesType.DATE.getCode()).equals(dto.getPropertiesType())) {
                    info += "格式:yyyy/MM/DD";
                } else if (dto.getPropertiesType() != null && typeList.contains(dto.getPropertiesType())) {
                    info += "多个数据用“/”隔开";
                } else {
                    info = info.replace(",", "");
                }
                info += ")";
                if ("()".equals(info)) {
                    lableName.add(dto.getPropertiesName());
                } else {
                    String lableNameStr = dto.getPropertiesName();
                    if (dto.getPropertiesType() != null && typeList.contains(dto.getPropertiesType())) {
                        lableNameStr = "^" + lableNameStr;
                    }
                    lableName.add(lableNameStr + info);
                }
                lable.add(dto.getIndexId());
            }
        }
        try {
            ExportExcel.exportExcel("实例模板导出", "实例模板导出", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
        } catch (IOException e) {
            log.error("exportTemplatel{}", e);
        }
        return Reply.ok("导出成功");
    }

    /**
     * 读EXCEL文件，获取信息集合
     *
     * @return
     */
    public Map getExcelInfo(MultipartFile mFile, Boolean isTableHand, ModelExportDataInfoListParam param) throws Exception {
        String fileName = mFile.getOriginalFilename();// 获取文件名
        if (!validateExcel(fileName)) {// 验证文件名是否合格
            return null;
        }
        boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
        if (isExcel2007(fileName)) {
            isExcel2003 = false;
        }
        Workbook wb = null;
        if (isExcel2003) {// 当excel是2003时,创建excel2003
            wb = new HSSFWorkbook(mFile.getInputStream());
        } else {// 当excel是2007时,创建excel2007
            wb = new XSSFWorkbook(mFile.getInputStream());
        }
        if (isTableHand) {//获取表头数据
            return readExcelHeadInfo(wb);// 读取Excel里面表头数据
        } else {
            Boolean isImportEditor = false;
            if (param.getIsImportEditor() != null) {
                isImportEditor = param.getIsImportEditor();//是否是导入修改
            }
            if (isFlag) {
                //获取文件数据按模型分组
                return readExcelValueGroup(wb, param.getParamList());// 读取Excel里面数据的信息
            } else {
                //获取导入数据按模型
                return readExcelValue(wb, param.getParamList(), isImportEditor);// 读取Excel里面数据的信息
            }
        }
    }

    /**
     * 获取表头数据
     *
     * @param wb
     * @return
     */
    private Map readExcelHeadInfo(Workbook wb) {
        int num = wb.getNumberOfSheets();
        Map map = new HashMap();
        List<String> filedList = new ArrayList<>();
        for (int x = 0; x < num; x++) {
            Sheet sheet = wb.getSheetAt(x);
            // 得到Excel的行数
            totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 0 && sheet.getRow(0) != null) {
                Iterator list = sheet.getRow(0).cellIterator();
                while (list.hasNext()) {
                    //如果表头字段有（）说明，去除（）的数据
                    String labelNameStr = list.next().toString();
                    if (labelNameStr.indexOf("分组/模型") == -1) {
                        if (labelNameStr.startsWith("^")) {
                            labelNameStr = labelNameStr.substring(1);
                        }
                        if (labelNameStr.indexOf("(") != -1) {
                            String labelName = labelNameStr.substring(0, labelNameStr.indexOf("("));
                            filedList.add(labelName);
                        } else {
                            filedList.add(labelNameStr);
                        }
                    }
                }
            }
        }
        map.put("headList", filedList);
        return map;
    }


    /**
     * 西藏邮储环境，模板中指定模型
     *
     * @param wb
     * @return
     */
    private Map readExcelValueGroup(Workbook wb, List<ModelExportDataInfoParam> exportList) {
        int num = wb.getNumberOfSheets();
        int errorNum = 0;
        Map map = new HashMap();
//        List onlyList = new ArrayList();
        errorMsg = new String();
        try {
            int listInstanceSize = 0;
            boolean isQuery = true;
            for (int x = 0; x < num; x++) {
                Sheet sheet = wb.getSheetAt(x);
                // 得到Excel的行数
                totalRows = sheet.getPhysicalNumberOfRows();
                // 得到Excel的列数(前提是有行数)
                if (totalRows > 1 && sheet.getRow(0) != null) {
                    totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
                }
                Map<String, List> m = new HashMap();
                if (totalRows <= 1) {
                    map.put(errorMsg, "导入失败，文件内容为空!");
                    return map;
                }
                List<Map> listModelInfo = new ArrayList<>();
                //获取分组/模型的数据，得到导入的模型id
                if (isFlag) {
                    for (int r = 0; r < totalRows; r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) {
                            continue;
                        }
                        //分组导入模式下，分组/模型设置信息
                        int c = (totalCells - 1);
                        if (r == 0) {
                            if (row.getCell(c) != null) {
                                Cell cell = row.getCell(c);
                                String val = cell.getStringCellValue();
                                if ("分组/模型".equals(val)) {
                                    continue;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
                List<AddAndUpdateModelInstanceParam> listInstance = new ArrayList<>();
                // 循环Excel行数
                for (int r = 1; r < totalRows; r++) {
                    AddAndUpdateModelInstanceParam aParam = new AddAndUpdateModelInstanceParam();
                    List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        continue;
                    }
                    //列表最后一列为模型的分类路径，据此可以确定每行数据需要导入的模型id
                    int index = (totalCells - 1);
                    Map modelMap = new HashMap();
                    if (row.getCell(index) != null) {
                        String modelName = "";
                        String groupName = "";
                        if (row.getCell(index) != null) {
                            Cell cell = row.getCell(index);
                            String val = cell.getStringCellValue();
                            String[] str = val.split("/");
                            if (str.length > 1) {
                                modelName = str[str.length - 1];
                                groupName = str[str.length - 2];
                                modelMap = mwModelExportDao.getIndexInfoByGroup(groupName, modelName);
                            }
                        }
                    }
                    for (int c = 0; c < (totalCells - 1); c++) {
                        //分组导入模式下，分组/模型设置信息
                        String modelIndex = "";
                        Integer modelId = 0;
                        String modelName = "";
                        if (modelMap != null && modelMap.size() > 0) {
                            modelIndex = modelMap.get("model_index").toString();
                            modelId = Integer.valueOf(modelMap.get("model_id").toString());
                            modelName = modelMap.get("model_name").toString();
                        } else {
                            propertiesList.clear();
                            break;
                        }
                        AddModelInstancePropertiesParam dto = new AddModelInstancePropertiesParam();
                        Cell cell = row.getCell(c);
                        if (cell == null) {
                            continue;
                        }
                        ModelExportDataInfoParam param = exportList.get(c);
                        //表头字段名称
                        String name = param.getTableName();
                        //模型属性的IndexId
                        String propertiesIndexId = param.getPropertiesIndexId();
                        Integer modelView = param.getModelView();
                        aParam.setModelViewType(modelView);
                        //模型属性的类型
                        //1	字符串、7 IP、9枚举型(单选)、10枚举型(多选)、11机构/部门、12负责人、13用户组  都视为String类型
                        //8	时间 为Date类型
                        String type = param.getPropertiesType();

                        List typeList = Arrays.asList(
                                String.valueOf(ModelPropertiesType.STRING.getCode()),
                                String.valueOf(ModelPropertiesType.IP.getCode()),
                                String.valueOf(ModelPropertiesType.SINGLE_ENUM.getCode()),
                                String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()),
                                String.valueOf(ModelPropertiesType.ORG.getCode()),
                                String.valueOf(ModelPropertiesType.USER.getCode()),
                                String.valueOf(ModelPropertiesType.GROUP.getCode()));
                        //是否唯一
                        Boolean isOnly = param.getIsOnly();
                        //是否必填
                        Boolean isMust = param.getIsMust();
                        //单选、多选下拉值  类型为字符串时，为正则表达式
                        String dropOp = param.getDropOp();
                        //是否忽略
                        Boolean ignore = param.getIgnore();
                        //必填校验，
                        if (isMust) {
                            if (null == cell) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，" + name + "不能为空；";
                                }
                                errorNum++;
                                propertiesList.clear();
                                break;
                            }
                        }
                        if (null != cell) {
                            Object cellValue = null;
                            //该列数据被忽略
                            if (ignore) {
                                continue;
                            }
                            //对获取数据类型做判断
                            switch (cell.getCellType()) {
                                case NUMERIC: // 数字
                                    short format = cell.getCellStyle().getDataFormat();
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        SimpleDateFormat sdf = null; ////System.out.println("cell.getCellStyle().getDataFormat()="+cell.getCellStyle().getDataFormat());
                                        if (format == 20 || format == 32) {
                                            sdf = new SimpleDateFormat("HH:mm");
                                        } else if (format == 14 || format == 31 || format == 57 || format == 58) { // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
                                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                                            double value = cell.getNumericCellValue();
                                            Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                                            cellValue = sdf.format(date);
                                        } else {// 日期
                                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        }
                                        try {
                                            cellValue = sdf.format(cell.getDateCellValue());// 日期
                                        } catch (Exception e) {
                                            try {
                                                throw new Exception("日期格式错误:".concat(e.toString()));
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        } finally {
                                            sdf = null;
                                        }
                                    } else {
                                        DataFormatter dataFormatter = new DataFormatter();
                                        cellValue = dataFormatter.formatCellValue(cell);
                                    }
                                    break;
                                case STRING: // 字符串
                                    //数据类型和选定的模型属性类型必须匹配
                                    if (typeList.contains(type)) {
                                        cellValue = cell.getStringCellValue();
                                    } else {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，数据类型不匹配；";
                                        }
                                        errorNum++;
                                        continue;
                                    }
                                    break;
                                case BOOLEAN: // Boolean
                                    cellValue = cell.getBooleanCellValue();
                                    break;
                                case FORMULA: // 公式
                                    cellValue = cell.getCellFormula();
                                    break;
                                case BLANK: // 空值
                                    if ("8".equals(type)) {
                                        continue;
                                    }
                                    break;
                                case ERROR: // 故障
                                    break;
                                default:
                                    break;
                            }
                            if (cellValue != null) {
                                dto.setPropertiesIndexId(propertiesIndexId);
                                dto.setPropertiesValue(cellValue.toString());
                                dto.setPropertiesType(Integer.valueOf(type));
                                //单选、多选
                                boolean isSelect = true;
                                if (String.valueOf(ModelPropertiesType.SINGLE_ENUM.getCode()).equals(type) || String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(type)) {
                                    String[] str = cellValue.toString().split("/");
                                    if (!Strings.isNullOrEmpty(dropOp)) {
                                        List valueList = new ArrayList();
                                        for (String s : str) {
                                            int indexs = dropOp.indexOf(s);
                                            if (indexs < 0) {
                                                if (errorNum < ERRORNUM) {
                                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列," + name + "下拉框数据不存在；";
                                                }
                                                errorNum++;
                                                isSelect = false;
                                                //结束本次循环
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!isSelect) {
                                    //单选、多选 数据错误，直接跳过该循环
                                    propertiesList.clear();
                                    continue;
                                }
                                //字符串 需要和regex正则匹配
                                if (String.valueOf(ModelPropertiesType.STRING.getCode()).equals(type) && (!Strings.isNullOrEmpty(dropOp))) {
                                    Pattern pattern = Pattern.compile(dropOp);
                                    boolean isMatch = pattern.matcher(cellValue.toString()).matches();
                                    if (isMatch) {
                                        dto.setPropertiesValue(cellValue.toString());
                                    } else {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，输入值不符合正则表达式：" + dropOp + "；";
                                        }
                                        errorNum++;
                                        break;
                                    }
                                }
                                //多选下数据
                                if (String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(type)) {
                                    String[] str = cellValue.toString().split("/");
                                    List<String> valueList = Arrays.asList(str);
                                    if (valueList != null && valueList.size() > 0) {
                                        dto.setPropertiesValue(JSON.toJSONString(valueList));
                                    }
                                }
                                //机构/部门
                                if (String.valueOf(ModelPropertiesType.ORG.getCode()).equals(type)) {
                                    String[] orgArr = cellValue.toString().split("/");
                                    String orgNames = StringUtils.join(orgArr, ",");
                                    List<String> orgIds = mwModelExportDao.selectOrgIdByName(orgNames);
                                    if (orgIds == null) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该部门机构；";
                                        }
                                        errorNum++;
                                    }
                                    List orgIdList = new ArrayList();
                                    for (String str : orgIds) {
                                        List list = Arrays.asList(str.split(","));
                                        list.removeAll(new ArrayList<>());
                                        orgIdList.add(list);
                                    }
                                    dto.setPropertiesValue(JSON.toJSONString(orgIdList));
                                }

                                //负责人
                                if (String.valueOf(ModelPropertiesType.USER.getCode()).equals(type)) {
                                    String[] userArr = cellValue.toString().split("/");
                                    String userNames = StringUtils.join(userArr, ",");
                                    List<Integer> userIds = mwModelExportDao.selectUserIdByName(userNames);
                                    if (userIds == null || userIds.size() != userArr.length) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该用户；";
                                        }
                                        errorNum++;
                                    }
                                    dto.setPropertiesValue(JSON.toJSONString(userIds));
                                }
                                //用户组
                                if (String.valueOf(ModelPropertiesType.GROUP.getCode()).equals(type)) {
                                    String[] groupArr = cellValue.toString().split("/");
                                    String groupNames = StringUtils.join(groupArr, ",");
                                    List<Integer> groupIds = mwModelExportDao.selectUserIdByName(groupNames);
                                    if (groupIds == null || groupIds.size() != groupArr.length) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该用户组；";
                                        }
                                        errorNum++;
                                    }
                                    dto.setPropertiesValue(JSON.toJSONString(groupIds));
                                }
                                //实例名称。
                                if (MwModelViewCommonService.INSTANCE_NAME_KEY.equals(propertiesIndexId) && cellValue != null) {
                                    aParam.setInstanceName(cellValue.toString());
                                }
                                //唯一性check
                                if (isOnly) {
                                    //对excel中的数据进行重复性校验
                                    if (m.containsKey(propertiesIndexId)) {
                                        List onlyList = m.get(propertiesIndexId);
                                        if (onlyList.contains(cellValue)) {
                                            if (errorNum < ERRORNUM) {
                                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，数据重复，已忽略；";
                                            }
                                            errorNum++;
                                            propertiesList.clear();
                                            break;//跳过当前行数据循环

                                        } else {
                                            onlyList.add(cellValue);
                                            m.put(propertiesIndexId, onlyList);
                                        }
                                    } else {
                                        List onlyList = new ArrayList();
                                        onlyList.add(cellValue);
                                        m.put(propertiesIndexId, onlyList);
                                    }
                                    //对es数据库数据进行唯一性校验
                                    QueryModelInstanceParam qParam = new QueryModelInstanceParam();
                                    qParam.setModelId(modelId);
                                    qParam.setModelIndex(modelIndex);
                                    qParam.setFieldList(Arrays.asList(propertiesIndexId));
                                    AddModelInstancePropertiesParam p = new AddModelInstancePropertiesParam();
                                    p.setPropertiesValue(cellValue.toString());
                                    p.setPropertiesIndexId(propertiesIndexId);
                                    p.setPropertiesName(name);
                                    qParam.setPropertiesList(Arrays.asList(p));
                                    Reply reply = mwModelInstanceService.modelInstanceFieldUnique(qParam);
                                    if (((ArrayList) reply.getData()).size() > 0) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，" + name + "重复，" + modelName + "模型中数据已存在；";
                                        }
                                        errorNum++;
                                        propertiesList.clear();
                                        break;//跳过当前行数据循环
                                    }
                                }
                                aParam.setModelId(modelId);
                                aParam.setModelIndex(modelIndex);
                                if (isMust && (cellValue == null || cellValue == "")) {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，" + name + "不能为空；";
                                    }
                                    errorNum++;
                                    propertiesList.clear();
                                    break;
                                }
                                if (isQuery) {
                                    Integer count = mwModelManageDao.selectCountInstanceBymodelId(modelId);
                                    //没有实例
                                    if (count == 0 && String.valueOf(ModelPropertiesType.DATE.getCode()).equals(type)) {
                                        //时间类型
                                        //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
                                        mwModelInstanceServiceImplV1.setESMappingByDate(modelIndex, propertiesIndexId);
                                    }
                                    isQuery = false;
                                }
                                propertiesList.add(dto);
                            }
                        }
                    }
                    if (propertiesList != null && propertiesList.size() > 0) {
                        aParam.setPropertiesList(propertiesList);
                        listInstance.add(aParam);
                    }
                    if (listInstance.size() > BATCH_COUNT) {
                        mwModelInstanceService.saveData(listInstance, true, true);
                        listInstanceSize += listInstance.size();
                        listInstance.clear();
                    }
                }
                if (listInstance != null && listInstance.size() > 0) {
                    //数据存储
                    mwModelInstanceService.saveData(listInstance, true, true);
                    listInstanceSize += listInstance.size();
                }
                map.put("alertMsg", errorMsg);
                map.put("addNum", listInstanceSize);
            }
            return map;
        } catch (ModelManagerException e) {
            throw new ModelManagerException("该模块新增数量已达许可数量上限！");
        } catch (Exception e) {
            map.put("alertMsg", "数据导入失败。");
            map.put("addNum", 0);
            return map;
        }
    }

    /**
     * 读取Excel里面客户的信息
     *
     * @param wb
     * @return
     */
    private Map readExcelValue(Workbook wb, List<ModelExportDataInfoParam> exportList, Boolean isImportEditor) throws Exception {
        int num = wb.getNumberOfSheets();
        Map map = new HashMap();
        errorMsg = new String();
        int errorNum = 0;
        int listInstanceSize = 0;
        boolean isQuery = true;
        //关联模型实例名称为key，实例Id为value
        Map<String, Integer> relationMap = new HashMap();
        Map<Integer, Object> layOutMapInfo = new HashMap<>();
        Map<String, Map<String, Object>> matchKeyMapInfo = new HashMap<>();
        //导入修改 获取唯一匹配的字段
        String matchKeyProperIndexId = "";
        Set<String> modelIndexSet = new HashSet<>();
        List<String> modelIndexs = new ArrayList<>();
        Integer modelView = 0;
        Map<Integer, Integer> instanceViewMap = new HashMap<>();
        //实例视图类型字段下标
        int viewTypeIndex = -1;
        //刀箱行数字段下标
        int bayRowIndex = -1;
        //刀箱列数字段下标
        int bayColIndex = -1;
        //实例名称字段下标
        int instanceNameIndex = -1;
        if (CollectionUtils.isNotEmpty(exportList)) {
            modelView = exportList.get(0).getModelView();
            List<String> relationModelIndex = new ArrayList<>();
            List<String> roomAndCabinetrelationModelIndex = new ArrayList<>();
            Boolean isRoomCabinetFlag = false;

            int x = 0;
            for (ModelExportDataInfoParam exportDataInfoParam : exportList) {
                //外部关联数据查询
                if (String.valueOf(ModelPropertiesType.SINGLE_RELATION.getCode()).equals(exportDataInfoParam.getPropertiesType())
                        && !Strings.isNullOrEmpty(exportDataInfoParam.getRelationModelIndex())) {
                    relationModelIndex.add(exportDataInfoParam.getRelationModelIndex());
                    if (exportDataInfoParam.getPropertiesIndexId().equals(RELATIONSITEROOM.getField()) ||
                            exportDataInfoParam.getPropertiesIndexId().equals(RELATIONSITECABINET.getField())) {
                        roomAndCabinetrelationModelIndex.add(exportDataInfoParam.getRelationModelIndex());
                    }
                }
                //导入文件表头有视图类型字段时，获取该字段的下标位置
                if (VIEW_SHOW_TYPE.equals(exportDataInfoParam.getPropertiesIndexId())) {
                    viewTypeIndex = x;
                }
                //导入文件表头有刀箱列数时，获取该字段的下标位置
                if (BAY_COL.equals(exportDataInfoParam.getPropertiesIndexId())) {
                    bayColIndex = x;
                }
                //导入文件表头有刀箱行数时，获取该字段的下标位置
                if (BAY_ROW.equals(exportDataInfoParam.getPropertiesIndexId())) {
                    bayRowIndex = x;
                }
                //导入文件表头实例名称字段下标位置
                if (INSTANCE_NAME_KEY.equals(exportDataInfoParam.getPropertiesIndexId())) {
                    instanceNameIndex = x;
                }
                //导入修改 获取唯一匹配的字段
                if (isImportEditor && exportDataInfoParam.getIsMatchKey()) {
                    matchKeyProperIndexId = exportDataInfoParam.getPropertiesIndexId();
                    modelIndexSet.add(exportDataInfoParam.getModelIndexId());
                }
                x++;
            }
            modelIndexs = new ArrayList<>(modelIndexSet);
            List<MwModelInstanceParam> instanceRelationInfo = mwModelInstanceDao.selectRelationInstanceInfo(relationModelIndex);
            //为了避免不同模型下的实例名称重复，
            for (MwModelInstanceParam param : instanceRelationInfo) {
                if(param.getModelView()>1){//机房有外部关联的话，不作处理
                    relationMap.put(param.getInstanceName() + "_" + param.getModelIndex() + "_" + param.getRelationInstanceId(), param.getInstanceId());
                }else{
                    relationMap.put(param.getInstanceName() + "_" + param.getModelIndex() + "_0" , param.getInstanceId());
                }
            }
            //根据机房机柜关联的modelIndex，获取对应模型下的实例信息，
            if (CollectionUtils.isNotEmpty(roomAndCabinetrelationModelIndex)) {
                QueryInstanceModelParam param = new QueryInstanceModelParam();
                param.setModelIndexs(roomAndCabinetrelationModelIndex);
                param.setFieldList(Arrays.asList(LAYOUTDATA.getField(), INSTANCE_ID_KEY));
                List<Map<String, Object>> layOutInfo = mwModelInstanceServiceImplV1.getInstanceInfoByModelIndexs(param);
                //实例Id为key，布局数据为value，为下面的机柜、机柜下属设备的坐标校验做准备
                layOutMapInfo = layOutInfo.stream().collect(Collectors.toMap(m -> intValueConvert(m.get(INSTANCE_ID_KEY)), m -> m.get(LAYOUTDATA.getField()), (
                        value1, value2) -> {
                    return value2;
                }));

                //获取机房机柜实例数据，以实例为Id，模型视图类型为value，转为map
                List<MwModelInstanceTypeDto> mapList = mwModelInstanceDao.getInstanceTypeById();
                instanceViewMap = mapList.stream().collect(Collectors.toMap(s -> s.getInstanceId(), s -> s.getViewType(), (
                        value1, value2) -> {
                    return value2;
                }));

            }
            //根据modelIndex，获取对应模型下的实例信息，
            if (CollectionUtils.isNotEmpty(modelIndexs)) {
                QueryInstanceModelParam param = new QueryInstanceModelParam();
                param.setModelIndexs(modelIndexs);
                param.setFieldList(Arrays.asList(matchKeyProperIndexId, INSTANCE_ID_KEY, INSTANCE_NAME_KEY, POSITIONBYCABINET.getField(), POSITIONBYROOM.getField()));
                List<Map<String, Object>> modelInfo = mwModelInstanceServiceImplV1.getInstanceInfoByModelIndexs(param);
                String finalMatchKeyProperIndexId = matchKeyProperIndexId;
                matchKeyMapInfo = modelInfo.stream().collect(Collectors.toMap(m -> m.get(finalMatchKeyProperIndexId).toString(), m -> m, (
                        value1, value2) -> {
                    return value2;
                }));
            }
        }
        for (int x = 0; x < num; x++) {
            Sheet sheet = wb.getSheetAt(x);
            // 得到Excel的行数
            totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 1 && sheet.getRow(0) != null) {
                totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }
            List<AddAndUpdateModelInstanceParam> listInstance = new ArrayList<>();
            Map<String, List> m = new HashMap();
            if (totalRows <= 1) {
                map.put(errorMsg, "导入失败，文件内容为空!");
                return map;
            }

            // 循环Excel行数
            for (int r = 1; r < totalRows; r++) {
                AddAndUpdateModelInstanceParam aParam = new AddAndUpdateModelInstanceParam();
                List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Integer relationInstanceId = 0;
                String layoutInfo = "";
                //关联机房Id
                int relationRoomId = 0;
                //关联机柜Id
                int relationCabinetId = 0;
                for (int c = 0; c < totalCells; c++) {

                    AddModelInstancePropertiesParam dto = new AddModelInstancePropertiesParam();
                    Cell cell = row.getCell(c);
                    ModelExportDataInfoParam param = exportList.get(c);
                    //是否忽略
                    Boolean ignore = param.getIgnore();
                    //该列数据被忽略
                    if (ignore != null && ignore) {
                        continue;
                    }
                    //表头字段名称
                    String name = param.getTableName();
                    //模型属性的IndexId
                    String propertiesIndexId = param.getPropertiesIndexId();
                    //模型属性的modelId
                    Integer modelId = param.getModelId();
                    //模型属性的modelIndexId
                    String modelIndexId = param.getModelIndexId();
                    aParam.setModelViewType(param.getModelView());
                    //获取导入文件中的视图类型数据
                    String instanceViewType = "默认视图";
                    if (viewTypeIndex != -1 && row.getCell(viewTypeIndex) != null) {
                        instanceViewType = row.getCell(viewTypeIndex).getStringCellValue();
                    }
                    //获取导入文件中的实例名称数据
                    if (instanceNameIndex != -1 && row.getCell(instanceNameIndex) != null) {
                        String instanceName = row.getCell(instanceNameIndex).getStringCellValue();
                        aParam.setInstanceName(instanceName);
                    }
                    //获取导入文件中的刀箱行数数据
                    int bayRowNum = 0;
                    //获取导入文件中的刀箱列数数据
                    int bayColNum = 0;
                    //获取导入文件中的刀箱行数数据
                    if (bayRowIndex != -1 && row.getCell(bayRowIndex) != null) {
                        bayRowNum = (int) row.getCell(bayRowIndex).getNumericCellValue();
                    }
                    //获取导入文件中的刀箱列数数据
                    if (bayColIndex != -1 && row.getCell(bayColIndex) != null) {
                        bayColNum = (int) (row.getCell(bayColIndex).getNumericCellValue());
                    }


                    //模型属性的类型
                    //1	字符串、5、外部关联，7 IP、9枚举型(单选)、10枚举型(多选)、11机构/部门、12负责人、13用户组  都视为EXCEL的String类型
                    //8	时间 为Date类型
                    String type = param.getPropertiesType();
                    List typeList = Arrays.asList(
                            String.valueOf(ModelPropertiesType.STRING.getCode()),
                            String.valueOf(ModelPropertiesType.SINGLE_RELATION.getCode()),
                            String.valueOf(ModelPropertiesType.MULTIPLE_RELATION.getCode()),
                            String.valueOf(ModelPropertiesType.IP.getCode()),
                            String.valueOf(ModelPropertiesType.SINGLE_ENUM.getCode()),
                            String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()),
                            String.valueOf(ModelPropertiesType.ORG.getCode()),
                            String.valueOf(ModelPropertiesType.USER.getCode()),
                            String.valueOf(ModelPropertiesType.GROUP.getCode()),
                            String.valueOf(ModelPropertiesType.LAYOUTDATA.getCode()));
                    //是否唯一
                    Boolean isOnly = param.getIsOnly();
                    //是否必填
                    Boolean isMust = param.getIsMust();
                    //单选、多选下拉值  类型为字符串时，为正则表达式
                    String dropOp = param.getDropOp();

                    //必填校验，
                    if (isMust != null && isMust) {
                        if (null == cell) {
                            if (errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，" + name + "不能为空；";
                            }
                            errorNum++;
                            propertiesList.clear();
                            break;
                        }
                    }
                    if (null != cell) {
                        Object cellValue = null;

                        //对获取数据类型做判断
                        switch (cell.getCellType()) {
                            case NUMERIC: // 数字
                                short format = cell.getCellStyle().getDataFormat();
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    SimpleDateFormat sdf = null; ////System.out.println("cell.getCellStyle().getDataFormat()="+cell.getCellStyle().getDataFormat());
                                    if (format == 20 || format == 32) {
                                        sdf = new SimpleDateFormat("HH:mm");
                                    } else if (format == 14 || format == 31 || format == 57 || format == 58) { // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
                                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        double value = cell.getNumericCellValue();
                                        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                                        cellValue = sdf.format(date);
                                    } else {// 日期
                                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    }
                                    try {
                                        cellValue = sdf.format(cell.getDateCellValue());// 日期
                                    } catch (Exception e) {
                                        try {
                                            throw new Exception("日期格式错误:".concat(e.toString()));
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    } finally {
                                        sdf = null;
                                    }
                                } else {
                                    DataFormatter dataFormatter = new DataFormatter();
                                    cellValue = dataFormatter.formatCellValue(cell);
                                }
                                break;
                            case STRING: // 字符串
                                //数据类型和选定的模型属性类型必须匹配
                                if (typeList.contains(type)) {
                                    cellValue = cell.getStringCellValue();
                                } else {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，数据类型不匹配；";
                                    }
                                    errorNum++;
                                }
                                break;
                            case BOOLEAN: // Boolean
                                cellValue = cell.getBooleanCellValue();
                                break;
                            case FORMULA: // 公式
                                cellValue = cell.getCellFormula();
                                break;
                            case BLANK: // 空值
                                cellValue = "";
                                break;
                            case ERROR: // 故障
                                break;
                            default:
                                break;
                        }
                        if (cellValue != null) {
                            dto.setPropertiesIndexId(propertiesIndexId);
                            dto.setPropertiesValue(cellValue.toString());
                            dto.setPropertiesType(Integer.valueOf(type));
                            if (isImportEditor) {
                                //批量修改导入
                                //matchKeyProperIndexId唯一匹配属性id存在且isImportEditor为ture 则表示为导入修改，该列数据无需保存
                                if (!Strings.isNullOrEmpty(matchKeyProperIndexId) && matchKeyProperIndexId.equals(propertiesIndexId)) {
                                    Map<String, Object> objectMap = matchKeyMapInfo.get(cellValue.toString());
                                    //获取批量修改的相应数据
                                    if (objectMap != null) {
                                        if (objectMap.get(ESID) != null && !Strings.isNullOrEmpty(objectMap.get(ESID).toString())) {
                                            String modelEsId = objectMap.get(ESID).toString();
                                            aParam.setEsId(modelEsId);
                                        }
                                        if (objectMap.get(INSTANCE_NAME_KEY) != null) {
                                            String instanceName = objectMap.get(INSTANCE_NAME_KEY).toString();
                                            aParam.setInstanceName(instanceName);
                                        }
                                        if (objectMap.get(INSTANCE_ID_KEY) != null) {
                                            Integer instanceId = Integer.valueOf(objectMap.get(INSTANCE_ID_KEY).toString());
                                            aParam.setInstanceId(instanceId);
                                        }
                                        if (objectMap.get(POSITIONBYCABINET.getField()) != null) {
                                            CabinetLayoutDataParam cabinetCoordinate = new CabinetLayoutDataParam();
                                            if (objectMap.get(POSITIONBYCABINET.getField()) instanceof List) {
                                                List<CabinetLayoutDataParam> cabinetCoordinateList = JSON.parseArray(JSONObject.toJSONString(objectMap.get(POSITIONBYCABINET.getField())), CabinetLayoutDataParam.class);
                                                if (CollectionUtils.isNotEmpty(cabinetCoordinateList)) {
                                                    cabinetCoordinate = cabinetCoordinateList.get(0);
                                                }
                                            } else {
                                                cabinetCoordinate = JSONObject.parseObject(JSONObject.toJSONString(objectMap.get(POSITIONBYCABINET.getField())), CabinetLayoutDataParam.class);
                                            }
                                            aParam.setCabinetCoordinate(cabinetCoordinate);
                                        }
                                        if (objectMap.get(POSITIONBYROOM.getField()) != null && !"[]".equals(objectMap.get(POSITIONBYROOM.getField()))) {
                                            List roomCoordinate = (List) JSONArray.parse(JSONObject.toJSONString(objectMap.get(POSITIONBYROOM.getField())));
                                            aParam.setRoomCoordinate(roomCoordinate);
                                        }
                                    } else {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,唯一值匹配值:" + matchKeyProperIndexId + "未查到数据";
                                        }
                                        errorNum++;
                                        //结束本次循环
                                        break;
                                    }
                                    aParam.setModelIndex(modelIndexId);
                                    continue;
                                }
                                //机房实例
                                if (param.getModelView() != null && param.getModelView().intValue() == 1) {
                                    if (ROWNUM.getField().equals(propertiesIndexId) || COLNUM.getField().equals(propertiesIndexId)) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，机房布局行数列数不能修改；";
                                        }
                                        continue;
                                    }
                                }
                                //机柜实例
                                if (param.getModelView() != null && param.getModelView().intValue() == 2) {
                                    if (UNUM.getField().equals(propertiesIndexId)) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，机柜布局U位数不能修改；";
                                        }
                                        continue;
                                    }
                                }
                            }
                            //matchKeyProperIndexId唯一匹配属性id存在且isImportEditor为ture 则表示为导入修改，该列数据无需保存
                            if (isImportEditor && !Strings.isNullOrEmpty(matchKeyProperIndexId) && matchKeyProperIndexId.equals(propertiesIndexId)) {
                                Map<String, Object> objectMap = matchKeyMapInfo.get(cellValue.toString());
                                //获取批量修改的相应数据
                                if (objectMap != null) {
                                    if (objectMap.get(ESID) != null && !Strings.isNullOrEmpty(objectMap.get(ESID).toString())) {
                                        String modelEsId = objectMap.get(ESID).toString();
                                        aParam.setEsId(modelEsId);
                                    }
                                    if (objectMap.get(INSTANCE_NAME_KEY) != null) {
                                        String instanceName = objectMap.get(INSTANCE_NAME_KEY).toString();
                                        aParam.setInstanceName(instanceName);
                                    }
                                    if (objectMap.get(INSTANCE_ID_KEY) != null) {
                                        Integer instanceId = Integer.valueOf(objectMap.get(INSTANCE_ID_KEY).toString());
                                        aParam.setInstanceId(instanceId);
                                    }
                                } else {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,唯一值匹配值:" + matchKeyProperIndexId + "未查到数据";
                                    }
                                    errorNum++;
                                    //结束本次循环
                                    break;
                                }
                                aParam.setModelIndex(modelIndexId);
                                continue;
                            }

                            //单选、多选
                            boolean isSelect = true;
                            if (String.valueOf(ModelPropertiesType.SINGLE_ENUM.getCode()).equals(type) || String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(type)) {
                                String[] str = cellValue.toString().split("/");
                                if (!Strings.isNullOrEmpty(dropOp)) {
                                    List valueList = new ArrayList();
                                    for (String s : str) {
                                        int index = dropOp.indexOf(s);
                                        if (index < 0) {
                                            if (errorNum < ERRORNUM) {
                                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列," + name + "下拉框数据不存在；";
                                            }
                                            errorNum++;
                                            isSelect = false;
                                            //结束本次循环
                                            break;
                                        }
                                    }

                                }
                            }
                            if (!isSelect) {
                                //单选、多选 数据错误，直接跳过该循环
                                propertiesList.clear();
                                continue;
                            }
                            //字符串 需要和regex正则匹配
                            if (String.valueOf(ModelPropertiesType.STRING.getCode()).equals(type) && (!Strings.isNullOrEmpty(dropOp))) {
                                Pattern pattern = Pattern.compile(dropOp);
                                boolean isMatch = pattern.matcher(cellValue.toString()).matches();
                                if (isMatch) {
                                    dto.setPropertiesValue(cellValue.toString());
                                } else {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，输入值不符合正则表达式：" + dropOp + "；";
                                    }
                                    errorNum++;
                                    break;
                                }
                            }

                            //外部关联(单选)
                            if (String.valueOf(ModelPropertiesType.SINGLE_RELATION.getCode()).equals(type)) {
                                //外部关联的indexIndex必须有值
                                dto.setPropertiesValue("0");//设默认值，避免新增时类型转换
                                Integer view = 0;
                                if (!Strings.isNullOrEmpty(param.getRelationModelIndex())) {
                                    aParam.setRelationModelIndex(param.getRelationModelIndex());
                                    if (relationMap != null && relationMap.size() > 0) {
                                        String relationModelIndex = param.getRelationModelIndex();
                                        relationInstanceId = relationMap.get(cellValue.toString() + "_" + relationModelIndex + "_0");
                                        //根据当前属性字段来判断布局位置校验时的视图类型（机柜位置校验和机房位置校验）
                                        if (param.getPropertiesIndexId().equals(RELATIONSITEROOM.getField())) {
                                            relationRoomId = relationInstanceId;
//                                            view = 1;
                                        }
                                        if (param.getPropertiesIndexId().equals(RELATIONSITECABINET.getField()) && relationRoomId != 0) {
                                            relationInstanceId = relationMap.get(cellValue.toString() + "_" + relationModelIndex + "_" + relationRoomId);
//                                            view = 2;
                                        }
                                        if (relationInstanceId == null) {
                                            if (errorNum < ERRORNUM) {
                                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，该数据不存在；";
                                            }
                                            errorNum++;
                                            propertiesList.clear();
                                            break;
                                        } else {
                                            aParam.setRelationInstanceId(relationInstanceId);
                                            dto.setPropertiesValue(relationInstanceId.toString());
                                            //获取关联Id的模型视图，布局校验中使用
                                            if (instanceViewMap != null && instanceViewMap.containsKey(relationInstanceId)) {
                                                view = instanceViewMap.get(relationInstanceId);
                                            }
                                            if (!Strings.isNullOrEmpty(layoutInfo)) {
                                                //机房机柜布局和外部关联 这两个字段进入时都要check一下布局坐标是否重复，
                                                //因为不确定布局(坐标数据)和外部关联哪个字段在前面，查询两次，
                                                // 总有一次layoutInfo和relationInstanceId都有值
                                                boolean isCheck = checkLayout(layOutMapInfo, layoutInfo, view, relationInstanceId, instanceViewType);
                                                if (isCheck) {
                                                    if (errorNum < ERRORNUM) {
                                                        errorMsg += "第" + (r + 1) + "行数据插入关联设备时，所选位置不存在或已被占用；";
                                                    }
                                                    errorNum++;
                                                    propertiesList.clear();
                                                    break;
                                                }
                                            }

                                        }
                                        dto.setPropertiesValue(relationInstanceId + "");
                                    }
                                }
                            }
                            //外部关联(多选)
                            if (String.valueOf(ModelPropertiesType.MULTIPLE_RELATION.getCode()).equals(type)) {
                                List<String> multipleRelationCode = new ArrayList<>();
                                //外部关联的indexIndex必须有值
                                dto.setPropertiesValue(JSON.toJSONString(multipleRelationCode));//设默认值，避免新增时类型转换
                                if (!Strings.isNullOrEmpty(param.getRelationModelIndex())) {
                                    aParam.setRelationModelIndex(param.getRelationModelIndex());
                                    if (relationMap != null && relationMap.size() > 0) {
                                        String relationModelIndex = param.getRelationModelIndex();

                                        String[] userArr = cellValue.toString().split("/");
                                        for (String str : userArr) {
                                            if (!Strings.isNullOrEmpty(str)) {
                                                relationInstanceId = relationMap.get(str + "_" + relationModelIndex);
                                                multipleRelationCode.add(relationInstanceId + "");
                                            }
                                        }
                                        if (relationInstanceId == null) {
                                            if (errorNum < ERRORNUM) {
                                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，该数据不存在；";
                                            }
                                            errorNum++;
                                            propertiesList.clear();
                                            break;
                                        }
                                        dto.setPropertiesValue(JSON.toJSONString(multipleRelationCode));
                                    }
                                }
                            }
                            //机房机柜布局
                            if (String.valueOf(ModelPropertiesType.LAYOUTDATA.getCode()).equals(type)) {
                                layoutInfo = cellValue.toString();
                                //根据当前属性字段来判断布局位置校验时的视图类型（机柜位置校验和机房位置校验）
                                Integer view = 0;
                                List<CabinetLayoutDataParam> cabinetLayoutInfo = new ArrayList<>();
                                if (relationInstanceId != null && relationInstanceId.intValue() != 0) {
                                    //获取关联Id的模型视图，布局校验中使用
                                    if (instanceViewMap != null && instanceViewMap.containsKey(relationInstanceId)) {
                                        view = instanceViewMap.get(relationInstanceId);
                                        //view == 2,表示relationInstanceId为机柜id,获取机柜Id的布局
                                        if (view == 2 && layOutMapInfo != null && layOutMapInfo.containsKey(relationInstanceId)) {
                                            Object layoutBay = layOutMapInfo.get(relationInstanceId);
                                            if (layoutBay != null && layoutBay instanceof List) {
                                                cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(layoutBay), CabinetLayoutDataParam.class);
                                            }
                                        }
                                    }
                                    //机房机柜布局和外部关联 这两个字段进入时都要check一下布局坐标是否重复，
                                    //因为不确定布局(坐标数据)和外部关联哪个字段在前面，查询两次，
                                    // 总有一次layoutInfo和relationInstanceId都有值
                                    boolean isCheck = checkLayout(layOutMapInfo, layoutInfo, view, relationInstanceId, instanceViewType);
                                    if (isCheck) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行数据插入关联设备时，所选位置不存在或已被占用；";
                                        }
                                        errorNum++;
                                        propertiesList.clear();
                                        break;
                                    }
                                }
                                //在机房中位置
                                if (POSITIONBYROOM.getField().equals(param.getPropertiesIndexId())) {
                                    List<Integer> position = new ArrayList<>();
                                    if (layoutInfo.split(",").length > 1) {
                                        String xVal = layoutInfo.split(",")[0];
                                        position.add(Integer.valueOf(xVal) - 1);
                                        String yVal = layoutInfo.split(",")[1];
                                        position.add(Integer.valueOf(yVal) - 1);
                                        dto.setPropertiesValue(JSONArray.toJSONString(position));
                                    }
                                }
                                //在机柜中的位置
                                if (POSITIONBYCABINET.getField().equals(param.getPropertiesIndexId())) {
                                    CabinetLayoutDataParam cabinetLayout = new CabinetLayoutDataParam();
                                    MwModelViewLayoutDTO viewLayoutDTO = getStartAndEndIndex(layoutInfo.split("/Bay")[0]);
                                    Integer startVal = viewLayoutDTO.getStartIndex();
                                    Integer endVal = viewLayoutDTO.getEndIndex();
                                    if (startVal >= 0 && endVal >= 0) {
                                        cabinetLayout.setStart(startVal);
                                        cabinetLayout.setEnd(endVal);
                                        cabinetLayout.setIsUsed(true);
                                        if (instanceViewType.equals(CHASSIS_VIEW)) {//刀箱视图
                                            cabinetLayout.setType(CHASSIS_VIEW);
                                            List<List<QueryBladeInstanceParam>> daoData = new ArrayList<>();
                                            //获取刀箱布局的行数，列数
                                            for (int a = 0; a < bayRowNum; a++) {
                                                List<QueryBladeInstanceParam> list = new ArrayList<>();
                                                for (int b = 0; b < bayColNum; b++) {
                                                    QueryBladeInstanceParam bladeInstanceParam = new QueryBladeInstanceParam();
                                                    bladeInstanceParam.setInstanceId("");
                                                    bladeInstanceParam.setInstanceName("");
                                                    bladeInstanceParam.setCurrentFlag(false);
                                                    list.add(bladeInstanceParam);
                                                }
                                                daoData.add(list);
                                            }
                                            cabinetLayout.setDaoData(daoData);
                                            cabinetLayout.setBayCol(bayColNum);
                                            cabinetLayout.setBayRow(bayRowNum);
                                        }
                                        //刀片视图布局
                                        if (instanceViewType.equals(BLADE_VIEW) && layoutInfo.split("Bay").length > 1) {//3-12U/Bay1-2
                                            cabinetLayout.setType(BLADE_VIEW);
                                            //设置刀片布局数据
                                            setBladeLayoutValue(cabinetLayoutInfo, layoutInfo, aParam.getInstanceName(), cabinetLayout, aParam.getInstanceId() + "");
                                        }
                                        layOutMapInfo.put(relationInstanceId, cabinetLayoutInfo);
                                        dto.setPropertiesValue(JSONObject.toJSONString(cabinetLayout));
                                    } else {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，设备U位数必须大于0；";
                                        }
                                        errorNum++;
                                        propertiesList.clear();
                                        break;//跳过当前行数据循环
                                    }
                                }

                            }

                            //多选下数据
                            if (String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(type)) {
                                String[] str = cellValue.toString().split("/");
                                List<String> valueList = Arrays.asList(str);
                                if (valueList != null && valueList.size() > 0) {
                                    dto.setPropertiesValue(JSON.toJSONString(valueList));
                                }
                            }
                            //机构/部门
                            if (String.valueOf(ModelPropertiesType.ORG.getCode()).equals(type)) {
                                String[] orgArr = cellValue.toString().split("/");
                                String orgNames = StringUtils.join(orgArr, ",");
                                List<String> orgIds = mwModelExportDao.selectOrgIdByName(orgNames);
                                if (orgIds == null) {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该部门机构；";
                                    }
                                    errorNum++;
                                    propertiesList.clear();
                                    break;//跳过当前行数据循环
                                }
                                List orgIdList = new ArrayList();
                                for (String str : orgIds) {
                                    List<String> list = Arrays.asList(str.split(","));
                                    list.removeAll(new ArrayList<>());
                                    List<Integer> listInt = list.stream().map(Integer::valueOf).collect(Collectors.toList());
                                    orgIdList.add(listInt);
                                }
                                dto.setPropertiesValue(JSON.toJSONString(orgIdList));
                            }

                            //负责人
                            if (String.valueOf(ModelPropertiesType.USER.getCode()).equals(type)) {
                                String[] userArr = cellValue.toString().split("/");
                                String userNames = StringUtils.join(userArr, ",");
                                List<Integer> userIds = mwModelExportDao.selectUserIdByName(userNames);
                                if (userIds == null) {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该用户；";
                                    }
                                    errorNum++;
                                    propertiesList.clear();
                                    break;//跳过当前行数据循环
                                }
                                dto.setPropertiesValue(JSON.toJSONString(userIds));
                            }
                            //用户组
                            if (String.valueOf(ModelPropertiesType.GROUP.getCode()).equals(type)) {
                                if (cellValue.toString() != "") {
                                    String[] groupArr = cellValue.toString().split("/");
                                    String groupNames = StringUtils.join(groupArr, ",");
                                    List<Integer> groupIds = mwModelExportDao.selectGroupIdByName(groupNames);
                                    if (groupIds == null || groupIds.size() != groupArr.length) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该用户组；";
                                        }
                                        errorNum++;
                                    }
                                    dto.setPropertiesValue(JSON.toJSONString(groupIds));
                                }
                            }


                            //实例名称。
                            if (MwModelViewCommonService.INSTANCE_NAME_KEY.equals(propertiesIndexId) && cellValue != null) {
                                aParam.setInstanceName(cellValue.toString());
                            }
                            //唯一性check
                            if (isOnly) {
                                //对excel中的数据进行重复性校验
                                if (m.containsKey(propertiesIndexId)) {
                                    List onlyList = m.get(propertiesIndexId);
                                    if (onlyList.contains(cellValue)) {
                                        if (errorNum < ERRORNUM) {
                                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，数据重复，已忽略；";
                                        }
                                        errorNum++;
                                        propertiesList.clear();
                                        break;//跳过当前行数据循环

                                    } else {
                                        onlyList.add(cellValue);
                                        m.put(propertiesIndexId, onlyList);
                                    }
                                } else {
                                    List onlyList = new ArrayList();
                                    onlyList.add(cellValue);
                                    m.put(propertiesIndexId, onlyList);
                                }
                                //对es数据库数据进行唯一性校验
                                QueryModelInstanceParam qParam = new QueryModelInstanceParam();
                                qParam.setModelId(modelId);
                                qParam.setModelIndex(modelIndexId);
                                qParam.setFieldList(Arrays.asList(propertiesIndexId));
                                AddModelInstancePropertiesParam p = new AddModelInstancePropertiesParam();
                                p.setPropertiesValue(cellValue.toString());
                                p.setPropertiesIndexId(propertiesIndexId);
                                p.setPropertiesName(name);
                                qParam.setPropertiesList(Arrays.asList(p));
                                Reply reply = mwModelInstanceService.modelInstanceFieldUnique(qParam);
                                if (((ArrayList) reply.getData()).size() > 0) {
                                    if (errorNum < ERRORNUM) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，" + name + "重复，系统数据已存在；";
                                    }
                                    errorNum++;
                                    propertiesList.clear();
                                    break;//跳过当前行数据循环
                                }
                            }
                            aParam.setModelId(modelId);
                            aParam.setModelIndex(modelIndexId);
                            if (isMust && (cellValue == null || cellValue == "")) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，" + name + "不能为空；";
                                }
                                errorNum++;
                                propertiesList.clear();
                                break;
                            }
                            if (isQuery) {
                                Integer count = mwModelManageDao.selectCountInstanceBymodelId(modelId);
                                //没有实例
                                if (count == 0 && String.valueOf(ModelPropertiesType.DATE.getCode()).equals(type)) {
                                    //时间类型
                                    //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
                                    mwModelInstanceServiceImplV1.setESMappingByDate(modelIndexId, propertiesIndexId);
                                }
                                isQuery = false;
                            }
                            propertiesList.add(dto);

                        }
                    }
                }
                if (propertiesList != null && propertiesList.size() > 0) {
                    aParam.setPropertiesList(propertiesList);
                    listInstance.add(aParam);
                }
                if (listInstance.size() > BATCH_COUNT) {
                    if (isImportEditor) {
                        //批量修改数据
                        mwModelInstanceService.editorData(listInstance);
                    } else {
                        mwModelInstanceService.saveData(listInstance, true, true);
                    }
                    listInstanceSize += listInstance.size();
                    listInstance.clear();
                }
            }
            //刀片布局数据转换
            valueConvert(listInstance, layOutMapInfo);
            if (listInstance != null && listInstance.size() > 0) {
                if (isImportEditor) {
                    //批量修改数据
                    mwModelInstanceService.editorData(listInstance);
                } else {
                    mwModelInstanceService.saveData(listInstance, true, true);
                }
                listInstanceSize += listInstance.size();
            }
            map.put("alertMsg", errorMsg);
            map.put("addNum", listInstanceSize);
        }
        return map;
    }

    private void valueConvert(List<AddAndUpdateModelInstanceParam> listInstance, Map<Integer, Object> layOutMapInfo) {
        //刀片布局数据需要重新组合,刀片的布局数据保存的是所有的整体数据，因此需要重新处理替换
        if (CollectionUtils.isNotEmpty(listInstance) && layOutMapInfo != null) {
            for (AddAndUpdateModelInstanceParam param : listInstance) {
                if (intValueConvert(param.getModelViewType()) == 3) {//模型视图为机柜下属设备
                    //刀片布局时，这里关联的是机柜Id
                    Integer relationInstanceId = param.getRelationInstanceId();
                    //是否是刀片布局
                    boolean isBlade = false;
                    int x = 0;
                    int layoutIndex = -1;
                    for (AddModelInstancePropertiesParam instanceParam : param.getPropertiesList()) {
                        if (instanceParam.getPropertiesIndexId().equals(VIEW_SHOW_TYPE) && instanceParam.getPropertiesValue().equals(BLADE_VIEW)) {
                            //布局视图为刀片布局
                            isBlade = true;
                        }
                        if (instanceParam.getPropertiesIndexId().equals(POSITIONBYCABINET.getField())) {
                            //布局视图为刀片布局
                            layoutIndex = x;
                        }
                        x++;
                    }
                    Map<String, String> collect = param.getPropertiesList().stream().collect(Collectors.toMap(s -> s.getPropertiesIndexId(), s -> s.getPropertiesValue()));
                    //获取所属机柜的布局数据
                    if (isBlade && collect != null && collect.containsKey(POSITIONBYCABINET.getField())) {
                        String value = collect.get(POSITIONBYCABINET.getField());
                        CabinetLayoutDataParam cdParam = JSONObject.parseObject(strValueConvert(value), CabinetLayoutDataParam.class);
                        Object layoutBay = layOutMapInfo.get(relationInstanceId);
                        if (layoutBay != null && layoutBay instanceof List) {
                            List<CabinetLayoutDataParam> cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(layoutBay), CabinetLayoutDataParam.class);
                            for (CabinetLayoutDataParam dataParam : cabinetLayoutInfo) {
                                //导入刀片在刀箱布局中的位置。
                                if (cdParam != null && layoutIndex != -1 && dataParam.getStart().equals(cdParam.getStart()) && dataParam.getEnd().equals(cdParam.getEnd())) {
                                    dataParam.setType(BLADE_VIEW);
                                    param.getPropertiesList().get(layoutIndex).setPropertiesValue(JSONObject.toJSONString(dataParam));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * 布局数据坐标校验
     *
     * @param layOutMapInfo      布局数据
     * @param value              导入值
     * @param modelView          校验的视图
     * @param relationInstanceId 关联实例Id
     * @param instanceViewType   实例布局类型
     * @return
     */
    private boolean checkLayout(Map<Integer, Object> layOutMapInfo, String value, Integer modelView, Integer
            relationInstanceId, String instanceViewType) {
        Object layoutInfo = layOutMapInfo.get(relationInstanceId);
        if (layoutInfo != null) {
            if (modelView.intValue() == 1) {//机柜实例数据，check所占用机房的布局
                List<List<QueryLayoutDataParam>> layoutData = new ArrayList<>();
                List<List> lists = new ArrayList();
                List<QueryLayoutDataParam> listLayoutDataParam = new ArrayList<>();
                lists = JSONArray.parseArray(JSONArray.toJSONString(layoutInfo), List.class);
                for (List listArr : lists) {
                    listLayoutDataParam = JSONArray.parseArray(JSONObject.toJSONString(listArr), QueryLayoutDataParam.class);
                    layoutData.add(listLayoutDataParam);
                }
                if (value.split(",").length > 1) {
                    Integer xVal = Integer.valueOf(value.split(",")[0]) - 1;
                    Integer yVal = Integer.valueOf(value.split(",")[1]) - 1;
                    //超出X轴界限
                    if (layoutData.size() < xVal) {
                        return true;
                    }
                    //超出Y轴界限
                    if (layoutData.get(0).size() < yVal) {
                        return true;
                    }
                    //所选位置已被禁选或者已被选择
                    QueryLayoutDataParam qParam = layoutData.get(xVal).get(yVal);
                    if (qParam.getIsBan() || qParam.getIsSelected()) {
                        return true;
                    }
                }
            }
            if (modelView.intValue() == 2) {//机柜下属设备数据，check所占用机柜的布局
                List<CabinetLayoutDataParam> cabinetLayoutInfo = new ArrayList<>();
                if (layoutInfo != null && layoutInfo instanceof List) {
                    cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(layoutInfo), CabinetLayoutDataParam.class);
                }
                //刀片视图布局占位校验
                if (instanceViewType.equals(BLADE_VIEW) && value.split("Bay").length > 1) {//3-12U/Bay1-2
                    String chassisLayout = value.split("/Bay")[0];
                    //所占机柜开始结束位置获取
                    MwModelViewLayoutDTO viewLayoutDTO = getStartAndEndIndex(chassisLayout);
                    //刀片所在位置:Bay1-2
                    String bayStr = value.split("/Bay")[1];
                    for (String bayIndexStr : bayStr.split("-")) {
                        int bayIndex = intValueConvert(bayIndexStr);
                        for (CabinetLayoutDataParam dataParam : cabinetLayoutInfo) {
                            //导入刀片在刀箱布局中的位置。
                            if (dataParam.getStart().equals(viewLayoutDTO.getStartIndex()) && dataParam.getEnd().equals(viewLayoutDTO.getEndIndex()) && CollectionUtils.isNotEmpty(dataParam.getDaoData())) {
                                int bayRow = dataParam.getDaoData().get(0).size();//行
                                //获取刀片所在布局的具体位置，几行几列
                                MwModelViewLayoutDTO bayViewLayoutDTO = getColAndRowIndex(bayIndex, bayRow);
                                int rowNum = bayViewLayoutDTO.getBayRow();
                                int colNum = bayViewLayoutDTO.getBayCol();
                                if (dataParam.getDaoData().size() <= rowNum || dataParam.getDaoData().get(rowNum).size() < colNum) {
                                    return true;
                                }
                                if (CollectionUtils.isEmpty(dataParam.getDaoData().get(rowNum)) || dataParam.getDaoData().get(rowNum).get(colNum) == null) {
                                    return true;
                                }
                                QueryBladeInstanceParam queryBladeInstanceParam = dataParam.getDaoData().get(rowNum).get(colNum);
                                if (!Strings.isNullOrEmpty(queryBladeInstanceParam.getInstanceId())) {//该位置的实例Id有值，表示已被占用
                                    return true;
                                }
                            }
                        }
                    }
                } else {
                    MwModelViewLayoutDTO viewLayoutDTO = getStartAndEndIndex(value);
                    Integer startVal = Integer.valueOf(viewLayoutDTO.getStartIndex());
                    Integer endVal = Integer.valueOf(viewLayoutDTO.getEndIndex());
                    //极限法 最小的大于最大的，或者最大的小于最小的，都不会重合。
                    for (int x = startVal; x < endVal; x++) {
                        for (CabinetLayoutDataParam dataParam : cabinetLayoutInfo) {
                            if ((startVal >= dataParam.getEnd() || endVal <= dataParam.getStart()) || !dataParam.getIsUsed()) {
                            } else {
                                return true;
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    /**
     * 验证EXCEL文件
     *
     * @param filePath
     * @return
     */
    public static boolean validateExcel(String filePath) {
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))) {
            errorMsg += "文件名不是excel格式";
            return false;
        }
        return true;
    }

    // @描述：是否是2003的excel，返回true是2003
    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    // @描述：是否是2007的excel，返回true是2007
    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

    private static SimpleDateFormat inSDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static SimpleDateFormat outSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat inSTime = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat outTime = new SimpleDateFormat("yyyy-MM-dd");

    public String formatDate(String inDate) {
        String outDate = "";
        if (inDate != null) {
            try {
                Date date = inSDF.parse(inDate);
                outDate = outSDF.format(date);
            } catch (Exception ex) {
            }
        }
        return outDate;
    }

    public String formatTime(String inDate) {
        String outDate = "";
        if (inDate != null) {
            try {
                Date date = inSTime.parse(inDate);
                outDate = outTime.format(date);
            } catch (Exception ex) {
            }
        }
        return outDate;
    }


    /**
     * 资产视图批量新增之数据导入
     *
     * @return
     */
    @Override
    public Reply batchInsertImportData(MultipartFile mFile) {
        String fileName = mFile.getOriginalFilename();// 获取文件名
        List<MwModelBatchAddByImportParam> list = new ArrayList<>();
        try {
            if (!validateExcel(fileName)) {// 验证文件名是否合格
                return Reply.fail(500, "导入文件不合格");
            }
            boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
            if (isExcel2007(fileName)) {
                isExcel2003 = false;
            }
            Workbook wb = null;
            if (isExcel2003) {// 当excel是2003时,创建excel2003
                wb = new HSSFWorkbook(mFile.getInputStream());
            } else {// 当excel是2007时,创建excel2007
                wb = new XSSFWorkbook(mFile.getInputStream());
            }
            // 读取Excel里面数据的信息
            list = readImportExcelValue(wb);
        } catch (Exception e) {
            log.error("读EXCEL文件失败", e);
            return Reply.fail(500, "读EXCEL文件失败");
        }
        return Reply.ok(list);
    }

    /**
     * 读取Excel里面客户的信息
     *
     * @param wb
     * @return
     */
    private List<MwModelBatchAddByImportParam> readImportExcelValue(Workbook wb) {
        int num = wb.getNumberOfSheets();
        errorMsg = new String();
        int errorNum = 0;
        List<MwModelBatchAddByImportParam> dtoList = new ArrayList<MwModelBatchAddByImportParam>();
        Map map = new HashMap();
        for (int x = 0; x < num; x++) {
            Sheet sheet = wb.getSheetAt(x);
            // 得到Excel的行数
            totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 1 && sheet.getRow(0) != null) {
                totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }
            // 循环Excel行数
            for (int r = 1; r < totalRows; r++) {
                MwModelBatchAddByImportParam dto = new MwModelBatchAddByImportParam();
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                boolean isflag = true;
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    Object cellValueObj = null;
                    Object cellValue = "";
                    if (null != cell) {
                        switch (cell.getCellType()) {
                            case NUMERIC: // 数字
                                cellValueObj = new DecimalFormat("0").format(cell.getNumericCellValue());
                                cellValue = cellValueObj.toString();
                                break;
                            case STRING: // 字符串
                                cellValueObj = cell.getStringCellValue();
                                cellValue = cellValueObj.toString();
                                break;
                            case BOOLEAN: // Boolean
                                cellValue = cell.getBooleanCellValue();
                                break;
                            default:
                                break;
                        }
                    }
                    if (c == 0) {
                        //资产名称
                        if (null == cellValue) {
                            if (errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,资产名称不能为空;";
                            }
                            errorNum++;
                            continue;
                        } else {
                            dto.setInstanceName(cellValue.toString());
                        }

                    } else if (c == 1) {
                        //IP地址
                        if (null == cellValue) {
                            isflag = false;
                            if (errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,IP地址不能为空;";
                            }
                            errorNum++;
                            continue;
                        } else {
                            Boolean flag = IpV4Util.isIP(cellValue.toString());
                            if (!flag) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,IP地址格式不正确;";
                                }
                                errorNum++;
                                continue;
                            }
                            dto.setInBandIp(cellValue.toString());
                        }

                    }
                }
                if (isflag) {//导入数据有错误，就不添加
                    dtoList.add(dto);
                }
            }
        }
        return dtoList;
    }


    /**
     * web监测的信息数据导入
     *
     * @return
     */
    @Override
    public Reply importWebMonitorData(MultipartFile mFile) {
        String fileName = mFile.getOriginalFilename();// 获取文件名
        ModelWebMonitorParam param = new ModelWebMonitorParam();
        try {
            if (!validateExcel(fileName)) {// 验证文件名是否合格
                return Reply.fail(500, "导入文件不合格");
            }
            boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
            if (isExcel2007(fileName)) {
                isExcel2003 = false;
            }
            Workbook wb = null;
            if (isExcel2003) {// 当excel是2003时,创建excel2003
                wb = new HSSFWorkbook(mFile.getInputStream());
            } else {// 当excel是2007时,创建excel2007
                wb = new XSSFWorkbook(mFile.getInputStream());
            }
            // 读取Excel里面数据的信息
            param = readWebMonitorImportExcelValue(wb);
        } catch (Exception e) {
            log.error("读EXCEL文件失败", e);
            return Reply.fail(500, "读EXCEL文件失败");
        }
        return Reply.ok(param);
    }

    @Override
    public List<MwModelPowerDTO> selectGroupIdInfo() {
        List<MwModelPowerDTO> mwModelPowerDTOS = mwModelExportDao.selectGroupIdInfo();
        return mwModelPowerDTOS;
    }

    @Override
    public List<MwModelPowerDTO> selectOrgIdInfo() {
        List<MwModelPowerDTO> mwModelPowerDTOS = mwModelExportDao.selectOrgIdInfo();
        return mwModelPowerDTOS;
    }

    @Override
    public List<MwModelPowerDTO> selectUserIdInfo() {
        List<MwModelPowerDTO> mwModelPowerDTOS = mwModelExportDao.selectUserIdInfo();
        return mwModelPowerDTOS;
    }

    private MwModelViewLayoutDTO getStartAndEndIndex(String layoutIndex) {
        MwModelViewLayoutDTO viewLayoutDTO = new MwModelViewLayoutDTO();
        if (layoutIndex.split("-").length > 1) {
            String chassisStart = layoutIndex.split("-")[0];
            String chassisEnd = layoutIndex.split("-")[1];
            if (chassisStart.endsWith("U") || chassisStart.endsWith("u")) {
                chassisStart = chassisStart.substring(0, chassisStart.length() - 1);
            }
            if (chassisEnd.endsWith("U") || chassisEnd.endsWith("u")) {
                chassisEnd = chassisEnd.substring(0, chassisEnd.length() - 1);
            }
            viewLayoutDTO.setStartIndex(intValueConvert(chassisStart) - 1);
            viewLayoutDTO.setEndIndex(intValueConvert(chassisEnd) - 1);
        }
        return viewLayoutDTO;
    }

    private MwModelViewLayoutDTO getColAndRowIndex(int bayIndex, int bayRow) {
        MwModelViewLayoutDTO viewLayoutDTO = new MwModelViewLayoutDTO();
        int row = bayIndex / bayRow;
        int col = bayIndex % bayRow - 1;
        if (col < 0) {//余数小于0，则往后退一位，退至上一行最后一位
            row = row - 1;
            col = col + bayRow;
        }
        viewLayoutDTO.setBayRow(row);
        viewLayoutDTO.setBayCol(col);
        return viewLayoutDTO;
    }


    private void setBladeLayoutValue(List<CabinetLayoutDataParam> cabinetLayoutInfo, String layoutInfo, String
            instanceName, CabinetLayoutDataParam cabinetLayout, String instanceId) {
        MwModelViewLayoutDTO viewLayoutDTO = getStartAndEndIndex(layoutInfo.split("/Bay")[0]);
        for (CabinetLayoutDataParam dataParam : cabinetLayoutInfo) {
            //导入刀片在刀箱布局中的位置。
            if (dataParam.getStart().equals(viewLayoutDTO.getStartIndex()) && dataParam.getEnd().equals(viewLayoutDTO.getEndIndex())) {
                //获取刀片布局
                List<List<QueryBladeInstanceParam>> bayData = dataParam.getDaoData();
                //设置当前资产刀片布局数据。
                String bayStr = layoutInfo.split("Bay")[1];
                if (bayStr.split("-").length > 0) {//刀片多占位
                    for (String bayIndexStr : bayStr.split("-")) {
                        int bayIndex = intValueConvert(bayIndexStr);
                        if (CollectionUtils.isNotEmpty(dataParam.getDaoData())) {
                            int bayRow = bayData.get(0).size();//行
                            //获取刀片所在布局的具体位置，几行几列
                            MwModelViewLayoutDTO bayViewLayoutDTO = getColAndRowIndex(bayIndex, bayRow);
                            int rowNum = bayViewLayoutDTO.getBayRow();
                            int colNum = bayViewLayoutDTO.getBayCol();
                            bayData.get(rowNum).get(colNum).setCurrentFlag(true);
                            bayData.get(rowNum).get(colNum).setInstanceName(instanceName);
                            bayData.get(rowNum).get(colNum).setInstanceId(instanceId);
                        }
                    }
                }
                cabinetLayout.setDaoData(bayData);
                cabinetLayout.setInfo(dataParam.getInfo());
            }
        }

    }


    /**
     * 读取Excelweb监测的信息
     *
     * @param wb
     * @return
     */
    private ModelWebMonitorParam readWebMonitorImportExcelValue(Workbook wb) {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        int num = wb.getNumberOfSheets();
        errorMsg = new String();
        int errorNum = 0;
        List<MwModelImportWebMonitorParam> dtoList = new ArrayList<MwModelImportWebMonitorParam>();
        Map map = new HashMap();

        //获取厂别，领域的对应数据。
        List<MwModelInstanceCommonParam> systemAndClassifyInfo = mwModelInstanceDao.getSystemAndClassifyInfo();
        Map<String, Integer> collect = systemAndClassifyInfo.stream().collect(Collectors.toMap(s -> s.getModelInstanceName(), s -> s.getModelInstanceId(), (
                value1, value2) -> {
            return value2;
        }));

        for (int x = 0; x < num; x++) {
            Sheet sheet = wb.getSheetAt(x);
            // 得到Excel的行数
            totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 1 && sheet.getRow(0) != null) {
                totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }
            // 循环Excel行数
            for (int r = 1; r < totalRows; r++) {
                MwModelImportWebMonitorParam dto = new MwModelImportWebMonitorParam();
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                boolean isflag = true;
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    Object cellValueObj = null;
                    Object cellValue = "";
                    if (null != cell) {
                        switch (cell.getCellType()) {
                            case NUMERIC: // 数字
                                cellValueObj = new DecimalFormat("0").format(cell.getNumericCellValue());
                                cellValue = cellValueObj.toString();
                                break;
                            case STRING: // 字符串
                                cellValueObj = cell.getStringCellValue();
                                cellValue = cellValueObj.toString();
                                break;
                            case BOOLEAN: // Boolean
                                cellValue = cell.getBooleanCellValue();
                                break;
                            default:
                                break;
                        }
                    }
                    if (c == 0) {
                        //网站名称
                        if (null == cellValue || cellValue.equals("")) {
                            if (errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,网站名称不能为空;";
                            }
                            errorNum++;
                            continue;
                        } else {
                            dto.setInstanceName(cellValue.toString());
                        }

                    } else if (c == 1) {
                        //网站Url
                        if (null == cellValue || cellValue.equals("")) {
                            isflag = false;
                            if (errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,网站Url不能为空;";
                            }
                            errorNum++;
                            continue;
                        } else {
                            dto.setWebUrl(cellValue.toString());
                        }

                    } else if (c == 2) {
                        //调用服务器IP
                        if (null == cellValue || cellValue.equals("")) {
                            isflag = false;
                            if (errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,调用服务器IP不能为空;";
                            }
                            errorNum++;
                            continue;
                        } else {
                            Boolean flag = IpV4Util.isIP(cellValue.toString());
                            if (!flag) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,IP地址格式不正确;";
                                }
                                errorNum++;
                                continue;
                            }
                            List<Map<String, Object>> ckeckDTO = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                                    .inBandIp(cellValue.toString()).monitorMode(1).assetsTypeId(1).build());
                            if ((CollectionUtils.isEmpty(ckeckDTO) || ckeckDTO.get(0) == null) && errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,Ip地址未对应资产服务器;";
                                errorNum++;
                                continue;
                            }
                            if ((ckeckDTO.get(0).get(ASSETS_ID) == null || ckeckDTO.get(0).get(ASSETS_ID).toString().equals("")) && errorNum < ERRORNUM) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,Ip地址对应的资产服务器未纳管;";
                                errorNum++;
                                continue;
                            }
                            dto.setInBandIp(cellValue.toString());
                            dto.setAssetsId(ckeckDTO.get(0).get(ASSETS_ID).toString());
                            dto.setAssetsName(ckeckDTO.get(0).get(INSTANCE_NAME_KEY).toString());
                            dto.setMonitorServerId(ckeckDTO.get(0).get(MONITOR_SERVER_ID).toString());
                            dto.setIsManage(true);
                            dto.setClient(101);
                        }

                    } else if (c == 3) {
                        //更新间隔
                        if (null == cellValue || cellValue.equals("")) {
                            cellValue = 120;//默认值
                        }
                        //判断是否是数字
                        boolean isNum = pattern.matcher(cellValue.toString()).matches();
                        if (!isNum && errorNum < ERRORNUM) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,更新间隔必须是数字;";
                            continue;
                        }
                        dto.setUpdateInterval(Integer.valueOf(cellValue.toString()));
                    } else if (c == 4) {
                        //尝试次数
                        if (null == cellValue || cellValue.equals("")) {
                            cellValue = 1;
                        }
                        //判断是否是数字
                        boolean isNum = pattern.matcher(cellValue.toString()).matches();
                        if (!isNum && errorNum < ERRORNUM) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,尝试次数必须是数字;";
                            continue;
                        }
                        dto.setAttempts(Integer.valueOf(cellValue.toString()));
                    } else if (c == 5) {
                        //启用状态
                        if (null == cellValue || cellValue.equals("")) {
                            cellValue = true;
                        }
                        dto.setEnable(booleanValueConvert(cellValue));
                    } else if (c == 6) {
                        //超时时间
                        if (null == cellValue || cellValue.equals("")) {
                            cellValue = 10;
                        }
                        boolean isNum = pattern.matcher(cellValue.toString()).matches();
                        if (!isNum && errorNum < ERRORNUM) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,超时时间必须是数字;";
                            continue;
                        }
                        dto.setTimeOut(Integer.valueOf(cellValue.toString()));
                    } else if (c == 7) {
                        //必要状态码
                        if (null == cellValue || cellValue.equals("")) {
                            cellValue = 200;
                        }
                        boolean isNum = pattern.matcher(cellValue.toString()).matches();
                        if (!isNum && errorNum < ERRORNUM) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列,状态码必须是数字;";
                            continue;
                        }
                        dto.setStatusCode(cellValue.toString());
                    } else if (c == 8) {
                        //负责人
                        List<Integer> userIds = new ArrayList<>();
                        if (null == cellValue || cellValue.equals("")) {
                            Integer userId = 106;
                            if (iLoginCacheInfo != null && iLoginCacheInfo.getLoginName() != null
                                    && iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()) != null) {
                                userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
                            }
                            userIds.add(userId);
                        } else {
                            String[] userArr = cellValue.toString().split("/");
                            String userNames = StringUtils.join(userArr, ",");
                            userIds = mwModelExportDao.selectUserIdByName(userNames);
                            if (userIds == null) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该用户;";
                                }
                                errorNum++;
                                break;//跳过当前行数据循环
                            }
                            dto.setUserIds(userIds);
                        }

                    } else if (c == 9) {
                        //部门/机构
                        if (null == cellValue || cellValue.equals("")) {
                            continue;
                        } else {
                            String[] orgArr = cellValue.toString().split("/");
                            String orgNames = StringUtils.join(orgArr, ",");
                            List<String> orgIds = mwModelExportDao.selectOrgIdByName(orgNames);
                            if (orgIds == null) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该部门机构；";
                                }
                                errorNum++;
                                break;//跳过当前行数据循环
                            }
                            List orgIdList = new ArrayList();
                            for (String str : orgIds) {
                                List<String> list = Arrays.asList(str.split(","));
                                list.removeAll(new ArrayList<>());
                                List<Integer> listInt = list.stream().map(Integer::valueOf).collect(Collectors.toList());
                                orgIdList.add(listInt);
                            }
                            dto.setOrgIds(orgIdList);
                        }

                    } else if (c == 10) {
                        //用户组
                        if (null != cellValue && !cellValue.toString().equals("")) {
                            String[] groupArr = cellValue.toString().split("/");
                            String groupNames = StringUtils.join(groupArr, ",");
                            List<Integer> groupIds = mwModelExportDao.selectGroupIdByName(groupNames);
                            if (groupIds == null || groupIds.size() != groupArr.length) {
                                if (errorNum < ERRORNUM) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该用户组；";
                                }
                                errorNum++;
                            }
                            dto.setGroupIds(groupIds);
                        }

                    } else if (c == 11) {
                        //厂别
                        if (cellValue == null || cellValue.equals("")) {
                            continue;
                        }
                        if (collect == null || !collect.containsKey(strValueConvert(cellValue))) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该厂别信息;";
                            errorNum++;
                            continue;
                        }
                        dto.setModelSystem(collect.get(strValueConvert(cellValue)) + "");
                    } else if (c == 12) {
                        //领域
                        if (cellValue == null || cellValue.equals("")) {
                            continue;
                        }
                        if (collect == null || !collect.containsKey(strValueConvert(cellValue))) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，未查到该领域信息;";
                            errorNum++;
                            continue;
                        }
                        dto.setModelClassify(collect.get(strValueConvert(cellValue)) + "");
                    }
                }
                if (isflag) {//导入数据有错误，就不添加
                    dtoList.add(dto);
                }
            }
        }
        ModelWebMonitorParam param = new ModelWebMonitorParam();
        param.setImportWebList(dtoList);
        param.setErrorMessage(errorMsg);
        return param;
    }

}
