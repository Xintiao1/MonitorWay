package cn.mw.xiangtai.plugin.mapper;

import cn.mw.xiangtai.plugin.domain.dto.*;
import cn.mw.xiangtai.plugin.domain.entity.SyslogAlertEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SyslogAlertMapper extends BaseMapper<SyslogAlertEntity> {

    List<AttackSourceDTO> attackSourceTopN(@Param("topN") Integer topN);

    List<AttackTypeDTO> attackTypeTopN(@Param("topN") Integer topN);

    // 五年 总数
    List<LogPointDTO> logEventCount(@Param("interval") Integer interval);
    // 12月 总数
    List<LogPointDTO> logEventCountByTwelveMonths(@Param("interval") Integer interval);
    // 30天 总数
    List<LogPointDTO> logEventCountByThirtyDay(@Param("interval") Integer interval);

    // 五年 攻击
    List<LogPointDTO> attackEventCount(@Param("interval") Integer interval);
    // 12个月 攻击
    List<LogPointDTO> attackEventCountByTwelveMonths(@Param("interval") Integer interval);
    // 30天 攻击
    List<LogPointDTO> attackEventCountByThirtyDay(@Param("interval") Integer interval);

    List<AttackDataDTO> getAttackData();

    List<LogPointDTO> getAttackEventByWeek();

    @MapKey("alertLevel")
    List<Map<String, Object>> getThreatLevel();

    List<AttackFrequencyDTO> getAttackFrequency();

    List<AttackAddressDataDTO> getAttackDataForTheDay();

    List<ThreatEventDTO> getThreatEventList();

    List<SyslogAlertEntity> getThreatEventListV1();
}
