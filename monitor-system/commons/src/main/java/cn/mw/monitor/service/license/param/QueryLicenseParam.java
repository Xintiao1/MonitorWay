package cn.mw.monitor.service.license.param;

import lombok.Data;

import java.util.Date;

@Data
public class QueryLicenseParam {
    //模块id
    String moduleId;
    //模块名称
    String moduleName;
    //剩余天数
    int remainDate;
    //创建时间
    Date createDate;

}
