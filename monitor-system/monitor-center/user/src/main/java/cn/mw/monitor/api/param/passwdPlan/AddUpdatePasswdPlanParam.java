package cn.mw.monitor.api.param.passwdPlan;

import cn.mw.monitor.api.param.org.AddUpdateOrgParam;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@GroupSequence({Insert.class,Update.class, AddUpdatePasswdPlanParam.class})
@ApiModel("新增或更新密码策略数据")
public class AddUpdatePasswdPlanParam {

    // 密码策略id
    @ApiModelProperty("密码策略id")
    @Null(message = "新增密码策略时密码策略id必须为空!",groups = {Insert.class})
    @NotNull(message = "更新密码策略时密码策略id不能为空!",groups = {Update.class})
    private Integer passwdId;
    // 密码策略名
    @ApiModelProperty("密码策略名")
    @NotEmpty(message = "密码策略名不能为空!",groups = {Insert.class,Update.class})
    @Size(max = 20,message = "密码策略名最大长度不能超过20字符!",groups = {Insert.class,Update.class})
    private String passwdName;
    // 密码策略关联部门
    @ApiModelProperty("密码策略关联部门")
    List<List<Integer>> department;
    // 最小密码长度
    @ApiModelProperty("最小密码长度")
    private Integer passwdMinLen;
    // 密码复杂度id
    @ApiModelProperty("密码复杂度id")
    private Integer passwdComplexId;
    // 是否开启密码历史检查
    @ApiModelProperty("是否开启密码历史检查")
    private Boolean hisCheckEnable;
    // 保留几次密码历史记录
    @ApiModelProperty("保留几次密码历史记录")
    @NotNull(message = "历史密码长度不能为空!",groups = {Insert.class,Update.class})
    private Integer hisNum;
    // 密码到期类型：true:永不到期 false：规定时间内到期
    @ApiModelProperty("密码到期类型  true：永不到期 false：规定时间内到期")
    private Boolean passwdExpireType;
    // 密码多少天过期
    @ApiModelProperty("密码多少天到期")
    private Integer passwdUpdateDate;
    // 密码过期前几天通知用户
    @ApiModelProperty("密码过期前几天通知用户")
    private Integer expireAlertDay;
    // 密码过期后几天必须修改密码
    @ApiModelProperty("密码过期后几天必须修改密码")
    private Integer afterResetDay;
    // 用户是否可以锁定
    @ApiModelProperty("用户是否可以锁定")
    private Boolean lockEnable;
    // 密码过期后几天强制锁定
    @ApiModelProperty("密码过期后几天强制锁定")
    private Integer afterLockDay;
    // 第一次登陆是否要求修改密码
    @ApiModelProperty("第一次登录是否要求修改密码")
    private Boolean firstPasswdEnable;
    // 是否强制用户修改密码
    @ApiModelProperty("是否强制用户修改密码")
    private Boolean changePasswdEnable;
    // 密码是否可以重置
    @ApiModelProperty("密码是否可以重置")
    private Boolean resetEnable;
    @ApiModelProperty("尝试登录失败后拒绝访问")
    // 尝试登陆失败后拒绝访问
    private Boolean isRefuseAcc;
    // 登陆重试几次
    @ApiModelProperty("登录重试几次")
    private Integer retryNum;
    // 重试间隔时间
    @ApiModelProperty("重试间隔时间")
    private Integer retrySec;
    // 密码盐值
    @ApiModelProperty("密码盐值")
    private String salt;
    // 散列类型ID
    @ApiModelProperty("散列类型id")
    private String hashTypeId;

}
