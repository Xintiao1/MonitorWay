package cn.mw.monitor.api.param.aduser;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zy.quaee on 2021/5/20 16:44.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ADGroupUserParam extends BaseParam {
    private Integer configId;
}
