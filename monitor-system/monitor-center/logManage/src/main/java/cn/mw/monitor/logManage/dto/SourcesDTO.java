package cn.mw.monitor.logManage.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "sources对象")
public class SourcesDTO {

    @ApiModelProperty(value = "间隔时间,以秒为单位传递")
    private Integer interval;

    @ApiModelProperty(value = "源名称")
    private String sourcesName;

    @ApiModelProperty(value = "解析规则类型")
    private String sourcesType;

    @ApiModelProperty(value = "字段信息")
    private JSONObject fieldInfo;
}
