package cn.mw.monitor.dev.controller;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.role.AddUpdateModuleParam;
import cn.mw.monitor.api.param.role.DeleteModuleParam;
import cn.mw.monitor.dev.service.MWModuleService;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "模块管理接口",tags = "模块管理接口")
public class MWModuleController extends BaseApiService {

    @Autowired
    private MWModuleService mwModuleService;


    @ApiOperation(value="模块新增")
    @PostMapping("/module/create")
    @ResponseBody
    public ResponseBase addModule(@Validated({Insert.class})@RequestBody AddUpdateModuleParam mParam, HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.insertRoleModule(mParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), mParam);
        }
    }

    @ApiOperation(value="删除模块")
    @PostMapping("/module/delete")
    @ResponseBody
    public ResponseBase delModule(@Validated @RequestBody DeleteModuleParam dParam, HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.deleteRoleModule(dParam.getIds());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), dParam);
        }
    }

    @ApiOperation(value="模块编辑")
    @PostMapping("/module/editor")
    @ResponseBody
    public ResponseBase updateModule(@Validated({Update.class}) @RequestBody AddUpdateModuleParam mParam,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.updateRoleModule(mParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), mParam);
        }
    }

    @ApiOperation(value="模块查询")
    @PostMapping("/module/browse")
    @ResponseBody
    public ResponseBase browseModule(@RequestBody AddUpdateModuleParam aParam,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.selectRoleModule(aParam.getId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), aParam);
        }
    }

}
