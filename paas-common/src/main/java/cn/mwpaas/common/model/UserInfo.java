package cn.mwpaas.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author phzhou
 * @ClassName UserInfo
 * @CreateDate 2019/4/4
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 5869245685265367758L;

    /**
     * 主键
     */
    private String id;

    /**
     * 账号
     */
    private String userName;

    /**
     * 密码
     */
    private String userPwd;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Boolean isDelete;

    /**
     * 盐
     */
    private String salt;

    /**
     * 密码加密方式
     */
    private String encryptType;

    /**
     * 用户信息
     */
    private String token;

    /**
     * 角色id
     */
    private String roleId;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 菜单id集合
     */
    private List<String> menuIds;
}
