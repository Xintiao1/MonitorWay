package cn.mw.monitor.model.param.virtual;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/27
 */
@Data
public class ModelVirtualUserListParam {
    List<ModelVirtualUserParam> paramList;
}
