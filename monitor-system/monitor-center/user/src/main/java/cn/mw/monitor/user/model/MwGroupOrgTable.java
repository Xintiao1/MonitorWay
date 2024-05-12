package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwGroupOrgTable {

    // 主键
    private Integer id;
    // 用户组id
    private Integer groupId;
    // 机构id
    private Integer orgId;
    // 更新时间
    private Date updateTime;
    // 删除标识
    private Boolean deleteFlag;

}