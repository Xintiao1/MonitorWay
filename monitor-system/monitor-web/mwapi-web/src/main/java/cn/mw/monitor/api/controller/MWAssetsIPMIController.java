package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.assets.param.AddUpdateOutbandAssetsParam;
import cn.mw.monitor.assets.api.param.assets.DeleteTangAssetsParam;
import cn.mw.monitor.service.assets.param.QueryOutbandAssetsParam;
import cn.mw.monitor.service.assets.api.MwOutbandAssetsService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.state.DataType;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author syt
 * @Date 2020/6/24 9:52
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(tags = "带外资产管理接口")
public class MWAssetsIPMIController extends BaseApiService {
    @Autowired
    MwOutbandAssetsService mwOutbandAssetsService;

    @Autowired
    MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    LicenseManagementService licenseManagement;

    @Autowired
    CheckCountService checkCountService;

    /**
     * 带外资产新增
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/create")
    @ResponseBody
    @ApiOperation("带外资产新增")
    public ResponseBase addOutbandAssets(
            @ApiParam(value = "新增外带资产数据",required = true)
            @RequestBody AddUpdateOutbandAssetsParam addOutbandAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            //许可校验
            //数量获取
            int aCount = checkCountService.selectAssetsCount(Arrays.asList(1,6,7,8,69), null);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengtAssets(69, aCount, 1);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            reply = mwOutbandAssetsService.insertAssets(addOutbandAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), addOutbandAssetsParam);
        }

        return setResultSuccess(reply);
    }
//
//    /**
//     * 带外资产批量编辑
//     */
//    @PostMapping("/assets/outband/batch/editor")
//    @ResponseBody
//    public ResponseBase batchAddOutbandAssets(@RequestBody AddUpdateOutbandAssetsParam updateOutbandAssetsParam,
//                                           HttpServletRequest request, RedirectAttributesModelMap model){
//        Reply reply;
//        try{
//            reply = mwOutbandAssetsService.batchInsertAssets(updateOutbandAssetsParam);
//            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        }catch (Throwable e){
//            log.error(e.getMessage());
//            return setResultFail(e.getMessage(), updateOutbandAssetsParam);
//        }
//
//        return setResultSuccess(reply);
//    }

    /**
     * 带外资产修改
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/editor")
    @ResponseBody
    @ApiOperation("带外资产修改")
    public ResponseBase updateTangAssets(
            @ApiParam(value = "修改带外资产数据",required = true)
            @RequestBody AddUpdateOutbandAssetsParam updateOutbandAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwOutbandAssetsService.updateAssets(updateOutbandAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.OUTBAND_ASSETS_MSG_210115, reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.OUTBAND_ASSETS_MSG_210115, updateOutbandAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 带外资产删除
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/delete")
    @ResponseBody
    @ApiOperation("删除带外资产")
    public ResponseBase deleteTangAssets(
            @ApiParam(value = "删除带外资产数据",required = true)
            @RequestBody DeleteTangAssetsParam deleteOutbandAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            List<DeleteTangAssetsID> idList = deleteOutbandAssetsParam.getIdList();
            // 验证内容正确性
            reply = mwOutbandAssetsService.deleteAssets(idList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), deleteOutbandAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 带外资产查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/browse")
    @ResponseBody
    @ApiOperation("查询带外资产")
    public ResponseBase browseTangAssets(
            @ApiParam(value = "查询外带资产数据",required = true)
            @RequestBody QueryOutbandAssetsParam browseOutbandAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwOutbandAssetsService.selectList(browseOutbandAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), browseOutbandAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 带外资产编辑框查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/popup/browse")
    @ResponseBody
    @ApiOperation("带外资产编辑框查询")
    public ResponseBase browseTangPopupAssets(
            @ApiParam(value = "查询外带资产数据",required = true)
            @RequestBody QueryOutbandAssetsParam browseOutbandAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwOutbandAssetsService.selectById(browseOutbandAssetsParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), browseOutbandAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 带外资产状态修改
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/perform")
    @ResponseBody
    @ApiOperation("带外资产状态修改")
    public ResponseBase updateTangibleState(
            @ApiParam(value = "更新资产状态数据",required = true)
            @RequestBody UpdateAssetsStateParam updateAssetsStateParam,
                                            HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            reply = mwOutbandAssetsService.updateState(updateAssetsStateParam);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), updateAssetsStateParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 带外资产下拉框查询
     */
    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/assets/outband/dropdown/browse")
    @ResponseBody
    @ApiOperation("查询带外资产下拉框")
    public ResponseBase outBandAssetsDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwOutbandAssetsService.selectDropdownList();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }

        return setResultSuccess(reply);
    }
    /**
     * 带外资产查看标签
     * @param qparam
     * @param request
     * @param model
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/getLabels")
    @ResponseBody
    @ApiOperation("查询带外资产下拉框")
    public ResponseBase getLabels(
            @ApiParam(value = "查询下拉框带外资产数据",required = true)
            @RequestBody QueryTangAssetsParam qparam, HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            List<MwAssetsLabelDTO> lists = mwLabelCommonServcie.getLabelBoard(qparam.getId(), DataType.OUTBANDASSETS.getName());
            reply = Reply.ok(lists);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 带外资产查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/outband/fuzzyQuery")
    @ResponseBody
    @ApiOperation("带外资产模糊查询下拉数据")
    public ResponseBase outBandAssetsFuzzyQuery() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwOutbandAssetsService.outBandAssetsFuzzyQuery();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(),"模糊查询带外资产失败");
        }
        return setResultSuccess(reply);
    }
}
