package cn.mw.monitor.assetsSubType.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.assetsSubType.api.param.AssetsSubType.QueryAssetsSubTypeParam;
import cn.mw.monitor.assetsSubType.dto.TypeTreeDTO;
import cn.mw.monitor.assetsSubType.model.MwAssetsSubTypeTable;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/20
 */
public interface MwAssetsSubTypeService {
    Reply updateAssetsGroupId();

    Reply selectById(Integer id);

    Reply selectList(QueryAssetsSubTypeParam qsDTO);

    Reply update(MwAssetsSubTypeTable ausDTO);

    Reply delete(List<Integer> ids);

    Reply insert(MwAssetsSubTypeTable ausDTO);

    Reply selectDorpdownList(QueryAssetsSubTypeParam qsParam);

    Reply selectGroupServerMapList();

    Reply selectTypeTrees(TypeTreeDTO typeTreeDTO);

    Reply selectDorpdownList(boolean subTypeFlag,  Integer classify);

//    Reply selectTopType(QueryAssetsSubTypeParam qsDTO);
}
