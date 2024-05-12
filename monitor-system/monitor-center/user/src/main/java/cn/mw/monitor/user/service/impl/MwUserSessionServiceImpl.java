package cn.mw.monitor.user.service.impl;


import cn.mw.monitor.api.param.user.ExportUserOnlineParam;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.user.dao.MwUserSessionMapper;
import cn.mw.monitor.user.model.MwUserSession;
import cn.mw.monitor.user.model.UserSessionDTO;
import cn.mw.monitor.user.service.MwUserSessionService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author swy
 * @since 2023-08-08
 */
@Service
@Slf4j
public class MwUserSessionServiceImpl extends ServiceImpl<MwUserSessionMapper, MwUserSession> implements MwUserSessionService {


    @Override
    public Integer saveUserSession(LoginInfo loginInfo) {
        String orgIds = loginInfo.getOrgs() != null ? loginInfo.getOrgs().stream().map(org -> String.valueOf(org.getOrgId())).collect(Collectors.joining(",")) : null;
        String orgName = loginInfo.getOrgs() != null ? loginInfo.getOrgs().stream().map(MWOrg::getOrgName).collect(Collectors.joining(",")) : null;
        Date now = new Date();
        MwUserSession userSession = new MwUserSession();
        userSession.setId(getMaxScriptId());
        userSession.setUserId(loginInfo.getUser().getUserId());
        userSession.setLoginName(loginInfo.getUser().getLoginName());
        userSession.setUserName(loginInfo.getUser().getUserName());
        userSession.setOrgId(orgIds);
        userSession.setOrgName(orgName);
        userSession.setCreateTime(DateUtils.getTimesMorning());
        userSession.setLoginTime(now);
        baseMapper.insert(userSession);
        return userSession.getId();
    }

    @Override
    public void saveLogoutTime(Integer sessionId) {
        Date now = new Date();
        MwUserSession userSession = this.getById(sessionId);
        if (ObjectUtils.isNotEmpty(userSession)) {
            this.lambdaUpdate()
                    .eq(MwUserSession::getId, userSession.getId())
                    .set(MwUserSession::getLogoutTime, now)
                    .set(MwUserSession::getOnlineTime, (now.getTime() - userSession.getLoginTime().getTime()) / 1000)
                    .update();
        }
    }

    @Override
    public void timeOutLogout(Integer sessionId) {
        Date now = new Date();
        log.info("用户超时自动登出={}", sessionId);
        MwUserSession userSession = this.getById(sessionId);
        if (ObjectUtils.isNotEmpty(userSession)) {
            this.lambdaUpdate()
                    .eq(MwUserSession::getId, userSession.getId())
                    .set(MwUserSession::getLogoutTime, now)
                    .set(MwUserSession::getOnlineTime, (now.getTime() - userSession.getLoginTime().getTime()) / 1000)
                    .update();
        }
    }

    @Override
    public void exportUserOnline(ExportUserOnlineParam param, HttpServletResponse response) {
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("userName");
        includeColumnFiledNames.add("orgName");
        includeColumnFiledNames.add("createTime");
        includeColumnFiledNames.add("onlineTime");
        ExcelWriter excelWriter = null;
        String failFileName = "用户在线时长";
        try {
            switch (param.getDateType()) {
                case 1:
                    param.setStartTime(getYesterdayMorning());
                    param.setEndTime(getYesterdayNight());
                    break;
                case 2:
                    param.setStartTime(DateUtils.getTimesMorning());
                    param.setEndTime(DateUtils.getTimesNight());
                    break;
                case 5:
                    param.setStartTime(getLastWeekMorning());
                    param.setEndTime(getLastWeekNight());
                    break;
                case 8:
                    param.setStartTime(getFirstDayOfLastMonth());
                    param.setEndTime(getLastDayOfLastMonth());
                    break;
                case 11:
                    param.setStartTime(param.getChooseTime().get(0));
                    param.setEndTime(param.getChooseTime().get(1));
                    break;
                default:
                    break;
            }

            QueryWrapper<MwUserSession> queryWrapper = new QueryWrapper();
            queryWrapper.lambda()
                    .isNotNull(MwUserSession::getOnlineTime)
                    .like(StringUtils.isNotEmpty(param.getUserName()), MwUserSession::getUserName, param.getUserName())
                    .between(ObjectUtils.isNotEmpty(param.getStartTime()) && ObjectUtils.isNotEmpty(param.getEndTime()), MwUserSession::getCreateTime, param.getStartTime(), param.getEndTime());
            List<MwUserSession> sessions = baseMapper.selectList(queryWrapper);
            List<UserSessionDTO> failList = sessions.stream()
                    .map(session -> {
                        UserSessionDTO dto = new UserSessionDTO();
                        dto.setUserId(session.getUserId());
                        dto.setUserName(session.getUserName());
                        dto.setOrgName(session.getOrgName());
                        dto.setOrgId(session.getOrgId());
                        dto.setCreateTime(DateUtils.format(session.getCreateTime(),DateConstant.NORM_DATE));
                        dto.setTotalOnlineTime(session.getOnlineTime());
                        return dto;
                    })
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(
                                    dto -> Arrays.asList(dto.getUserId(), dto.getCreateTime()), // Key
                                    Function.identity(), // Value
                                    (dto1, dto2) -> {
                                        UserSessionDTO mergedDto = new UserSessionDTO();
                                        mergedDto.setUserId(dto1.getUserId());
                                        mergedDto.setUserName(dto1.getUserName());
                                        mergedDto.setOrgId(dto1.getOrgId());
                                        mergedDto.setOrgName(dto1.getOrgName());
                                        mergedDto.setCreateTime(dto1.getCreateTime());
                                        mergedDto.setTotalOnlineTime(dto1.getTotalOnlineTime()+dto2.getTotalOnlineTime());
                                        return mergedDto;
                                    }
                            ),
                            map -> {
                                List<UserSessionDTO> list = new ArrayList<>(map.values());
                                list.forEach(UserSessionDTO::convertSecondsToTimeStr);
                                return list;
                            }
                    ));
            failList.sort(Comparator.comparing(UserSessionDTO::getCreateTime));
            excelWriter = ExcelUtils.getExcelWriter(failFileName, response, UserSessionDTO.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(failList, sheet);
            log.info("导出成功");
        } catch (IOException e) {
            log.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }


    }

    @Override
    public Reply queryPage(ExportUserOnlineParam param) {
        try {
            switch (param.getDateType()) {
                case 1:
                    param.setStartTime(getYesterdayMorning());
                    param.setEndTime(getYesterdayNight());
                    break;
                case 2:
                    param.setStartTime(DateUtils.getTimesMorning());
                    param.setEndTime(DateUtils.getTimesNight());
                    break;
                case 5:
                    param.setStartTime(getLastWeekMorning());
                    param.setEndTime(getLastWeekNight());
                    break;
                case 8:
                    param.setStartTime(getFirstDayOfLastMonth());
                    param.setEndTime(getLastDayOfLastMonth());
                    break;
                case 11:
                    param.setStartTime(param.getChooseTime().get(0));
                    param.setEndTime(param.getChooseTime().get(1));
                    break;
                default:
                    break;
            }
            QueryWrapper<MwUserSession> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .isNotNull(MwUserSession::getOnlineTime)
                    .like(StringUtils.isNotEmpty(param.getUserName()), MwUserSession::getUserName, param.getUserName())
                    .between(ObjectUtils.isNotEmpty(param.getStartTime()) && ObjectUtils.isNotEmpty(param.getEndTime()), MwUserSession::getCreateTime, param.getStartTime(), param.getEndTime());
            List<MwUserSession> sessions = baseMapper.selectList(queryWrapper);
            List<UserSessionDTO> dtos = sessions.stream()
                    .map(session -> {
                        UserSessionDTO dto = new UserSessionDTO();
                        dto.setUserId(session.getUserId());
                        dto.setUserName(session.getUserName());
                        dto.setOrgName(session.getOrgName());
                        dto.setOrgId(session.getOrgId());
                        dto.setCreateTime(DateUtils.format(session.getCreateTime(),DateConstant.NORM_DATE));
                        dto.setTotalOnlineTime(session.getOnlineTime());
                        return dto;
                    })
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(
                                    dto -> Arrays.asList(dto.getUserId(), dto.getCreateTime()), // Key
                                    Function.identity(), // Value
                                    (dto1, dto2) -> {
                                        UserSessionDTO mergedDto = new UserSessionDTO();
                                        mergedDto.setUserId(dto1.getUserId());
                                        mergedDto.setUserName(dto1.getUserName());
                                        mergedDto.setOrgId(dto1.getOrgId());
                                        mergedDto.setOrgName(dto1.getOrgName());
                                        mergedDto.setCreateTime(dto1.getCreateTime());
                                        mergedDto.setTotalOnlineTime(dto1.getTotalOnlineTime()+dto2.getTotalOnlineTime());
                                        return mergedDto;
                                    }
                            ),
                            map -> {
                                List<UserSessionDTO> list = new ArrayList<>(map.values());
                                list.forEach(UserSessionDTO::convertSecondsToTimeStr);
                                return list;
                            }
                    ));
            dtos.sort(Comparator.comparing(UserSessionDTO::getCreateTime));
            //创建Page类
            Page page = new Page(param.getPageNumber(), param.getPageSize());
            //为Page类中的total属性赋值
            int total = dtos.size();
            page.setTotal(total);
            //计算当前需要显示的数据下标起始值
            int startIndex = (param.getPageNumber() - 1) * param.getPageSize();
            int endIndex = Math.min(startIndex + param.getPageSize(),total);
            //从链表中截取需要显示的子链表，并加入到Page
            page.addAll(dtos.subList(startIndex,endIndex));
            //以Page创建PageInfo
            PageInfo<UserSessionDTO> pageInfo = new PageInfo<>(page);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail queryPage={}", param, e);
            return Reply.fail("查询分页信息失败");
        }
    }

    /**
     * 获得昨天0点时间
     *
     * @return
     */
    public static Date getYesterdayMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获得昨天23:59:59时间
     *
     * @return
     */
    public static Date getYesterdayNight() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获得上周0点时间
     *
     * @return
     */
    public static Date getLastWeekMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.WEEK_OF_YEAR, -1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * 获得上周末23:59:59时间
     *
     * @return
     */
    public static Date getLastWeekNight() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.add(Calendar.DAY_OF_YEAR, 6);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获得上月第一天时间
     *
     * @return
     */
    public static Date getFirstDayOfLastMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获得上月最后一天 23:59:59时间
     *
     * @return
     */
    public static Date getLastDayOfLastMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxScriptId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<MwUserSession> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        MwUserSession userSession = baseMapper.selectOne(wrapper);
        if (userSession != null) {
            maxId += userSession.getId();
        }
        return maxId;
    }
}
