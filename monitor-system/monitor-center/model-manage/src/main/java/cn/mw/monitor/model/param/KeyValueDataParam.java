package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 */
@Data
@ApiModel
public class KeyValueDataParam {
    private String label;
    private String value;
    //关联Id
    private String relationId;
}
