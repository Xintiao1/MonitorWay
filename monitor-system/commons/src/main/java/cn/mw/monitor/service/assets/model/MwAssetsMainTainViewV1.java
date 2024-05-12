package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.assets.param.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MwAssetsMainTainViewV1{

    /**
     * 维护表Id
     */
    @ApiModelProperty("ID")
    private Integer id;

    /**
     * 维护名称
     */
    @ApiModelProperty("名称")
    private String name;

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
     * 主机集合
     */
    @ApiModelProperty("主机")
    private List<MWMainTainHostView> hostids;

    /**
     * 生效开始时间
     */
    @ApiModelProperty("生效开始时间")
    private Date activeSince;

    /**
     * 生效结束时间
     */
    @ApiModelProperty("生效结束时间")
    private Date activeTill;

    /**
     * 一次性
     */
    @ApiModelProperty("一次性")
    private MwAssetsMainTainV1Once onceParam;

    /**
     * 每日
     */
    @ApiModelProperty("每日")
    private MwAssetsMainTainV1Day dayParam;

    /**
     * 每周
     */
    @ApiModelProperty("每周")
    private MwAssetsMainTainV1Week weekParam;

    /**
     * 每月
     */
    @ApiModelProperty("每月")
    private MwAssetsMainTainV1Month monthParam;

    /**
     * 维护期间
     */
    @ApiModelProperty("维护期间")
    private List<MwAssetsMainTainV1Period> periods;
}
