package cn.mw.monitor.assets.service;

import cn.joinhealth.monitor.assets.dto.ItemDTO;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * 硬件管理
 * @auth dev
 * @desc 返回不同主机类型的硬件信息
 * @date 2020/01/18
 */
public interface HardwareService {
    /**
     * 初始化
     * @return
     */
    public void init(Map<String, Object> params);

    /**
     * 获取所有硬件
     * @return
     */
    public List<ItemDTO> getHardwareInfo();

    /**
     * 获取zabbix分组下的主机磁盘信息
     * @return
     */
    public void getDiskInfoByGroup(List<String> groups);

}
