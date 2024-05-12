package cn.mw.monitor.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/8/4 15:58
 * @Version 1.0
 */
@Data
public class MwModelUserDTO {

    private Integer userId;

    private String userName;

    private String loginName;

    private String userState;

    /**
     * 机构/部门
     */
    private List<MwModelOrgDTO> userDepartment;
}
