package cn.mw.syslog.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class SysLogDTO {
    //日志时间
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

}
