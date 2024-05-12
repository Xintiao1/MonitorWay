package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/8/31
 */
@Data
@ApiModel
public class MwInstanceFieldShowType {
    //查询字段类型(insert, list,editor,look)
    private String type;
}
