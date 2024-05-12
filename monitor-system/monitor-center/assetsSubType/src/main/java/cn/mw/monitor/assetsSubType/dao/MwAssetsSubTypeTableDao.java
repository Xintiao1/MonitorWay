package cn.mw.monitor.assetsSubType.dao;

import cn.mw.monitor.assetsSubType.api.param.AssetsSubType.QueryAssetsSubTypeParam;
import cn.mw.monitor.assetsSubType.dto.TypeTreeDTO;
import cn.mw.monitor.assetsSubType.model.MwAssetsGroupTable;
import cn.mw.monitor.assetsSubType.model.MwAssetsSubTypeTable;
import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwAssetsSubTypeTableDao {
    List<Integer> getAssetsUseSubType();
    List<Integer> getAssetsUseType();

    int insertBatchGroupServerMap(List<MwAssetsGroupTable> list);

    List<MwAssetsSubTypeTable> selectGroupNames();

    void cleanGroupServerMap();

    int delete(List<Integer> id);

    int insert(MwAssetsSubTypeTable record);

    int insertBatch(List<MwAssetsSubTypeTable> record);

    MwAssetsSubTypeTable selectById(Integer id);

    int updateById(MwAssetsSubTypeTable record);

    int updateBatch(List<MwAssetsSubTypeTable> record);

    List<MwAssetsSubTypeTable> selectList(QueryAssetsSubTypeParam qParam);

    List<MwAssetsSubTypeTable> selectAllList();

    List<Integer> selectAutoIncrment();

    int batCreateGroupServerMap(List<MwAssetsGroupTable> list);
    List<MwAssetsGroupTable> selectGroupServerMap(@Param("assetsSubTypeId") Integer assetsSubTypeId);
    int deleteGroupIds(List<MwAssetsGroupTable> id);

    List<TypeTreeDTO> selectTypeTrees(TypeTreeDTO typeTreeDTO);

    List<DropdownDTO> selectTypeList(@Param("pid") Integer pid, @Param("classify") Integer classify);

//    List<MwAssetsSubTypeTable> selectType(QueryAssetsSubTypeParam qParam);
}
