package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.api.param.UpdateCustomPageParam;
import cn.mw.monitor.customPage.service.MwCustomcolService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "自定义查询接口", tags = "自定义查询接口")
public class MWCustomcolController extends BaseApiService {

    @Autowired
    private MwCustomcolService mwCustomcolService;

    @ApiOperation(value = "自定义列查询")
    @PostMapping("/customcol/browse")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", required = true),
            @ApiImplicitParam(name = "pageId", value = "页面id", paramType = "query", required = true)
    })
    public ResponseBase browseCustomcol(@RequestBody QueryCustomPageParam qParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwCustomcolService.selectById(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "自定义列个性化设置")
    @PostMapping("/customcol/editor")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "models", value = "个性化设置参数", paramType = "query", required = true),
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", required = true),
            @ApiImplicitParam(name = "pageId", value = "页面id", paramType = "query", required = true)
    })
    public ResponseBase updateCustomcol(@RequestBody UpdateCustomPageParam uParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwCustomcolService.update(uParam.getModels());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), uParam);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "自定义列个性化设置--还原")
    @PostMapping("/customcol/reset")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "models", value = "个性化设置参数", paramType = "query", required = true),
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", required = true),
            @ApiImplicitParam(name = "pageId", value = "页面id", paramType = "query", required = true)
    })
    public ResponseBase resetCustomcol(@RequestBody UpdateCustomPageParam uParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwCustomcolService.reset(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), uParam);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "多个页面id自定义列查询")
    @PostMapping("/custom-multi-col/browse")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", required = true),
            @ApiImplicitParam(name = "pageIds", value = "页面id集合", paramType = "query", required = true)
    })
    public ResponseBase browseMultiCustomcol(@RequestBody QueryCustomMultiPageParam qParam,
                                             HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwCustomcolService.selectByMultiPageId(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
        return setResultSuccess(reply);
    }

}
