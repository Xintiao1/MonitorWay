package cn.mw.module.security.control;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.module.security.dto.MessageParam;
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
 * @date 2020/9/11 9:35
 */
@RequestMapping("/mwapi/message")
@Controller
@Slf4j
@Api(value = "eslog", tags = "日志")
public class EsMassageController extends BaseApiService {

    @Autowired(required=false)
    private EslogService eslogService;

    @PostMapping("/browse")
    @ResponseBody
    public ResponseBase getMessageList(@RequestBody MessageParam param) {
        Reply reply;
        try {
            if (null == eslogService){
                log.error("eslogService is null,checkout es component!");
                return setResultSuccess(Reply.ok());
            }
            reply = eslogService.getMessageList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getMessageList",e);
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }



}
