package cn.mw.monitor.service.zbx.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.alert.dto.AlertConfirmUserParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @author xhy
 * @date 2020/3/27 9:34
 */
@Data
public class AlertParam extends BaseParam {
    //筛选条件
    @ApiModelProperty("告警級別")
    private List<String> severity;//告警級別

    private String name;//告警标题
    private String objectName;//对象名称
    private String alertType;//告警类型
    private String ip;//告警ip
    private String longTime;//持续时间
    private String acknowledged;//确认状态

    private String startTime;//开始时间
    private String endTime;//结束时间

    private String isSeverDay;//是否是过去七天 0 不是 1是
    private String days;//自定义天数
    private List<String> hostids;

    private List<String> queryHostIds;

    private Integer queryMonitorServerId;

    @ApiModelProperty("userId")
    private Integer userId;//用户的uid

    private String eventid;
    private String objectId;

    private Integer monitorServerId;
    private String monitorServerName;
    private String rclock;
    private String fuzzyQuery;

    private Integer hisAlertCount;
    private Boolean isRedis=true;

    private Boolean isNowAlert;

    private String clockStart;//开始时间
    private String clockEnd;//结束时间
    private String modelClassify;
    private String modelSystem;

}
