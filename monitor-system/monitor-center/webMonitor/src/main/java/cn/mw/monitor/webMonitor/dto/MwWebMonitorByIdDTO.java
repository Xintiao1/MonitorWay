package cn.mw.monitor.webMonitor.dto;

import cn.mw.monitor.webMonitor.model.MwWebmonitorTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MwWebMonitorByIdDTO extends MwWebmonitorTable {

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
}
