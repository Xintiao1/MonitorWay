package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/3/07 14:44
 */
@Data
public class QuerySelectDataListParam  extends BaseParam {
    private String modelIndex;
    private Integer instanceId;

    private String propertiesIndex;
    private List<Integer> coordinate;//修改后坐标
    private Integer currentRoomId;
    private Integer beforeRoomId;
    private List<Integer> beforeCoordinate;//修改前坐标
    private Boolean isDelete;
}
