package cn.mw.monitor.service.alert.dto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/10/30 9:51
 */
@Data
public class RecordParam extends BaseParam {
    //是否为今日：false：不是；true：是
    private Boolean isToday;
    //发送状态：1：成功；2：失败；为其它值查询全部
    private Integer sendState;
    private Boolean isAlarm;
    //事件ID
    private String eventid;
    private String hostid;
    //筛选条件
    private Date date;
    private String method;
    private String text;
    private String userName;
    private String resultState;
    private Date startDate;
    private Date endDate;
    private String seachAll;
}
