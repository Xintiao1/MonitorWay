package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.visualized.dto.MwVisualizedAssetsDto;
import cn.mw.monitor.visualized.dto.MwVisualizedIndexDto;
import cn.mw.monitor.visualized.service.MwVisualizedMenuService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName MwVisualizedChartController
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/15 11:00
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "可视化菜单接口")
public class MwVisualizedMenuController extends BaseApiService {

    @Autowired
    private MwVisualizedMenuService visualizedChartService;

    /**
     * 查询基线的监控项数据
     */
    @PostMapping("/visualized/chart/browse")
    @ResponseBody
    @ApiOperation("可视化菜单查询")
    public ResponseBase selectVisualizedChart() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedChartService.selectVisualizedChart();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化菜单查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化菜单查询失败", null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 查询基线的监控项数据
     */
    @PostMapping("/visualized/dimension/browse")
    @ResponseBody
    @ApiOperation("可视化维度查询")
    public ResponseBase selectVisualizedDimension() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedChartService.selectVisualizedDimension();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化维度查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化维度查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询基线的监控项数据
     */
    @PostMapping("/visualized/index/browse")
    @ResponseBody
    @ApiOperation("可视化指标查询")
    public ResponseBase selectVisualizedIndex(@RequestBody MwVisualizedIndexDto dtos) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedChartService.selectVisualizedIndex(dtos);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化指标查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化指标查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询基线的监控项数据
     */
    @PostMapping("/visualized/imageAndTextArea/upload")
    @ResponseBody
    @ApiOperation("可视化图文区图片上传")
    public ResponseBase visualizedImageAndTextAreaUpload(@RequestBody MultipartFile file) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedChartService.visualizedImageAndTextAreaUpload(file);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化图文区图片上传失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化图文区图片上传失败", null);
        }
        return setResultSuccess(reply);
    }
}
