package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.AssetsDto;
import cn.mw.monitor.report.dto.linkdto.ExportLinkParam;
import cn.mw.monitor.report.dto.linkdto.InterfaceReportDtos;
import cn.mw.monitor.report.dto.linkdto.LinkHistoryDto;
import cn.mw.monitor.report.param.EditorTimeParam;
import cn.mw.monitor.report.param.ExcelReportParam;
import cn.mw.monitor.report.param.ReportBase;
import cn.mw.monitor.report.param.ReportCountParam;
import cn.mw.monitor.report.service.MwSyReportService;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.report.service.manager.*;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.MWUtils;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MwSyReportServiceImpl implements MwSyReportService {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/report");

    private static Map<Integer, ReportBase> reportMap = new HashMap<>();

    static {
        ReportBase[] values = ReportBase.values();
        for (ReportBase reportBase : values) {
            reportMap.put(reportBase.getId(), reportBase);
        }
    }

    @Resource
    private MwReportDao mwReportDao;

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
    CpuReportSyManage cpuReportSyManage;
    @Autowired
    AssetsReportManager assetsReportManager;
    @Autowired
    LinkReportManager linkReportManager;
    @Autowired
    DiskReportManager diskReportManager;
    @Autowired
    DiskReportSyManage diskReportSyManage;
    @Autowired
    NetWorkReportManager netWorkReportManager;
    @Autowired
    NetWorkReportSyManager netWorkReportSyManager;
    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

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

            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);

            logger.info("REPORT_LOG[]report[]report报表[]报表查询信息[]{}[]", queryReportParam);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectReport with mtaDTO={}, cause:{}", queryReportParam, e);
            return Reply.fail(ErrorConstant.REPORT_SELECT_CODE_303009, ErrorConstant.REPORT_SELECT_MSG_303009);
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
     * 根据资产类型查询下属资产
     *
     * @param assetsTypeId
     * @return
     */
    @Override
    public Reply getSelectColumnByType(int assetsTypeId) {
        QueryTangAssetsParam queryTangAssetsParam = new QueryTangAssetsParam();
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        List<String> columnList = new ArrayList<>();
        queryTangAssetsParam.setAssetsTypeId(assetsTypeId);
        //查询全部不分页
        queryTangAssetsParam.setPageNumber(-1);
        queryTangAssetsParam.setPageSize(0);
        Reply reply = mwTangibleAssetsService.selectList(queryTangAssetsParam);
        if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
            return Reply.fail(reply.getMsg());
        }
        PageInfo pageInfoList = (PageInfo) reply.getData();
        mwTangibleassetsDTOS = pageInfoList.getList();
        if (mwTangibleassetsDTOS.size() > 0) {
            for (MwTangibleassetsTable item : mwTangibleassetsDTOS) {
                columnList.add(item.getInBandIp() + "[" + item.getMonitorServerName() + "]" + "[" + item.getMonitorServerId() + "]");
            }
        }
        return Reply.ok(columnList);
    }

    /**
     * 查询出报表对应的资产数据
     *
     * @param trendParam
     * @return
     */

    public PageInfo getMwTangibleassets(TrendParam trendParam) {
        String initial = trendParam.getAssertIp();
        String ip = initial.substring(0, initial.indexOf("["));
        int mid = Integer.parseInt(initial.substring(initial.lastIndexOf("[") + 1, initial.lastIndexOf("]")));
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        PageHelper.startPage(trendParam.getPageNumber(), trendParam.getPageSize());
        QueryTangAssetsParam query = new QueryTangAssetsParam();
        query.setPageNumber(trendParam.getPageNumber());
        query.setPageSize(trendParam.getPageSize());
        query.setAssetsTypeId(trendParam.getAssetsTypeId());
        query.setInBandIp(ip);
        query.setMonitorServerId(mid);
        query.setManufacturer(trendParam.getManufacturer());
        query.setAssetsName(trendParam.getAssetsName());
        query.setAssetsTypeName(trendParam.getAssetsTypeName());
        query.setModificationDateStart(trendParam.getModificationDateStart());
        query.setModificationDateEnd(trendParam.getModificationDateEnd());
        query.setSpecifications(trendParam.getSpecifications());
        query.setAssetsTypeSubName(trendParam.getAssetsTypeSubName());
        query.setMonitorFlag(trendParam.getMonitorFlag());
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
        trendParam.setParticle(uParam.getParticle());
        trendParam.setSeniorchecked(uParam.getSeniorchecked());
        trendParam.setAssetsTypeId(uParam.getAssetsTypeId());
        trendParam.setAssertIp(uParam.getAssertIp());
        switch (reportBase) {
            case CPUANDMEMORY:
                dtoclass = CpuAndMemorySyDto.class;
                pa = getMwTangibleassets(trendParam);
                list = getCpuAndMemoryTrend(trendParam);
//                list = uParam.getCpulistsy();
                break;
            case DISK:
                dtoclass = TrendDiskSyDto.class;
                pa = getMwTangibleassets(trendParam);
                list = getDiskTrend(trendParam);
//                list = uParam.getDisklistsy();
                break;
            case NETWORK:
                dtoclass = TrendNetSyDto.class;
                pa = getMwTangibleassets(trendParam);
                list = getNetTrend(trendParam);
//                list = uParam.getNetlistsy();
                break;
            case ASSETS_COLLECTION:
                dtoclass = AssetsDto.class;
                list = uParam.getAssetsDtoList();
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
    public List<CpuAndMemorySyDto> getCpuAndMemoryTrend(TrendParam trendParam) {
        return cpuReportSyManage.getCpuAndMemoryTrend(trendParam);
    }

    @Override
    public List<TrendDiskSyDto> getDiskTrend(TrendParam trendParam) {
        return diskReportSyManage.getDiskTrend(trendParam);
    }

    @Override
    public List<TrendNetSyDto> getNetTrend(TrendParam trendParam) {
        return netWorkReportSyManager.getNetTrend(trendParam);
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

}
