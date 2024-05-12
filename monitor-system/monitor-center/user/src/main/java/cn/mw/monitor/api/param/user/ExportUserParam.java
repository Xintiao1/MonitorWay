package cn.mw.monitor.api.param.user;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gui.quanwang
 * @className ExportUserParam
 * @description 导入导出用户信息数据
 * @date 2021/12/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "导入用户数据")
public class ExportUserParam {

    /**
     * 用户登录名称
     */
    @ApiModelProperty(value = "用户名")
    @ExcelProperty(value = "用户名", index = 0)
    private String loginName;

    /**
     * 用户姓名
     */
    @ApiModelProperty(value = "姓名")
    @ExcelProperty(value = "姓名", index = 1)
    private String userName;

    /**
     * 用户手机号
     */
    @ApiModelProperty(value = "手机号")
    @ExcelProperty(value = "手机号", index = 2)
    private String phoneNumber;

    /**
     * 用户登录密码
     */
    @ApiModelProperty(value = "密码")
    @ExcelProperty(value = "密码", index = 3)
    private String password;

    /**
     * 用户所属机构，如果有多个用;传递
     */
    @ApiModelProperty(value = "所属机构/部门")
    @ExcelProperty(value = "所属机构/部门", index = 4)
    private String orgs;

    /**
     * 用户组数据，如果有多个用;传递
     */
    @ApiModelProperty(value = "用户组")
    @ExcelProperty(value = "用户组", index = 5)
    private String groups;

    /**
     * 角色名称，为用户导入赋予角色信息
     */
    @ApiModelProperty(value = "角色名称")
    @ExcelProperty(value = "角色名称", index = 6)
    private String roleName;

    /**
     * 错误信息，导入失败后返回
     */
    @ApiModelProperty(value = "错误信息")
    @ExcelProperty(value = "错误信息", index = 7)
    private String errorMsg;
}
