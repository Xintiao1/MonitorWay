package cn.mw.xiangtai.plugin.service.impl;

import cn.mw.xiangtai.plugin.domain.dto.*;
import cn.mw.xiangtai.plugin.domain.entity.SyslogAlertEntity;
import cn.mw.xiangtai.plugin.domain.param.EventParam;
import cn.mw.xiangtai.plugin.mapper.SyslogAlertMapper;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional("mclickhouseTransactionManager")
public class SyslogAlertServiceImpl extends ServiceImpl<SyslogAlertMapper, SyslogAlertEntity> implements SyslogAlertService {

    @Override
    public List<AttackSourceDTO> attackSourceTopN(Integer topN) {
        return this.baseMapper.attackSourceTopN(topN);
    }

    @Override
    public List<AttackTypeDTO> attackTypeTopN(Integer topN) {
        return this.baseMapper.attackTypeTopN(topN);
    }

    @Override
    public List<LogPointDTO> logEventCount(EventParam param) {
        Integer dateType = param.getDateType();
        List<LogPointDTO> logPointDTOS;
        if (1 == dateType) {
            logPointDTOS = this.baseMapper.logEventCount(param.getInterval());
        } else if (2 == dateType) {
            logPointDTOS = this.baseMapper.logEventCountByTwelveMonths(param.getInterval());
        } else {
            logPointDTOS = this.baseMapper.logEventCountByThirtyDay(param.getInterval());
        }
        return logPointDTOS;
    }

    @Override
    public List<LogPointDTO> attackEventCount(EventParam param) {
        Integer dateType = param.getDateType();
        List<LogPointDTO> logPointDTOS;
        if (1 == dateType) {
            logPointDTOS = this.baseMapper.attackEventCount(param.getInterval());
        } else if (2 == dateType) {
            logPointDTOS = this.baseMapper.attackEventCountByTwelveMonths(param.getInterval());
        } else {
            logPointDTOS = this.baseMapper.attackEventCountByThirtyDay(param.getInterval());
        }
        return logPointDTOS;
    }

    @Override
    public List<AttackDataDTO> attackData() {
        return this.baseMapper.getAttackData();
    }

    @Override
    public List<LogPointDTO> attackEventCountByWeek() {
        return this.baseMapper.getAttackEventByWeek();
    }

    @Override
    public List<Map<String, Integer>> getThreatLevel() {
        List<Map<String, Object>> threatLevelList = this.baseMapper.getThreatLevel();
        // clickhouse count默认返回的unSignedLong类型，被其他http调用地方时会导致归零
        List<Map<String, Integer>> threatLevels = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(threatLevelList)) {
            for (Map<String, Object> map : threatLevelList) {
                Map<String, Integer> valueMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    valueMap.put(entry.getKey(), Integer.parseInt(String.valueOf(entry.getValue())));
                }
                threatLevels.add(valueMap);
            }
        }
        return threatLevels;
    }

    @Override
    public List<AttackFrequencyDTO> getAttackFrequency() {
        return this.baseMapper.getAttackFrequency();
    }

    @Override
    public List<AttackAddressDataDTO> getAttackDataForTheDay() {
        return this.baseMapper.getAttackDataForTheDay();
    }

    @Override
    public List<ThreatEventDTO> getThreatEventListV2() {
        return this.baseMapper.getThreatEventList();
    }

    @Override
    public List<SyslogAlertEntity> getThreatEventListV1() {
        return this.baseMapper.getThreatEventListV1();
    }
}
