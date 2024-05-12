package cn.mw.monitor.bean;

import cn.mw.monitor.state.DataPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * @author zy.quaee
 * @date 2021/7/8 14:35
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataPermissionDTO {

    /**
     * 当前登录用户id
     */
    private Integer userId;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 当前用户的机构及子机构id集合
     */
    private List<Integer> orgIds;
    /**
     * 当前用户的用户组id集合
     */
    private List<Integer> groupIds;
    /**
     * 当前用户角色的数据权限  PUBLIC or PRIVATE
     */
    private DataPermission dataPermission;

    /**
     *  true 系统管理员角色
     */
    private Boolean adminRoleRight;

}
