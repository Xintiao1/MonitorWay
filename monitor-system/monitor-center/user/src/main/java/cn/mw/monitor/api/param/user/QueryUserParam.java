package cn.mw.monitor.api.param.user;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/3/24
 */
@Data
@ApiModel(description = "查询用户列表数据")
public class QueryUserParam extends BaseParam {

    /**
     * 用户ID
     */
    @ApiModelProperty("用户id")
    private Integer userId;
    /**
     * 角色ID
     */
    @ApiModelProperty("角色id")
    private Integer roleId;
    /**
     * 机构ID
     */
    @ApiModelProperty("机构id")
    private Integer orgId;
    /**
     * 姓名
     */
    @ApiModelProperty("姓名")
    private String userName;

    /**
     * 机构/部门
     */
    @ApiModelProperty("机构/部门")
    private String department;
    /**
     * 电话号码
     */
    @ApiModelProperty("电话号码")
    private String phoneNumber;
    /**
     * 微信公众号
     */
    @ApiModelProperty("微信")
    private String wechatId;
    /**
     * 钉钉ID
     */
    @ApiModelProperty("钉钉id")
    private String ddId;
    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;
    /**
     * 用户状态,1-启用,-1禁用
     */
    @ApiModelProperty("用户状态")
    private String userState;
    /**
     * 登录状态
     */
    @ApiModelProperty("登录状态")
    private String loginState;
    /**
     * 密码策略
     */
    @ApiModelProperty("密码策略")
    private String activePasswdPlan;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTimeStart;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTimeEnd;
    /**
     *  修改时间
     */
    @ApiModelProperty("修改时间")
    private Date updateTimeStart;
    /**
     *  修改时间
     */
    @ApiModelProperty("修改时间")
    private Date updateTimeEnd;
    /**
     * 有效期
     */
    @ApiModelProperty("有效期")
    private String userExpireState;
    /**
     * 有效期日期
     */
    @ApiModelProperty("有效期日期")
    private Date userExpiryDateStart;
    /**
     * 有效期日期
     */
    @ApiModelProperty("有效期日期")
    private Date userExpiryDateEnd;
    /**
     * 登录名
     * */
    @ApiModelProperty("登录名")
    private String loginName;

    /**
     * 角色名
     * */
    @ApiModelProperty("角色名")
    private String roleName;
    /**
     * 密码策略名
     * */
    @ApiModelProperty("密码策略名")
    private String activePasswdPlanName;

    //用户登录控制条件  满足-1  不满足-2
    @ApiModelProperty("用户登录控制条件")
    private String conditionsValue;
    //用户登录控制动作  允许-1  不允许-2
    @ApiModelProperty("用户登录控制动作")
    private String actionValue;

    /**
     *  用户类型
     */
    @ApiModelProperty("用户类型")
    private String userType;

    private String browseType;

    /**
     * 组织/机构ID
     */
    @ApiModelProperty("用户组ID")
    private Integer groupId;
}
