package cn.mw.monitor.model.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.model.dto.UpdateCustomPageByModelParam;
import cn.mw.monitor.model.param.QueryCustomModelparam;
import cn.mw.monitor.model.service.MwModelCustomcolService;
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

@RequestMapping("/mwapi/model")
@Controller
@Slf4j
@Api(value = "自定义查询接口", tags = "自定义查询接口")
public class MWModelCustomcolController extends BaseApiService {

    @Autowired
    private MwModelCustomcolService mwModelCustomcolService;

    @ApiOperation(value = "自定义列查询")
    @PostMapping("/customcol/browse")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", required = true),
            @ApiImplicitParam(name = "modelId", value = "模型id", paramType = "query", required = true)
    })
    public ResponseBase browseCustomcol(@RequestBody QueryCustomModelparam qParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwModelCustomcolService.selectById(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseCustomcol fail {}",e);
            return setResultFail("自定义列查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "自定义列个性化设置")
    @PostMapping("/customCol/editor")
    @ResponseBody
    public ResponseBase updateCustomcol(@RequestBody UpdateCustomPageByModelParam uParam) {
        Reply reply;
        try {
            reply = mwModelCustomcolService.update(uParam.getModels());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateCustomcol fail {}",e);
            return setResultFail("自定义列个性化设置失败", "");
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "自定义列个性化设置--还原")
    @PostMapping("/customCol/reset")
    @ResponseBody
    public ResponseBase resetCustomcol(@RequestBody UpdateCustomPageByModelParam uParam) {
        Reply reply;
        try {
            reply = mwModelCustomcolService.reset(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("resetCustomcol fail {}",e);
            return setResultFail("自定义列个性化还原设置失败", "");
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
            reply = mwModelCustomcolService.selectByMultiPageId(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseMultiCustomcol fail {}",e);
            return setResultFail("自定义列查询失败", "");
        }
        return setResultSuccess(reply);
    }

}
