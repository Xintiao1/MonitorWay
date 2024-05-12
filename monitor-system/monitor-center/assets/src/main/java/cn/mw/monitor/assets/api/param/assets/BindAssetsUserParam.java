package cn.mw.monitor.assets.api.param.assets;

import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/27
 */
@Data
public class BindAssetsUserParam {
    private List<Integer> userIds;

    private String assetsId;

    public BindAssetsUserParam(List<Integer> userIds, String assetsId){
        this.userIds = userIds;
        this.assetsId = assetsId;
    }
}
