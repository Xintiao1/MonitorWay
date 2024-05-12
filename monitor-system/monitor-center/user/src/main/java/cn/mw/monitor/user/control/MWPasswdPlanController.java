package cn.mw.monitor.user.control;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.passwdPlan.AddUpdatePasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.DeletePasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.QueryPasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.UpdatePasswdPlanStateParam;
import cn.mw.monitor.user.service.MWPasswordPlanService;
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
@Api(value = "密码策略管理接口", tags = "密码策略管理接口")
public class MWPasswdPlanController extends BaseApiService {

    @Autowired
    private MWPasswordPlanService mwpasswordPlanService;

    @ApiOperation(value = "新增密码策略")
    @PostMapping("/passwdplan/create")
    @ResponseBody
    public ResponseBase addPasswdPlan(@Validated({Insert.class}) @RequestBody AddUpdatePasswdPlanParam passwdPlanParam,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.addPasswordPlan(passwdPlanParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), passwdPlanParam);
        }
    }

    @ApiOperation(value = "删除密码策略")
    @PostMapping("/passwdplan/delete")
    @ResponseBody
    public ResponseBase deletePasswdPlan(@RequestBody DeletePasswdPlanParam dParam,
                                         HttpServletRequest request,
                                         RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.delete(dParam.getPasswdPlanIds());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), dParam);
        }
    }

    @ApiOperation(value="修改密码策略状态")
    @PostMapping("/passwd/perform")
    @ResponseBody
    public ResponseBase updatePasswdState(@Validated @RequestBody UpdatePasswdPlanStateParam dParam,
                                          HttpServletRequest request,
                                          RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.updatePasswdState(dParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), dParam);
        }
    }

    @ApiOperation(value = "更新密码策略")
    @PostMapping("/passwdplan/editor")
    @ResponseBody
    public ResponseBase updPasswdPlan(@Validated({Update.class})@RequestBody AddUpdatePasswdPlanParam passwdPlanParam,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.updatePasswordPlan(passwdPlanParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), passwdPlanParam);
        }
    }

    @ApiOperation(value = "分页查找密码策略")
    @PostMapping("/passwdplan/browse")
    @ResponseBody
    public ResponseBase selectList(@RequestBody QueryPasswdPlanParam qParam,
                                   HttpServletRequest request,
                                   RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.selectList(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="通过ID查询密码策略")
    @PostMapping(value = "/passwd/popup/browse")
    @ResponseBody
    public ResponseBase selectPopupById(@RequestBody QueryPasswdPlanParam qParam,
                                          HttpServletRequest request,
                                          RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.selectPopupById(qParam.getPasswdId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value = "密码策略下拉框查询")
    @PostMapping("/passwdplan/dropdown/browse")
    @ResponseBody
    public ResponseBase orgDropdownBrowse(HttpServletRequest request,
                                          RedirectAttributesModelMap model) {
        try {
            Reply reply = mwpasswordPlanService.selectDropdownList();
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(),null);
        }
    }

}
