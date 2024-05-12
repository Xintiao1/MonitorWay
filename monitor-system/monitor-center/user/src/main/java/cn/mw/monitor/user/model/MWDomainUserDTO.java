package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.math.NumberUtils;

/**
 * @ClassName User
 * @Description: TODO
 * @Author zhaoy
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MWDomainUserDTO {

    /**
     * AD域，表示账户被禁用
     */
    private final static int ACCOUNT_DISABLED = 2;

    /**
     * 用户名（姓名）
     */
    private String userName;

    /**
     * 登录名称
     */
    private String loginName;

    private String cName;
    /**
     * 邮箱
     */
    private String mail;
    /**
     * 手机号
     */
    private String phone;

    /**
     * 企业微信号
     */
    private String wxNo;
    /**
     * 钉钉号
     */
    private String dingdingNo;
    /**
     * 用户状态
     */
    private String userState;

    /**
     * 2023-03-30 TCL项目，用于更新科长属性（上级）
     */
    private String manager;

    /**
     * TCL项目，获取用户是否有效
     *
     * @return true:正常使用   false：禁止使用
     */
    public String getEnabled() {
        if (NumberUtils.isNumber(userState)) {
            Long value = Long.parseLong(userState);
            return String.valueOf((value & ACCOUNT_DISABLED) != ACCOUNT_DISABLED);
        }
        return userState;
    }
}
