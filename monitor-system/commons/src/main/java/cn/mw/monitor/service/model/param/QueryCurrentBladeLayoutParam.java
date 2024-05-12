package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class QueryCurrentBladeLayoutParam {
    //所属刀箱实例Id
    private String chassisInstanceId;
    //当前选择的刀片Id
    private String instanceId;
    //刀片布局数据
    private List<List<QueryBladeInstanceParam>> daoData;
}
