package cn.mw.monitor.activiti.entiy.OA;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 202303 9:25
 * @description
 */
@Data
@ApiModel(value = "组织架构数据")
public class Targets {
    @ApiModelProperty(value = "EKP系统组织架构唯一标识")
    String Id;
    @ApiModelProperty(value = "EKP系统组织架构个人编号")
    String PersonNo;
    @ApiModelProperty(value = "EKP系统组织架构部门编号")
    String DeptNo;
    @ApiModelProperty(value = "EKP系统组织架构岗位编号")
    String PostNo;
    @ApiModelProperty(value = "EKP系统组织架构常用群组编号")
    String GroupNo;
    @ApiModelProperty(value = "EKP系统组织架构个人登录名")
    String LoginName;
    @ApiModelProperty(value = "EKP系统组织架构关键字")
    String Keyword;
    @ApiModelProperty(value = "和LDAP集成时LDAP中DN值")
    String LdapDN;
}
