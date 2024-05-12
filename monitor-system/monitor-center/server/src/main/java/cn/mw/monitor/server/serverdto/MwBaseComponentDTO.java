package cn.mw.monitor.server.serverdto;

import cn.mw.monitor.server.model.MwBaseComponent;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/2/3 15:23
 * @Version 1.0
 */
@Data
public class MwBaseComponentDTO extends MwBaseComponent {
    private JSONObject selfParam;
    private JSONObject param;
}
