package cn.mw.monitor.model.param;

import cn.mw.monitor.state.DataType;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/27
 */
@Data
public class ModelRelationInstanceUserListParam {
    List<ModelRelationInstanceUserParam> paramList;

    private DataType type;
}
