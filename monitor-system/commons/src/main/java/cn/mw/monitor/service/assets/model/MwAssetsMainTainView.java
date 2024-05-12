package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.util.GzipTool;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.*;

@Data
public class MwAssetsMainTainView {

    private static Map<Integer ,String> weekMap;
    private static Map<Integer ,String> monthMap;
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

    /**
     * 维护的主机组ID
     */
    @ApiModelProperty("主机组")
    private List<String> groupids;

    /**
     * 主机集合
     */
    @ApiModelProperty("主机")
    private List<MWMainTainHostView> hostids;

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

    /**
     * 周期频率
     */
    private Integer cyclicFrequency;

    /**
     * 维护期间
     */
    private List<MwAssetsMainTainV1Period> periods;

    private String taskDescription;

    static {
        weekMap = new HashMap<>();
        weekMap.put(1 ,"周一");
        weekMap.put(2 ,"周二");
        weekMap.put(3 ,"周三");
        weekMap.put(4 ,"周四");
        weekMap.put(5 ,"周五");
        weekMap.put(6 ,"周六");
        weekMap.put(7 ,"周天");
    }

    public void extractFrom(MwAssetsMainTainParam param ,MwAssetsMainTainParamV1 mwAssetsMainTainParamV1){
        this.id = param.getId();
        this.name = param.getName();
        this.maintenanceid = param.getMaintenanceid();
        this.description = param.getDescription();
        this.maintenanceType = param.getMaintenanceType();
        this.activeSince = param.getActiveSince();
        this.activeTill = param.getActiveTill();
        this.groupids = param.getGroupids();
        this.hostids = new ArrayList<>();
        if(null != param.getHostids()){
            for(MWMainTainHostParam mwMainTainHostParam : param.getHostids()){
                MWMainTainHostView view = new MWMainTainHostView();
                BeansUtils.copyProperties(mwMainTainHostParam ,view);
            }
        }

        this.status = param.getStatus();
        this.serverId = param.getServerId();
        this.creator = param.getCreator();
        this.createDate = param.getCreateDate();

        if(null != mwAssetsMainTainParamV1) {
            StringBuilder sb = new StringBuilder();
            if (null != mwAssetsMainTainParamV1.getOnceParam()) {
                List<String> onceDates = mwAssetsMainTainParamV1.getOnceParam().getOnceDates();
                if(CollectionUtils.isEmpty(onceDates)){
                    sb.append(PeriodType.once.getChnName()).append(" ,")
                            .append(mwAssetsMainTainParamV1.getOnceParam().getStartDay()).append(" ");
                }else{
                    sb.append(PeriodType.once.getChnName()).append(" ,");
                    for (String onceDate : onceDates) {
                        sb.append(onceDate).append(" ");
                    }
                }

            }

            if (null != mwAssetsMainTainParamV1.getDayParam()) {
                sb.append(PeriodType.day.getChnName()).append(" ,");
            }

            if (null != mwAssetsMainTainParamV1.getWeekParam()) {
                sb.append(PeriodType.week.getChnName()).append(" ");
                for(Integer index : mwAssetsMainTainParamV1.getWeekParam().getWeekIndexes()){
                    sb.append(",");
                    String day = weekMap.get(index);
                    sb.append(day);
                }
                sb.append(" ");
            }

            if (null != mwAssetsMainTainParamV1.getMonthParam()) {
                MwAssetsMainTainV1Month monthParam = mwAssetsMainTainParamV1.getMonthParam();
                sb.append(PeriodType.month.getChnName()).append(" ");

                for(Integer monthIndex : monthParam.getMonthIndexes()){
                    sb.append(",").append(monthIndex).append("月");
                }

                sb.append(" ");

                if(null != monthParam.getWeekIndexPerMonth()){
                    for(Integer index: monthParam.getWeekIndexPerMonth()){
                        sb.append(",第").append(index).append("周");
                    }
                    sb.append(" ");

                    for(Integer index: monthParam.getWeekIndexes()){
                        String weekName = weekMap.get(index);
                        sb.append(",").append(weekName);
                    }
                }

                if(null != monthParam.getDayIndexes()){
                    for(Integer dayIndex: monthParam.getDayIndexes()){
                        sb.append(",").append(dayIndex).append("号");
                    }
                }

                sb.append(" ");
            }

            if(null != mwAssetsMainTainParamV1.getPeriods()){
                this.periods = mwAssetsMainTainParamV1.getPeriods();
                for(MwAssetsMainTainV1Period period1 : this.periods){
                    sb.append(period1.getStart()).append("-").append(period1.getEnd()).append(" ");
                }
            }
            this.taskDescription = sb.toString();
        }

    }
}
