package cn.mw.monitor.hybridclouds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author qzg
 * @Date 2021/6/6
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
