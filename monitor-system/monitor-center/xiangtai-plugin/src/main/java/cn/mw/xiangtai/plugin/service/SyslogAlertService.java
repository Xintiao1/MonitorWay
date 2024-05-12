package cn.mw.xiangtai.plugin.service;

import cn.mw.xiangtai.plugin.domain.dto.*;
import cn.mw.xiangtai.plugin.domain.entity.SyslogAlertEntity;
import cn.mw.xiangtai.plugin.domain.param.EventParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface SyslogAlertService extends IService<SyslogAlertEntity> {

    List<AttackSourceDTO> attackSourceTopN(Integer topN);

    List<AttackTypeDTO> attackTypeTopN(Integer topN);

    List<LogPointDTO> logEventCount(EventParam param);

    List<LogPointDTO> attackEventCount(EventParam param);

    List<AttackDataDTO> attackData();

    List<LogPointDTO> attackEventCountByWeek();

    List<Map<String, Integer>> getThreatLevel();

    List<AttackFrequencyDTO> getAttackFrequency();

    /**
     * 获取当前的攻击情况
     * @return list
     */
    List<AttackAddressDataDTO> getAttackDataForTheDay();

    /**
     * 获取威胁事件列表
     * @return list
     */
    List<ThreatEventDTO> getThreatEventListV2();

    /**
     * 获取威胁事件列表
     * @return list
     */
    List<SyslogAlertEntity> getThreatEventListV1();
}
