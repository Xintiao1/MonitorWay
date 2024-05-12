package cn.mw.monitor.service.systemLog.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

@Data
public class QueryLogParam extends BaseParam {
    String tableNameKey;

    /*调用者名称*/
    String userName;
    /*用户IP地址*/
    String userIp;
    /*调用者名称*/
    String remark;
    /*方法调用时间*/
    Date createDateStart;

    /*方法调用时间*/
    Date createDateEnd;

    /*日志类型*/
    Integer logType;

}
