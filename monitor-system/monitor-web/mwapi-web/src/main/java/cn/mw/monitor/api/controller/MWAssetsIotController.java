package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.api.param.assets.AddUpdateAssetsIotParam;
import cn.mw.monitor.assets.dto.SoundParam;
import cn.mw.monitor.assets.service.MwAssetsIotService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xhy
 * @date 2020/6/6 10:29
 */
@RequestMapping("/mwapi/assetsIot")
@Controller
@Slf4j
public class MWAssetsIotController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWAssetsIotController.class.getName());

    @Autowired
    private MwAssetsIotService mwAssetsIotService;

    @Autowired
    private MwServerService service;
    /**
     * 查看当前资产的阈值
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/perform")
    @ResponseBody
    public ResponseBase selectThreshold(@RequestParam String assetsId){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsIotService.selectThreshold(assetsId);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsIotController{} selectThreshold() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改温湿度阈值
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/popup/editor")
    @ResponseBody
    public ResponseBase updateAssetsIot(@RequestBody AddUpdateAssetsIotParam auParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsIotService.aupdate(auParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsIotController{} updateAssetsIot() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改/添加声音告警
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/editor")
    @ResponseBody
    public ResponseBase updateVoice(@RequestBody SoundParam soundParam,
                                    HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsIotService.updateVoice(soundParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsIotController{} updateVoice() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 查看温湿度告警列表
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/browse")
    @ResponseBody
    public ResponseBase selectAssetsIot( @RequestBody AddUpdateAssetsIotParam param, HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply = null;
        try{
            // 验证内容正确性
            reply = mwAssetsIotService.selectList(param);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsIotController{} selectAssetsIot() error", "");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation("获取IOT分类列表")
    @GetMapping("/iotTypeList")
    @ResponseBody
    public ResponseBase selectTypeList(HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply = null;
        try {
            reply = mwAssetsIotService.selectIotTypeList();
        } catch (Throwable e) {
            logger.error("selectTypeList error :"+e.getMessage());
            return setResultFail("MWAssetsIotController{} selectTypeList() error", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @ApiOperation("获取通道信息列表")
    @PostMapping("/channelInfoList")
    @ResponseBody
    public ResponseBase getChannelInfoList(
            @ApiParam(value = "资产分页数据",required = true)
            @RequestBody AssetsIdsPageInfoParam param){
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getChannelInfoList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("MWAssetsIotController{} getChannelInfoList() error", "");
        }
        return setResultSuccess(reply);
    }
}
