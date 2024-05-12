package cn.mw.monitor.assetsSubType.api.param;

import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date
 */
@Data
public class DeleteAssetsSubTypeParam {

    private List<Integer> typeIdList;
}
