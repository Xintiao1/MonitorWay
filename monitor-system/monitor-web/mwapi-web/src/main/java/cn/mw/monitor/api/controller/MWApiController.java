package cn.mw.monitor.api.controller;

import cn.joinhealth.echarts.utils.QueryUtils;
import cn.joinhealth.monitor.assets.dto.ItemDTO;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.service.HardwareService;
import cn.mw.monitor.assets.service.impl.HardwareServiceImpl;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.api.param.assets.QueryDiskUsageParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * api管理
 * @auth dev
 * @desc
 * @date 2020/2/4
 */
@RequestMapping("/mwapi")
@Controller
public class MWApiController extends BaseApiService {

    /**
     * 查询监控项信息
     */
    @PostMapping("/itemlist")
    @ResponseBody
    public ResponseBase itemList(HttpServletRequest request) {
        Map<String, Object> conditionMap = QueryUtils.getParams(request);
        if(conditionMap.size()==0){
            return setResultFail("参数不对", null);
        }

        HardwareService hardwareService = SpringUtils.getBeanByName(HardwareServiceImpl.class);
        hardwareService.init(conditionMap);
        List<ItemDTO> data = hardwareService.getHardwareInfo();
        return setResultSuccess(data);
    }

    /**
     * 临时查询各个主机磁盘利用率信息
     */
    @PostMapping("/diskusage")
    @ResponseBody
    public ResponseBase diskusage(@RequestBody QueryDiskUsageParam queryDiskUsageParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {

        if(null == queryDiskUsageParam && null == queryDiskUsageParam.getGroupIds()){
            return setResultFail("参数不对",null);
        }

        HardwareService hardwareService = SpringUtils.getBeanByName(HardwareServiceImpl.class);
        hardwareService.getDiskInfoByGroup(queryDiskUsageParam.getGroupIds());

        return setResultSuccess(null);
    }
}
