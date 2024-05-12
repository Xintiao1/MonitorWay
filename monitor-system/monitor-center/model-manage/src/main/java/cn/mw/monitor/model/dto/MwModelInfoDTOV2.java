package cn.mw.monitor.model.dto;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/9/23
 */
@Data
public class MwModelInfoDTOV2 {
    //模型id
    private String modelId;
    //模型名称
    private String modelName;
    //模型索引
    private String modelIndex;
    //模型分组名称
    private String modelGroupName;

    private String instanceId;
}
