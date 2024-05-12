package cn.mw.monitor.engineManage.dto;

import cn.mw.monitor.service.engineManage.model.MwEngineManageTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *  @author sunyating
 *  @date 2020/4/27
 */
@Data
public class EngineManageTableDTO extends MwEngineManageTable {
    /**
     *模式
     */
    private String modeName;

    /**
     *加密方式
     */
    private String encryptionName;

    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value="负责人")
    private List<Integer> principal;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;
}
