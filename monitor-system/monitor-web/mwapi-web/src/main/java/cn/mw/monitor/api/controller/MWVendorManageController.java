package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.vendor.api.MWVendorDropDown;
import cn.mw.monitor.vendor.param.AddOrUpdateVendorManageParam;
import cn.mw.monitor.vendor.param.QueryVendorManageParam;
import cn.mw.monitor.vendor.service.MwVendorManageService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

/**
 * @author syt
 * @Date 2021/1/20 14:57
 * @Version 1.0
 */
@RequestMapping("/mwapi/vendorManage")
@Controller
@Slf4j
public class MWVendorManageController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWVendorManageController.class.getName());

    @Autowired
    MwVendorManageService mwVendorManageService;
    @Autowired
    MWVendorDropDown mwVendorDropDown;

    /**
     * 新增厂商规格型号
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation(value = "新增厂商规格型号数据")
    public ResponseBase addVendorMange(@RequestBody AddOrUpdateVendorManageParam aParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorManageService.insert(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.VENDOR_MANAGE_INSERT_MSG_312003, e);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除厂商规格型号
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除厂商规格型号数据")
    public ResponseBase deleteVendorManage(@RequestBody QueryVendorManageParam dParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorManageService.delete(dParam.getIds(),dParam.getVendorIds());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.VENDOR_MANAGE_DELETE_MSG_312004, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.VENDOR_MANAGE_DELETE_MSG_312004, e);
        }
        return setResultSuccess(reply);
    }

    /**
     * 修改厂商规格型号
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/editor")
    @ResponseBody
    @ApiOperation(value = "修改厂商规格型号数据")
    public ResponseBase updateVendorMange(@RequestBody AddOrUpdateVendorManageParam auParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorManageService.update(auParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.VENDOR_MANAGE_UPDATE_MSG_312002, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.VENDOR_MANAGE_UPDATE_MSG_312002, e);
        }
        return setResultSuccess(reply);
    }

    /**
     * 分页查询厂商规格型号
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "分页查询厂商规格型号数据")
    public ResponseBase browseVendorManage(@RequestBody QueryVendorManageParam qParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), qParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 厂商下拉框查询
     */
    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/dropdown/browse")
    @ResponseBody
    @ApiOperation(value = "厂商下拉框查询")
    public ResponseBase VendorDropdownBrowse(@PathParam("specification") String specification, @PathParam("selectFlag") boolean selectFlag, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorDropDown.selectVendorDropdownList(specification, selectFlag);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 根据厂商查询规格型号下拉框
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/model/dropdown/browse")
    @ResponseBody
    @ApiOperation(value = "根据厂商查询规格型号下拉框")
    public ResponseBase modelDropdownByVendor(@RequestBody QueryVendorManageParam qParam, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorDropDown.selectVModelDropdownList(qParam.getBrand());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 厂商规格型号子页面查询
     */
    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/popup/browse")
    @ResponseBody
    @ApiOperation(value = "厂商规格型号子页面查询")
    public ResponseBase VendorManagePopupBrowse(@PathParam("id") Integer id,
                                                HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwVendorManageService.selectById(id);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

}
