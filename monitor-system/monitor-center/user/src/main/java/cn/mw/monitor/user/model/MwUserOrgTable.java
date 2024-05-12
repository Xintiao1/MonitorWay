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
public class MwUserOrgTable {

    // 主键id
    private Integer id;
    // 用户id
    private Integer userId;
    // 机构id
    private Integer orgId;
    // 更新时间
    private Date updateTime;
    // 删除标识
    private Boolean deleteFlag;

}
