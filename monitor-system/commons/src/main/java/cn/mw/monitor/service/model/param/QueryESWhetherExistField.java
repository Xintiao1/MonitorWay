package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class QueryESWhetherExistField{
    //需要查询的实例id
    private List<Integer> instanceIds;
    //需要查询的modleIndex
    private List<String> modelIndexs;
    //存在的指定字段
    private List<String> existFields;
    //不存在的指定字段
    private List<String> notExistFields;

    @ApiModelProperty("是否基础数据，true为基础设施下的数据，否为所有数据")
    private Boolean isBaseData;

}
