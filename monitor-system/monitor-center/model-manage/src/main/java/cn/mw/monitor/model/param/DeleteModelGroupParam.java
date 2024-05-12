package cn.mw.monitor.model.param;

import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/19 15:39
 */
@Data
public class DeleteModelGroupParam {
    private Integer modelGroupId;
    private String modelGroupName;
    private Integer deep;
    private Integer modelType;
    private Integer groupLevel;
}
