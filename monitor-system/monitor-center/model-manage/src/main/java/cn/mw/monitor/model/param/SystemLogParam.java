package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemLogParam extends BaseParam {
    //第几季度日志
    private String tableNameKey;
    //系统日志时间
    private Date logTime;
    //操作登录名
    private String userName;
    //模块名
    private String modelName;
    //操作对象
    private String objName;
    //操作内容
    private String operateDes;
    //模块类型
    private String type;

    //登录ip
    private String userIp;
    //登录是否成功
    private String isSuccess;
    //失败原因
    private String failType;
    //登录方式
    private String loginWay;

    //登录日志时间
    private Date createDate;

    /*日志类型*/
    Integer logType;

    private Date createDateStart;

    private Date createDateEnd;

    //模型管理使用；模型实例Id
    private Integer instanceId;
}
