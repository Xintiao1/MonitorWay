package cn.mw.monitor.virtualization.dto;

import lombok.Data;

import java.util.List;

/**
 * 虚拟化负责人设置
 */
@Data
public class VirtualUser{
    private String typeId;//虚拟化资产标识Id
    private List<String> typeIdList;//虚拟化资产标识IdList
    private String userId;//负责人id
    private String userName;//负责人
    private String orgId;//机构id
    private String orgNodes;
    private List<Integer> orgIds;
    private String orgName;//机构
    private String groupId;//用户组Id
    private String groupName;//用户组
    private String type;//模块类型
}
