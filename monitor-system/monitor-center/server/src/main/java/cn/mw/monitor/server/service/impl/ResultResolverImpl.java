package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.TPServer.model.TPServerTypeEnum;
import cn.mw.monitor.server.serverdto.ApplicationDTO;
import cn.mw.monitor.server.serverdto.ApplicationDTOV6;
import cn.mw.monitor.server.serverdto.ZbApplicationNameEnum;
import cn.mw.monitor.server.service.ResultResolver;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gui.quanwang
 * @className ResultResolverImpl
 * @description 解析数据
 * @date 2023/2/13
 */
@Slf4j
@Service
public class ResultResolverImpl implements ResultResolver {

    /**
     * zabbix高于5.2版本，无应用集后的获取应用集方式
     */
    private final static String HIGH_ZABBIX_VERSION_TAG = "Application";

    /**
     * 解析数据
     *
     * @param serverType 服务器类别
     * @param data       原始数据
     * @return
     */
    @Override
    public List<ApplicationDTO> analysisResult(TPServerTypeEnum serverType, String data) {
        List<ApplicationDTO> list = new ArrayList<>();
        try {
            switch (serverType) {
                case Zabbix6_0:
                    list = new ArrayList<>();
                    List<ApplicationDTOV6> appList = JSONArray.parseArray(data, ApplicationDTOV6.class);
                    Map<String, List<String>> appMap = new HashMap<>();
                    List<String> itemList;
                    String tagKey;
                    for (ApplicationDTOV6 app : appList) {
                        for (ApplicationDTOV6.TagV6 tag : app.getTags()) {
                            if (HIGH_ZABBIX_VERSION_TAG.equalsIgnoreCase(tag.getTag())) {
                                tagKey = tag.getValue();
                                if (appMap.containsKey(tagKey)) {
                                    itemList = appMap.get(tagKey);
                                    itemList.add(app.getItemid());
                                } else {
                                    itemList = new ArrayList<>();
                                    itemList.add(app.getItemid());
                                    appMap.put(tagKey, itemList);
                                }
                            }
                        }
                    }
                    ApplicationDTO app;
                    String chName;
                    for (String appKey : appMap.keySet()) {
                        app = new ApplicationDTO();
                        chName = ZbApplicationNameEnum.getChName(appKey);
                        itemList = appMap.get(appKey);
                        app.setChName(chName == null ? appKey : chName);
                        app.setCount(itemList.size());
                        app.setItemIds(itemList);
                        app.setName(appKey);
                        list.add(app);
                    }
                    break;
                case Zabbix5_0:
                case Zabbix4_0:
                case Zabbix3_0:
                    list = JSONArray.parseArray(data, ApplicationDTO.class);
                    if (list.size() > 0) {
                        String cnName = "";
                        for (ApplicationDTO application : list) {
                            try {
                                cnName = ZbApplicationNameEnum.valueOf(application.getName()).getChName();
                            } catch (Exception e) {
                                log.info("fail to getApplicationChName with ZbApplicationNameEnum = " +
                                        application.getName(), e);
                                cnName = application.getName();
                            }
                            application.setChName(cnName);
                            application.setCount(application.getItems().size());
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("解析数据失败", e);
        }
        return list;
    }
}
