package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.ModelSnmpInfoDTO;

import java.util.List;

public interface MwModelSnmpInfoDao {
    ModelSnmpInfoDTO selectById(String id);
    void insert(ModelSnmpInfoDTO snmpInfoDTO);
    void batchDel(List<String> ids);

    void cleanTable();
}
