package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.link.dao.MWNetWorkLinkDao;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dao.MwReportTerraceManageDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.*;
import cn.mw.monitor.report.dto.linkdto.ExportLinkParam;
import cn.mw.monitor.report.dto.linkdto.InterfaceReportDtos;
import cn.mw.monitor.report.dto.linkdto.LinkHistoryDto;
import cn.mw.monitor.report.param.*;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.report.service.manager.*;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.user.api.*;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/5/9 16:12
 */
@Service
@Slf4j
@Transactional
public class MwReportServiceImpl implements MwReportService {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/report");

    private static Map<Integer, ReportBase> reportMap = new HashMap<>();

    static {
        ReportBase[] values = ReportBase.values();
        for (ReportBase reportBase : values) {
            reportMap.put(reportBase.getId(), reportBase);
        }
    }
    @Autowired
    private MwReportTerraceManageDao terraceManageDao;
    @Resource
    private MwReportDao mwReportDao;
    @Autowired
    private MwReportTerraceManageService terraceManageService;
    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;
    @Autowired
    private MWCommonService mwCommonService;
    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;
    @Autowired
    CpuReportManager cpuReportManager;
    @Autowired
    AssetsReportManager assetsReportManager;
    @Autowired
    LinkReportManager linkReportManager;
    @Autowired
    DiskReportManager diskReportManager;
    @Autowired
    NetWorkReportManager netWorkReportManager;
    @Autowired
    RunTimeReportManager runTimeReportManager;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Resource
    private MWNetWorkLinkDao mwNetWorkLinkDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MWUserService userService;

    @Override
    public Reply getReportType() {
        try {
            List<ReportTypeDto> list = mwReportDao.getReportType();
            logger.info("REPORT_LOG[]report[]报表[]查询报表类型[]{}", list);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getReportType with d={}, cause:{}", e);
            return Reply.fail(ErrorConstant.REPORT_TYPE_CODE_303004, ErrorConstant.REPORT_TYPE_MSG_303004);
        }
    }

    @Override
    public Reply getReportTimeTask() {
        try {
            List<ReportTimeTaskDto> list = mwReportDao.getReportTimeTask();
            logger.info("REPORT_LOG[]report[]报表[]查询报表定时任务[]{}", list);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getReportTimeTask with d={}, cause:{}", e);
            return Reply.fail(ErrorConstant.REPORT_TIME_TASK_CODE_303005, ErrorConstant.REPORT_TIME_TASK_MSG_303005);
        }
    }

    @Override
    public Reply getReportAction() {
        try {
            List<ReportActionDto> list = mwReportDao.getReportAction();
            logger.info("REPORT_LOG[]report[]报表[]查询报表定时任务通知方式[]{}", list);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getReportTimeTask with d={}, cause:{}", e);
            return Reply.fail(ErrorConstant.REPORT_ACTION_CODE_303010, ErrorConstant.REPORT_ACTION_MSG_303010);
        }
    }

    @Override
    public Reply creatReport(CreatAndUpdateReportParam creatAndUpdateReportParam) {
        try {
            //添加报表
            creatAndUpdateReportParam.setReportId(UuidUtil.getUid());
            String reportId = mwReportDao.insertReportTable(creatAndUpdateReportParam);
            InsertDto report = InsertDto.builder()
                    .groupIds(creatAndUpdateReportParam.getGroupIds())
                    .userIds(creatAndUpdateReportParam.getUserIds())
                    .orgIds(creatAndUpdateReportParam.getOrgIds())
                    .typeId(creatAndUpdateReportParam.getReportId())
                    .type(DataType.REPORT.getName())
                    .desc(DataType.REPORT.getDesc()).build();

            //添加负责人
            mwCommonService.addMapperAndPerm(report);
            //绑定定时任务
            List<MwTimeActionMapper> timeActionMappers = new ArrayList<>();
            creatAndUpdateReportParam.getActionNameIds().forEach(
                    actionId -> timeActionMappers.add(MwTimeActionMapper.builder().reportId(creatAndUpdateReportParam.getReportId()).actionId(actionId).build())
            );
            mwReportDao.insertReportTimeActionMapper(timeActionMappers);
            logger.info("REPORT_LOG[]report[]report报表[]新增报表数据[]{}[]", creatAndUpdateReportParam);
            return Reply.ok("新增成功");

        } catch (Exception e) {
            log.error("fail to insertWebMonitor with aParam={}, cause:{}", creatAndUpdateReportParam, e);
            return Reply.fail(ErrorConstant.REPORT_INSERT_CODE_303006, ErrorConstant.REPORT_INSERT_MSG_303006);
        }


    }


    @Override
    public Reply updateReport(CreatAndUpdateReportParam creatAndUpdateReportParam) {
        try {
            //修改报表
            String loginName = iLoginCacheInfo.getLoginName();
            creatAndUpdateReportParam.setModifier(loginName);
            mwReportDao.updateReportTable(creatAndUpdateReportParam);
            //修改报表用户组
            DeleteDto deleteDto = DeleteDto.builder().typeId(creatAndUpdateReportParam.getReportId()).type(DataType.REPORT.getName()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);

            InsertDto report = InsertDto.builder()
                    .groupIds(creatAndUpdateReportParam.getGroupIds())
                    .userIds(creatAndUpdateReportParam.getUserIds())
                    .orgIds(creatAndUpdateReportParam.getOrgIds())
                    .typeId(creatAndUpdateReportParam.getReportId())
                    .type(DataType.REPORT.getName())
                    .desc(DataType.REPORT.getDesc()).build();

            //添加负责人
            mwCommonService.addMapperAndPerm(report);
            //修改报表定时任务
            mwReportDao.deleteReportTimeActionMapper(creatAndUpdateReportParam.getReportId());
            mwReportDao.deleteReportTimeRuleMapper(creatAndUpdateReportParam.getReportId());
            //批量添加关联方式
            if (null != creatAndUpdateReportParam.getActionNameIds() && creatAndUpdateReportParam.getActionNameIds().size() > 0) {
                List<MwTimeActionMapper> timeActionMappers = new ArrayList<>();
                creatAndUpdateReportParam.getActionNameIds().forEach(
                        actionId -> timeActionMappers.add(MwTimeActionMapper.builder().reportId(creatAndUpdateReportParam.getReportId()).actionId(actionId).build())
                );
                mwReportDao.insertReportTimeActionMapper(timeActionMappers);
            }
            if (null != creatAndUpdateReportParam.getRuleIds() && creatAndUpdateReportParam.getRuleIds().size() > 0) {
                //批量添加通知规则
                List<MwReportRuleMapper> ruleMappers = new ArrayList<>();
                creatAndUpdateReportParam.getRuleIds().forEach(
                        ruleId -> {
                            if (null != ruleId) {
                                MwReportRuleMapper build = MwReportRuleMapper.builder().reportId(creatAndUpdateReportParam.getReportId()).ruleId(ruleId).build();
                                if (creatAndUpdateReportParam.getActionNameIds().contains(3)) {
                                    build.setRuleType(3);
                                } else {
                                    build.setRuleType(0);
                                }
                                ruleMappers.add(build);
                            }
                        }
                );
                if (ruleMappers.size() > 0) {
                    mwReportDao.insertReportRuleMapper(ruleMappers);
                }
            }
            //修改通知用户和用户组数据
            List<Integer> noticeUser = creatAndUpdateReportParam.getNoticeUser();
            List<Integer> noticeUserGroup = creatAndUpdateReportParam.getNoticeUserGroup();
            String reportId = creatAndUpdateReportParam.getReportId();
            List<MWReportSendUserDto> sendUserDtos = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(noticeUser)){
                for (Integer userId : noticeUser) {
                    MWReportSendUserDto dto = new MWReportSendUserDto();
                    dto.setReportId(reportId);
                    dto.setType("USER");
                    dto.setUserId(userId);
                    sendUserDtos.add(dto);
                }
            }
            if(CollectionUtils.isNotEmpty(noticeUserGroup)){
                for (Integer groupId : noticeUserGroup) {
                    MWReportSendUserDto dto = new MWReportSendUserDto();
                    dto.setReportId(reportId);
                    dto.setType("GROUP");
                    dto.setGroupId(groupId);
                    sendUserDtos.add(dto);
                }
            }
            //删除原有数据
            mwReportDao.deleteReportSendUser(reportId);
            if(CollectionUtils.isNotEmpty(sendUserDtos) && creatAndUpdateReportParam.getSendTime()){
                //添加新的数据
                mwReportDao.insertReportSendUser(sendUserDtos);
            }
            logger.info("REPORT_LOG[]report[]report报表[]修改报表数据[]{}[]", creatAndUpdateReportParam);
            return Reply.ok("修改成功");

        } catch (Exception e) {
            log.error("fail to updateReport with aParam={}, cause:{}", creatAndUpdateReportParam, e);
            return Reply.fail(ErrorConstant.REPORT_UPDATE_CODE_303007, ErrorConstant.REPORT_UPDATE_MSG_303007);
        }


    }

    @Override
    public Reply deleteReport(DeleteParam deleteParam) {
        try {
            logger.info("REPORT_LOG[]report[]report报表[]批量删除报表 []{}[]", deleteParam);
            if (null != deleteParam && deleteParam.getReportIdList().size() > 0) {
                mwReportDao.deleteReportTable(deleteParam.getReportIdList());
                //删除负责人和权限
                List<String> reportIdList = deleteParam.getReportIdList();
                reportIdList.forEach(id -> {
                    DeleteDto report = DeleteDto.builder().typeId(id).type(DataType.REPORT.getName()).build();
                    mwCommonService.deleteMapperAndPerm(report);
                    mwReportDao.deleteReportSendUser(id);
                });
                return Reply.ok("删除成功");

            } else {
                return Reply.ok("传参有误");
            }
        } catch (Exception e) {
            log.error("fail to deleteReport with aParam={}, cause:{}", deleteParam, e);
            return Reply.fail(ErrorConstant.REPORT_DELETE_CODE_303008, ErrorConstant.REPORT_DELETE_MSG_303008);
        }
    }


    @Override
    public Reply selectReport(QueryReportParam queryReportParam) {
        try {
            List<MwReportTable> list = new ArrayList<>();

            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != groupIds && groupIds.size() > 0) {
                queryReportParam.setGroupIds(groupIds);
            }
            switch (dataPermission) {
                case PRIVATE:
                    queryReportParam.setUserId(userId);
                    PageHelper.startPage(queryReportParam.getPageNumber(), queryReportParam.getPageSize());
                    Map priCriteria = PropertyUtils.describe(queryReportParam);
                    list = mwReportDao.selectPriReport(priCriteria);
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
                        queryReportParam.setOrgIds(orgIds);
                    }
                    queryReportParam.setIsAdmin(isAdmin);
                    PageHelper.startPage(queryReportParam.getPageNumber(), queryReportParam.getPageSize());
                    Map pubCriteria = PropertyUtils.describe(queryReportParam);
                    list = mwReportDao.selectPubReport(pubCriteria);
                    break;
            }
            //处理机构和用户组数据
            handleOrgAndUserGroup(list);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);

            logger.info("REPORT_LOG[]report[]report报表[]报表查询信息[]{}[]", queryReportParam);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            logger.error("fail to selectReport with mtaDTO={}, cause:{}", queryReportParam, e);
            return Reply.fail(ErrorConstant.REPORT_SELECT_CODE_303009, ErrorConstant.REPORT_SELECT_MSG_303009);
        }
    }

    /**
     * 设置机构和用户组字符串
     * @param list
     */
    private void handleOrgAndUserGroup(List<MwReportTable> list){
        if(CollectionUtils.isEmpty(list))return;
        for (MwReportTable mwReportTable : list) {
            List<OrgDTO> department = mwReportTable.getDepartment();
            if(CollectionUtils.isNotEmpty(department)){
                List<String> collect = department.stream().map(OrgDTO::getOrgName).collect(Collectors.toList());
                mwReportTable.setDepartmentString(String.join(",", collect));
            }
            List<UserDTO> principal = mwReportTable.getPrincipal();
            if(CollectionUtils.isNotEmpty(principal)){
                List<String> collect = principal.stream().map(UserDTO::getUserName).collect(Collectors.toList());
                mwReportTable.setPrincipalString(String.join(",", collect));
            }
            List<GroupDTO> groups = mwReportTable.getGroups();
            if(CollectionUtils.isNotEmpty(groups)){
                List<String> collect = groups.stream().map(GroupDTO::getGroupName).collect(Collectors.toList());
                mwReportTable.setGroupsString(String.join(",", collect));
            }
        }
    }

    @Override
    public Reply selectById(String reportId) {
        try {
            MwReportTable mwReportTable = mwReportDao.selectById(reportId);
            // usergroup重新赋值使页面可以显示
            List<Integer> groupIds = new ArrayList<>();
            mwReportTable.getGroups().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            mwReportTable.setGroupIds(groupIds);
            // user重新赋值
            List<Integer> userIds = new ArrayList<>();
            mwReportTable.getPrincipal().forEach(
                    userDTO -> userIds.add(userDTO.getUserId())
            );
            mwReportTable.setUserIds(userIds);
            // 机构重新赋值使页面可以显示

            List<List<Integer>> orgNodes = new ArrayList<>();
            if (mwReportTable.getDepartment() != null && mwReportTable.getDepartment().size() > 0) {
                mwReportTable.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                mwReportTable.setOrgIds(orgNodes);
            }
            logger.info("ACCESS_LOG[]TangibleAssets[]有形资产管理[]根据自增序列ID取资产信息[]{}", reportId);
            //查询通知用户和通知用户组
            List<MWReportSendUserDto> sendUserDtos = mwReportDao.selectReportSendUser(reportId);
            List<Integer> noticeUser = new ArrayList<>();
            List<Integer> noticeUserGroup = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(sendUserDtos)){
                for (MWReportSendUserDto sendUserDto : sendUserDtos) {
                    int groupId = sendUserDto.getGroupId();
                    int userId = sendUserDto.getUserId();
                    String type = sendUserDto.getType();
                    if(StringUtils.isNotBlank(type) && "USER".equals(type)){
                        noticeUser.add(userId);
                    }
                    if(StringUtils.isNotBlank(type) && "GROUP".equals(type)){
                        noticeUserGroup.add(groupId);
                    }
                }
            }
            mwReportTable.setNoticeUser(noticeUser);
            mwReportTable.setNoticeUserGroup(noticeUserGroup);
            List<String> actionNames = new ArrayList<>();
            actionNames.add("邮件");
            List<Integer> actionIds = new ArrayList<>();
            actionIds.add(3);
            mwReportTable.setActionName(actionNames);
            mwReportTable.setActionNameIds(actionIds);
            return Reply.ok(mwReportTable);
        } catch (Exception e) {
            log.error("fail to selectById with d={}, cause:{}", reportId, e);
            return Reply.fail(ErrorConstant.REPORT_SELECT_CODE_303009, ErrorConstant.REPORT_SELECT_MSG_303009);
        }
    }


    @Override
    public List<NetWorkDto> getNetTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime) {
        return netWorkReportManager.getNetTrends(mwTangibleassetsDTOS, startTime, endTime);
    }

    /**
     * 查询报表详情,根据报表id区分不同的报表
     *
     * @param trendParam
     * @return
     */
    @Override
    public Reply getReportDetail(TrendParam trendParam) {
        try {
            Integer reportId = trendParam.getReportId();
            ReportBase reportBase = reportMap.get(reportId);
            List list = new ArrayList();
            PageInfo pa = new PageInfo();
            PageList pageList = new PageList();
            PageInfo pageInfo = new PageInfo<>();
            List newList = new ArrayList();
            int count = 0;
            int psize = trendParam.getPageSize();
            switch (reportBase) {
                case CPUANDMEMORY:
                    pa = getMwTangibleassets(trendParam);
                    list = getCpuAndMemoryTrend(trendParam);
                    count = list.size();
                    newList = pageList.getList(list, trendParam.getPageNumber(), trendParam.getPageSize());
                    pageInfo = new PageInfo<>(list);
                    pageInfo.setPages(count % psize == 0 ? count / psize : count / psize + 1);
                    pageInfo.setPageNum(trendParam.getPageNumber());
                    pageInfo.setEndRow(pageList.getEndRow());
                    pageInfo.setStartRow(pageList.getStartRow());
                    pageInfo.setPageSize(psize);
                    pageInfo.setList(newList);
                    break;
                case DISK:
                    pa = getMwTangibleassets(trendParam);
                    list = getDiskTrend(trendParam);
                    count = list.size();
                    newList = pageList.getList(list, trendParam.getPageNumber(), trendParam.getPageSize());
                    pageInfo = new PageInfo<>(list);
                    pageInfo.setPages(count % psize == 0 ? count / psize : count / psize + 1);
                    pageInfo.setPageNum(trendParam.getPageNumber());
                    pageInfo.setEndRow(pageList.getEndRow());
                    pageInfo.setStartRow(pageList.getStartRow());
                    pageInfo.setPageSize(psize);
                    pageInfo.setList(newList);
                    break;
                case NETWORK:
                    pa = getMwTangibleassets(trendParam);
                    list = getNetTrend(trendParam);
                    count = list.size();
                    newList = pageList.getList(list, trendParam.getPageNumber(), trendParam.getPageSize());
                    pageInfo = new PageInfo<>(list);
                    pageInfo.setPages(count % psize == 0 ? count / psize : count / psize + 1);
                    pageInfo.setPageNum(trendParam.getPageNumber());
                    pageInfo.setEndRow(pageList.getEndRow());
                    pageInfo.setStartRow(pageList.getStartRow());
                    pageInfo.setPageSize(psize);
                    pageInfo.setList(newList);
                    break;
                case LINK:
                    log.info("查询线路流量zabbix报表"+trendParam);
                    list = linkReportManager.getLink(trendParam);
                    count = list.size();
                    newList = pageList.getList(list, trendParam.getPageNumber(), trendParam.getPageSize());
                    pageInfo = new PageInfo<>(list);
                    pageInfo.setPages(count % psize == 0 ? count / psize : count / psize + 1);
                    pageInfo.setPageNum(trendParam.getPageNumber());
                    pageInfo.setEndRow(pageList.getEndRow());
                    pageInfo.setStartRow(pageList.getStartRow());
                    pageInfo.setPageSize(psize);
                    pageInfo.setList(newList);
                    break;
                case ASSETS_COLLECTION:
                    //trendParam.setAssetsTypeId(MWUtils.ASSETS_TYPE.get("网络设备"));//网络设备
                    pa = getMwTangibleassets(trendParam);
                    list = assetsReportManager.getReport(trendParam);
                    count = list.size();
                    newList = pageList.getList(list, trendParam.getPageNumber(), trendParam.getPageSize());
                    pageInfo = new PageInfo<>(list);
                    pageInfo.setPages(count % psize == 0 ? count / psize : count / psize + 1);
                    pageInfo.setPageNum(trendParam.getPageNumber());
                    pageInfo.setEndRow(pageList.getEndRow());
                    pageInfo.setStartRow(pageList.getStartRow());
                    pageInfo.setPageSize(psize);
                    pageInfo.setList(newList);
                    break;
                default:
                    break;
            }
            logger.info("REPORT_LOG[]report[]report报表[]根据报表Id查询详情信息[]{}[]", trendParam);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getReportDetail with mtaDTO={}, cause:{}", trendParam, e);
            return Reply.fail(ErrorConstant.REPORT_DETAIL_CODE_303001, ErrorConstant.REPORT_DETAIL_MSG_303001);
        }
    }

    /**
     * 查询出报表对应的资产数据
     *
     * @param trendParam
     * @return
     */

    public PageInfo getMwTangibleassets(TrendParam trendParam) {
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        PageHelper.startPage(trendParam.getPageNumber(), trendParam.getPageSize());
        QueryTangAssetsParam query = new QueryTangAssetsParam();
        //导出时查询所有，分页在查询所有后手动分页
        query.setPageNumber(-1);
        query.setPageSize(0);
        query.setAssetsTypeId(trendParam.getAssetsTypeId());
        query.setManufacturer(trendParam.getManufacturer());
        query.setAssetsName(trendParam.getAssetsName());
        query.setAssetsTypeName(trendParam.getAssetsTypeName());
        query.setModificationDateStart(trendParam.getModificationDateStart());
        query.setModificationDateEnd(trendParam.getModificationDateEnd());
        query.setSpecifications(trendParam.getSpecifications());
        query.setAssetsTypeSubName(trendParam.getAssetsTypeSubName());
        query.setMonitorFlag(trendParam.getMonitorFlag());
        query.setInBandIp(trendParam.getAssertIp());
        Reply reply = mwTangibleAssetsService.selectList(query);
        Object data = reply.getData();

        PageInfo newPageInfo = new PageInfo<>();
        if (null != data) {
            PageInfo pageInfo = (PageInfo) data;
            mwTangibleassetsDTOS = pageInfo.getList();
            trendParam.setMwTangibleassetsDTOS(mwTangibleassetsDTOS);
            newPageInfo.setTotal(pageInfo.getTotal());
            newPageInfo.setPageNum(pageInfo.getPageNum());
        }
        return newPageInfo;
    }


    @Override
    public Reply getReportCount(ReportCountParam reportCountParam) {
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            reportCountParam.setGroupIds(groupIds);
        }

        List<ReportCountDto> list = null;
        switch (dataPermission) {
            case PRIVATE:
                reportCountParam.setUserId(userId);
                list = mwReportDao.selectPriReportCount(reportCountParam);
                break;
            case PUBLIC:
                String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                List<Integer> orgIds = new ArrayList<>();
                Boolean isAdmin = false;
                if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                    isAdmin = true;
                }
                if (!isAdmin) {
                    //List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                    //orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);
                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);

                }
                if (null != orgIds && orgIds.size() > 0) {
                    reportCountParam.setOrgIds(orgIds);
                }
                reportCountParam.setIsAdmin(isAdmin);
                list = mwReportDao.selectPubReportCount(reportCountParam);
                break;
        }
        List<ReportCountDto> newList = new ArrayList();
        //list分组转map
        Map<Integer, List<ReportCountDto>> collect = list.stream().collect(Collectors.groupingBy(ReportCountDto::getReportTypeId));
        int sum = 0;
        for (Integer key : collect.keySet()) {
            List<ReportCountDto> listparam = collect.get(key);
            int count = 0;
            for (ReportCountDto dto : listparam) {
                count += 1;
                sum += 1;
            }
            listparam.get(0).setCount(count);
            newList.add(listparam.get(0));
        }
        ReportCountDto newDto = new ReportCountDto();
        newDto.setChildren(newList);
        newDto.setCount(sum);
        newDto.setIcon("all");
        newDto.setReportTypeName("全部分类");
        List<ReportCountDto> allList = new ArrayList();
        allList.add(newDto);
        return Reply.ok(allList);
    }

    @Override
    public Reply selectLinkHistory(TrendParam trendParam) {
        List<LinkHistoryDto> linkHistoryDtos = linkReportManager.selectLinkHistory(trendParam);
        return Reply.ok(linkHistoryDtos);
    }

    @Override
    public Reply getHistoryByList(TrendParam param) {
        List<List<LinkHistoryDto>> linkHistoryDtos = linkReportManager.getHistoryByList(param);
        return Reply.ok(linkHistoryDtos);
    }

    @Override
    public Reply groupSelect(TrendParam param) {
        List<Map<String, String>> list = linkReportManager.groupSelect(param);
        return Reply.ok(list);
    }

    @Override
    public void exportLink(ExportLinkParam uParam, HttpServletResponse response) {
        linkReportManager.exportLink(uParam, response);
    }

    @Override
    public void export(ExcelReportParam uParam, HttpServletResponse response) {
        ReportBase reportBase = reportMap.get(uParam.getReportId());
        Class dtoclass = null;
        List list = null;
        PageInfo pa;
        TrendParam trendParam = new TrendParam();
        trendParam.setReportId(uParam.getReportId());
        trendParam.setChooseTime(uParam.getChooseTime());
        trendParam.setSeniorchecked(uParam.getSeniorchecked());
        trendParam.setAssetsTypeId(uParam.getAssetsTypeId());
        trendParam.setDayType(uParam.getDayType());
        switch (reportBase) {
            case CPUANDMEMORY:
                dtoclass = CpuAndMemoryDto.class;
                pa = getMwTangibleassets(trendParam);
                list = getCpuAndMemoryTrend(trendParam);
                break;
            case DISK:
                dtoclass = TrendDiskDto.class;
                pa = getMwTangibleassets(trendParam);
                list = getDiskTrend(trendParam);
                break;
            case NETWORK:
                dtoclass = TrendNetDto.class;
                pa = getMwTangibleassets(trendParam);
                list = getNetTrend(trendParam);
                break;
            case ASSETS_COLLECTION:
                trendParam.setAssertIp(uParam.getAssertIp());
                trendParam.setAssetsName(uParam.getAssetsName());
                trendParam.setAssetsTypeName(uParam.getAssetsTypeName());
                trendParam.setAssetsTypeSubName(uParam.getAssetsTypeSubName());
                trendParam.setManufacturer(uParam.getManufacturer());
                trendParam.setModificationDateStart(uParam.getModificationDateStart());
                trendParam.setModificationDateEnd(uParam.getModificationDateEnd());
                trendParam.setMonitorFlag(uParam.getMonitorFlag());
                trendParam.setSpecifications(uParam.getSpecifications());
                dtoclass = AssetsDto.class;
//                list = uParam.getAssetsDtoList();
                pa = getMwTangibleassets(trendParam);
                list = assetsReportManager.getReport(trendParam);
                break;
            default:
                break;
        }
        ExcelWriter excelWriter = null;
        try {
            Set<String> includeColumnFiledNames = new HashSet<>();
            if (uParam.getFields() != null && uParam.getFields().size() > 0 && dtoclass != null) {
                includeColumnFiledNames = uParam.getFields();
            }
            //创建easyExcel写出对象
            excelWriter = ReportUtil.getExcelWriter(uParam, response, dtoclass);
            //计算sheet分页
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
            logger.info("导出成功");
        } catch (Exception e) {
            logger.error("导出失败", e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }

    @Override
    public Reply selectLinkEditDropdown(TrendParam trendParam) {
        List<Object> list = new ArrayList();
        Map<String, String> map = new HashMap<>();
        NetWorkLinkDto netWorkLinkDto = mwNetWorkLinkDao.selectById(trendParam.getInterfaceID());
        String rootPort = netWorkLinkDto.getRootPort();
        String targetPort = netWorkLinkDto.getTargetPort();
        if (!trendParam.getSeniorchecked()) {
            map.put("rootPort", rootPort);
            map.put("targetPort", "");
            list.add(map);
            return Reply.ok(list);
        }
        map.put("rootPort", rootPort);
        if (targetPort == null) {
            map.put("targetPort", "");
        } else {
            map.put("targetPort", targetPort);
        }
        list.add(map);
        return Reply.ok(list);
    }

    @Override
    public Reply getRunTimeReportOfAeest(RunTimeQueryParam param) {
        log.info("运行状态报表导出word,service参数"+param);
        Map<String, List<Integer>> map = null;
        try {
            List<Date> dateByType = runTimeReportManager.getDateByType(param);
            Date dateStart = dateByType.get(0);
            Date dateEnd = dateByType.get(1);
            List<AssetByTypeDto> assetByTypeDtos = getAssetsInfo();
            List<AssetByTypeDto> assetByTypeDtos3 = assetByTypeDtos;
            List<AssetByTypeDto> assetByTypeDtos1 = assetByTypeDtos;
            log.info("wprd导出1"+assetByTypeDtos+"deteEnd"+dateEnd);
            log.info("wprd导出2"+assetByTypeDtos1+"dateStart"+dateStart);
            log.info("wprd导出3"+assetByTypeDtos3+"dateStart"+dateStart);
            Boolean flag = false;
            if (assetByTypeDtos.size() == 0) {
                return null;
            }
            if (assetByTypeDtos.equals(assetByTypeDtos1)) {
                flag = true;
            }
            //加资产健康状态
            if (assetByTypeDtos != null && assetByTypeDtos.size() > 0) {
                Map<Integer, List<String>> groupMap = assetByTypeDtos.stream().filter(item->item.getAssetsId() != null && item.getMonitorServerId() != null)
                        .collect(Collectors.groupingBy(AssetByTypeDto::getMonitorServerId, Collectors.mapping(AssetByTypeDto::getAssetsId, Collectors.toList())));
                Map<String, String> statusMap = new HashMap<>();
                for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                    if (value.getKey() != null && value.getKey() > 0) {
                        //有改动-zabbi
                        MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.ASSETS_STATUS, value.getValue());
                        if (!statusData.isFail()) {
                            JsonNode jsonNode = (JsonNode) statusData.getData();
                            if (jsonNode.size() > 0) {
                                for (JsonNode node : jsonNode) {
                                    Integer lastvalue = node.get("lastvalue").asInt();
                                    String hostId = node.get("hostid").asText();
                                    String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                    statusMap.put(value.getKey() + ":" + hostId, status);
                                }
                            }
                        }
                        /*statusMap.put(value.getKey() + ":" + value.getValue(), "ABNORMAL");*/
                    }
                }
                String status = "";
                for (AssetByTypeDto asset : assetByTypeDtos) {
                    String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
                    if (s != null && StringUtils.isNotEmpty(s)) {
                        status = s;
                    } else {
                        status = "UNKNOWN";
                    }
                    asset.setItemAssetsStatus(status);
                }
            }

            Map<Integer, List<AssetByTypeDto>> collect = assetByTypeDtos.stream().collect(Collectors.groupingBy(AssetByTypeDto::getAssetsTypeId));
            Map<Integer, List<AssetByTypeDto>> collect1 = assetByTypeDtos1.stream().collect(Collectors.groupingBy(AssetByTypeDto::getAssetsTypeId));
            map = new HashMap<>();

            if (flag) {
                for (Map.Entry<Integer, List<AssetByTypeDto>> en : collect.entrySet()) {
                    String typeName = en.getValue().get(0).getTypeName();
                    Map<String, Integer> map1 = new HashMap<>();
                    Map<String, List<AssetByTypeDto>> collect2 = en.getValue().stream().collect(Collectors.groupingBy(AssetByTypeDto::getItemAssetsStatus));
                    for (Map.Entry<String, List<AssetByTypeDto>> entry : collect2.entrySet()) {
                        String key = entry.getKey();
                        int size = entry.getValue().size();
                        map1.put(key, size);
                    }
                    map.put(typeName, Arrays.asList(en.getValue().size(), map1.get("NORMAL") == null ? 0 : map1.get("NORMAL"),
                            en.getValue().size() - (map1.get("NORMAL") == null ? 0 : map1.get("NORMAL")), 0));
                }
                int totalNormalCount = assetByTypeDtos.stream().collect(Collectors.groupingBy(AssetByTypeDto::getItemAssetsStatus)).get("NORMAL").size();
                map.put("total", Arrays.asList(assetByTypeDtos.size(), totalNormalCount, assetByTypeDtos.size() - totalNormalCount, 0));
            } else {
                int allIncrease = 0;
                Set<Integer> keySet = collect.keySet();
                Set<Integer> keySet1 = collect1.keySet();

                for (Map.Entry<Integer, List<AssetByTypeDto>> en : collect.entrySet()) {
                    final Integer enKey = en.getKey();
                    String typeName = en.getValue().get(0).getTypeName();
                    Map<String, Integer> map1 = new HashMap<>();
                    Map<String, List<AssetByTypeDto>> collect3 = en.getValue().stream().collect(Collectors.groupingBy(AssetByTypeDto::getItemAssetsStatus));
                    for (Map.Entry<String, List<AssetByTypeDto>> entry : collect3.entrySet()) {
                        String key = entry.getKey();
                        int size = entry.getValue().size();
                        map1.put(key, size);
                    }

                    for (Map.Entry<Integer, List<AssetByTypeDto>> entry : collect1.entrySet()) {
                        final Integer entryKey = entry.getKey();
                        if (enKey.equals(entryKey)) {
                            String typeName1 = entry.getValue().get(0).getTypeName();
                            int itemIncrease = en.getValue().size() - entry.getValue().size();

                            map.put(typeName, Arrays.asList(en.getValue().size(), map1.get("NORMAL") == null ? 0 : map1.get("NORMAL"),
                                    en.getValue().size() - (map1.get("NORMAL") == null ? 0 : map1.get("NORMAL")), itemIncrease));
                            allIncrease += itemIncrease;
                            break;
                        }
                    }
                }
                boolean removeAll = keySet.removeAll(keySet1);
                if (removeAll) {
                    if (keySet.size() > 0) {
                        for (Integer key : keySet) {
                            List<AssetByTypeDto> dtoList = collect.get(key);
                            List<AssetByTypeDto> col = dtoList.stream().filter(f -> f.getItemAssetsStatus() == "NORMAL").collect(Collectors.toList());
                            String typeName = dtoList.get(0).getTypeName();
                            map.put(typeName, Arrays.asList(dtoList.size(), col.size(), dtoList.size() - col.size(), dtoList.size()));
                            allIncrease += dtoList.size();
                        }
                    }
                }
                int totalNormalCount = assetByTypeDtos.stream().collect(Collectors.groupingBy(AssetByTypeDto::getItemAssetsStatus)).get("NORMAL").size();
                map.put("total", Arrays.asList(assetByTypeDtos.size(), totalNormalCount, assetByTypeDtos.size() - totalNormalCount, allIncrease));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Reply.fail(e.getMessage());
        }
        log.info("运行状态报表导出word,service返回数据"+map);
        return Reply.ok(map);
    }

    /**
     * 获取资产信息
     * @return
     */
    private List<AssetByTypeDto> getAssetsInfo(){
        List<AssetByTypeDto> typeDtos = new ArrayList<>();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(assetsParam);
        if(CollectionUtils.isEmpty(assetsTable)){return typeDtos;}
        for (MwTangibleassetsTable mwTangibleassetsTable : assetsTable) {
            AssetByTypeDto typeDto = new AssetByTypeDto();
            typeDto.extractFrom(mwTangibleassetsTable);
            typeDtos.add(typeDto);
        }
        return typeDtos;
    }

    @Override
    public Reply getRunTimeItemUtilization(RunTimeQueryParam param) {
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        Long startTime = longTimeByType.get(0);
        Long endTime = longTimeByType.get(1);
        Integer userId = null;
        Boolean timingType = param.getTimingType();
        userId = mwUserCommonService.getAdmin();
        List<RunTimeItemValue> list = new ArrayList<>();
        if (param.getDateType() != 0 && !param.getTimingType()) {
            list = getOldData(param.getItemName(),new Date(startTime*1000l),new Date(endTime*1000l));
        } else {
            list = runTimeReportManager.getrunTimeMemory(userId, param.getItemName(), startTime, endTime, param.getDateType());
        }
        if (null != list && list.size() > 0) {
            list = list.stream().filter(f -> f != null).collect(Collectors.toList());
//            list=list.stream().sorted(Comparator.comparing(RunTimeItemValue::getAvgValue).reversed()).collect(Collectors.toList());
            Collections.sort(list, new RunTimeItemValue());
            if (list.size() > param.getDataSize()) {
                list = list.subList(0, param.getDataSize());
            }
        }
        return Reply.ok(list);
    }

    @Override
    public Reply getRunTimeItem(RunTimeQueryParam param) {
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        Long startTime = longTimeByType.get(0);
        Long endTime = longTimeByType.get(1);
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        List<RunTimeItemValue> list = null;
        try {
            if (param.getDateType() != 0 && !param.getTimingType()) {
                if (param.getItemName().equals("DISK_UTILIZATION")) {
                    list = getOldData(param.getItemName(), new Date(startTime * 1000l), new Date(endTime * 1000l));
                } else {
                    list = getOldInData(param.getItemName(), new Date(startTime * 1000l), new Date(endTime * 1000l));
                }
            } else {
                list = runTimeReportManager.getrunTimeMemory(userId, param.getItemName(), startTime, endTime, param.getDateType());
            }
            if (null != list && list.size() > 0) {
                list = list.stream().filter(f -> StringUtils.isNotEmpty(f.getAssetName())).collect(Collectors.toList());
//                list=list.stream().sorted(Comparator.comparing(RunTimeItemValue::getAvgValue).reversed()).collect(Collectors.toList());
                Collections.sort(list, new RunTimeItemValue());
                if (list.size() > param.getDataSize()) {
                    list = list.subList(0, param.getDataSize());
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Reply.fail(e.getMessage());
        }
        return Reply.ok(list);
    }

    @Override
    public Reply getRunTimeAssetUtilization(RunTimeQueryParam param) {
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        Long startTime = longTimeByType.get(0);
        Long endTime = longTimeByType.get(1);
//        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        List<RunTimeItemValue> list = new ArrayList<>();
        try {
            RunTimeQueryParam params = new RunTimeQueryParam();
            params.setPageNumber(1);
            params.setPageSize(param.getDataSize());

            params.setDateType(chage(param.getDateType()));
            params.setChooseTime(param.getChooseTime());
            Reply reply = terraceManageService.selectReportAssetsUsability(params, false);
            PageInfo data = (PageInfo) reply.getData();
            List<MwAssetsUsabilityParam> kill = data.getList();
            if (kill != null && kill.size() > 0) {
                for (MwAssetsUsabilityParam m : kill) {
                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                    runTimeItemValue.setAssetName(m.getAssetsName());
                    runTimeItemValue.setAssetUtilization(m.getAssetsUsability());
                    runTimeItemValue.setIp(m.getIp());
                    list.add(runTimeItemValue);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Reply.fail(e.getMessage());
        }
        return Reply.ok(list);
    }

    private Integer chage(Integer dateType) {
        switch (dateType) {
            case 0:
                dateType = 2;
                break;
            case 1:
                dateType = 1;
                break;
            case 2:
                dateType = 5;
                break;
            default:
                dateType = 11;
                break;
        }

        return dateType;
    }

    @Override
    public Reply getRunTimeReportTrend(RunTimeQueryParam param) {
        PeriodTrendDto assetPeriodTrendDto = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            assetPeriodTrendDto = new PeriodTrendDto();
            Calendar calendar = Calendar.getInstance();
            String startTime = "";
            String endTime = "";
            switch (param.getDateType()) {
                case 0:
                    //            今天
                    endTime = format.format(new Date(calendar.getTimeInMillis()));
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                case 1:
                    //            昨天
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    endTime = format.format(new Date(calendar.getTimeInMillis()));

                    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                case 2:
                    //            上周
                    //判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题
                    int dayWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    if (0 == dayWeek) {
                        dayWeek = 7;
                    }
                    calendar.add(Calendar.DATE, -dayWeek + 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    endTime = format.format(new Date(calendar.getTimeInMillis()));
                    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 7);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                case 3:
                    //            月度
                    endTime = format.format(new Date(calendar.getTimeInMillis()));
                    calendar.add(Calendar.MONTH, -1);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                default:
                    break;

            }

            param.getTrendType();
            RuntimeTrendEnum trendEnum = RuntimeTrendEnum.getByValue(param.getTrendType());
            switch (trendEnum) {
                case ALERT_TREND:
                    assetPeriodTrendDto = runTimeReportManager.getAlertTrendDto(param);
                    assetPeriodTrendDto.setUnits("次");
                    break;
                case ASSET_TREND:
                    assetPeriodTrendDto = runTimeReportManager.getAssetPeriodTrendDto(param);
                    assetPeriodTrendDto.setUnits("个");
                    break;
                case ASSET_UNNORMAL_TREND:
                    assetPeriodTrendDto = runTimeReportManager.getAssetUnNormalPeriodTrendDto(param);
                    assetPeriodTrendDto.setUnits("个");
                    break;
                default:
                    break;
            }
            assetPeriodTrendDto.setTitle(startTime + "~" + endTime);
        } catch (Exception e) {
            log.error(e.getMessage());
            Reply.fail(e.getMessage());
        }
        return Reply.ok(assetPeriodTrendDto);
    }


    /**
     * 查询运行状态报表趋势图（优化后）
     *
     * @param param
     * @return
     */
    @Override
    public Reply getOptimizeRunTimeReportTrend(RunTimeQueryParam param) {
        List<PeriodTrendDto> periodTrendDtos = new ArrayList<>();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String startTime = "";
            String endTime = "";
            switch (param.getDateType()) {
                case 0:
                    //            今天
                    endTime = format.format(new Date(calendar.getTimeInMillis()));
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                case 1:
                    //            昨天
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    endTime = format.format(new Date(calendar.getTimeInMillis()));

                    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                case 2:
                    //            上周
                    //判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题
                    int dayWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    if (0 == dayWeek) {
                        dayWeek = 7;
                    }
                    calendar.add(Calendar.DATE, -dayWeek + 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    endTime = format.format(new Date(calendar.getTimeInMillis()));
                    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 7);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                case 3:
                    //            月度
                    endTime = format.format(new Date(calendar.getTimeInMillis()));
                    calendar.add(Calendar.MONTH, -1);
                    startTime = format.format(new Date(calendar.getTimeInMillis()));
                    break;
                default:
                    break;

            }

            param.setTrendType(0);
            PeriodTrendDto assetPeriodTrendDto = runTimeReportManager.getAlertTrendDto(param);
            assetPeriodTrendDto.setType(0);
            assetPeriodTrendDto.setTitle(startTime + "~" + endTime);
            assetPeriodTrendDto.setUnits("次");
            periodTrendDtos.add(assetPeriodTrendDto);

            param.setTrendType(1);
            PeriodTrendDto assetPeriodTrendDto2 = runTimeReportManager.getAssetPeriodTrendDto(param);
            assetPeriodTrendDto2.setType(1);
            assetPeriodTrendDto2.setTitle(startTime + "~" + endTime);
            assetPeriodTrendDto2.setUnits("个");
            periodTrendDtos.add(assetPeriodTrendDto2);

            param.setTrendType(2);
            PeriodTrendDto assetPeriodTrendDto3 = runTimeReportManager.getAssetUnNormalPeriodTrendDto(param);
            assetPeriodTrendDto3.setType(2);
            assetPeriodTrendDto3.setTitle(startTime + "~" + endTime);
            assetPeriodTrendDto3.setUnits("个");
            periodTrendDtos.add(assetPeriodTrendDto3);

        } catch (Exception e) {
            log.error(e.getMessage());
            Reply.fail(e.getMessage());
        }
        return Reply.ok(periodTrendDtos);
    }

    @Override
    public Reply doptimize(RunTimeQueryParam param) {

        //获取数据期间
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        //开始时间
        Long startTime = longTimeByType.get(0);
        //结束时间
        Long endTime = longTimeByType.get(1);
        //获取用户ID
        //全资产查询
        Integer id = mwUserCommonService.getAdmin();

        //最终返回数据集合
        List<RunTimeItemValue> listData = new ArrayList<>();
        //查询itemName为 "CPU_UTILIZATION""MEMORY_UTILIZATION""ICMP_LOSS"的数据
        runTimeReportManager.getrunTimeMemory(id, startTime, endTime, param);
        return null;
    }


    @Override
    public List<DiskDto> getDiskTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime) {
        return diskReportManager.getDiskTrends(mwTangibleassetsDTOS, startTime, endTime);
    }


    @Override
    public List<CpuAndMemoryDtos> getCpuAndMemoryTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long
            startTime, Long endTime) {
        return cpuReportManager.getCpuAndMemoryTrends(mwTangibleassetsDTOS, startTime, endTime);
    }

    @Override
    public List<InterfaceReportDtos> getLinks(List<NetWorkLinkDto> netWorkLinkDtos, Long startTime, Long endTime) {
        return linkReportManager.getLinks(netWorkLinkDtos, startTime, endTime);
    }


    @Override
    public Reply editorTime(EditorTimeParam param) {
        try {
            String startTime = param.getPeriod().get(0);
            String endTime = param.getPeriod().get(1);
            int i = startTime.indexOf(":");
            SolarTimeDto solarTimeDto = SolarTimeDto.builder()
                    .startHourTime(Integer.parseInt(startTime.substring(0, i)))
                    .startMinuteTime(Integer.parseInt(startTime.substring(i + 1, startTime.length())))
                    .endHourTime(Integer.parseInt(endTime.substring(0, i)))
                    .endMinuteTime(Integer.parseInt(endTime.substring(i + 1, endTime.length())))
                    .creator(iLoginCacheInfo.getLoginName())
                    .modifier(iLoginCacheInfo.getLoginName())
                    .type(param.getType())
                    .userId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId()).build();

            if (null == param.getId()) {
                mwReportDao.insertTime(solarTimeDto);
            } else {
                mwReportDao.deleteTime(param.getId(), param.getType());
                mwReportDao.insertTime(solarTimeDto);
            }
            logger.info("SOLAR_REPORT_LOG[]report[]报表[]编辑Solar报表自定义时间段[]{}", param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to editorTime with param={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SOLAR_TIME_EDITOR_CODE_306003, ErrorConstant.SOLAR_TIME_EDITOR_MSG_306003);
        }
    }

    @Override
    public Reply selectTime(EditorTimeParam param) {
        try {
            SolarTimeDto solarTimeDto1 = mwReportDao.selectTime(param.getType());
            HashMap<String, Object> map = new HashMap<>();
            if (null != solarTimeDto1) {
                List<String> list = new ArrayList<>();
                String startTime = (solarTimeDto1.getStartHourTime() > 10 ? solarTimeDto1.getStartHourTime() : "0" + solarTimeDto1.getStartHourTime()) + ":" + (solarTimeDto1.getStartMinuteTime() > 10 ? solarTimeDto1.getStartMinuteTime() : "0" + solarTimeDto1.getStartMinuteTime());
                String endTime = (solarTimeDto1.getEndHourTime() > 10 ? solarTimeDto1.getEndHourTime() : "0" + solarTimeDto1.getEndHourTime()) + ":" + (solarTimeDto1.getEndMinuteTime() > 10 ? solarTimeDto1.getEndMinuteTime() : "0" + solarTimeDto1.getEndMinuteTime());
                list.add(startTime);
                list.add(endTime);
                if (null != map) {
                    map.put("id", solarTimeDto1.getId());
                    map.put("list", list);
                }
            }
            return Reply.ok(map);
        } catch (Exception e) {
            log.error("fail to editorTime  cause:{}", e);
            return Reply.fail(ErrorConstant.SOLAR_TIME_SELECT_CODE_306004, ErrorConstant.SOLAR_TIME_SELECT_MSG_306004);
        }
    }

    @Override
    public Reply selectDayType(EditorTimeParam param) {
        try {
            SolarTimeDto solarTimeDto1 = mwReportDao.selectTime(param.getType());
            List<String> list = new ArrayList<>();
            String startTime = "未设置";
            String endTime = "未设置";
            if (null != solarTimeDto1) {
                startTime = (solarTimeDto1.getStartHourTime() > 10 ? solarTimeDto1.getStartHourTime() : "0" + solarTimeDto1.getStartHourTime()) + ":" + (solarTimeDto1.getStartMinuteTime() > 10 ? solarTimeDto1.getStartMinuteTime() : "0" + solarTimeDto1.getStartMinuteTime());
                endTime = (solarTimeDto1.getEndHourTime() > 10 ? solarTimeDto1.getEndHourTime() : "0" + solarTimeDto1.getEndHourTime()) + ":" + (solarTimeDto1.getEndMinuteTime() > 10 ? solarTimeDto1.getEndMinuteTime() : "0" + solarTimeDto1.getEndMinuteTime());
            }
            list.add("全天(24小时)");
            list.add("全天(" + startTime + "-" + endTime + ")");
            list.add("工作日(24小时)");
            list.add("工作日(" + startTime + "-" + endTime + ")");
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to editorTime  cause:{}", e);
            return Reply.fail(ErrorConstant.SOLAR_TIME_SELECT_CODE_306004, ErrorConstant.SOLAR_TIME_SELECT_MSG_306004);
        }
    }


    @SneakyThrows
    @Override
    @Transactional
    public Reply inputTime(EditorTimeParam inputParam) {
        Integer year = Integer.valueOf(inputParam.getExportDate().substring(0, 4));
        Integer mouth = Integer.valueOf(inputParam.getExportDate().substring(4, 6));
        Integer day = Integer.valueOf(inputParam.getExportDate().substring(6, 8));

        String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, year, mouth - 1, day);
        String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, year, mouth - 1, day);
        String tableName = "";
        if (inputParam.getId() == ReportBase.CPUANDMEMORY.getId()) {
            tableName = "mw_report_cpu_memory_allday";
        } else if (inputParam.getId() == ReportBase.DISK.getId()) {
            tableName = "mw_report_disk_allday";
        } else if (inputParam.getId() == ReportBase.NETWORK.getId()) {
            tableName = "mw_report_network_allday";
        } else if (inputParam.getId() == ReportBase.LINK.getId()) {
            tableName = "mw_report_link_allday";
        }
        int timeDateCount = mwReportDao.getReportTimeDateCount(tableName, allDayStartTime, allDayEndTime);

        if (timeDateCount == 0) {//当天的数据还没有从zabbix的数据库里面计算存入monitor的数据库
            QueryTangAssetsParam query = new QueryTangAssetsParam();
            // List<Integer> orgIds = mwUserOrgMapperDao.getAllOrgIdByUserId("admin");
            //query.setOrgIds(orgIds);
            query.setIsAdmin(true);
            Map pubCriteria = PropertyUtils.describe(query);
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = mwTangibleAssetsDao.selectPubList(pubCriteria);
            Long startFrom = MWUtils.getDate(allDayStartTime, MWDateConstant.NORM_DATETIME);
            Long endTill = MWUtils.getDate(allDayEndTime, MWDateConstant.NORM_DATETIME);
            Date dateTime = MWUtils.strToDateLong(allDayStartTime);


            Integer reportId = inputParam.getId();
            ReportBase reportBase = reportMap.get(reportId);
            switch (reportBase) {
                case CPUANDMEMORY:
                    cpuReportManager.inputCpuAndMemory(mwTangibleassetsDTOS, startFrom, endTill, dateTime);
                    break;
                case DISK:
                    diskReportManager.inputDisk(year, mouth, day, mwTangibleassetsDTOS, startFrom, endTill, dateTime);
                    break;
                case NETWORK:
                    netWorkReportManager.inputNetWork(mwTangibleassetsDTOS, startFrom, endTill, dateTime);
                    break;
                case LINK:
                    linkReportManager.inputLink(startFrom, endTill, dateTime);
                    break;
                default:
                    break;
            }
        }
        return Reply.ok();
    }


    @Override
    public List<CpuAndMemoryDto> getCpuAndMemoryTrend(TrendParam trendParam) {
        return cpuReportManager.getCpuAndMemoryTrend(trendParam);
    }

    @Override
    public List<TrendDiskDto> getDiskTrend(TrendParam trendParam) {
        return diskReportManager.getDiskTrend(trendParam);
    }

    @Override
    public List<TrendNetDto> getNetTrend(TrendParam trendParam) {
        return netWorkReportManager.getNetTrend(trendParam);
    }


    private List<HistoryValueDto> getValueData(MWZabbixAPIResult result) {
        List<HistoryValueDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(HistoryValueDto.builder().value(data.get("value").asDouble()).build());
                });
            }

        }
        return list;
    }


    /**
     * 查询运行状态报表TopN数据（优化后）
     *
     * @param param
     * @return
     */
    @Override
    public Reply getRunTimeItemOptimizeUtilization(RunTimeQueryParam param, boolean addNew,boolean cacheTrue) {
        //进行数据缓存
        param.setTimingType(addNew);

        //获取数据期间
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        //开始时间
        Long startTime = longTimeByType.get(0);
        //结束时间
        Long endTime = longTimeByType.get(1);

        Integer datesize = param.getDataSize();

        //获取全数据 用于做缓存
        param.setDataSize(1000000);
        //获取用户ID

        Integer userId = mwUserCommonService.getAdmin();

        Map<String, List<RunTimeItemValue>> AlltopN = new HashMap<>();

        if (addNew) {
            List<RunTimeItemValue> runTimeItemValues = new ArrayList<>();
            Map<String, List<RunTimeItemValue>> list = runTimeReportManager.getrunTimeMemory(0, startTime, endTime, param);
            Map<String, List<RunTimeItemValue>> interFaceAndDisk = getInterFaceAndDisk(param);
            list.putAll(interFaceAndDisk);
            for (String s : list.keySet()) {
                try {
                    runTimeItemValues.addAll(list.get(s));
                } catch (Exception e) {

                }
            }
            if (runTimeItemValues.size() > 0) {
                //设置更新结果及存储时间
                runTimeItemValues.forEach(item->{
                    item.setSaveTime(new Date(endTime * 1000l));
                    item.setUpdateSuccess(true);
                });
                terraceManageDao.saveRunStateReportDaily(runTimeItemValues);
            } else {
                RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                runTimeItemValue.setSaveTime(new Date(endTime * 1000l));
                runTimeItemValue.setUpdateSuccess(false);
                runTimeItemValues.add(runTimeItemValue);
                terraceManageDao.saveRunStateReportDaily(runTimeItemValues);
            }
            return Reply.ok(AlltopN);
        }
        //缓存定时任务
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);

        if (!cacheTrue){
            if(param.getDateType() == null || param.getDateType() != 0 || !CollectionUtils.isEmpty(param.getChooseTime())){
                //根据时间查询缓存数据
                Map<String, List<RunTimeItemValue>> dataBaseCache = getDataBaseCache(param);
                if(dataBaseCache != null){
                    AlltopN.putAll(dataBaseCache);
                }
            }else{
                Map<String, List<RunTimeItemValue>> list = runTimeReportManager.getrunTimeMemory(userId, startTime, endTime, param);
                AlltopN.putAll(list);
                //按照名称
                //获取接口磁盘利用率数据
                Map<String, List<RunTimeItemValue>> interFaceAndDisk = getInterFaceAndDisk(param);
                AlltopN.putAll(interFaceAndDisk);
                if (param.getDateType()==0){
                    redisUtils.set("mw_report_time_status",AlltopN);
                    List<RunTimeItemValue> runTimeItemValues = AlltopN.get("INTERFACE_IN_UTILIZATION");
                    List<Map<String,Object>> listMap = new ArrayList<>();
                    if(!CollectionUtils.isEmpty(runTimeItemValues)){
                        for (RunTimeItemValue runTimeItemValue : runTimeItemValues) {
                            Map<String,Object> map = new HashMap<>();
                            map.put("assetsId",runTimeItemValue.getAssetsId());
                            map.put("interfaceName",runTimeItemValue.getInterfaceName());
                            map.put("avgValue",runTimeItemValue.getAvgValue());
                            map.put("outInterfaceAvgValue",runTimeItemValue.getOutInterfaceAvgValue());
                            listMap.add(map);
                        }
                        redisUtils.set("mw_new_screen_intedace",listMap);
                    }
                }
            }
        }else{
            if ((Map<? extends String, ? extends List<RunTimeItemValue>>) redisUtils.get("mw_report_time_status")!=null){
                AlltopN.putAll((Map<? extends String, ? extends List<RunTimeItemValue>>) redisUtils.get("mw_report_time_status"));
            }

        }
        //做结果缓存
        List<RunTimeItemValue> listassets = new ArrayList<>();
        RunTimeQueryParam params = new RunTimeQueryParam();
        params.setPageNumber(1);
        params.setPageSize(param.getDataSize());
        params.setDateType(chage(param.getDateType()));
        params.setChooseTime(param.getChooseTime());
        Reply reply = terraceManageService.selectReportAssetsUsability(params, false);
        PageInfo data = (PageInfo) reply.getData();
        Map<String, List<RunTimeItemValue>> reportAssetsUsability = new HashMap<>();
        if(data != null){
            List<MwAssetsUsabilityParam> kill = data.getList();
            if (kill != null && kill.size() > 0) {
                for (MwAssetsUsabilityParam m : kill) {
                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                    runTimeItemValue.setAssetName(m.getAssetsName());
                    runTimeItemValue.setAssetUtilization(m.getAssetsUsability());
                    runTimeItemValue.setIp(m.getIp());
                    listassets.add(runTimeItemValue);
                }
            }
            //运行状态报表
            reportAssetsUsability.put("MW_REPORT_ASSETS", listassets);
        }
        AlltopN.putAll(reportAssetsUsability);




        //获取资产可用率
//        Map<String, List<RunTimeItemValue>> assetsUsability = getAssetsUsability(param);
//        AlltopN.putAll(assetsUsability);


        //全部过滤数据
        for (String key : AlltopN.keySet()) {
            List<RunTimeItemValue> runTimeItemValuess = AlltopN.get(key);
            List<RunTimeItemValue> runTimeItemValues = new ArrayList<>();
            if (param.getSearchName() != null && !param.getSearchName().trim().equals("")) {
                for (RunTimeItemValue r : runTimeItemValuess) {
                    if (r.getAssetName().contains(param.getSearchName())) {
                        runTimeItemValues.add(r);
                    }
                }
            } else {
                runTimeItemValues.addAll(runTimeItemValuess);
            }
            if (runTimeItemValues.size() > datesize) {
                runTimeItemValues = runTimeItemValues.subList(0, datesize);
            }
            AlltopN.put(key, runTimeItemValues);
        }





        return Reply.ok(AlltopN);
    }


    private Map<String,List<RunTimeItemValue>> getDataBaseCache(RunTimeQueryParam param){
        //根据时间类型查询数据库数据
        Integer dateType = param.getDateType();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<RunTimeItemValue> realData = new ArrayList<>();
        if(dateType != null && dateType == 1){//昨日数据
            List<Date> yesterday = ReportDateUtil.getYesterday();
            List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateDailyData(yesterday.get(0), yesterday.get(1));
            realData = MWReportHandlerDataLogic.handleRunStateReportData(runTimeItemValues);
        }
        if(dateType != null && dateType == 2){//上周数据
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateWeeklyData(format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1)));
            realData = MWReportHandlerDataLogic.handleRunStateReportData(runTimeItemValues);
        }
        if(!CollectionUtils.isEmpty(param.getChooseTime())){//自定义i时间
            List<String> chooseTime = param.getChooseTime();
            try {
                List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateDailyData(format.parse(chooseTime.get(0).substring(0,10)),format.parse(chooseTime.get(1).substring(0,10)));
                realData = MWReportHandlerDataLogic.handleRunStateReportData(runTimeItemValues);
            }catch (Exception e){
                log.error("运行状态报表时间转换异常");
            }

        }
        if(CollectionUtils.isEmpty(realData)){
            return null;
        }
        //进行名称分组排序
        Map<String,List<RunTimeItemValue>> realDataMap = new HashMap<>();
        realData.forEach(item->{
            String itemName = item.getItemName();
            if(realDataMap.containsKey(itemName)){
                List<RunTimeItemValue> valueList = realDataMap.get(itemName);
                valueList.add(item);
                realDataMap.put(itemName,valueList);
            }else{
                List<RunTimeItemValue> valueList = new ArrayList<>();
                valueList.add(item);
                realDataMap.put(itemName,valueList);
            }
        });
        if(realDataMap.isEmpty()){
            return realDataMap;
        }
        Integer dataSize = param.getDataSize();
        for (String key : realDataMap.keySet()) {
            List<RunTimeItemValue> runTimeItemValues = realDataMap.get(key);
            List<RunTimeItemValue> itemValueList = new ArrayList<>();
            if(dataSize != null && runTimeItemValues.size() > dataSize){
                itemValueList = runTimeItemValues.subList(0, dataSize);
            }else{
                itemValueList = runTimeItemValues;
            }
            if(CollectionUtils.isEmpty(itemValueList)){
                realDataMap.put(key,itemValueList);
                continue;
            }
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            List<RunTimeItemValue> dataList = itemValueList.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getAvgValue()), pinyin4jUtil.getStringPinYin(o2.getAvgValue()))).collect(Collectors.toList());
            realDataMap.put(key,itemValueList);
        }
        return realDataMap;
    }


    /**
     * 查询接口磁盘数据
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, List<RunTimeItemValue>> getInterFaceAndDisk(RunTimeQueryParam param) {
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        Long startTime = longTimeByType.get(0);
        Long endTime = longTimeByType.get(1);
        Boolean timingType = param.getTimingType();
        //全资产查询
        Integer adminId = mwUserCommonService.getAdmin();
        List<RunTimeItemValue> list = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        strings.add("DISK_UTILIZATION");
        strings.add("INTERFACE_IN_UTILIZATION");
        Map<String, List<RunTimeItemValue>> topNMap = new HashMap<>();
        for (String s : strings) {
            List<RunTimeItemValue> value = new ArrayList<>();
            if (param.getDateType() != 0 && !param.getTimingType()) {
                if (s.equals("DISK_UTILIZATION")) {
                    value = getOldData(s, new Date(startTime * 1000l), new Date(endTime * 1000l));
                } else {
                    value = getOldInData(s, new Date(startTime * 1000l), new Date(endTime * 1000l));
                }

            } else {
                value = runTimeReportManager.getrunTimeMemory(adminId, s, startTime, endTime, param.getDateType());
            }

            if (null != value && value.size() > 0) {
                value = value.stream().filter(f -> StringUtils.isNotEmpty(f.getAssetName())).collect(Collectors.toList());
                Collections.sort(value, new RunTimeItemValue());
                if (value.size() > param.getDataSize()) {
                    value = value.subList(0, param.getDataSize());
                }
            }
            topNMap.put(s, value);
        }

        return topNMap;
    }

    private List<RunTimeItemValue> getOldInData(String s, Date startTime, Date endTime) {
        List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateNameDailyData(s, startTime, endTime);
        List<RunTimeItemValue> runTimeItemValueList = new ArrayList<>();
        for (int i = 0; i < runTimeItemValues.size(); i++) {
            Boolean add = true;
            for (int j = 0; j < runTimeItemValueList.size(); j++) {
                if (runTimeItemValues.get(i).getAssetsId().equals(runTimeItemValueList.get(j).getAssetsId()) && runTimeItemValues.get(i).getInterfaceName().equals(runTimeItemValueList.get(j).getInterfaceName())) {
                    try {
                        String outInterfaceAvgValue = String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getOutInterfaceAvgValue()) + Double.parseDouble(runTimeItemValues.get(i).getOutInterfaceAvgValue())) / 2).setScale(2, BigDecimal.ROUND_HALF_UP));
                        String avg = String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getAvgValue()) + Double.parseDouble(runTimeItemValues.get(i).getAvgValue())) / 2).setScale(2, BigDecimal.ROUND_HALF_UP));
                        runTimeItemValueList.get(j).setAvgValue(avg);
                        runTimeItemValueList.get(j).setOutInterfaceAvgValue(outInterfaceAvgValue);
                    } catch (Exception e) {
                    }
                    add = false;
                }
            }
            if (add) {
                runTimeItemValueList.add(runTimeItemValues.get(i));
            }
        }
        return runTimeItemValueList;
    }

    private List<RunTimeItemValue> getOldData(String s, Date startTime, Date endTime) {
        List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateNameDailyData(s, startTime, endTime);
        List<RunTimeItemValue> runTimeItemValueList = new ArrayList<>();
        for (int i = 0; i < runTimeItemValues.size(); i++) {
            Boolean add = true;
            for (int j = 0; j < runTimeItemValueList.size(); j++) {
                if (runTimeItemValues.get(i).getAssetsId().equals(runTimeItemValueList.get(j).getAssetsId()) && runTimeItemValues.get(i).getDiskName().equals(runTimeItemValueList.get(j).getDiskName())) {
                    try {
                        String avg = String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getAvgValue()) + Double.parseDouble(runTimeItemValues.get(i).getAvgValue())) / 2).setScale(2, BigDecimal.ROUND_HALF_UP));
                        runTimeItemValueList.get(j).setAvgValue(avg);
                    } catch (Exception e) {
                    }
                    add = false;
                }
            }
            if (add) {
                runTimeItemValueList.add(runTimeItemValues.get(i));
            }
        }
        return runTimeItemValueList;
    }


    /**
     * 查询资产可用性
     *
     * @param param
     * @return
     */
    private Map<String, List<RunTimeItemValue>> getAssetsUsability(RunTimeQueryParam param) {
        List<Long> longTimeByType = runTimeReportManager.getLongTimeByType(param);
        Long startTime = longTimeByType.get(0);
        Long endTime = longTimeByType.get(1);
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        List<RunTimeItemValue> list = null;
        list = runTimeReportManager.getrunTimeMemory3(userId, startTime, endTime);
        if (null != list && list.size() > 0) {
            list = list.stream().filter(f -> StringUtils.isNotEmpty(f.getAssetName())).collect(Collectors.toList());
            Collections.sort(list, (o1, o2) -> {
                return Double.compare(Double.parseDouble(o1.getAssetUtilization()), Double.valueOf(o2.getAssetUtilization()));
            });
            if (list.size() > param.getDataSize()) {
                list = list.subList(0, param.getDataSize());
            }
            //设置类型
            list.forEach(v -> {
                v.setType(0);
            });
        }
        //该类型没有itemName,默认为未知
        Map<String, List<RunTimeItemValue>> topNMap = new HashMap<>();
        topNMap.put("unknown", list);
        return topNMap;
    }

}
