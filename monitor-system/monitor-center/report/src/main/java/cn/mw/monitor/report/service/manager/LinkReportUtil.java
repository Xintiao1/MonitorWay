package cn.mw.monitor.report.service.manager;

import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.report.dto.linkdto.MwHistoryDto;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.NewUnits;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWZabbixAPIResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhy
 * @date 2020/12/28 11:12
 */
public class LinkReportUtil {
    /**
     * 对zabbix中的历史数据做转换
     * @param historyRsult
     * @return
     */
    public static  List<MwHistoryDto> getMwHistoryDto(MWZabbixAPIResult historyRsult) {
        List<MwHistoryDto> list = new ArrayList<>();
        if (historyRsult.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) historyRsult.getData();
            if (jsonNode.size() > 0) {
                jsonNode.forEach(node -> {
                    MwHistoryDto historyDto = MwHistoryDto.builder()
                            .date(MWUtils.getLongToDate(node.get("clock").asLong()))
                            .value(node.get("value").asText())
                            .build();
                    list.add(historyDto);
                });
            }
        }
        return list;
    }

    /*对查询到的历史数据加上单位*/
    public static List<MwHistoryDto> setHistoryValueUnits(List<MwHistoryDto> list, String lastUnits) {
        List<MwHistoryDto> newList = new ArrayList<>();
        if (list.size() > 0) {
            for (MwHistoryDto dto : list) {
                if (null != dto.getValue() && StringUtils.isNotEmpty(dto.getValue())) {
                    String value = UnitsUtil.getValueMap(dto.getValue(), lastUnits, NewUnits.bPS.getUnits()).get("value");
                    MwHistoryDto build = MwHistoryDto.builder().value(value).date(dto.getDate()).build();
                    newList.add(build);
                }
            }
        }
        return newList;
    }
}
