package cn.mw.monitor.netflow.dao;

import cn.mw.monitor.netflow.dto.MWNetFlowConfigDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MWNetflowDao {
    List<MWNetFlowConfigDTO> selectNetflowConigInfo();
    void updateNetflowConigInfo(MWNetFlowConfigDTO mwNetFlowConfigDTO);

    /**
     * 获取资产名称和IP
     * @return
     */
    List<Map<String,String>> getAssetsNameMap();

    /**
     * 保存流量明细缓存数据
     *
     * @param userId    用户ID
     * @param cacheInfo 缓存数据
     */
    void saveNetlowCacheInfo(@Param(value = "userId") int userId,
                             @Param(value = "cacheInfo") String cacheInfo);

    /**
     * 获取流量明细缓存数据
     *
     * @param userId 用户ID
     */
    String getNetflowCacheInfo(@Param(value = "userId") int userId);

    /**
     * 删除流量明细缓存数据
     *
     * @param userId 用户ID
     */
    void deleteNetflowCacheInfo(@Param(value = "userId") int userId);
}
