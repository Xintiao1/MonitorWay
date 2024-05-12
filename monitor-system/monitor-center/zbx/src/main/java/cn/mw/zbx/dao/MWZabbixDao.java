package cn.mw.zbx.dao;

import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/25 18:19
 */
public interface MWZabbixDao {
    Integer getItemidBykey(@Param("hostId") Integer hostId, @Param("key_") String key_);
    String  getValueByItemid(@Param("itemid") Integer itemid);
    List<MWItemHistoryDto> getValueByItemidAndClock(@Param("itemid") Integer itemid, @Param("clockFrom") Long clockFrom, @Param("clockTill") Long clockTill);

    String getUintValueByItemid(Integer itemid_respcode);
}
