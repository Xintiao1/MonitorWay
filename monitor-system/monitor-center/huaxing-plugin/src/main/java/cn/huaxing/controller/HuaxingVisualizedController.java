package cn.huaxing.controller;

import cn.huaxing.param.HuaxingVisualizedParam;
import cn.huaxing.service.HuaxingVisualizedService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author gengjb
 * @date 2023/9/4 14:20
 */
@RestController
@RequestMapping("/mwapi/visualized/huaxing")
@Slf4j
public class HuaxingVisualizedController extends BaseApiService {

    @Autowired
    private HuaxingVisualizedService huaxingVisualizedService;

    @PostMapping("/getHuaxingData")
    @ResponseBody
    public ResponseBase getHuaxingDataInfo(@RequestBody HuaxingVisualizedParam huaxingVisualizedParam) {
        Reply reply;
        try {
            reply =huaxingVisualizedService.getHuaxingDataBaseInfo(huaxingVisualizedParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("HuaxingVisualizedController{} getHuaxingDataInfo()", e);
            return setResultFail("HuaxingVisualizedController{} getHuaxingDataInfo()", "");
        }
    }
}
