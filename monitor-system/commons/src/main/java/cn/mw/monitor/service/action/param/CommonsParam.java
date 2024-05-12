package cn.mw.monitor.service.action.param;

import lombok.Data;
import cn.mw.monitor.service.assets.model.UserDTO;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/27 14:35
 */
@Data
public class CommonsParam {
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;
    private List<Integer> userIds;
    private List<Integer> groupIds;
    private List<List<Integer>> orgIds;

    private List<UserDTO> principal;

    private List<OrgDTO> department;

    private List<GroupDTO> groups;
}
