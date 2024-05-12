package cn.mw.monitor.api.param.user;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gui.quanwang
 * @className UserExport
 * @description 用户导出
 * @date 2022/9/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExport {

    /**
     * 用户登录名称
     */
    @ExcelProperty(value = "用户名", index = 0)
    private String loginName;

    /**
     * 用户姓名
     */
    @ExcelProperty(value = "姓名", index = 1)
    private String userName;

    /**
     * 用户手机号
     */
    @ExcelProperty(value = "手机号", index = 2)
    private String phoneNumber;

    /**
     * 用户登录密码
     */
    @ExcelProperty(value = "密码", index = 3)
    private String password;

    /**
     * 用户所属机构，如果有多个用;传递
     */
    @ExcelProperty(value = "所属机构/部门", index = 4)
    private String orgs;

    /**
     * 用户组数据，如果有多个用;传递
     */
    @ExcelProperty(value = "用户组", index = 5)
    private String groups;

    /**
     * 角色名称，为用户导入赋予角色信息
     */
    @ExcelProperty(value = "角色名称", index = 6)
    private String roleName;

    /**
     * 微信号
     */
    @ExcelProperty(value = "微信号", index = 7)
    private String weChatNo;

    /**
     * 钉钉
     */
    @ExcelProperty(value = "钉钉", index = 8)
    private String dingTalkNo;

    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱", index = 9)
    private String emailNo;

    /**
     * 有效期时间
     */
    @ExcelProperty(value = "有效期时间", index = 10)
    private String validTime;

    /**
     * 用户状态
     */
    @ExcelProperty(value = "用户状态", index = 11)
    private String userState;

    /**
     * 用户类型
     */
    @ExcelProperty(value = "用户类型", index = 12)
    private String userType;

    /**
     * 错误信息，导出失败后返回
     */
    @ExcelProperty(value = "错误信息", index = 13)
    private String exportErrorMsg;
}
