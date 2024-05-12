package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 16:07
 */
@Data
@ApiModel
public class QueryRelationInstanceModelParam extends BaseParam {
    @ApiModelProperty("模型实例id")
    private Integer instanceId;
    @ApiModelProperty("模型实例ids")
    private List<Integer> instanceIds;
    @ApiModelProperty("模型id")
    private Integer modelId;
    @ApiModelProperty("模型Index")
    private String modelIndex;
    @ApiModelProperty("模型Indexs")
    private List<String> modelIndexs;

    //指定返回字段
    private List<String> fieldList;
    //指定不返回字段
    private List<String> noFieldList;

    private Integer relationInstanceId;

    private List<Integer> relationInstanceIds;
    //是否查询单前实例信息
    private Boolean queryOwnInstancInfo;

    private String keyword;
}
