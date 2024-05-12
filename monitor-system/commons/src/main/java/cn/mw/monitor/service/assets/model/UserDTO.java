package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.assets.model.OrgDTO;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/30
 */
@Data
public class UserDTO {

    private Integer userId;

    private String userName;

    private String loginName;

    private String userState;

    /**
     * 机构/部门
     */
    private List<OrgDTO> userDepartment;
}
