package cn.mw.xiangtai.plugin.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.domain.vo.ComprehensiveSituationDataVO;
import cn.mw.xiangtai.plugin.domain.vo.ThreatSituationDataVO;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModuleService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mwapi/visualized/xiangtai")
@Slf4j
@Api(value = "祥泰大屏数据接口", tags = "祥泰大屏数据接口")
public class XiangtaiVisualizedController extends BaseApiService {

    private final XiangtaiVisualizedService visualizedService;

    @Autowired
    private XiangtaiVisualizedModuleService visualizedModuleService;

    @Autowired
    public XiangtaiVisualizedController(XiangtaiVisualizedService visualizedService) {
        this.visualizedService = visualizedService;
    }

    @ApiOperation("威胁态势-自测接口入口")
    @PostMapping("/getThreatSituationData")
    public ResponseBase<Map<String, Object>> getThreatSituationData() {
        ThreatSituationDataVO data = visualizedService.getThreatSituationData();
        ComprehensiveSituationDataVO vo = visualizedService.getComprehensiveSituationData();
        Map<String, Object> map = new HashMap<>();
        map.put("threat", data);
        map.put("comprehen", vo);
        return this.setResultSuccess(map);
    }


    @ApiOperation("祥泰大屏接口")
    @ResponseBody
    @PostMapping("/getXiangtaiVisualizedData")
    public ResponseBase getXiangtaiVisualizedData(@RequestBody XiangtaiVisualizedParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedModuleService.getXiangtaiVisualizedData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("XiangtaiVisualizedController{} getXiangtaiVisualizedData() error","");
        }
        return setResultSuccess(reply);
    }

}
