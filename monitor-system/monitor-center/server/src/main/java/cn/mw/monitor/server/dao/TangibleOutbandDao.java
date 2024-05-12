package cn.mw.monitor.server.dao;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author syt
 * @Date 2020/5/24 23:00
 * @Version 1.0
 */
public interface TangibleOutbandDao {
    AssetsBaseDTO getOutHostId(@Param("ipAddress") String ipAddress);
    /**
     * 查询ip重复的其他资产
     */
    List<MwTangibleassetsDTO> checkIpAddress(String checkParam);

}
