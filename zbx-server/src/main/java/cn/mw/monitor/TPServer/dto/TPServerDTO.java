package cn.mw.monitor.TPServer.dto;

import cn.mw.monitor.TPServer.model.MwTPServerTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/30 16:39
 * @Version 1.0
 */
@Data
public class TPServerDTO extends MwTPServerTable {
    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value="负责人")
    private List<Integer> principal;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;

    private String protocol;

    private String port;
}
