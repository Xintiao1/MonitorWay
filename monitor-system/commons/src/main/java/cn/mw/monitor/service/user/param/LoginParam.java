package cn.mw.monitor.service.user.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel(description = "登录用户数据")
public class LoginParam {

    // 登录用户id
    @ApiModelProperty("登录用户id")
    private String userId;
    // 登录名
    @ApiModelProperty("登录名")
    @NotEmpty(message = "用户名不能为空!")
    private String loginName;
    // 登录密码
    @ApiModelProperty("登录密码")
    @NotEmpty(message = "登录密码不能为空!")
    private String password;
    // 验证码
    @ApiModelProperty("验证码")
    private String captcha;
    // shiro校验产生的token
    @ApiModelProperty("shiro校验产生的token")
    private String token;

    @ApiModelProperty("登录类型")
    private String loginType;

    @ApiModelProperty("密码检测方式:0.需要加密检测 1.不需要加密检测")
    private Integer passwordCheck=0;

    private Boolean LdapRight;
}
