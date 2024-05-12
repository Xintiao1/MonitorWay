package cn.mw.monitor.smartdisc.controller;

import cn.mw.monitor.annotation.MwSysLog;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.smartDiscovery.api.MWNmapGroupService;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapResultParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "Nmap节点组",tags = "Nmap节点组")
public class MWNmapGroupController extends BaseApiService {

    @Resource
    private MWNmapGroupService mwNmapGroupService;

    @ApiOperation(value="查看节点组下拉框")
    @MwSysLog("查看节点组下拉框")
    @PostMapping("/nmapGroup/dropdown/nodeGroupBrowse")
    @ResponseBody
    public ResponseBase nodeDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwNmapGroupService.getDropDownNodeGroup();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value="查看端口组下拉框")
    @MwSysLog("查看端口组下拉框")
    @PostMapping("/nmapGroup/dropdown/portGroupBrowse")
    @ResponseBody
    public ResponseBase portDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwNmapGroupService.getDropDownPortGroup();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value="查看存活探测节点组下拉框")
    @MwSysLog("查看存活探测节点组下拉框")
    @PostMapping("/nmapGroup/dropdown/liveNodeGroupBrowse")
    @ResponseBody
    public ResponseBase liveNodeDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwNmapGroupService.getDropDownLiveNodeGroup();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value="查看指纹探测节点组下拉框")
    @MwSysLog("查看指纹探测节点组下拉框")
    @PostMapping("/nmapGroup/dropdown/fingerNodeGroupBrowse")
    @ResponseBody
    public ResponseBase fingerNodeDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwNmapGroupService.getDropDownFingerNodeGroup();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value="查看例外IP节点组下拉框")
    @MwSysLog("查看例外IP节点组下拉框")
    @PostMapping("/nmapGroup/dropdown/exceptionNodeGroupBrowse")
    @ResponseBody
    public ResponseBase excepitonNodeDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwNmapGroupService.getDropDownExceptionNodeGroup();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }
}
