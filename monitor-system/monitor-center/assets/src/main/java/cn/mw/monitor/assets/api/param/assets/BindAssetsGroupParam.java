package cn.mw.monitor.assets.api.param.assets;

import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/27
 */
@Data
public class BindAssetsGroupParam {
    private List<Integer> groupIds;

    private String assetsId;

    public BindAssetsGroupParam(List<Integer> groupIds, String assetsId){
        this.groupIds = groupIds;
        this.assetsId = assetsId;
    }
}
