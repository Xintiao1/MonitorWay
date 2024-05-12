package cn.mw.monitor.bean;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.state.DataType;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className DataPermission
 * @description 数据权限总类
 * @date 2022/4/25
 */
@Data
public class DataPermission {

    /**
     * 对应数据的主键ID
     */
    private String id;

    /**
     * 所属类别
     */
    private DataType dataType;

    /**
     * 负责人列表
     */
    private List<UserDTO> principal;

    /**
     * 所属部门列表
     */
    private List<OrgDTO> department;

    /**
     * 所属用户组列表
     */
    private List<GroupDTO> groups;

    /**
     * 机构节点列表
     */
    private List<List<Integer>> orgNodes;

    /**
     * 机构ID列表
     */
    private List<Integer> orgIds;

    /**
     * 负责人ID列表
     */
    private List<Integer> userIds;

    /**
     * 用户组ID列表
     */
    private List<Integer> groupIds;

}
