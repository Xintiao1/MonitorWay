package cn.mw.monitor.TPServer.dto;

import cn.mw.monitor.TPServer.model.MwTPServerTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/30 15:41
 * @Version 1.0
 */
@Data
public class AddOrUpdateTPServerParam extends MwTPServerTable {

    private String protocol;

    private String port;

//    private List<List<Integer>> department;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
}
