package cn.mw.module.security.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/9/11 9:43
 */
@Data
public class MessageParam extends BaseParam {
    private String[] queryDate;
    @ApiModelProperty("消息")
    private String message;
    @ApiModelProperty("主机名称")
    private String host;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("时间")
    private String timestamp;
    @ApiModelProperty("日志来源")
    private String logSource;
    private String assetsType;

}
