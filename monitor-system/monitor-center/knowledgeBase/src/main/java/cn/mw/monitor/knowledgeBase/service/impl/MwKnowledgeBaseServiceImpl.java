package cn.mw.monitor.knowledgeBase.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.knowledgeBase.dao.MwKnowledgeBaseTableDao;
import cn.mw.monitor.knowledgeBase.dao.MwKnowledgeLikeOrHateRecordDao;
import cn.mw.monitor.knowledgeBase.dao.MwKnowledgeUserMapperDao;
import cn.mw.monitor.knowledgeBase.dto.*;
import cn.mw.monitor.knowledgeBase.model.MwKnowledgeLikeOrHateRecord;
import cn.mw.monitor.knowledgeBase.model.MwKnowledgeUserMapper;
import cn.mw.monitor.knowledgeBase.service.MwKnowledgeBaseService;
import cn.mw.monitor.knowledgeBase.service.MwKnowledgeLoveActionService;
import cn.mw.monitor.knowledgeBase.service.RedisService;
import cn.mw.monitor.service.activitiAndMoudle.ActivitiSever;
import cn.mw.monitor.service.activitiAndMoudle.KenwSever;
import cn.mw.monitor.service.knowledgeBase.api.MwKnowledgeService;
import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.lucene.LuceneUtils;
import cn.mw.monitor.util.lucene.dto.LuceneFieldsDTO;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author syt
 * @Date 2020/8/19 17:34
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class MwKnowledgeBaseServiceImpl implements MwKnowledgeBaseService, MwKnowledgeLoveActionService, MwKnowledgeService, KenwSever {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/knowledgeBase");
    // 树状导航栏为资产类型
    private static final String TREETYPEALL = "All";
    // 树状导航栏为知识状态类型
    private static final String TREETYPEMINE = "Mine";
    private static final Map<Integer, String> knTreeTypeMap = new HashMap<>();

    @Autowired
    ActivitiSever activitiServer;
    @Value("${report.excelPath}")
    private String path;

    static {
        knTreeTypeMap.put(-1, "未发布");
        knTreeTypeMap.put(1, "审核中");
        knTreeTypeMap.put(2, "已撤销");
        knTreeTypeMap.put(3, "被驳回");
        knTreeTypeMap.put(4, "已发布");
    }

    //对应流程图的id
    private static final String INSTANCEKEY = "Process_knowledge_plus";
    private static final Integer ACTIVITI_UNPUBLISHED_STATUS = -1;
    //知识库全部的类型一级名
    private static final String TREETYPENAME_ALL = "全部";
    //知识库全部的类型一级图标名称
    private static final String TREETYPEICON_GENERAL_LIST = "总目录";

    @Resource
    private MwKnowledgeBaseTableDao mwKnowledgeBaseTableDao;
    @Resource
    private MwKnowledgeUserMapperDao mwKnowledgeUserMapperDao;
    @Resource
    private MwKnowledgeLikeOrHateRecordDao mwKnowledgeLikeOrHateRecordDao;
    @Autowired
    ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    RedisService redisService;

    @Override
    public Reply selectById(String id, Boolean giveFlag) {
        try {
            MwKnowledgeBaseTable mwKnowledgeBaseTable = mwKnowledgeBaseTableDao.selectById(id);
            mwKnowledgeBaseTable.setSolution(HtmlUtils.htmlUnescape(mwKnowledgeBaseTable.getSolution()));
            MwKnowledgeBaseTableDTO mwKnowledgeBaseTableDTO = new MwKnowledgeBaseTableDTO();
            BeanUtils.copyProperties(mwKnowledgeBaseTable, mwKnowledgeBaseTableDTO);
            String attachmentUrls = mwKnowledgeBaseTable.getAttachmentUrl();
            if (!"".equals(attachmentUrls) && attachmentUrls != null) {
                //处理url,变成list
                List<FileList> list = getFileList(attachmentUrls);
                mwKnowledgeBaseTableDTO.setFileList(list);
            }
            if (giveFlag) {
                //点赞数量
                String loginName = iLoginCacheInfo.getLoginName();
                Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
                Reply likedStatusAndCount = redisService.getLikedStatusAndCount(id, userId);
                KnowledgeLikedDTO knowledgeLikedDTO = (KnowledgeLikedDTO) likedStatusAndCount.getData();
                mwKnowledgeBaseTableDTO.setLikedCount(knowledgeLikedDTO.getLikedCount());
                mwKnowledgeBaseTableDTO.setHatedCount(knowledgeLikedDTO.getHatedCount());
                mwKnowledgeBaseTableDTO.setLikedStatus(knowledgeLikedDTO.getStatus());
            }
            logger.info("KNOWLEDGE_LOG[]selectById[]根据id获取知识信息");
            logger.info("selectById 根据id获取知识信息,运行成功结束");
            return Reply.ok(mwKnowledgeBaseTableDTO);
        } catch (Exception e) {
            log.error("fail to selectById id:{}  cause:{}", id, e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_BASE_SELECT_BY_ID_CODE_309006, ErrorConstant.KNOWLEDGE_BASE_SELECT_BY_ID_MSG_309006);
        }
    }

    @Override
    public Reply selectTableList(QueryKnowledgeBaseParam qParam) {
        try {
            PageInfo pageInfo = new PageInfo();
            List<MwKnowledgeBaseTable> knowledgeTable = new ArrayList();
            if (qParam.getSolution() != null && !"".equals(qParam.getSolution())) {
                //搜索引擎查询
                Map solution = new HashMap<>();
                solution.put("solution", qParam.getSolution());
                int start = (qParam.getPageNumber() - 1) * qParam.getPageSize();
                int end = qParam.getPageNumber() * qParam.getPageSize();
                List<Map> mapList = LuceneUtils.searchByTypeIds(qParam.getTypeIds(), solution, start, end);
                int total = 0;
                if (mapList.size() > 0) {
                    total = (int) mapList.get(0).get("total");
                }
                for (Map map : mapList) {
                    MwKnowledgeBaseTable mwKnowledgeBaseTable = new MwKnowledgeBaseTable();
                    mwKnowledgeBaseTable.setId(map.get("id").toString());
                    Map priCriteria = PropertyUtils.describe(mwKnowledgeBaseTable);
                    knowledgeTable.addAll(mwKnowledgeBaseTableDao.selectTableList(priCriteria));
                }
                pageInfo.setTotal(total);
                pageInfo.setPageNum(qParam.getPageNumber());
                pageInfo.setPageSize(qParam.getPageSize());
            } else {
                //数据库查询
                if (qParam.getTypeIds() == null || qParam.getTypeIds().size() <= 0) {
                    qParam.setTypeIds(null);
                }
                if (qParam.getUserId() != null && qParam.getUserId() > 0) {
                    qParam.setCreator(iLoginCacheInfo.getLoginName());
                }
                PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                Map priCriteria = PropertyUtils.describe(qParam);
                knowledgeTable = mwKnowledgeBaseTableDao.selectTableList(priCriteria);
                pageInfo = new PageInfo<>(knowledgeTable);
            }
            pageInfo.setList(knowledgeTable);
            logger.info("KNOWLEDGE_LOG[]selectTableList[]获取知识库table分页");
            logger.info("selectTableList 获取知识库table分页,运行成功结束");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectTableList param:{}  cause:{}", qParam, e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_BASE_SELECT_TABLE_CODE_309002, ErrorConstant.KNOWLEDGE_BASE_SELECT_TABLE_MSG_309002);
        }
    }

    @Override
    public Reply update(AddOrUpdateKnowledgeBaseParam uParam) throws Exception {
        //调用流程审批
        Map map = activitiServer.OperMoudleContainActiviti("-1", 1, uParam);
        //返回类型为0，表示不走流程 否则进入流程审批环节
        if (map != null && map.get("type") != null && map.get("message") != null) {
            Integer type = (Integer) map.get("type");
            String message = map.get("message").toString();
            if (type == 0) {
                uParam.setModifier(iLoginCacheInfo.getLoginName());
                int version = mwKnowledgeBaseTableDao.selectVersionById(uParam.getId());
                uParam.setVersion(version);
                mwKnowledgeBaseTableDao.update(uParam);
                //修改索引
                LuceneFieldsDTO luceneFieldsDTO = new LuceneFieldsDTO(uParam.getId(), uParam.getTitle(), uParam.getTypeId().toString(), uParam.getTriggerCause(), uParam.getSolution());
                try {
                    LuceneUtils.updateIndex(luceneFieldsDTO);
                } catch (Exception e) {
                    log.error("fail to LuceneUtils.updateIndex() luceneFieldsDTO:{}  cause:{}", luceneFieldsDTO, e.getMessage());
                    throw new Exception("修改索引失败！");
                }
                logger.info("KNOWLEDGE_LOG[]update[]修改知识");
                logger.info("update 修改知识,运行成功结束");
                return Reply.ok("修改知识库成功");
            } else if (type == 3) {
                return Reply.ok("修改知识库的流程已提交");
            } else {
                return Reply.fail("非流程确定人员提交");
            }
        }else {
            return Reply.fail("非流程确定人员提交");
        }
//            log.error("fail to update uParam:{} cause:{}", uParam, e.getMessage());
//            return Reply.fail(ErrorConstant.KNOWLEDGE_BASE_UPDATE_CODE_309004, ErrorConstant.KNOWLEDGE_BASE_UPDATE_MSG_309004);

    }

    @Override
    public Reply delete(DeleteKnowledgeParam param) throws Exception {

        //删除数据库
        //调用流程审批
        List<MwKnowledgeBaseTable> p = new ArrayList<>();

        for (String id:param.getIds()) {
            MwKnowledgeBaseTable aParam = selectById(id);
            p.add(aParam);
        }
        param.setKnowledgeBaseTables(p);
        Map map = activitiServer.OperMoudleContainActiviti("-1", 2, p);

        //返回类型为0，表示不走流程 否则进入流程审批环节
        if (map != null && map.get("type") != null && map.get("message") != null) {
            Integer type = (Integer) map.get("type");
            String message = map.get("message").toString();
            if (type == 0) {
                //删除数据库
                mwKnowledgeBaseTableDao.delete(param.getIds());
                //删除索引
                try {
                    LuceneUtils.delete(param.getIds());
                } catch (Exception e) {
                    log.error("fail to LuceneUtils.delete() aParam:{} cause:{}", param.getIds(), e.getMessage());
                    throw new Exception("删除索引失败！");
                }
                //删除关联点赞表
                mwKnowledgeUserMapperDao.delete(param.getIds());
                //删除点赞总数量表
                mwKnowledgeLikeOrHateRecordDao.delete(param.getIds());
                logger.info("KNOWLEDGE_LOG[]delete[]删除知识");
                logger.info("delete 删除知识,运行成功结束");
                return Reply.ok("删除知识库成功");
            } else if (type == 3) {
                return Reply.ok("删除知识库的流程已提交");
            } else {
                return Reply.fail("非流程确定人员提交");
            }
        }
        return Reply.ok();

    }

    @Override
    public Reply insert(AddOrUpdateKnowledgeBaseParam aParam) {
        aParam.setSolution(HtmlUtils.htmlEscapeHex(aParam.getSolution()));
        aParam.setId(UUIDUtils.getUUID());
        aParam.setDeleteFlag(false);
        aParam.setCreator(iLoginCacheInfo.getLoginName());
        aParam.setModifier(iLoginCacheInfo.getLoginName());
        aParam.setVersion(1);
        aParam.setActivitiStatus(ACTIVITI_UNPUBLISHED_STATUS);
        mwKnowledgeBaseTableDao.insert(aParam);
        logger.info("KNOWLEDGE_LOG[]insert[]新增知识");
        logger.info("insert 新增知识,运行成功结束");
        return Reply.ok();
//            log.error("fail to insert aParam:{} cause:{}", aParam, e.getMessage());
//            return Reply.fail(ErrorConstant.KNOWLEDGE_BASE_INSERT_CODE_309003, ErrorConstant.KNOWLEDGE_BASE_INSERT_MSG_309003);
    }

    @Override
    public Reply getTypeTree(String type) {
        try {

            List<TypeTreeDTO> topList = new ArrayList<TypeTreeDTO>();
            TypeTreeDTO topTypeTreeDTO = new TypeTreeDTO();
            topTypeTreeDTO.setTypeName(TREETYPENAME_ALL);
            topTypeTreeDTO.setActivitiStatus(0);
            topTypeTreeDTO.setTypeId(0);
            int topTotal = 0;
            topTypeTreeDTO.setUrlName(KnowledgeIconEnum.getInfoByTypeName(TREETYPEICON_GENERAL_LIST).getIcon());
            //当类型为一级时
            List<TypeTreeDTO> knowledgeTypeDTOS = new ArrayList<>();
            if (TREETYPEALL.equals(type)) {
                knowledgeTypeDTOS = mwKnowledgeBaseTableDao.selectTypeClassByPId(0);
                if (knowledgeTypeDTOS.size() > 0) {
                    for (TypeTreeDTO knowledgeTypeDTO : knowledgeTypeDTOS) {//一级后的下一级
                        int total = 0;
                        List<TypeTreeDTO> firstChildren = mwKnowledgeBaseTableDao.selectTypeClassByPId(knowledgeTypeDTO.getTypeId());
                        if (firstChildren.size() > 0) {
                            for (TypeTreeDTO child : firstChildren) {
                                total += child.getKCount();
                            }
                            knowledgeTypeDTO.addChildren(firstChildren);
                        }
                        knowledgeTypeDTO.setKCount(knowledgeTypeDTO.getKCount() + total);
                        knowledgeTypeDTO.setUrlName(KnowledgeIconEnum.getInfoByTypeName(knowledgeTypeDTO.getTypeName()).getIcon());
                        topTotal += knowledgeTypeDTO.getKCount();
                    }
                }
            } else if (TREETYPEMINE.equals(type)) {
                Map<String, Object> typeMine = getTreeTypeMine();
                knowledgeTypeDTOS = (List<TypeTreeDTO>) typeMine.get("children");
                topTotal = (int) typeMine.get("totalCount");
            }
            topTypeTreeDTO.setKCount(topTotal);
            topTypeTreeDTO.addChildren(knowledgeTypeDTOS);
            topList.add(topTypeTreeDTO);
            logger.info("KNOWLEDGE_LOG[]selectTypeTree[]获取知识库的所有分类，得到树形结构");
            logger.info("selectTypeTree 获取知识库的所有分类，得到树形结构,运行成功结束");
            return Reply.ok(topList);
        } catch (Exception e) {
            log.error("fail to selectTypeTree  cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_BASE_SELECT_TYPE_TREE_CODE_309001, ErrorConstant.KNOWLEDGE_BASE_SELECT_TYPE_TREE_MSG_309001);
        }
    }

    @Override
    public Reply updateMyKnowledge(AddOrUpdateKnowledgeBaseParam uParam) {
        uParam.setModifier(iLoginCacheInfo.getLoginName());
        int version = mwKnowledgeBaseTableDao.selectVersionById(uParam.getId());
        uParam.setVersion(version);
        uParam.setActivitiStatus(4);
        //调用流程审批
        Map map = activitiServer.OperMoudleContainActiviti("-1", 1, uParam);
        //返回类型为0，表示不走流程 否则进入流程审批环节
        if (map != null && map.get("type") != null && map.get("message") != null) {
            Integer type = (Integer) map.get("type");
            String message = map.get("message").toString();
            if (type == 0) {
                mwKnowledgeBaseTableDao.update(uParam);
                return Reply.ok();
            } else if (type == 3) {
                return Reply.ok();
            } else {
                return Reply.fail("非流程确定人员提交");
            }
        }
        return Reply.ok();
    }

    @Override
    public Reply deleteMyKnowledge(DeleteKnowledgeParam param) {
        //删除数据库
        //调用流程审批
        Map map = activitiServer.OperMoudleContainActiviti("-1", 2, param);
        //返回类型为0，表示不走流程 否则进入流程审批环节
        if (map != null && map.get("type") != null && map.get("message") != null) {
            Integer type = (Integer) map.get("type");
            String message = map.get("message").toString();
            if (type == 0) {
                mwKnowledgeBaseTableDao.delete(param.getIds());
                return Reply.ok();
            } else if (type == 3) {
                return Reply.ok();
            } else {
                return Reply.fail("非流程确定人员提交");
            }
        }
        return Reply.ok();
    }

    @Override
    public Reply insertMyKnowledge(AddOrUpdateKnowledgeBaseParam aParam) {
        aParam.setSolution(HtmlUtils.htmlEscapeHex(aParam.getSolution()));
        aParam.setId(UUIDUtils.getUUID());
        aParam.setDeleteFlag(false);
        aParam.setCreator(iLoginCacheInfo.getLoginName());
        aParam.setModifier(iLoginCacheInfo.getLoginName());
        aParam.setVersion(1);
        aParam.setActivitiStatus(4);
        aParam.setActivitiStatus(ACTIVITI_UNPUBLISHED_STATUS);
        //调用流程审批
        Map map = activitiServer.OperMoudleContainActiviti("-1", 0, aParam);
        //返回类型为0，表示不走流程 否则进入流程审批环节
        if (map != null && map.get("type") != null && map.get("message") != null) {
            Integer type = (Integer) map.get("type");
            String message = map.get("message").toString();
            if (type == 0) {
                mwKnowledgeBaseTableDao.insert(aParam);
                LuceneFieldsDTO luceneFieldsDTO = new LuceneFieldsDTO(aParam.getId(), aParam.getTitle(), aParam.getTypeId().toString(), aParam.getTriggerCause(), aParam.getSolution());
                try {
                    LuceneUtils.createIndex(luceneFieldsDTO);
                } catch (Exception e) {
                    log.error("fail to LuceneUtils.createIndex(luceneFieldsDTO) cause:{}", e.getMessage());
                }
                return Reply.ok("新增知识库成功");
            } else if (type == 3) {
                return Reply.ok("新增知识库的流程已提交");
            } else {
                return Reply.fail("非流程确定人员提交");
            }
        }
        return Reply.fail("新增实例失败", "");
    }

    /**
     * 知识库模糊搜索所有字段联想
     *
     * @param param
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(QueryKnowledgeBaseParam param) {

            //根据值模糊查询数据
            List<Map<String, String>> fuzzSeachAllFileds = mwKnowledgeBaseTableDao.fuzzSearchAllFiled(param.getActivitiStatus(),param.getValue());
            Set<String> fuzzSeachData = new HashSet<>();
            if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
                for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                    String title = fuzzSeachAllFiled.get("title");
                    String trigger_cause = fuzzSeachAllFiled.get("trigger_cause");
                    String type_name = fuzzSeachAllFiled.get("type_name");
                    String creator = fuzzSeachAllFiled.get("creator");
                    String modifier = fuzzSeachAllFiled.get("modifier");
                    if (StringUtils.isNotBlank(title) && title.contains(param.getValue())) {
                        fuzzSeachData.add(title);
                    }
                    if (StringUtils.isNotBlank(trigger_cause) && trigger_cause.contains(param.getValue())) {
                        fuzzSeachData.add(trigger_cause);
                    }
                    if (StringUtils.isNotBlank(type_name) && type_name.contains(param.getValue())) {
                        fuzzSeachData.add(type_name);
                    }
                    if (StringUtils.isNotBlank(creator) && creator.contains(param.getValue())) {
                        fuzzSeachData.add(creator);
                    }
                    if (StringUtils.isNotBlank(modifier) && modifier.contains(param.getValue())) {
                        fuzzSeachData.add(modifier);
                    }
                }
            }
            Map<String, Set<String>> fuzzyQuery = new HashMap<>();
            fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
            return Reply.ok(fuzzyQuery);

    }

    @Override
    public Reply exportTableList(QueryKnowledgeBaseParam qParam, HttpServletResponse response) {
        try {
            //1文件地址+名称
            String name = UUIDUtils.getUUID() + ".xlsx";
            Date now = new Date();
            String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
            File f = new File(paths);
            if (!f.exists()) {
                f.mkdirs();
            }
            String pathName = paths + name;

            List<MwKnowledgeBaseTable> knowledgeTable = new ArrayList();
            if (qParam.getSolution() != null && !"".equals(qParam.getSolution())) {
                //搜索引擎查询
                Map solution = new HashMap<>();
                solution.put("solution", qParam.getSolution());
                int start = 0;
                int end = 1000000;
                List<Map> mapList = LuceneUtils.searchByTypeIds(qParam.getTypeIds(), solution, start, end);
                for (Map map : mapList) {
                    MwKnowledgeBaseTable mwKnowledgeBaseTable = new MwKnowledgeBaseTable();
                    mwKnowledgeBaseTable.setId(map.get("id").toString());
                    Map priCriteria = PropertyUtils.describe(mwKnowledgeBaseTable);
                    knowledgeTable.addAll(mwKnowledgeBaseTableDao.selectTableList(priCriteria));
                }
            } else {
                //数据库查询
                if (qParam.getTypeIds() == null || qParam.getTypeIds().size() <= 0) {
                    qParam.setTypeIds(null);
                }
                if (qParam.getUserId() != null && qParam.getUserId() > 0) {
                    qParam.setCreator(iLoginCacheInfo.getLoginName());
                }
                Map priCriteria = PropertyUtils.describe(qParam);
                knowledgeTable = mwKnowledgeBaseTableDao.selectTableList(priCriteria);
            }
            //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            List<List<MwKnowledgeBaseTable>> list = getSubLists(knowledgeTable, 50000);

            //4创建easyExcel写出对象
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + UUIDUtils.getUUID() + ".xlsx");

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), MwKnowledgeBaseTable.class).build();
            WriteSheet sheet = EasyExcel.writerSheet(1, "sheet" + 1).build();
            excelWriter.write(list.get(0), sheet);

           /* //5计算sheet分页
            Integer sheetNum = list.size() % 50000 == 0 ? list.size() / 50000 : list.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                        .build();
                excelWriter.write(list.get(i), sheet);
            }*/

            excelWriter.finish();
            logger.info("KNOWLEDGE_LOG[]selectTableList[]获取知识库table分页");
            logger.info("selectTableList 获取知识库table分页,运行成功结束");
            return Reply.ok(pathName);
        } catch (Exception e) {
            log.error("fail to selectTableList param:{}  cause:{}", qParam, e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_BASE_SELECT_TABLE_CODE_309002, ErrorConstant.KNOWLEDGE_BASE_SELECT_TABLE_MSG_309002);
        }
    }

    @Override
    public Reply templateInfoImport(MultipartFile file, HttpServletResponse response) throws IOException {
        byte[] byteArr = file.getBytes();
        InputStream inputStream = new ByteArrayInputStream(byteArr);
        //获取工作簿
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        //加载
        List<AddOrUpdateKnowledgeBaseParam> maps = getAddOrUpdateKnowledgeBaseParam(workbook);
        for (AddOrUpdateKnowledgeBaseParam a:maps) {
            insertMyKnowledge(a);
        }
        return Reply.ok();
    }

    private List<AddOrUpdateKnowledgeBaseParam> getAddOrUpdateKnowledgeBaseParam(XSSFWorkbook workbook) {

        XSSFSheet hs = workbook.getSheetAt(0);
        List<AddOrUpdateKnowledgeBaseParam> addOrUpdateKnowledgeBaseParams = new ArrayList<>();

        //获取Sheet的第一个行号和最后一个行号
        int last = hs.getLastRowNum()+1;
        int first = hs.getFirstRowNum();
        for (int i = first + 1; i < last+1; i++) {
            try {
                AddOrUpdateKnowledgeBaseParam addOrUpdateKnowledgeBaseParam = new AddOrUpdateKnowledgeBaseParam();
                XSSFRow row = hs.getRow(i);
                //组装对象
                //组装第二列
                XSSFCell cell = row.getCell(1);
                addOrUpdateKnowledgeBaseParam.setTitle(cell.toString());
                //组装第三列
                XSSFCell cell2 = row.getCell(2);
                addOrUpdateKnowledgeBaseParam.setTriggerCause(cell2.toString());

                //组装第五列
                XSSFCell cell3 = row.getCell(3);
                addOrUpdateKnowledgeBaseParam.setAttachmentUrl(cell3.toString());

                //组装第五列
                XSSFCell cell4 = row.getCell(5);
                addOrUpdateKnowledgeBaseParam.setTypeId(Double.valueOf(cell4.toString()).intValue());
                //组装第六列
                XSSFCell cell5 = row.getCell(4);
                addOrUpdateKnowledgeBaseParam.setSolution(cell5.toString());
                addOrUpdateKnowledgeBaseParams.add(addOrUpdateKnowledgeBaseParam);
            }catch (Exception e){

            }

        }
        return addOrUpdateKnowledgeBaseParams;
    }

    //将list集合数据按照指定大小分成好几个小的list
        public <T> List<List<T>> getSubLists(List<T> allData, int size) {
            List<List<T>> result = new ArrayList();
            for (int begin = 0; begin < allData.size(); begin = begin + size) {
                int end = (begin + size > allData.size() ? allData.size() : begin + size);
                result.add(allData.subList(begin, end));
            }
            return result;
        }
    /**
     * 根据字符串获取数组对象
     *
     * @param s
     * @return
     */
    public List<FileList> getFileList(String s) {
        //处理url,变成list
        List<FileList> list = new ArrayList<>();
        if (s.indexOf(",") != -1) {
            //如果存在逗号说明有多个，数据库以逗号隔开
            String[] fileNames = s.split(",");
            if (fileNames.length > 0) {
                for (int i = 0; i < fileNames.length; i++) {
                    FileList fileList = new FileList();
                    //这是加密过的文件名
                    String fileName = fileNames[i];
                    fileList.setUrl(fileName);
                    fileList.setName(fileName.substring(fileName.indexOf("|") + 1));
                    list.add(fileList);
                }
            }
        } else {
            FileList fileList = new FileList();
            //这是加密过的文件名
            fileList.setUrl(s);
            //这是原文件名
            fileList.setName(s.substring(s.indexOf("|") + 1));
            list.add(fileList);
        }
        return list;
    }

    @Override
    public Reply transLikedFromRedisToMysql() {
        try {
            Reply likedDataFromRedis = redisService.getLikedDataFromRedis();
            List<MwKnowledgeUserMapper> list = (List<MwKnowledgeUserMapper>) likedDataFromRedis.getData();
            for (MwKnowledgeUserMapper mwKnowledgeUserMapper : list) {
                MwKnowledgeUserMapper knowledgeUserMapper = mwKnowledgeUserMapperDao
                        .selectLikedByKnowledgeIdAndUserId(mwKnowledgeUserMapper.getKnowledgeId(), mwKnowledgeUserMapper.getUserId());
                if (knowledgeUserMapper == null) {
                    //没有记录，直接存入
                    mwKnowledgeUserMapperDao.insert(mwKnowledgeUserMapper);
                } else {
                    //有记录，需要更新
                    mwKnowledgeUserMapperDao.updateStatus(mwKnowledgeUserMapper.getStatus(), mwKnowledgeUserMapper.getKnowledgeId(), mwKnowledgeUserMapper.getUserId());
                }
            }
            logger.info("KNOWLEDGE_LOG[]transLikedFromRedisToMysql[]点赞数据存入数据库");
            logger.info("transLikedFromRedisToMysql 点赞数据存入数据库,运行成功结束");
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to transLikedFromRedisToMysql cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_SAVE_MYSQL_CODE_309012, ErrorConstant.KNOWLEDGE_LIKED_SAVE_MYSQL_MSG_309012);
        }
    }

    @Override
    public Reply transLikedCountFromRedisToMysql() {
        try {
            Reply likedCountFromRedis = redisService.getLikedCountFromRedis();
            List<MwKnowledgeLikeOrHateRecord> list = (List<MwKnowledgeLikeOrHateRecord>) likedCountFromRedis.getData();
            for (MwKnowledgeLikeOrHateRecord mwKnowledgeLikeOrHateRecord : list) {
                MwKnowledgeLikeOrHateRecord knowledgeLikeOrHateRecord = mwKnowledgeLikeOrHateRecordDao
                        .selectByKnowledgeIdAndStatus(mwKnowledgeLikeOrHateRecord.getKnowledgeId(), mwKnowledgeLikeOrHateRecord.getStatus());
                if (knowledgeLikeOrHateRecord == null) {
                    //没有记录，直接存入
                    mwKnowledgeLikeOrHateRecordDao.insert(mwKnowledgeLikeOrHateRecord);
                } else {
                    //有记录，需要更新
                    Integer times = knowledgeLikeOrHateRecord.getTimes() + mwKnowledgeLikeOrHateRecord.getTimes();
                    mwKnowledgeLikeOrHateRecordDao.updateTimes(times, mwKnowledgeLikeOrHateRecord.getKnowledgeId(), mwKnowledgeLikeOrHateRecord.getStatus());
                }
            }
            logger.info("KNOWLEDGE_LOG[]transLikedCountFromRedisToMysql[]点赞数据数量存入数据库");
            logger.info("transLikedCountFromRedisToMysql 点赞数据数量存入数据库,运行成功结束");
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to transLikedCountFromRedisToMysql cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_SAVE_MYSQL_CODE_309012, ErrorConstant.KNOWLEDGE_LIKED_SAVE_MYSQL_MSG_309012);
        }
    }

    /**
     * 获取根据工作流状态划分的类型以及数量，总数量
     *
     * @return
     */
    public Map<String, Object> getTreeTypeMine() {
        Map<String, Object> map = new HashMap<>();
        List<TypeTreeDTO> list = new ArrayList<>();
        Integer totalCount = 0;
        for (Map.Entry<Integer, String> entry : knTreeTypeMap.entrySet()) {
            TypeTreeDTO typeTreeDTO = new TypeTreeDTO();
            typeTreeDTO.setActivitiStatus(entry.getKey());
            typeTreeDTO.setTypeName(entry.getValue());
            List<MwKnowledgeBaseTable> mwKnowledgeBaseTables = mwKnowledgeBaseTableDao.selectList(iLoginCacheInfo.getLoginName(), entry.getKey());
            typeTreeDTO.setKCount(mwKnowledgeBaseTables.size());
            totalCount = totalCount + mwKnowledgeBaseTables.size();
            //图片
            list.add(typeTreeDTO);
        }
        map.put("totalCount", totalCount);
        map.put("children", list);
        return map;
    }

    @Override
    public void editorActivitiParam(String processId, Integer activitiStatus, String knowledgeId) {
        mwKnowledgeBaseTableDao.editorActivitiParam(processId, activitiStatus, knowledgeId);
    }

    @Override
    public MwKnowledgeBaseTable selectByProcessId(String processId) {
        MwKnowledgeBaseTable mwKnowledgeBaseTable = mwKnowledgeBaseTableDao.selectByProcessId(processId);
        return mwKnowledgeBaseTable;
    }

    @Override
    public MwKnowledgeBaseTable selectById(String id) {
        MwKnowledgeBaseTable mwKnowledgeBaseTable = mwKnowledgeBaseTableDao.selectById(id);
        return mwKnowledgeBaseTable;
    }

    @Override
    public Integer getLikeOrHateCount(String knowledgeId, int status) {
        //从redis中查找点赞数量
        log.info("getLikeOrHateCount knowledgeId:" + knowledgeId);
        log.info("getLikeOrHateCount status:" + status);
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        Reply likedStatusAndCount = redisService.getLikedStatusAndCount(knowledgeId, userId);
        KnowledgeLikedDTO knowledgeLikedDTO = (KnowledgeLikedDTO) likedStatusAndCount.getData();
        Integer likedCount = knowledgeLikedDTO.getLikedCount();
        Integer liked = redisService.getLikedCount(knowledgeId, status);
        log.info("getLikeOrHateCount likedCount:" + likedCount);
        log.info("getLikeOrHateCount liked:" + liked);
        //从数据库中查点赞数量
        Integer times = mwKnowledgeLikeOrHateRecordDao.selectTimes(knowledgeId, status);
        log.info("getLikeOrHateCount times:" + times);
        times = (times == null ? 0 : times) + likedCount;
        return times == null ? 0 : times;
    }

    @Override
    public Map<String, Integer> getLikeOrHateListCount(List<String> knowledgeIds, int status) {
        Map<String, Integer> map = new HashMap<>();
        if (knowledgeIds.size() > 0) {
            knowledgeIds.forEach(knowledgeId -> {
                //从redis中查找点赞数量
                Integer likedCount = redisService.getLikedCount(knowledgeId, status);
                //从数据库中查点赞数量
                Integer times = mwKnowledgeLikeOrHateRecordDao.selectTimes(knowledgeId, status);
                times = (times == null ? 0 : times) + likedCount;
                map.put(knowledgeId, times == null ? 0 : times);
            });
        }
        return map;
    }

    @Override
    public Reply creatModelKenwSever(Object instanceParam, Integer type) {
        AddOrUpdateKnowledgeBaseParam param = new AddOrUpdateKnowledgeBaseParam();
        if (type == 0) {
            param = (AddOrUpdateKnowledgeBaseParam) instanceParam;
        } else {
            param = JSONObject.parseObject(instanceParam.toString(), AddOrUpdateKnowledgeBaseParam.class);
        }
        mwKnowledgeBaseTableDao.insert(param);
        MwKnowledgeBaseTable aParam = selectById(param.getId());
        LuceneFieldsDTO luceneFieldsDTO = new LuceneFieldsDTO(aParam.getId(), aParam.getTitle(), aParam.getTypeId().toString(), aParam.getTriggerCause(), aParam.getSolution());
        try {
            LuceneUtils.createIndex(luceneFieldsDTO);
        } catch (Exception e) {
            log.error("fail to LuceneUtils.createIndex(luceneFieldsDTO) cause:{}", e.getMessage());
        }
        return Reply.ok(param.getId());
    }

    @Override
    public Reply updateModelKenwSever(Object instanceParam, Integer type) {
        AddOrUpdateKnowledgeBaseParam param = new AddOrUpdateKnowledgeBaseParam();
        if (type == 0) {
            param = (AddOrUpdateKnowledgeBaseParam) instanceParam;
        } else {
            param = JSONObject.parseObject(instanceParam.toString(), AddOrUpdateKnowledgeBaseParam.class);
        }
        MwKnowledgeBaseTable aParam = selectById(param.getId());
        LuceneFieldsDTO luceneFieldsDTO = new LuceneFieldsDTO(param.getId(), param.getTitle(), param.getTypeId().toString(), param.getTriggerCause(), param.getSolution());
        try {
            LuceneUtils.updateIndex(luceneFieldsDTO);
        } catch (Exception e) {
            log.error("fail to LuceneUtils.createIndex(luceneFieldsDTO) cause:{}", e.getMessage());
        }
        mwKnowledgeBaseTableDao.update(param);
        return Reply.ok();
    }

    @Override
    public Reply deleteModelKenwSever(Object instanceParam, Integer type) {
        DeleteKnowledgeParam param = new DeleteKnowledgeParam();
        if (type == 0) {
            param = (DeleteKnowledgeParam) instanceParam;
        } else {
            param = JSONObject.parseObject(instanceParam.toString(), DeleteKnowledgeParam.class);
        }

        //删除数据库
        mwKnowledgeBaseTableDao.delete(param.getIds());
        //删除索引
        try {
            LuceneUtils.delete(param.getIds());
        } catch (Exception e) {
            log.error("fail to LuceneUtils.delete() aParam:{} cause:{}", param.getIds(), e.getMessage());
        }
        //删除关联点赞表
        mwKnowledgeUserMapperDao.delete(param.getIds());
        //删除点赞总数量表
        mwKnowledgeLikeOrHateRecordDao.delete(param.getIds());
        logger.info("KNOWLEDGE_LOG[]delete[]删除知识");
        logger.info("delete 删除知识,运行成功结束");
        return Reply.ok();
    }
}
