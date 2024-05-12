package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.model.dao.MWModelTemplateDao;
import cn.mw.monitor.model.dao.MWModelVendorDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dto.MwModelInfoDTO;
import cn.mw.monitor.model.dto.MwModelTemplateNamesDto;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MWModelTemplateService;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.model.param.MwModelTemplateDTO;
import cn.mw.monitor.service.model.param.MwModelTemplateTable;
import cn.mw.monitor.service.model.param.MwModelZabbixTemplateParam;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.tpserver.api.MwCommonsTPServer;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWTPServerProxy;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.util.ListMapObjUtils.objectsToMaps;

/**
 * @author qzg
 * @date 2022/5/05
 */
@Service
@Slf4j
public class MWModelTemplateServiceImpl implements MWModelTemplateService {
    @Resource
    MWModelTemplateDao mwModelTemplateDao;
    @Autowired
    ILoginCacheInfo iLoginCacheInfo;
    @Resource
    MwModelManageDao mwModelManageDao;
    @Autowired
    private MwCommonsTPServer mwCommonsTPServer;
    @Resource
    MWModelVendorDao mwModelVendorDao;

    @Resource
    private MWWebZabbixManger mwWebZabbixManger;

    @Resource
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwModelViewServiceImpl mwModelViewService;

    // 总行数
    private static int totalRows = 0;
    // 总条数
    private static int totalCells = 0;
    // 错误信息接收器
    private static String errorMsg = "";


    @Override
    public Reply addModelTemplate(AddAndUpdateModelTemplateParam aParam) {
        try {
            List<String> items = new ArrayList<>();
            //重复性校验
            List<MwModelTemplateTable> check = new ArrayList<>();
            if (!StringUtils.isEmpty(aParam.getDescription())) {
                check = mwModelTemplateDao.check(QueryModelTemplateParam.builder()
                        .description(aParam.getDescription())
                        .templateName(aParam.getTemplateName())
                        .specification(aParam.getSpecification()).build());
            }
            if (check.size() >= 1) {
                items.add("特征信息不能重复");
            }
            List<MwModelTemplateTable> check1 = new ArrayList<>();
            if (!StringUtils.isEmpty(aParam.getSystemObjid())) {
                check1 = mwModelTemplateDao.check(QueryModelTemplateParam.builder()
                        .systemObjid(aParam.getSystemObjid())
                        .templateName(aParam.getTemplateName())
                        .specification(aParam.getSpecification()).build());
            }
            if (check1.size() >= 1) {
                items.add("系统oid不能重复");
            }
            if (items.size() > 0) {
                return Reply.fail(StringUtils.join(new String[]{ErrorConstant.ASSETSTEMPLATE_MSG_280101, StringUtils.join(items, "、 ")}));
            }
            aParam.setCreator(iLoginCacheInfo.getLoginName());
            aParam.setModifier(iLoginCacheInfo.getLoginName());

            if (aParam.getTemplateName() != null && !"".equals(aParam.getTemplateName())) {
                mwModelTemplateDao.insert(aParam);
            } else {
                return Reply.fail("模板名称不能为空！");
            }

            //根据多zabbix 更新mapper表信息
            List<MwModelZabbixTemplateParam> templateServerTables = new CopyOnWriteArrayList<MwModelZabbixTemplateParam>();
            List<String> zabbixInfo = new ArrayList<>();
            if (aParam.getId() != null && !aParam.getId().equals(0)) {
                List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
                for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.templateGet(mwtpServerAPI.getServerId()
                            , aParam.getTemplateName(), true);
                    if (mwZabbixAPIResult.getCode() == 0) {
                        JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                        if (node.size() > 0) {
                            String templateid = "";
                            for (JsonNode data : node) {
                                templateid = data.get("templateid").asText();
                            }
                            MwModelZabbixTemplateParam mwModelZabbixTemplateParam = new MwModelZabbixTemplateParam();
                            mwModelZabbixTemplateParam.setAssetstemplateId(aParam.getId());
                            mwModelZabbixTemplateParam.setServerId(mwtpServerAPI.getServerId());
                            mwModelZabbixTemplateParam.setTemplateId(intValueConvert(templateid));
                            templateServerTables.add(mwModelZabbixTemplateParam);
                        } else {
                            MwModelTPServerTable tpServerDTO = mwModelTemplateDao.selectTPServerById(mwtpServerAPI.getServerId());
                            zabbixInfo.add(tpServerDTO.getMonitoringServerName());
                        }
                    }
                }
            }
            //批量插入模版及第三方服务器映射表
            if (templateServerTables.size() > 0) {
                mwModelTemplateDao.insertBatchTemplateServerMap(templateServerTables);
            }
            if (zabbixInfo.size() > 0) {
                String join = StringUtils.join(new String[]{StringUtils.join(zabbixInfo, "、 ")});
                return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280101, join + "中没有相关模板！");
            }
            log.info("EngineManage_LOG[]EngineManage[]资产模板管理[]新增资产模板信息[]{}", aParam);
            return Reply.ok("新增成功！");
        } catch (Throwable e) {
            log.error("fail to addModelTemplate param{}, case by {}", aParam, e);
            return Reply.fail(500, "新增模板数据失败");
        }
    }

    @Override
    public Reply updateModelTemplate(AddAndUpdateModelTemplateParam auParam) {
        try {
            List<String> items = new ArrayList<>();
            //重复性校验
            List<MwModelTemplateTable> check = new ArrayList<>();
            if (!StringUtils.isEmpty(auParam.getDescription())) {
                check = mwModelTemplateDao.check(QueryModelTemplateParam.builder()
                        .id(auParam.getId())
                        .description(auParam.getDescription())
                        .templateName(auParam.getTemplateName())
                        .specification(auParam.getSpecification()).build());
            }
            if (check.size() >= 1) {
                items.add("特征信息不能重复");
            }
            List<MwModelTemplateTable> check1 = new ArrayList<>();
            if (!StringUtils.isEmpty(auParam.getSystemObjid())) {
                check1 = mwModelTemplateDao.check(QueryModelTemplateParam.builder()
                        .id(auParam.getId())
                        .systemObjid(auParam.getSystemObjid())
                        .templateName(auParam.getTemplateName())
                        .specification(auParam.getSpecification()).build());
            }
            if (check1.size() >= 1) {
                items.add("系统oid不能重复");
            }
            if (items.size() > 0) {
                return Reply.fail(StringUtils.join(new String[]{ErrorConstant.ASSETSTEMPLATE_MSG_280102, StringUtils.join(items, "、 ")}));
            }
            auParam.setModifier(iLoginCacheInfo.getLoginName());

            mwModelTemplateDao.update(auParam);
            //根据多zabbix 更新mapper表信息
            List<String> zabbixInfo = new ArrayList<>();
            List<MwModelZabbixTemplateParam> templateServerTables = new CopyOnWriteArrayList<MwModelZabbixTemplateParam>();
            List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
            for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.templateGet(mwtpServerAPI.getServerId()
                        , auParam.getTemplateName(), true);
                if (mwZabbixAPIResult.getCode() == 0) {
                    JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                    if (node.size() > 0) {
                        String templateid = "";
                        for (JsonNode data : node) {
                            templateid = data.get("templateid").asText();
                        }
                        MwModelZabbixTemplateParam mwModelZabbixTemplateParam = new MwModelZabbixTemplateParam();
                        mwModelZabbixTemplateParam.setAssetstemplateId(auParam.getId());
                        mwModelZabbixTemplateParam.setServerId(mwtpServerAPI.getServerId());
                        mwModelZabbixTemplateParam.setTemplateId(intValueConvert(templateid));
                        templateServerTables.add(mwModelZabbixTemplateParam);
                    } else {
                        MwModelTPServerTable tpServerDTO = mwModelTemplateDao.selectTPServerById(mwtpServerAPI.getServerId());
                        zabbixInfo.add(tpServerDTO.getMonitoringServerName());
                    }
                }
            }
            //先根据模板id删除mapper
            List<Integer> ids = new ArrayList<>();
            ids.add(auParam.getId());
            mwModelTemplateDao.deleteBatchTemplateServerMap(ids);
            //插入模版及第三方服务器映射表
            if (templateServerTables.size() > 0) {
                mwModelTemplateDao.insertBatchTemplateServerMap(templateServerTables);
            }
            if (zabbixInfo.size() > 0) {
                String join = StringUtils.join(new String[]{StringUtils.join(zabbixInfo, "、 ")});
                return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280102, join + "中没有相关模板！");
            }
            log.info("EngineManage_LOG[]EngineManage[]资产模板管理[]更新资产模板信息[]{}", auParam);
            return Reply.ok("更新成功！");
        } catch (Throwable e) {
            log.error("fail to updateModelTemplate param{}, case by {}", auParam, e);
            return Reply.fail(500, "模板信息更新失败");
        }
    }

    @Override
    public Reply checkModelFirmByName(AddAndUpdateModelFirmParam param) {
        try {
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to checkModelFirmByName param{}, case by {}", param, e);
            return Reply.fail(500, "模板信息更新失败");
        }
    }

    @Override
    public Reply queryModelTemplate(QueryModelTemplateParam param) {
        try {
            Map criteria = PropertyUtils.describe(param);

            QueryInstanceModelParam instanceParam = new QueryInstanceModelParam();
            mwModelViewService.getInstanceListData(instanceParam);
            instanceParam.setPageSize(10000);
            Map<String, Object> map = mwModelViewService.getModelListInfoByBase(instanceParam);
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }
            List<MwModelTemplateInfo> templateInfos = mwModelTemplateDao.getAssetsTemplateId();
            Map<String, String> templateInfoMap = new HashMap();
            //将模板数据的服务器模板Id、服务器Id、模板规格型号 作为key，模板Id作为value，用于资产数据获取assetsTemplateId
            for (MwModelTemplateInfo templateInfo : templateInfos) {
                String key = templateInfo.getServerTemplateId() + "_" + templateInfo.getServerId() + "_" + templateInfo.getSpecification();
                String value = templateInfo.getTemplateId();
                templateInfoMap.put(key, value);
            }

            Map<String, List<Integer>> instanceInfoMap = new HashMap();
            List<Integer> instanceIds = new ArrayList<>();
            for (Map<String, Object> instanceMap : listMap) {
                if (instanceMap.get("templateId") != null && instanceMap.get("templateId") != "") {
                    String templateId = instanceMap.get("templateId") != null ? instanceMap.get("templateId").toString() : "";
                    String monitorServerId = instanceMap.get("monitorServerId") != null ? instanceMap.get("monitorServerId").toString() : "";
                    String specifications = instanceMap.get("specifications") != null ? instanceMap.get("specifications").toString() : "";
                    Integer modelInstanceId = intValueConvert(instanceMap.get("modelInstanceId"));
                    String assetsKey = templateId + "_" + monitorServerId + "_" + specifications;
                    if (templateInfoMap != null && templateInfoMap.size() > 0 && templateInfoMap.get(assetsKey) != null) {
                        String assetsTemplateId = templateInfoMap.get(assetsKey);
                        if (instanceInfoMap.containsKey(assetsTemplateId)) {
                            instanceIds = instanceInfoMap.get(assetsTemplateId);
                        } else {
                            instanceIds = new ArrayList<>();
                        }
                        instanceIds.add(modelInstanceId);
                        instanceInfoMap.put(assetsTemplateId, instanceIds);
                    }
                }
            }

            List<MwModelTemplateDTO> mwScanList = mwModelTemplateDao.selectTepmplateTableList(criteria);
            mwScanList.forEach(template -> {
                if (template != null && template.getGroupNodes() != null) {
                    List<String> list = Arrays.asList(template.getGroupNodes().substring(1).split(","));
                    List<Integer> lists = list.stream().map(Integer::parseInt).collect(Collectors.toList());
                    template.setModelGroupIdList(lists);
                }
                String assetsTemplateId = template.getId().toString();
                template.setAssetsIds(new ArrayList<>());
                template.setAssetsCount(0);
                if (instanceInfoMap != null && instanceInfoMap.size() > 0) {
                    List<Integer> instanceIdList = instanceInfoMap.get(assetsTemplateId);
                    if (CollectionUtils.isNotEmpty(instanceIdList)) {
                        template.setAssetsIds(instanceIdList);
                        template.setAssetsCount(instanceIdList.size());
                    }
                }
            });
            PageList pageList = new PageList();
            PageInfo pageInfo = new PageInfo<>(mwScanList);
            pageInfo.setTotal(mwScanList.size());
            mwScanList = pageList.getList(mwScanList, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(mwScanList);
            log.info("EngineManage_LOG[]EngineManage[]资产模板管理[]分页查询资产模板信息[]{}[]", mwScanList);
            return Reply.ok(pageInfo);
        } catch (Throwable e) {
            log.error("fail to queryModelTemplate param{}, case by {}", param, e);
            return Reply.fail(500, "模板信息查询失败");
        }
    }


    @Override
    public Reply popupBrowseModelTemplate(QueryModelTemplateParam param) {
        try {
            MwModelTemplateDTO msDto = mwModelTemplateDao.selectTemplateById(param.getId());
            if (msDto != null && msDto.getGroupNodes() != null) {
                List<String> list = Arrays.asList(msDto.getGroupNodes().substring(1).split(","));
                List<Integer> lists = list.stream().map(Integer::parseInt).collect(Collectors.toList());
                msDto.setModelGroupIdList(lists);
            }
            log.info("EngineManage_LOG[]EngineManage[]资产模板管理[]根据自增序列ID取资产模板信息[]{}", param.getId());
            return Reply.ok(msDto);
        } catch (Throwable e) {
            log.error("fail to popupBrowseModelTemplate param{}, case by {}", param, e);
            return Reply.fail(500, "模板信息编辑查询失败");
        }
    }

    @Override
    public Reply selectList(QueryModelTemplateParam param) {
        try {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            Map criteria = PropertyUtils.describe(param);
            List<MwModelTemplateDTO> list = mwModelTemplateDao.selectList(criteria);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to selectList param{}, case by {}", param, e);
            return Reply.fail(500, "模板信息查询失败");
        }
    }

    @Override
    public Reply deleteModelTemplate(AddAndUpdateModelTemplateParam param) {
        try {
            //删除资产模板数据
            mwModelTemplateDao.deleteBatch(param.getIdList());
            mwModelTemplateDao.deleteBatchTemplateServerMap(param.getIdList());
            log.info("EngineManage_LOG[]EngineManage[]资产模板管理[]删除资产模板信息[]{}", param.getIdList());
            return Reply.ok("删除成功");
        } catch (Throwable e) {
            log.error("fail to deleteModelTemplate param{}, case by {}", param, e);
            return Reply.fail(500, "模板信息删除失败");
        }
    }

    @Override
    public Reply fuzzSearchAllFiledData(String value) {
        //根据值模糊查询数据
        List<Map<String, String>> fuzzSeachAllFileds = mwModelTemplateDao.fuzzSearchAllFiled(value);
        Set<String> fuzzSeachData = new HashSet<>();
        if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                String templateName = fuzzSeachAllFiled.get("templateName");
                String systemObjid = fuzzSeachAllFiled.get("systemObjid");
                String description = fuzzSeachAllFiled.get("description");
                String brand = fuzzSeachAllFiled.get("brand");
                String specification = fuzzSeachAllFiled.get("specification");
                String creator = fuzzSeachAllFiled.get("creator");
                String modifier = fuzzSeachAllFiled.get("modifier");
                String typeName = fuzzSeachAllFiled.get("typeName");
                String subTapeName = fuzzSeachAllFiled.get("subTapeName");
                String monitorModeName = fuzzSeachAllFiled.get("monitorModeName");
                if (StringUtils.isNotBlank(templateName) && templateName.contains(value)) {
                    fuzzSeachData.add(templateName);
                }
                if (StringUtils.isNotBlank(systemObjid) && systemObjid.contains(value)) {
                    fuzzSeachData.add(systemObjid);
                }
                if (StringUtils.isNotBlank(description) && description.contains(value)) {
                    fuzzSeachData.add(description);
                }
                if (StringUtils.isNotBlank(brand) && brand.contains(value)) {
                    fuzzSeachData.add(brand);
                }
                if (StringUtils.isNotBlank(specification) && specification.contains(value)) {
                    fuzzSeachData.add(specification);
                }
                if (StringUtils.isNotBlank(typeName) && typeName.contains(value)) {
                    fuzzSeachData.add(typeName);
                }
                if (StringUtils.isNotBlank(subTapeName) && subTapeName.contains(value)) {
                    fuzzSeachData.add(subTapeName);
                }
                if (StringUtils.isNotBlank(creator) && creator.contains(value)) {
                    fuzzSeachData.add(creator);
                }
                if (StringUtils.isNotBlank(modifier) && modifier.contains(value)) {
                    fuzzSeachData.add(modifier);
                }
                if (StringUtils.isNotBlank(monitorModeName) && monitorModeName.contains(value)) {
                    fuzzSeachData.add(monitorModeName);
                }
            }
        }
        Map<String, Set<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
        return Reply.ok(fuzzyQuery);
    }

    @Override
    public Reply templateGet() {
        try {
            Reply reply = mwCommonsTPServer.selectByMainServer();
            if (reply.getData() != null && (int) reply.getData() != 0) {
                List templateList = mwWebZabbixManger.templateGet((int) reply.getData(), null);
                return Reply.ok(templateList);
            } else {
                return Reply.fail("无监控服务器，没有关联模板");
            }
        } catch (Throwable e) {
            log.error("fail to templateGet param{}, case by {}", e);
            return Reply.fail(500, "获取zabbix关联模板失败");
        }
    }

    /**
     * 关联template_id批量更新
     *
     * @return
     */
    @Override
    public Reply updateModelTemplateByMore() {
        try {
            updateAssetsTemplate();
            updateAssetsGroupId();
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to updateModelTemplateByMore param{}, case by {}", e);
            return Reply.fail(500, "获取zabbix关联模板失败");
        }
    }

    @Override
    public Reply templateInfoExport(QueryModelTemplateExportParam qParam, HttpServletResponse response) {
        try {
            Map criteria = PropertyUtils.describe(qParam);
            List<MwModelTemplateDTO> mwScanList = mwModelTemplateDao.selectTepmplateTableList(criteria);
            mwScanList.forEach(template -> {
                Integer count = 0;
                if (template != null && template.getAssetsIds() != null && template.getAssetsIds().size() > 1) {
                    List<Integer> assetsIds = template.getAssetsIds();
                    count = assetsIds.size();
                }
                template.setAssetsCount(count);
            });
            List<Map> mapList = objectsToMaps(mwScanList);
            List<String> lable = qParam.getHeader();
            List<String> lableName = qParam.getHeaderName();
            ExportExcel.exportExcel("模板管理列表数据导出", "模板管理列表数据导出", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
        } catch (Exception e) {
            log.error("模板管理列表数据导出失败templateInfoExport{}", e);
            return Reply.fail(500, "模板管理列表数据导出失败");
        }
        return Reply.ok("导出成功");
    }

    @Override
    public Reply templateInfoImport(MultipartFile file, HttpServletResponse response) {
        try {
            String fileName = file.getOriginalFilename();
            String insertMessage = "";
            int count = 0;
            int error = 0;
            int success = 0;

            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                Map map = getExcelInfo(file);
                if (map != null && map.get("dataList") != null) {
                    List<AddAndUpdateModelTemplateParam> list = (List<AddAndUpdateModelTemplateParam>) map.get("dataList");
                    String errorMsg = map.get("errorMsg").toString();
                    for (AddAndUpdateModelTemplateParam param : list) {
                        Reply insertReply = null;
                        try {
                            insertReply = addModelTemplate(param);
                            if (null != insertReply) {
                                if (insertReply.getRes() == PaasConstant.RES_SUCCESS) {
                                    //新增成功
                                    success++;
                                } else {
                                    errorMsg += insertReply.getMsg() + "；";
                                    count++;
                                }
                            } else {
                                count++;
                            }
                        } catch (RuntimeException e) {
                            //重复数据
                            count++;
                        }
                    }
                    if (list == null || list.size() == 0) {
                        return Reply.fail("数据导入失败。" + errorMsg);
                    } else {
                        if (success > 0) {
                            //错误数据 ((totalRows-1) - success) > 0;
                            if (count > 0) {
                                return Reply.warn("部分数据导入成功，" + count + "条重复数据已被忽略。" + errorMsg);
                            }
                            if (success == (totalRows - 1)) {
                                return Reply.ok("数据导入成功。" + errorMsg);
                            } else {
                                return Reply.warn("部分数据导入成功。" + errorMsg);
                            }
                        } else {
                            if (count > 0) {
                                return Reply.fail("数据导入失败，" + count + "条重复数据。" + errorMsg);
                            }
                        }
                    }
                } else {
                    return Reply.fail("导入失败！");
                }
            } else {
                log.error("没有传入正确的excel文件名", file);
            }
            return Reply.ok("导入成功！");
        } catch (Exception e) {
            log.error("fail to templateInfoImport with MultipartFile={}, cause:{}", file, e);
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280106, ErrorConstant.ASSETSTEMPLATE_MSG_280106);
        }
    }

    /**
     * 模板导出
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    public Reply exportByFormwork(HttpServletRequest request, HttpServletResponse response) {
        try {
            List<Map> mapList = new ArrayList<>();
            List<String> lable = Arrays.asList("brand", "specification", "monitorMode", "systemObjid", "interfacesType", "templateName", "assetsTypeName", "subAssetsTypeName", "description");
            List<String> lableName = Arrays.asList("厂商(必填)", "规格型号(必填)", "监控方式(必填)", "系统OID(监控方式为SNMP时,必填)", "类型(输入0或4;0:默认；4:JMX)", "关联模板名称(必填)", "资产类型(必填)", "子类型(必填)", "特征信息");
            ExportExcel.exportExcel("模板管理模板导出", "模板管理模板导出", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
            return Reply.ok("导出成功！");
        } catch (Exception e) {
            log.error("fail to exportByFormwork with MultipartFile={}, cause:{}", e);
            return Reply.fail(500, "模板导出失败");
        }
    }

    @Override
    public Reply updateTemplateStatus(List<UpdateModelTemplateStatusParam> list) {
        try {
            mwModelTemplateDao.updateTemplateStatus(list);
            return Reply.ok("模板状态设置成功");
        } catch (Exception e) {
            log.error("fail to updateTemplateStatus cause:{}", e);
            return Reply.fail(500, "模板状态设置失败");
        }
    }

    public Reply updateAssetsTemplate() {
        try {
            //清空模版及第三方服务器映射表
            mwModelTemplateDao.cleanTemplateServerMap();
            List<MwModelTemplateNamesDto> templateNamesDtos = mwModelTemplateDao.selectTemplateNames();
            List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
            List<MwModelZabbixTemplateParam> mwModelZabbixTemplateParams = new CopyOnWriteArrayList<MwModelZabbixTemplateParam>();
            for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                //获取所有的模板数据信息
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.templateGet(mwtpServerAPI.getServerId()
                        , null, true);
                if (mwZabbixAPIResult.getCode() == 0) {
                    JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                    if (node.size() > 0) {
                        String templateid = "";
                        for (JsonNode data : node) {
                            String templateName = data.get("name").asText();
                            for (MwModelTemplateNamesDto templateNamesDto : templateNamesDtos) {
                                if (templateNamesDto.getTemplateName().equals(templateName)) {
                                    templateid = data.get("templateid").asText();
                                    MwModelZabbixTemplateParam mwModelZabbixTemplateParam = new MwModelZabbixTemplateParam();
                                    mwModelZabbixTemplateParam.setAssetstemplateId(templateNamesDto.getId());
                                    mwModelZabbixTemplateParam.setServerId(mwtpServerAPI.getServerId());
                                    mwModelZabbixTemplateParam.setTemplateId(intValueConvert(templateid));
                                    mwModelZabbixTemplateParams.add(mwModelZabbixTemplateParam);
                                }
                            }
                        }
                    }
                }
            }

            List<Integer> ids = new ArrayList<>();
            for (MwModelTemplateNamesDto templateNamesDto : templateNamesDtos) {
                try {
                    if (Strings.isNullOrEmpty(templateNamesDto.getTemplateName())) {
                        ids.add(templateNamesDto.getId());
                    }
                } catch (Exception e) {
                    log.info("fail to update 删除资产模板表中的模板id失败, cause:{}", e);
                }
            }
            if (CollectionUtils.isNotEmpty(ids)) {
                mwModelTemplateDao.deleteBatch(ids);
                mwModelTemplateDao.deleteBatchTemplateServerMap(ids);
            }

            //批量插入模版及第三方服务器映射表
            List<List<MwModelZabbixTemplateParam>> listGroups = null;
            if (mwModelZabbixTemplateParams.size() > 0) {
                listGroups = Lists.partition(mwModelZabbixTemplateParams, 1000);
                for (List<MwModelZabbixTemplateParam> listParam : listGroups) {
                    mwModelTemplateDao.insertBatchTemplateServerMap(listParam);
                }
            }
            log.info("updateAssetsTemplate[]资产模板管理[]更新资产模板信息[]{}");
            return Reply.ok("修改成功");
        } catch (Exception e) {
            log.error("fail to updateAssetsTemplate, cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280104, ErrorConstant.ASSETSTEMPLATE_MSG_280104);
        }
    }


    public Reply updateAssetsGroupId() {
        try {
            //1: 清空映射表
            mwModelTemplateDao.cleanGroupServerMap();

            //2： 查询多Zabbix和资产类型类型表
            List<ModelGroupAsSubDeviceType> groupNames = mwModelTemplateDao.selectModelGroupNames();
            List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
            List<MwModelAssetsGroupTable> groupServerTables = new ArrayList<>();
            Boolean flag = false;

            List<ModelGroupAsSubDeviceType> disList = new ArrayList<>();
            for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                List<String> zabbixGroupNames = new ArrayList<>();
                //获取所有的主机分组信息
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostGroupGet(mwtpServerAPI.getServerId(), null, true);
                if (mwZabbixAPIResult.getCode() == 0) {
                    JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                    if (node.size() > 0) {
                        String groupid = "";
                        String name = "";
                        for (JsonNode data : node) {
                            groupid = data.get("groupid").asText();
                            name = data.get("name").asText();
                            String disName = name;
                            if (name.indexOf("[分组]") != -1) {
                                disName = name.split("]")[1];
                            }
                            zabbixGroupNames.add(disName);
                            for (ModelGroupAsSubDeviceType groupInfo : groupNames) {
                                //分组名称存在的，直接获取分组id
                                if (("[分组]" + groupInfo.getNetwork()).equals(name)) {
                                    MwModelAssetsGroupTable groupTable = new MwModelAssetsGroupTable();
                                    groupTable.setGroupId(groupid);
                                    groupTable.setMonitorServerId(mwtpServerAPI.getServerId());
                                    groupTable.setAssetsSubtypeId(groupInfo.getGroupId());
                                    groupServerTables.add(groupTable);
                                }
                            }
                        }

                    }
                }
                //从zabbix上更新来下的groupId
                Set<Integer> ModelGroupId = groupServerTables.stream().map(MwModelAssetsGroupTable::getAssetsSubtypeId).collect(Collectors.toSet());
                List<Map> nameList = new ArrayList<>();
                for (ModelGroupAsSubDeviceType type : groupNames) {
                    //去除从zabbix上更新来下的groupId，没有匹配的需要新增
                    if (!zabbixGroupNames.contains(type.getNetwork())) {
                        disList.add(type);
                        Map<String, String> m = new HashMap<>();
                        m.put("name", "[分组]" + type.getNetwork());
                        nameList.add(m);
                    }
                }
                if (CollectionUtils.isNotEmpty(nameList)) {
                    MWZabbixAPIResult mwZabbixAPIResult2 = mwtpServerAPI.batchCreateHostGroup(mwtpServerAPI.getServerId(), nameList);
                    if (mwZabbixAPIResult2.getCode() == 0) {
                        JsonNode node = (JsonNode) mwZabbixAPIResult2.getData();
                        if (node.size() > 0) {
                            JsonNode groupids = node.get("groupids");
                            for (int x = 0; x < groupids.size(); x++) {
                                String groupid = groupids.get(x).asText();
                                //批量新增的主机分组，返回groupId数组和传入的数据顺序一样。
                                ModelGroupAsSubDeviceType type = disList.get(x);
                                MwModelAssetsGroupTable groupTable = new MwModelAssetsGroupTable();
                                groupTable.setGroupId(groupid);
                                groupTable.setMonitorServerId(mwtpServerAPI.getServerId());
                                groupTable.setAssetsSubtypeId(type.getGroupId());
                                groupServerTables.add(groupTable);
                            }
                        } else {
                            throw new Exception("创建主机群组失败：" + mwtpServerAPI.getServerId());
                        }
                    }
                }

            }


            List<List<MwModelAssetsGroupTable>> listGroups = null;
            if (groupServerTables.size() > 0) {
                log.info("groupServerTables 分组信息：" + JSONObject.toJSONString(groupServerTables));
                listGroups = Lists.partition(groupServerTables, 1000);
                for (List<MwModelAssetsGroupTable> listParam : listGroups) {
                    mwModelTemplateDao.insertBatchGroupServerMap(listParam);
                }
            }

            //3 ：根据资产类型表中的类型名称取对应Zabbix中查询对应的groupid来保存
//            if (groupNames.size() > 0 && mwtpServerAPIS.size() > 0) {
//                //将数据加入线程池中
//                int threadSize = 5;
//                ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
//                List<Future<Boolean>> futureList = new ArrayList<>();
//                for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
//
//                    groupNames.forEach(groupName -> {
//                        MwModelSaveGroupIdMap item = new MwModelSaveGroupIdMap(mwtpServerAPI, groupName.getNetwork(), groupName.getGroupId(), groupServerTables);
//                        Future<Boolean> f = executorService.submit(item);
//                        futureList.add(f);
//                    });
//                }
//
//                //判断有无失败的子线程
//                for (Future<Boolean> f : futureList) {
//                    try {
//                        //是否出错
//                        Boolean aBoolean = f.get(100, TimeUnit.SECONDS);
//                        if (aBoolean) {
//                            flag = true;
//                        }
//                    } catch (Exception e) {
//                        log.error("updateGroupId", e);
//                        f.cancel(true);
//                    }
//                }
//                executorService.shutdown();
//            }
//
//            //保存映射数据
//            if (groupServerTables.size() > 0) {
//                log.info("groupServerTables 分组信息：" + JSONObject.toJSONString(groupServerTables));
//                mwModelTemplateDao.insertBatchGroupServerMap(groupServerTables);
//            }

            return Reply.ok("修改成功");
        } catch (Exception e) {
            log.error("fail to updateAssetsGroupId, cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270106, ErrorConstant.ASSETSSUBTEOY_MSG_270106);
        }
    }


    public Integer getMonitorModeIdByName(String name) {
        Integer monitorModeId = mwModelTemplateDao.getMonitorModeId(name);
        return monitorModeId;
    }

    /**
     * 获取模型分组id（资产类型id）
     *
     * @param name
     * @return
     */
    public Integer getAssetsTypeIdByName(String name) {
        Integer assetsTypeId = mwModelTemplateDao.getAssetsType(name);
        return assetsTypeId;
    }

    /**
     * 获取模型id（资产子类型id）
     *
     * @param typeSubName
     * @return
     */
    public Integer getAssetsSubTypeIdByName(String typeSubName) {
        Integer assetsTypeId = mwModelTemplateDao.getAssetsSubType(typeSubName);
        return assetsTypeId;
    }

    /**
     * 读EXCEL文件，获取信息集合
     *
     * @return
     */
    public Map getExcelInfo(MultipartFile mFile) {
        String fileName = mFile.getOriginalFilename();// 获取文件名
        try {
            if (!validateExcel(fileName)) {// 验证文件名是否合格
                return null;
            }
            boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
            if (isExcel2007(fileName)) {
                isExcel2003 = false;
            }
            return createExcelImport(mFile.getInputStream(), isExcel2003);
        } catch (Exception e) {
            log.error("fail to getExcelInfo with MultipartFile={}, cause:{}", mFile, e);
        }
        return null;
    }

    /**
     * 根据excel里面的内容读取信息
     *
     * @param is          输入流
     * @param isExcel2003 excel是2003还是2007版本
     * @return
     * @throws IOException
     */
    public Map createExcelImport(InputStream is, boolean isExcel2003) {
        try {
            Workbook wb = null;
            if (isExcel2003) {// 当excel是2003时,创建excel2003
                wb = new HSSFWorkbook(is);
            } else {// 当excel是2007时,创建excel2007
                wb = new XSSFWorkbook(is);
            }
            return readExcelValue(wb);// 读取Excel里面客户的信息
        } catch (IOException e) {
            log.error("fail to createExcelImport, cause:{}", e);
        }
        return null;
    }

    /**
     * 读取Excel里面客户的信息
     *
     * @param wb
     * @return
     */
    private Map readExcelValue(Workbook wb) {
        List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
        String loginName = iLoginCacheInfo.getLoginName();
        int num = wb.getNumberOfSheets();
        List<AddAndUpdateModelTemplateParam> dtoList = new ArrayList<AddAndUpdateModelTemplateParam>();
        Map map = new HashMap();
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        //从第四个sheet表开始
        errorMsg = "";
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
                AddAndUpdateModelTemplateParam dto = new AddAndUpdateModelTemplateParam();
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                boolean isflag = true;
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    Object cellValueObj = null;
                    String cellValue = "";
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
                            default:
                                break;
                        }
                    }
                    if (c == 0) {
                        //厂商
                        if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，厂商不能为空；";
                            isflag = false;
                            continue;
                        } else {
                            Boolean check = checkBrand(cellValue);
                            if (!check) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，厂商数据不存在；";
                                isflag = false;
                                continue;
                            }
                        }
                        dto.setBrand(cellValue);
                    } else if (c == 1) {
                        String brandName = row.getCell(0).getStringCellValue();
                        //规格型号
                        if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，规格型号不能为空；";
                            isflag = false;
                            continue;
                        } else {
                            Boolean check = checkSpecification(cellValue, brandName);
                            if (!check) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，规格型号数据不存在；";
                                isflag = false;
                                continue;
                            }
                        }
                        dto.setSpecification(cellValue);
                    } else if (c == 2) {
                        //监控方式
                        if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，监控方式不能为空；";
                            isflag = false;
                            continue;
                        }
                        String value = cellValue;
                        if ("MWAGENT".equals(cellValue) || "mwagent".equals(cellValue)) {
                            value = "MwAgent";
                        }
                        Integer monitorMoId = getMonitorModeIdByName(value);
                        if (monitorMoId == null) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，监控方式输入信息有误，数据库中没有对应信息；";
                            isflag = false;
                            continue;
                        }
                        dto.setMonitorMode(monitorMoId);
                    } else if (c == 3) {
                        //系统OID（监控方式为SNMP时）
                        if ("SNMP".equals(row.getCell(2).getStringCellValue())) {
                            //系统OID
                            if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，SNMP监控方式下，系统OID不能为空；";
                                isflag = false;
                                continue;
                            }
                            dto.setSystemObjid(cellValue);
                        }
                    } else if (c == 4) {
                        //类型（监控方式为中间件、数据库时）
                        int type = 0;
                        if ("中间件".equals(row.getCell(2).getStringCellValue()) || "应用".equals(row.getCell(2).getStringCellValue())) {
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                //单元格为数字
                                double douVal = cell.getNumericCellValue();
                                Integer integer = (int) douVal;
                                //判断是 0 或 4（0：默认，4：JMX）。
                                boolean isNum = (integer == 0 || integer == 4);
                                if (!isNum) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，类型输入信息有误，只能是0和4；";
                                    isflag = false;
                                    continue;
                                } else {
                                    dto.setInterfacesType(integer);
                                }
                            } else {
                                dto.setInterfacesType(type);
                            }
                        } else {
                            dto.setInterfacesType(type);
                        }
                    } else if (c == 5) {
                        //关联模板
                        //模板名称
                        if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，模板名称不能为空；";
                            isflag = false;
                            continue;
                        } else {
                            Boolean check = checkTemplate(cellValue, mwtpServerAPIS);
                            if (!check) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，zabbix中没有相关模板；";
                                isflag = false;
                                continue;
                            }
                        }
                        dto.setTemplateName(cellValue);
                    } else if (c == 6) {
                        //资产类型
                        if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产类型不能为空；";
                            isflag = false;
                            continue;
                        }
                        Integer assetsTypeId = getAssetsTypeIdByName(cellValue);
                        if (assetsTypeId == null) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产类型输入信息有误，数据库中没有对应该信息；";
                            isflag = false;
                            continue;
                        }
                        dto.setModelGroupId(assetsTypeId);
                    } else if (c == 7) {
                        //资产子类型
                        if (null == cell || Strings.isNullOrEmpty(cellValue)) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产子类型不能为空；";
                            isflag = false;
                            continue;
                        }
                        //资产类型值
                        Cell cell2 = row.getCell(6);
                        Integer subAssetsTypeId;
                        List<MwModelInfoDTO> list = new ArrayList<>();
                        if (cell2 != null) {
                            AddAndUpdateModelGroupParam groupParam = new AddAndUpdateModelGroupParam();
                            groupParam.setModelGroupName(cell2.getStringCellValue());
                            groupParam.setModelName(cellValue);
                            list = mwModelManageDao.queryOrdinaryModelInfo(groupParam);
                        }
                        if (list == null || list.size() == 0) {
                            errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产子类型输入信息有误，数据库中没有对应该信息；";
                            isflag = false;
                            continue;
                        }
                        subAssetsTypeId = intValueConvert(list.get(0).getModelId());
                        dto.setModelId(subAssetsTypeId);
                    } else if (c == 8) {
                        //特征信息
                        dto.setDescription(cellValue);
                    }
                }
                if (isflag) {//导入数据有错误，就不添加
                    dto.setCreator(loginName);
                    dto.setModifier(loginName);
                    dtoList.add(dto);
                }
            }
            map.put("dataList", dtoList);
            map.put("errorMsg", errorMsg);
        }
        return map;
    }


    /**
     * 验证厂商是否存在
     *
     * @param brandName
     * @return
     */
    public boolean checkBrand(String brandName) {
        AddAndUpdateModelFirmParam param = mwModelVendorDao.getFirmByName(brandName);
        if (param != null && (param.getId() != null || param.getId() != 0)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 验证规格型号是否存在
     *
     * @param brand
     * @param specification
     * @return
     */
    public boolean checkSpecification(String specification, String brand) {
        List<AddAndUpdateModelSpecificationParam> list = mwModelVendorDao.querySpecificationByBrand(brand, specification);
        if (list != null && list.size() > 0) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 验证监控服务器是否存在
     *
     * @param templateName
     * @return
     */
    public Boolean checkTemplate(String templateName, List<MWTPServerAPI> mwtpServerAPIS) {
        List<String> zabbixInfo = new ArrayList<>();
        for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.templateGet(mwtpServerAPI.getServerId()
                    , templateName, true);
            if (mwZabbixAPIResult.getCode() == 0) {
                JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                if (node.size() > 0) {
                } else {
                    MwModelTPServerTable tpServerDTO = mwModelTemplateDao.selectTPServerById(mwtpServerAPI.getServerId());
                    zabbixInfo.add(tpServerDTO.getMonitoringServerName());
                }
            } else {
                MwModelTPServerTable tpServerDTO = mwModelTemplateDao.selectTPServerById(mwtpServerAPI.getServerId());
                zabbixInfo.add(tpServerDTO.getMonitoringServerName());
            }
        }
        if (mwtpServerAPIS.size() == zabbixInfo.size()) {
            return false;
        }
        return true;
    }

    /**
     * 验证EXCEL文件
     *
     * @param filePath
     * @return
     */
    public static boolean validateExcel(String filePath) {
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))) {
            errorMsg = "文件名不是excel格式";
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
}
