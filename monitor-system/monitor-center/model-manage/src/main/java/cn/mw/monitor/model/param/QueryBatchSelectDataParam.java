package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/3/07 14:44
 */
@Data
public class QueryBatchSelectDataParam{
    List<QuerySelectDataListParam> layoutDataList;

}
