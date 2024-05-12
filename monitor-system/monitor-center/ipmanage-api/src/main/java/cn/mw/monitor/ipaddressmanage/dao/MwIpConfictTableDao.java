package cn.mw.monitor.ipaddressmanage.dao;

import cn.mw.monitor.ipaddressmanage.dto.IpConflictHisTableDTO;
import java.util.List;

public interface MwIpConfictTableDao {
    void batchInsertIpConflictHis(List<IpConflictHisTableDTO> list);
    void batchInsertIpConflictHisDetail(List<IpConflictHisTableDTO> list);
}
