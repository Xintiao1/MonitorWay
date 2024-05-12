package cn.mw.monitor.user.dto;

import cn.mw.monitor.service.user.model.MWUser;
import lombok.Data;

/**
 * @Author shenwenyi
 * @Date 2023/10/16 11:33
 * @PackageName:cn.mw.monitor.user.dto
 * @ClassName: UserGroupDTO
 * @Description: TODD
 * @Version 1.0
 */
@Data
public class UserOrgDTO extends MWUser {

    private Integer orgId;
}
