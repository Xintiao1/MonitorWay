package cn.mw.monitor.user.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by zy.quaee on 2021/5/8 11:01.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MWDomainInfoDTO {
    private Integer id;
    private String adServerIpAdd;
    private String adPort;
    private String adServerName;
    private Date updateTime;
    private String adInfo;
    /**
     * 部门/机构配置信息
     */
    private String localInfo;
    /**
     * 用户组信息
     */
    private String groupInfo;

    private String searchNodes;
    private String type;

    /**
     * 映射本地 用户组IDs 机构IDs
     */
    private String  userGroup;
    private String department;
    private String roleId;

    /**
     * 配置备注
     */
    private String configDesc;
}
