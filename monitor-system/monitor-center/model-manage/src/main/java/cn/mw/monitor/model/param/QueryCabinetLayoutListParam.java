package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class QueryCabinetLayoutListParam {
    List<QueryCabinetLayoutParam> cabinetLayoutList;
}
