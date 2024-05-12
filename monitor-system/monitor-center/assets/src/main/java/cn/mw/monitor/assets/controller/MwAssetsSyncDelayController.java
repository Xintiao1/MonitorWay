package cn.mw.monitor.assets.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.dto.AssetsSyncDelayParam;
import cn.mw.monitor.assets.service.MwAssetsSyncDelayService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author qzg
 * @date 2021/7/2
 */
@RequestMapping("/mwapi/assetsSyncDelay")
@Controller
@Slf4j
@Api(value = "资产管理同步延时接口", tags = "资产管理同步延时接口")
public class MwAssetsSyncDelayController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("MwAssetsSyncDelayController");
    @Autowired
    MwAssetsSyncDelayService mwAssetsSyncDelayService;

    @PostMapping("/getDelayTable/browse")
    @ResponseBody
    @ApiOperation("资产管理同步延时数据接口")
    public ResponseBase getDelayTable(@RequestBody AssetsSyncDelayParam param) {
        Reply reply;
        try {
            reply = mwAssetsSyncDelayService.getDelayTable(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产管理同步延时失败", e);
            return setResultFail("资产管理同步延时", "");
        }
    }

}
