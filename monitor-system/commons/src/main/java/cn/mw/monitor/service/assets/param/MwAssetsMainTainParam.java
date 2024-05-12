package cn.mw.monitor.service.assets.param;

/**
 * @ClassName MwAssetsMainTainParam
 * @Description 猫维资产资产管理模块新增维护功能参数
 * @Author gengjb
 * @Date 2021/7/26 15:19
 * @Version 1.0
 **/

import cn.mw.monitor.bean.BaseParam;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.*;

@Data
public class MwAssetsMainTainParam extends BaseParam {

    private Integer id;

    /**
     * 维护ID
     */
    private Integer maintenanceid;
    /**
     * 维护名称
     */
    @ApiModelProperty("名称")
    private String name;

    /**
     * 启用时间
     */
    @ApiModelProperty("启用自从")
    private Date activeSince;

    /**
     * 结束时间
     */
    @ApiModelProperty("启用直到")
    private Date activeTill;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    /**
     * 维护类型  0：(默认，有数据收集) 1：无数据收集
     */
    @ApiModelProperty("维护类型")
    private Integer maintenanceType;

    @ApiModelProperty("维护周期")
    List<Object> mainTainPeriodParams;

    /**
     * 维护的主机组ID
     */
    @ApiModelProperty("主机组")
    private List<String> groupids;

    /**
     * 主机集合
     */
    @ApiModelProperty("主机")
    private List<MWMainTainHostParam> hostids;

    @ApiModelProperty("时期")
    private Integer period;

    /**
     * 时间段参数列表
     */
    @ApiModelProperty("时间段列表")
    private List<HashMap> times;

    private String creator;

    private String modifier;

    private Date createDate;

    private Date modificationDate;

    /**
     * 过期状态 -1:任何状态  0：启用中 1：接近中 2：已过期
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 应用服务器ID
     */
    private Integer serverId;


    /**
     * 标记数据
     */
    private List<Map<String,Object>> tags;

    private  Integer typeId;

    /**
     * 标记
     */
    private Integer tagsEvalType;


    private String formData;

    /**
     * 周期频率
     */
    private Integer cyclicFrequency;

    /**
     * 是否过期
     */
    private Boolean isExpire;

    public void extractFrom(MwAssetsMainTainParamV1 mwAssetsMainTainParamV1){
        this.name = mwAssetsMainTainParamV1.getName();
        this.description = mwAssetsMainTainParamV1.getDescription();
        this.maintenanceType = mwAssetsMainTainParamV1.getMaintenanceType();
        this.hostids = mwAssetsMainTainParamV1.getHostids();

        if(null == mwAssetsMainTainParamV1.getActiveSince() && null == mwAssetsMainTainParamV1.getActiveTill()){
            this.activeSince = new Date();
            this.activeTill = DateUtils.addYears(this.activeSince ,10);
        }else{
            if(null != mwAssetsMainTainParamV1.getActiveSince()) {
                this.activeSince = mwAssetsMainTainParamV1.getActiveSince();
            }
            if(null != mwAssetsMainTainParamV1.getActiveTill()){
                this.activeTill = mwAssetsMainTainParamV1.getActiveTill();
            }
        }

        mwAssetsMainTainParamV1.setActiveSince(this.activeSince);
        mwAssetsMainTainParamV1.setActiveTill(this.activeTill);

        this.mainTainPeriodParams = new ArrayList<>();
        MwAssetsMainTainV1Once once = mwAssetsMainTainParamV1.getOnceParam();
        MwAssetsMainTainV1Adapter adapter = new MwAssetsMainTainV1Adapter();
        if(null != once){
            List<Date> dates = new ArrayList<>();
            //找到维护周期最终结束时间
            for(MwAssetsMainTainV1Period v1Period : mwAssetsMainTainParamV1.getPeriods()){
                once.setStartDay(v1Period.getStart());
                dates.add(DateUtils.parse(v1Period.getStart(), "yyyy-MM-dd HH:mm"));
                dates.add(DateUtils.parse(v1Period.getEnd(), "yyyy-MM-dd HH:mm"));
                MwAssetsMainTainOnceParam mwAssetsMainTainOnceParam = new MwAssetsMainTainOnceParam();
                adapter.apply(mwAssetsMainTainOnceParam ,once ,v1Period);
                v1Period.setStart(once.getStartDay());
                once.setStartDay(null);
                this.mainTainPeriodParams.add(mwAssetsMainTainOnceParam);
            }
            //根据存储时间集合取最大最小时间，最小时间为开始时间，最大时间为结束时间
            Date maxDate = dates.stream().max(Date::compareTo).get();
            Date minDate = dates.stream().min(Date::compareTo).get();
            this.activeSince = minDate;
            this.activeTill = maxDate;
            mwAssetsMainTainParamV1.setActiveSince(this.activeSince);
            mwAssetsMainTainParamV1.setActiveTill(this.activeTill);
        }

        MwAssetsMainTainV1Day day = mwAssetsMainTainParamV1.getDayParam();
        if(null != day){
            this.activeSince = day.getStartDay();
            mwAssetsMainTainParamV1.setActiveSince(this.activeSince);
            for(MwAssetsMainTainV1Period v1Period : mwAssetsMainTainParamV1.getPeriods()){
                MwAssetsMainTainDayParam mwAssetsMainTainDayParam = new MwAssetsMainTainDayParam();
                adapter.apply(mwAssetsMainTainDayParam ,day ,v1Period);
                this.mainTainPeriodParams.add(mwAssetsMainTainDayParam);
            }
        }

        MwAssetsMainTainV1Week week = mwAssetsMainTainParamV1.getWeekParam();
        if(null != week){
            for(MwAssetsMainTainV1Period v1Period : mwAssetsMainTainParamV1.getPeriods()){
                MwAssetsMainTainWeekParam mwAssetsMainTainWeekParam = new MwAssetsMainTainWeekParam();
                adapter.apply(mwAssetsMainTainWeekParam ,week ,v1Period);
                this.mainTainPeriodParams.add(mwAssetsMainTainWeekParam);
            }
        }

        MwAssetsMainTainV1Month month = mwAssetsMainTainParamV1.getMonthParam();
        if(null != month){
            if(null != month.getDayIndexes()){
                for(Integer dayIndex : month.getDayIndexes()){
                    for(MwAssetsMainTainV1Period v1Period : mwAssetsMainTainParamV1.getPeriods()){
                        MwAssetsMainTainMonthParam mwAssetsMainTainMonthParam = new MwAssetsMainTainMonthParam();
                        mwAssetsMainTainMonthParam.setDayPerMonth(dayIndex);
                        adapter.apply(mwAssetsMainTainMonthParam ,month ,v1Period);
                        this.mainTainPeriodParams.add(mwAssetsMainTainMonthParam);
                    }
                }
            }

            if(null != month.getWeekIndexes()){
                for(MwAssetsMainTainV1Period v1Period : mwAssetsMainTainParamV1.getPeriods()){
                    for(Integer weekIndexPerMonth : month.getWeekIndexPerMonth()){
                        MwAssetsMainTainMonthParam mwAssetsMainTainMonthParam = new MwAssetsMainTainMonthParam();
                        mwAssetsMainTainMonthParam.setWeekIndexPerMonth(weekIndexPerMonth);
                        adapter.apply(mwAssetsMainTainMonthParam ,month ,v1Period);
                        this.mainTainPeriodParams.add(mwAssetsMainTainMonthParam);
                    }

                }
            }
        }
    }
}
