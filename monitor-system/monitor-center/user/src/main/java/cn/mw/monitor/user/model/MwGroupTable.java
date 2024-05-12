package cn.mw.monitor.user.model;

import lombok.Data;

import java.util.Date;

@Data
public class MwGroupTable {

    // 用户组id
    private Integer groupId;
    // 用户组名称
    private String groupName;
    // 用户组状态
    private String enable;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date createDate;
    // 修改时间
    private Date modificationDate;
    // 删除标识
    private Boolean deleteFlag;

}