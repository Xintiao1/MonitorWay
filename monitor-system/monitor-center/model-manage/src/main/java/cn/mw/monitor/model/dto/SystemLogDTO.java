package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDTO extends BaseProcessParam {
    //日志时间
    private Date logTime;
    //操作登录名
    private String userName;
    //模块名
    private String modelName;
    //模型名称
    private String mwModelName;
    //操作对象
    private String objName;
    //变更之后内容
    private String operateDes;
    //变更之前内容
    private String operateDesBefore;
    //版本记录
    private Integer version;

    private String type;
}
