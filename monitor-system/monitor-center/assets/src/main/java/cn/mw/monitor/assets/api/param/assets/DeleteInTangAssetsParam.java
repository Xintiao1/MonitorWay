package cn.mw.monitor.assets.api.param.assets;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
public class DeleteInTangAssetsParam {
    private List<String>  idList = new ArrayList<String>();
}
