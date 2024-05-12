package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dto.AssetsSyncDelayDTO;
import cn.mw.monitor.assets.dto.AssetsSyncDelayParam;
import cn.mw.monitor.assets.service.MwAssetsSyncDelayService;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author qzg
 * @date 2021/7/2
 */
@Service
@Slf4j
public class MwAssetsSyncDelayImpl implements MwAssetsSyncDelayService {
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public Reply getDelayTable(AssetsSyncDelayParam param) {
        PageInfo pageInfo = new PageInfo<List>();
        MWZabbixAPIResult result = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), param.getHostId(), "Always_On", "同步延迟时间");
        List<AssetsSyncDelayDTO> list = new ArrayList<>();
        if (!result.isFail()) {
            JsonNode datas = (JsonNode) result.getData();
            if (datas != null && datas.size() > 0) {
                String data = datas.get(0).get("lastvalue").asText();
                log.info("同步延迟时间: " + JSON.toJSONString(data));
                list = JSONObject.parseArray(String.valueOf(data), AssetsSyncDelayDTO.class);
            }
            for (AssetsSyncDelayDTO dto : list) {
                if (!Strings.isNullOrEmpty(dto.getDiffMS())) {
                    long time = Integer.valueOf(dto.getDiffMS()) / 1000;
                    String diffMS = SeverityUtils.getLastTime(time);
                    dto.setDiffMS(diffMS);
                }
            }
            PageList pageList = new PageList();
            pageInfo.setTotal(list.size());
            List<AssetsSyncDelayDTO> listByPage = pageList.getList(list, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(listByPage);
        }
        return Reply.ok(pageInfo);
    }

}
