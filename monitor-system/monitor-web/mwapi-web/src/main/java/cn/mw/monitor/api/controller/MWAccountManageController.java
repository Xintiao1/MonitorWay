package cn.mw.monitor.api.controller;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.accountmanage.entity.AddAccountManageParam;
import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.accountmanage.service.MwAccountManageService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "账号管理")
public class MWAccountManageController extends BaseApiService {

    @Autowired
    private MwAccountManageService mwAccountManageService;

    @PostMapping("/accountManage/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "账号管理编辑前查询")
    public ResponseBase editorBrowse(@RequestBody QueryAccountManageParam param) {
        Reply reply;
        try {
            reply = mwAccountManageService.selectList1(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/accountManage/browse")
    @ResponseBody
    @ApiOperation(value = "账号管理查询")
    public ResponseBase browse(@RequestBody QueryAccountManageParam param) {
        Reply reply;
        try {
            reply = mwAccountManageService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/accountManage/delete")
    @ResponseBody
    @ApiOperation(value = "账号管理删除")
    public ResponseBase delete(@RequestBody List<Integer> list) {
        Reply reply;
        try {
            reply = mwAccountManageService.delete(list);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), list);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/accountManage/editor")
    @ResponseBody
    @ApiOperation(value = "账号管理修改")
    public ResponseBase update(@Validated @RequestBody AddAccountManageParam addUpdateIpAddressManageParam) {
        Reply reply;
        try {
            reply = mwAccountManageService.update(addUpdateIpAddressManageParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), addUpdateIpAddressManageParam);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "新增账号管理")
    @PostMapping("/accountManage/create")
    @ResponseBody
    public ResponseBase add(@Validated @RequestBody AddAccountManageParam param,
                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwAccountManageService.insert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "新增账号管理")
    @RequestMapping(value = "/accountManage/dropDownBrowse",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseBase selectDrop() {
        Reply reply;
        try {
            reply = mwAccountManageService.selectDrop();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(),null);
        }
        return setResultSuccess(reply);
    }



}
