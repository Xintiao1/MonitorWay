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
public class QueryESInstanceParam {
   private List<MwPropertyParam> propertyParams;

    //指定返回字段
    private List<String> fieldList;
    //指定不返回字段
    private List<String> noFieldList;
}
