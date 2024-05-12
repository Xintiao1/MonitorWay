package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.screen.dto.LargeScreenMapDto;
import cn.mw.monitor.screen.service.MWLargeScreenMapService;
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

import java.util.List;

/**
 * @ClassName MWLagerScreenMapController
 * @Description 监控大屏地图接口
 * @Author gengjb
 * @Date 2022/9/2 8:41
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Controller
@Api(value = "大屏地图", tags = "大屏地图")
@Slf4j
public class MWLargeScreenMapController extends BaseApiService {

    @Autowired
    private MWLargeScreenMapService lagerScreenMapService;

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/map/getAssets")
    @ResponseBody
    @ApiOperation(value = "大屏地图选择的资产数据")
    public ResponseBase getMapTree() {
        Reply reply;
        try {
            reply = lagerScreenMapService.getScreenMapChoiceInformation();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图选择的资产数据失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/map/data/create")
    @ResponseBody
    @ApiOperation(value = "大屏地图选择的数据新增")
    public ResponseBase createMapData(@RequestBody LargeScreenMapDto screenMapDto) {
        Reply reply;
        try {
            reply = lagerScreenMapService.createScreenMapShowInformation(screenMapDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图选择的数据新增失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/map/data/browse")
    @ResponseBody
    @ApiOperation(value = "大屏地图的展示数据查询")
    public ResponseBase selectMapData(@RequestBody LargeScreenMapDto screenMapDto) {
        Reply reply;
        try {
            reply = lagerScreenMapService.selectScreenMapShowInformation(screenMapDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图的展示数据查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/map/data/editor")
    @ResponseBody
    @ApiOperation(value = "大屏地图的展示数据修改")
    public ResponseBase updateMapData(@RequestBody LargeScreenMapDto screenMapDto) {
        Reply reply;
        try {
            reply = lagerScreenMapService.updateScreenMapShowInformation(screenMapDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图的展示数据修改失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/map/data/delete")
    @ResponseBody
    @ApiOperation(value = "大屏地图的展示数据删除")
    public ResponseBase deleteMapData(@RequestBody LargeScreenMapDto screenMapDto) {
        Reply reply;
        try {
            reply = lagerScreenMapService.deleteScreenMapShowInformation(screenMapDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图的展示数据删除失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/map/data/getItemName")
    @ResponseBody
    @ApiOperation(value = "大屏获取监控项信息")
    public ResponseBase getScreenItemName() {
        Reply reply;
        try {
            reply = lagerScreenMapService.getScreenItemName();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏获取监控项信息失败", "");
        }
        return setResultSuccess(reply);
    }
}
