package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author qzg
 * @date 2023/5/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "添加和更新webmonitor数据")
public class AddAndUpdateModelWebMonitorParam {

    @ApiModelProperty("zibbix返回的ID")
    private Integer httpTestId;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("网站名称")
    private String instanceName;

    @ApiModelProperty("网站url")
    private String webUrl;

    @ApiModelProperty("更新间隔")
    @Max(value=86400,message = "更新间隔不能大于86400s")
    private Integer updateInterval;

    @ApiModelProperty("尝试次数1-10")
    private Integer attempts;

    @ApiModelProperty("客户端")
    private Integer client;

    @ApiModelProperty("http代理")
    private String httpProxy = "";

    @ApiModelProperty("启用状态")
    private boolean enable;

    @ApiModelProperty("跟随跳转")
    private Boolean followJump=false;

    @ApiModelProperty("超时")
    @Max(value=3600,message = "超时最大时间不能大于3600s")
    @Min(value=1,message = "超时最小时间不能小于1s")
    private Integer timeOut;

    @ApiModelProperty("必要状态码/监测代码")
    @Max(value=86400,message = "状态码不能大于86400")
    private String statusCode;

    @ApiModelProperty("必要字符串")
    private String string = "";


    private String creator;


    private String modifier;


    @ApiModelProperty(value = "责任人id")
    private List<Integer> userIds;

    @ApiModelProperty(value = "机构id")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组id")
    private List<Integer> groupIds;


    @ApiModelProperty(value = "关联监控服务器id")
    private Integer monitorServerId;

    @ApiModelProperty(value = "关联主机id")
    private String assetsId;

    @ApiModelProperty(value = "关联主机名称")
    private String assetsName;

    @ApiModelProperty(value = "是否监控")
    private Boolean isManage;
}
