package cn.mw.monitor.service.timetask.api;

import cn.mw.monitor.service.timetask.dto.MwBaseLineHealthValueCommonsDto;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwBaseLineValueService
 * @Description 查询基线健康值
 * @Author gengjb
 * @Date 2022/5/30 9:51
 * @Version 1.0
 **/
public interface MwBaseLineValueService {

    /**
     * 根据监控项名称与资产主机ID查询基线的健康值
     * @param itemNames 监控项名称集合
     * @param assetsId 资产主机ID
     * @return
     */
    Reply selectBaseLineHealthValue(List<String> itemNames, String assetsId);

    Reply selectHealthValueByAssets();

    /**
     * 获取基线所有监控项健康值
     * @return
     */
    List<Map<String,Object>> getBaseLineAllData();
}
