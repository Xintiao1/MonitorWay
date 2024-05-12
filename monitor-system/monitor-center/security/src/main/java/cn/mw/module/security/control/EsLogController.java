package cn.mw.module.security.control;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.module.security.dto.AddEsLogParam;
import cn.mw.module.security.dto.EslogParam;
import cn.mw.module.security.dto.EslogUpdateParam;
import cn.mw.module.security.service.EslogService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author xhy
 * @date 2020/9/6 17:55
 */
@RequestMapping("/mwapi/security")
@Controller
@Slf4j
@Api(value = "eslog", tags = "日志")
public class EsLogController extends BaseApiService {
    @Autowired(required=false)
    private EslogService eslogService;


    @PostMapping("/create")
    @ResponseBody
    public ResponseBase creatLog(@RequestBody AddEsLogParam param) {
        Reply reply;
        try {
            reply = eslogService.creatLog(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("creatLog {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    @PostMapping("/browse")
    @ResponseBody
    public ResponseBase getLogList(@RequestBody EslogParam param) {
        Reply reply;
        try {
            reply = eslogService.getLogList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getLogList {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/popup/editor")
    @ResponseBody
    public ResponseBase updateLogList(@RequestBody EslogUpdateParam param) {
        Reply reply;
        try {
            reply = eslogService.updateLogList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateLogList {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }



}
