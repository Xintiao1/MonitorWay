package cn.mw.monitor.virtualization.dto;

import cn.mw.monitor.common.util.GroupHosts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author syt
 * @Date 2021/4/8 10:04
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupHostDTO {
    private String groupid;
    private String name;
    private List<GroupHosts> hosts;
}
