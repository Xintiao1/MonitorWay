package cn.mw.monitor.assets.api.param.assets;

import lombok.Data;

import java.util.List;

/**
 * @author dev
 * @date 2020/06/10
 */
@Data
public class UpdateMonStateParam {
    private List<String> idList;

    private List<String> hostIds;

    private String stateType;
    private boolean monitorFlag;

}
