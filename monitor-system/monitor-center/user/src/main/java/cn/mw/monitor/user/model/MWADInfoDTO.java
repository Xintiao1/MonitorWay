package cn.mw.monitor.user.model;

import cn.mw.monitor.service.user.model.MWUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by zy.quaee on 2021/5/8 10:48.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MWADInfoDTO {

    private Integer id;
    private String adInfo;
    private String localInfo;
    private String groupInfo;
    private String countUserNum;

    private List<MWUser> users;
    private String adType;
    private String searchNodes;

    private String roleId;
    private String orgId;
    private String groupId;

    /**
     * 映射本地 用户组IDs 机构IDs
     */
    private List<Integer> userGroup;
    private List<Integer> department;

    /**
     * 配置备注
     */
    private String configDesc;
}
