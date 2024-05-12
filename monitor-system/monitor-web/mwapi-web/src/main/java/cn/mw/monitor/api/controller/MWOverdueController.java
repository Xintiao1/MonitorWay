package cn.mw.monitor.api.controller;


import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.weixin.entity.MwOverdueTable;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.weixin.service.WxPortalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Api(value = "过期告警")
public class MWOverdueController extends BaseApiService{

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    @Autowired
    private WxPortalService wxPortalService;

    @PostMapping("/overdue/editor")
    @ResponseBody
    @ApiOperation(value = "过期消息发送")
    public ResponseBase update(@RequestBody MwOverdueTable param) {
        Reply reply;
        try {
            reply = wxPortalService.update(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/overdue/delete")
    @ResponseBody
    @ApiOperation(value = "过期消息删除")
    public ResponseBase delete(@RequestBody List<MwOverdueTable> list) {
        Reply reply;
        try {
            reply = wxPortalService.delete(list);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), list);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/overdue/browse")
    @ResponseBody
    @ApiOperation(value = "过期消息查询")
    public ResponseBase browse(@RequestBody MwOverdueTable param) {
        Reply reply;
        try {
            reply = wxPortalService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

}
