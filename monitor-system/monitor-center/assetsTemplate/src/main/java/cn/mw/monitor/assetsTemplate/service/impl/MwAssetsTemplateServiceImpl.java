package cn.mw.monitor.assetsTemplate.service.impl;

import cn.mw.monitor.TPServer.dao.MwTPServerTableDao;
import cn.mw.monitor.TPServer.dto.MwTPServerDTO;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.AddAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.TemplateNamesDto;
import cn.mw.monitor.assetsTemplate.dao.MwAseetstemplateTableDao;
import cn.mw.monitor.assetsTemplate.model.MwTemplateServerTable;
import cn.mw.monitor.assetsTemplate.service.MwAssetsTemplateService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.assetsTemplate.dto.MwZabbixTemplateDTO;
import cn.mw.monitor.service.assetsTemplate.model.MwAssetsTemplateTable;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.tpserver.api.MwCommonsTPServer;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWTPServerProxy;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by baochengbin on 2020/3/17.
 */
@Service
@Slf4j
@Transactional
public class MwAssetsTemplateServiceImpl implements MwAssetsTemplateService {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/AssetsTemplate");

    @Resource
    private MwAseetstemplateTableDao mwAseetstemplateTableDao;

    @Resource
    private MWWebZabbixManger mwWebZabbixManger;

    @Resource
    private MwTPServerTableDao mwTPServerTableDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwCommonsTPServer mwCommonsTPServer;

    /**
     * 根据资产模板ID取资产模板信息
     *
     * @param id 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(Integer id) {
        try {
            MwAssetsTemplateDTO msDto = mwAseetstemplateTableDao.selectById(id);

            logger.info("EngineManage_LOG[]EngineManage[]资产模板管理[]根据自增序列ID取资产模板信息[]{}", id);
            return Reply.ok(msDto);
        } catch (Exception e) {
            log.error("fail to selectById with id={}, cause:{}", id, e);
            return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240101, ErrorConstant.ENGINEMANAGE_MSG_240101);
        }
    }

    /**
     * 分页查询资产模板信息
     *
     * @param qsParam
     * @return
     */
    @Override
    public Reply selectList(QueryAssetsTemplateParam qsParam) {
        try {
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());

            Map criteria = PropertyUtils.describe(qsParam);
            List<MwAssetsTemplateDTO> mwScanList = mwAseetstemplateTableDao.selectList(criteria);

            PageInfo pageInfo = new PageInfo<>(mwScanList);
            pageInfo.setList(mwScanList);

            logger.info("EngineManage_LOG[]EngineManage[]资产模板管理[]分页查询资产模板信息[]{}[]", mwScanList);

            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to selectListEngineManage with qsParam={}, cause:{}", qsParam, e);
            return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240102, ErrorConstant.ENGINEMANAGE_MSG_240102);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    @Override
    public Reply selectTepmplateTableList(QueryAssetsTemplateParam qsParam) {
        try {
            //判断是否需要根据关联资产数量进行排序
            if(StringUtils.isBlank(qsParam.getRelationAssetsSort())){
                Map criteria = PropertyUtils.describe(qsParam);
                List<MwAssetsTemplateDTO> mwScanList = mwAseetstemplateTableDao.selectTepmplateTableList(criteria);
                mwScanList.forEach(template -> {
                    List<String> assetsIds = template.getAssetsIds();
                    if (assetsIds != null && assetsIds.size() > 1) {
                        Set set = new HashSet(assetsIds);
                        assetsIds.clear();
                        assetsIds.addAll(set);
                        template.setAssetsIds(assetsIds);
                    }
                });

                PageList pageList = new PageList();
                PageInfo pageInfo = new PageInfo<>(mwScanList);
                pageInfo.setTotal(mwScanList.size());
                mwScanList = pageList.getList(mwScanList, qsParam.getPageNumber(), qsParam.getPageSize());
                pageInfo.setList(mwScanList);

                logger.info("EngineManage_LOG[]EngineManage[]资产模板管理[]分页查询资产模板信息[]{}[]", mwScanList);

                return Reply.ok(pageInfo);
            }else{
                Integer pageNumber = qsParam.getPageNumber();
                Integer pageSize = qsParam.getPageSize();
                qsParam.setPageNumber(1);
                qsParam.setPageSize(Integer.MAX_VALUE);
                Map criteria = PropertyUtils.describe(qsParam);
                List<MwAssetsTemplateDTO> mwScanList = mwAseetstemplateTableDao.selectTepmplateTableList(criteria);
                if(CollectionUtils.isEmpty(mwScanList)){
                    PageInfo pageInfo = new PageInfo<>(mwScanList);
                    pageInfo.setTotal(mwScanList.size());
                    pageInfo.setList(mwScanList);
                    return Reply.ok(pageInfo);
                }
                mwScanList.forEach(template -> {
                    List<String> assetsIds = template.getAssetsIds();
                    if (assetsIds != null && assetsIds.size() > 1) {
                        Set set = new HashSet(assetsIds);
                        assetsIds.clear();
                        assetsIds.addAll(set);
                        template.setAssetsIds(assetsIds);
                    }
                });
                Collections.sort(mwScanList, new Comparator<MwAssetsTemplateDTO>() {
                    @Override
                    public int compare(MwAssetsTemplateDTO o1, MwAssetsTemplateDTO o2) {
                        if("ASC".equals(qsParam.getRelationAssetsSort())){
                            if(o1.getAssetsIds().size() > o2.getAssetsIds().size()){
                                return 1;
                            }
                            if(o1.getAssetsIds().size() < o2.getAssetsIds().size()){
                                return -1;
                            }
                            return 0;
                        }
                        if("DESC".equals(qsParam.getRelationAssetsSort())){
                            if(o1.getAssetsIds().size() > o2.getAssetsIds().size()){
                                return -1;
                            }
                            if(o1.getAssetsIds().size() < o2.getAssetsIds().size()){
                                return 1;
                            }
                            return 0;
                        }
                        return 0;
                    }
                });
                int fromIndex = pageSize * (pageNumber -1);
                int toIndex = pageSize * pageNumber;
                if(toIndex > mwScanList.size()){
                    toIndex = mwScanList.size();
                }
                List<MwAssetsTemplateDTO> templateDTOS = mwScanList.subList(fromIndex, toIndex);
                PageInfo pageInfo = new PageInfo<>(templateDTOS);
                pageInfo.setTotal(mwScanList.size());
                pageInfo.setList(templateDTOS);
                return Reply.ok(pageInfo);
            }
        } catch (Exception e) {
            log.error("fail to selectListEngineManage with qsParam={}, cause:{}", qsParam, e);
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280107, ErrorConstant.ASSETSTEMPLATE_MSG_280107);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }


    /**
     * 更新资产模板信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply update(AddAssetsTemplateParam auParam) throws Exception {
        List<String> items = new ArrayList<>();
//        //重复性校验
        List<MwAssetsTemplateTable> check = new ArrayList<>();
        if(!StringUtils.isEmpty(auParam.getDescription())){
            check = mwAseetstemplateTableDao.check(QueryAssetsTemplateParam.builder()
                    .description(auParam.getDescription())
                    .templateName(auParam.getTemplateName())
                    .specification(auParam.getSpecification()).build());
        }
        if (check.size() >= 1) {
            items.add("特征信息不能同时相同");
        }
        List<MwAssetsTemplateTable> check1 = new ArrayList<>();
        if(!StringUtils.isEmpty(auParam.getSystemObjid())){
            check1 = mwAseetstemplateTableDao.check(QueryAssetsTemplateParam.builder()
                    .systemObjid(auParam.getSystemObjid())
                    .templateName(auParam.getTemplateName())
                    .specification(auParam.getSpecification()).build());
        }
        if (check1.size() >= 1) {
            items.add("系统oid不能同时相同");
        }
        if (items.size() > 0) {
            Reply.fail(StringUtils.join(new String[]{ErrorConstant.ASSETSTEMPLATE_MSG_280102, StringUtils.join(items, "、 ")}));
        }
        auParam.setModifier(iLoginCacheInfo.getLoginName());

        mwAseetstemplateTableDao.update(auParam);
        //根据多zabbix 更新mapper表信息
        List<String> zabbixInfo = new ArrayList<>();
        List<MwTemplateServerTable> templateServerTables = new CopyOnWriteArrayList<MwTemplateServerTable>();
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
                    MwTemplateServerTable mwTemplateServerTable = new MwTemplateServerTable();
                    mwTemplateServerTable.setAssetstemplateId(auParam.getId());
                    mwTemplateServerTable.setServerId(mwtpServerAPI.getServerId());
                    mwTemplateServerTable.setServerTemplateId(templateid);
                    templateServerTables.add(mwTemplateServerTable);
                } else {
                    MwTPServerDTO tpServerDTO = mwTPServerTableDao.selectById(mwtpServerAPI.getServerId());
                    zabbixInfo.add(tpServerDTO.getMonitoringServerName());
                }
            }
        }
        //先根据模板id删除mapper
        List<Integer> ids = new ArrayList<>();
        ids.add(auParam.getId());
        mwAseetstemplateTableDao.deleteBatchTemplateServerMap(ids);
        //插入模版及第三方服务器映射表
        if (templateServerTables.size() > 0) {
            mwAseetstemplateTableDao.insertBatchTemplateServerMap(templateServerTables);
        }
        if (zabbixInfo.size() > 0) {
            String join = StringUtils.join(new String[]{StringUtils.join(zabbixInfo, "、 ")});
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280102, join + "中没有相关模板！");
        }
        logger.info("EngineManage_LOG[]EngineManage[]资产模板管理[]更新资产模板信息[]{}", auParam);
        return Reply.ok("更新成功！");
    }

    /**
     * 新增模板信息
     *
     * @param aParam
     * @return
     */
    @Override
    public Reply insert(AddAssetsTemplateParam aParam) throws Exception {
        List<String> items = new ArrayList<>();
//        //重复性校验
        List<MwAssetsTemplateTable> check = new ArrayList<>();
        if(!StringUtils.isEmpty(aParam.getDescription())){
            check = mwAseetstemplateTableDao.check(QueryAssetsTemplateParam.builder()
                    .description(aParam.getDescription())
                    .templateName(aParam.getTemplateName())
                    .specification(aParam.getSpecification()).build());
        }
        if (check.size() >= 1) {
            items.add("特征信息不能同时相同");
        }
        List<MwAssetsTemplateTable> check1 = new ArrayList<>();
        if(!StringUtils.isEmpty(aParam.getSystemObjid())){
            check1 = mwAseetstemplateTableDao.check(QueryAssetsTemplateParam.builder()
                    .systemObjid(aParam.getSystemObjid())
                    .templateName(aParam.getTemplateName())
                    .specification(aParam.getSpecification()).build());
        }
        if (check1.size() >= 1) {
            items.add("系统oid不能同时相同");
        }
        if (items.size() > 0) {
            Reply.fail(StringUtils.join(new String[]{ErrorConstant.ASSETSTEMPLATE_MSG_280102, StringUtils.join(items, "、 ")}));
        }
        aParam.setCreator(iLoginCacheInfo.getLoginName());
        aParam.setModifier(iLoginCacheInfo.getLoginName());

        if (aParam.getTemplateName() != null && !"".equals(aParam.getTemplateName())) {
            mwAseetstemplateTableDao.insert(aParam);
        } else {
            throw new Exception("模板名称不能为空！");
        }
//            String typeName = "";
//            if (aParam.getAssetsTypeId() != null) {
//                typeName = mwAseetstemplateTableDao.selectTypeName(aParam.getAssetsTypeId());
//            }
//            List<BaseDto> group = mwWebZabbixManger.hostGroupGet("[分组]" + typeName, true);
//            if (group != null && group.size() > 0) {
//                aParam.setGroupId(group.get(0).getId());
//            }
//        mwAseetstemplateTableDao.insert(aParam);

        //根据多zabbix 更新mapper表信息
        List<MwTemplateServerTable> templateServerTables = new CopyOnWriteArrayList<MwTemplateServerTable>();
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
                        MwTemplateServerTable mwTemplateServerTable = new MwTemplateServerTable();
                        mwTemplateServerTable.setAssetstemplateId(aParam.getId());
                        mwTemplateServerTable.setServerId(mwtpServerAPI.getServerId());
                        mwTemplateServerTable.setServerTemplateId(templateid);
                        templateServerTables.add(mwTemplateServerTable);
                    } else {
                        MwTPServerDTO tpServerDTO = mwTPServerTableDao.selectById(mwtpServerAPI.getServerId());
                        zabbixInfo.add(tpServerDTO.getMonitoringServerName());
//                        return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280102,tpServerDTO.getMonitoringServerName() + "中没有相关模板！");
//                    throw new Exception(tpServerDTO.getMonitoringServerName() + "中没有相关模板！");
                    }
                }
            }
        }
        //批量插入模版及第三方服务器映射表
        if (templateServerTables.size() > 0) {
            mwAseetstemplateTableDao.insertBatchTemplateServerMap(templateServerTables);
        }
        if (zabbixInfo.size() > 0) {
            String join = StringUtils.join(new String[]{StringUtils.join(zabbixInfo, "、 ")});
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280102, join + "中没有相关模板！");
        }
        logger.info("EngineManage_LOG[]EngineManage[]资产模板管理[]新增资产模板信息[]{}", aParam);
        return Reply.ok("新增成功！");
    }

    @Override
    public Reply templateGet(String name) {
        Reply reply = mwCommonsTPServer.selectByMainServer();
        if (reply.getData() != null && (int) reply.getData() != 0) {
            List templateList = mwWebZabbixManger.templateGet((int) reply.getData(), name);
            return Reply.ok(templateList);
        } else {
            return Reply.fail("无监控服务器，没有关联模板");
        }
    }

//    @Override
//    public Reply resetTemplateIdBatch() {
//        List<String> templateList = mwAseetstemplateTableDao.getTemplate();
//        List<BaseDto> zabbixTemplateList = mwWebZabbixManger.templateGet(null);
//        Map<String, String> zabbixTemplateMap = zabbixTemplateList.stream().collect(Collectors.toMap(
//                BaseDto::getName, BaseDto::getId, (key1, key2) -> key2
//        ));
//        List<UpdateTemplateDTO> updateTemplateDTOS = new ArrayList<>();
//        templateList.forEach(
//                templateName -> {
//                    if (StringUtils.isNotEmpty(templateName)) {
//                        updateTemplateDTOS.add(UpdateTemplateDTO.builder().templateName(templateName).templateId(zabbixTemplateMap.get(templateName)).build());
//                    }
//                }
//        );
//        List<MwAssetsGroupDTO> groupList = mwAseetstemplateTableDao.getGroup();
//        List<BaseDto> zabbixGroupList = mwWebZabbixManger.hostGroupGet("[分组]", false);
//        Map<String, String> zabbixGroupMap = zabbixGroupList.stream().collect(Collectors.toMap(
//                BaseDto::getName, BaseDto::getId, (key1, key2) -> key2
//        ));
//        groupList.forEach(
//                group -> {
//                    if (StringUtils.isNotEmpty(group.getGroupName())) {
//                        updateTemplateDTOS.add(UpdateTemplateDTO.builder().assetsTypeId(group.getAssetsTypeId()).groupId(zabbixGroupMap.get("[分组]" + group.getGroupName())).build());
//                    }
//                }
//        );
//        mwAseetstemplateTableDao.updateTemplateBatch(updateTemplateDTOS);
//
//        return Reply.ok("重置成功！");
//    }

    /**
     * @return
     */
    @Override
    public Reply updateAssetsTemplate() {
        try {
            Boolean isFail = false;

            //清空模版及第三方服务器映射表
            mwAseetstemplateTableDao.cleanTemplateServerMap();
            List<TemplateNamesDto> templateNamesDtos = mwAseetstemplateTableDao.selectTemplateNames();
            List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
            List<MwTemplateServerTable> templateServerTables = new CopyOnWriteArrayList<MwTemplateServerTable>();

            if (templateNamesDtos.size() > 0 && mwtpServerAPIS.size() > 0) {
                int threadSize = templateNamesDtos.size() > 1 ? (templateNamesDtos.size() / 2) : templateNamesDtos.size();
                ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
                List<Future<Boolean>> futureList = new ArrayList<>();

                AddAssetsTemplateParam addAssetsTemplateParam = new AddAssetsTemplateParam();
                for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                    List<MwZabbixTemplateDTO> zabbixTemplateDTOS = new ArrayList<MwZabbixTemplateDTO>();
                    templateNamesDtos.forEach(templateNamesDto -> {
                        GetTemplateThread getTemplateThread = new GetTemplateThread() {
                            @Override
                            public Boolean call() throws Exception {
                                Boolean flag = true;
                                Boolean isDelete = false;
                                if (null != templateNamesDto.getTemplateName() && StringUtils.isNotEmpty(templateNamesDto.getTemplateName())) {

                                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.templateGet(mwtpServerAPI.getServerId()
                                            , templateNamesDto.getTemplateName(), true);

                                    if (mwZabbixAPIResult.getCode() == 0) {
                                        JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                                        if (node.size() > 0) {
                                            String templateid = "";
                                            for (JsonNode data : node) {
                                                templateid = data.get("templateid").asText();
                                            }

                                            MwTemplateServerTable mwTemplateServerTable = new MwTemplateServerTable();
                                            mwTemplateServerTable.setAssetstemplateId(templateNamesDto.getId());
                                            mwTemplateServerTable.setServerId(mwtpServerAPI.getServerId());
                                            mwTemplateServerTable.setServerTemplateId(templateid);
                                            templateServerTables.add(mwTemplateServerTable);
                                        } else {
                                            isDelete = true;
                                        }
                                    }
                                } else {
                                    isDelete = true;
                                    if (isDelete) {
                                        try {
                                            ArrayList<Integer> ids = new ArrayList<>();
                                            ids.add(templateNamesDto.getId());
                                            mwAseetstemplateTableDao.deleteBatch(ids);
                                            mwAseetstemplateTableDao.deleteBatchTemplateServerMap(ids);
                                        } catch (Exception e) {
                                            logger.info("fail to update 删除资产模板表中的模板id失败, cause:{}", e);
                                            flag = false;
                                        }
                                    }
                                }
                                return flag;
                            }
                        };
                        Future<Boolean> f = executorService.submit(getTemplateThread);
                        futureList.add(f);
                    });
                }
                for (Future<Boolean> f : futureList) {
                    try {
                        Boolean aBoolean = f.get(10, TimeUnit.SECONDS);
                        if (!aBoolean) {
                            isFail = true;
                        }
                    } catch (Exception e) {
                        log.error("updateAssetsTemplate", e);
                        f.cancel(true);
                    }
                }
                executorService.shutdown();
                logger.info("关闭线程池");
            }

            //批量插入模版及第三方服务器映射表
            if (templateServerTables.size() > 0) {
                mwAseetstemplateTableDao.insertBatchTemplateServerMap(templateServerTables);
            }

            logger.info("updateAssetsTemplate[]资产模板管理[]更新资产模板信息[]{}");
            if (!isFail) {
                return Reply.ok("修改成功");
            } else {
                return Reply.fail("修改失败");
            }
        } catch (Exception e) {
            log.error("fail to updateAssetsTemplate, cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280104, ErrorConstant.ASSETSTEMPLATE_MSG_280104);
        }
    }

    /**
     * 删除资产模板信息
     *
     * @param ids
     * @return
     */
    @Override
    public Reply delete(List<Integer> ids) {
        //删除资产模板数据
        mwAseetstemplateTableDao.deleteBatch(ids);
        mwAseetstemplateTableDao.deleteBatchTemplateServerMap(ids);
        logger.info("EngineManage_LOG[]EngineManage[]资产模板管理[]删除资产模板信息[]{}", ids);
        return Reply.ok("删除成功");
    }

    /**
     * 资产模板模糊搜索所有字段联想
     *
     * @param value
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(String value) {
        //根据值模糊查询数据
        List<Map<String, String>> fuzzSeachAllFileds = mwAseetstemplateTableDao.fuzzSearchAllFiled(value);
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
}
