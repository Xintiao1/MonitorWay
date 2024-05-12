package cn.mw.monitor.user.dto;

import cn.mw.monitor.state.DataPermission;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className GlobalUserInfo
 * @description 全局用户信息
 * @date 2021/10/21
 */
@Data
public class GlobalUserInfo extends MwUserDTO {

    /**
     * 组织机构ID列表
     */
    private List<Integer> orgIdList;

    /**
     * 用户组ID列表
     */
    private List<Integer> userGroupIdList;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 是否为系统管理员
     */
    private boolean systemUser;

    /**
     * 数据权限
     */
    private DataPermission dataPermission;
}
