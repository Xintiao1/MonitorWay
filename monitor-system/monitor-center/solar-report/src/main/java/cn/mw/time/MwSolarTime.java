package cn.mw.time;

import cn.mw.module.solarwind.dao.MWSolarReportDao;
import cn.mw.module.solarwind.dto.SolarDetailDto;
import cn.mw.module.solarwind.dto.SolarReportDto;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.SolarTimeDto;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.solarwind.dao.MwMonitorSolarReportDao;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * @author xhy
 * @date 2020/6/29 14:14
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "solarwind", name = "task-enable", havingValue = "true")
@EnableScheduling
public class MwSolarTime implements InitializingBean {

    @Resource
    MWSolarReportDao mwSolarReportDao;
    @Resource
    MwMonitorSolarReportDao mwMonitorSolarReportDao;
    @Resource
    private MwReportDao mwReportDao;

    @Value("${solarwind.datesource.name}")
    private String dateSourceName;

    @Value("${solarwind.report.initializeDay}")
    private int initializeDay;

    /**
     * 分别存入到对应的四张表中
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveSolarDayDetail() {
        try {
            log.info(">>>>>>>saveSolarDayDetail>>>>>start>>>>>");
            String tableName = dateSourceName + ".dbo.InterfaceTraffic_Detail_" + MWUtils.getSolarData(0, 0, 0, MWDateConstant.MOUTH_DATE, 0);
            log.info("MwSolarTime{} saveSolarDayDetail() tableName::"+tableName);
            //SolarReportDto solarReportDto = SolarReportDto.builder().tableName(tableName).build();
            String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, -1);
            String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, -1);
            log.info("MwSolarTime{} saveSolarDayDetail() allDayStartTime::"+allDayStartTime+":::allDayEndTime::"+allDayEndTime);
            SolarReportDto solarReportDto = SolarReportDto.builder().tableName(tableName).dateTime(MWUtils.strToDateLong(allDayStartTime)).build();
            solarReportDto.setStartTime(allDayStartTime);
            solarReportDto.setEndTime(allDayEndTime);
            log.info("MwSolarTime{} saveSolarDayDetail() solarReportDto::"+solarReportDto);

            List<SolarDetailDto> allDay = mwSolarReportDao.selectInterfaceDetail(solarReportDto);

            log.info("MwSolarTime{} saveSolarDayDetail() allDay::"+allDay);

            SolarTimeDto solarTimeDto = mwReportDao.selectTime(0);
            String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);

            log.info("MwSolarTime{} saveSolarDayDetail() workStartTime::"+workStartTime+":::workEndTime::"+workEndTime);

            solarReportDto.setStartTime(workStartTime);
            solarReportDto.setEndTimeDay(workEndTime);

            log.info("MwSolarTime{} saveSolarDayDetail() solarReportDto22::"+solarReportDto);
            List<SolarDetailDto> allDayWorkTime = mwSolarReportDao.selectInterfaceDetail(solarReportDto);
            log.info("MwSolarTime{} saveSolarDayDetail() allDayWorkTime::"+allDayWorkTime);
            String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//三点保存的是昨天的数据 所有用-1
            log.info("MwSolarTime{} saveSolarDayDetail() solarData::"+solarData);
            int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);
            log.info("MwSolarTime{} saveSolarDayDetail() count::"+count);
            if (count == 0) {
                /**
                 * 工作日
                 * 0-24小时的数据
                 * 807（自定义时间段）点的数据
                 */
                mwMonitorSolarReportDao.insertWorkDay(allDay);
                mwMonitorSolarReportDao.insertWorkDayWorkTIme(allDayWorkTime);
            }
            /**
             * 全天
             * 0-24小时的数据
             * 807（自定义时间段）点的数据
             */
            mwMonitorSolarReportDao.insertAllday(allDay);
            mwMonitorSolarReportDao.insertAlldayWorkTime(allDayWorkTime);
            log.info("MwSolarTime{} saveSolarDayDetail() end::"+new Date());
        }catch (Throwable e){
            log.error("MwSolarTime{} saveSolarDayDetail() error::",e);
        }
        log.info(">>>>>>>saveSolarDayDetail>>>>end>>>>>>");
    }



    //    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveSolarInitializeDay() {
        try {
            Long  time = toDayStartTime();
            for (int i = 0; i < initializeDay; i++){
                log.info(">>>>>>>saveSolarInitializeDay>>>>>start>>>>>");
                String allDayStartTime = DateUtils.getFormatDate(time - (86400000l*(i+1)));
                String tableName = dateSourceName + ".dbo.InterfaceTraffic_Detail_" + allDayStartTime.substring(0,10);
                log.info("MwSolarTime{} saveSolarInitializeDay() tableName::"+tableName);
                String allDayEndTime = DateUtils.getFormatDate(time - 1000 - (86400000l*i));
                log.info("MwSolarTime{} saveSolarInitializeDay() allDayStartTime::"+allDayStartTime+":::allDayEndTime::"+allDayEndTime);
                SolarReportDto solarReportDto = SolarReportDto.builder().tableName(tableName).dateTime(MWUtils.strToDateLong(allDayStartTime)).build();
                solarReportDto.setStartTime(allDayStartTime);
                solarReportDto.setEndTime(allDayEndTime);
                log.info("MwSolarTime{} saveSolarInitializeDay() solarReportDto::"+solarReportDto);
                List<SolarDetailDto> allDay = mwSolarReportDao.selectInterfaceDetail(solarReportDto);
                log.info("MwSolarTime{} saveSolarInitializeDay() allDay::"+allDay);
                String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//三点保存的是昨天的数据 所有用-1
                log.info("MwSolarTime{} saveSolarInitializeDay() solarData::"+solarData);
                int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);
                log.info("MwSolarTime{} saveSolarInitializeDay() count::"+count);
                if (count == 0) {
                    /**
                     * 工作日
                     * 0-24小时的数据
                     * 807（自定义时间段）点的数据
                     */
                    mwMonitorSolarReportDao.insertWorkDay(allDay);
                    mwMonitorSolarReportDao.insertWorkDayWorkTIme(allDay);
                }
                /**
                 * 全天
                 * 0-24小时的数据
                 * 807（自定义时间段）点的数据
                 */
                mwMonitorSolarReportDao.insertAllday(allDay);
                mwMonitorSolarReportDao.insertAlldayWorkTime(allDay);
                log.info("MwSolarTime{} saveSolarDayDetail() end::"+new Date());
            }
        }catch (Throwable e){
            log.error("MwSolarTime{} saveSolarDayDetail() error::",e);
        }
        log.info(">>>>>>>saveSolarDayDetail>>>>end>>>>>>");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(">>>>>>>MwSolarTime start afterPropertiesSet saveSolarDayDetail>>>>>>>>>>");
    }

    //返回今天零时零点零分
    private static Long toDayStartTime(){
        Calendar currDate = new GregorianCalendar();
        currDate.set(Calendar.HOUR_OF_DAY,0);
        currDate.set(Calendar.MINUTE,0);
        currDate.set(Calendar.SECOND,0);
        Date time = currDate.getTime();
        return  time.getTime();
    }
}
