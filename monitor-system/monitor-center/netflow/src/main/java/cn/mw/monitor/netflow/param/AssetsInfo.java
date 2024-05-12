package cn.mw.monitor.netflow.param;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className AssetsInfo
 * @description 资产数据
 * @date 2022/11/16
 */
@Data
public class AssetsInfo extends MwTangibleassetsTable {

    /**
     * 已经选择的端口列表
     */
    List<QueryAssetsInterfaceParam> selectedInterfaceList;

}
