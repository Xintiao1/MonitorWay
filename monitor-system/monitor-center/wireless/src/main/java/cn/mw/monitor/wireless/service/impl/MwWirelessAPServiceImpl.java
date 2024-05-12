package cn.mw.monitor.wireless.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.service.server.api.dto.ApplicationTableInfos;
import cn.mw.monitor.service.server.api.MyMonitorCommons;
import cn.mw.monitor.wireless.api.param.QueryWirelessAPParam;
import cn.mw.monitor.wireless.service.MwWirelessAPService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2021/6/21 15:15
 * @Version 1.0
 */
@Service
@Slf4j
public class MwWirelessAPServiceImpl implements MwWirelessAPService {
    @Autowired
    private MyMonitorCommons myMonitorCommons;

    @Override
    public Reply getAPTableInfo(QueryWirelessAPParam param) {
        try {
            ApplicationTableInfos tableInfos = myMonitorCommons.getApplicationTableInfos(param);
            if (param.isLimitFlag()) {
                Map<String, List<Map<String, Object>>> allData = tableInfos.getAllData();
                Map<String, PageInfo> pageInfoData = new HashMap<>();
                for (Map.Entry<String, List<Map<String, Object>>> entry : allData.entrySet()) {
                    PageInfo pageInfo = new PageInfo<List>();
                    PageList pageList = new PageList();
                    List<Map<String, Object>> value = entry.getValue();
                    if (value != null && value.size() > 0) {
                        //根据前端传的查询参数过滤出符合的数据
                        String queryName = param.getQueryName();
                        String queryValue = param.getQueryValue();
                        if (param.getQueryName() != null && StringUtils.isNotEmpty(param.getQueryName()) && param.getQueryValue() != null) {
                            value = value.stream().filter(e -> e.get(queryName).toString().trim().indexOf(queryValue.trim()) != -1).collect(Collectors.toList());
                        }

                        // 表格排序
                        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField()) && param.getSortMode() != null) {
                            value = sortMapList(value, param.getSortField(), param.getSortMode());
                        }

                        pageInfo.setTotal(value.size());
                    }
                    List list = pageList.getList(value, param.getPageNumber(), param.getPageSize());
                    pageInfo.setList(list);
                    pageInfoData.put(entry.getKey(), pageInfo);
                }
                tableInfos.setPageInfo(pageInfoData);
            } else {
                Map<String, List<Map<String, Object>>> allData = tableInfos.getAllData();
                for (Map.Entry<String, List<Map<String, Object>>> entry : allData.entrySet()) {
                    List<Map<String, Object>> value = entry.getValue();
                    if (value != null && value.size() > 0) {
                        //根据前端传的查询参数过滤出符合的数据
                        String queryName = param.getQueryName();
                        String queryValue = param.getQueryValue();
                        if (param.getQueryName() != null && StringUtils.isNotEmpty(param.getQueryName()) && param.getQueryValue() != null) {
                            value = value.stream().filter(e -> e.get(queryName).toString().trim().indexOf(queryValue.trim()) != -1).collect(Collectors.toList());
                        }

                        // 表格排序
                        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField()) && param.getSortMode() != null) {
                            value = sortMapList(value, param.getSortField(), param.getSortMode());
                        }
                        // 放回
                        allData.put(entry.getKey(),value);
                    }
                }
            }
            return Reply.ok(tableInfos);
        } catch (Exception e) {
            log.error("fail to getAPTableInfo with param={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.WIRELESS_AP_SELECT_CODE_319001, ErrorConstant.WIRELESS_AP_SELECT_MSG_319001);
        }
    }

    private List<Map<String, Object>> sortMapList(List<Map<String, Object>> list, String sortField, Integer sortMode) {
        String sort = "sort" + sortField;
        List<Map<String, Object>> sortList = list;
        if (list != null && list.size() > 0) {
            Map<String, Object> map = list.get(0);
            if (map.get("sort" + sortField) != null) {
                if (sortMode == 0) {
                    sortList = list.stream().sorted((v1, v2) -> {
                        Double d1 = Double.parseDouble(v1.get(sort).toString());
                        Double d2 = Double.parseDouble(v2.get(sort).toString());
                        if (d1 != null) {
                            return d1.compareTo(d2);
                        }
                        return 0;
                    }).collect(Collectors.toList());
                } else {
                    sortList = list.stream().sorted((v1, v2) -> {
                        Double d1 = Double.parseDouble(v1.get(sort).toString());
                        Double d2 = Double.parseDouble(v2.get(sort).toString());
                        if (d2 != null) {
                            return d2.compareTo(d1);
                        }
                        return 0;
                    }).collect(Collectors.toList());
                }
            } else {
                if (sortMode == 0) {
                    sortList = list.stream().sorted((v1, v2) -> {
                        String d1 = v1.get(sortField).toString();
                        String d2 = v2.get(sortField).toString();
                        if (d1 != null) {
                            return d1.compareTo(d2);
                        }
                        return 0;
                    }).collect(Collectors.toList());
                } else {
                    sortList = list.stream().sorted((v1, v2) -> {
                        String d1 = v1.get(sortField).toString();
                        String d2 = v2.get(sortField).toString();
                        if (d2 != null) {
                            return d2.compareTo(d1);
                        }
                        return 0;
                    }).collect(Collectors.toList());
                }
            }
        }
        return sortList;
    }
}
