package cn.mw.monitor.labelManage.dao;

import cn.mw.monitor.labelManage.model.MwAssetslabelTable;
import org.apache.ibatis.annotations.Param;

public interface MwAssetslabelTableDao {

    int deleteByPrimaryKey(Integer id);

    int insert(MwAssetslabelTable record);

    int insertSelective(MwAssetslabelTable record);

    MwAssetslabelTable selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MwAssetslabelTable record);

    int updateByPrimaryKey(MwAssetslabelTable record);
    /**
     * 根据标签Id查询关联资产数量
     */
    Integer countByLabelId(@Param("labelId")Integer labelId);

}