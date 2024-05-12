package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ShenzhenSmsParam {

    private String ruleId;
    //应用ID
    private int appID;

    //应用密码
    private String appPWD;

    //业务大类编号
    private String bizClassID;

    //业务类型编号
    private String bizTypeID;

    //业务子类型编号
    private String bizSubTypeID;

    //扩展编号
    private String extNo;

    //是否需要状态报告
    private int isNeedReport;

    //合同账号
    private int custID;

    //应用系统标识
    private  String appSMSCode;

    //api接口地址
    private String apiUrl;

    //角色名
    private String apiRoleName;
}
