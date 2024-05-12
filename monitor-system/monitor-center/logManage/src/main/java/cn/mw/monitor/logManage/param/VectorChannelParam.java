package cn.mw.monitor.logManage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "通道请求参数")
public class VectorChannelParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID 更新使用")
    private Long id;

    /**
     * vector通道名称
     */
    @ApiModelProperty(value = "通道名称")
    private String channelName;

    /**
     * vector_ip地址
     */
    @ApiModelProperty(value = "通道IP地址")
    private String channelIp;

    /**
     * vector服务端口
     */
    @ApiModelProperty(value = "通道端口")
    private Integer channelPort;

    @ApiModelProperty(value = "通道类型")
    private String type = "内置";

    /**
     * vector服务状态 0.关闭 1.开启. 添加数据时不用传
     */
    @ApiModelProperty(value = "是否认证 0：关闭  1：开启; 添加数据时不用传")
    private Integer status;

    @ApiModelProperty(value = "通道用户名")
    private String channelUserName;

    @ApiModelProperty(value = "通道密码")
    private String channelPassword;

    @ApiModelProperty(value = "通道KEY")
    private String keyPath;

    public boolean validation() throws IllegalArgumentException {
        if (StringUtils.isBlank(channelName)) {
            throw new IllegalArgumentException("通道名不能为空");
        }
        if (StringUtils.isBlank(channelIp)) {
            throw new IllegalArgumentException("通道IP地址不能为空");
        }
        if (channelPort == null || channelPort == 0) {
            throw new IllegalArgumentException("通道端口不能为空");
        }
        if (StringUtils.isBlank(channelUserName) || StringUtils.isBlank(channelPassword)) {
            throw new IllegalArgumentException("用户名与密码不能为空");
        }
        if (channelPassword.length() > 16) {
            throw new IllegalArgumentException("密码不能超过16位");
        }

        return true;
    }

}
