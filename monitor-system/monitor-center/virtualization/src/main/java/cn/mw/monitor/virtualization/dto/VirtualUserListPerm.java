package cn.mw.monitor.virtualization.dto;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/3/31
 */
@Data
public class VirtualUserListPerm {
    List<VirtualUserPerm> permList;
}
