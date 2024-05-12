package cn.mw.monitor.assets.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.param.QueryAssetsLabelParam;
import cn.mw.monitor.assets.service.MwAssetsLabelService;
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
 * @author syt
 * @Date 2021/7/25 11:44
 * @Version 1.0
 */
@RequestMapping("/mwapi/assets")
@Controller
@Slf4j
@Api(value = "资产标签", tags = "资产标签查询")
public class MwAssetsLabelController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("MwAssetsLabelController");
    @Autowired
    MwAssetsLabelService mwAssetsLabelService;

    @PostMapping("/getAssetsLabels/browse")
    @ResponseBody
    @ApiOperation("资产标签查询")
    public ResponseBase getAssetsLabels(@RequestBody QueryAssetsLabelParam param) {
        Reply reply;
        try {
            reply = mwAssetsLabelService.selectLabelList(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产标签查询失败", e);
            return setResultFail("资产标签查询失败", "");
        }
    }
}
