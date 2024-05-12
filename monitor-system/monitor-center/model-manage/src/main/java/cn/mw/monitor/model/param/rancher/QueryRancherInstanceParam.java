package cn.mw.monitor.model.param.rancher;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/15
 */
@Data
public class QueryRancherInstanceParam extends BaseParam {
    @ApiModelProperty("设备类型")
    private String rancherType;
    @ApiModelProperty("设备PId")
    private String rancherPId;
    @ApiModelProperty("设备Id")
    private String rancherId;
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("设备名称")
    private String rancherName;
    @ApiModelProperty("列表展示类型")
    private String listType;

    //导出的字段
    private List<String> header;
    //导出的字段名
    private List<String> headerName;
    @ApiModelProperty("关联依附实例Id")
    private Integer relationInstanceId;
    @ApiModelProperty("关联依附模型Index")
    private String relationModelIndex;
    private String clusterId;
    public void extractFrom(AddAndUpdateModelInstanceParam addAndUpdateModelInstanceParam){
        this.modelId = addAndUpdateModelInstanceParam.getModelId();
        this.modelInstanceId = addAndUpdateModelInstanceParam.getInstanceId();
        this.modelIndex = addAndUpdateModelInstanceParam.getModelIndex();
    }
}
