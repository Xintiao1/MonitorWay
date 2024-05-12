package cn.mw.monitor.wireless.dao;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qzg
 * @date 2021/6/23
 */
public interface MwWirelessDataShowDao {
    List<MwTangibleassetsTable> selectAssetsByAssetsTypeId(@Param("assetsTypeId") Integer assetsTypeId, @Param("assetsTypeSubId") Integer assetsTypeSubId);
}
