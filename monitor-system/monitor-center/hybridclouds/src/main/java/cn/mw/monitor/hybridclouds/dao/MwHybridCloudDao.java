package cn.mw.monitor.hybridclouds.dao;

import cn.mw.monitor.hybridclouds.dto.QueryNewHostParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qzg
 * @date 2021/6/8
 */
public interface MwHybridCloudDao {
    List<MwTangibleassetsDTO> selectAssetsByAssetsTypeId(@Param("assetsTypeId") Integer assetsTypeId);

    List<MwTangibleassetsTable> getAssetsIdById(QueryNewHostParam qParam);
}
