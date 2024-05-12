package cn.mw.monitor.logManage.param;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@ApiModel(value = "转发配置请求参数")
public class ForWardAddParam {

    @ApiModelProperty(value = "主键,添加数据时不需要传递")
    private Long id;

    @ApiModelProperty(value = "转发配置名")
    private String forwardingName;

    @ApiModelProperty(value = "类型")
    private String forwardingType;

    @ApiModelProperty(value = "转发配置方式")
    private String forwardingMethod;

    @ApiModelProperty(value = "转发配置地址")
    private String forwardingIp;

    @ApiModelProperty(value = "转发配置端口")
    private Integer forwardingPort;

    /**
     * 选择类型与转发方式获取分类后将其他信息以json object传输
     */
    @ApiModelProperty(value = "不同类型的字段信息")
    private JSONObject fieldInfo;



    public boolean validation() throws IllegalArgumentException {
        if (StringUtils.isBlank(forwardingName)) {
            throw new IllegalArgumentException("转发配置名为空");
        }
        if (StringUtils.isBlank(forwardingMethod)) {
            throw new IllegalArgumentException("转发配置方式为空");
        }
        if (StringUtils.isBlank(forwardingIp)) {
            throw new IllegalArgumentException("转发配置地址为空");
        }
        if (forwardingPort == null) {
            throw new IllegalArgumentException("转发配置端口为空");
        }
        return true;
    }

}
