package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.assets.param.QueryAssetsTypeParam;
import cn.mw.monitor.service.timetask.api.MwBaseLineValueService;
import cn.mw.monitor.timetask.entity.MwBaseLineHealthValueDto;
import cn.mw.monitor.timetask.entity.MwBaseLineItemNameDto;
import cn.mw.monitor.timetask.entity.MwBaseLineManageDto;
import cn.mw.monitor.timetask.service.MwBaseLineService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
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

/**
 * @ClassName MwBaseLineControlle
 * @Description 基线模块
 * @Author gengjb
 * @Date 2022/3/31 13:03
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "基线模块")
public class MwBaseLineControlle extends BaseApiService {

    @Autowired
    private MwBaseLineService service;

    @Autowired
    private MwBaseLineValueService lineValueService;

    /**
     * 查询基线的监控项数据
     */
    @PostMapping("/baseline/getItemName")
    @ResponseBody
    @ApiOperation("基线监控项查询")
    public ResponseBase getItemName(@RequestBody MwBaseLineItemNameDto baseLineItemNameDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getItemName(baseLineItemNameDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} getItemName() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 添加基线数据
     */
    @PostMapping("/baseline/create")
    @ResponseBody
    @ApiOperation("基线新增")
    public ResponseBase createBaseLine(@RequestBody MwBaseLineManageDto baseLineManageDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.addBaseLineData(baseLineManageDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} createBaseLine() error", "");
        }
        return setResultSuccess(reply);
    }


    /**
     * 修改基线数据
     */
    @PostMapping("/baseline/editor")
    @ResponseBody
    @ApiOperation("基线修改")
    public ResponseBase updateBaseLine(@RequestBody MwBaseLineManageDto baseLineManageDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.updateBaseLineData(baseLineManageDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} updateBaseLine() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除基线数据
     */
    @PostMapping("/baseline/delete")
    @ResponseBody
    @ApiOperation("基线删除")
    public ResponseBase deleteBaseLine(@RequestBody MwBaseLineManageDto baseLineManageDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.deleteBaseLineData(baseLineManageDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} deleteBaseLine() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询基线数据
     */
    @PostMapping("/baseline/browse")
    @ResponseBody
    @ApiOperation("基线查询")
    public ResponseBase selectBaseLine(@RequestBody MwBaseLineManageDto baseLineManageDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.selectBaseLineData(baseLineManageDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} selectBaseLine() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询基线数据
     */
    @PostMapping("/baseline/getHealthValue")
    @ResponseBody
    @ApiOperation("基线查询健康值")
    public ResponseBase selectHealthValue(@RequestBody MwBaseLineHealthValueDto healthValueDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = lineValueService.selectBaseLineHealthValue(healthValueDto.getNames(),healthValueDto.getAssetsId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} selectHealthValue() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询基线数据
     */
    @PostMapping("/baseline/getHealthValueByAssets")
    @ResponseBody
    @ApiOperation("基线查询健康值")
    public ResponseBase selectHealthValueByAssets() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = lineValueService.selectHealthValueByAssets();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MwBaseLineControlle{} selectHealthValueByAssets() error", "");
        }
        return setResultSuccess(reply);
    }
}
