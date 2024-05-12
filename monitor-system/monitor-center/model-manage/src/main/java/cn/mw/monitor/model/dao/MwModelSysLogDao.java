package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.model.param.SystemLogParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qzg
 * @date 2021/11/11
 */
public interface MwModelSysLogDao {

    void saveInstaceChangeHistory(SystemLogDTO dto);

    void batchSaveInstaceChangeHistory(List<SystemLogDTO> list);

    Integer getChangeHistoryVersion(String type);

    List<SystemLogDTO> getInstaceChangeHistory(SystemLogParam qParam);

    void updateInstaceChangeHistory(@Param("targetType")String targetType,@Param("ownType") String ownType);
}
